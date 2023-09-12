package hlml.checker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Meaningful constructs in the program. */
public sealed interface Semantic {
  /** Scope that the built-in symbols are defined into. */
  String built_in_scope = "mlog";

  /** Collective understanding of a piece of code. */
  record Target(String name, Map<String, Source> sources) implements Semantic {}

  /** Files that hold the code. */
  record Source(
    Optional<Entrypoint> entrypoint,
    Map<String, Definition> globals) implements Semantic
  {}

  /** Asserting a fact about the program. */
  sealed interface Declaration extends Semantic {
    /** Returns the names of definitions this entity needs before it can be
     * understood. */
    Set<Name> dependencies();
  }

  /** First instructions that are executed by the processor. */
  record Entrypoint(Statement body) implements Declaration {
    @Override
    public Set<Name> dependencies() { return body.dependencies(); }
  }

  /** Definition of a construct in code. */
  sealed interface Definition extends Declaration {
    /** Name of the definition. */
    Name name();
  }

  /** Defining a symbol as building linked to the processor. */
  record Link(Name name, String building) implements Definition {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Definition of a procedure that could be built-in or user-defined. */
  sealed interface Procedure extends Definition {
    /** Parameters this procedure takes. */
    List<Parameter> parameters();
  }

  /** Procedures that are user-defined. */
  record Proc(Name name, List<Parameter> parameters, Statement body)
    implements Procedure
  {
    @Override
    public Set<Name> dependencies() { return body.dependencies(); }
  }

