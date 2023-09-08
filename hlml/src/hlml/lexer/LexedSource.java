package hlml.lexer;

import java.nio.file.Path;
import java.util.List;

import hlml.loader.LoadedSource;
import hlml.parser.Node;
import hlml.reporter.Subject;

/** Lexical representation of a source file. */
public class LexedSource extends LoadedSource {
  /** List of the tokens in the source file. */
  public final List<Token> tokens;

  /** Constructs. */
  public LexedSource(Path path, String contents, List<Token> tokens) {
    super(path, contents);
    this.tokens = tokens;
  }

  /** Constructs. */
  public LexedSource(LoadedSource parent, List<Token> tokens) {
    this(parent.path, parent.contents, tokens);
  }

  /** Returns a node's text. */
  public String text(Node node) {
    return node.text(contents, tokens);
  }

  /** Returns a declaration's representative's text. */
  public String representative_text(Node.Declaration declaration) {
    return text(representative(declaration));
  }

  /** Returns a subject as a declaration in this source file. */
  public Subject subject(Node.Declaration declaration) {
    return subject(representative(declaration));
  }

  /** Returns a declaration's representative. */
  public Token representative(Node.Declaration declaration) {
    return declaration.representative(tokens);
  }

  /** Returns a subject as a node in this source file. */
  public Subject subject(Node node) {
    return subject(node.start(tokens), node.end(tokens));
  }
}
