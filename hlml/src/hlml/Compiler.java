package hlml;

import java.nio.file.Path;

/** Wires the compiler's implementation together to make it run macro tasks such
 * as `check`, `build`. */
final class Compiler {
  /** Creates the instructions for the program in a source source. */
  static void build(Path source_file, Path output_file) {
    System.err
      .printf(
        "%s: info: Building source file to '%s'...%n",
        source_file.toAbsolutePath().normalize(),
        output_file.toAbsolutePath().normalize());
  }

  /** Validates correctness of a source file. */
  static void check(Path source_file) {
    System.err
      .printf(
        "%s: info: Checking source file...%n",
        source_file.toAbsolutePath().normalize());
  }

  /** Constructor. */
  private Compiler() {}
}
