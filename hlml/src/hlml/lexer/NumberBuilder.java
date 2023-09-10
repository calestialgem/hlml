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

  /** Returns a clean builder for the given base. */
  static NumberBuilder create(NumberBase base) {
    return new NumberBuilder(base, 0, 0, 0, false, Stage.whole);
  }

  /** {@link Number#base}. */
  private final NumberBase base;

  /** {@link Number#significand}. */
  private long significand;

  /** {@link Number#scale}. */
  private int scale;

  /** {@link Number#exponent} without its sign. */
  private int exponent_magnitude;

  /** Whether a negative sign was provided for the trailing exponent. */
  private boolean exponent_sign;

  /** Current stage the builder is in. */
  private Stage stage;

  /** Constructs. */
  private NumberBuilder(
    NumberBase base,
    long significand,
    int scale,
    int exponent_magnitude,
    boolean exponent_sign,
    Stage stage)
  {
    this.base = base;
    this.significand = significand;
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
        if (Long.numberOfLeadingZeros(significand) < b.power()) {
          throw on_overflow();
        }
        significand <<= b.power();
      }
      case NumberBase.Arbitrary b -> {
        if (Long
          .compareUnsigned(significand, Long.divideUnsigned(-1, b.radix()))
          > 0)
        {
          throw on_overflow();
        }
        significand *= b.radix();
      }
    }
    if (Long.compareUnsigned(significand, -1 - digit) > 0) {
      throw on_overflow();
    }
    significand += digit;
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
  Number build() {
    return switch (base) {
      case NumberBase.PowerOfTwo b -> {
        int power = b.power();
        if (scale < Integer.MIN_VALUE / power) { throw on_overflow(); }
        int scale_contribution = scale * power;
        if (exponent_sign) {
          if (scale_contribution < Integer.MIN_VALUE + exponent_magnitude) {
            throw on_exponent_overflow();
          }
          yield new Number.Binary(
            significand,
            scale_contribution - exponent_magnitude);
        }
        yield new Number.Binary(
          significand,
          scale_contribution + exponent_magnitude);
      }
      case NumberBase.Arbitrary b -> {
        if (exponent_sign) {
          if (scale < Integer.MIN_VALUE + exponent_magnitude)
            throw on_exponent_overflow();
          yield new Number.Arbitrary(
            significand,
            b.radix(),
            scale - exponent_magnitude);
        }
        yield new Number.Arbitrary(
          significand,
          b.radix(),
          scale + exponent_magnitude);
      }
    };
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
