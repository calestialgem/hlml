package hlml;

import java.nio.file.Path;

/** Representation of a HLML source file. */
record Source(Path path) {
  /** Returns a subject as this source file. */
  Subject subject() {
    return Subject.of(path);
  }

  /** Returns a subject as a character in this source file. */
  Subject subject(int index) {
    return Subject.of(path, index);
  }
}
