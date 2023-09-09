package hlml.builder;

import hlml.checker.Name;

/** Memory location on a processor or a literal. Literals are inlined; thus,
 * this is might not actually have a location in the processor. */
sealed interface Register {
  /** Memory location that hosts a global variable. */
  record Global(Name name) implements Register {}

  /** Memory location that hosts a local variable. */
  record Local(Name symbol, String identifier) implements Register {}

  /** Memory location that holds a temporary value. */
  record Temporary(int index) implements Register {}

  /** Literal that holds a constant. */
  record Constant(double value) implements Register {}

  /** Literal that holds an instruction index. */
  record Instruction(Waypoint waypoint) implements Register {}

  /** Literal that holds the program counter, which is the currently executed
   * instruction. */
  record Counter() implements Register {}

  /** Literal that holds no value. */
  record Null() implements Register {}

  /** Returns a register hosting the global variable with the given name. */
  static Register global(Name name) {
    return new Global(name);
  }

  /** Returns a register hosting the local variable with the given
   * identifier. */
  static Register local(Name symbol, String identifier) {
    return new Local(symbol, identifier);
  }

  /** Returns a literal holding the given constant. */
  static Register constant(double value) {
    return new Constant(value);
  }

  /** Returns a literal holding the given instruction index. */
  static Register instruction(Waypoint waypoint) {
    return new Instruction(waypoint);
  }

  /** Returns a literal holding the program counter. */
  static Register counter() {
    return new Counter();
  }

  /** Returns a literal holding nothing. */
  static Register null_() {
    return new Null();
  }
}