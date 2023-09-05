package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Formatter;

/** Transforms a target to a list of instructions that could be run by a
 * processor. */
final class Builder {
  /** Assembly file extension. */
  public static final String extension = ".mlog";

  /** Builds a target. Returns the path to the output assembly file. */
  static Path build(Subject subject, Path artifacts, Semantic.Target target) {
    Builder builder = new Builder(subject, artifacts, target);
    return builder.build();
  }

  /** Subject that is reported when the entrypoint is not found. */
  private final Subject subject;

  /** Path to the directory where compilation artifacts can be recorded to. */
  private final Path artifacts;

  /** Target that is built. */
  private final Semantic.Target target;

  /** Tool to write to the output file. */
  private Formatter formatter;

  /** Tool to format numbers. */
  private DecimalFormat decimal_formatter;

  /** Number of unique registers used through the program. */
  private int register_count;

  /** Constructor. */
  private Builder(Subject subject, Path artifacts, Semantic.Target target) {
    this.subject = subject;
    this.target = target;
    this.artifacts = artifacts;
  }

  /** Builds the target. */
  @SuppressWarnings("resource")
  private Path build() {
    if (target.entrypoint().isEmpty()) {
      throw subject
        .to_diagnostic("error", "There is no entrypoint in the target!")
        .to_exception();
    }
    Path output = artifacts.resolve("%s%s".formatted(target.name(), extension));
    try {
      formatter = new Formatter(Files.newOutputStream(output));
    }
    catch (IOException cause) {
      throw Subject
        .of(output)
        .to_diagnostic("failure", "Could not write to the output file!")
        .to_exception(cause);
    }
    decimal_formatter = new DecimalFormat("0.#");
    decimal_formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
    register_count = 0;
    build_statement(target.entrypoint().get().body());
    formatter.format("end%n");
    formatter.close();
    if (formatter.ioException() != null) {
      throw Subject
        .of(output)
        .to_diagnostic("failure", "Could not write to the output file!")
        .to_exception(formatter.ioException());
    }
    return output;
  }

  /** Builds a statement. */
  private void build_statement(Semantic.Statement statement) {
    switch (statement) {
      case Semantic.Block block ->
        block.inner_statements().forEach(this::build_statement);
      case Semantic.Discard discard -> build_expression(discard.discarded());
    }
  }

  /** Builds an expression. */
  private int build_expression(Semantic.Expression expression) {
    return switch (expression) {
      case Semantic.NumberConstant number_constant -> {
        formatter
          .format(
            "set r%d %s%n",
            register_count,
            decimal_formatter.format(number_constant.value()));
        yield register_count;
      }
      default ->
        throw Subject
          .of("compiler")
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
    };
  }
}
