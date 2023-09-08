package hlml.builder;

/** Tool for initializing a binary operation of unknown type. */
@FunctionalInterface
interface BinaryOperationInitializer {
  /** Initializes a binary operation of an unknown type. */
  Instruction.BinaryOperation initialize(
    Register target,
    Register left_operand,
    Register right_operand);
}
