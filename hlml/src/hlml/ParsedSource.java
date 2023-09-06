package hlml;

import java.util.List;

/** Syntactical representation of a source file. */
record ParsedSource(LexedSource source, List<Node.Declaration> declarations) {
  /** Returns the source file's name. */
  String name() { return source.name(); }

  /** Returns a subject as a node in this source file. */
  Subject subject(Node node) {
    return source.subject(node);
  }
}
