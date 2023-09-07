package hlml.builder;

/** Result of executing an expression. */
sealed interface Evaluation {
  /** Evaluation of a global variable. */
  record GlobalVariable(int index) implements Evaluation {}

  /** Evaluation of a local variable. */
  record LocalVariable(int index) implements Evaluation {}

  /** Evaluation in a register. */
  record Register(int index) implements Evaluation {}

  /** Evaluation that can be immediate operand, side-stepping stack. */
  record Immediate(double value) implements Evaluation {}
}
