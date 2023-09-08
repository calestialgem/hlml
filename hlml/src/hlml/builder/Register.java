package hlml.builder;

import hlml.checker.Name;

/** Memory location on a processor. */
sealed interface Register {
  /** Register that holds a global variable. */
  record Global(Name name) implements Register {}

  /** Register that holds a local variable. */
  record Local(String identifier) implements Register {}

  /** Register that holds a temporary value. */
  record Temporary(int index) implements Register {}

  /** Register that holds a constant value. Literals are inlined; thus, this is
   * not actually taking a place in the processor. */
  record Literal(double value) implements Register {}
}
