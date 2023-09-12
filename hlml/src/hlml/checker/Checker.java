package hlml.checker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import hlml.Source;
import hlml.reporter.Subject;
import hlml.resolver.ResolvedSource;
import hlml.resolver.Resolver;

/** Semantically analyzes a target. */
public final class Checker {
  /** Checks a target. */
  public static Semantic.Target check(
    Subject subject,
    Path artifacts,
    List<Path> includes,
    String name)
  {
    Checker checker = new Checker(subject, artifacts, includes, name);
    return checker.check();
  }

  /** Subject that is reported when the target is not found. */
  private final Subject subject;

  /** Path to the directory where compilation artifacts can be recorded to. */
  private final Path artifacts;

  /** Ordered collection of directories to look for a source file by its
   * name. */
  private final List<Path> includes;

  /** Name of the checked target. */
  private final String name;

  /** Checked sources depended on by the target. */
  private Map<String, Semantic.Source> sources;

  /** Sources that are being checked. */
  private Set<String> currently_checked;

  /** Constructor. */
  private Checker(
    Subject subject,
    Path artifacts,
    List<Path> includes,
    String name)
  {
    this.subject = subject;
    this.artifacts = artifacts;
    this.includes = includes;
    this.name = name;
  }

  /** Checks the target. */
  private Semantic.Target check() {
    try {
      Files.createDirectories(artifacts);
    }
    catch (IOException cause) {
      throw Subject
        .of(artifacts)
        .to_diagnostic("failure", "Could not create the artifact directory!")
        .to_exception(cause);
    }
    sources = new HashMap<>();
    sources
      .put(
        Semantic.built_in_scope,
        new Semantic.Source(
          Optional.empty(),
          Set
            .of(
              new Semantic.Read(),
              new Semantic.Write(),
              new Semantic.DrawClear(),
              new Semantic.DrawColor(),
              new Semantic.DrawCol(),
              new Semantic.DrawStroke(),
              new Semantic.DrawLine(),
              new Semantic.DrawRect(),
              new Semantic.DrawLinerect(),
              new Semantic.DrawPoly(),
              new Semantic.DrawLinepoly(),
              new Semantic.DrawTriangle(),
              new Semantic.DrawImage(),
              new Semantic.Drawflush(),
              new Semantic.PackColor(),
              new Semantic.Print(),
              new Semantic.Printflush(),
              new Semantic.Getlink(),
              new Semantic.ControlEnabled(),
              new Semantic.ControlShoot(),
              new Semantic.ControlShootp(),
              new Semantic.ControlConfig(),
              new Semantic.ControlColor(),
              new Semantic.Sensor(),
              new Semantic.Wait(),
              new Semantic.Stop(),
              new Semantic.LookupBlock(),
              new Semantic.LookupUnit(),
              new Semantic.LookupItem(),
              new Semantic.LookupLiquid(),
              new Semantic.Ubind(),
              new Semantic.UcontrolIdle(),
              new Semantic.UcontrolStop(),
              new Semantic.UcontrolMove(),
              new Semantic.UcontrolApproach(),
              new Semantic.UcontrolPathfind(),
              new Semantic.UcontrolAutopathfind(),
              new Semantic.UcontrolBoost(),
              new Semantic.UcontrolTarget(),
              new Semantic.UcontrolTargetp(),
              new Semantic.UcontrolItemdrop(),
              new Semantic.UcontrolItemtake(),
              new Semantic.UcontrolPaydrop(),
              new Semantic.UcontrolPaytake(),
              new Semantic.UcontrolPayenter(),
              new Semantic.UcontrolMine(),
              new Semantic.UcontrolFlag(),
              new Semantic.UcontrolBuild(),
              new Semantic.UcontrolGetblock(),
              new Semantic.UcontrolWithin(),
              new Semantic.UcontrolUnbind(),
              new Semantic.RadarDistance(),
              new Semantic.RadarEnemyDistance(),
              new Semantic.RadarEnemyAllyDistance(),
              new Semantic.RadarEnemyAllyPlayerDistance(),
              new Semantic.RadarEnemyAllyAttackerDistance(),
              new Semantic.RadarEnemyAllyFlyingDistance(),
              new Semantic.RadarEnemyAllyBossDistance(),
              new Semantic.RadarEnemyAllyGroundDistance(),
              new Semantic.RadarEnemyPlayerDistance(),
              new Semantic.RadarEnemyPlayerAttackerDistance(),
              new Semantic.RadarEnemyPlayerFlyingDistance(),
              new Semantic.RadarEnemyPlayerBossDistance(),
              new Semantic.RadarEnemyPlayerGroundDistance(),
              new Semantic.RadarEnemyAttackerDistance(),
              new Semantic.RadarEnemyAttackerFlyingDistance(),
              new Semantic.RadarEnemyAttackerBossDistance(),
              new Semantic.RadarEnemyAttackerGroundDistance(),
              new Semantic.RadarEnemyFlyingDistance(),
              new Semantic.RadarEnemyFlyingBossDistance(),
              new Semantic.RadarEnemyFlyingGroundDistance(),
              new Semantic.RadarEnemyBossDistance(),
              new Semantic.RadarEnemyBossGroundDistance(),
              new Semantic.RadarEnemyGroundDistance(),
              new Semantic.RadarAllyDistance(),
              new Semantic.RadarAllyPlayerDistance(),
              new Semantic.RadarAllyPlayerAttackerDistance(),
              new Semantic.RadarAllyPlayerFlyingDistance(),
              new Semantic.RadarAllyPlayerBossDistance(),
              new Semantic.RadarAllyPlayerGroundDistance(),
              new Semantic.RadarAllyAttackerDistance(),
              new Semantic.RadarAllyAttackerFlyingDistance(),
              new Semantic.RadarAllyAttackerBossDistance(),
              new Semantic.RadarAllyAttackerGroundDistance(),
              new Semantic.RadarAllyFlyingDistance(),
              new Semantic.RadarAllyFlyingBossDistance(),
              new Semantic.RadarAllyFlyingGroundDistance(),
              new Semantic.RadarAllyBossDistance(),
              new Semantic.RadarAllyBossGroundDistance(),
              new Semantic.RadarAllyGroundDistance(),
              new Semantic.RadarPlayerDistance(),
              new Semantic.RadarPlayerAttackerDistance(),
              new Semantic.RadarPlayerAttackerFlyingDistance(),
              new Semantic.RadarPlayerAttackerBossDistance(),
              new Semantic.RadarPlayerAttackerGroundDistance(),
              new Semantic.RadarPlayerFlyingDistance(),
              new Semantic.RadarPlayerFlyingBossDistance(),
              new Semantic.RadarPlayerFlyingGroundDistance(),
              new Semantic.RadarPlayerBossDistance(),
              new Semantic.RadarPlayerBossGroundDistance(),
              new Semantic.RadarPlayerGroundDistance(),
              new Semantic.RadarAttackerDistance(),
              new Semantic.RadarAttackerFlyingDistance(),
              new Semantic.RadarAttackerFlyingBossDistance(),
              new Semantic.RadarAttackerFlyingGroundDistance(),
              new Semantic.RadarAttackerBossDistance(),
              new Semantic.RadarAttackerBossGroundDistance(),
              new Semantic.RadarAttackerGroundDistance(),
              new Semantic.RadarFlyingDistance(),
              new Semantic.RadarFlyingBossDistance(),
              new Semantic.RadarFlyingBossGroundDistance(),
              new Semantic.RadarFlyingGroundDistance(),
              new Semantic.RadarBossDistance(),
              new Semantic.RadarBossGroundDistance(),
              new Semantic.RadarGroundDistance(),
              new Semantic.RadarHealth(),
              new Semantic.RadarEnemyHealth(),
              new Semantic.RadarEnemyAllyHealth(),
              new Semantic.RadarEnemyAllyPlayerHealth(),
              new Semantic.RadarEnemyAllyAttackerHealth(),
              new Semantic.RadarEnemyAllyFlyingHealth(),
              new Semantic.RadarEnemyAllyBossHealth(),
              new Semantic.RadarEnemyAllyGroundHealth(),
              new Semantic.RadarEnemyPlayerHealth(),
              new Semantic.RadarEnemyPlayerAttackerHealth(),
              new Semantic.RadarEnemyPlayerFlyingHealth(),
              new Semantic.RadarEnemyPlayerBossHealth(),
              new Semantic.RadarEnemyPlayerGroundHealth(),
              new Semantic.RadarEnemyAttackerHealth(),
              new Semantic.RadarEnemyAttackerFlyingHealth(),
              new Semantic.RadarEnemyAttackerBossHealth(),
              new Semantic.RadarEnemyAttackerGroundHealth(),
              new Semantic.RadarEnemyFlyingHealth(),
              new Semantic.RadarEnemyFlyingBossHealth(),
              new Semantic.RadarEnemyFlyingGroundHealth(),
              new Semantic.RadarEnemyBossHealth(),
              new Semantic.RadarEnemyBossGroundHealth(),
              new Semantic.RadarEnemyGroundHealth(),
              new Semantic.RadarAllyHealth(),
              new Semantic.RadarAllyPlayerHealth(),
              new Semantic.RadarAllyPlayerAttackerHealth(),
              new Semantic.RadarAllyPlayerFlyingHealth(),
              new Semantic.RadarAllyPlayerBossHealth(),
              new Semantic.RadarAllyPlayerGroundHealth(),
              new Semantic.RadarAllyAttackerHealth(),
              new Semantic.RadarAllyAttackerFlyingHealth(),
              new Semantic.RadarAllyAttackerBossHealth(),
              new Semantic.RadarAllyAttackerGroundHealth(),
              new Semantic.RadarAllyFlyingHealth(),
              new Semantic.RadarAllyFlyingBossHealth(),
              new Semantic.RadarAllyFlyingGroundHealth(),
              new Semantic.RadarAllyBossHealth(),
              new Semantic.RadarAllyBossGroundHealth(),
              new Semantic.RadarAllyGroundHealth(),
              new Semantic.RadarPlayerHealth(),
              new Semantic.RadarPlayerAttackerHealth(),
              new Semantic.RadarPlayerAttackerFlyingHealth(),
              new Semantic.RadarPlayerAttackerBossHealth(),
              new Semantic.RadarPlayerAttackerGroundHealth(),
              new Semantic.RadarPlayerFlyingHealth(),
              new Semantic.RadarPlayerFlyingBossHealth(),
              new Semantic.RadarPlayerFlyingGroundHealth(),
              new Semantic.RadarPlayerBossHealth(),
              new Semantic.RadarPlayerBossGroundHealth(),
              new Semantic.RadarPlayerGroundHealth(),
              new Semantic.RadarAttackerHealth(),
              new Semantic.RadarAttackerFlyingHealth(),
              new Semantic.RadarAttackerFlyingBossHealth(),
              new Semantic.RadarAttackerFlyingGroundHealth(),
              new Semantic.RadarAttackerBossHealth(),
              new Semantic.RadarAttackerBossGroundHealth(),
              new Semantic.RadarAttackerGroundHealth(),
              new Semantic.RadarFlyingHealth(),
              new Semantic.RadarFlyingBossHealth(),
              new Semantic.RadarFlyingBossGroundHealth(),
              new Semantic.RadarFlyingGroundHealth(),
              new Semantic.RadarBossHealth(),
              new Semantic.RadarBossGroundHealth(),
              new Semantic.RadarGroundHealth(),
              new Semantic.RadarShield(),
              new Semantic.RadarEnemyShield(),
              new Semantic.RadarEnemyAllyShield(),
              new Semantic.RadarEnemyAllyPlayerShield(),
              new Semantic.RadarEnemyAllyAttackerShield(),
              new Semantic.RadarEnemyAllyFlyingShield(),
              new Semantic.RadarEnemyAllyBossShield(),
              new Semantic.RadarEnemyAllyGroundShield(),
              new Semantic.RadarEnemyPlayerShield(),
              new Semantic.RadarEnemyPlayerAttackerShield(),
              new Semantic.RadarEnemyPlayerFlyingShield(),
              new Semantic.RadarEnemyPlayerBossShield(),
              new Semantic.RadarEnemyPlayerGroundShield(),
              new Semantic.RadarEnemyAttackerShield(),
              new Semantic.RadarEnemyAttackerFlyingShield(),
              new Semantic.RadarEnemyAttackerBossShield(),
              new Semantic.RadarEnemyAttackerGroundShield(),
              new Semantic.RadarEnemyFlyingShield(),
              new Semantic.RadarEnemyFlyingBossShield(),
              new Semantic.RadarEnemyFlyingGroundShield(),
              new Semantic.RadarEnemyBossShield(),
              new Semantic.RadarEnemyBossGroundShield(),
              new Semantic.RadarEnemyGroundShield(),
              new Semantic.RadarAllyShield(),
              new Semantic.RadarAllyPlayerShield(),
              new Semantic.RadarAllyPlayerAttackerShield(),
              new Semantic.RadarAllyPlayerFlyingShield(),
              new Semantic.RadarAllyPlayerBossShield(),
              new Semantic.RadarAllyPlayerGroundShield(),
              new Semantic.RadarAllyAttackerShield(),
              new Semantic.RadarAllyAttackerFlyingShield(),
              new Semantic.RadarAllyAttackerBossShield(),
              new Semantic.RadarAllyAttackerGroundShield(),
              new Semantic.RadarAllyFlyingShield(),
              new Semantic.RadarAllyFlyingBossShield(),
              new Semantic.RadarAllyFlyingGroundShield(),
              new Semantic.RadarAllyBossShield(),
              new Semantic.RadarAllyBossGroundShield(),
              new Semantic.RadarAllyGroundShield(),
              new Semantic.RadarPlayerShield(),
              new Semantic.RadarPlayerAttackerShield(),
              new Semantic.RadarPlayerAttackerFlyingShield(),
              new Semantic.RadarPlayerAttackerBossShield(),
              new Semantic.RadarPlayerAttackerGroundShield(),
              new Semantic.RadarPlayerFlyingShield(),
              new Semantic.RadarPlayerFlyingBossShield(),
              new Semantic.RadarPlayerFlyingGroundShield(),
              new Semantic.RadarPlayerBossShield(),
              new Semantic.RadarPlayerBossGroundShield(),
              new Semantic.RadarPlayerGroundShield(),
              new Semantic.RadarAttackerShield(),
              new Semantic.RadarAttackerFlyingShield(),
              new Semantic.RadarAttackerFlyingBossShield(),
              new Semantic.RadarAttackerFlyingGroundShield(),
              new Semantic.RadarAttackerBossShield(),
              new Semantic.RadarAttackerBossGroundShield(),
              new Semantic.RadarAttackerGroundShield(),
              new Semantic.RadarFlyingShield(),
              new Semantic.RadarFlyingBossShield(),
              new Semantic.RadarFlyingBossGroundShield(),
              new Semantic.RadarFlyingGroundShield(),
              new Semantic.RadarBossShield(),
              new Semantic.RadarBossGroundShield(),
              new Semantic.RadarGroundShield(),
              new Semantic.RadarArmor(),
              new Semantic.RadarEnemyArmor(),
              new Semantic.RadarEnemyAllyArmor(),
              new Semantic.RadarEnemyAllyPlayerArmor(),
              new Semantic.RadarEnemyAllyAttackerArmor(),
              new Semantic.RadarEnemyAllyFlyingArmor(),
              new Semantic.RadarEnemyAllyBossArmor(),
              new Semantic.RadarEnemyAllyGroundArmor(),
              new Semantic.RadarEnemyPlayerArmor(),
              new Semantic.RadarEnemyPlayerAttackerArmor(),
              new Semantic.RadarEnemyPlayerFlyingArmor(),
              new Semantic.RadarEnemyPlayerBossArmor(),
              new Semantic.RadarEnemyPlayerGroundArmor(),
              new Semantic.RadarEnemyAttackerArmor(),
              new Semantic.RadarEnemyAttackerFlyingArmor(),
              new Semantic.RadarEnemyAttackerBossArmor(),
              new Semantic.RadarEnemyAttackerGroundArmor(),
              new Semantic.RadarEnemyFlyingArmor(),
              new Semantic.RadarEnemyFlyingBossArmor(),
              new Semantic.RadarEnemyFlyingGroundArmor(),
              new Semantic.RadarEnemyBossArmor(),
              new Semantic.RadarEnemyBossGroundArmor(),
              new Semantic.RadarEnemyGroundArmor(),
              new Semantic.RadarAllyArmor(),
              new Semantic.RadarAllyPlayerArmor(),
              new Semantic.RadarAllyPlayerAttackerArmor(),
              new Semantic.RadarAllyPlayerFlyingArmor(),
              new Semantic.RadarAllyPlayerBossArmor(),
              new Semantic.RadarAllyPlayerGroundArmor(),
              new Semantic.RadarAllyAttackerArmor(),
              new Semantic.RadarAllyAttackerFlyingArmor(),
              new Semantic.RadarAllyAttackerBossArmor(),
              new Semantic.RadarAllyAttackerGroundArmor(),
              new Semantic.RadarAllyFlyingArmor(),
              new Semantic.RadarAllyFlyingBossArmor(),
              new Semantic.RadarAllyFlyingGroundArmor(),
              new Semantic.RadarAllyBossArmor(),
              new Semantic.RadarAllyBossGroundArmor(),
              new Semantic.RadarAllyGroundArmor(),
              new Semantic.RadarPlayerArmor(),
              new Semantic.RadarPlayerAttackerArmor(),
              new Semantic.RadarPlayerAttackerFlyingArmor(),
              new Semantic.RadarPlayerAttackerBossArmor(),
              new Semantic.RadarPlayerAttackerGroundArmor(),
              new Semantic.RadarPlayerFlyingArmor(),
              new Semantic.RadarPlayerFlyingBossArmor(),
              new Semantic.RadarPlayerFlyingGroundArmor(),
              new Semantic.RadarPlayerBossArmor(),
              new Semantic.RadarPlayerBossGroundArmor(),
              new Semantic.RadarPlayerGroundArmor(),
              new Semantic.RadarAttackerArmor(),
              new Semantic.RadarAttackerFlyingArmor(),
              new Semantic.RadarAttackerFlyingBossArmor(),
              new Semantic.RadarAttackerFlyingGroundArmor(),
              new Semantic.RadarAttackerBossArmor(),
              new Semantic.RadarAttackerBossGroundArmor(),
              new Semantic.RadarAttackerGroundArmor(),
              new Semantic.RadarFlyingArmor(),
              new Semantic.RadarFlyingBossArmor(),
              new Semantic.RadarFlyingBossGroundArmor(),
              new Semantic.RadarFlyingGroundArmor(),
              new Semantic.RadarBossArmor(),
              new Semantic.RadarBossGroundArmor(),
              new Semantic.RadarGroundArmor(),
              new Semantic.RadarMaxhealth(),
              new Semantic.RadarEnemyMaxhealth(),
              new Semantic.RadarEnemyAllyMaxhealth(),
              new Semantic.RadarEnemyAllyPlayerMaxhealth(),
              new Semantic.RadarEnemyAllyAttackerMaxhealth(),
              new Semantic.RadarEnemyAllyFlyingMaxhealth(),
              new Semantic.RadarEnemyAllyBossMaxhealth(),
              new Semantic.RadarEnemyAllyGroundMaxhealth(),
              new Semantic.RadarEnemyPlayerMaxhealth(),
              new Semantic.RadarEnemyPlayerAttackerMaxhealth(),
              new Semantic.RadarEnemyPlayerFlyingMaxhealth(),
              new Semantic.RadarEnemyPlayerBossMaxhealth(),
              new Semantic.RadarEnemyPlayerGroundMaxhealth(),
              new Semantic.RadarEnemyAttackerMaxhealth(),
              new Semantic.RadarEnemyAttackerFlyingMaxhealth(),
              new Semantic.RadarEnemyAttackerBossMaxhealth(),
              new Semantic.RadarEnemyAttackerGroundMaxhealth(),
              new Semantic.RadarEnemyFlyingMaxhealth(),
              new Semantic.RadarEnemyFlyingBossMaxhealth(),
              new Semantic.RadarEnemyFlyingGroundMaxhealth(),
              new Semantic.RadarEnemyBossMaxhealth(),
              new Semantic.RadarEnemyBossGroundMaxhealth(),
              new Semantic.RadarEnemyGroundMaxhealth(),
              new Semantic.RadarAllyMaxhealth(),
              new Semantic.RadarAllyPlayerMaxhealth(),
              new Semantic.RadarAllyPlayerAttackerMaxhealth(),
              new Semantic.RadarAllyPlayerFlyingMaxhealth(),
              new Semantic.RadarAllyPlayerBossMaxhealth(),
              new Semantic.RadarAllyPlayerGroundMaxhealth(),
              new Semantic.RadarAllyAttackerMaxhealth(),
              new Semantic.RadarAllyAttackerFlyingMaxhealth(),
              new Semantic.RadarAllyAttackerBossMaxhealth(),
              new Semantic.RadarAllyAttackerGroundMaxhealth(),
              new Semantic.RadarAllyFlyingMaxhealth(),
              new Semantic.RadarAllyFlyingBossMaxhealth(),
              new Semantic.RadarAllyFlyingGroundMaxhealth(),
              new Semantic.RadarAllyBossMaxhealth(),
              new Semantic.RadarAllyBossGroundMaxhealth(),
              new Semantic.RadarAllyGroundMaxhealth(),
              new Semantic.RadarPlayerMaxhealth(),
              new Semantic.RadarPlayerAttackerMaxhealth(),
              new Semantic.RadarPlayerAttackerFlyingMaxhealth(),
              new Semantic.RadarPlayerAttackerBossMaxhealth(),
              new Semantic.RadarPlayerAttackerGroundMaxhealth(),
              new Semantic.RadarPlayerFlyingMaxhealth(),
              new Semantic.RadarPlayerFlyingBossMaxhealth(),
              new Semantic.RadarPlayerFlyingGroundMaxhealth(),
              new Semantic.RadarPlayerBossMaxhealth(),
              new Semantic.RadarPlayerBossGroundMaxhealth(),
              new Semantic.RadarPlayerGroundMaxhealth(),
              new Semantic.RadarAttackerMaxhealth(),
              new Semantic.RadarAttackerFlyingMaxhealth(),
              new Semantic.RadarAttackerFlyingBossMaxhealth(),
              new Semantic.RadarAttackerFlyingGroundMaxhealth(),
              new Semantic.RadarAttackerBossMaxhealth(),
              new Semantic.RadarAttackerBossGroundMaxhealth(),
              new Semantic.RadarAttackerGroundMaxhealth(),
              new Semantic.RadarFlyingMaxhealth(),
              new Semantic.RadarFlyingBossMaxhealth(),
              new Semantic.RadarFlyingBossGroundMaxhealth(),
              new Semantic.RadarFlyingGroundMaxhealth(),
              new Semantic.RadarBossMaxhealth(),
              new Semantic.RadarBossGroundMaxhealth(),
              new Semantic.RadarGroundMaxhealth())
            .stream()
            .collect(
              Collectors
                .toMap(d -> d.name().identifier(), Function.identity()))));
    currently_checked = new HashSet<>();
    check_source(subject, name);
    Semantic.Target target = new Semantic.Target(name, sources);
    Path target_artifact_path =
      artifacts.resolve("%s.%s%s".formatted(name, "target", Source.extension));
    try {
      Files.writeString(target_artifact_path, target.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(target_artifact_path)
        .to_diagnostic(
          "failure",
          "Could not record the target of source `%s`",
          name)
        .to_exception(cause);
    }
    return target;
  }

  /** Find a global symbol. */
  private Semantic.Definition find_global(Subject subject, Name name) {
    Semantic.Source source = check_source(subject, name.source());
    if (source.globals().containsKey(name.identifier())) {
      return source.globals().get(name.identifier());
    }
    throw subject
      .to_diagnostic(
        "error",
        "Could not find the symbol `%s::%s`!",
        name.source(),
        name.identifier())
      .to_exception();
  }

  /** Check a source file. */
  private Semantic.Source check_source(Subject subject, String name) {
    if (sources.containsKey(name)) { return sources.get(name); }
    if (currently_checked.contains(name)) {
      throw subject
        .to_diagnostic("error", "Cyclic definition with `%s`!", name)
        .to_exception();
    }
    currently_checked.add(name);
    Path file = find_source(subject, name);
    ResolvedSource resolution = Resolver.resolve(file, artifacts);
    Semantic.Source source = SourceChecker.check(resolution, this::find_global);
    sources.put(name, source);
    currently_checked.remove(name);
    return source;
  }

  /** Find a source file. */
  private Path find_source(Subject subject, String name) {
    String full_name = name + Source.extension;
    for (Path site : includes) {
      Path file = site.resolve(full_name);
      if (Files.exists(file)) { return file; }
    }
    throw subject
      .to_diagnostic(
        "error",
        "Could not find a source named `%s` in the fallowing directories: `%s`!",
        name,
        includes)
      .to_exception();
  }
}
