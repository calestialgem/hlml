package hlml.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import hlml.FloatingPointFormatter;
import hlml.PackedColorFormatter;

/** Ordered collection of instructions are executed sequentially for a
 * meaningful usage of the processor. */
final class Program {
  /** Returns an empty program. */
  static Program create() {
    return new Program(new ArrayList<>(), new HashMap<>());
  }

  /** Instructions that are added to the program. */
  private final List<Instruction> instructions;

  /** Instruction indices that can be used to jump to an instruction. */
  private final Map<Waypoint, OptionalInt> waypoints;

  /** Constructs. */
  private Program(
    List<Instruction> instructions,
    Map<Waypoint, OptionalInt> waypoints)
  {
    this.instructions = instructions;
    this.waypoints = waypoints;
  }

  /** Add an instruction the the end of the program. */
  void instruct(Instruction instruction) {
    instructions.add(instruction);
  }

  /** Returns a new waypoint at an unknown position. */
  Waypoint waypoint() {
    Waypoint waypoint = new Waypoint(waypoints.size());
    waypoints.put(waypoint, OptionalInt.empty());
    return waypoint;
  }

  /** Makes the given waypoint point to the next instruction that will be
   * given. */
  void define(Waypoint waypoint) {
    waypoints.put(waypoint, OptionalInt.of(instructions.size()));
  }

  /** Returns the index of the instruction that is pointed to by a waypoint. */
  int resolve(Waypoint waypoint) {
    return waypoints.get(waypoint).getAsInt();
  }

  /** Appends the program to an appendable. */
  void append_to(Appendable appendable) throws IOException {
    for (Instruction instruction : instructions) {
      append_instruction(appendable, instruction);
      appendable.append(System.lineSeparator());
    }
  }

