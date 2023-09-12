package hlml.builder;

import hlml.checker.Name;
import hlml.checker.Semantic;

/** Memory location on a processor or a literal. Literals are inlined; thus,
 * this is might not actually have a location in the processor. */
sealed interface Register {
  /** Memory location that hosts a global variable. */
  record Global(Name name) implements Register {
    @Override
    public boolean is_volatile() { return true; }
  }

  /** Memory location that hosts a local variable. */
  record Local(Name symbol, String identifier) implements Register {
    @Override
    public boolean is_volatile() { return true; }
  }

  /** Memory location that holds a temporary value. */
  record Temporary(int index) implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that links to a building. */
  record Link(String building) implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds a constant number. */
  record NumberConstant(double value) implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds a constant color. */
  record ColorConstant(int value) implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds a constant string. */
  record StringConstant(String value) implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds an instruction index. */
  record Instruction(Waypoint waypoint) implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds a variable built-in to the processor. */
  record Builtin(String name) implements Register {
    @Override
    public boolean is_volatile() { return true; }
  }

  /** Literal that holds the false Boolean value. */
  record False() implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds the true Boolean value. */
  record True() implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Literal that holds no value. */
  record Null() implements Register {
    @Override
    public boolean is_volatile() { return false; }
  }

  /** Returns a register hosting the global variable with the given name. */
  static Register global(Name name) {
    return new Global(name);
  }

  /** Returns a register hosting the parameter of the given procedure at the
   * given index. */
  static Register parameter(
    Semantic.UserDefinedProcedure procedure,
    int index)
  {
    return local(
      procedure.name(),
      procedure.parameters().get(index).identifier());
  }

  /** Returns a register hosting the local variable with the given
   * identifier. */
  static Register local(Name symbol, String identifier) {
    return new Local(symbol, identifier);
  }

  /** Returns a literal linking to the given building. */
  static Register link(String building) {
    return new Link(building);
  }

  /** Returns a literal holding the given constant number. */
  static Register number(double value) {
    return new NumberConstant(value);
  }

  /** Returns a literal holding the given constant color. */
  static Register color(int value) {
    return new ColorConstant(value);
  }

  /** Returns a literal holding the given constant string. */
  static Register string(String value) {
    return new StringConstant(value);
  }

  /** Returns a literal holding the given instruction index. */
  static Register instruction(Waypoint waypoint) {
    return new Instruction(waypoint);
  }

  /** Returns a literal holding the a built-in processor variable. */
  static Register builtin(String name) {
    return new Builtin(name);
  }

  /** Returns a literal holding false Boolean value. */
  static Register false_() {
    return new False();
  }

  /** Returns a literal holding true Boolean value. */
  static Register true_() {
    return new True();
  }

  /** Returns a literal holding nothing. */
  static Register null_() {
    return new Null();
  }

  /** Returns whether setting this register cannot be ignored even if it would
   * not be read afterwards. */
  boolean is_volatile();
}
