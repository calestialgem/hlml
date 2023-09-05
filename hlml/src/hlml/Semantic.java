package hlml;

import java.util.List;
import java.util.Map;

/** Meaningful constructs in the program. */
sealed interface Semantic {
  /** Collective code that is executed on a processor. */
  record Program(Map<String, Parcel> parcels) implements Semantic {}

  /** Subdivisions of program that are in an acyclic dependency graph. */
  record Parcel(Map<String, Source> sources) implements Semantic {}

  /** Files that hold the code. */
  record Source(Map<String, Declaration> declarations) implements Semantic {}

  /** Definition of a construct in code. */
  sealed interface Declaration extends Semantic {}

  /** First instructions that are executed by the processor. */
  record Entrypoint(Statement body) implements Declaration {}

  /** Instructions to be executed by the processor. */
  sealed interface Statement extends Semantic {}

  /** Statements that are sequentially executed. */
  record Block(List<Statement> inner_statements) implements Statement {}
}
