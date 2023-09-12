package hlml.builder;

/** Command that can be executed by a processor. */
sealed interface Instruction {
  /** Instruction that reads from a location in a memory cell. */
  record Read(Register value, Register cell, Register location)
    implements Instruction
  {}

  /** Instruction that writes to a location in a memory cell. */
  record Write(Register value, Register cell, Register location)
    implements Instruction
  {}

  /** Instruction that fills the display with a color. */
  record DrawClear(Register r, Register g, Register b) implements Instruction {}

  /** Instruction that sets the color for the fallowing drawing instructions.
   * Takes the color as separated to channels. */
  record DrawColor(Register r, Register g, Register b, Register a)
    implements Instruction
  {}

  /** Instruction that sets the color for the fallowing drawing instructions.
   * Takes the color as an packed integer. */
  record DrawCol(Register c) implements Instruction {}

  /** Instruction that sets the thickness of the lines for the fallowing drawing
   * instructions. */
  record DrawStroke(Register t) implements Instruction {}

  /** Instruction that draws a line segment. */
  record DrawLine(Register x0, Register y0, Register x1, Register y1)
    implements Instruction
  {}

  /** Instruction that draws a rectangle. */
  record DrawRect(Register x, Register y, Register w, Register h)
    implements Instruction
  {}

  /** Instruction that draws a rectangle outline. */
  record DrawLinerect(Register x, Register y, Register w, Register h)
    implements Instruction
  {}

  /** Instruction that draws a regular polygon. */
  record DrawPoly(Register x, Register y, Register n, Register r, Register a)
    implements Instruction
  {}

  /** Instructions that draws a regular polygon outline */
  record DrawLinepoly(
    Register x,
    Register y,
    Register n,
    Register r,
    Register a) implements Instruction
  {}

  /** Instruction that draws a triangle. */
  record DrawTriangle(
    Register x0,
    Register y0,
    Register x1,
    Register y1,
    Register x2,
    Register y2) implements Instruction
  {}

  /** Instruction that draws an image from the game's contents. */
  record DrawImage(Register x, Register y, Register i, Register r, Register a)
    implements Instruction
  {}

  /** Instruction that sends all the accumulated drawing instructions to a
   * display. */
  record Drawflush(Register d) implements Instruction {}

  /** Instruction that packs a color made up of three floating point channels to
   * a single unsigned integer. */
  record Packcolor(Register c, Register r, Register g, Register b)
    implements Instruction
  {}

  /** Instruction that prints some value as text. */
  record Print(Register s) implements Instruction {}

  /** Instruction that sends all the accumulated printing instructions to a
   * message. */
  record Printflush(Register m) implements Instruction {}

  /** Instruction that gets a link by its index. */
  record Getlink(Register l, Register i) implements Instruction {}

  /** Instruction that sets the enabled status of a building. */
  record ControlEnabled(Register b, Register e) implements Instruction {}

  /** Instruction that makes a turret building shoot at a coordinate. */
  record ControlShoot(Register b, Register x, Register y, Register s)
    implements Instruction
  {}

  /** Instruction that makes a turret building shoot at a unit with velocity
   * prediction. */
  record ControlShootp(Register b, Register u, Register s)
    implements Instruction
  {}

  /** Instruction that changes the configuration of a building. */
  record ControlConfig(Register b, Register c) implements Instruction {}

  /** Instruction that changes the color of a building. */
  record ControlColor(Register b, Register c) implements Instruction {}

  /** Instruction that gets information from a building or a unit. */
  record Sensor(Register r, Register t, Register i) implements Instruction {}

  /** Instruction that makes the processor postpone executing instructions for
   * some amount of seconds. */
  record Wait(Register t) implements Instruction {}

