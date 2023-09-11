package hlml.generator.radar;

import java.nio.file.Path;

import hlml.generator.Persistance;

/** Generates test and code for radar instruction variants. */
public final class RadarGenerator {
  /** Filters that can be given to the radar instruction other than `any`. */
  private static final String[] filters =
    { "enemy", "ally", "player", "attacker", "flying", "boss", "ground" };

  /** Metrics that can be given to the radar instruction. */
  private static final String[] metrics =
    { "distance", "health", "shield", "armor", "maxHealth" };

  /** Generates for radar. */
  public static void generate(Path generated_directory) {
    RadarGenerator generator = new RadarGenerator(generated_directory);
    generator.generate();
  }

  /** Directory under which the radar files are generated. */
  private final Path generated_directory;

  /** Buffer of tests. */
  private StringBuilder tests;

  /** Buffer of procedure types. */
  private StringBuilder procedures;

  /** Buffer of built-in scope members. */
  private StringBuilder builtins;

  /** Buffer of instruction types. */
  private StringBuilder instructions;

  /** Buffer of building cases. */
  private StringBuilder buildings;

  /** Buffer of appending cases. */
  private StringBuilder appendings;

  /** Constructs. */
  private RadarGenerator(Path generated_directory) {
    this.generated_directory = generated_directory;
  }

  /** Generates radar files. */
  private void generate() {
    Path radar_directory = generated_directory.resolve("radar");
    Persistance.recreate(radar_directory);

    tests = new StringBuilder();
    tests.append("# Tests radar instruction.");
    tests.append(System.lineSeparator());
    tests.append(System.lineSeparator());
    tests.append("entrypoint {");
    tests.append(System.lineSeparator());
    tests.append("  var unit;");
    tests.append(System.lineSeparator());

    procedures = new StringBuilder();
    procedures.append("sealed interface Procedure{");

    builtins = new StringBuilder();
    builtins.append("final class Builtin{{Set.of(null");

    instructions = new StringBuilder();
    instructions.append("sealed interface Instruction{");

    buildings = new StringBuilder();
    buildings.append("final class Building{{int a=switch(null){");

    appendings = new StringBuilder();
    appendings.append("final class Appending{{switch(null){");

    for (int m = 0; m < metrics.length; m++) {
      combination(metrics[m]);
      for (int i = 0; i < filters.length; i++) {
        combination(metrics[m], filters[i]);
        for (int j = i + 1; j < filters.length; j++) {
          combination(metrics[m], filters[i], filters[j]);
          for (int k = j + 1; k < filters.length; k++) {
            combination(metrics[m], filters[i], filters[j], filters[k]);
          }
        }
      }
    }

    tests.append("}");
    tests.append(System.lineSeparator());
    Path test_file = radar_directory.resolve("building_radar_test.hlml");
    Persistance.write(test_file, tests.toString());

    procedures.append("}");
    Path procedure_file = radar_directory.resolve("Procedure.java");
    Persistance.write(procedure_file, procedures.toString());

    builtins.append(");}}");
    Path builtin_file = radar_directory.resolve("Builtin.java");
    Persistance.write(builtin_file, builtins.toString());

    instructions.append("}");
    Path instruction_file = radar_directory.resolve("Instruction.java");
    Persistance.write(instruction_file, instructions.toString());

    buildings.append("default->{}};}}");
    Path building_file = radar_directory.resolve("Building.java");
    Persistance.write(building_file, buildings.toString());

    appendings.append("default->{}}}}");
    Path appending_file = radar_directory.resolve("Appending.java");
    Persistance.write(appending_file, appendings.toString());
  }

  /** Generates a radar instruction for the given combination of filters and
   * metric. */
  private void combination(String metric, String... filters) {
    String identifier = "";
    for (String filter : filters)
      identifier += filter.toLowerCase() + "_";
    identifier += metric.toLowerCase();
    tests
      .append(
        "%-66s # radar %8s %8s %8s %9s building 1 unit%n"
          .formatted(
            "  mlog::radar_" + identifier + "(building, 1, unit);",
            filters.length >= 1 ? filters[0] : "any",
            filters.length >= 2 ? filters[1] : "any",
            filters.length >= 3 ? filters[2] : "any",
            metric));

    String suffix = "";
    for (String filter : filters) {
      suffix += filter.toUpperCase().substring(0, 1);
      suffix += filter.toLowerCase().substring(1);
    }
    suffix += metric.toUpperCase().substring(0, 1);
    suffix += metric.toLowerCase().substring(1);

    procedures
      .append("/** Procedure that compiles to the `radar` instruction's `");
    procedures.append(metric);
    procedures.append("` subinstruction with filters ");
    procedures.append("`");
    procedures.append(filters.length >= 1 ? filters[0] : "any");
    procedures.append("`, `");
    procedures.append(filters.length >= 2 ? filters[1] : "any");
    procedures.append("`, `");
    procedures.append(filters.length >= 3 ? filters[2] : "any");
    procedures.append("`. */record Radar");
    procedures.append(suffix);
    procedures
      .append(
        "() implements Procedure{@Override public Name name(){return new Name(built_in_scope,\"radar_");
    procedures.append(identifier);
    procedures
      .append(
        "\");}@Override public List<Parameter>parameters(){return List.of(new Parameter(\"b\",false), new Parameter(\"o\", false), new Parameter(\"u\", true));}@Override public Set<Name> dependencies(){return Set.of();}}");

    builtins.append(", new Radar");
    builtins.append(suffix);
    builtins.append("()");

    instructions
      .append(
        "/** Instruction that finds the first or last unit in a building's range via `");
    instructions.append(metric);
    instructions.append("` as the metric");
    if (filters.length == 0) {
      instructions.append(". */");
    }
    else {
      instructions.append(" after filtering them by ");
      instructions.append("`");
      instructions.append(filters[0]);
      instructions.append("`");
      for (int i = 1; i < filters.length; i++) {
        instructions.append(", `");
        instructions.append(filters[i]);
        instructions.append("`");
      }
      instructions.append(". */");
    }
    instructions.append("record Radar");
    instructions.append(suffix);
    instructions
      .append("(Register b, Register o, Register u)implements Instruction{}");

    buildings.append("case Semantic.Radar");
    buildings.append(suffix);
    buildings.append(" p->{program.instruct(new Instruction.Radar");
    buildings.append(suffix);
    buildings
      .append(
        "(build_argument(e.arguments(),0),build_argument(e.arguments(),1),build_argument(e.arguments(),2)));yield Register.null_();}");

    appendings.append("case Instruction.Radar");
    appendings.append(suffix);
    appendings.append(" i->{appendable.append(\"radar ");
    appendings.append(filters.length >= 1 ? filters[0] : "any");
    appendings.append(" ");
    appendings.append(filters.length >= 2 ? filters[1] : "any");
    appendings.append(" ");
    appendings.append(filters.length >= 3 ? filters[2] : "any");
    appendings.append(" ");
    appendings.append(metric);
    appendings.append("\");append_operands(appendable,i.b(),i.o(),i.u());}");
  }
}
