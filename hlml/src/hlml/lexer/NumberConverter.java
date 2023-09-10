package hlml.lexer;

/** Converts a {@link Number} of any base to a {@code double}. */
final class NumberConverter {
  /** Converts a number. Rounds to even. Throws {@link ArithmeticException} if
   * the exponent in the given number's base does not fit to an {@code int} when
   * converted to base {@code 2}. Or number is outside of the {@code double}s
   * range. */
  static double convert(Number number) {
    NumberConverter converter = new NumberConverter(number);
    return converter.convert();
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

  /** Rescaler that rescales a number to have the same width as a
   * {@code double}. */
  private static final NumberRescaler double_rescaler =
    NumberRescaler.create(double_significand_width);

  /** Converted number. */
  private final Number number;

  /** Constructs. */
  private NumberConverter(Number number) {
    this.number = number;
  }

  /** Converts the number. */
  private double convert() {
    Number.Binary as_binary = switch (number) {
      case Number.Binary n -> n;
      case Number.Arbitrary n -> {
        Number.Binary result = new Number.Binary(n.significand(), 0);
        if (n.exponent() == 0) { yield result; }
        int radix = n.radix();
        if (n.exponent() > 0) {
          int radix_width = Integer.SIZE - Integer.numberOfLeadingZeros(radix);
          NumberRescaler overflowing_rescaler =
            NumberRescaler.create(Long.SIZE - radix_width + 1);
          NumberRescaler safe_rescaler =
            NumberRescaler.create(Long.SIZE - radix_width);
          for (int i = 0; i < n.exponent(); i++) {
            Number.Binary old = result;
            result = overflowing_rescaler.rescale(result);
            boolean overflows =
              Long
                .compareUnsigned(
                  result.significand(),
                  Long.divideUnsigned(-1, radix))
                > 0;
            if (overflows) { result = safe_rescaler.rescale(old); }
            result =
              new Number.Binary(
                result.significand() * radix,
                result.exponent());
          }
          yield result;
        }
        int middle_point = radix / 2;
        NumberRescaler rescaler = NumberRescaler.create(Long.SIZE);
        for (int i = 0; i < -n.exponent(); i++) {
          result = rescaler.rescale(result);
          long reminder = Long.remainderUnsigned(result.significand(), radix);
          result =
            new Number.Binary(
              Long.divideUnsigned(result.significand(), radix),
              result.exponent());
          boolean rounds_up =
            reminder > middle_point
              || reminder == middle_point && (result.significand() & 1) != 0;
          if (rounds_up) { result = rescaler.round_up(result); }
        }
        yield result;
      }
    };
    as_binary = double_rescaler.rescale(as_binary);
    if (as_binary.significand() == 0)
      return 0;
    int exponent_pattern =
      as_binary.exponent() + double_mantissa_width + double_exponent_bias;
    if (exponent_pattern >= double_exponent_limit) {
      return Double.POSITIVE_INFINITY;
    }
    if (exponent_pattern > 0) {
      long mantissa = as_binary.significand() & double_mantissa_mask;
      return Double
        .longBitsToDouble(
          (long) exponent_pattern << double_mantissa_width | mantissa);
    }
    if (-exponent_pattern > double_significand_width) { return 0; }
    long mantissa = as_binary.significand() >>> 1 - exponent_pattern;
    return Double.longBitsToDouble(mantissa);
  }
}