  /** Instruction that makes the processor stop executing instructions. */
  record Stop() implements Instruction {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric. */
  record RadarDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`. */
  record RadarEnemyDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ally`. */
  record RadarEnemyAllyDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ally`,
   * `player`. */
  record RadarEnemyAllyPlayerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ally`,
   * `attacker`. */
  record RadarEnemyAllyAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ally`,
   * `flying`. */
  record RadarEnemyAllyFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ally`,
   * `boss`. */
  record RadarEnemyAllyBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ally`,
   * `ground`. */
  record RadarEnemyAllyGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `player`. */
  record RadarEnemyPlayerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `player`,
   * `attacker`. */
  record RadarEnemyPlayerAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `player`,
   * `flying`. */
  record RadarEnemyPlayerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `player`,
   * `boss`. */
  record RadarEnemyPlayerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `player`,
   * `ground`. */
  record RadarEnemyPlayerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `attacker`. */
  record RadarEnemyAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `attacker`,
   * `flying`. */
  record RadarEnemyAttackerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `attacker`,
   * `boss`. */
  record RadarEnemyAttackerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `attacker`,
   * `ground`. */
  record RadarEnemyAttackerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `flying`. */
  record RadarEnemyFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `flying`,
   * `boss`. */
  record RadarEnemyFlyingBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `flying`,
   * `ground`. */
  record RadarEnemyFlyingGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `boss`. */
  record RadarEnemyBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `boss`,
   * `ground`. */
  record RadarEnemyBossGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `enemy`, `ground`. */
  record RadarEnemyGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`. */
  record RadarAllyDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `player`. */
  record RadarAllyPlayerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `player`,
   * `attacker`. */
  record RadarAllyPlayerAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `player`,
   * `flying`. */
  record RadarAllyPlayerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `player`,
   * `boss`. */
  record RadarAllyPlayerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `player`,
   * `ground`. */
  record RadarAllyPlayerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `attacker`. */
  record RadarAllyAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `attacker`,
   * `flying`. */
  record RadarAllyAttackerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `attacker`,
   * `boss`. */
  record RadarAllyAttackerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `attacker`,
   * `ground`. */
  record RadarAllyAttackerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `flying`. */
  record RadarAllyFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `flying`,
   * `boss`. */
  record RadarAllyFlyingBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `flying`,
   * `ground`. */
  record RadarAllyFlyingGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `boss`. */
  record RadarAllyBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `boss`,
   * `ground`. */
  record RadarAllyBossGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ally`, `ground`. */
  record RadarAllyGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`. */
  record RadarPlayerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `attacker`. */
  record RadarPlayerAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `attacker`,
   * `flying`. */
  record RadarPlayerAttackerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `attacker`,
   * `boss`. */
  record RadarPlayerAttackerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `attacker`,
   * `ground`. */
  record RadarPlayerAttackerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `flying`. */
  record RadarPlayerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `flying`,
   * `boss`. */
  record RadarPlayerFlyingBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `flying`,
   * `ground`. */
  record RadarPlayerFlyingGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `boss`. */
  record RadarPlayerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `boss`,
   * `ground`. */
  record RadarPlayerBossGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `player`, `ground`. */
  record RadarPlayerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`. */
  record RadarAttackerDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`, `flying`. */
  record RadarAttackerFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`, `flying`,
   * `boss`. */
  record RadarAttackerFlyingBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`, `flying`,
   * `ground`. */
  record RadarAttackerFlyingGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`, `boss`. */
  record RadarAttackerBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`, `boss`,
   * `ground`. */
  record RadarAttackerBossGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `attacker`, `ground`. */
  record RadarAttackerGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `flying`. */
  record RadarFlyingDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `flying`, `boss`. */
  record RadarFlyingBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `flying`, `boss`,
   * `ground`. */
  record RadarFlyingBossGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `flying`, `ground`. */
  record RadarFlyingGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `boss`. */
  record RadarBossDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `boss`, `ground`. */
  record RadarBossGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `distance` as the metric after filtering them by `ground`. */
  record RadarGroundDistance(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric. */
  record RadarHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`. */
  record RadarEnemyHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ally`. */
  record RadarEnemyAllyHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ally`,
   * `player`. */
  record RadarEnemyAllyPlayerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ally`,
   * `attacker`. */
  record RadarEnemyAllyAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ally`,
   * `flying`. */
  record RadarEnemyAllyFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ally`,
   * `ground`. */
  record RadarEnemyAllyGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `player`. */
  record RadarEnemyPlayerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `player`,
   * `attacker`. */
  record RadarEnemyPlayerAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `player`,
   * `flying`. */
  record RadarEnemyPlayerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `player`,
   * `boss`. */
  record RadarEnemyPlayerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `player`,
   * `ground`. */
  record RadarEnemyPlayerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `attacker`. */
  record RadarEnemyAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `attacker`,
   * `flying`. */
  record RadarEnemyAttackerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `attacker`,
   * `boss`. */
  record RadarEnemyAttackerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `attacker`,
   * `ground`. */
  record RadarEnemyAttackerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `flying`. */
  record RadarEnemyFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `flying`,
   * `boss`. */
  record RadarEnemyFlyingBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `flying`,
   * `ground`. */
  record RadarEnemyFlyingGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `boss`. */
  record RadarEnemyBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `boss`,
   * `ground`. */
  record RadarEnemyBossGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `enemy`, `ground`. */
  record RadarEnemyGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`. */
  record RadarAllyHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `player`. */
  record RadarAllyPlayerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `player`,
   * `attacker`. */
  record RadarAllyPlayerAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `player`,
   * `flying`. */
  record RadarAllyPlayerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `player`,
   * `ground`. */
  record RadarAllyPlayerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `attacker`. */
  record RadarAllyAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `attacker`,
   * `flying`. */
  record RadarAllyAttackerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `attacker`,
   * `boss`. */
  record RadarAllyAttackerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `attacker`,
   * `ground`. */
  record RadarAllyAttackerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `flying`. */
  record RadarAllyFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `flying`,
   * `ground`. */
  record RadarAllyFlyingGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `boss`. */
  record RadarAllyBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ally`, `ground`. */
  record RadarAllyGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`. */
  record RadarPlayerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `attacker`. */
  record RadarPlayerAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `attacker`,
   * `flying`. */
  record RadarPlayerAttackerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `attacker`,
   * `boss`. */
  record RadarPlayerAttackerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `attacker`,
   * `ground`. */
  record RadarPlayerAttackerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `flying`. */
  record RadarPlayerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `flying`,
   * `boss`. */
  record RadarPlayerFlyingBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `flying`,
   * `ground`. */
  record RadarPlayerFlyingGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `boss`. */
  record RadarPlayerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `boss`,
   * `ground`. */
  record RadarPlayerBossGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `player`, `ground`. */
  record RadarPlayerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`. */
  record RadarAttackerHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`, `flying`. */
  record RadarAttackerFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`, `flying`,
   * `boss`. */
  record RadarAttackerFlyingBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`, `flying`,
   * `ground`. */
  record RadarAttackerFlyingGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`, `boss`. */
  record RadarAttackerBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`, `boss`,
   * `ground`. */
  record RadarAttackerBossGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `attacker`, `ground`. */
  record RadarAttackerGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `flying`. */
  record RadarFlyingHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `flying`, `boss`. */
  record RadarFlyingBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `flying`, `boss`,
   * `ground`. */
  record RadarFlyingBossGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `flying`, `ground`. */
  record RadarFlyingGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `boss`. */
  record RadarBossHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `boss`, `ground`. */
  record RadarBossGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `health` as the metric after filtering them by `ground`. */
  record RadarGroundHealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric. */
  record RadarShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`. */
  record RadarEnemyShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ally`. */
  record RadarEnemyAllyShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ally`,
   * `player`. */
  record RadarEnemyAllyPlayerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ally`,
   * `attacker`. */
  record RadarEnemyAllyAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ally`,
   * `flying`. */
  record RadarEnemyAllyFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ally`,
   * `ground`. */
  record RadarEnemyAllyGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `player`. */
  record RadarEnemyPlayerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `player`,
   * `attacker`. */
  record RadarEnemyPlayerAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `player`,
   * `flying`. */
  record RadarEnemyPlayerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `player`,
   * `boss`. */
  record RadarEnemyPlayerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `player`,
   * `ground`. */
  record RadarEnemyPlayerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `attacker`. */
  record RadarEnemyAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `attacker`,
   * `flying`. */
  record RadarEnemyAttackerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `attacker`,
   * `boss`. */
  record RadarEnemyAttackerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `attacker`,
   * `ground`. */
  record RadarEnemyAttackerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `flying`. */
  record RadarEnemyFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `flying`,
   * `boss`. */
  record RadarEnemyFlyingBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `flying`,
   * `ground`. */
  record RadarEnemyFlyingGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `boss`. */
  record RadarEnemyBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `boss`,
   * `ground`. */
  record RadarEnemyBossGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `enemy`, `ground`. */
  record RadarEnemyGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`. */
  record RadarAllyShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `player`. */
  record RadarAllyPlayerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `player`,
   * `attacker`. */
  record RadarAllyPlayerAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `player`,
   * `flying`. */
  record RadarAllyPlayerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `player`,
   * `ground`. */
  record RadarAllyPlayerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `attacker`. */
  record RadarAllyAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `attacker`,
   * `flying`. */
  record RadarAllyAttackerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `attacker`,
   * `boss`. */
  record RadarAllyAttackerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `attacker`,
   * `ground`. */
  record RadarAllyAttackerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `flying`. */
  record RadarAllyFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `flying`,
   * `ground`. */
  record RadarAllyFlyingGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `boss`. */
  record RadarAllyBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ally`, `ground`. */
  record RadarAllyGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`. */
  record RadarPlayerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `attacker`. */
  record RadarPlayerAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `attacker`,
   * `flying`. */
  record RadarPlayerAttackerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `attacker`,
   * `boss`. */
  record RadarPlayerAttackerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `attacker`,
   * `ground`. */
  record RadarPlayerAttackerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `flying`. */
  record RadarPlayerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `flying`,
   * `boss`. */
  record RadarPlayerFlyingBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `flying`,
   * `ground`. */
  record RadarPlayerFlyingGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `boss`. */
  record RadarPlayerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `boss`,
   * `ground`. */
  record RadarPlayerBossGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `player`, `ground`. */
  record RadarPlayerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`. */
  record RadarAttackerShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`, `flying`. */
  record RadarAttackerFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`, `flying`,
   * `boss`. */
  record RadarAttackerFlyingBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`, `flying`,
   * `ground`. */
  record RadarAttackerFlyingGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`, `boss`. */
  record RadarAttackerBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`, `boss`,
   * `ground`. */
  record RadarAttackerBossGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `attacker`, `ground`. */
  record RadarAttackerGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `flying`. */
  record RadarFlyingShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `flying`, `boss`. */
  record RadarFlyingBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `flying`, `boss`,
   * `ground`. */
  record RadarFlyingBossGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `flying`, `ground`. */
  record RadarFlyingGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `boss`. */
  record RadarBossShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `boss`, `ground`. */
  record RadarBossGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `shield` as the metric after filtering them by `ground`. */
  record RadarGroundShield(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric. */
  record RadarArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`. */
  record RadarEnemyArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ally`. */
  record RadarEnemyAllyArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ally`, `player`. */
  record RadarEnemyAllyPlayerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ally`,
   * `attacker`. */
  record RadarEnemyAllyAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ally`, `flying`. */
  record RadarEnemyAllyFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ally`, `ground`. */
  record RadarEnemyAllyGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `player`. */
  record RadarEnemyPlayerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `player`,
   * `attacker`. */
  record RadarEnemyPlayerAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `player`,
   * `flying`. */
  record RadarEnemyPlayerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `player`, `boss`. */
  record RadarEnemyPlayerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `player`,
   * `ground`. */
  record RadarEnemyPlayerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `attacker`. */
  record RadarEnemyAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `attacker`,
   * `flying`. */
  record RadarEnemyAttackerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `attacker`,
   * `boss`. */
  record RadarEnemyAttackerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `attacker`,
   * `ground`. */
  record RadarEnemyAttackerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `flying`. */
  record RadarEnemyFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `flying`, `boss`. */
  record RadarEnemyFlyingBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `flying`,
   * `ground`. */
  record RadarEnemyFlyingGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `boss`. */
  record RadarEnemyBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `boss`, `ground`. */
  record RadarEnemyBossGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `enemy`, `ground`. */
  record RadarEnemyGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`. */
  record RadarAllyArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `player`. */
  record RadarAllyPlayerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `player`,
   * `attacker`. */
  record RadarAllyPlayerAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `player`,
   * `flying`. */
  record RadarAllyPlayerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `player`,
   * `ground`. */
  record RadarAllyPlayerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `attacker`. */
  record RadarAllyAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `attacker`,
   * `flying`. */
  record RadarAllyAttackerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `attacker`,
   * `boss`. */
  record RadarAllyAttackerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `attacker`,
   * `ground`. */
  record RadarAllyAttackerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `flying`. */
  record RadarAllyFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `flying`,
   * `ground`. */
  record RadarAllyFlyingGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `boss`. */
  record RadarAllyBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ally`, `ground`. */
  record RadarAllyGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`. */
  record RadarPlayerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `attacker`. */
  record RadarPlayerAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `attacker`,
   * `flying`. */
  record RadarPlayerAttackerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `attacker`,
   * `boss`. */
  record RadarPlayerAttackerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `attacker`,
   * `ground`. */
  record RadarPlayerAttackerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `flying`. */
  record RadarPlayerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `flying`,
   * `boss`. */
  record RadarPlayerFlyingBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `flying`,
   * `ground`. */
  record RadarPlayerFlyingGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `boss`. */
  record RadarPlayerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `boss`,
   * `ground`. */
  record RadarPlayerBossGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `player`, `ground`. */
  record RadarPlayerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`. */
  record RadarAttackerArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`, `flying`. */
  record RadarAttackerFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`, `flying`,
   * `boss`. */
  record RadarAttackerFlyingBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`, `flying`,
   * `ground`. */
  record RadarAttackerFlyingGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`, `boss`. */
  record RadarAttackerBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`, `boss`,
   * `ground`. */
  record RadarAttackerBossGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `attacker`, `ground`. */
  record RadarAttackerGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `flying`. */
  record RadarFlyingArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `flying`, `boss`. */
  record RadarFlyingBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `flying`, `boss`,
   * `ground`. */
  record RadarFlyingBossGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `flying`, `ground`. */
  record RadarFlyingGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `boss`. */
  record RadarBossArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `boss`, `ground`. */
  record RadarBossGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `armor` as the metric after filtering them by `ground`. */
  record RadarGroundArmor(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric. */
  record RadarMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`. */
  record RadarEnemyMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ally`. */
  record RadarEnemyAllyMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ally`,
   * `player`. */
  record RadarEnemyAllyPlayerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ally`,
   * `attacker`. */
  record RadarEnemyAllyAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ally`,
   * `flying`. */
  record RadarEnemyAllyFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ally`,
   * `boss`. */
  record RadarEnemyAllyBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ally`,
   * `ground`. */
  record RadarEnemyAllyGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `player`. */
  record RadarEnemyPlayerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `player`,
   * `attacker`. */
  record RadarEnemyPlayerAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `player`,
   * `flying`. */
  record RadarEnemyPlayerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `player`,
   * `boss`. */
  record RadarEnemyPlayerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `player`,
   * `ground`. */
  record RadarEnemyPlayerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `attacker`. */
  record RadarEnemyAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `attacker`,
   * `flying`. */
  record RadarEnemyAttackerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `attacker`,
   * `boss`. */
  record RadarEnemyAttackerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `attacker`,
   * `ground`. */
  record RadarEnemyAttackerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `flying`. */
  record RadarEnemyFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `flying`,
   * `boss`. */
  record RadarEnemyFlyingBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `flying`,
   * `ground`. */
  record RadarEnemyFlyingGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `boss`. */
  record RadarEnemyBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `boss`,
   * `ground`. */
  record RadarEnemyBossGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `enemy`, `ground`. */
  record RadarEnemyGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`. */
  record RadarAllyMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `player`. */
  record RadarAllyPlayerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `player`,
   * `attacker`. */
  record RadarAllyPlayerAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `player`,
   * `flying`. */
  record RadarAllyPlayerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `player`,
   * `boss`. */
  record RadarAllyPlayerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `player`,
   * `ground`. */
  record RadarAllyPlayerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `attacker`. */
  record RadarAllyAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `attacker`,
   * `flying`. */
  record RadarAllyAttackerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `attacker`,
   * `boss`. */
  record RadarAllyAttackerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `attacker`,
   * `ground`. */
  record RadarAllyAttackerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `flying`. */
  record RadarAllyFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `flying`,
   * `boss`. */
  record RadarAllyFlyingBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `flying`,
   * `ground`. */
  record RadarAllyFlyingGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `boss`. */
  record RadarAllyBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `boss`,
   * `ground`. */
  record RadarAllyBossGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ally`, `ground`. */
  record RadarAllyGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`. */
  record RadarPlayerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `attacker`. */
  record RadarPlayerAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `attacker`,
   * `flying`. */
  record RadarPlayerAttackerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `attacker`,
   * `boss`. */
  record RadarPlayerAttackerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `attacker`,
   * `ground`. */
  record RadarPlayerAttackerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `flying`. */
  record RadarPlayerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `flying`,
   * `boss`. */
  record RadarPlayerFlyingBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `flying`,
   * `ground`. */
  record RadarPlayerFlyingGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `boss`. */
  record RadarPlayerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `boss`,
   * `ground`. */
  record RadarPlayerBossGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `player`, `ground`. */
  record RadarPlayerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`. */
  record RadarAttackerMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`, `flying`. */
  record RadarAttackerFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`, `flying`,
   * `boss`. */
  record RadarAttackerFlyingBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`, `flying`,
   * `ground`. */
  record RadarAttackerFlyingGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`, `boss`. */
  record RadarAttackerBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`, `boss`,
   * `ground`. */
  record RadarAttackerBossGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `attacker`, `ground`. */
  record RadarAttackerGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `flying`. */
  record RadarFlyingMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `flying`, `boss`. */
  record RadarFlyingBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `flying`, `boss`,
   * `ground`. */
  record RadarFlyingBossGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `flying`, `ground`. */
  record RadarFlyingGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `boss`. */
  record RadarBossMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `boss`, `ground`. */
  record RadarBossGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that finds the first or last unit in a building's range via
   * `maxHealth` as the metric after filtering them by `ground`. */
  record RadarGroundMaxhealth(Register b, Register o, Register u)
    implements Instruction
  {}

  /** Instruction that makes the currently run instruction to change out of
   * sequence. */
  sealed interface Jump extends Instruction {
    /** Waypoint to the instruction the jump will happen to. */
    Waypoint goal();
  }

  /** Jumps that are always taken. */
  record JumpAlways(Waypoint goal) implements Jump {}

  /** Jumps that happen when the condition is true. */
  record JumpOnTrue(Waypoint goal, Register condition) implements Jump {}

  /** Jumps that happen when the condition is false. */
  record JumpOnFalse(Waypoint goal, Register condition) implements Jump {}

  /** Instruction that marks the end of the program. Practically equivalent to
   * jumping back to the first instruction as the processor loops the program
   * when it runs out of instructions or comes to this instruction. */
  record End() implements Instruction {}

  /** Sets the value in the target register to be the same as the value in the
   * source register. */
  record Set(Register target, Register source) implements Instruction {}

  /** Instruction that operates on values. */
  sealed interface Operation extends Instruction {
    /** Identifier that separates this operation from the other operations. */
    String operation_code();

    /** Register the result of the operation will go to. */
    Register target();
  }

  /** Operations with one operand. */
  sealed interface UnaryOperation extends Operation {
    /** Register that holds the operand of this operation. */
    Register operand();
  }

  /** Unary operation that evaluates the NOT of the value bitwise. */
  record BitwiseNot(Register target, Register operand)
    implements UnaryOperation
  {
    @Override
    public String operation_code() { return "not"; }
  }

  /** Operations with two operands. */
  sealed interface BinaryOperation extends Operation {
    /** Register that holds the left operand of this operation. */
    Register left_operand();

    /** Register that holds the right operand of this operation. */
    Register right_operand();
  }

  /** Binary operation that multiplies the values. */
  record Multiplication(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "mul"; }
  }

  /** Binary operation that divides the values. */
  record Division(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "div"; }
  }

  /** Binary operation that divides the values as integers. */
  record IntegerDivision(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "idiv"; }
  }

  /** Binary operation that finds the equivalent of the left value under the
   * modulus of right value. */
  record Modulus(Register target, Register left_operand, Register right_operand)
    implements BinaryOperation
  {
    @Override
    public String operation_code() { return "mod"; }
  }

  /** Binary operation that adds the values. */
  record Addition(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "add"; }
  }

  /** Binary operation that subtracts the values. */
  record Subtraction(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "sub"; }
  }

  /** Binary operation that shifts the left value right value many bits to the
   * left. */
  record LeftShift(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "shl"; }
  }

  /** Binary operation that shifts the left value right value many bits to the
   * right. */
  record RightShift(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "shr"; }
  }

  /** Binary operation that ANDs the values bitwise. */
  record BitwiseAnd(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "and"; }
  }

  /** Binary operation that XORs the values bitwise. */
  record BitwiseXor(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "xor"; }
  }

  /** Binary operation that ORs the values bitwise. */
  record BitwiseOr(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "or"; }
  }

  /** Binary operation that compares whether the left value is smaller than the
   * right one. */
  record LessThan(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "lessThan"; }
  }

  /** Binary operation that compares whether the left value is smaller than or
   * equal to the right one. */
  record LessThanOrEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "lessThanEq"; }
  }

  /** Binary operation that compares whether the left value is bigger than the
   * right one. */
  record GreaterThan(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "greaterThan"; }
  }

  /** Binary operation that compares whether the left value is bigger than or
   * equal to the right one. */
  record GreaterThanOrEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "greaterThanEq"; }
  }

  /** Binary operation that compares whether the values are the same after
   * implicit conversions. */
  record EqualTo(Register target, Register left_operand, Register right_operand)
    implements BinaryOperation
  {
    @Override
    public String operation_code() { return "equal"; }
  }

  /** Binary operation that compares whether the values are not the same after
   * implicit conversions. */
  record NotEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "notEqual"; }
  }

  /** Binary operation that compares whether the values are the same without any
   * implicit conversions. */
  record StrictlyEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "strictEqual"; }
  }
}
