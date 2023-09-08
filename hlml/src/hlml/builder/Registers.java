package hlml.builder;

import hlml.checker.Name;

/** Holds knowledge about the registers in the processor. */
final class Registers {
  /** Returns an empty register set. */
  static Registers create() {
    return new Registers(0);
  }

  /** Current number of temporary value holding registers. */
  private int temporaries;

  /** Constructor. */
  private Registers(int temporaries) {
    this.temporaries = temporaries;
  }

  /** Returns a global register. */
  Register global(Name name) {
    return new Register.Global(name);
  }

  /** Returns a local register. */
  Register local(String identifier) {
    return new Register.Local(identifier);
  }

  /** Returns a temporary register for a calculation involving the given
   * registers. Used for optimizing the register usage when the operands are
   * temporaries. */
  Register temporary(Register left_operand, Register right_operand) {
    if (discard(right_operand)) { return temporary(left_operand); }
    if (discard(left_operand)) { return temporary(right_operand); }
    return temporary();
  }

  /** Returns a temporary register for a calculation involving the given
   * registers. Used for optimizing the register usage when the operands are
   * temporaries. */
  Register temporary(Register operand) {
    if (discard(operand)) { return operand; }
    return temporary();
  }

  /** Returns a temporary register. */
  Register temporary() {
    return new Register.Temporary(temporaries++);
  }

  /** Marks the given register unused if it is a temporary. Useful for reducing
   * the needed temporaries. Returns whether the temporary was marked
   * available. */
  boolean discard(Register register) {
    boolean is_top_temporary =
      register instanceof Register.Temporary temporary
        && temporary.index() + 1 == temporaries;
    if (is_top_temporary) { temporaries--; }
    return is_top_temporary;
  }

  /** Returns a literal register. */
  Register literal(double value) {
    return new Register.Literal(value);
  }
}
