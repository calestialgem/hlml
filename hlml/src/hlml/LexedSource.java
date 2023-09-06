package hlml;

import java.util.List;

/** Lexical representation of a source file. */
record LexedSource(LoadedSource source, List<Token> tokens) {
  /** Returns the source file's name. */
  String name() { return source.name(); }

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
    return source.subject(start, end);
  }
}
