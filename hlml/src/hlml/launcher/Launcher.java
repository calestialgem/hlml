package hlml.launcher;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Formatter;
import java.util.List;

import hlml.Source;
import hlml.builder.Builder;
import hlml.checker.Checker;
import hlml.checker.Semantic;
import hlml.reporter.Subject;

/** Holds the entrypoint. */
final class Launcher {
  /** Launch option. */
  enum Option {
    build_executables, generate_builtin_variable_test,
  }

  /** Entrypoint of the compiler. */
  public static void main(String... arguments) {
    Launcher launcher = new Launcher();
    launcher.launch(Option.build_executables);
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
  private Launcher() {
    subject = Subject.of("launcher");
    tests = Path.of("tests");
    artifacts = tests.resolve("artifacts");
    executables = tests.resolve("executables");
    libraries = tests.resolve("libraries");
    includes = List.of(executables, libraries);
  }

  private void launch(Option option) {
    switch (option) {
      case build_executables -> launch_tests();
      case generate_builtin_variable_test -> generate_builtin_variable_test();
      default ->
        subject
          .to_diagnostic("failure", "Unknown launch option `%d`!", option)
          .to_exception();
    }
  }

  /** Tests the compiler by building the executable tests. */
  private void launch_tests() {
    try {
      Files.walkFileTree(artifacts, new Deletor());
    }
    catch (IOException cause) {
      throw subject
        .to_diagnostic(
          "failure",
          "Could not delete the existing artifact directory '%s'!",
          artifacts.toAbsolutePath().normalize())
        .to_exception(cause);
    }
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
      Semantic.Target target =
        Checker.check(subject, artifacts, includes, name);
      Builder.build(subject, artifacts, target);
    }
    catch (Throwable exception) {
      exception.printStackTrace();
    }
  }

  /** Creates the built-in variable test. */
  private void generate_builtin_variable_test() {
    try (
      Formatter formatter =
        new Formatter(
          new BufferedOutputStream(
            Files
              .newOutputStream(
                executables.resolve("builtin_variables_test.hlml")))))
    {
      formatter
        .format(
          "# Tests accessing to the variables that are built-in to the processor.%n");
      formatter.format("%n");
      formatter.format("entrypoint {%n");
      formatter.format("  var a;%n");
      List<String> builtin_text =
        Files.readAllLines(Path.of("builtin_variables.txt"));
      for (String builtin : builtin_text) {
        if (builtin.charAt(0) != '@')
          continue;
        String name = builtin.substring(1).replace('-', '_');
        formatter
          .format(
            "%-41s # set a %s%n",
            "  a = mlog::%s;".formatted(name),
            builtin);
      }
      formatter.format("}%n");
    }
    catch (IOException cause) {
      throw subject
        .to_diagnostic(
          "failure",
          "Could not generate the builtin variable test!")
        .to_exception(cause);
    }
  }
}
