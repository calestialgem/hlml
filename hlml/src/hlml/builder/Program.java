package hlml.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Ordered collection of instructions are executed sequentially for a
 * meaningful usage of the processor. */
final class Program {
  /** Returns an empty program. */
  static Program create() {
    return new Program(new ArrayList<>());
  }

  /** Instructions that are added to the program. */
  private List<Instruction> instructions;

  /** Constructs. */
  private Program(List<Instruction> instructions) {
    this.instructions = instructions;
  }

  /** Add an instruction the the end of the program. */
  void instruct(Instruction instruction) {
    instructions.add(instruction);
  }

  /** Appends the program to an appendable. */
  void append_to(Appendable appendable) throws IOException {
    for (Instruction instruction : instructions) {
      instruction.append_to(appendable);
      appendable.append(System.lineSeparator());
    }
  }
}
