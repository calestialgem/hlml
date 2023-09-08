package hlml.resolver;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hlml.lexer.Token;
import hlml.parser.Node;
import hlml.parser.ParsedSource;

/** Tabulated user-defined constructs in a source file. */
public class ResolvedSource extends ParsedSource {
  /** Resolved entrypoint in the file if there is one. */
  public final Optional<Node.Entrypoint> entrypoint;

  /** Resolved global symbols in the file. */
  public final Map<String, Node.Definition> globals;

  /** Constructs. */
  public ResolvedSource(
    Path path,
    String contents,
    List<Token> tokens,
    List<Node.Declaration> declarations,
    Optional<Node.Entrypoint> entrypoint,
    Map<String, Node.Definition> globals)
  {
    super(path, contents, tokens, declarations);
    this.entrypoint = entrypoint;
    this.globals = globals;
  }

  /** Constructs. */
  public ResolvedSource(
    ParsedSource parent,
    Optional<Node.Entrypoint> entrypoint,
    Map<String, Node.Definition> globals)
  {
    this(
      parent.path,
      parent.contents,
      parent.tokens,
      parent.declarations,
      entrypoint,
      globals);
  }
}