  /** Procedure that compiles to the `read` instruction. */
  record Read() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "read"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("v", true),
          new Parameter("c", false),
          new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `write` instruction. */
  record Write() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "write"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("v", false),
          new Parameter("c", false),
          new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `clear`
   * subinstruction. */
  record DrawClear() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_clear"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("r", false),
          new Parameter("g", false),
          new Parameter("b", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `color`
   * subinstruction. */
  record DrawColor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_color"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("r", false),
          new Parameter("g", false),
          new Parameter("b", false),
          new Parameter("a", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `col`
   * subinstruction. */
  record DrawCol() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_col"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("c", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `stroke`
   * subinstruction. */
  record DrawStroke() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_stroke"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("t", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `line`
   * subinstruction. */
  record DrawLine() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_line"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x0", false),
          new Parameter("y0", false),
          new Parameter("x1", false),
          new Parameter("y1", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `rect`
   * subinstruction. */
  record DrawRect() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_rect"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x", false),
          new Parameter("y", false),
          new Parameter("w", false),
          new Parameter("h", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `lineRect`
   * subinstruction. */
  record DrawLinerect() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_linerect"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x", false),
          new Parameter("y", false),
          new Parameter("w", false),
          new Parameter("h", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `poly`
   * subinstruction. */
  record DrawPoly() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_poly"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x", false),
          new Parameter("y", false),
          new Parameter("n", false),
          new Parameter("r", false),
          new Parameter("a", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `linePoly`
   * subinstruction. */
  record DrawLinepoly() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_linepoly"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x", false),
          new Parameter("y", false),
          new Parameter("n", false),
          new Parameter("r", false),
          new Parameter("a", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `triangle`
   * subinstruction. */
  record DrawTriangle() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_triangle"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x0", false),
          new Parameter("y0", false),
          new Parameter("x1", false),
          new Parameter("y1", false),
          new Parameter("x2", false),
          new Parameter("y2", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `draw` instruction's `image`
   * subinstruction. */
  record DrawImage() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "draw_image"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("x", false),
          new Parameter("y", false),
          new Parameter("i", false),
          new Parameter("r", false),
          new Parameter("a", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `drawflush` instruction. */
  record Drawflush() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "drawflush"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("d", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `packcolor` instruction. */
  record PackColor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "pack_color"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("c", true),
          new Parameter("r", false),
          new Parameter("g", false),
          new Parameter("b", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `print` instruction. */
  record Print() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "print"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("s", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `printflush` instruction. */
  record Printflush() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "printflush"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("m", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `getlink` instruction. */
  record Getlink() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "getlink"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("l", true), new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `control` instruction's `enabled`
   * subinstruction. */
  record ControlEnabled() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "control_enabled"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("b", false), new Parameter("e", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `control` instruction's `shoot`
   * subinstruction. */
  record ControlShoot() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "control_shoot"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("x", false),
          new Parameter("y", false),
          new Parameter("s", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `control` instruction's `shootp`
   * subinstruction. */
  record ControlShootp() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "control_shootp"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("u", false),
          new Parameter("s", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `control` instruction's `config`
   * subinstruction. */
  record ControlConfig() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "control_config"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("b", false), new Parameter("c", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `control` instruction's `color`
   * subinstruction. */
  record ControlColor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "control_color"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("b", false), new Parameter("c", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `sensor` instruction. */
  record Sensor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "sensor"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("r", true),
          new Parameter("t", false),
          new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `wait` instruction. */
  record Wait() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "wait"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("t", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `stop` instruction. */
  record Stop() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "stop"); }

    @Override
    public List<Parameter> parameters() { return List.of(); }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `lookup` instruction's `block`
   * subinstruction. */
  record LookupBlock() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "lookup_block"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("t", true), new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `lookup` instruction's `unit`
   * subinstruction. */
  record LookupUnit() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "lookup_unit"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("t", true), new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `lookup` instruction's `item`
   * subinstruction. */
  record LookupItem() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "lookup_item"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("t", true), new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `lookup` instruction's `liquid`
   * subinstruction. */
  record LookupLiquid() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "lookup_liquid"); }

    @Override
    public List<Parameter> parameters() {
      return List.of(new Parameter("t", true), new Parameter("i", false));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `any`, `any`, `any`. */
  record RadarDistance() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_distance"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `any`, `any`. */
  record RadarEnemyDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ally`, `any`. */
  record RadarEnemyAllyDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ally`, `player`. */
  record RadarEnemyAllyPlayerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_player_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ally`, `attacker`. */
  record RadarEnemyAllyAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ally`, `flying`. */
  record RadarEnemyAllyFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ally`, `ground`. */
  record RadarEnemyAllyGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `player`, `any`. */
  record RadarEnemyPlayerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `player`, `attacker`. */
  record RadarEnemyPlayerAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `player`, `flying`. */
  record RadarEnemyPlayerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `player`, `boss`. */
  record RadarEnemyPlayerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `player`, `ground`. */
  record RadarEnemyPlayerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `attacker`, `any`. */
  record RadarEnemyAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `attacker`, `flying`. */
  record RadarEnemyAttackerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `attacker`, `boss`. */
  record RadarEnemyAttackerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `attacker`, `ground`. */
  record RadarEnemyAttackerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `flying`, `any`. */
  record RadarEnemyFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `flying`, `boss`. */
  record RadarEnemyFlyingBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `flying`, `ground`. */
  record RadarEnemyFlyingGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `boss`, `any`. */
  record RadarEnemyBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `boss`, `ground`. */
  record RadarEnemyBossGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `enemy`, `ground`, `any`. */
  record RadarEnemyGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `any`, `any`. */
  record RadarAllyDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `player`, `any`. */
  record RadarAllyPlayerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `player`, `attacker`. */
  record RadarAllyPlayerAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `player`, `flying`. */
  record RadarAllyPlayerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `player`, `ground`. */
  record RadarAllyPlayerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `attacker`, `any`. */
  record RadarAllyAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `attacker`, `flying`. */
  record RadarAllyAttackerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `attacker`, `boss`. */
  record RadarAllyAttackerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `attacker`, `ground`. */
  record RadarAllyAttackerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `flying`, `any`. */
  record RadarAllyFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `flying`, `ground`. */
  record RadarAllyFlyingGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `boss`, `any`. */
  record RadarAllyBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ally`, `ground`, `any`. */
  record RadarAllyGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `any`, `any`. */
  record RadarPlayerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `attacker`, `any`. */
  record RadarPlayerAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `attacker`, `flying`. */
  record RadarPlayerAttackerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `attacker`, `boss`. */
  record RadarPlayerAttackerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `attacker`, `ground`. */
  record RadarPlayerAttackerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `flying`, `any`. */
  record RadarPlayerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `flying`, `boss`. */
  record RadarPlayerFlyingBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `flying`, `ground`. */
  record RadarPlayerFlyingGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `boss`, `any`. */
  record RadarPlayerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `boss`, `ground`. */
  record RadarPlayerBossGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `player`, `ground`, `any`. */
  record RadarPlayerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `any`, `any`. */
  record RadarAttackerDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `flying`, `any`. */
  record RadarAttackerFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `flying`, `boss`. */
  record RadarAttackerFlyingBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `flying`, `ground`. */
  record RadarAttackerFlyingGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `boss`, `any`. */
  record RadarAttackerBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `boss`, `ground`. */
  record RadarAttackerBossGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `attacker`, `ground`, `any`. */
  record RadarAttackerGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `flying`, `any`, `any`. */
  record RadarFlyingDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `flying`, `boss`, `any`. */
  record RadarFlyingBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `flying`, `boss`, `ground`. */
  record RadarFlyingBossGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `flying`, `ground`, `any`. */
  record RadarFlyingGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `boss`, `any`, `any`. */
  record RadarBossDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `boss`, `ground`, `any`. */
  record RadarBossGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `distance`
   * subinstruction with filters `ground`, `any`, `any`. */
  record RadarGroundDistance() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ground_distance");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `any`, `any`, `any`. */
  record RadarHealth() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_health"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `any`, `any`. */
  record RadarEnemyHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ally`, `any`. */
  record RadarEnemyAllyHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ally`, `player`. */
  record RadarEnemyAllyPlayerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_player_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ally`, `attacker`. */
  record RadarEnemyAllyAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ally`, `flying`. */
  record RadarEnemyAllyFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ally`, `ground`. */
  record RadarEnemyAllyGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `player`, `any`. */
  record RadarEnemyPlayerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `player`, `attacker`. */
  record RadarEnemyPlayerAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `player`, `flying`. */
  record RadarEnemyPlayerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `player`, `boss`. */
  record RadarEnemyPlayerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `player`, `ground`. */
  record RadarEnemyPlayerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `attacker`, `any`. */
  record RadarEnemyAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `attacker`, `flying`. */
  record RadarEnemyAttackerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `attacker`, `boss`. */
  record RadarEnemyAttackerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `attacker`, `ground`. */
  record RadarEnemyAttackerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `flying`, `any`. */
  record RadarEnemyFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `flying`, `boss`. */
  record RadarEnemyFlyingBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `flying`, `ground`. */
  record RadarEnemyFlyingGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `boss`, `any`. */
  record RadarEnemyBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `boss`, `ground`. */
  record RadarEnemyBossGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `enemy`, `ground`, `any`. */
  record RadarEnemyGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `any`, `any`. */
  record RadarAllyHealth() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_ally_health"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `player`, `any`. */
  record RadarAllyPlayerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `player`, `attacker`. */
  record RadarAllyPlayerAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `player`, `flying`. */
  record RadarAllyPlayerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `player`, `ground`. */
  record RadarAllyPlayerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `attacker`, `any`. */
  record RadarAllyAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `attacker`, `flying`. */
  record RadarAllyAttackerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `attacker`, `boss`. */
  record RadarAllyAttackerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `attacker`, `ground`. */
  record RadarAllyAttackerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `flying`, `any`. */
  record RadarAllyFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `flying`, `ground`. */
  record RadarAllyFlyingGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `boss`, `any`. */
  record RadarAllyBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ally`, `ground`, `any`. */
  record RadarAllyGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `any`, `any`. */
  record RadarPlayerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `attacker`, `any`. */
  record RadarPlayerAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `attacker`, `flying`. */
  record RadarPlayerAttackerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `attacker`, `boss`. */
  record RadarPlayerAttackerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `attacker`, `ground`. */
  record RadarPlayerAttackerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `flying`, `any`. */
  record RadarPlayerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `flying`, `boss`. */
  record RadarPlayerFlyingBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `flying`, `ground`. */
  record RadarPlayerFlyingGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `boss`, `any`. */
  record RadarPlayerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `boss`, `ground`. */
  record RadarPlayerBossGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `player`, `ground`, `any`. */
  record RadarPlayerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `any`, `any`. */
  record RadarAttackerHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `flying`, `any`. */
  record RadarAttackerFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `flying`, `boss`. */
  record RadarAttackerFlyingBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `flying`, `ground`. */
  record RadarAttackerFlyingGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `boss`, `any`. */
  record RadarAttackerBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `boss`, `ground`. */
  record RadarAttackerBossGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `attacker`, `ground`, `any`. */
  record RadarAttackerGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `flying`, `any`, `any`. */
  record RadarFlyingHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `flying`, `boss`, `any`. */
  record RadarFlyingBossHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `flying`, `boss`, `ground`. */
  record RadarFlyingBossGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `flying`, `ground`, `any`. */
  record RadarFlyingGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `boss`, `any`, `any`. */
  record RadarBossHealth() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_boss_health"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `boss`, `ground`, `any`. */
  record RadarBossGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `health`
   * subinstruction with filters `ground`, `any`, `any`. */
  record RadarGroundHealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ground_health");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `any`, `any`, `any`. */
  record RadarShield() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_shield"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `any`, `any`. */
  record RadarEnemyShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ally`, `any`. */
  record RadarEnemyAllyShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ally`, `player`. */
  record RadarEnemyAllyPlayerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_player_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ally`, `attacker`. */
  record RadarEnemyAllyAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ally`, `flying`. */
  record RadarEnemyAllyFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ally`, `ground`. */
  record RadarEnemyAllyGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `player`, `any`. */
  record RadarEnemyPlayerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `player`, `attacker`. */
  record RadarEnemyPlayerAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `player`, `flying`. */
  record RadarEnemyPlayerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `player`, `boss`. */
  record RadarEnemyPlayerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `player`, `ground`. */
  record RadarEnemyPlayerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `attacker`, `any`. */
  record RadarEnemyAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `attacker`, `flying`. */
  record RadarEnemyAttackerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `attacker`, `boss`. */
  record RadarEnemyAttackerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `attacker`, `ground`. */
  record RadarEnemyAttackerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `flying`, `any`. */
  record RadarEnemyFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `flying`, `boss`. */
  record RadarEnemyFlyingBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `flying`, `ground`. */
  record RadarEnemyFlyingGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `boss`, `any`. */
  record RadarEnemyBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `boss`, `ground`. */
  record RadarEnemyBossGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `enemy`, `ground`, `any`. */
  record RadarEnemyGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `any`, `any`. */
  record RadarAllyShield() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_ally_shield"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `player`, `any`. */
  record RadarAllyPlayerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `player`, `attacker`. */
  record RadarAllyPlayerAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `player`, `flying`. */
  record RadarAllyPlayerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `player`, `ground`. */
  record RadarAllyPlayerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `attacker`, `any`. */
  record RadarAllyAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `attacker`, `flying`. */
  record RadarAllyAttackerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `attacker`, `boss`. */
  record RadarAllyAttackerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `attacker`, `ground`. */
  record RadarAllyAttackerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `flying`, `any`. */
  record RadarAllyFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `flying`, `ground`. */
  record RadarAllyFlyingGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `boss`, `any`. */
  record RadarAllyBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ally`, `ground`, `any`. */
  record RadarAllyGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `any`, `any`. */
  record RadarPlayerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `attacker`, `any`. */
  record RadarPlayerAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `attacker`, `flying`. */
  record RadarPlayerAttackerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `attacker`, `boss`. */
  record RadarPlayerAttackerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `attacker`, `ground`. */
  record RadarPlayerAttackerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `flying`, `any`. */
  record RadarPlayerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `flying`, `boss`. */
  record RadarPlayerFlyingBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `flying`, `ground`. */
  record RadarPlayerFlyingGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `boss`, `any`. */
  record RadarPlayerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `boss`, `ground`. */
  record RadarPlayerBossGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `player`, `ground`, `any`. */
  record RadarPlayerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `any`, `any`. */
  record RadarAttackerShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `flying`, `any`. */
  record RadarAttackerFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `flying`, `boss`. */
  record RadarAttackerFlyingBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `flying`, `ground`. */
  record RadarAttackerFlyingGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `boss`, `any`. */
  record RadarAttackerBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `boss`, `ground`. */
  record RadarAttackerBossGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `attacker`, `ground`, `any`. */
  record RadarAttackerGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `flying`, `any`, `any`. */
  record RadarFlyingShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `flying`, `boss`, `any`. */
  record RadarFlyingBossShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `flying`, `boss`, `ground`. */
  record RadarFlyingBossGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `flying`, `ground`, `any`. */
  record RadarFlyingGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `boss`, `any`, `any`. */
  record RadarBossShield() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_boss_shield"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `boss`, `ground`, `any`. */
  record RadarBossGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `shield`
   * subinstruction with filters `ground`, `any`, `any`. */
  record RadarGroundShield() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ground_shield");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `any`, `any`, `any`. */
  record RadarArmor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_armor"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `any`, `any`. */
  record RadarEnemyArmor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_enemy_armor"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ally`, `any`. */
  record RadarEnemyAllyArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ally`, `player`. */
  record RadarEnemyAllyPlayerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_player_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ally`, `attacker`. */
  record RadarEnemyAllyAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ally`, `flying`. */
  record RadarEnemyAllyFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ally`, `ground`. */
  record RadarEnemyAllyGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `player`, `any`. */
  record RadarEnemyPlayerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `player`, `attacker`. */
  record RadarEnemyPlayerAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `player`, `flying`. */
  record RadarEnemyPlayerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `player`, `boss`. */
  record RadarEnemyPlayerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `player`, `ground`. */
  record RadarEnemyPlayerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `attacker`, `any`. */
  record RadarEnemyAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `attacker`, `flying`. */
  record RadarEnemyAttackerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `attacker`, `boss`. */
  record RadarEnemyAttackerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `attacker`, `ground`. */
  record RadarEnemyAttackerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `flying`, `any`. */
  record RadarEnemyFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `flying`, `boss`. */
  record RadarEnemyFlyingBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `flying`, `ground`. */
  record RadarEnemyFlyingGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `boss`, `any`. */
  record RadarEnemyBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `boss`, `ground`. */
  record RadarEnemyBossGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `enemy`, `ground`, `any`. */
  record RadarEnemyGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `any`, `any`. */
  record RadarAllyArmor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_ally_armor"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `player`, `any`. */
  record RadarAllyPlayerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `player`, `attacker`. */
  record RadarAllyPlayerAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `player`, `flying`. */
  record RadarAllyPlayerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `player`, `ground`. */
  record RadarAllyPlayerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `attacker`, `any`. */
  record RadarAllyAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `attacker`, `flying`. */
  record RadarAllyAttackerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `attacker`, `boss`. */
  record RadarAllyAttackerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `attacker`, `ground`. */
  record RadarAllyAttackerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `flying`, `any`. */
  record RadarAllyFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `flying`, `ground`. */
  record RadarAllyFlyingGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `boss`, `any`. */
  record RadarAllyBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ally`, `ground`, `any`. */
  record RadarAllyGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `any`, `any`. */
  record RadarPlayerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `attacker`, `any`. */
  record RadarPlayerAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `attacker`, `flying`. */
  record RadarPlayerAttackerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `attacker`, `boss`. */
  record RadarPlayerAttackerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `attacker`, `ground`. */
  record RadarPlayerAttackerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `flying`, `any`. */
  record RadarPlayerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `flying`, `boss`. */
  record RadarPlayerFlyingBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `flying`, `ground`. */
  record RadarPlayerFlyingGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `boss`, `any`. */
  record RadarPlayerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `boss`, `ground`. */
  record RadarPlayerBossGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `player`, `ground`, `any`. */
  record RadarPlayerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `any`, `any`. */
  record RadarAttackerArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `flying`, `any`. */
  record RadarAttackerFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `flying`, `boss`. */
  record RadarAttackerFlyingBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `flying`, `ground`. */
  record RadarAttackerFlyingGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `boss`, `any`. */
  record RadarAttackerBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `boss`, `ground`. */
  record RadarAttackerBossGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `attacker`, `ground`, `any`. */
  record RadarAttackerGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `flying`, `any`, `any`. */
  record RadarFlyingArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `flying`, `boss`, `any`. */
  record RadarFlyingBossArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `flying`, `boss`, `ground`. */
  record RadarFlyingBossGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `flying`, `ground`, `any`. */
  record RadarFlyingGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `boss`, `any`, `any`. */
  record RadarBossArmor() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_boss_armor"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `boss`, `ground`, `any`. */
  record RadarBossGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `armor`
   * subinstruction with filters `ground`, `any`, `any`. */
  record RadarGroundArmor() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ground_armor");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `any`, `any`, `any`. */
  record RadarMaxhealth() implements Procedure {
    @Override
    public Name name() { return new Name(built_in_scope, "radar_maxhealth"); }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `any`, `any`. */
  record RadarEnemyMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ally`, `any`. */
  record RadarEnemyAllyMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ally`, `player`. */
  record RadarEnemyAllyPlayerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_player_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ally`, `attacker`. */
  record RadarEnemyAllyAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ally`, `flying`. */
  record RadarEnemyAllyFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ally`, `boss`. */
  record RadarEnemyAllyBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ally`, `ground`. */
  record RadarEnemyAllyGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ally_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `player`, `any`. */
  record RadarEnemyPlayerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `player`, `attacker`. */
  record RadarEnemyPlayerAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `player`, `flying`. */
  record RadarEnemyPlayerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `player`, `boss`. */
  record RadarEnemyPlayerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `player`, `ground`. */
  record RadarEnemyPlayerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_player_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `attacker`, `any`. */
  record RadarEnemyAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `attacker`, `flying`. */
  record RadarEnemyAttackerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `attacker`, `boss`. */
  record RadarEnemyAttackerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `attacker`, `ground`. */
  record RadarEnemyAttackerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_attacker_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `flying`, `any`. */
  record RadarEnemyFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `flying`, `boss`. */
  record RadarEnemyFlyingBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `flying`, `ground`. */
  record RadarEnemyFlyingGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_flying_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `boss`, `any`. */
  record RadarEnemyBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `boss`, `ground`. */
  record RadarEnemyBossGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_boss_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `enemy`, `ground`, `any`. */
  record RadarEnemyGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_enemy_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `any`, `any`. */
  record RadarAllyMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `player`, `any`. */
  record RadarAllyPlayerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `player`, `attacker`. */
  record RadarAllyPlayerAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `player`, `flying`. */
  record RadarAllyPlayerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `player`, `boss`. */
  record RadarAllyPlayerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `player`, `ground`. */
  record RadarAllyPlayerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_player_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `attacker`, `any`. */
  record RadarAllyAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `attacker`, `flying`. */
  record RadarAllyAttackerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `attacker`, `boss`. */
  record RadarAllyAttackerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `attacker`, `ground`. */
  record RadarAllyAttackerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_attacker_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `flying`, `any`. */
  record RadarAllyFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `flying`, `boss`. */
  record RadarAllyFlyingBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `flying`, `ground`. */
  record RadarAllyFlyingGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_flying_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `boss`, `any`. */
  record RadarAllyBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `boss`, `ground`. */
  record RadarAllyBossGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_boss_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ally`, `ground`, `any`. */
  record RadarAllyGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ally_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `any`, `any`. */
  record RadarPlayerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `attacker`, `any`. */
  record RadarPlayerAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `attacker`, `flying`. */
  record RadarPlayerAttackerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `attacker`, `boss`. */
  record RadarPlayerAttackerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `attacker`, `ground`. */
  record RadarPlayerAttackerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_attacker_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `flying`, `any`. */
  record RadarPlayerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `flying`, `boss`. */
  record RadarPlayerFlyingBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `flying`, `ground`. */
  record RadarPlayerFlyingGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_flying_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `boss`, `any`. */
  record RadarPlayerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `boss`, `ground`. */
  record RadarPlayerBossGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_boss_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `player`, `ground`, `any`. */
  record RadarPlayerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_player_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `any`, `any`. */
  record RadarAttackerMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `flying`, `any`. */
  record RadarAttackerFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `flying`, `boss`. */
  record RadarAttackerFlyingBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `flying`, `ground`. */
  record RadarAttackerFlyingGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_flying_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `boss`, `any`. */
  record RadarAttackerBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `boss`, `ground`. */
  record RadarAttackerBossGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_boss_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `attacker`, `ground`, `any`. */
  record RadarAttackerGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_attacker_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `flying`, `any`, `any`. */
  record RadarFlyingMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `flying`, `boss`, `any`. */
  record RadarFlyingBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `flying`, `boss`, `ground`. */
  record RadarFlyingBossGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_boss_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `flying`, `ground`, `any`. */
  record RadarFlyingGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_flying_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `boss`, `any`, `any`. */
  record RadarBossMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `boss`, `ground`, `any`. */
  record RadarBossGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_boss_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedure that compiles to the `radar` instruction's `maxHealth`
   * subinstruction with filters `ground`, `any`, `any`. */
  record RadarGroundMaxhealth() implements Procedure {
    @Override
    public Name name() {
      return new Name(built_in_scope, "radar_ground_maxhealth");
    }

    @Override
    public List<Parameter> parameters() {
      return List
        .of(
          new Parameter("b", false),
          new Parameter("o", false),
          new Parameter("u", true));
    }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Definition of a procedure's parameter. */
  record Parameter(String identifier, boolean in_out) implements Semantic {}

  /** Definition of a constant. */
  record Const(Name name, Constant value) implements Definition {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Definition of a global variable. */
  record GlobalVar(Name name, Optional<Expression> initial_value)
    implements Definition
  {
    @Override
    public Set<Name> dependencies() {
      return initial_value.map(Expression::dependencies).orElseGet(Set::of);
    }
  }

  /** Instructions to be executed by the processor. */
  sealed interface Statement extends Semantic {
    /** Returns the names of definitions this entity needs before it can be
     * understood. */
    Set<Name> dependencies();
  }

  /** Statements that are sequentially executed. */
  record Block(List<Statement> inner_statements) implements Statement {
    @Override
    public Set<Name> dependencies() {
      return Sets.union(inner_statements.stream().map(Statement::dependencies));
    }
  }

  /** Statements that branch the control flow. */
  record If(
    List<LocalVar> variables,
    Expression condition,
    Statement true_branch,
    Optional<Statement> false_branch) implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return Sets
        .union(
          Sets.union(variables.stream().map(LocalVar::dependencies)),
          condition.dependencies(),
          true_branch.dependencies(),
          false_branch.map(Statement::dependencies).orElseGet(Set::of));
    }
  }

  /** Statements that loop the control flow. */
  record While(
    List<LocalVar> variables,
    Expression condition,
    Optional<Statement> interleaved,
    Statement loop,
    Optional<Statement> zero_branch) implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return Sets
        .union(
          Sets.union(variables.stream().map(LocalVar::dependencies)),
          condition.dependencies(),
          interleaved.map(Statement::dependencies).orElseGet(Set::of),
          loop.dependencies(),
          zero_branch.map(Statement::dependencies).orElseGet(Set::of));
    }
  }

  /** Statements that exit a loop. */
  record Break(int loop) implements Statement {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Statements that skip the remaining in a loop. */
  record Continue(int loop) implements Statement {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Statements that provide a value to the procedures caller. */
  record Return(Optional<Expression> value) implements Statement {
    @Override
    public Set<Name> dependencies() {
      return value.map(Expression::dependencies).orElseGet(Set::of);
    }
  }

  /** Definition of a local variable. */
  record LocalVar(String identifier, Optional<Expression> initial_value)
    implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return initial_value.map(Expression::dependencies).orElseGet(Set::of);
    }
  }

  /** Statements that affect the processors context. Useful for parsing as all
   * the initial tokens of these statements are same as expressions. */
  sealed interface Affect extends Statement {}

  /** Statements that mutate a variable. */
  sealed interface Mutate extends Affect {
    /** The mutated variable. */
    VariableAccess target();

    @Override
    default Set<Name> dependencies() { return target().dependencies(); }
  }

  /** Statements that increment the value hold in a variable. */
  record Increment(VariableAccess target) implements Mutate {}

  /** Statements that decrement the value hold in a variable. */
  record Decrement(VariableAccess target) implements Mutate {}

  /** Statements that set the value hold in a variable. */
  sealed interface Assign extends Affect {
    /** The changed variable. */
    VariableAccess target();

    /** The new value or the right operand. */
    Expression source();

    @Override
    default Set<Name> dependencies() {
      return Sets.union(target().dependencies(), source().dependencies());
    }
  }

  /** Statements that set the target to be the same as the source. */
  record DirectlyAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the multiplication of the target and
   * the source. */
  record MultiplyAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the division of the target and the
   * source. */
  record DivideAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the integer division of the target
   * and the source. */
  record DivideIntegerAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the modulus of the target and the
   * source. */
  record ModulusAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the addition of the target and the
   * source. */
  record AddAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the subtract of the target and the
   * source. */
  record SubtractAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the left shift of the target and the
   * source. */
  record ShiftLeftAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the right shift of the target and the
   * source. */
  record ShiftRightAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise and of the target and the
   * source. */
  record AndBitwiseAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise xor of the target and the
   * source. */
  record XorBitwiseAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise or of the target and the
   * source. */
  record OrBitwiseAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that evaluate an expression and discard its value. Useful for
   * side effects, not the value. */
  record Discard(Expression source) implements Affect {
    @Override
    public Set<Name> dependencies() { return source.dependencies(); }
  }

  /** Value calculations to be evaluated by the processor. */
  sealed interface Expression extends Semantic {
    /** Returns the names of definitions this entity needs before it can be
     * understood. */
    Set<Name> dependencies();
  }

  /** Expression made up of one operand and an operator at the left. */
  sealed interface UnaryOperation extends Expression {
    /** Operand of the operator. */
    Expression operand();

    @Override
    default Set<Name> dependencies() { return operand().dependencies(); }
  }

  /** Expression made up of two operands and an operator in the middle. */
  sealed interface BinaryOperation extends Expression {
    /** Operand that is at the left of the operator. */
    Expression left_operand();

    /** Operand that is at the right of the operator. */
    Expression right_operand();

    @Override
    default Set<Name> dependencies() {
      return Sets
        .union(left_operand().dependencies(), right_operand().dependencies());
    }
  }

  /** Expression that yields one when the left operand is equal to the right
   * operand, and zero otherwise. */
  record EqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is not equal to the right
   * operand, and zero otherwise. */
  record NotEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is equal to the right
   * operand without any operand undergoing implicit conversions, and zero
   * otherwise. */
  record StrictlyEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is less than the right
   * operand, and zero otherwise. */
  record LessThan(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is less than or equal to
   * the right operand, and zero otherwise. */
  record LessThanOrEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than the right
   * operand, and zero otherwise. */
  record GreaterThan(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than or equal
   * to the right operand, and zero otherwise. */
  record GreaterThanOrEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields a number that has the bit pattern that is the OR'ed
   * version of its operands matching bits. */
  record BitwiseOr(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields a number that has the bit pattern that is the
   * XOR'ed version of its operands matching bits. */
  record BitwiseXor(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields a number that has the bit pattern that is the
   * AND'ed version of its operands matching bits. */
  record BitwiseAnd(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the left operand's bits shifted left by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record LeftShift(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the left operand's bits shifted right by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record RightShift(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the addition of its operands. */
  record Addition(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the subtraction of its operands. */
  record Subtraction(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the multiplication of its operands. */
  record Multiplication(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the division of its operands. */
  record Division(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the floor of the division of its operands. */
  record IntegerDivision(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the left operand in mod right operand. */
  record Modulus(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that keeps the sign of a number. */
  record Promotion(Expression operand) implements UnaryOperation {}

  /** Expression that flips the sign of a number. */
  record Negation(Expression operand) implements UnaryOperation {}

  /** Expression that flips every bit when the value is taken as an 53-bit
   * signed two's complement integer. */
  record BitwiseNot(Expression operand) implements UnaryOperation {}

  /** Expression that yields one when the operand is zero, and zero
   * otherwise. */
  record LogicalNot(Expression operand) implements UnaryOperation {}

  /** Expression that evaluates to the value held by a symbol. */
  sealed interface SymbolAccess extends Expression {}

  /** Expression that has a known value. */
  sealed interface Constant extends SymbolAccess {
    @Override
    default Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to a hard-coded number value. */
  record NumberConstant(double value) implements Constant {}

  /** Expression that evaluates to a hard-coded color value. */
  record ColorConstant(int value) implements Constant {}

  /** Expression that evaluates to a hard-coded string value. */
  record StringConstant(String value) implements Constant {}

  /** Expression that evaluates to a link. */
  record LinkAccess(String building) implements SymbolAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to the value held by a variable. */
  sealed interface VariableAccess extends SymbolAccess {}

  /** Expression that evaluates to a global variable's value. */
  record GlobalVariableAccess(Name name) implements VariableAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(name); }
  }

  /** Expression that evaluates to a local variable's value. */
  record LocalVariableAccess(String identifier) implements VariableAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to the return value of executing a procedure
   * with a given argument list. */
  record Call(Name procedure, List<Expression> arguments)
    implements Expression
  {
    @Override
    public Set<Name> dependencies() {
      return Sets
        .union(
          Set.of(procedure),
          Sets.union(arguments.stream().map(Expression::dependencies)));
    }
  }
}
