package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Holds the entrypoint. */
final class Main {
  /** Entrypoint of the compiler. */
  public static void main(String... arguments) {
    Main main = new Main();
    main.launch_tests();
  }

  /** Subject that is reported when the launcher fails. */
  private final Subject subject;

  /** Path to the directory that holds test files. */
  private final Path tests;

  /** Test artifact directory. */
  private final Path artifacts;

  /** Test executable source files' directory. */
  private final Path executables;

  /** Test library source files' directory. */
  private final Path libraries;

  /** Test include directories. */
  private final List<Path> includes;

  /** Constructor. */
  private Main() {
    subject = Subject.of("compiler launcher");
    tests = Path.of("tests");
    artifacts = tests.resolve("artifacts");
    executables = tests.resolve("executables");
    libraries = tests.resolve("libraries");
    includes = List.of(executables, libraries);
  }

  /** Tests the compiler by building the executable tests. */
  private void launch_tests() {
    try {
      Files
        .list(executables)
        .map(Path::getFileName)
        .map(Path::toString)
        .filter(n -> n.endsWith(Source.extension))
        .map(n -> n.substring(0, n.length() - Source.extension.length()))
        .forEach(this::launch_test);
    }
    catch (IOException cause) {
      throw subject
        .to_diagnostic("failure", "Could not list the executable tests!")
        .to_exception(cause);
    }
  }

  /** Builds an executable test. */
  private void launch_test(String name) {
    try {
      Path artifacts = this.artifacts.resolve(name);
      Semantic.Target target =
        Checker.check(subject, artifacts, includes, name);
      Builder.build(subject, artifacts, target);
    }
    catch (Throwable exception) {
      exception.printStackTrace();
    }
  }
}
