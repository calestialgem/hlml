package hlml.lexer;

import java.util.List;

import hlml.loader.LoadedSource;
import hlml.parser.Node;
import hlml.reporter.Subject;

/** Lexical representation of a source file. */
public record LexedSource(LoadedSource source, List<Token> tokens) {
  /** Returns the source file's name. */
  public String name() {
    return source.name();
  }

  /** Returns a subject as a node in this source file. */
  public Subject subject(Node node) {
    return subject(node.start(tokens), node.end(tokens));
  }

  /** Returns a subject as a token in this source file. */
  public Subject subject(Token token) {
    return subject(token.start(), token.end());
  }

  /** Returns a subject as a range of characters in this source file. */
  public Subject subject(int start, int end) {
    return source.subject(start, end);
  }
}
