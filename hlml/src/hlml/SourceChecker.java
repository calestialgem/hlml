package hlml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

  /** Definitions that are being checked. */
  private Set<Node.Definition> currently_checked;

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
    for (String identifier : source.globals().keySet())
      find_global(source.subject(source.globals().get(identifier)), identifier);
    return new Semantic.Source(entrypoint, globals);
  }

  /** Finds a global in the current source and checks it if it is unchecked. */
  private Semantic.Definition find_global(Subject subject, String identifier) {
    if (globals.containsKey(identifier))
      return globals.get(identifier);
    if (!source.globals().containsKey(identifier)) {
      throw subject
        .to_diagnostic(
          "error",
          "Could not find a global named `%s` in the current source!",
          identifier)
        .to_exception();
    }
    Semantic.Definition global =
      check_definition(subject, source.globals().get(identifier));
    globals.put(identifier, global);
    return global;
  }

  /** Check a definition. */
  private Semantic.Definition check_definition(
    Subject subject,
    Node.Definition node)
  {
    if (currently_checked.contains(node)) {
      throw subject
        .to_diagnostic(
          "error",
          "Cyclic definition with `%s`!",
          node.identifier())
        .to_exception();
    }
    currently_checked.add(node);
    Semantic.Definition definition = switch (node) {
      case Node.Var var ->
        new Semantic.Var(
          var.identifier(),
          check_expression(Scope.create(), var.initial_value()));
    };
    currently_checked.remove(node);
    return definition;
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
      case Node.Local local -> {
        Semantic.Definition definition =
          check_definition(source.subject(node), local.definition());
        scope.introduce(definition);
        yield new Semantic.Local(definition);
      }
      case Node.Discard discard ->
        new Semantic.Discard(check_expression(scope, discard.discarded()));
    };
  }

  /** Checks an expression. */
  private Semantic.Expression check_expression(
    Scope scope,
    Node.Expression node)
  {
    return switch (node) {
      case Node.EqualTo(var l, var r) ->
        new Semantic.EqualTo(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.NotEqualTo(var l, var r) ->
        new Semantic.NotEqualTo(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.StrictlyEqualTo(var l, var r) ->
        new Semantic.StrictlyEqualTo(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.LessThan(var l, var r) ->
        new Semantic.LessThan(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.LessThanOrEqualTo(var l, var r) ->
        new Semantic.LessThanOrEqualTo(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.GreaterThan(var l, var r) ->
        new Semantic.GreaterThan(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.GreaterThanOrEqualTo(var l, var r) ->
        new Semantic.GreaterThanOrEqualTo(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.BitwiseOr(var l, var r) ->
        new Semantic.BitwiseOr(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.BitwiseXor(var l, var r) ->
        new Semantic.BitwiseXor(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.BitwiseAnd(var l, var r) ->
        new Semantic.BitwiseAnd(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.LeftShift(var l, var r) ->
        new Semantic.LeftShift(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.RightShift(var l, var r) ->
        new Semantic.RightShift(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.Addition(var l, var r) ->
        new Semantic.Addition(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.Subtraction(var l, var r) ->
        new Semantic.Subtraction(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.Multiplication(var l, var r) ->
        new Semantic.Multiplication(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.Division(var l, var r) ->
        new Semantic.Division(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.IntegerDivision(var l, var r) ->
        new Semantic.IntegerDivision(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.Modulus(var l, var r) ->
        new Semantic.Modulus(
          check_expression(scope, l),
          check_expression(scope, r));
      case Node.Promotion(var o) ->
        new Semantic.Promotion(check_expression(scope, o));
      case Node.Negation(var o) ->
        new Semantic.Negation(check_expression(scope, o));
      case Node.BitwiseNot(var o) ->
        new Semantic.BitwiseNot(check_expression(scope, o));
      case Node.LogicalNot(var o) ->
        new Semantic.LogicalNot(check_expression(scope, o));
      case Node.NumberConstant number_constant ->
        new Semantic.NumberConstant(number_constant.value());
      case Node.VariableAccess v -> {
        Optional<Semantic.Definition> local = scope.find(v.identifier());
        if (local.isPresent()) {
          if (!(local.get() instanceof Semantic.Var accessed)) {
            throw source
              .subject(node)
              .to_diagnostic(
                "error",
                "Accessed local `%s` is not a variable!",
                v.identifier())
              .to_exception();
          }
          yield new Semantic.LocalVariableAccess(accessed);
        }
        Semantic.Definition global =
          find_global(source.subject(node), v.identifier());
        if (!(global instanceof Semantic.Var accessed)) {
          throw source
            .subject(node)
            .to_diagnostic(
              "error",
              "Accessed global `%s` is not a variable!",
              v.identifier())
            .to_exception();
        }
        yield new Semantic.GlobalVariableAccess(accessed);
      }
    };
  }
}
