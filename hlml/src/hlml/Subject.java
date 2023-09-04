package hlml;

import java.io.IOException;
import java.nio.file.Files;
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
  record Location(Path file, int start, int end) implements Subject {
    @Override
    public void format_to(Formatter formatter) {
      formatter.format("%s", file.toAbsolutePath().normalize());
      String contents;
      try {
        contents = Files.readString(file);
      }
      catch (IOException cause) {
        throw of(file)
          .to_diagnostic("failure", "Could not read the source file!")
          .to_exception(cause);
      }
      if (contents.length() <= start || contents.length() < end) {
        throw of(file)
          .to_diagnostic(
            "error",
            "File changed between lexing and reporting! Length: %d, Start: %d, End: %d",
            contents.length(),
            start,
            end)
          .to_exception();
      }
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
      int length = end - start;
      if (length <= 1)
        return;
      for (; index < end; index = contents.offsetByCodePoints(index, 1)) {
        column++;
        int character = contents.codePointAt(index);
        if (character == '\n') {
          line++;
          column = 1;
        }
      }
      formatter.format(":%d:%d", line, column);
    }
  }

  /** Returns a subject by the given name. */
  static Subject of(String name) {
    return new Nominal(name);
  }

  /** Returns a subject as the given path. */
  static Subject of(Path path) {
    return of(path.toAbsolutePath().normalize());
  }

  /** Returns a subject as the location in the given file from the start byte up
   * to the end byte. */
  static Subject of(Path file, int start, int end) {
    return new Location(file, start, end);
  }

  /** Returns a subject as the character at the given index in the given
   * file. */
  static Subject of(Path file, int index) {
    return of(file, index, index + 1);
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
