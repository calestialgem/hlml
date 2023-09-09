package hlml;

import java.nio.file.Path;

import hlml.reporter.Subject;

/** Representation of a HLML source file. */
public class Source {
  /** File extension of HLML source files. */
  public static final String extension = ".hlml";

  /** Path to the source file. */
  public final Path path;

  /** Constructs. */
  public Source(Path path) {
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
    boolean is_initial =
      initial >= 'a' && initial <= 'z' || initial >= 'A' && initial <= 'Z';
    if (!is_initial) {
      throw Subject
        .of(path)
        .to_diagnostic(
          "error",
          "Source file's name must start with a letter, not `%c`!",
          initial)
        .to_exception();
    }
    for (int i = 0; i < name.length(); i = name.offsetByCodePoints(i, 1)) {
      int character = name.codePointAt(i);
      boolean is_valid =
        character >= 'a' && character <= 'z'
          || character >= 'A' && character <= 'Z'
          || character >= '0' && character <= '9'
          || character == '_';
      if (!is_valid) {
        throw Subject
          .of(path)
          .to_diagnostic(
            "error",
            "Source file's name must consist of letters, digits and underscores, not `%c`!",
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
    this.path = path;
  }

  /** Returns the source file's name. */
  public String name() {
    String name = path.getFileName().toString();
    return name.substring(0, name.length() - extension.length());
  }

  /** Returns a subject as this source file. */
  public Subject subject() {
    return Subject.of(path);
  }
}
