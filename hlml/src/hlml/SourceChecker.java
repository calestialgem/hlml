package hlml;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Checks a source. */
final class SourceChecker {
  /** Checks a source. */
  static Semantic.Source check(Resolution.Source source) {
    SourceChecker checker = new SourceChecker(source);
    return checker.check();
  }

  /** Checked source. */
  private final Resolution.Source source;

  /** Definitions that were checked. */
  private Map<String, Semantic.Definition> globals;

  /** Constructor. */
  private SourceChecker(Resolution.Source source) {
    this.source = source;
  }

  /** Check the source. */
  private Semantic.Source check() {
    globals = new HashMap<>();
    Optional<Semantic.Entrypoint> entrypoint = Optional.empty();
    if (source.entrypoint().isPresent()) {
      Node.Entrypoint node = source.entrypoint().get();
      Semantic.Statement body = check_statement(node.body());
      entrypoint = Optional.of(new Semantic.Entrypoint(body));
    }
    for (Node.Definition node : source.globals().values()) {
      Semantic.Definition definition = check_definition(node);
      globals.put(definition.identifier(), definition);
    }
    return new Semantic.Source(entrypoint, globals);
  }

  /** Check a definition. */
  private Semantic.Definition check_definition(Node.Definition node) {
    return switch (node) {
      case Node.Var var ->
        new Semantic.Var(
          var.identifier(),
          check_expression(var.initial_value()));
    };
  }

  /** Checks a statement. */
  private Semantic.Statement check_statement(Node.Statement node) {
    return switch (node) {
      case Node.Block block ->
        new Semantic.Block(
          block
            .inner_statements()
            .stream()
            .map(this::check_statement)
            .toList());
      case Node.Discard discard ->
        new Semantic.Discard(check_expression(discard.discarded()));
      default ->
        throw source
          .subject(node)
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
    };
  }

  /** Checks an expression. */
  private Semantic.Expression check_expression(Node.Expression node) {
    return switch (node) {
      case Node.EqualTo(var l, var r) ->
        new Semantic.EqualTo(check_expression(l), check_expression(r));
      case Node.NotEqualTo(var l, var r) ->
        new Semantic.NotEqualTo(check_expression(l), check_expression(r));
      case Node.StrictlyEqualTo(var l, var r) ->
        new Semantic.StrictlyEqualTo(check_expression(l), check_expression(r));
      case Node.LessThan(var l, var r) ->
        new Semantic.LessThan(check_expression(l), check_expression(r));
      case Node.LessThanOrEqualTo(var l, var r) ->
        new Semantic.LessThanOrEqualTo(
          check_expression(l),
          check_expression(r));
      case Node.GreaterThan(var l, var r) ->
        new Semantic.GreaterThan(check_expression(l), check_expression(r));
      case Node.GreaterThanOrEqualTo(var l, var r) ->
        new Semantic.GreaterThanOrEqualTo(
          check_expression(l),
          check_expression(r));
      case Node.BitwiseOr(var l, var r) ->
        new Semantic.BitwiseOr(check_expression(l), check_expression(r));
      case Node.BitwiseXor(var l, var r) ->
        new Semantic.BitwiseXor(check_expression(l), check_expression(r));
      case Node.BitwiseAnd(var l, var r) ->
        new Semantic.BitwiseAnd(check_expression(l), check_expression(r));
      case Node.LeftShift(var l, var r) ->
        new Semantic.LeftShift(check_expression(l), check_expression(r));
      case Node.RightShift(var l, var r) ->
        new Semantic.RightShift(check_expression(l), check_expression(r));
      case Node.Addition(var l, var r) ->
        new Semantic.Addition(check_expression(l), check_expression(r));
      case Node.Subtraction(var l, var r) ->
        new Semantic.Subtraction(check_expression(l), check_expression(r));
      case Node.Multiplication(var l, var r) ->
        new Semantic.Multiplication(check_expression(l), check_expression(r));
      case Node.Division(var l, var r) ->
        new Semantic.Division(check_expression(l), check_expression(r));
      case Node.IntegerDivision(var l, var r) ->
        new Semantic.IntegerDivision(check_expression(l), check_expression(r));
      case Node.Modulus(var l, var r) ->
        new Semantic.Modulus(check_expression(l), check_expression(r));
      case Node.Promotion(var o) -> new Semantic.Promotion(check_expression(o));
      case Node.Negation(var o) -> new Semantic.Negation(check_expression(o));
      case Node.BitwiseNot(var o) ->
        new Semantic.BitwiseNot(check_expression(o));
      case Node.LogicalNot(var o) ->
        new Semantic.LogicalNot(check_expression(o));
      case Node.NumberConstant number_constant ->
        new Semantic.NumberConstant(number_constant.value());
      default ->
        throw source
          .subject(node)
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
    };
  }
}
