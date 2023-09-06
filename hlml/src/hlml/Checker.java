package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Semantically analyzes a target. */
final class Checker {
  /** Checks a target. */
  static Semantic.Target check(
    Subject subject,
    Path artifacts,
    List<Path> includes,
    String name)
  {
    Checker checker = new Checker(subject, artifacts, includes, name);
    return checker.check();
  }

  /** Subject that is reported when the target is not found. */
  private final Subject subject;

  /** Path to the directory where compilation artifacts can be recorded to. */
  private final Path artifacts;

  /** Ordered collection of directories to look for a source file by its
   * name. */
  private final List<Path> includes;

  /** Name of the checked target. */
  private final String name;

  /** Checked sources depended on by the target. */
  private Map<String, Semantic.Source> sources;

  /** Entrypoint declaration if there is one in the target. */
  private Optional<Semantic.Entrypoint> entrypoint;

  /** Currently checked source. */
  private Resolution.Source source;

  /** Definitions in the currently checked source. */
  private Map<String, Semantic.Definition> globals;

  /** Constructor. */
  private Checker(
    Subject subject,
    Path artifacts,
    List<Path> includes,
    String name)
  {
    this.subject = subject;
    this.artifacts = artifacts;
    this.includes = includes;
    this.name = name;
  }

  /** Checks the target. */
  private Semantic.Target check() {
    try {
      Files.createDirectories(artifacts);
    }
    catch (IOException cause) {
      throw Subject
        .of(artifacts)
        .to_diagnostic("failure", "Could not create the artifact directory!")
        .to_exception(cause);
    }
    sources = new HashMap<>();
    entrypoint = Optional.empty();
    check_source(subject, name);
    Semantic.Target target = new Semantic.Target(name, sources, entrypoint);
    Path target_artifact_path =
      artifacts.resolve("%s.%s%s".formatted(name, "target", Source.extension));
    try {
      Files.writeString(target_artifact_path, target.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(target_artifact_path)
        .to_diagnostic(
          "failure",
          "Could not record the target of source `%s`",
          name)
        .to_exception(cause);
    }
    return target;
  }

  /** Check a source file. */
  private Semantic.Source check_source(Subject subject, String name) {
    if (sources.containsKey(name)) { return sources.get(name); }
    Path file = find_source(subject, name);
    source = Resolver.resolve(file, artifacts);
    globals = new HashMap<>();
    if (source.entrypoint().isPresent()) {
      Node.Entrypoint node = source.entrypoint().get();
      if (entrypoint.isPresent()) {
        throw source
          .subject(node)
          .to_diagnostic(
            "error",
            "Redeclaration of the entrypoint in the target!")
          .to_exception();
      }
      entrypoint =
        Optional.of(new Semantic.Entrypoint(check_statement(node.body())));
    }
    for (Node.Definition node : source.globals().values()) {
      Semantic.Definition definition = check_definition(node);
      globals.put(definition.identifier(), definition);
    }
    Semantic.Source checked_source = new Semantic.Source(globals);
    sources.put(name, checked_source);
    return checked_source;
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

  /** Find a source file. */
  private Path find_source(Subject subject, String name) {
    String full_name = name + Source.extension;
    for (Path site : includes) {
      Path file = site.resolve(full_name);
      if (Files.exists(file)) { return file; }
    }
    throw subject
      .to_diagnostic(
        "error",
        "Could not find a source named `%s` in the fallowing directories: `%s`!",
        name,
        includes)
      .to_exception();
  }
}
