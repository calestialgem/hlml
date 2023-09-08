package hlml.builder;

import java.text.DecimalFormat;
import java.util.Formattable;
import java.util.Formatter;

/** Memory location on a processor. */
sealed interface Register extends Formattable {
  /** Register that holds a global variable. */
  record Global(int index) implements Register {
    @Override
    public void formatTo(
      Formatter formatter,
      int flags,
      int width,
      int precision)
    {
      formatter.format("g%d", index);
    }
  }

  /** Register that holds a local variable. */
  record Local(int index) implements Register {
    @Override
    public void formatTo(
      Formatter formatter,
      int flags,
      int width,
      int precision)
    {
      formatter.format("l%d", index);
    }
  }

  /** Register that holds a temporary value. */
  record Temporary(int index) implements Register {
    @Override
    public void formatTo(
      Formatter formatter,
      int flags,
      int width,
      int precision)
    {
      formatter.format("t%d", index);
    }
  }

  /** Register that holds a constant value. Literals are inlined; thus, this is
   * not actually taking a place in the processor. */
  record Literal(double value) implements Register {
    @Override
    public void formatTo(
      Formatter formatter,
      int flags,
      int width,
      int precision)
    {
      DecimalFormat decimal_formatter = new DecimalFormat("0.#");
      decimal_formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
      formatter.format(decimal_formatter.format(value));
    }
  }
}
