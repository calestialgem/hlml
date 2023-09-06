package hlml.parser;

import java.util.List;

import hlml.lexer.LexedSource;
import hlml.reporter.Subject;

/** Syntactical representation of a source file. */
public record ParsedSource(
  LexedSource source,
  List<Node.Declaration> declarations)
{
  /** Returns the source file's name. */
  public String name() {
    return source.name();
  }

  /** Returns a subject as a node in this source file. */
  public Subject subject(Node node) {
    return source.subject(node);
  }
}
