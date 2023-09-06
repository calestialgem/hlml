package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Optional;

import hlml.Semantic.Entrypoint;

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
    Optional<Entrypoint> entrypoint =
      target.sources().get(target.name()).entrypoint();
    if (entrypoint.isEmpty()) {
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
    build_statement(entrypoint.get().body());
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
      case Semantic.Var local ->
        throw Subject
          .of("compiler")
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
      case Semantic.Discard discard -> build_expression(discard.discarded());
    }
  }

  /** Builds an expression. */
  private Evaluation build_expression(Semantic.Expression expression) {
    return switch (expression) {
      case Semantic.EqualTo b -> build_binary_operation(b, "equal");
      case Semantic.NotEqualTo b -> build_binary_operation(b, "notEqual");
      case Semantic.StrictlyEqualTo b ->
        build_binary_operation(b, "strictEqual");
      case Semantic.LessThan b -> build_binary_operation(b, "lessThan");
      case Semantic.LessThanOrEqualTo b ->
        build_binary_operation(b, "lessThanEq");
      case Semantic.GreaterThan b -> build_binary_operation(b, "greaterThan");
      case Semantic.GreaterThanOrEqualTo b ->
        build_binary_operation(b, "greaterThanEq");
      case Semantic.BitwiseOr b -> build_binary_operation(b, "or");
      case Semantic.BitwiseXor b -> build_binary_operation(b, "xor");
      case Semantic.BitwiseAnd b -> build_binary_operation(b, "and");
      case Semantic.LeftShift b -> build_binary_operation(b, "shl");
      case Semantic.RightShift b -> build_binary_operation(b, "shr");
      case Semantic.Addition b -> build_binary_operation(b, "add");
      case Semantic.Subtraction b -> build_binary_operation(b, "sub");
      case Semantic.Multiplication b -> build_binary_operation(b, "mul");
      case Semantic.Division b -> build_binary_operation(b, "div");
      case Semantic.IntegerDivision b -> build_binary_operation(b, "idiv");
      case Semantic.Modulus b -> build_binary_operation(b, "mod");
      case Semantic.Promotion u -> build_unary_operation(u, "add");
      case Semantic.Negation u -> build_unary_operation(u, "sub");
      case Semantic.BitwiseNot u -> {
        Evaluation operand = build_expression(u.operand());
        int result = overwrite_evaluation(operand);
        formatter.format("op not r%d ", result);
        build_evaluation(operand);
        formatter.format(" 0%n");
        yield new Evaluation.Register(result);
      }
      case Semantic.LogicalNot u -> build_unary_operation(u, "notEqual");
      case Semantic.NumberConstant number_constant ->
        new Evaluation.Immediate(number_constant.value());
      case Semantic.LocalVariableAccess l ->
        throw Subject
          .of("compiler")
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
      case Semantic.GlobalVariableAccess g ->
        throw Subject
          .of("compiler")
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
    };
  }

  /** Builds a binary operation. */
  private Evaluation build_binary_operation(
    Semantic.BinaryOperation operation,
    String operation_code)
  {
    Evaluation left_operand = build_expression(operation.left_operand());
    register_count++;
    Evaluation right_operand = build_expression(operation.right_operand());
    register_count--;
    int result = overwrite_evaluation(left_operand);
    formatter.format("op %s r%d ", operation_code, result);
    build_evaluation(left_operand);
    formatter.format(" ");
    build_evaluation(right_operand);
    formatter.format("%n");
    return new Evaluation.Register(result);
  }

  /** Builds a unary operation. */
  private Evaluation build_unary_operation(
    Semantic.UnaryOperation operation,
    String operation_code)
  {
    Evaluation operand = build_expression(operation.operand());
    int result = overwrite_evaluation(operand);
    formatter.format("op %s r%d 0 ", operation_code, result);
    build_evaluation(operand);
    formatter.format("%n");
    return new Evaluation.Register(result);
  }

  /** Builds an evaluation as an instruction parameter. */
  private void build_evaluation(Evaluation evaluation) {
    switch (evaluation) {
      case Evaluation.Register r -> formatter.format("r%d", r.index());
      case Evaluation.Immediate i ->
        formatter.format(decimal_formatter.format(i.value()));
    }
  }

  /** Returns the register index to overwrite the evaluation if it is on a
   * register. */
  private int overwrite_evaluation(Evaluation evaluation) {
    return switch (evaluation) {
      case Evaluation.Register r -> r.index();
      case Evaluation.Immediate i -> register_count;
    };
  }
}
