package hlml;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Tabulated user-defined constructs in a parcel. */
sealed interface Resolution {
  /** File that holds the contents of the program as text. */
  record Source(
    Path path,
    String contents,
    List<Token> tokens,
    Map<String, Declaration> declarations) implements Resolution
  {}

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
