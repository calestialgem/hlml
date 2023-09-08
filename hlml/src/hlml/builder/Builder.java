package hlml.builder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

  /** Global symbols that are already built. */
  private Set<Name> built;

  /** Instruction list. */
  private Program program;

  /** Temporary register list. */
  private Stack stack;

  /** Waypoint to the currently built loop's condition check's first
   * instruction. Used for building continue statements. */
  private Waypoint loop_begin;

  /** Waypoint to the instruction that is executed after the currently built
   * loop. Used for building break statements. */
  private Waypoint loop_end;

  /** Constructor. */
  private Builder(Subject subject, Path artifacts, Semantic.Target target) {
    this.subject = subject;
    this.target = target;
    this.artifacts = artifacts;
  }

  /** Builds the target. */
  private Path build() {
    Optional<Semantic.Entrypoint> entrypoint =
      target.sources().get(target.name()).entrypoint();
    if (entrypoint.isEmpty()) {
      throw subject
        .to_diagnostic("error", "There is no entrypoint in the target!")
        .to_exception();
    }
    built = new HashSet<>();
    program = Program.create();
    stack = Stack.create();
    for (Name dependency : entrypoint.get().dependencies()) {
      build_dependency(dependency);
    }
    build_statement(entrypoint.get().body());
    program.instruct(new Instruction.End());
    Path output_path =
      artifacts.resolve("%s%s".formatted(target.name(), extension));
    try (
      BufferedWriter output =
        new BufferedWriter(
          new OutputStreamWriter(Files.newOutputStream(output_path))))
    {
      program.append_to(output);
    }
    catch (IOException cause) {
      throw Subject
        .of(output_path)
        .to_diagnostic("failure", "Could not write to the output file!")
        .to_exception(cause);
    }
    return output_path;
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
      case Semantic.Var v -> {}
    }
  }

  /** Builds a statement if it is there. */
  private void build_statement(Optional<Semantic.Statement> statement) {
    statement.ifPresent(this::build_statement);
  }

  /** Builds a statement. */
  private void build_statement(Semantic.Statement statement) {
    switch (statement) {
      case Semantic.Block block ->
        block.inner_statements().forEach(this::build_statement);
      case Semantic.If s -> {
        Register condition = build_expression(s.condition());
        Waypoint after_true_branch = program.waypoint();
        program
          .instruct(new Instruction.JumpOnFalse(after_true_branch, condition));
        stack.pop(condition);
        build_statement(s.true_branch());
        Waypoint after_false_branch = program.waypoint();
        program.instruct(new Instruction.JumpAlways(after_false_branch));
        program.define(after_true_branch);
        build_statement(s.false_branch());
        program.define(after_false_branch);
      }
      case Semantic.While s -> {
        Register first_condition = build_expression(s.condition());
        Waypoint loop = program.waypoint();
        program.instruct(new Instruction.JumpOnTrue(loop, first_condition));
        stack.pop(first_condition);
        build_statement(s.zero_branch());
        loop_end = program.waypoint();
        program.instruct(new Instruction.JumpAlways(loop_end));
        program.define(loop);
        loop_begin = program.waypoint();
        build_statement(s.loop());
        program.define(loop_begin);
        build_statement(s.interleaved());
        Register remaining_conditions = build_expression(s.condition());
        program
          .instruct(new Instruction.JumpOnTrue(loop, remaining_conditions));
        stack.pop(remaining_conditions);
        program.define(loop_end);
      }
      case Semantic.Break s ->
        program.instruct(new Instruction.JumpAlways(loop_end));
      case Semantic.Continue s ->
        program.instruct(new Instruction.JumpAlways(loop_begin));
      case Semantic.Var l -> {
        Register variable = Register.local(l.identifier());
        if (l.initial_value().isPresent()) {
          Register initial_value = build_expression(l.initial_value().get());
          program.instruct(new Instruction.Set(variable, initial_value));
        }
      }
      case Semantic.Increment m -> build_mutate(m, Instruction.Addition::new);
      case Semantic.Decrement m ->
        build_mutate(m, Instruction.Subtraction::new);
      case Semantic.DirectlyAssign a -> {
        Register target = build_expression(a.target());
        Register source = build_expression(a.source());
        program.instruct(new Instruction.Set(target, source));
      }
      case Semantic.MultiplyAssign a ->
        build_assign(a, Instruction.Multiplication::new);
      case Semantic.DivideAssign a ->
        build_assign(a, Instruction.Division::new);
      case Semantic.DivideIntegerAssign a ->
        build_assign(a, Instruction.IntegerDivision::new);
      case Semantic.ModulusAssign a ->
        build_assign(a, Instruction.Modulus::new);
      case Semantic.AddAssign a -> build_assign(a, Instruction.Addition::new);
      case Semantic.SubtractAssign a ->
        build_assign(a, Instruction.Subtraction::new);
      case Semantic.ShiftLeftAssign a ->
        build_assign(a, Instruction.LeftShift::new);
      case Semantic.ShiftRightAssign a ->
        build_assign(a, Instruction.RightShift::new);
      case Semantic.AndBitwiseAssign a ->
        build_assign(a, Instruction.BitwiseAnd::new);
      case Semantic.XorBitwiseAssign a ->
        build_assign(a, Instruction.BitwiseXor::new);
      case Semantic.OrBitwiseAssign a ->
        build_assign(a, Instruction.BitwiseOr::new);
      case Semantic.Discard d -> stack.pop(build_expression(d.source()));
    }
  }

  /** Builds a mutate statement. */
  private void build_mutate(
    Semantic.Mutate statement,
    BinaryOperationInitializer initializer)
  {
    Register target = build_expression(statement.target());
    program
      .instruct(initializer.initialize(target, target, Register.literal(1)));
  }

  /** Builds a assign statement. */
  private void build_assign(
    Semantic.Assign statement,
    BinaryOperationInitializer initializer)
  {
    Register target = build_expression(statement.target());
    Register source = build_expression(statement.source());
    program.instruct(initializer.initialize(target, target, source));
  }

  /** Builds an expression. */
  private Register build_expression(Semantic.Expression expression) {
    return switch (expression) {
      case Semantic.EqualTo b ->
        build_binary_operation(b, Instruction.EqualTo::new);
      case Semantic.NotEqualTo b ->
        build_binary_operation(b, Instruction.NotEqualTo::new);
      case Semantic.StrictlyEqualTo b ->
        build_binary_operation(b, Instruction.StrictlyEqualTo::new);
      case Semantic.LessThan b ->
        build_binary_operation(b, Instruction.LessThan::new);
      case Semantic.LessThanOrEqualTo b ->
        build_binary_operation(b, Instruction.LessThanOrEqualTo::new);
      case Semantic.GreaterThan b ->
        build_binary_operation(b, Instruction.GreaterThan::new);
      case Semantic.GreaterThanOrEqualTo b ->
        build_binary_operation(b, Instruction.GreaterThanOrEqualTo::new);
      case Semantic.BitwiseOr b ->
        build_binary_operation(b, Instruction.BitwiseOr::new);
      case Semantic.BitwiseXor b ->
        build_binary_operation(b, Instruction.BitwiseXor::new);
      case Semantic.BitwiseAnd b ->
        build_binary_operation(b, Instruction.BitwiseAnd::new);
      case Semantic.LeftShift b ->
        build_binary_operation(b, Instruction.LeftShift::new);
      case Semantic.RightShift b ->
        build_binary_operation(b, Instruction.RightShift::new);
      case Semantic.Addition b ->
        build_binary_operation(b, Instruction.Addition::new);
      case Semantic.Subtraction b ->
        build_binary_operation(b, Instruction.Subtraction::new);
      case Semantic.Multiplication b ->
        build_binary_operation(b, Instruction.Multiplication::new);
      case Semantic.Division b ->
        build_binary_operation(b, Instruction.Division::new);
      case Semantic.IntegerDivision b ->
        build_binary_operation(b, Instruction.IntegerDivision::new);
      case Semantic.Modulus b ->
        build_binary_operation(b, Instruction.Modulus::new);
      case Semantic.Promotion u ->
        build_unary_operation(u, Instruction.Addition::new);
      case Semantic.Negation u ->
        build_unary_operation(u, Instruction.Subtraction::new);
      case Semantic.BitwiseNot u -> {
        Register operand = build_expression(u.operand());
        Register target = stack.push(operand);
        program.instruct(new Instruction.BitwiseNot(target, operand));
        yield target;
      }
      case Semantic.LogicalNot u ->
        build_unary_operation(u, Instruction.NotEqualTo::new);
      case Semantic.NumberConstant c -> Register.literal(c.value());
      case Semantic.ConstantAccess c -> Register.literal(c.value());
      case Semantic.GlobalVariableAccess g -> Register.global(g.name());
      case Semantic.LocalVariableAccess l -> Register.local(l.identifier());
    };
  }

  /** Builds a binary operation. */
  private Register build_binary_operation(
    Semantic.BinaryOperation operation,
    BinaryOperationInitializer initializer)
  {
    Register left_operand = build_expression(operation.left_operand());
    Register right_operand = build_expression(operation.right_operand());
    Register target = stack.push(left_operand, right_operand);
    program
      .instruct(initializer.initialize(target, left_operand, right_operand));
    return target;
  }

  /** Builds a unary operation. */
  private Register build_unary_operation(
    Semantic.UnaryOperation operation,
    BinaryOperationInitializer initializer)
  {
    Register operand = build_expression(operation.operand());
    Register target = stack.push(operand);
    program
      .instruct(initializer.initialize(target, Register.literal(0), operand));
    return target;
  }
}
