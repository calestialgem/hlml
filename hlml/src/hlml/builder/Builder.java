package hlml.builder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

  /** Name of the the currently built symbol. */
  private Name current;

  /** Instruction list. */
  private Program program;

  /** Temporary register list. */
  private Stack stack;

  /** Global variables in the program with initial values. */
  private Set<Name> initialized;

  /** Addresses of the procedures called in the program. */
  private Map<Name, Waypoint> addresses;

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
    initialized = new HashSet<>();
    addresses = new HashMap<>();
    for (Name dependency : entrypoint.get().dependencies()) {
      build_dependency(dependency);
    }
    for (Name global_variable : initialized) {
      current = global_variable;
      Semantic.GlobalVar var =
        (Semantic.GlobalVar) target
          .sources()
          .get(global_variable.source())
          .globals()
          .get(global_variable.identifier());
      Register value = build_expression(var.initial_value().get());
      Register global = Register.global(global_variable);
      stack.pop(value);
      program.instruct(new Instruction.Set(global, value));
    }
    current = new Name(target.name(), "entrypoint");
    build_statement(new ArrayList<>(), entrypoint.get().body());
    program.instruct(new Instruction.End());
    for (Name procedure : addresses.keySet()) {
      current = procedure;
      Semantic.UserDefinedProcedure proc =
        (Semantic.UserDefinedProcedure) target
          .sources()
          .get(procedure.source())
          .globals()
          .get(procedure.identifier());
      program.define(addresses.get(procedure));
      build_statement(new ArrayList<>(), proc.body());
      Register value = Register.null_();
      Register return_value = Register.local(current, "return$value");
      program.instruct(new Instruction.Set(return_value, value));
      Register return_location = Register.local(current, "return$location");
      Register program_counter = Register.builtin("counter");
      program.instruct(new Instruction.Set(program_counter, return_location));
    }
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
      case Semantic.UserDefinedProcedure d ->
        addresses.put(name, program.waypoint());
      case Semantic.GlobalVar d ->
        d.initial_value().ifPresent(i -> initialized.add(name));
      default -> {}
    }
  }

  /** Builds a statement if it is there. */
  private void build_statement(
    List<LoopWaypoints> loop_waypoints,
    Optional<Semantic.Statement> statement)
  {
    statement.ifPresent(s -> build_statement(loop_waypoints, s));
  }

  /** Builds a statement. */
  private void build_statement(
    List<LoopWaypoints> loop_waypoints,
    Semantic.Statement statement)
  {
    switch (statement) {
      case Semantic.Block s ->
        s.inner_statements().forEach(i -> build_statement(loop_waypoints, i));
      case Semantic.If s -> {
        s.variables().forEach(this::build_variable);
        Register condition = build_expression(s.condition());
        Waypoint after_true_branch = program.waypoint();
        program
          .instruct(new Instruction.JumpOnFalse(after_true_branch, condition));
        stack.pop(condition);
        build_statement(loop_waypoints, s.true_branch());
        Waypoint after_false_branch = program.waypoint();
        program.instruct(new Instruction.JumpAlways(after_false_branch));
        program.define(after_true_branch);
        build_statement(loop_waypoints, s.false_branch());
        program.define(after_false_branch);
      }
      case Semantic.While s -> {
        s.variables().forEach(this::build_variable);
        Register first_condition = build_expression(s.condition());
        Waypoint loop = program.waypoint();
        program.instruct(new Instruction.JumpOnTrue(loop, first_condition));
        stack.pop(first_condition);
        build_statement(loop_waypoints, s.zero_branch());
        Waypoint end = program.waypoint();
        program.instruct(new Instruction.JumpAlways(end));
        program.define(loop);
        Waypoint begin = program.waypoint();
        loop_waypoints.add(new LoopWaypoints(begin, end));
        build_statement(loop_waypoints, s.loop());
        loop_waypoints.remove(loop_waypoints.size() - 1);
        program.define(begin);
        build_statement(loop_waypoints, s.interleaved());
        Register remaining_conditions = build_expression(s.condition());
        program
          .instruct(new Instruction.JumpOnTrue(loop, remaining_conditions));
        stack.pop(remaining_conditions);
        program.define(end);
      }
      case Semantic.Break s ->
        program
          .instruct(
            new Instruction.JumpAlways(loop_waypoints.get(s.loop()).end()));
      case Semantic.Continue s ->
        program
          .instruct(
            new Instruction.JumpAlways(loop_waypoints.get(s.loop()).begin()));
      case Semantic.Return s -> {
        if (s.value().isPresent()) {
          Register value = build_expression(s.value().get());
          Register return_value = Register.local(current, "return$value");
          stack.pop(value);
          program.instruct(new Instruction.Set(return_value, value));
        }
        Register return_location = Register.local(current, "return$location");
        Register program_counter = Register.builtin("counter");
        program.instruct(new Instruction.Set(program_counter, return_location));
      }
      case Semantic.LocalVar l -> build_variable(l);
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

  /** Builds a local variable. */
  private void build_variable(Semantic.LocalVar l) {
    Register variable = Register.local(current, l.identifier());
    if (l.initial_value().isPresent()) {
      Register initial_value = build_expression(l.initial_value().get());
      stack.pop(initial_value);
      program.instruct(new Instruction.Set(variable, initial_value));
    }
  }

  /** Builds a mutate statement. */
  private void build_mutate(
    Semantic.Mutate statement,
    BinaryOperationInitializer initializer)
  {
    Register target = build_expression(statement.target());
    program
      .instruct(initializer.initialize(target, target, Register.number(1)));
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
      case Semantic.LogicalOr e -> {
        Waypoint after_evaluation = program.waypoint();
        Register left_evaluation = build_expression(e.left_operand());
        Register evaluation = stack.push(left_evaluation);
        program.instruct(new Instruction.Set(evaluation, left_evaluation));
        program
          .instruct(new Instruction.JumpOnTrue(after_evaluation, evaluation));
        Register right_evaluation = build_expression(e.right_operand());
        stack.pop(right_evaluation);
        program.instruct(new Instruction.Set(evaluation, right_evaluation));
        program.define(after_evaluation);
        yield evaluation;
      }
      case Semantic.LogicalAnd e -> {
        Waypoint after_evaluation = program.waypoint();
        Register left_evaluation = build_expression(e.left_operand());
        Register evaluation = stack.push(left_evaluation);
        program.instruct(new Instruction.Set(evaluation, left_evaluation));
        program
          .instruct(new Instruction.JumpOnFalse(after_evaluation, evaluation));
        Register right_evaluation = build_expression(e.right_operand());
        stack.pop(right_evaluation);
        program.instruct(new Instruction.Set(evaluation, right_evaluation));
        program.define(after_evaluation);
        yield evaluation;
      }
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
      case Semantic.KnownFalse c -> Register.false_();
      case Semantic.KnownTrue c -> Register.true_();
      case Semantic.KnownNull c -> Register.null_();
      case Semantic.KnownBuiltin c -> Register.builtin(c.name());
      case Semantic.KnownNumber c -> Register.number(c.numeric());
      case Semantic.KnownColor e -> Register.color(e.value());
      case Semantic.KnownString e -> Register.string(e.value());
      case Semantic.LinkAccess e -> Register.link(e.building());
      case Semantic.GlobalVariableAccess g -> Register.global(g.name());
      case Semantic.LocalVariableAccess l ->
        Register.local(current, l.identifier());
      case Semantic.Call e -> {
        Semantic.Procedure procedure =
          (Semantic.Procedure) target
            .sources()
            .get(e.procedure().source())
            .globals()
            .get(e.procedure().identifier());
        yield switch (procedure) {
          case Semantic.UserDefinedProcedure p -> {
            Waypoint after_call = program.waypoint();
            Register return_address = Register.instruction(after_call);
            Register return_location =
              Register.local(e.procedure(), "return$location");
            program
              .instruct(new Instruction.Set(return_location, return_address));
            List<Register> arguments = new ArrayList<>();
            for (int i = 0; i < e.arguments().size(); i++) {
              Register argument = build_expression(e.arguments().get(i));
              arguments.add(argument);
              Register parameter = Register.parameter(p, i);
              stack.pop(argument);
              program.instruct(new Instruction.Set(parameter, argument));
            }
            for (int i = e.arguments().size(); i < p.parameters().size(); i++) {
              Register argument = Register.null_();
              Register parameter = Register.parameter(p, i);
              program.instruct(new Instruction.Set(parameter, argument));
            }
            Waypoint address = addresses.get(e.procedure());
            program.instruct(new Instruction.JumpAlways(address));
            program.define(after_call);
            for (int i = 0; i < e.arguments().size(); i++) {
              if (!p.parameters().get(i).in_out()) { continue; }
              Register argument = arguments.get(i);
              if (!argument.is_volatile()) { continue; }
              Register parameter = Register.parameter(p, i);
              program.instruct(new Instruction.Set(argument, parameter));
            }
            Register return_value =
              Register.local(e.procedure(), "return$value");
            yield return_value;
          }
          case Semantic.BuiltinProcedure p -> {
            program
              .instruct(
                new Instruction.DirectlyCompiled(
                  p.instruction_text(),
                  build_arguments(e.arguments(), p.parameter_count())));
            yield Register.null_();
          }
          case Semantic.BuiltinProcedureWithDummy p -> {
            program
              .instruct(
                new Instruction.DirectlyCompiledWithDummy(
                  p.instruction_text(),
                  p.dummy_argument(),
                  build_arguments(e.arguments(), p.parameter_count())));
            yield Register.null_();
          }
        };
      }
      case Semantic.MemberAccess e -> {
        Register object = build_expression(e.object());
        Register member = build_expression(e.member());
        Register result = stack.push(object, member);
        program.instruct(new Instruction.Sensor(result, object, member));
        yield result;
      }
    };
  }

  /** Builds the argument list from the provided arguments and puts null for
   * parameters that were left unprovided. */
  private List<Register> build_arguments(
    List<Semantic.Expression> provided_arguments,
    int parameter_count)
  {
    List<Register> arguments = new ArrayList<>();
    for (Semantic.Expression a : provided_arguments) {
      Register argument = build_expression(a);
      stack.pop(argument);
      arguments.add(argument);
    }
    for (int i = provided_arguments.size(); i < parameter_count; i++) {
      arguments.add(Register.null_());
    }
    return arguments;
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
      .instruct(initializer.initialize(target, Register.number(0), operand));
    return target;
  }
}
