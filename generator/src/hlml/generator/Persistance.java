package hlml.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Holds utility functions on files and directories. */
public final class Persistance {
  /** Writes text to a file. */
  public static void write(Path file, String text) {
    try {
      Files.writeString(file, text);
    }
    catch (IOException cause) {
      throw new RuntimeException(
        "Could not write to '%s'!".formatted(file),
        cause);
    }
  }

  /** Cleans a directory if it exists, otherwise creates an empty one. */
  public static void recreate(Path directory) {
    try {
      if (Files.exists(directory))
        delete(directory);
      Files.createDirectory(directory);
    }
    catch (IOException cause) {
      throw new RuntimeException(
        "Could not recreate '%s'!".formatted(directory),
        cause);
    }
  }

  /** Deletes a file, or a directory with its entries. */
  private static void delete(Path path) {
    try {
      Files.walkFileTree(path, new Deletor());
    }
    catch (IOException cause) {
      throw new RuntimeException(
        "Could not delete '%s'!".formatted(path),
        cause);
    }
  }

  /** Constructs. */
  private Persistance() {}
}
