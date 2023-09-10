package hlml.lexer;

/** Rescales a {@link Number.Binary} to a particular width, which means the
 * number might loose precision and get rounded to even. */
final class NumberRescaler {
  /** Returns a rescaler to the given width. */
  static NumberRescaler create(int target_width) {
    return new NumberRescaler(
      target_width,
      (1L << target_width + 1) - 1,
      1L << target_width - 1);
  }

  /** Width the {@link Number.Binary}s will be rescaled to. */
  private final int target_width;

  /** Maximum value the significand can have when it has the
   * {@link #target_width}. */
  private final long max_significand;

  /** Value to round the {@link #max_significand} to, when one bit is carried to
   * {@link Number.Binary#exponent}. */
  private final long rounded_up_max_significand;

  /** Constructs. */
  private NumberRescaler(
    int target_width,
    long max_significand,
    long rounded_up_max_significand)
  {
    this.target_width = target_width;
    this.max_significand = max_significand;
    this.rounded_up_max_significand = rounded_up_max_significand;
  }

  /** Rescales a number. Rounds to even when precision is lost. Throws
   * {@link ArithmeticException} if the exponent overflows. */
  Number.Binary rescale(Number.Binary number) {
    long significand = number.significand();
    int exponent = number.exponent();
    int width = Long.SIZE - Long.numberOfLeadingZeros(significand);
    if (width == 0) { return new Number.Binary(0, 0); }
    int change = target_width - width;
    if (change == 0) { return number; }
    if (change > 0) {
      if (exponent < Integer.MIN_VALUE + change) {
        throw new ArithmeticException("Exponent is too small!");
      }
      return new Number.Binary(significand << change, exponent - change);
    }
    if (exponent > Integer.MAX_VALUE + change) {
      throw new ArithmeticException("Exponent is too big!");
    }
    long truncated = significand >>> -change;
    Number.Binary rounded_down =
      new Number.Binary(truncated, exponent - change);
    long half = ((truncated << 1) + 1) << -change - 1;
    if (significand < half || significand == half && (truncated & 1) == 0)
      return rounded_down;
    return round_up(rounded_down);
  }

  /** Rounds the given rounded down version of the number up. Throws
   * {@link ArithmeticException} if the exponent overflows. */
  Number.Binary round_up(Number.Binary rounded_down) {
    long significand = rounded_down.significand();
    int exponent = rounded_down.exponent();
    if (Long.compareUnsigned(significand, max_significand) != 0) {
      return new Number.Binary(significand + 1, exponent);
    }
    if (exponent == Integer.MAX_VALUE) {
      throw new ArithmeticException("Exponent is too big!");
    }
    return new Number.Binary(rounded_up_max_significand, exponent + 1);
  }
}
