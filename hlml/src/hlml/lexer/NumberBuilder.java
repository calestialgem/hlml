package hlml.lexer;

/** Builds a {@link Number} from digits as the whole, fraction and exponent
 * parts. */
final class NumberBuilder {
  /** Type that signifies how to handle input given to the builder. The building
   * process is separated to the whole part, fraction after a fraction
   * separator, and the exponent after an exponent separator with an optional
   * exponent sign. */
  private enum Stage {
    whole, fraction, exponent;
  }

  /** Number of bits in a {@code double}s mantissa part. */
  private static final int double_mantissa_width = 52;

  /** Number of bits that can be represented by a {@code double}s
   * significand. */
  private static final int double_significand_width = double_mantissa_width + 1;

  /** {@code long} that can be ANDed with a {@code double}s bit pattern to
   * extract the mantissa part out. Also can be ANDed with a significand to get
   * the mantissa. */
  private static final long double_mantissa_mask =
    (1L << double_mantissa_width) - 1;

  /** Number of bits in a {@code double}s exponent part. */
  private static final int double_exponent_width =
    Double.SIZE - double_mantissa_width - 1;

  /** Number that can be added to a {@code double}s extracted exponent pattern
   * to get the exponent's value. */
  private static final int double_exponent_bias =
    (1 << double_exponent_width - 1) - 1;

  /** Maximum exponent pattern that a {@code double} can hold. Exponent patterns
   * above this are rounded to infinity. */
  private static final int double_exponent_limit =
    (1 << double_exponent_width) - 1;

  /** Returns a clean builder for the given base. */
  static NumberBuilder create(NumberBase base) {
    return new NumberBuilder(base, 0, 0, 0, false, Stage.whole);
  }

  /** Base of the number's scale. For non-powers of 2, it is also the base of
   * the number's exponent. For powers of 2, number's exponent is precision;
   * thus, is base 2. */
  private final NumberBase base;

  /** Combination of whole part and the fraction of the number. */
  private long digits;

  /** Number of digits in the fraction multiplied by -1. */
  private int scale;

  /** Magnitude of the exponent without its sign. */
  private int exponent_magnitude;

  /** Whether the exponent is negative. */
  private boolean exponent_sign;

  /** Current stage the builder is in. */
  private Stage stage;

  /** Significand of the number in {@code significand*2^exponent}. */
  private long significand;

  /** Exponent of the number in {@code significand*2^exponent}. */
  private int exponent;

  /** Constructs. */
  private NumberBuilder(
    NumberBase base,
    long digits,
    int scale,
    int exponent_magnitude,
    boolean exponent_sign,
    Stage stage)
  {
    this.base = base;
    this.digits = digits;
    this.scale = scale;
    this.exponent_magnitude = exponent_magnitude;
    this.exponent_sign = exponent_sign;
    this.stage = stage;
  }

  /** Inserts a digit to the right of the number. Depending on the stage, the
   * digit would go the the whole part, the fraction or the exponent. Exponent
   * digits are always in decimal regardless of the base! Throws
   * {@link ArithmeticException} if the digit makes the number overflow! */
  void insert(int digit) {
    if (stage == Stage.exponent) {
      if (exponent_magnitude > Integer.MAX_VALUE / 10) {
        throw on_exponent_overflow();
      }
      exponent_magnitude *= 10;
      if (exponent_magnitude > Integer.MAX_VALUE - digit) {
        throw on_exponent_overflow();
      }
      exponent_magnitude += digit;
      return;
    }
    if (stage == Stage.fraction) {
      if (scale == Integer.MIN_VALUE) { throw on_overflow(); }
      scale--;
    }
    switch (base) {
      case NumberBase.PowerOfTwo b -> {
        if (Long.numberOfLeadingZeros(digits) < b.power()) {
          throw on_overflow();
        }
        digits <<= b.power();
      }
      case NumberBase.Arbitrary b -> {
        if (Long.compareUnsigned(digits, Long.divideUnsigned(-1, b.radix()))
          > 0)
        {
          throw on_overflow();
        }
        digits *= b.radix();
      }
    }
    if (Long.compareUnsigned(digits, -1 - digit) > 0) { throw on_overflow(); }
    digits += digit;
  }

  /** Marks the end of the whole part and start of the fraction after the
   * fraction separator. Digits inserted after this will go to the fraction
   * part. There may be no digits provided after this call. */
  void fraction_separator() {
    stage = Stage.fraction;
  }

  /** Marks the end of the whole part or the fraction and start of the exponent
   * with an optional sign to turn the exponent negative. Digits inserted after
   * this will go to the exponent part. There may be no digits provided after
   * this call. */
  void exponent_separator(boolean is_negative) {
    stage = Stage.exponent;
    exponent_sign = is_negative;
  }

