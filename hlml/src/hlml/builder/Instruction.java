package hlml.builder;

import java.util.List;

/** Command that can be executed by a processor. */
sealed interface Instruction {
  /** Instructions that directly compile as given. */
  record DirectlyCompiled(String text, List<Register> arguments)
    implements Instruction
  {}

  /** Instructions that directly compile as given with a dummy argument at the
   * second place. */
  record DirectlyCompiledWithDummy(
    String text,
    String dummy_argument,
    List<Register> arguments) implements Instruction
  {}

  /** Instruction that makes the currently run instruction to change out of
   * sequence. */
  sealed interface Jump extends Instruction {
    /** Waypoint to the instruction the jump will happen to. */
    Waypoint goal();
  }

  /** Jumps that are always taken. */
  record JumpAlways(Waypoint goal) implements Jump {}

  /** Jumps that happen when the condition is true. */
  record JumpOnTrue(Waypoint goal, Register condition) implements Jump {}

  /** Jumps that happen when the condition is false. */
  record JumpOnFalse(Waypoint goal, Register condition) implements Jump {}

  /** Instruction that marks the end of the program. Practically equivalent to
   * jumping back to the first instruction as the processor loops the program
   * when it runs out of instructions or comes to this instruction. */
  record End() implements Instruction {}

  /** Sets the value in the target register to be the same as the value in the
   * source register. */
  record Set(Register target, Register source) implements Instruction {}

  /** Gets the value of a property for a source object. */
  record Sensor(Register target, Register source, Register property)
    implements Instruction
  {}

  /** Instruction that operates on values. */
  sealed interface Operation extends Instruction {
    /** Identifier that separates this operation from the other operations. */
    String operation_code();

    /** Register the result of the operation will go to. */
    Register target();
  }

  /** Operations with one operand. */
  sealed interface UnaryOperation extends Operation {
    /** Register that holds the operand of this operation. */
    Register operand();
  }

  /** Unary operation that evaluates the NOT of the value bitwise. */
  record BitwiseNot(Register target, Register operand)
    implements UnaryOperation
  {
    @Override
    public String operation_code() { return "not"; }
  }

  /** Operations with two operands. */
  sealed interface BinaryOperation extends Operation {
    /** Register that holds the left operand of this operation. */
    Register left_operand();

    /** Register that holds the right operand of this operation. */
    Register right_operand();
  }

  /** Binary operation that multiplies the values. */
  record Multiplication(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "mul"; }
  }

  /** Binary operation that divides the values. */
  record Division(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "div"; }
  }

  /** Binary operation that divides the values as integers. */
  record IntegerDivision(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "idiv"; }
  }

  /** Binary operation that finds the equivalent of the left value under the
   * modulus of right value. */
  record Modulus(Register target, Register left_operand, Register right_operand)
    implements BinaryOperation
  {
    @Override
    public String operation_code() { return "mod"; }
  }

  /** Binary operation that adds the values. */
  record Addition(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "add"; }
  }

  /** Binary operation that subtracts the values. */
  record Subtraction(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "sub"; }
  }

  /** Binary operation that shifts the left value right value many bits to the
   * left. */
  record LeftShift(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "shl"; }
  }

  /** Binary operation that shifts the left value right value many bits to the
   * right. */
  record RightShift(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "shr"; }
  }

  /** Binary operation that ANDs the values bitwise. */
  record BitwiseAnd(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "and"; }
  }

  /** Binary operation that XORs the values bitwise. */
  record BitwiseXor(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "xor"; }
  }

  /** Binary operation that ORs the values bitwise. */
  record BitwiseOr(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "or"; }
  }

  /** Binary operation that compares whether the left value is smaller than the
   * right one. */
  record LessThan(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "lessThan"; }
  }

  /** Binary operation that compares whether the left value is smaller than or
   * equal to the right one. */
  record LessThanOrEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "lessThanEq"; }
  }

  /** Binary operation that compares whether the left value is bigger than the
   * right one. */
  record GreaterThan(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "greaterThan"; }
  }

  /** Binary operation that compares whether the left value is bigger than or
   * equal to the right one. */
  record GreaterThanOrEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "greaterThanEq"; }
  }

  /** Binary operation that compares whether the values are the same after
   * implicit conversions. */
  record EqualTo(Register target, Register left_operand, Register right_operand)
    implements BinaryOperation
  {
    @Override
    public String operation_code() { return "equal"; }
  }

  /** Binary operation that compares whether the values are not the same after
   * implicit conversions. */
  record NotEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "notEqual"; }
  }

  /** Binary operation that compares whether the values are the same without any
   * implicit conversions. */
  record StrictlyEqualTo(
    Register target,
    Register left_operand,
    Register right_operand) implements BinaryOperation
  {
    @Override
    public String operation_code() { return "strictEqual"; }
  }
}
