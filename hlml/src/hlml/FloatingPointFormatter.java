package hlml;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formats a floating point number in a way that can be parsed back without
 * loosing any information. */
public final class FloatingPointFormatter {
  /** Tool for formatting floating point numbers with maximum accuracy. */
  private static final DecimalFormat formatter;

  static {
    formatter = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.US));
    formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
  }

  /** Formats a number. */
  public static String format(double value) {
    return formatter.format(value);
  }

  /** Constructs. */
  private FloatingPointFormatter() {}
}
