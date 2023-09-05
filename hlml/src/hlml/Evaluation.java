package hlml;

/** Result of executing an expression. */
sealed interface Evaluation {
  /** Evaluation in a register. */
  record Register(int index) implements Evaluation {}

  /** Evaluation that can be immediate operand, side-stepping stack. */
  record Immediate(double value) implements Evaluation {}
}
