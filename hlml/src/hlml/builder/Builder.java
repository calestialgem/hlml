package hlml.builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.text.DecimalFormat;

import hlml.checker.Name;
import hlml.checker.Semantic;
import hlml.reporter.Subject;

/** Transforms a target to a list of instructions that could be run by a
 * processor. */
public final class Builder {
  /** Assembly file extension. */
  public static final String extension = ".mlog";

  /** Builds a target. Returns the path to the output assembly file. */
  public static Path build(
    Subject subject,
    Path artifacts,
    Semantic.Target target)
  {
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

  /** Tool to write the numbers with necessary precision. */
  private DecimalFormat decimal_formatter;

  /** Global symbols that are already built. */
  private Set<Name> built;

  /** Register set. */
  private Registers registers;

  /** Number of emitted instructions. */
  private int instructions;

  /** Constructor. */
  private Builder(Subject subject, Path artifacts, Semantic.Target target) {
    this.subject = subject;
    this.target = target;
    this.artifacts = artifacts;
  }

  /** Builds the target. */
  @SuppressWarnings("resource")
  private Path build() {
    Optional<Semantic.Entrypoint> entrypoint =
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
    built = new HashSet<>();
    registers = Registers.create();
    instructions = 0;
    for (Name dependency : entrypoint.get().dependencies()) {
      build_dependency(dependency);
    }
    build_statement(entrypoint.get().body());
    build_instruction("end");
    formatter.close();
    if (formatter.ioException() != null) {
      throw Subject
        .of(output)
        .to_diagnostic("failure", "Could not write to the output file!")
        .to_exception(formatter.ioException());
    }
    return output;
  }

  /** Builds a dependency. */
  private void build_dependency(Name name) {
    if (built.contains(name)) { return; }
    built.add(name);
    Semantic.Source source = target.sources().get(name.source());
    Semantic.Definition definition = source.globals().get(name.identifier());
    for (Name dependency : definition.dependencies()) {
      build_dependency(dependency);
    }
    switch (definition) {
      case Semantic.Const c -> {}
      case Semantic.Var v -> registers.global(name);
    }
  }

  /** Builds a statement. */
  private void build_statement(Semantic.Statement statement) {
    switch (statement) {
      case Semantic.Block block ->
        block.inner_statements().forEach(this::build_statement);
      case Semantic.Var l -> {
        Register variable = registers.local(l.identifier());
        if (l.initial_value().isPresent()) {
          Register initial_value = build_expression(l.initial_value().get());
          build_instruction("set", variable, initial_value);
        }
      }
      case Semantic.Increment m -> build_mutate(m, "add");
      case Semantic.Decrement m -> build_mutate(m, "sub");
      case Semantic.DirectlyAssign a -> {
        Register target = build_expression(a.target());
        Register source = build_expression(a.source());
        build_instruction("set", target, source);
      }
      case Semantic.MultiplyAssign a -> build_assign(a, "mul");
      case Semantic.DivideAssign a -> build_assign(a, "div");
      case Semantic.DivideIntegerAssign a -> build_assign(a, "idiv");
      case Semantic.ModulusAssign a -> build_assign(a, "mod");
      case Semantic.AddAssign a -> build_assign(a, "add");
      case Semantic.SubtractAssign a -> build_assign(a, "sub");
      case Semantic.ShiftLeftAssign a -> build_assign(a, "shl");
      case Semantic.ShiftRightAssign a -> build_assign(a, "shr");
      case Semantic.AndBitwiseAssign a -> build_assign(a, "and");
      case Semantic.XorBitwiseAssign a -> build_assign(a, "xor");
      case Semantic.OrBitwiseAssign a -> build_assign(a, "or");
      case Semantic.Discard d ->
        registers.discard(build_expression(d.source()));
      default ->
        throw Subject
          .of("compiler")
          .to_diagnostic("failure", "Unimplemented!")
          .to_exception();
    }
  }

  /** Builds a mutate statement. */
  private void build_mutate(Semantic.Mutate statement, String operation_code) {
    Register target = build_expression(statement.target());
    build_instruction("op", operation_code, target, target);
  }

  /** Builds a assign statement. */
  private void build_assign(Semantic.Assign statement, String operation_code) {
    Register target = build_expression(statement.target());
    Register source = build_expression(statement.source());
    build_instruction("op", operation_code, target, target, source);
  }

  /** Builds an expression. */
  private Register build_expression(Semantic.Expression expression) {
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
        Register operand = build_expression(u.operand());
        Register result = registers.temporary(operand);
        build_instruction("op", "not", result, operand);
        yield result;
      }
      case Semantic.LogicalNot u -> build_unary_operation(u, "notEqual");
      case Semantic.NumberConstant c -> registers.literal(c.value());
      case Semantic.ConstantAccess c -> registers.literal(c.value());
      case Semantic.GlobalVariableAccess g -> registers.global(g.name());
      case Semantic.LocalVariableAccess l -> registers.local(l.identifier());
    };
  }

  /** Builds a binary operation. */
  private Register build_binary_operation(
    Semantic.BinaryOperation operation,
    String operation_code)
  {
    Register left_operand = build_expression(operation.left_operand());
    Register right_operand = build_expression(operation.right_operand());
    Register result = registers.temporary(left_operand, right_operand);
    build_instruction(
      "op",
      operation_code,
      result,
      left_operand,
      right_operand);
    return result;
  }

  /** Builds a unary operation. */
  private Register build_unary_operation(
    Semantic.UnaryOperation operation,
    String operation_code)
  {
    Register operand = build_expression(operation.operand());
    Register result = registers.temporary(operand);
    build_instruction("op", operation_code, result, operand);
    return result;
  }

  /** Builds an instruction. Returns the instruction number. */
  private int build_instruction(
    String instruction,
    String subinstruction,
    Register... operands)
  {
    return build_instruction(
      instruction,
      Optional.of(subinstruction),
      operands);
  }

  /** Builds an instruction. Returns the instruction number. */
  private int build_instruction(String instruction, Register... operands) {
    return build_instruction(instruction, Optional.empty(), operands);
  }

  /** Builds an instruction. Returns the instruction number. */
  private int build_instruction(
    String instruction,
    Optional<String> subinstruction,
    Register... operands)
  {
    formatter.format("%s", instruction);
    if (subinstruction.isPresent()) {
      formatter.format(" %s", subinstruction.get());
    }
    for (Register operand : operands) {
      switch (operand) {
        case Register.Global(var n) ->
          formatter.format(" %s$%s", n.source(), n.identifier());
        case Register.Local(var i) -> formatter.format(" $%s", i);
        case Register.Temporary(var i) -> formatter.format(" $%d", i);
        case Register.Literal(var v) ->
          formatter.format(" %s", decimal_formatter.format(v));
      }
    }
    formatter.format("%n");
    return instructions++;
  }
}
