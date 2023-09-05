package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /** Cached resolved sources. Used for skipping the context-free stages when
   * the same source is mentioned multiple times. */
  private Map<String, Resolution.Source> sources;

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
    if (Files.exists(artifacts)) {
      try {
        Files.walkFileTree(artifacts, new Deletor());
      }
      catch (IOException cause) {
        throw Subject
          .of(artifacts)
          .to_diagnostic(
            "failure",
            "Could not delete the existing artifact directory!")
          .to_exception(cause);
      }
    }
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
    resolve_source(subject, name);
    return null;
  }

  /** Resolve a source file. */
  private Resolution.Source resolve_source(Subject subject, String name) {
    if (sources.containsKey(name)) { return sources.get(name); }
    Path file = find_source(subject, name);
    Resolution.Source parcel = Resolver.resolve(file, artifacts);
    sources.put(name, parcel);
    return parcel;
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
