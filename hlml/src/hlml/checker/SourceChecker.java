package hlml.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import hlml.lexer.Token;
import hlml.parser.Node;
import hlml.resolver.ResolvedSource;

/** Checks a source. */
final class SourceChecker {
  /** Checks a source. */
  static Semantic.Source check(ResolvedSource source, GlobalFinder finder) {
    SourceChecker checker = new SourceChecker(source, finder);
    return checker.check();
  }

  /** Checked source. */
  private final ResolvedSource source;

  /** Global finder. */
  private final GlobalFinder finder;

  /** Global symbols that were checked. */
  private Map<String, Semantic.Definition> globals;

  /** Global symbols that are being checked. */
  private Set<String> currently_checked;

  /** Representative text of the currently checked global entity. Used for
   * reporting locals with a namespace under the representative. */
  private String representative;

  /** Constructor. */
  private SourceChecker(ResolvedSource source, GlobalFinder finder) {
    this.source = source;
    this.finder = finder;
  }

  /** Check the source. */
  private Semantic.Source check() {
    globals = new HashMap<>();
    currently_checked = new HashSet<>();
    Optional<Semantic.Entrypoint> entrypoint = Optional.empty();
    if (source.entrypoint.isPresent()) {
      Node.Entrypoint node = source.entrypoint.get();
      representative = source.representative_text(node);
      Semantic.Statement body =
        check_statement(Scope.create(), new ArrayList<>(), node.body());
      entrypoint = Optional.of(new Semantic.Entrypoint(body));
    }
    for (String identifier : source.globals.keySet()) {
      find_global(identifier);
    }
    return new Semantic.Source(entrypoint, globals);
  }

  /** Checks a mention. */
  private Semantic.Definition check_mention(Node.Mention node) {
    if (node.source().isPresent()) {
      String scope = node.source().get().text();
      if (!scope.equals(source.name())) {
        Name name = new Name(scope, node.identifier().text());
        return finder.find(source.subject(node), name);
      }
    }
    return check_identifier(node.identifier());
  }

  /** Checks an identifier. */
  private Semantic.Definition check_identifier(Token.Identifier identifier) {
    Optional<Semantic.Definition> global = find_global(identifier.text());
    if (!global.isPresent()) {
      throw source
        .subject(identifier)
        .to_diagnostic(
          "error",
          "Could not find the symbol `%s::%s`",
          source.name(),
          identifier.text())
        .to_exception();
    }
    Semantic.Definition definition = global.get();
    if (definition instanceof Semantic.Using using)
      return using.aliased();
    return definition;

  }

