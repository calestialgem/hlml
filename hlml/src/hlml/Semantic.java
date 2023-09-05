package hlml;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Meaningful constructs in the program. */
sealed interface Semantic {
  /** Collective understanding of a piece of code. */
  record Target(Map<String, Source> sources, Optional<Entrypoint> entrypoint)
    implements Semantic
  {}

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
