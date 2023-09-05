package hlml;

import java.nio.file.Path;
import java.util.Formatter;

/** A diagnostic's creator. */
sealed interface Subject {
  /** Creator by name. */
  record Nominal(String name) implements Subject {
    @Override
    public void format_to(Formatter formatter) { formatter.format("%s", name); }
  }

  /** Creator as a location in a source file. */
  record Location(Path file, String contents, int start, int end)
    implements Subject
  {
    @Override
    public void format_to(Formatter formatter) {
      formatter.format("%s", file.toAbsolutePath().normalize());
      int line = 1;
      int column = 1;
      int index = 0;
      for (; index < start; index = contents.offsetByCodePoints(index, 1)) {
        column++;
        int character = contents.codePointAt(index);
        if (character == '\n') {
          line++;
          column = 1;
        }
      }
      formatter.format(":%d:%d", line, column);
      int start_line = line;
      int start_column = column;
      for (; index < end; index = contents.offsetByCodePoints(index, 1)) {
        column++;
        int character = contents.codePointAt(index);
        if (character == '\n') {
          line++;
          column = 1;
        }
      }
      if (start_line == line && start_column == column - 1)
        return;
      formatter.format(":%d:%d", line, column);
    }
  }

  /** Returns a subject by the given name. */
  static Subject of(String name) {
    return new Nominal(name);
  }

  /** Returns a subject as the given path. */
  static Subject of(Path path) {
    return of(path.toAbsolutePath().normalize().toString());
  }

  /** Returns a subject as the location in the given file from the start byte up
   * to the end byte. */
  static Subject of(Path file, String contents, int start, int end) {
    return new Location(file, contents, start, end);
  }

  /** Returns a message from this subject. */
  default Diagnostic to_diagnostic(
    String title,
    String format,
    Object... arguments)
  {
    StringBuilder buffer = new StringBuilder();
    try (Formatter formatter = new Formatter(buffer)) {
      format_to(formatter);
      formatter.format(": %s: ", title);
      formatter.format(format, arguments);
    }
    String message = buffer.toString();
    Diagnostic diagnostic = new Diagnostic(message);
    return diagnostic;
  }

  /** Formats the subject. */
  void format_to(Formatter formatter);
}