  /** Builds a {@link Number} using the currently inserted digits. Does not
   * invalidate or affect the builder. */
  double build() {
    significand = digits;
    switch (base) {
      case NumberBase.PowerOfTwo b -> {
        int power = b.power();
        if (scale < Integer.MIN_VALUE / power) { throw on_overflow(); }
        exponent = scale * power;
        if (!exponent_sign) {
          exponent += exponent_magnitude;
          break;
        }
        if (exponent < Integer.MIN_VALUE + exponent_magnitude) {
          throw on_exponent_overflow();
        }
        exponent -= exponent_magnitude;
      }
      case NumberBase.Arbitrary b -> {
        if (!exponent_sign) {
          convert_to_binary_exponent(b.radix(), scale + exponent_magnitude);
          break;
        }
        if (scale < Integer.MIN_VALUE + exponent_magnitude)
          throw on_exponent_overflow();
        convert_to_binary_exponent(b.radix(), scale - exponent_magnitude);
      }
    }
    rescale(double_significand_width);
    if (significand == 0)
      return 0;
    exponent += double_mantissa_width + double_exponent_bias;
    if (exponent >= double_exponent_limit) { return Double.POSITIVE_INFINITY; }
    if (exponent > 0) {
      significand &= double_mantissa_mask;
      return Double
        .longBitsToDouble(
          (long) exponent << double_mantissa_width | significand);
    }
    if (-exponent > double_significand_width) { return 0; }
    significand >>>= 1 - exponent;
    return Double.longBitsToDouble(significand);
  }

  /** Converts the built number's exponent to base 2. */
  private void convert_to_binary_exponent(int radix, int arbitrary_exponent) {
    exponent = 0;
    if (arbitrary_exponent > 0) {
      scale_up_to_binary(radix, arbitrary_exponent);
      return;
    }
    if (arbitrary_exponent < 0) {
      scale_down_to_binary(radix, arbitrary_exponent);
      return;
    }
  }

  /** Multiplies by the radix until the arbitrary exponent is gone and the
   * binary exponent is built up. */
  private void scale_up_to_binary(int radix, int arbitrary_exponent) {
    int radix_width = Integer.SIZE - Integer.numberOfLeadingZeros(radix);
    int safe_width = Long.SIZE - radix_width;
    int overflowing_width = safe_width + 1;
    for (int i = 0; i < arbitrary_exponent; i++) {
      long old_significand = significand;
      int old_exponent = exponent;
      rescale(overflowing_width);
      boolean overflows =
        Long.compareUnsigned(significand, Long.divideUnsigned(-1, radix)) > 0;
      if (overflows) {
        significand = old_significand;
        exponent = old_exponent;
        rescale(safe_width);
      }
      significand *= radix;
    }
  }

  /** Divides by the radix until the arbitrary exponent is gone and the binary
   * exponent is built up. */
  private void scale_down_to_binary(int radix, int arbitrary_exponent) {
    int middle_point = radix / 2;
    for (int i = 0; i < -arbitrary_exponent; i++) {
      rescale(Long.SIZE);
      long reminder = Long.remainderUnsigned(significand, radix);
      significand = Long.divideUnsigned(significand, radix);
      boolean rounds_up =
        reminder > middle_point
          || reminder == middle_point && (significand & 1) != 0;
      if (rounds_up) { round_up(Long.SIZE); }
    }
  }

  /** Rescales a number. Rounds to even when precision is lost. Throws
   * {@link ArithmeticException} if the exponent overflows. */
  private void rescale(int target_width) {
    int width = Long.SIZE - Long.numberOfLeadingZeros(significand);
    if (width == 0) {
      exponent = 0;
      return;
    }
    int change = target_width - width;
    if (change == 0) { return; }
    if (change > 0) {
      if (exponent < Integer.MIN_VALUE + change) {
        throw new ArithmeticException("Exponent is too small!");
      }
      significand <<= change;
      exponent -= change;
      return;
    }
    if (exponent > Integer.MAX_VALUE + change) {
      throw new ArithmeticException("Exponent is too big!");
    }
    long truncated = significand >>> -change;
    long middle_point = ((truncated << 1) + 1) << -change - 1;
    boolean rounds_up =
      !(significand < middle_point
        || significand == middle_point && (truncated & 1) == 0);
    significand = truncated;
    exponent -= change;
    if (rounds_up)
      round_up(target_width);
  }

  /** Rounds the given rounded down version of the number up. Throws
   * {@link ArithmeticException} if the exponent overflows. */
  private void round_up(int target_width) {
    long max_significand = (1L << target_width + 1) - 1;
    if (Long.compareUnsigned(significand, max_significand) != 0) {
      significand++;
      return;
    }
    if (exponent == Integer.MAX_VALUE) {
      throw new ArithmeticException("Exponent is too big!");
    }
    long rounded_up_max_significand = 1L << target_width - 1;
    significand = rounded_up_max_significand;
    exponent++;
  }

  /** Returns an exception to be thrown when the number's exponent overflows. */
  private ArithmeticException on_exponent_overflow() {
    return new ArithmeticException(
      "Exponent is too %s!".formatted(exponent_sign ? "small" : "big"));
  }

  /** Returns an exception to be thrown when the number's significand
   * overflows. */
  private ArithmeticException on_overflow() {
    return new ArithmeticException(
      "Number is too %s!".formatted(stage == Stage.whole ? "big" : "precise"));
  }
}
