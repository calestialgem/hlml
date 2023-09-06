package hlml;

import java.util.Map;
import java.util.Optional;

/** Tabulated user-defined constructs in a parcel. */
sealed interface Resolution {
  /** File that holds the contents of the program as text. */
  record Source(
    ParsedSource source,
    Optional<Node.Entrypoint> entrypoint,
    Map<String, Node.Definition> globals) implements Resolution
  {
    /** Returns the source file's name. */
    String name() {
      return source.name();
    }

    /** Returns a subject as a node in this source file. */
    Subject subject(Node node) {
      return source.subject(node);
    }
  }
}
