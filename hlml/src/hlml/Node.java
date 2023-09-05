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
  record Block(int first, List<Statement> inner_statements)
    implements Statement
  {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) {
      if (!inner_statements.isEmpty()) {
        return inner_statements.get(inner_statements.size() - 1).last(tokens)
          + 1;
      }
      return first + 1;
    }
  }

  /** Statements that evaluates an expression and discards it. */
  record Discard(Expression discarded) implements Statement {
    @Override
    public int first(List<Token> tokens) { return discarded.first(tokens); }

    @Override
    public int last(List<Token> tokens) { return discarded.last(tokens) + 1; }
  }

  /** Calculations that denote a value. */
  sealed interface Expression extends Node {}

  /** Expression that directly denotes a compile-time known number value. */
  record NumberConstant(int first, double value) implements Expression {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) { return first; }
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
