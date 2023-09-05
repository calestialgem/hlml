package hlml;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Tabulated user-defined constructs in a parcel. */
sealed interface Resolution {
  /** File that holds the contents of the program as text. */
  record Source(
    Path file,
    String contents,
    List<Token> tokens,
    Map<String, Declaration> declarations) implements Resolution
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

  /** Definition of an entity. */
  sealed interface Declaration extends Resolution {
    /** Word that can be used to refer to an entity. */
    String designator();

    /** Declaration as a node. */
    Node.Declaration node();
  }

  /** Declaration of the program's first instructions. */
  record Entrypoint(Node.Entrypoint node) implements Declaration {
    @Override
    public String designator() { return "entrypoint"; }
  }
}
