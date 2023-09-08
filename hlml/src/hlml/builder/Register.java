package hlml.builder;

import java.io.IOException;
import java.text.DecimalFormat;

import hlml.checker.Name;

/** Memory location on a processor. */
sealed interface Register {
  /** Register that holds a global variable. */
  record Global(Name name) implements Register {
    @Override
    public void append_to(Appendable appendable) throws IOException {
      appendable.append(name.source());
      appendable.append('$');
      appendable.append(name.identifier());
    }
  }

  /** Register that holds a local variable. */
  record Local(String identifier) implements Register {
    @Override
    public void append_to(Appendable appendable) throws IOException {
      appendable.append('$');
      appendable.append(identifier);
    }
  }

  /** Register that holds a temporary value. */
  record Temporary(int index) implements Register {
    @Override
    public void append_to(Appendable appendable) throws IOException {
      appendable.append('$');
      appendable.append(Integer.toString(index));
    }
  }

  /** Register that holds a constant value. Literals are inlined; thus, this is
   * not actually taking a place in the processor. */
  record Literal(double value) implements Register {
    @Override
    public void append_to(Appendable appendable) throws IOException {
      DecimalFormat decimal_formatter = new DecimalFormat("0.#");
      decimal_formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
      appendable.append(decimal_formatter.format(value));
    }
  }

  /** Returns a register holding the global variable with the given name. */
  static Register global(Name name) {
    return new Global(name);
  }

  /** Returns a register holding the local variable with the given
   * identifier. */
  static Register local(String identifier) {
    return new Local(identifier);
  }

  /** Returns a literal holding the given value. */
  static Register literal(double value) {
    return new Literal(value);
  }

  /** Appends the register to an appendable. */
  void append_to(Appendable appendable) throws IOException;
}
