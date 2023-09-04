package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Holds the entrypoint. */
final class Main {
  /** Entrypoint of the compiler. */
  public static void main(String... arguments) throws IOException {
    Main main = new Main();
    main.launch_tests();
  }

  /** Constructor. */
  private Main() {}

  /** Tests the compiler by using the test suite at `tests`. */
  private void launch_tests() throws IOException {
    Files.list(Path.of("tests")).forEach(this::launch_test);
  }

  /** Tests compiling the given code. */
  private void launch_test(Path test_path) {
    String test_name = test_path.getFileName().toString();
    test_name = test_name.substring(0, test_name.length() - ".hlml".length());
    Path output_file = Path.of("tests", test_name + ".mlog");
    Compiler.build(test_path, output_file);
  }
}
