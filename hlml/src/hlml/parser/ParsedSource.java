package hlml.parser;

import java.nio.file.Path;
import java.util.List;

import hlml.lexer.LexedSource;
import hlml.lexer.Token;

/** Syntactical representation of a source file. */
public class ParsedSource extends LexedSource {
  /** List of the declarations in the source file. */
  public final List<Node.Declaration> declarations;

  /** Constructs. */
  public ParsedSource(
    Path path,
    String contents,
    List<Token> tokens,
    List<Node.Declaration> declarations)
  {
    super(path, contents, tokens);
    this.declarations = declarations;
  }

  /** Constructs. */
  public ParsedSource(LexedSource parent, List<Node.Declaration> declarations) {
    this(parent.path, parent.contents, parent.tokens, declarations);
  }
}
