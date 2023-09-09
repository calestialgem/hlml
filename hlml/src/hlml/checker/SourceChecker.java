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
import java.util.stream.Stream;

import hlml.lexer.Token;
import hlml.parser.Node;
import hlml.resolver.ResolvedSource;

/** Checks a source. */
final class SourceChecker {
  /** Checks a source. */
  static Semantic.Source check(ResolvedSource source) {
    SourceChecker checker = new SourceChecker(source);
    return checker.check();
  }

  /** Checked source. */
  private final ResolvedSource source;

  /** Global symbols that were checked. */
  private Map<String, Semantic.Definition> globals;

  /** Global symbols that are being checked. */
  private Set<String> currently_checked;

  /** Representative text of the currently checked global entity. Used for
   * reporting locals with a namespace under the representative. */
  private String representative;

  /** Constructor. */
  private SourceChecker(ResolvedSource source) {
    this.source = source;
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
        check_statement(Scope.create(), false, node.body());
      entrypoint = Optional.of(new Semantic.Entrypoint(body));
    }
    for (String identifier : source.globals.keySet()) {
      find_global(identifier);
    }
    return new Semantic.Source(entrypoint, globals);
  }

  /** Checks a mention. */
  private Optional<Semantic.Definition> find_global(Node.Mention node) {
    if (node.source().isPresent()
      && !node.source().get().text().equals(source.name()))
      throw source
        .subject(node)
        .to_diagnostic("failure", "Unimplemented!")
        .to_exception();
    String identifier = node.identifier().text();
    return find_global(identifier);
  }

  /** Finds a global in the current source and checks it if it is unchecked. */
  private Optional<Semantic.Definition> find_global(String identifier) {
    if (globals.containsKey(identifier)) {
      return Optional.of(globals.get(identifier));
    }
    if (!source.globals.containsKey(identifier)) { return Optional.empty(); }
    Semantic.Definition global =
      check_definition(source.globals.get(identifier));
    globals.put(identifier, global);
    return Optional.of(global);
  }

  /** Check a definition. */
  private Semantic.Definition check_definition(Node.Definition node) {
    String identifier = node.identifier().text();
    if (currently_checked.contains(identifier)) {
      throw source
        .subject(node)
        .to_diagnostic("error", "Cyclic definition with `%s`!", identifier)
        .to_exception();
    }
    currently_checked.add(identifier);
    String old_representative = representative;
    representative = source.representative_text(node);
    Semantic.Definition definition = switch (node) {
      case Node.Using d ->
        throw source
          .subject(node)
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
      case Node.Proc d -> {
        Scope scope = Scope.create();
        for (Token.Identifier p : d.parameters()) {
          Node.Var local = new Node.Var(p, Optional.empty());
          check_local(scope, local);
        }
        Semantic.Statement body = check_statement(scope, false, d.body());
        yield new Semantic.Proc(
          identifier,
          d.parameters().stream().map(Token.Identifier::text).toList(),
          body);
      }
      case Node.Const c -> {
        Semantic.Expression value = check_expression(Scope.create(), c.value());
        if (!(value instanceof Semantic.Constant constant)) {
          throw source
            .subject(c.value())
            .to_diagnostic("error", "Constant's value must be a constant!")
            .to_exception();
        }
        yield new Semantic.Const(identifier, constant.value());
      }
      case Node.Var var -> check_var(Optional.empty(), var);
    };
    representative = old_representative;
    currently_checked.remove(identifier);
    return definition;
  }

  /** Check a variable definition. */
  private Semantic.Var check_var(Optional<Scope> scope, Node.Var node) {
    Optional<Semantic.Expression> initial_value =
      node
        .initial_value()
        .map(i -> check_expression(scope.orElseGet(Scope::create), i));
    if (scope.isEmpty()
      && initial_value.isPresent()
      && !(initial_value.get() instanceof Semantic.Constant))
    {
      throw source
        .subject(node.initial_value().get())
        .to_diagnostic(
          "error",
          "Global variables cannot have a non-constant expressions as initial values!")
        .to_exception();
    }
    return new Semantic.Var(node.identifier().text(), initial_value);
  }

  /** Checks a statement. */
  private Semantic.Statement check_statement(
    Scope scope,
    boolean in_loop,
    Node.Statement node)
  {
    return switch (node) {
      case Node.Block block -> {
        Scope inner_scope = scope.create_child();
        List<Semantic.Statement> inner_statements = new ArrayList<>();
        for (Node.Statement i : block.inner_statements()) {
          inner_statements.add(check_statement(inner_scope, in_loop, i));
        }
        yield new Semantic.Block(inner_statements);
      }
      case Node.If s -> {
        Semantic.Expression condition = check_expression(scope, s.condition());
        Semantic.Statement true_branch =
          check_statement(scope.create_child(), in_loop, s.true_branch());
        Optional<Semantic.Statement> false_branch = Optional.empty();
        if (s.false_branch().isPresent()) {
          Semantic.Statement checked_branch =
            check_statement(
              scope.create_child(),
              in_loop,
              s.false_branch().get());
          false_branch = Optional.of(checked_branch);
        }
        yield new Semantic.If(condition, true_branch, false_branch);
      }
      case Node.While s -> {
        Semantic.Expression condition = check_expression(scope, s.condition());
        Optional<Semantic.Statement> interleaved = Optional.empty();
        if (s.interleaved().isPresent()) {
          Semantic.Statement checked_branch =
            check_statement(
              scope.create_child(),
              in_loop,
              s.interleaved().get());
          interleaved = Optional.of(checked_branch);
        }
        Semantic.Statement loop =
          check_statement(scope.create_child(), true, s.loop());
        Optional<Semantic.Statement> zero_branch = Optional.empty();
        if (s.zero_branch().isPresent()) {
          Semantic.Statement checked_branch =
            check_statement(
              scope.create_child(),
              in_loop,
              s.zero_branch().get());
          zero_branch = Optional.of(checked_branch);
        }
        yield new Semantic.While(condition, interleaved, loop, zero_branch);
      }
      case Node.Break s -> {
        if (!in_loop) {
          throw source
            .subject(node)
            .to_diagnostic("error", "Break statement must be in a loop!")
            .to_exception();
        }
        yield new Semantic.Break();
      }
      case Node.Continue s -> {
        if (!in_loop) {
          throw source
            .subject(node)
            .to_diagnostic("error", "Continue statement must be in a loop!")
            .to_exception();
        }
        yield new Semantic.Continue();
      }
      case Node.Return s ->
        new Semantic.Return(s.value().map(e -> check_expression(scope, e)));
      case Node.Var v -> check_local(scope, v);
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

  /** Introduces a local variable with the given name to the scope. */
  private Semantic.Var check_local(Scope scope, Node.Var node) {
    Optional<Semantic.Var> old_local = scope.find(node.identifier().text());
    if (old_local.isPresent()) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Redeclaration of symbol `%s::%s::%s`",
          source.name(),
          representative,
          node.identifier().text())
        .to_exception();
    }
    Semantic.Var local = check_var(Optional.of(scope), node);
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
        new Semantic.NumberConstant(number_constant.value());
      case Node.SymbolAccess v -> check_symbol_access(scope, v);
      case Node.Grouping g -> check_expression(scope, g.grouped());
      case Node.Call e -> {
        Optional<Semantic.Definition> called = find_global(e.called());
        if (called.isEmpty()) {
          throw source
            .subject(node)
            .to_diagnostic(
              "error",
              "Could not find the symbol named `%s::%s`!",
              source.name(),
              e.called())
            .to_exception();
        }
        yield check_call(called.get(), e.arguments(), scope, node);
      }
      case Node.MemberCall e -> {
        Optional<Semantic.Definition> called = find_global(e.called());
        if (called.isEmpty()) {
          throw source
            .subject(node)
            .to_diagnostic(
              "error",
              "Could not find the symbol named `%s::%s`!",
              source.name(),
              e.called())
            .to_exception();
        }
        List<Node.Expression> arguments =
          Stream
            .concat(
              Stream.of(e.first_argument()),
              e.remaining_arguments().stream())
            .toList();
        yield check_call(called.get(), arguments, scope, node);
      }
    };
  }

  /** Folds a binary operation if the operands are constants. */
  private Semantic.Expression fold_binary_operation(
    Semantic.BinaryOperation operation,
    DoubleBinaryOperator operator)
  {
    if (!(operation.left_operand() instanceof Semantic.Constant l)
      || !(operation.right_operand() instanceof Semantic.Constant r))
    {
      return operation;
    }
    return new Semantic.NumberConstant(
      operator.applyAsDouble(l.value(), r.value()));
  }

  /** Folds a unary operation if the operand is constant. */
  private Semantic.Expression fold_unary_operation(
    Semantic.UnaryOperation operation,
    DoubleUnaryOperator operator)
  {
    if (!(operation.operand() instanceof Semantic.Constant o)) {
      return operation;
    }
    return new Semantic.NumberConstant(operator.applyAsDouble(o.value()));
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
    Optional<Semantic.Var> local =
      scope.find(node.accessed().identifier().text());
    if (local.isPresent()) {
      return new Semantic.LocalVariableAccess(local.get().identifier());
    }
    Optional<Semantic.Definition> global = find_global(node.accessed());
    if (global.isPresent()) {
      if (global.get() instanceof Semantic.Const accessed) {
        return new Semantic.ConstantAccess(accessed.value());
      }
      if (!(global.get() instanceof Semantic.Var accessed)) {
        throw source
          .subject(node)
          .to_diagnostic(
            "error",
            "Accessed symbol `%s::%s` is not a variable!",
            source.name(),
            global.get().identifier())
          .to_exception();
      }
      return new Semantic.GlobalVariableAccess(
        new Name(source.name(), accessed.identifier()));
    }
    throw source
      .subject(node)
      .to_diagnostic(
        "error",
        "Could not find the symbol named `%s::%s`!",
        node
          .accessed()
          .source()
          .map(Token.Identifier::text)
          .orElseGet(source::name),
        node.accessed().identifier().text())
      .to_exception();
  }

  /** Checks a procedure call. */
  private Semantic.Call check_call(
    Semantic.Definition called,
    List<Node.Expression> arguments,
    Scope scope,
    Node.Expression node)
  {
    if (!(called instanceof Semantic.Proc procedure)) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Called symbol `%s::%s` is not a procedure!",
          source.name(),
          called.identifier())
        .to_exception();
    }
    if (procedure.parameters().size() > arguments.size()) {
      throw source
        .subject(node)
        .to_diagnostic(
          "error",
          "Providing more arguments (%d) than parameters (%d) for calling procedure `%s::%s`!",
          arguments.size(),
          procedure.parameters().size(),
          source.name(),
          procedure.identifier())
        .to_exception();
    }
    return new Semantic.Call(
      new Name(source.name(), procedure.identifier()),
      arguments.stream().map(a -> check_expression(scope, a)).toList());
  }
}
