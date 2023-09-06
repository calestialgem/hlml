package hlml.loader;

import hlml.Source;
import hlml.lexer.Token;
import hlml.reporter.Subject;

/** Raw representation of the source file. */
public record LoadedSource(Source source, String contents) {
  /** Returns the source file's name. */
  public String name() {
    return source.name();
  }

  /** Returns a subject as a token in this source file. */
  public Subject subject(Token token) {
    return subject(token.start(), token.end());
  }

  /** Returns a subject as a range of characters in this source file. */
  public Subject subject(int start, int end) {
    return Subject.of(source.path(), contents, start, end);
  }
}
