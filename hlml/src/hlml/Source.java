package hlml;

import java.nio.file.Path;

/** Representation of a HLML source file. */
record Source(Path path) {
  /** File extension of HLML source files. */
  public static final String extension = ".hlml";

  /** Validates the given path and returns it as a source. */
  static Source of(Path path) {
    String name = path.getFileName().toString();
    if (!name.endsWith(extension)) {
      throw Subject
        .of(path)
        .to_diagnostic(
          "error",
          "Source files must have extension `%s`!",
          extension)
        .to_exception();
    }
    name = name.substring(0, name.length() - extension.length());
    if (name.isEmpty()) {
      throw Subject
        .of(path)
        .to_diagnostic("error", "Source file's name is empty!")
        .to_exception();
    }
    int initial = name.codePointAt(0);
    if (initial < 'a' || initial > 'z') {
      throw Subject
        .of(path)
        .to_diagnostic(
          "error",
          "Source file's name must start with a lowercase letter, not `%c`!",
          initial)
        .to_exception();
    }
    for (int i = 0; i < name.length(); i = name.offsetByCodePoints(i, 1)) {
      int character = name.codePointAt(i);
      boolean is_valid =
        character >= 'a' && character <= 'z'
          || character >= '0' && character <= '9'
          || character == '_';
      if (!is_valid) {
        throw Subject
          .of(path)
          .to_diagnostic(
            "error",
            "Source file's name must consist of lowercase letters, decimal digits and underscores, not `%c`!",
            character)
          .to_exception();
      }
    }
    switch (name) {
      case "entrypoint" ->
        throw Subject
          .of(path)
          .to_diagnostic(
            "error",
            "Source file's name is the keyword `%s`!",
            name)
          .to_exception();
      default -> {}
    }
    return new Source(path);
  }

  /** Returns the source file's name. */
  String name() {
    String name = path.getFileName().toString();
    return name.substring(0, name.length() - extension.length());
  }

  /** Returns a subject as this source file. */
  Subject subject() {
    return Subject.of(path);
  }
}
