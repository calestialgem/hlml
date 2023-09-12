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
      Semantic.Proc proc =
        (Semantic.Proc) target
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
      Register program_counter = Register.counter();
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
      case Semantic.Proc d -> addresses.put(name, program.waypoint());
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
        Register program_counter = Register.counter();
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
      case Semantic.NumberConstant c -> Register.number(c.value());
      case Semantic.ColorConstant e -> Register.color(e.value());
      case Semantic.StringConstant e -> Register.string(e.value());
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
          case Semantic.Proc proc -> {
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
              Register parameter = Register.parameter(proc, i);
              stack.pop(argument);
              program.instruct(new Instruction.Set(parameter, argument));
            }
            for (
              int i = e.arguments().size();
              i < proc.parameters().size();
              i++)
            {
              Register argument = Register.null_();
              Register parameter = Register.parameter(proc, i);
              program.instruct(new Instruction.Set(parameter, argument));
            }
            Waypoint address = addresses.get(e.procedure());
            program.instruct(new Instruction.JumpAlways(address));
            program.define(after_call);
            for (int i = 0; i < e.arguments().size(); i++) {
              if (!proc.parameters().get(i).in_out()) { continue; }
              Register argument = arguments.get(i);
              if (!argument.is_volatile()) { continue; }
              Register parameter = Register.parameter(proc, i);
              program.instruct(new Instruction.Set(argument, parameter));
            }
            Register return_value =
              Register.local(e.procedure(), "return$value");
            yield return_value;
          }
          case Semantic.Read p -> {
            program
              .instruct(
                new Instruction.Read(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.Write p -> {
            program
              .instruct(
                new Instruction.Write(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.DrawClear p -> {
            program
              .instruct(
                new Instruction.DrawClear(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.DrawColor p -> {
            program
              .instruct(
                new Instruction.DrawColor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.DrawCol p -> {
            program
              .instruct(
                new Instruction.DrawCol(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.DrawStroke p -> {
            program
              .instruct(
                new Instruction.DrawStroke(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.DrawLine p -> {
            program
              .instruct(
                new Instruction.DrawLine(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.DrawRect p -> {
            program
              .instruct(
                new Instruction.DrawRect(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.DrawLinerect p -> {
            program
              .instruct(
                new Instruction.DrawLinerect(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.DrawPoly p -> {
            program
              .instruct(
                new Instruction.DrawPoly(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3),
                  build_argument(e.arguments(), 4)));
            yield Register.null_();
          }
          case Semantic.DrawLinepoly p -> {
            program
              .instruct(
                new Instruction.DrawLinepoly(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3),
                  build_argument(e.arguments(), 4)));
            yield Register.null_();
          }
          case Semantic.DrawTriangle p -> {
            program
              .instruct(
                new Instruction.DrawTriangle(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3),
                  build_argument(e.arguments(), 4),
                  build_argument(e.arguments(), 5)));
            yield Register.null_();
          }
          case Semantic.DrawImage p -> {
            program
              .instruct(
                new Instruction.DrawImage(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3),
                  build_argument(e.arguments(), 4)));
            yield Register.null_();
          }
          case Semantic.Drawflush p -> {
            program
              .instruct(
                new Instruction.Drawflush(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.PackColor p -> {
            program
              .instruct(
                new Instruction.Packcolor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.Print p -> {
            program
              .instruct(
                new Instruction.Print(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.Printflush p -> {
            program
              .instruct(
                new Instruction.Printflush(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.Getlink p -> {
            program
              .instruct(
                new Instruction.Getlink(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.ControlEnabled p -> {
            program
              .instruct(
                new Instruction.ControlEnabled(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.ControlShoot p -> {
            program
              .instruct(
                new Instruction.ControlShoot(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.ControlShootp p -> {
            program
              .instruct(
                new Instruction.ControlShootp(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.ControlConfig p -> {
            program
              .instruct(
                new Instruction.ControlConfig(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.ControlColor p -> {
            program
              .instruct(
                new Instruction.ControlColor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.Sensor p -> {
            program
              .instruct(
                new Instruction.Sensor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.Wait p -> {
            program
              .instruct(new Instruction.Wait(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.Stop p -> {
            program.instruct(new Instruction.Stop());
            yield Register.null_();
          }
          case Semantic.LookupBlock p -> {
            program
              .instruct(
                new Instruction.LookupBlock(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.LookupUnit p -> {
            program
              .instruct(
                new Instruction.LookupUnit(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.LookupItem p -> {
            program
              .instruct(
                new Instruction.LookupItem(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.LookupLiquid p -> {
            program
              .instruct(
                new Instruction.LookupLiquid(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.Ubind p -> {
            program
              .instruct(
                new Instruction.Ubind(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.UcontrolIdle p -> {
            program.instruct(new Instruction.UcontrolIdle());
            yield Register.null_();
          }
          case Semantic.UcontrolStop p -> {
            program.instruct(new Instruction.UcontrolStop());
            yield Register.null_();
          }
          case Semantic.UcontrolMove p -> {
            program
              .instruct(
                new Instruction.UcontrolMove(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.UcontrolApproach p -> {
            program
              .instruct(
                new Instruction.UcontrolApproach(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.UcontrolPathfind p -> {
            program
              .instruct(
                new Instruction.UcontrolPathfind(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.UcontrolAutopathfind p -> {
            program.instruct(new Instruction.UcontrolAutopathfind());
            yield Register.null_();
          }
          case Semantic.UcontrolBoost p -> {
            program
              .instruct(
                new Instruction.UcontrolBoost(
                  build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.UcontrolTarget p -> {
            program
              .instruct(
                new Instruction.UcontrolTarget(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.UcontrolTargetp p -> {
            program
              .instruct(
                new Instruction.UcontrolTargetp(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.UcontrolItemdrop p -> {
            program
              .instruct(
                new Instruction.UcontrolItemdrop(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.UcontrolItemtake p -> {
            program
              .instruct(
                new Instruction.UcontrolItemtake(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.UcontrolPaydrop p -> {
            program.instruct(new Instruction.UcontrolPaydrop());
            yield Register.null_();
          }
          case Semantic.UcontrolPaytake p -> {
            program
              .instruct(
                new Instruction.UcontrolPaytake(
                  build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.UcontrolPayenter p -> {
            program.instruct(new Instruction.UcontrolPayenter());
            yield Register.null_();
          }
          case Semantic.UcontrolMine p -> {
            program
              .instruct(
                new Instruction.UcontrolMine(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1)));
            yield Register.null_();
          }
          case Semantic.UcontrolFlag p -> {
            program
              .instruct(
                new Instruction.UcontrolFlag(build_argument(e.arguments(), 0)));
            yield Register.null_();
          }
          case Semantic.UcontrolBuild p -> {
            program
              .instruct(
                new Instruction.UcontrolBuild(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3),
                  build_argument(e.arguments(), 4)));
            yield Register.null_();
          }
          case Semantic.UcontrolGetblock p -> {
            program
              .instruct(
                new Instruction.UcontrolGetblock(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3),
                  build_argument(e.arguments(), 4)));
            yield Register.null_();
          }
          case Semantic.UcontrolWithin p -> {
            program
              .instruct(
                new Instruction.UcontrolWithin(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2),
                  build_argument(e.arguments(), 3)));
            yield Register.null_();
          }
          case Semantic.UcontrolUnbind p -> {
            program.instruct(new Instruction.UcontrolUnbind());
            yield Register.null_();
          }
          case Semantic.RadarDistance p -> {
            program
              .instruct(
                new Instruction.RadarDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyPlayerDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyPlayerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarEnemyGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAllyGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarPlayerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarAttackerGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingDistance p -> {
            program
              .instruct(
                new Instruction.RadarFlyingDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarFlyingGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossDistance p -> {
            program
              .instruct(
                new Instruction.RadarBossDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarBossGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarGroundDistance p -> {
            program
              .instruct(
                new Instruction.RadarGroundDistance(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarHealth p -> {
            program
              .instruct(
                new Instruction.RadarHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyPlayerHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyPlayerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingHealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossHealth p -> {
            program
              .instruct(
                new Instruction.RadarBossHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarBossGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarGroundHealth p -> {
            program
              .instruct(
                new Instruction.RadarGroundHealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarShield p -> {
            program
              .instruct(
                new Instruction.RadarShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyPlayerShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyPlayerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyBossShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingBossShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarEnemyGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingBossShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAllyGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingBossShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarPlayerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingBossShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarAttackerGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingShield p -> {
            program
              .instruct(
                new Instruction.RadarFlyingShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossShield p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarFlyingGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossShield p -> {
            program
              .instruct(
                new Instruction.RadarBossShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarBossGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarGroundShield p -> {
            program
              .instruct(
                new Instruction.RadarGroundShield(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarArmor p -> {
            program
              .instruct(
                new Instruction.RadarArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyPlayerArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyPlayerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarEnemyGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAllyGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarPlayerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarAttackerGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingArmor p -> {
            program
              .instruct(
                new Instruction.RadarFlyingArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarFlyingGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossArmor p -> {
            program
              .instruct(
                new Instruction.RadarBossArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarBossGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarGroundArmor p -> {
            program
              .instruct(
                new Instruction.RadarGroundArmor(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyPlayerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyPlayerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAllyGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAllyGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyPlayerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyPlayerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyAttackerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyAttackerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyFlyingGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyFlyingGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyBossGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyBossGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarEnemyGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarEnemyGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyPlayerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyPlayerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyAttackerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyAttackerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyFlyingGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyFlyingGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyBossGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyBossGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAllyGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAllyGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerAttackerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerAttackerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerFlyingGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerFlyingGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerBossGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerBossGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarPlayerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarPlayerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerFlyingGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerFlyingGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerBossGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerBossGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarAttackerGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarAttackerGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingBossGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingBossGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarFlyingGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarFlyingGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarBossMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarBossGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarBossGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
          case Semantic.RadarGroundMaxhealth p -> {
            program
              .instruct(
                new Instruction.RadarGroundMaxhealth(
                  build_argument(e.arguments(), 0),
                  build_argument(e.arguments(), 1),
                  build_argument(e.arguments(), 2)));
            yield Register.null_();
          }
        };
      }
    };
  }

  /** Builds and pops the argument in the list if it exists. Otherwise returns a
   * register holding null. */
  private Register build_argument(
    List<Semantic.Expression> arguments,
    int index)
  {
    if (index >= arguments.size()) { return Register.null_(); }
    Register argument = build_expression(arguments.get(index));
    stack.pop(argument);
    return argument;
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
