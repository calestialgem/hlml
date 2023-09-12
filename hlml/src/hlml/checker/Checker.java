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

  /** Definitions that are not user-made. */
  private Set<Semantic.Definition> builtins = new HashSet<>();

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
    builtins = new HashSet<>();
    builtin("read", 3);
    builtin("write", 3);
    builtin("draw", "clear", 3);
    builtin("draw", "color", 4);
    builtin("draw", "col", 1);
    builtin("draw", "stroke", 1);
    builtin("draw", "line", 4);
    builtin("draw", "rect", 4);
    builtin("draw", "lineRect", 4);
    builtin("draw", "poly", 5);
    builtin("draw", "linePoly", 5);
    builtin("draw", "triangle", 6);
    builtin("draw", "image", 5);
    builtin("drawflush", 1);
    builtin("packcolor", 4);
    builtin("print", 1);
    builtin("printflush", 1);
    builtin("getlink", 2);
    builtin("control", "enabled", 2);
    builtin("control", "shoot", 4);
    builtin("control", "shootp", 3);
    builtin("control", "config", 2);
    builtin("control", "color", 2);
    builtin("sensor", 3);
    builtin("wait", 1);
    builtin("stop", 0);
    builtin("lookup", "block", 2);
    builtin("lookup", "unit", 2);
    builtin("lookup", "item", 2);
    builtin("lookup", "liquid", 2);
    builtin("ubind", 1);
    builtin("ucontrol", "idle", 0);
    builtin("ucontrol", "stop", 0);
    builtin("ucontrol", "move", 2);
    builtin("ucontrol", "approach", 3);
    builtin("ucontrol", "pathfind", 2);
    builtin("ucontrol", "autoPathfind", 0);
    builtin("ucontrol", "boost", 1);
    builtin("ucontrol", "target", 3);
    builtin("ucontrol", "targetp", 2);
    builtin("ucontrol", "itemDrop", 2);
    builtin("ucontrol", "itemTake", 3);
    builtin("ucontrol", "payDrop", 0);
    builtin("ucontrol", "payTake", 1);
    builtin("ucontrol", "payEnter", 0);
    builtin("ucontrol", "mine", 2);
    builtin("ucontrol", "flag", 1);
    builtin("ucontrol", "build", 5);
    builtin("ucontrol", "getBlock", 5);
    builtin("ucontrol", "within", 4);
    builtin("ucontrol", "unbind", 0);

    String[] filters =
      { "enemy", "ally", "player", "attacker", "flying", "boss", "ground" };
    int filter_0_combinations = 1;
    int filter_1_combinations = filter_0_combinations * filters.length;
    int filter_2_combinations =
      filter_1_combinations * (filters.length - 1) / 2;
    int filter_3_combinations =
      filter_2_combinations * (filters.length - 2) / 3;
    int filter_combinations =
      filter_0_combinations
        + filter_1_combinations
        + filter_2_combinations
        + filter_3_combinations;
    String[] filter_name_combinations = new String[filter_combinations];
    String[] filter_instruction_combinations = new String[filter_combinations];
    int combination = 0;
    filter_name_combinations[combination] = "";
    filter_instruction_combinations[combination] = "any any any";
    combination++;
    for (int i = 0; i < filters.length; i++) {
      String filter_i = filters[i];
      String filter_i_name = filter_i.toLowerCase();
      filter_name_combinations[combination] = filter_i_name;
      filter_instruction_combinations[combination] = filter_i + " any any";
      combination++;
      for (int j = i + 1; j < filters.length; j++) {
        String filter_j = filters[j];
        String filter_j_name = filter_j.toLowerCase();
        filter_name_combinations[combination] =
          filter_i_name + "_" + filter_j_name;
        filter_instruction_combinations[combination] =
          filter_i + " " + filter_j + " any";
        combination++;
        for (int k = j + 1; k < filters.length; k++) {
          String filter_k = filters[k];
          String filter_k_name = filter_k.toLowerCase();
          filter_name_combinations[combination] =
            filter_i_name + "_" + filter_j_name + "_" + filter_k_name;
          filter_instruction_combinations[combination] =
            filter_i + " " + filter_j + " " + filter_k;
          combination++;
        }
      }
    }

    String[] metrics = { "distance", "health", "shield", "armor", "maxHealth" };
    for (String metric : metrics) {
      String metric_name = metric.toLowerCase();
      for (int i = 0; i < filter_combinations; i++) {
        builtins
          .add(
            new Semantic.Instruction(
              "radar_"
                + filter_name_combinations[i]
                + (filter_name_combinations[i].length() == 0 ? "" : "_")
                + metric_name,
              "radar " + filter_instruction_combinations[i] + " " + metric,
              3));
        builtins
          .add(
            new Semantic.Instruction(
              "uradar_"
                + filter_name_combinations[i]
                + (filter_name_combinations[i].length() == 0 ? "" : "_")
                + metric_name,
              "uradar "
                + filter_instruction_combinations[i]
                + " "
                + metric
                + " 0",
              2));
      }
    }

    sources = new HashMap<>();
    sources
      .put(
        Semantic.built_in_scope,
        new Semantic.Source(
          Optional.empty(),
          builtins
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

  /** Defines a built-in procedure as the given instruction. */
  private void builtin(String instruction, int parameter_count) {
    builtins
      .add(
        new Semantic.Instruction(
          instruction.toLowerCase(),
          instruction,
          parameter_count));
  }

  /** Defines a built-in procedure as the given instruction with the given
   * subinstruction. */
  private void builtin(
    String instruction,
    String subinstruction,
    int parameter_count)
  {
    builtins
      .add(
        new Semantic.Instruction(
          instruction.toLowerCase() + '_' + subinstruction.toLowerCase(),
          instruction + ' ' + subinstruction,
          parameter_count));
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
