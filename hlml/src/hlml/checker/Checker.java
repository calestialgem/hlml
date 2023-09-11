package hlml.checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import hlml.Source;
import hlml.reporter.Subject;
import hlml.resolver.ResolvedSource;
import hlml.resolver.Resolver;

/** Semantically analyzes a target. */
public final class Checker {
  /** Checks a target. */
  public static Semantic.Target check(
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

  /** Sources that are being checked. */
  private Set<String> currently_checked;

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
    sources
      .put(
        Semantic.built_in_scope,
        new Semantic.Source(
          Optional.empty(),
          Set
            .of(
              new Semantic.Read(),
              new Semantic.Write(),
              new Semantic.DrawClear(),
              new Semantic.DrawColor(),
              new Semantic.DrawCol(),
              new Semantic.DrawStroke(),
              new Semantic.DrawLine(),
              new Semantic.DrawRect(),
              new Semantic.DrawLineRect(),
              new Semantic.DrawPoly(),
              new Semantic.DrawLinePoly(),
              new Semantic.DrawTriangle(),
              new Semantic.DrawImage(),
              new Semantic.DrawFlush(),
              new Semantic.PackColor(),
              new Semantic.Print(),
              new Semantic.PrintFlush(),
              new Semantic.Getlink())
            .stream()
            .collect(
              Collectors
                .toMap(d -> d.name().identifier(), Function.identity()))));
    currently_checked = new HashSet<>();
    check_source(subject, name);
    Semantic.Target target = new Semantic.Target(name, sources);
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

  /** Find a global symbol. */
  private Semantic.Definition find_global(Subject subject, Name name) {
    Semantic.Source source = check_source(subject, name.source());
    if (source.globals().containsKey(name.identifier())) {
      return source.globals().get(name.identifier());
    }
    throw subject
      .to_diagnostic(
        "error",
        "Could not find the symbol `%s::%s`!",
        name.source(),
        name.identifier())
      .to_exception();
  }

  /** Check a source file. */
  private Semantic.Source check_source(Subject subject, String name) {
    if (sources.containsKey(name)) { return sources.get(name); }
    if (currently_checked.contains(name)) {
      throw subject
        .to_diagnostic("error", "Cyclic definition with `%s`!", name)
        .to_exception();
    }
    currently_checked.add(name);
    Path file = find_source(subject, name);
    ResolvedSource resolution = Resolver.resolve(file, artifacts);
    Semantic.Source source = SourceChecker.check(resolution, this::find_global);
    sources.put(name, source);
    currently_checked.remove(name);
    return source;
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
