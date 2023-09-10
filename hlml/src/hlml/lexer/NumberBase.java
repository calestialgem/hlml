package hlml.lexer;

/** Base of the {@link Number#scale}. */
sealed interface NumberBase {
  /** A base that is a power of {@code 2}. Used for simpler and faster routs for
   * number conversions. Exponent's base is {@code 2}. */
  record PowerOfTwo(int power) implements NumberBase {
    @Override
    public int radix() { return 1 << power; }
  }

  /** A base that is not a power of {@code 2}. This makes conversions complex
   * and slow. Exponent's base is the same as the scale's base. */
  record Arbitrary(int radix) implements NumberBase {}

  /** Returns a base of the given value. */
  static NumberBase of(int radix) {
    if (Integer.bitCount(radix) == 1) {
      return new PowerOfTwo(Integer.numberOfTrailingZeros(radix));
    }
    return new Arbitrary(radix);
  }

  /** Value of the base. */
  int radix();
}
