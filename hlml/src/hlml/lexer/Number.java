package hlml.lexer;

/** Represents a number. */
sealed interface Number {
  /** Number as {@code significand*2^exponent}. */
  record Binary(long significand, int exponent) implements Number {}

  /** Number as {@code significand*radix^exponent} */
  record Arbitrary(long significand, int radix, int exponent)
    implements Number
  {}
}
