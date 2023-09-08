package hlml.builder;

/** Holds knowledge about the temporary registers. */
final class Stack {
  /** Returns an empty stack. */
  static Stack create() { return new Stack(0); }

  /** Current number of temporary value holding registers. */
  private int length;

  /** Constructor. */
  private Stack(int length) { this.length = length; }

  /** Returns a temporary register for a calculation involving the given
   * registers. Used for optimizing the register usage when the operands are
   * temporaries. */
  Register push(Register left_operand, Register right_operand) {
    if (pop(right_operand)) { return push(left_operand); }
    if (pop(left_operand)) { return push(right_operand); }
    return push();
  }

  /** Returns a temporary register for a calculation involving the given
   * registers. Used for optimizing the register usage when the operands are
   * temporaries. */
  Register push(Register operand) {
    if (pop(operand)) { return operand; }
    return push();
  }

  /** Returns a temporary register. */
  Register push() {
    return new Register.Temporary(length++);
  }

  /** Marks the given register unused if it is a temporary. Useful for reducing
   * the needed temporaries. Returns whether the temporary was marked
   * available. */
  boolean pop(Register register) {
    boolean is_top =
      register instanceof Register.Temporary temporary
        && temporary.index() + 1 == length;
    if (is_top) { length--; }
    return is_top;
  }
}
