package hlml;

/** Formats a packed color which can be parsed back by adding the prefix `0p` or
 * parsed by Mindustry by adding the prefix `%`. */
public final class PackedColorFormatter {
  /** Formats a packed color. */
  public static String format(int value) {
    return "%08x".formatted(value);
  }

  /** Constructor. */
  private PackedColorFormatter() {}
}
