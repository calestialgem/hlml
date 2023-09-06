package hlml;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Tabulated user-defined constructs in a parcel. */
sealed interface Resolution {
  /** File that holds the contents of the program as text. */
  record Source(
    Path file,
    String contents,
    List<Token> tokens,
    Optional<Node.Entrypoint> entrypoint,
    Map<String, Node.Definition> globals) implements Resolution
  {
    /** Returns a subject as a node in this source file. */
    Subject subject(Node node) {
      return subject(node.start(tokens), node.end(tokens));
    }

    /** Returns a subject as a token in this source file. */
    Subject subject(Token token) {
      return subject(token.start(), token.end());
    }

    /** Returns a subject as a range of characters in this source file. */
    Subject subject(int start, int end) {
      return Subject.of(file, contents, start, end);
    }
  }
}
