package hlml.resolver;

import java.util.Map;
import java.util.Optional;

import hlml.parser.Node;
import hlml.parser.ParsedSource;
import hlml.reporter.Subject;

/** Tabulated user-defined constructs in a parcel. */
public sealed interface Resolution {
  /** File that holds the contents of the program as text. */
  record Source(
    ParsedSource source,
    Optional<Node.Entrypoint> entrypoint,
    Map<String, Node.Definition> globals) implements Resolution
  {
    /** Returns the source file's name. */
    public String name() {
      return source.name();
    }

    /** Returns a subject as a node in this source file. */
    public Subject subject(Node node) {
      return source.subject(node);
    }
  }
}
