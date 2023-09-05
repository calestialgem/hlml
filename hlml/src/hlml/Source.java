package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Representation of a HLML source file. */
record Source(Path path) {
  /** Returns the contents of the source file. Loaded to the memory at each
   * request! */
  String contents() {
    try {
      return Files.readString(path);
    }
    catch (IOException cause) {
      throw subject()
        .to_diagnostic("failure", "Could not read the source file!")
        .to_exception(cause);
    }
  }

  /** Returns a subject as this source file. */
  Subject subject() {
    return Subject.of(path);
  }

  /** Returns a subject as a character in this source file. */
  Subject subject(int index) {
    return Subject.of(path, index);
  }
}
