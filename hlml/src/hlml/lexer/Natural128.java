package hlml.lexer;

import java.util.Optional;
import java.util.OptionalDouble;

/** 128-bit unsigned integral number. Implemented using two Java {@code long}s
 * that are interpreted as 64-bit unsigned integral numbers. */
public record Natural128(long high_part, long low_part) {
  /** Maximum value a {@code long} that is interpreted as 64-bit unsigned
   * integral number can hold. */
  private static final long scalar_max = -1;

  /** {@code 0} of {@link Natural128}. */
  public static final Natural128 zero = new Natural128(0, 0);

  /** Returns whether addition of two {@code long}s that are interpreted as
   * 64-bit unsigned integral numbers overflows. */
  private static boolean does_overflow(long scalar_0, long scalar_1) {
    return Long.compareUnsigned(scalar_0, scalar_max - scalar_1) > 0;
  }

  /** Adds a {@code long} that is interpreted as 64-bit unsigned integral
   * number. Returns the result if not overflowed. */
  public Optional<Natural128> add(long scalar) {
    if (!does_overflow(low_part, scalar)) {
      return Optional.of(new Natural128(high_part, low_part + scalar));
    }
    if (does_overflow(high_part, 1)) { return Optional.empty(); }
    return Optional.of(new Natural128(high_part + 1, low_part + scalar));
  }

  /** Multiplies a {@code int} that is interpreted as 64-bit unsigned integral
   * number. Returns the result if not overflowed. */
  public Optional<Natural128> multiply(int scalar) {
    // M = 2^32
    // M^2 = 2^64
    // M^3 = 2^96
    // M^4 = 2^128
    // l1 = i1*M + i2
    // l2 = i3*M + i4
    // l1 * l2 = i1 * M * i4 + i1 * M * i3 * M + i2 * i3 * M + i2 * i4
    // l1 * l2 = i1*i3 * M^2 + (i1*i4 + i2*i3) * M + i2*i4

    // hp = hp_h * M + hp_l
    // lp = lp_h * M + lp_l
    // s = 0 * M + s_l
    // n1 = hp * M^2 + lp
    // n2 = 0 * M^2 + s
    // n1 * n2 = hp * M^2 * (0 * M^2 + s) + lp * (0 * M^2 + s)
    // n1 * n2 = hp * M^2 * s + lp * s
    // n1 * n2 = hp * s * M^2 + lp * s
    // n1 * n2 = (hp_h*s_h * M^2 + (hp_h*s_l + hp_l*s_h) * M + hp_l*s_l) * M^2
    // + lp_h*s_h * M^2 + (lp_h*s_l + lp_l*s_h) * M + lp_l*s_l
    // n1 * n2 = hp_h*s_h * M^4 + (hp_h*s_l + hp_l*s_h) * M^3 + hp_l*s_l * M^2
    // + lp_h*s_h * M^2 + (lp_h*s_l + lp_l*s_h) * M + lp_l*s_l
    // n1 * n2 = hp_h*s_h * M^4 + (hp_h*s_l + hp_l*s_h) * M^3
    // + (hp_l*s_l + lp_h*s_h) * M^2 + (lp_h*s_l + lp_l*s_h) * M + lp_l*s_l
    // n1 * n2 = hp_h*s_l * M^3 + hp_l*s_l * M^2 + lp_h*s_l * M + lp_l*s_l

    long low_bit_mask = (1L << 32) - 1;
    long hight_part_high_bits = high_part >>> 32;
    long hight_part_low_bits = high_part & low_bit_mask;
    long low_part_high_bits = low_part >>> 32;
    long low_part_low_bits = low_part & low_bit_mask;
    long scalar_low_bits = Integer.toUnsignedLong(scalar);
    long term_0 = hight_part_high_bits * scalar_low_bits;
    long term_1 = hight_part_low_bits * scalar_low_bits;
    long term_2 = low_part_high_bits * scalar_low_bits;
    long term_3 = low_part_low_bits * scalar_low_bits;
    if (term_0 > low_bit_mask) { return Optional.empty(); }
    // long low_part_contribution_0 = 0;
    // long low_part_contribution_1 = 0;
    long low_part_contribution_2 = (term_2 & low_bit_mask) << 32;
    long low_part_contribution_3 = term_3;
    long low_part = low_part_contribution_2 + low_part_contribution_3;
    boolean does_carry =
      does_overflow(low_part_contribution_2, low_part_contribution_3);
    long high_part_contribution_0 = term_0 << 32;
    long high_part_contribution_1 = term_1;
    long high_part_contribution_2 = term_2 >>> 32;
    // long high_part_contribution_3 = 0;
    if (does_overflow(high_part_contribution_0, high_part_contribution_1)) {
      return Optional.empty();
    }
    long high_part = high_part_contribution_0 + high_part_contribution_1;
    if (does_overflow(high_part, high_part_contribution_2)) {
      return Optional.empty();
    }
    high_part += high_part_contribution_2;
    if (does_carry) {
      if (does_overflow(high_part, 1)) { return Optional.empty(); }
      high_part++;
    }
    return Optional.of(new Natural128(high_part, low_part));
  }

  /** Converts the natural to a {@code double} if it fits. */
  public OptionalDouble to_double() {
    if (high_part != 0) { return OptionalDouble.empty(); }
    int scale_up = Long.numberOfTrailingZeros(low_part);
    long scaled_natural = low_part >>> scale_up;
    int precision = Long.SIZE - Long.numberOfLeadingZeros(scaled_natural);
    int double_mantissa_bits = 52;
    int double_mantissa_precision = double_mantissa_bits + 1;
    int floating_places = double_mantissa_precision - precision;
    if (floating_places < 0) { return OptionalDouble.empty(); }
    long mantissa = scaled_natural << floating_places;
    long mantissa_mask = (1L << double_mantissa_bits) - 1;
    long masked_mantissa = mantissa & mantissa_mask;
    int exponent = scale_up + precision - 1;
    long double_exponent_bits = Long.SIZE - 1 - double_mantissa_bits;
    long double_exponent_bias = (1L << double_exponent_bits - 1) - 1;
    long biased_exponent = exponent + double_exponent_bias;
    long as_double_bits =
      biased_exponent << double_mantissa_bits | masked_mantissa;
    double as_double = Double.longBitsToDouble(as_double_bits);
    return OptionalDouble.of(as_double);
  }

  @Override
  public String toString() {
    if (high_part == 0) { return Long.toUnsignedString(low_part); }
    String high_part_hex = Long.toUnsignedString(high_part, 16);
    String low_part_hex = Long.toUnsignedString(low_part, 16);
    StringBuilder buffer = new StringBuilder();
    buffer.append("0x");
    for (int i = 0; i + high_part_hex.length() < 8; i++) { buffer.append('0'); }
    buffer.append(high_part_hex);
    for (int i = 0; i + low_part_hex.length() < 8; i++) { buffer.append('0'); }
    buffer.append(low_part_hex);
    return buffer.toString();
  }
}
