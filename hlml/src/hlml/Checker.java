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

  /** Declarations in the currently checked source. */
  private Map<String, Semantic.Declaration> declarations;

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
    Resolution.Source resolved_source = Resolver.resolve(file, artifacts);
    declarations = new HashMap<>();
    for (Resolution.Declaration resolution : resolved_source
      .declarations()
      .values())
    {
      Semantic.Declaration declaration = check_declaration(resolution);
      switch (declaration) {
        case Semantic.Entrypoint entrypoint -> {
          if (this.entrypoint.isPresent())
            throw resolved_source
              .subject(resolution.node())
              .to_diagnostic(
                "error",
                "Redeclaration of the entrypoint in the target!")
              .to_exception();
          this.entrypoint = Optional.of(entrypoint);
        }
      }
    }
    Semantic.Source source = new Semantic.Source(declarations);
    sources.put(name, source);
    return source;
  }

  /** Check a declaration. */
  private Semantic.Declaration check_declaration(
    Resolution.Declaration resolution)
  {
    return switch (resolution) {
      case Resolution.Entrypoint entrypoint ->
        new Semantic.Entrypoint(check_statement(entrypoint.node().body()));
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
    };
  }

  /** Checks an expression. */
  private Semantic.Expression check_expression(Node.Expression node) {
    return switch (node) {
      case Node.NumberConstant number_constant ->
        new Semantic.NumberConstant(number_constant.value());
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