  /** Appends an instruction. */
  private void append_instruction(
    Appendable appendable,
    Instruction instruction)
    throws IOException
  {
    switch (instruction) {
      case Instruction.Read i -> {
        appendable.append("read");
        append_operands(appendable, i.value(), i.cell(), i.location());
      }
      case Instruction.Write i -> {
        appendable.append("write");
        append_operands(appendable, i.value(), i.cell(), i.location());
      }
      case Instruction.DrawClear i -> {
        appendable.append("draw clear");
        append_operands(appendable, i.r(), i.g(), i.b());
      }
      case Instruction.DrawColor i -> {
        appendable.append("draw color");
        append_operands(appendable, i.r(), i.g(), i.b(), i.a());
      }
      case Instruction.DrawCol i -> {
        appendable.append("draw col");
        append_operands(appendable, i.c());
      }
      case Instruction.DrawStroke i -> {
        appendable.append("draw stroke");
        append_operands(appendable, i.t());
      }
      case Instruction.DrawLine i -> {
        appendable.append("draw line");
        append_operands(appendable, i.x0(), i.y0(), i.x1(), i.y1());
      }
      case Instruction.DrawRect i -> {
        appendable.append("draw rect");
        append_operands(appendable, i.x(), i.y(), i.w(), i.h());
      }
      case Instruction.DrawLinerect i -> {
        appendable.append("draw lineRect");
        append_operands(appendable, i.x(), i.y(), i.w(), i.h());
      }
      case Instruction.DrawPoly i -> {
        appendable.append("draw poly");
        append_operands(appendable, i.x(), i.y(), i.n(), i.r(), i.a());
      }
      case Instruction.DrawLinepoly i -> {
        appendable.append("draw linePoly");
        append_operands(appendable, i.x(), i.y(), i.n(), i.r(), i.a());
      }
      case Instruction.DrawTriangle i -> {
        appendable.append("draw triangle");
        append_operands(
          appendable,
          i.x0(),
          i.y0(),
          i.x1(),
          i.y1(),
          i.x2(),
          i.y2());
      }
      case Instruction.DrawImage i -> {
        appendable.append("draw image");
        append_operands(appendable, i.x(), i.y(), i.i(), i.r(), i.a());
      }
      case Instruction.Drawflush i -> {
        appendable.append("drawflush");
        append_operands(appendable, i.d());
      }
      case Instruction.Packcolor i -> {
        appendable.append("packcolor");
        append_operands(appendable, i.c(), i.r(), i.g(), i.b());
      }
      case Instruction.Print i -> {
        appendable.append("print");
        append_operands(appendable, i.s());
      }
      case Instruction.Printflush i -> {
        appendable.append("printflush");
        append_operands(appendable, i.m());
      }
      case Instruction.Getlink i -> {
        appendable.append("getlink");
        append_operands(appendable, i.l(), i.i());
      }
      case Instruction.ControlEnabled i -> {
        appendable.append("control enabled");
        append_operands(appendable, i.b(), i.e());
      }
      case Instruction.ControlShoot i -> {
        appendable.append("control shoot");
        append_operands(appendable, i.b(), i.x(), i.y(), i.s());
      }
      case Instruction.ControlShootp i -> {
        appendable.append("control shootp");
        append_operands(appendable, i.b(), i.u(), i.s());
      }
      case Instruction.ControlConfig i -> {
        appendable.append("control config");
        append_operands(appendable, i.b(), i.c());
      }
      case Instruction.ControlColor i -> {
        appendable.append("control color");
        append_operands(appendable, i.b(), i.c());
      }
      case Instruction.Sensor i -> {
        appendable.append("sensor");
        append_operands(appendable, i.r(), i.t(), i.i());
      }
      case Instruction.Wait i -> {
        appendable.append("wait");
        append_operands(appendable, i.t());
      }
      case Instruction.Stop i -> appendable.append("stop");
      case Instruction.LookupBlock i -> {
        appendable.append("lookup block");
        append_operands(appendable, i.t(), i.i());
      }
      case Instruction.LookupUnit i -> {
        appendable.append("lookup unit");
        append_operands(appendable, i.t(), i.i());
      }
      case Instruction.LookupItem i -> {
        appendable.append("lookup item");
        append_operands(appendable, i.t(), i.i());
      }
      case Instruction.LookupLiquid i -> {
        appendable.append("lookup liquid");
        append_operands(appendable, i.t(), i.i());
      }
      case Instruction.RadarDistance i -> {
        appendable.append("radar any any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyDistance i -> {
        appendable.append("radar enemy any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyDistance i -> {
        appendable.append("radar enemy ally any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyPlayerDistance i -> {
        appendable.append("radar enemy ally player distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyAttackerDistance i -> {
        appendable.append("radar enemy ally attacker distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyFlyingDistance i -> {
        appendable.append("radar enemy ally flying distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyBossDistance i -> {
        appendable.append("radar enemy ally boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyGroundDistance i -> {
        appendable.append("radar enemy ally ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerDistance i -> {
        appendable.append("radar enemy player any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerAttackerDistance i -> {
        appendable.append("radar enemy player attacker distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerFlyingDistance i -> {
        appendable.append("radar enemy player flying distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerBossDistance i -> {
        appendable.append("radar enemy player boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerGroundDistance i -> {
        appendable.append("radar enemy player ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerDistance i -> {
        appendable.append("radar enemy attacker any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerFlyingDistance i -> {
        appendable.append("radar enemy attacker flying distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerBossDistance i -> {
        appendable.append("radar enemy attacker boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerGroundDistance i -> {
        appendable.append("radar enemy attacker ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingDistance i -> {
        appendable.append("radar enemy flying any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingBossDistance i -> {
        appendable.append("radar enemy flying boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingGroundDistance i -> {
        appendable.append("radar enemy flying ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossDistance i -> {
        appendable.append("radar enemy boss any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossGroundDistance i -> {
        appendable.append("radar enemy boss ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyGroundDistance i -> {
        appendable.append("radar enemy ground any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyDistance i -> {
        appendable.append("radar ally any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerDistance i -> {
        appendable.append("radar ally player any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerAttackerDistance i -> {
        appendable.append("radar ally player attacker distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerFlyingDistance i -> {
        appendable.append("radar ally player flying distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerBossDistance i -> {
        appendable.append("radar ally player boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerGroundDistance i -> {
        appendable.append("radar ally player ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerDistance i -> {
        appendable.append("radar ally attacker any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerFlyingDistance i -> {
        appendable.append("radar ally attacker flying distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerBossDistance i -> {
        appendable.append("radar ally attacker boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerGroundDistance i -> {
        appendable.append("radar ally attacker ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingDistance i -> {
        appendable.append("radar ally flying any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingBossDistance i -> {
        appendable.append("radar ally flying boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingGroundDistance i -> {
        appendable.append("radar ally flying ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossDistance i -> {
        appendable.append("radar ally boss any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossGroundDistance i -> {
        appendable.append("radar ally boss ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyGroundDistance i -> {
        appendable.append("radar ally ground any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerDistance i -> {
        appendable.append("radar player any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerDistance i -> {
        appendable.append("radar player attacker any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerFlyingDistance i -> {
        appendable.append("radar player attacker flying distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerBossDistance i -> {
        appendable.append("radar player attacker boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerGroundDistance i -> {
        appendable.append("radar player attacker ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingDistance i -> {
        appendable.append("radar player flying any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingBossDistance i -> {
        appendable.append("radar player flying boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingGroundDistance i -> {
        appendable.append("radar player flying ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossDistance i -> {
        appendable.append("radar player boss any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossGroundDistance i -> {
        appendable.append("radar player boss ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerGroundDistance i -> {
        appendable.append("radar player ground any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerDistance i -> {
        appendable.append("radar attacker any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingDistance i -> {
        appendable.append("radar attacker flying any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingBossDistance i -> {
        appendable.append("radar attacker flying boss distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingGroundDistance i -> {
        appendable.append("radar attacker flying ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossDistance i -> {
        appendable.append("radar attacker boss any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossGroundDistance i -> {
        appendable.append("radar attacker boss ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerGroundDistance i -> {
        appendable.append("radar attacker ground any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingDistance i -> {
        appendable.append("radar flying any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossDistance i -> {
        appendable.append("radar flying boss any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossGroundDistance i -> {
        appendable.append("radar flying boss ground distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingGroundDistance i -> {
        appendable.append("radar flying ground any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossDistance i -> {
        appendable.append("radar boss any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossGroundDistance i -> {
        appendable.append("radar boss ground any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarGroundDistance i -> {
        appendable.append("radar ground any any distance");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarHealth i -> {
        appendable.append("radar any any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyHealth i -> {
        appendable.append("radar enemy any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyHealth i -> {
        appendable.append("radar enemy ally any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyPlayerHealth i -> {
        appendable.append("radar enemy ally player health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyAttackerHealth i -> {
        appendable.append("radar enemy ally attacker health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyFlyingHealth i -> {
        appendable.append("radar enemy ally flying health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyBossHealth i -> {
        appendable.append("radar enemy ally boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyGroundHealth i -> {
        appendable.append("radar enemy ally ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerHealth i -> {
        appendable.append("radar enemy player any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerAttackerHealth i -> {
        appendable.append("radar enemy player attacker health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerFlyingHealth i -> {
        appendable.append("radar enemy player flying health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerBossHealth i -> {
        appendable.append("radar enemy player boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerGroundHealth i -> {
        appendable.append("radar enemy player ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerHealth i -> {
        appendable.append("radar enemy attacker any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerFlyingHealth i -> {
        appendable.append("radar enemy attacker flying health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerBossHealth i -> {
        appendable.append("radar enemy attacker boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerGroundHealth i -> {
        appendable.append("radar enemy attacker ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingHealth i -> {
        appendable.append("radar enemy flying any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingBossHealth i -> {
        appendable.append("radar enemy flying boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingGroundHealth i -> {
        appendable.append("radar enemy flying ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossHealth i -> {
        appendable.append("radar enemy boss any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossGroundHealth i -> {
        appendable.append("radar enemy boss ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyGroundHealth i -> {
        appendable.append("radar enemy ground any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyHealth i -> {
        appendable.append("radar ally any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerHealth i -> {
        appendable.append("radar ally player any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerAttackerHealth i -> {
        appendable.append("radar ally player attacker health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerFlyingHealth i -> {
        appendable.append("radar ally player flying health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerBossHealth i -> {
        appendable.append("radar ally player boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerGroundHealth i -> {
        appendable.append("radar ally player ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerHealth i -> {
        appendable.append("radar ally attacker any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerFlyingHealth i -> {
        appendable.append("radar ally attacker flying health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerBossHealth i -> {
        appendable.append("radar ally attacker boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerGroundHealth i -> {
        appendable.append("radar ally attacker ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingHealth i -> {
        appendable.append("radar ally flying any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingBossHealth i -> {
        appendable.append("radar ally flying boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingGroundHealth i -> {
        appendable.append("radar ally flying ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossHealth i -> {
        appendable.append("radar ally boss any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossGroundHealth i -> {
        appendable.append("radar ally boss ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyGroundHealth i -> {
        appendable.append("radar ally ground any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerHealth i -> {
        appendable.append("radar player any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerHealth i -> {
        appendable.append("radar player attacker any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerFlyingHealth i -> {
        appendable.append("radar player attacker flying health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerBossHealth i -> {
        appendable.append("radar player attacker boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerGroundHealth i -> {
        appendable.append("radar player attacker ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingHealth i -> {
        appendable.append("radar player flying any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingBossHealth i -> {
        appendable.append("radar player flying boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingGroundHealth i -> {
        appendable.append("radar player flying ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossHealth i -> {
        appendable.append("radar player boss any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossGroundHealth i -> {
        appendable.append("radar player boss ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerGroundHealth i -> {
        appendable.append("radar player ground any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerHealth i -> {
        appendable.append("radar attacker any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingHealth i -> {
        appendable.append("radar attacker flying any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingBossHealth i -> {
        appendable.append("radar attacker flying boss health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingGroundHealth i -> {
        appendable.append("radar attacker flying ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossHealth i -> {
        appendable.append("radar attacker boss any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossGroundHealth i -> {
        appendable.append("radar attacker boss ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerGroundHealth i -> {
        appendable.append("radar attacker ground any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingHealth i -> {
        appendable.append("radar flying any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossHealth i -> {
        appendable.append("radar flying boss any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossGroundHealth i -> {
        appendable.append("radar flying boss ground health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingGroundHealth i -> {
        appendable.append("radar flying ground any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossHealth i -> {
        appendable.append("radar boss any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossGroundHealth i -> {
        appendable.append("radar boss ground any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarGroundHealth i -> {
        appendable.append("radar ground any any health");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarShield i -> {
        appendable.append("radar any any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyShield i -> {
        appendable.append("radar enemy any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyShield i -> {
        appendable.append("radar enemy ally any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyPlayerShield i -> {
        appendable.append("radar enemy ally player shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyAttackerShield i -> {
        appendable.append("radar enemy ally attacker shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyFlyingShield i -> {
        appendable.append("radar enemy ally flying shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyBossShield i -> {
        appendable.append("radar enemy ally boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyGroundShield i -> {
        appendable.append("radar enemy ally ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerShield i -> {
        appendable.append("radar enemy player any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerAttackerShield i -> {
        appendable.append("radar enemy player attacker shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerFlyingShield i -> {
        appendable.append("radar enemy player flying shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerBossShield i -> {
        appendable.append("radar enemy player boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerGroundShield i -> {
        appendable.append("radar enemy player ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerShield i -> {
        appendable.append("radar enemy attacker any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerFlyingShield i -> {
        appendable.append("radar enemy attacker flying shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerBossShield i -> {
        appendable.append("radar enemy attacker boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerGroundShield i -> {
        appendable.append("radar enemy attacker ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingShield i -> {
        appendable.append("radar enemy flying any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingBossShield i -> {
        appendable.append("radar enemy flying boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingGroundShield i -> {
        appendable.append("radar enemy flying ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossShield i -> {
        appendable.append("radar enemy boss any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossGroundShield i -> {
        appendable.append("radar enemy boss ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyGroundShield i -> {
        appendable.append("radar enemy ground any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyShield i -> {
        appendable.append("radar ally any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerShield i -> {
        appendable.append("radar ally player any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerAttackerShield i -> {
        appendable.append("radar ally player attacker shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerFlyingShield i -> {
        appendable.append("radar ally player flying shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerBossShield i -> {
        appendable.append("radar ally player boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerGroundShield i -> {
        appendable.append("radar ally player ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerShield i -> {
        appendable.append("radar ally attacker any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerFlyingShield i -> {
        appendable.append("radar ally attacker flying shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerBossShield i -> {
        appendable.append("radar ally attacker boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerGroundShield i -> {
        appendable.append("radar ally attacker ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingShield i -> {
        appendable.append("radar ally flying any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingBossShield i -> {
        appendable.append("radar ally flying boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingGroundShield i -> {
        appendable.append("radar ally flying ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossShield i -> {
        appendable.append("radar ally boss any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossGroundShield i -> {
        appendable.append("radar ally boss ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyGroundShield i -> {
        appendable.append("radar ally ground any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerShield i -> {
        appendable.append("radar player any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerShield i -> {
        appendable.append("radar player attacker any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerFlyingShield i -> {
        appendable.append("radar player attacker flying shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerBossShield i -> {
        appendable.append("radar player attacker boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerGroundShield i -> {
        appendable.append("radar player attacker ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingShield i -> {
        appendable.append("radar player flying any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingBossShield i -> {
        appendable.append("radar player flying boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingGroundShield i -> {
        appendable.append("radar player flying ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossShield i -> {
        appendable.append("radar player boss any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossGroundShield i -> {
        appendable.append("radar player boss ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerGroundShield i -> {
        appendable.append("radar player ground any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerShield i -> {
        appendable.append("radar attacker any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingShield i -> {
        appendable.append("radar attacker flying any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingBossShield i -> {
        appendable.append("radar attacker flying boss shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingGroundShield i -> {
        appendable.append("radar attacker flying ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossShield i -> {
        appendable.append("radar attacker boss any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossGroundShield i -> {
        appendable.append("radar attacker boss ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerGroundShield i -> {
        appendable.append("radar attacker ground any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingShield i -> {
        appendable.append("radar flying any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossShield i -> {
        appendable.append("radar flying boss any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossGroundShield i -> {
        appendable.append("radar flying boss ground shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingGroundShield i -> {
        appendable.append("radar flying ground any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossShield i -> {
        appendable.append("radar boss any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossGroundShield i -> {
        appendable.append("radar boss ground any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarGroundShield i -> {
        appendable.append("radar ground any any shield");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarArmor i -> {
        appendable.append("radar any any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyArmor i -> {
        appendable.append("radar enemy any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyArmor i -> {
        appendable.append("radar enemy ally any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyPlayerArmor i -> {
        appendable.append("radar enemy ally player armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyAttackerArmor i -> {
        appendable.append("radar enemy ally attacker armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyFlyingArmor i -> {
        appendable.append("radar enemy ally flying armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyBossArmor i -> {
        appendable.append("radar enemy ally boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyGroundArmor i -> {
        appendable.append("radar enemy ally ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerArmor i -> {
        appendable.append("radar enemy player any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerAttackerArmor i -> {
        appendable.append("radar enemy player attacker armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerFlyingArmor i -> {
        appendable.append("radar enemy player flying armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerBossArmor i -> {
        appendable.append("radar enemy player boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerGroundArmor i -> {
        appendable.append("radar enemy player ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerArmor i -> {
        appendable.append("radar enemy attacker any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerFlyingArmor i -> {
        appendable.append("radar enemy attacker flying armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerBossArmor i -> {
        appendable.append("radar enemy attacker boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerGroundArmor i -> {
        appendable.append("radar enemy attacker ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingArmor i -> {
        appendable.append("radar enemy flying any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingBossArmor i -> {
        appendable.append("radar enemy flying boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingGroundArmor i -> {
        appendable.append("radar enemy flying ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossArmor i -> {
        appendable.append("radar enemy boss any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossGroundArmor i -> {
        appendable.append("radar enemy boss ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyGroundArmor i -> {
        appendable.append("radar enemy ground any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyArmor i -> {
        appendable.append("radar ally any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerArmor i -> {
        appendable.append("radar ally player any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerAttackerArmor i -> {
        appendable.append("radar ally player attacker armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerFlyingArmor i -> {
        appendable.append("radar ally player flying armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerBossArmor i -> {
        appendable.append("radar ally player boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerGroundArmor i -> {
        appendable.append("radar ally player ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerArmor i -> {
        appendable.append("radar ally attacker any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerFlyingArmor i -> {
        appendable.append("radar ally attacker flying armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerBossArmor i -> {
        appendable.append("radar ally attacker boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerGroundArmor i -> {
        appendable.append("radar ally attacker ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingArmor i -> {
        appendable.append("radar ally flying any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingBossArmor i -> {
        appendable.append("radar ally flying boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingGroundArmor i -> {
        appendable.append("radar ally flying ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossArmor i -> {
        appendable.append("radar ally boss any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossGroundArmor i -> {
        appendable.append("radar ally boss ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyGroundArmor i -> {
        appendable.append("radar ally ground any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerArmor i -> {
        appendable.append("radar player any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerArmor i -> {
        appendable.append("radar player attacker any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerFlyingArmor i -> {
        appendable.append("radar player attacker flying armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerBossArmor i -> {
        appendable.append("radar player attacker boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerGroundArmor i -> {
        appendable.append("radar player attacker ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingArmor i -> {
        appendable.append("radar player flying any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingBossArmor i -> {
        appendable.append("radar player flying boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingGroundArmor i -> {
        appendable.append("radar player flying ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossArmor i -> {
        appendable.append("radar player boss any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossGroundArmor i -> {
        appendable.append("radar player boss ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerGroundArmor i -> {
        appendable.append("radar player ground any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerArmor i -> {
        appendable.append("radar attacker any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingArmor i -> {
        appendable.append("radar attacker flying any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingBossArmor i -> {
        appendable.append("radar attacker flying boss armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingGroundArmor i -> {
        appendable.append("radar attacker flying ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossArmor i -> {
        appendable.append("radar attacker boss any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossGroundArmor i -> {
        appendable.append("radar attacker boss ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerGroundArmor i -> {
        appendable.append("radar attacker ground any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingArmor i -> {
        appendable.append("radar flying any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossArmor i -> {
        appendable.append("radar flying boss any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossGroundArmor i -> {
        appendable.append("radar flying boss ground armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingGroundArmor i -> {
        appendable.append("radar flying ground any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossArmor i -> {
        appendable.append("radar boss any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossGroundArmor i -> {
        appendable.append("radar boss ground any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarGroundArmor i -> {
        appendable.append("radar ground any any armor");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarMaxhealth i -> {
        appendable.append("radar any any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyMaxhealth i -> {
        appendable.append("radar enemy any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyMaxhealth i -> {
        appendable.append("radar enemy ally any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyPlayerMaxhealth i -> {
        appendable.append("radar enemy ally player maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyAttackerMaxhealth i -> {
        appendable.append("radar enemy ally attacker maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyFlyingMaxhealth i -> {
        appendable.append("radar enemy ally flying maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyBossMaxhealth i -> {
        appendable.append("radar enemy ally boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAllyGroundMaxhealth i -> {
        appendable.append("radar enemy ally ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerMaxhealth i -> {
        appendable.append("radar enemy player any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerAttackerMaxhealth i -> {
        appendable.append("radar enemy player attacker maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerFlyingMaxhealth i -> {
        appendable.append("radar enemy player flying maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerBossMaxhealth i -> {
        appendable.append("radar enemy player boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyPlayerGroundMaxhealth i -> {
        appendable.append("radar enemy player ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerMaxhealth i -> {
        appendable.append("radar enemy attacker any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerFlyingMaxhealth i -> {
        appendable.append("radar enemy attacker flying maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerBossMaxhealth i -> {
        appendable.append("radar enemy attacker boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyAttackerGroundMaxhealth i -> {
        appendable.append("radar enemy attacker ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingMaxhealth i -> {
        appendable.append("radar enemy flying any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingBossMaxhealth i -> {
        appendable.append("radar enemy flying boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyFlyingGroundMaxhealth i -> {
        appendable.append("radar enemy flying ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossMaxhealth i -> {
        appendable.append("radar enemy boss any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyBossGroundMaxhealth i -> {
        appendable.append("radar enemy boss ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarEnemyGroundMaxhealth i -> {
        appendable.append("radar enemy ground any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyMaxhealth i -> {
        appendable.append("radar ally any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerMaxhealth i -> {
        appendable.append("radar ally player any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerAttackerMaxhealth i -> {
        appendable.append("radar ally player attacker maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerFlyingMaxhealth i -> {
        appendable.append("radar ally player flying maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerBossMaxhealth i -> {
        appendable.append("radar ally player boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyPlayerGroundMaxhealth i -> {
        appendable.append("radar ally player ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerMaxhealth i -> {
        appendable.append("radar ally attacker any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerFlyingMaxhealth i -> {
        appendable.append("radar ally attacker flying maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerBossMaxhealth i -> {
        appendable.append("radar ally attacker boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyAttackerGroundMaxhealth i -> {
        appendable.append("radar ally attacker ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingMaxhealth i -> {
        appendable.append("radar ally flying any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingBossMaxhealth i -> {
        appendable.append("radar ally flying boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyFlyingGroundMaxhealth i -> {
        appendable.append("radar ally flying ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossMaxhealth i -> {
        appendable.append("radar ally boss any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyBossGroundMaxhealth i -> {
        appendable.append("radar ally boss ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAllyGroundMaxhealth i -> {
        appendable.append("radar ally ground any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerMaxhealth i -> {
        appendable.append("radar player any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerMaxhealth i -> {
        appendable.append("radar player attacker any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerFlyingMaxhealth i -> {
        appendable.append("radar player attacker flying maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerBossMaxhealth i -> {
        appendable.append("radar player attacker boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerAttackerGroundMaxhealth i -> {
        appendable.append("radar player attacker ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingMaxhealth i -> {
        appendable.append("radar player flying any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingBossMaxhealth i -> {
        appendable.append("radar player flying boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerFlyingGroundMaxhealth i -> {
        appendable.append("radar player flying ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossMaxhealth i -> {
        appendable.append("radar player boss any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerBossGroundMaxhealth i -> {
        appendable.append("radar player boss ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarPlayerGroundMaxhealth i -> {
        appendable.append("radar player ground any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerMaxhealth i -> {
        appendable.append("radar attacker any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingMaxhealth i -> {
        appendable.append("radar attacker flying any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingBossMaxhealth i -> {
        appendable.append("radar attacker flying boss maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerFlyingGroundMaxhealth i -> {
        appendable.append("radar attacker flying ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossMaxhealth i -> {
        appendable.append("radar attacker boss any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerBossGroundMaxhealth i -> {
        appendable.append("radar attacker boss ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarAttackerGroundMaxhealth i -> {
        appendable.append("radar attacker ground any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingMaxhealth i -> {
        appendable.append("radar flying any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossMaxhealth i -> {
        appendable.append("radar flying boss any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingBossGroundMaxhealth i -> {
        appendable.append("radar flying boss ground maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarFlyingGroundMaxhealth i -> {
        appendable.append("radar flying ground any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossMaxhealth i -> {
        appendable.append("radar boss any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarBossGroundMaxhealth i -> {
        appendable.append("radar boss ground any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.RadarGroundMaxhealth i -> {
        appendable.append("radar ground any any maxHealth");
        append_operands(appendable, i.b(), i.o(), i.u());
      }
      case Instruction.JumpAlways i -> {
        appendable.append("jump ");
        appendable.append(Integer.toString(resolve(i.goal())));
        appendable.append(" always");
      }
      case Instruction.JumpOnTrue i -> {
        appendable.append("jump ");
        appendable.append(Integer.toString(resolve(i.goal())));
        appendable.append(" equal true ");
        append_register(appendable, i.condition());
      }
      case Instruction.JumpOnFalse i -> {
        appendable.append("jump ");
        appendable.append(Integer.toString(resolve(i.goal())));
        appendable.append(" equal false ");
        append_register(appendable, i.condition());
      }
      case Instruction.End i -> appendable.append("end");
      case Instruction.Set i -> {
        appendable.append("set");
        append_operands(appendable, i.target(), i.source());
      }
      case Instruction.UnaryOperation i -> {
        appendable.append("op ");
        appendable.append(i.operation_code());
        append_operands(appendable, i.target(), i.operand());
      }
      case Instruction.BinaryOperation i -> {
        appendable.append("op ");
        appendable.append(i.operation_code());
        append_operands(
          appendable,
          i.target(),
          i.left_operand(),
          i.right_operand());
      }
    }
  }

  /** Append operands. */
  private void append_operands(Appendable appendable, Register... operands)
    throws IOException
  {
    for (Register operand : operands) {
      appendable.append(' ');
      append_register(appendable, operand);
    }
  }

  /** Appends a register. */
  private void append_register(Appendable appendable, Register register)
    throws IOException
  {
    switch (register) {
      case Register.Global r -> {
        appendable.append(r.name().source());
        appendable.append('$');
        appendable.append(r.name().identifier());
      }
      case Register.Local r -> {
        appendable.append(r.symbol().source());
        appendable.append('$');
        appendable.append(r.symbol().identifier());
        appendable.append('$');
        appendable.append(r.identifier());
      }
      case Register.Temporary r -> {
        appendable.append('_');
        appendable.append(Integer.toString(r.index()));
      }
      case Register.Link r -> appendable.append(r.building());
      case Register.NumberConstant r ->
        appendable.append(FloatingPointFormatter.format(r.value()));
      case Register.ColorConstant r -> {
        appendable.append('%');
        appendable.append(PackedColorFormatter.format(r.value()));
      }
      case Register.StringConstant r -> {
        appendable.append('"');
        appendable.append(r.value());
        appendable.append('"');
      }
      case Register.Instruction r ->
        appendable.append(Integer.toString(resolve(r.waypoint())));
      case Register.Counter r -> appendable.append("@counter");
      case Register.Null r -> appendable.append("null");
    }
  }
}
