package hlml.launcher;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import hlml.builder.Builder;
import hlml.checker.Checker;
import hlml.checker.Semantic;
import hlml.reporter.Subject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
  name = "hlml",
  version = "0.1.0",
  description = "High Level Mindustry Logic Compiler",
  mixinStandardHelpOptions = true)
final class Launcher implements Callable<Integer> {
  /** Launches after parsing commands. */
  public static void main(String... arguments) {
    System.exit(new CommandLine(new Launcher()).execute(arguments));
  }

  @Option(
    names = "-o",
    defaultValue = "a.mlog",
    description = "File the compiled instructions will be saved to.")
  private Path output_path;

  @Option(
    names = "-I",
    defaultValue = ".",
    description = "A directory to look for source files.")
  private List<Path> includes;

  @Parameters(description = "Name of the compiled source.")
  private String name;

  @Override
  public Integer call() {
    try {
      Subject subject = Subject.of("compiler");
      Semantic.Target target =
        Checker.check(subject, includes, name, Optional.empty());
      Builder.build(subject, output_path, target);
      return 0;
    }
    catch (Throwable cause) {
      while (cause != null) {
        System.err.println(cause.getMessage());
        cause = cause.getCause();
      }
      return -1;
    }
  }
}
