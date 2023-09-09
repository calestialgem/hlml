package hlml.loader;

import java.nio.file.Path;

import hlml.Source;
import hlml.lexer.Token;
import hlml.reporter.Subject;

/** Raw representation of the source file. */
public class LoadedSource extends Source {
  /** Contents of the source file when it was last read. */
  public final String contents;

  /** Constructs. */
  public LoadedSource(Path path, String contents) {
    super(path);
    this.contents = contents;
  }

  /** Constructs. */
  public LoadedSource(Source parent, String contents) {
    this(parent.path, contents);
  }

  /** Returns a token's text. */
  public String text(Token token) {
    return token.text(contents);
  }

  /** Returns a subject as a token in this source file. */
  public Subject subject(Token token) {
    return subject(token.start(), token.end());
  }

  /** Returns a subject as a character in this source file. */
  public Subject subject(int index) {
    return subject(index, index + 1);
  }

  /** Returns a subject as a range of characters in this source file. */
  public Subject subject(int start, int end) {
    return Subject.of(path, contents, start, end);
  }
}