  /** Finds a global in the current source and checks it if it is unchecked. */
  private Optional<Semantic.Definition> find_global(String identifier) {
    if (globals.containsKey(identifier)) {
      return Optional.of(globals.get(identifier));
    }
    if (!source.globals.containsKey(identifier)) { return Optional.empty(); }
    Node.Definition node = source.globals.get(identifier);
    if (currently_checked.contains(identifier)) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Cyclic definition with `%s::%s`!",
          source.name(),
          identifier)
        .to_exception();
    }
    currently_checked.add(identifier);
    String old_representative = representative;
    representative = source.representative_text(node);
    Semantic.Definition definition = switch (node) {
      case Node.Link d ->
        new Semantic.Link(
          node.modifier().isPresent(),
          new Name(source.name(), identifier),
          d.building().text());
      case Node.Using d ->
        new Semantic.Using(
          node.modifier().isPresent(),
          new Name(source.name(), identifier),
          check_mention(d.used()));
      case Node.Proc d -> {
        Scope scope = Scope.create();
        for (Node.Parameter p : d.parameters()) {
          Node.LocalVar local =
            new Node.LocalVar(p.identifier(), Optional.empty());
          check_local(scope, local);
        }
        Semantic.Statement body =
          check_statement(scope, new ArrayList<>(), d.body());
        yield new Semantic.UserDefinedProcedure(
          node.modifier().isPresent(),
          new Name(source.name(), identifier),
          d
            .parameters()
            .stream()
            .map(p -> new Semantic.Parameter(p.identifier().text(), p.in_out()))
            .toList(),
          body);
      }
      case Node.Const c -> {
        Semantic.Expression value = check_expression(Scope.create(), c.value());
        if (!(value instanceof Semantic.Known constant)) {
          throw source
            .subject(c.value())
            .to_diagnostic(
              "error",
              "Constants' values must be known in compile-time!")
            .to_exception();
        }
        yield new Semantic.UserDefinedConstant(
          node.modifier().isPresent(),
          new Name(source.name(), identifier),
          constant);
      }
      case Node.GlobalVar var -> {
        Optional<Semantic.Expression> initial_value =
          var.initial_value().map(i -> check_expression(Scope.create(), i));
        if (initial_value.isPresent()
          && !(initial_value.get() instanceof Semantic.Known))
        {
          throw source
            .subject(var.initial_value().get())
            .to_diagnostic(
              "error",
              "Global variables cannot have a non-constant expressions as initial values!")
            .to_exception();
        }
        yield new Semantic.GlobalVar(
          node.modifier().isPresent(),
          new Name(source.name(), var.identifier().text()),
          initial_value);
      }
    };
    representative = old_representative;
    globals.put(identifier, definition);
    currently_checked.remove(identifier);
    return Optional.of(definition);
  }

  /** Checks a statement. */
  private Semantic.Statement check_statement(
    Scope scope,
    List<Optional<String>> loops,
    Node.Statement node)
  {
    return switch (node) {
      case Node.Block block -> {
        Scope inner_scope = scope.create_child();
        List<Semantic.Statement> inner_statements = new ArrayList<>();
        for (Node.Statement i : block.inner_statements()) {
          inner_statements.add(check_statement(inner_scope, loops, i));
        }
        yield new Semantic.Block(inner_statements);
      }
      case Node.If s -> {
        Scope inner = scope.create_child();
        List<Semantic.LocalVar> variables =
          check_variables(inner, s.variables());
        Semantic.Expression condition = check_expression(inner, s.condition());
        Semantic.Statement true_branch =
          check_statement(inner.create_child(), loops, s.true_branch());
        Optional<Semantic.Statement> false_branch = Optional.empty();
        if (s.false_branch().isPresent()) {
          Semantic.Statement checked_branch =
            check_statement(
              inner.create_child(),
              loops,
              s.false_branch().get());
          false_branch = Optional.of(checked_branch);
        }
        yield new Semantic.If(variables, condition, true_branch, false_branch);
      }
      case Node.While s -> {
        Scope inner = scope.create_child();
        List<Semantic.LocalVar> variables =
          check_variables(inner, s.variables());
        Semantic.Expression condition = check_expression(inner, s.condition());
        Optional<Semantic.Statement> interleaved = Optional.empty();
        if (s.interleaved().isPresent()) {
          Semantic.Statement checked_branch =
            check_statement(inner.create_child(), loops, s.interleaved().get());
          interleaved = Optional.of(checked_branch);
        }
        Optional<String> label = s.label().map(Token.Identifier::text);
        if (label.isPresent() && loops.contains(label)) {
          throw source
            .subject(node)
            .to_diagnostic(
              "error",
              "Redeclaration of the loop labeled `%s::%s::%s`!",
              source.name(),
              representative,
              label.get())
            .to_exception();
        }
        loops.add(label);
        Semantic.Statement loop =
          check_statement(inner.create_child(), loops, s.loop());
        loops.remove(loops.size() - 1);
        Optional<Semantic.Statement> zero_branch = Optional.empty();
        if (s.zero_branch().isPresent()) {
          Semantic.Statement checked_branch =
            check_statement(inner.create_child(), loops, s.zero_branch().get());
          zero_branch = Optional.of(checked_branch);
        }
        yield new Semantic.While(
          variables,
          condition,
          interleaved,
          loop,
          zero_branch);
      }
      case Node.Break s -> {
        int index = loops.lastIndexOf(s.label().map(Token.Identifier::text));
        if (index == -1) {
          if (s.label().isPresent()) {
            throw source
              .subject(s.label().get())
              .to_diagnostic(
                "error",
                "Could not find the loop labeled `%s::%s::%s`!",
                source.name(),
                representative,
                s.label().get().text())
              .to_exception();
          }
          throw source
            .subject(node)
            .to_diagnostic("error", "Break statement must be in a loop!")
            .to_exception();
        }
        yield new Semantic.Break(index);
      }
      case Node.Continue s -> {
        int index = loops.lastIndexOf(s.label().map(Token.Identifier::text));
        if (index == -1) {
          if (s.label().isPresent()) {
            throw source
              .subject(s.label().get())
              .to_diagnostic(
                "error",
                "Could not find the loop labeled `%s::%s::%s`!",
                source.name(),
                representative,
                s.label().get().text())
              .to_exception();
          }
          throw source
            .subject(node)
            .to_diagnostic("error", "Continue statement must be in a loop!")
            .to_exception();
        }
        yield new Semantic.Continue(index);
      }
      case Node.Return s ->
        new Semantic.Return(s.value().map(e -> check_expression(scope, e)));
      case Node.LocalVar v -> check_local(scope, v);
      case Node.Increment m ->
        new Semantic.Increment(check_target(scope, m.target()));
      case Node.Decrement m ->
        new Semantic.Decrement(check_target(scope, m.target()));
      case Node.DirectlyAssign a ->
        new Semantic.DirectlyAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.MultiplyAssign a ->
        new Semantic.MultiplyAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.DivideAssign a ->
        new Semantic.DivideAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.DivideIntegerAssign a ->
        new Semantic.DivideIntegerAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.ModulusAssign a ->
        new Semantic.ModulusAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.AddAssign a ->
        new Semantic.AddAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.SubtractAssign a ->
        new Semantic.SubtractAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.ShiftLeftAssign a ->
        new Semantic.ShiftLeftAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.ShiftRightAssign a ->
        new Semantic.ShiftRightAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.AndBitwiseAssign a ->
        new Semantic.AndBitwiseAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.XorBitwiseAssign a ->
        new Semantic.XorBitwiseAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.OrBitwiseAssign a ->
        new Semantic.OrBitwiseAssign(
          check_target(scope, a.target()),
          check_expression(scope, a.source()));
      case Node.Discard d ->
        new Semantic.Discard(check_expression(scope, d.source()));
    };
  }

  /** Checks inner variables of a statement. */
  private List<Semantic.LocalVar> check_variables(
    Scope scope,
    List<Node.LocalVar> nodes)
  {
    List<Semantic.LocalVar> variables = new ArrayList<>();
    for (Node.LocalVar node : nodes) {
      variables.add(check_local(scope, node));
    }
    return variables;
  }

  /** Introduces a local variable with the given name to the scope. */
  private Semantic.LocalVar check_local(Scope scope, Node.LocalVar node) {
    Optional<Semantic.LocalVar> old_local =
      scope.find(node.identifier().text());
    if (old_local.isPresent()) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Redeclaration of the local symbol `%s::%s::%s`",
          source.name(),
          representative,
          node.identifier().text())
        .to_exception();
    }
    Semantic.LocalVar local =
      new Semantic.LocalVar(
        node.identifier().text(),
        node.initial_value().map(i -> check_expression(scope, i)));
    scope.introduce(local);
    return local;
  }

  /** Checks an expression. */
  private Semantic.Expression check_expression(
    Scope scope,
    Node.Expression node)
  {
    return switch (node) {
      case Node.EqualTo(var l, var r) ->
        fold_binary_operation(
          new Semantic.EqualTo(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> Math.abs(a - b) < 0.000001 ? 1 : 0);
      case Node.NotEqualTo(var l, var r) ->
        fold_binary_operation(
          new Semantic.NotEqualTo(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> Math.abs(a - b) < 0.000001 ? 0 : 1);
      case Node.StrictlyEqualTo(var l, var r) ->
        fold_binary_operation(
          new Semantic.StrictlyEqualTo(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> Math.abs(a - b) < 0.000001 ? 1 : 0);
      case Node.LessThan(var l, var r) ->
        fold_binary_operation(
          new Semantic.LessThan(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a < b ? 1 : 0);
      case Node.LessThanOrEqualTo(var l, var r) ->
        fold_binary_operation(
          new Semantic.LessThanOrEqualTo(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a <= b ? 1 : 0);
      case Node.GreaterThan(var l, var r) ->
        fold_binary_operation(
          new Semantic.GreaterThan(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a > b ? 1 : 0);
      case Node.GreaterThanOrEqualTo(var l, var r) ->
        fold_binary_operation(
          new Semantic.GreaterThanOrEqualTo(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a >= b ? 1 : 0);
      case Node.BitwiseOr(var l, var r) ->
        fold_binary_operation(
          new Semantic.BitwiseOr(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> (long) a | (long) b);
      case Node.BitwiseXor(var l, var r) ->
        fold_binary_operation(
          new Semantic.BitwiseXor(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> (long) a ^ (long) b);
      case Node.BitwiseAnd(var l, var r) ->
        fold_binary_operation(
          new Semantic.BitwiseAnd(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> (long) a & (long) b);
      case Node.LeftShift(var l, var r) ->
        fold_binary_operation(
          new Semantic.LeftShift(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> (long) a << (long) b);
      case Node.RightShift(var l, var r) ->
        fold_binary_operation(
          new Semantic.RightShift(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> (long) a >> (long) b);
      case Node.Addition(var l, var r) ->
        fold_binary_operation(
          new Semantic.Addition(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a + b);
      case Node.Subtraction(var l, var r) ->
        fold_binary_operation(
          new Semantic.Subtraction(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a - b);
      case Node.Multiplication(var l, var r) ->
        fold_binary_operation(
          new Semantic.Multiplication(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a * b);
      case Node.Division(var l, var r) ->
        fold_binary_operation(
          new Semantic.Division(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a / b);
      case Node.IntegerDivision(var l, var r) ->
        fold_binary_operation(
          new Semantic.IntegerDivision(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> Math.floor(a / b));
      case Node.Modulus(var l, var r) ->
        fold_binary_operation(
          new Semantic.Modulus(
            check_expression(scope, l),
            check_expression(scope, r)),
          (a, b) -> a % b);
      case Node.Promotion(var o) ->
        fold_unary_operation(
          new Semantic.Promotion(check_expression(scope, o)),
          a -> a);
      case Node.Negation(var o) ->
        fold_unary_operation(
          new Semantic.Negation(check_expression(scope, o)),
          a -> -a);
      case Node.BitwiseNot(var o) ->
        fold_unary_operation(
          new Semantic.BitwiseNot(check_expression(scope, o)),
          a -> ~(long) a);
      case Node.LogicalNot(var o) ->
        fold_unary_operation(
          new Semantic.LogicalNot(check_expression(scope, o)),
          a -> a != 0 ? 1 : 0);
      case Node.NumberConstant number_constant ->
        new Semantic.KnownNumber(number_constant.value());
      case Node.ColorConstant e -> new Semantic.KnownColor(e.value());
      case Node.StringConstant e -> new Semantic.KnownString(e.value());
      case Node.SymbolAccess v -> check_symbol_access(scope, v);
      case Node.Grouping g -> check_expression(scope, g.grouped());
      case Node.Call e -> {
        yield check_call(check_mention(e.called()), e.arguments(), scope, node);
      }
      case Node.MemberCall e -> {
        Semantic.Definition called = check_identifier(e.called());
        List<Node.Expression> arguments = new ArrayList<>();
        arguments.add(e.first_argument());
        arguments.addAll(e.remaining_arguments());
        yield check_call(called, arguments, scope, node);
      }
      case Node.MemberAccess e -> {
        Semantic.Definition builtin =
          finder
            .find(
              source.subject(e.member()),
              new Name(Semantic.built_in_scope, e.member().text()));
        if (!(builtin instanceof Semantic.BuiltinConstant property)) {
          throw source
            .subject(node)
            .to_diagnostic(
              "error",
              "Member `%s::%s` is not a sensible property!",
              builtin.name().source(),
              builtin.name().identifier())
            .to_exception();
        }
        yield new Semantic.MemberAccess(
          check_expression(scope, e.object()),
          property.value());
      }
    };
  }

  /** Folds a binary operation if the operands are constants. */
  private Semantic.Expression fold_binary_operation(
    Semantic.BinaryOperation operation,
    DoubleBinaryOperator operator)
  {
    if (!(operation.left_operand() instanceof Semantic.KnownNumeric l)
      || !(operation.right_operand() instanceof Semantic.KnownNumeric r))
    {
      return operation;
    }
    return new Semantic.KnownNumber(
      operator.applyAsDouble(l.numeric(), r.numeric()));
  }

  /** Folds a unary operation if the operand is constant. */
  private Semantic.Expression fold_unary_operation(
    Semantic.UnaryOperation operation,
    DoubleUnaryOperator operator)
  {
    if (!(operation.operand() instanceof Semantic.KnownNumeric o)) {
      return operation;
    }
    return new Semantic.KnownNumber(operator.applyAsDouble(o.numeric()));
  }

  /** Checks a variable access. */
  private Semantic.VariableAccess check_target(
    Scope scope,
    Node.SymbolAccess node)
  {
    Semantic.SymbolAccess symbol = check_symbol_access(scope, node);
    if (!(symbol instanceof Semantic.VariableAccess target)) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Accessed symbol `%s::%s` is not mutable!",
          source.name(),
          node.accessed().identifier().text())
        .to_exception();
    }
    return target;
  }

  /** Checks a symbol access. */
  private Semantic.SymbolAccess check_symbol_access(
    Scope scope,
    Node.SymbolAccess node)
  {
    Optional<Semantic.LocalVar> local =
      scope.find(node.accessed().identifier().text());
    if (local.isPresent()) {
      return new Semantic.LocalVariableAccess(local.get().identifier());
    }
    Semantic.Definition global = check_mention(node.accessed());
    return switch (global) {
      case Semantic.Link g -> new Semantic.LinkAccess(g.building());
      case Semantic.Constant g -> g.value();
      case Semantic.GlobalVar g -> new Semantic.GlobalVariableAccess(g.name());
      default ->
        throw source
          .subject(node)
          .to_diagnostic(
            "error",
            "Accessed symbol `%s::%s` is not a variable!",
            global.name().source(),
            global.name().identifier())
          .to_exception();
    };
  }

  /** Checks a procedure call. */
  private Semantic.Call check_call(
    Semantic.Definition called,
    List<Node.Expression> arguments,
    Scope scope,
    Node.Expression node)
  {
    if (!(called instanceof Semantic.Procedure procedure)) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Called symbol `%s::%s` is not a procedure!",
          called.name().source(),
          called.name().identifier())
        .to_exception();
    }
    if (arguments.size() > procedure.parameter_count()) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Providing more arguments (%d) than parameters (%d) for calling procedure `%s::%s`!",
          arguments.size(),
          procedure.parameter_count(),
          procedure.name().source(),
          procedure.name().identifier())
        .to_exception();
    }
    return new Semantic.Call(
      procedure.name(),
      arguments.stream().map(a -> check_expression(scope, a)).toList());
  }
}
