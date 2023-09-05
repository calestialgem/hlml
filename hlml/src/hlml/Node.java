package hlml;

import java.util.List;

/** Hierarchical collection of tokens in the source file. */
sealed interface Node {
  /** Defining a new entity. */
  sealed interface Declaration extends Node {}

  /** Declaration of the program's first instructions. */
  record Entrypoint(Statement body) implements Declaration {
    @Override
    public int first(List<Token> tokens) { return body.first(tokens) - 1; }

    @Override
    public int last(List<Token> tokens) { return body.last(tokens); }
  }

  /** Instructions that can be given to the processor. */
  sealed interface Statement extends Node {}

  /** Sequentially executed collection of instructions. */
  record Block(int first, List<Statement> body) implements Statement {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) {
      if (!body.isEmpty())
        return body.get(body.size() - 1).last(tokens);
      return first + 1;
    }
  }

  /** Index of the node's first token. Used for reporting diagnostics with a
   * source location. */
  int first(List<Token> tokens);

  /** Index of the node's last token. Used for reporting diagnostics with a
   * source location. */
  int last(List<Token> tokens);

  /** Index of the node's first character's first byte from the beginning of the
   * file. Used for reporting diagnostics with a source location. */
  default int start(List<Token> tokens) {
    return tokens.get(first(tokens)).start();
  }

  /** Index of the first byte of the character after the node's last one from
   * the beginning of the file. Used for reporting diagnostics with a source
   * location. */
  default int end(List<Token> tokens) {
    return tokens.get(last(tokens)).end();
  }
}