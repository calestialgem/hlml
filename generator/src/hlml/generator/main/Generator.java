package hlml.generator.main;

import java.nio.file.Path;

import hlml.generator.Persistance;
import hlml.generator.radar.RadarGenerator;

/** Generates Java code to be used in the compiler. */
final class Generator {
  /** Generates the code. */
  public static void main(String... arguments) {
    Path generated_directory = Path.of("generated");
    Persistance.recreate(generated_directory);
    RadarGenerator.generate(generated_directory);
  }
}
