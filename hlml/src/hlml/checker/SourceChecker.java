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

import hlml.parser.Node;
import hlml.resolver.Resolution;

/** Checks a source. */
final class SourceChecker {
  /** Checks a source. */
  static Semantic.Source check(Resolution.Source source) {
    SourceChecker checker = new SourceChecker(source);
    return checker.check();
  }

  /** Checked source. */
  private final Resolution.Source source;

  /** Global symbols that were checked. */
  private Map<String, Semantic.Definition> globals;

  /** Global symbols that are being checked. */
  private Set<String> currently_checked;

  /** Constructor. */
  private SourceChecker(Resolution.Source source) {
    this.source = source;
  }

  /** Check the source. */
  private Semantic.Source check() {
    globals = new HashMap<>();
    currently_checked = new HashSet<>();
    Optional<Semantic.Entrypoint> entrypoint = Optional.empty();
    if (source.entrypoint().isPresent()) {
      Node.Entrypoint node = source.entrypoint().get();
      Semantic.Statement body = check_statement(Scope.create(), node.body());
      entrypoint = Optional.of(new Semantic.Entrypoint(body));
    }
    for (String identifier : source.globals().keySet()) {
      find_global(identifier);
    }
    return new Semantic.Source(entrypoint, globals);
  }

  /** Finds a global in the current source and checks it if it is unchecked. */
  private Optional<Semantic.Definition> find_global(String identifier) {
    if (globals.containsKey(identifier)) {
      return Optional.of(globals.get(identifier));
    }
    if (!source.globals().containsKey(identifier)) { return Optional.empty(); }
    Semantic.Definition global =
      check_definition(source.globals().get(identifier));
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
    Semantic.Definition definition = switch (node) {
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
    currently_checked.remove(identifier);
    return definition;
  }

  /** Check a variable definition. */
  private Semantic.Var check_var(Optional<Scope> scope, Node.Var node) {
    if (scope.isEmpty() && node.initial_value().isPresent()) {
      throw source
        .subject(node.initial_value().get())
        .to_diagnostic(
          "error",
          "Global variables cannot have an initial value!")
        .to_exception();
    }
    return new Semantic.Var(
      node.identifier().text(),
      node.initial_value().map(i -> check_expression(scope.get(), i)));
  }

  /** Checks a statement. */
  private Semantic.Statement check_statement(Scope scope, Node.Statement node) {
    return switch (node) {
      case Node.Block block -> {
        Scope inner_scope = scope.create_child();
        List<Semantic.Statement> inner_statements = new ArrayList<>();
        for (Node.Statement i : block.inner_statements()) {
          inner_statements.add(check_statement(inner_scope, i));
        }
        yield new Semantic.Block(inner_statements);
      }
      case Node.Var v -> {
        Semantic.Var local = check_var(Optional.of(scope), v);
        scope.introduce(local);
        yield local;
      }
      case Node.DirectlyAssign a -> {
        Semantic.SymbolAccess access = check_variable_access(scope, a.target());
        if (!(access instanceof Semantic.VariableAccess variable))
          throw source
            .subject(a.target())
            .to_diagnostic("error", "Assigned symbol is not a variable!")
            .to_exception();
        Semantic.Expression new_value = check_expression(scope, a.source());
        yield new Semantic.Assignment(variable, new_value);
      }
      case Node.Discard discard ->
        new Semantic.Discard(check_expression(scope, discard.source()));
      default ->
        throw source
          .subject(node)
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
    };
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
      case Node.SymbolAccess v -> check_variable_access(scope, v);
      case Node.Grouping g -> check_expression(scope, g.grouped());
    };
  }

  /** Folds a binary operation if the operands are constants. */
  private Semantic.Expression fold_binary_operation(
    Semantic.BinaryOperation operation,
    DoubleBinaryOperator operator)
  {
    if (!(operation.left_operand() instanceof Semantic.Constant l
      && operation.right_operand() instanceof Semantic.Constant r))
      return operation;
    return new Semantic.NumberConstant(
      operator.applyAsDouble(l.value(), r.value()));
  }

  /** Folds a unary operation if the operand is constant. */
  private Semantic.Expression fold_unary_operation(
    Semantic.UnaryOperation operation,
    DoubleUnaryOperator operator)
  {
    if (!(operation.operand() instanceof Semantic.Constant o))
      return operation;
    return new Semantic.NumberConstant(operator.applyAsDouble(o.value()));
  }

  /** Checks a variable access. */
  private Semantic.SymbolAccess check_variable_access(
    Scope scope,
    Node.SymbolAccess node)
  {
    Optional<Semantic.Definition> local = scope.find(node.identifier());
    if (local.isPresent()) {
      if (!(local.get() instanceof Semantic.Var accessed)) {
        throw source
          .subject(node)
          .to_diagnostic(
            "error",
            "Accessed symbol `%s` is not a variable!",
            node.identifier())
          .to_exception();
      }
      return new Semantic.LocalVariableAccess(accessed.identifier());
    }
    Optional<Semantic.Definition> global = find_global(node.identifier());
    if (global.isPresent()) {
      if (global.get() instanceof Semantic.Const accessed) {
        return new Semantic.ConstantAccess(accessed.value());
      }
      if (!(global.get() instanceof Semantic.Var accessed)) {
        throw source
          .subject(node)
          .to_diagnostic(
            "error",
            "Accessed symbol `%s` is not a variable!",
            node.identifier())
          .to_exception();
      }
      return new Semantic.GlobalVariableAccess(
        new Name(source.name(), accessed.identifier()));
    }
    throw source
      .subject(node)
      .to_diagnostic(
        "error",
        "Could not find a symbol named `%s`!",
        node.identifier())
      .to_exception();
  }
}
