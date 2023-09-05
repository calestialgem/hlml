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

  /** Expression made up of one operand and an operator at the left. */
  sealed interface UnaryOperation extends Expression {
    /** Operand of the operator. */
    Expression operand();

    @Override
    default int first(List<Token> tokens) {
      return operand().first(tokens) - 1;
    }

    @Override
    default int last(List<Token> tokens) { return operand().last(tokens); }
  }

  /** Expression made up of two operands and an operator in the middle. */
  sealed interface BinaryOperation extends Expression {
    /** Operand that is at the left of the operator. */
    Expression left_operand();

    /** Operand that is at the right of the operator. */
    Expression right_operand();

    @Override
    default int first(List<Token> tokens) {
      return left_operand().first(tokens);
    }

    @Override
    default int last(List<Token> tokens) {
      return right_operand().last(tokens);
    }
  }

  /** Expressions at precedence level 9. */
  sealed interface Precedence9 extends Expression {}

  /** Expression that yields one when the left operand is equal to the right
   * operand, and zero otherwise. */
  record EqualTo(Precedence9 left_operand, Precedence9 right_operand)
    implements Precedence9, BinaryOperation
  {}

  /** Expression that yields one when the left operand is not equal to the right
   * operand, and zero otherwise. */
  record NotEqualTo(Precedence9 left_operand, Precedence9 right_operand)
    implements Precedence9, BinaryOperation
  {}

  /** Expression that yields one when the left operand is equal to the right
   * operand without any operand undergoing implicit conversions, and zero
   * otherwise. */
  record StrictlyEqualTo(Precedence9 left_operand, Precedence9 right_operand)
    implements Precedence9, BinaryOperation
  {}

  /** Expressions at precedence level 8. */
  sealed interface Precedence8 extends Precedence9 {}

  /** Expression that yields one when the left operand is less than the right
   * operand, and zero otherwise. */
  record LessThan(Precedence8 left_operand, Precedence8 right_operand)
    implements Precedence8, BinaryOperation
  {}

  /** Expression that yields one when the left operand is less than or equal to
   * the right operand, and zero otherwise. */
  record LessThanOrEqualTo(Precedence8 left_operand, Precedence8 right_operand)
    implements Precedence8, BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than the right
   * operand, and zero otherwise. */
  record GreaterThan(Precedence8 left_operand, Precedence8 right_operand)
    implements Precedence8, BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than or equal
   * to the right operand, and zero otherwise. */
  record GreaterThanOrEqualTo(
    Precedence8 left_operand,
    Precedence8 right_operand) implements Precedence8, BinaryOperation
  {}

  /** Expressions at precedence level 7. */
  sealed interface Precedence7 extends Precedence8 {}

  /** Expression that yields a number that has the bit pattern that is the OR'ed
   * version of its operands matching bits. */
  record BitwiseOr(Precedence7 left_operand, Precedence7 right_operand)
    implements Precedence7, BinaryOperation
  {}

  /** Expressions at precedence level 6. */
  sealed interface Precedence6 extends Precedence7 {}

  /** Expression that yields a number that has the bit pattern that is the
   * XOR'ed version of its operands matching bits. */
  record BitwiseXor(Precedence6 left_operand, Precedence6 right_operand)
    implements Precedence6, BinaryOperation
  {}

  /** Expressions at precedence level 5. */
  sealed interface Precedence5 extends Precedence6 {}

  /** Expression that yields a number that has the bit pattern that is the
   * AND'ed version of its operands matching bits. */
  record BitwiseAnd(Precedence5 left_operand, Precedence5 right_operand)
    implements Precedence5, BinaryOperation
  {}

  /** Expressions at precedence level 4. */
  sealed interface Precedence4 extends Precedence5 {}

  /** Expression that yields the left operand's bits shifted left by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record LeftShift(Precedence4 left_operand, Precedence4 right_operand)
    implements Precedence4, BinaryOperation
  {}

  /** Expression that yields the left operand's bits shifted right by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record RightShift(Precedence4 left_operand, Precedence4 right_operand)
    implements Precedence4, BinaryOperation
  {}

  /** Expressions at precedence level 3. */
  sealed interface Precedence3 extends Precedence4 {}

  /** Expression that yields the addition of its operands. */
  record Addition(Precedence3 left_operand, Precedence3 right_operand)
    implements Precedence3, BinaryOperation
  {}

  /** Expression that yields the subtraction of its operands. */
  record Subtraction(Precedence3 left_operand, Precedence3 right_operand)
    implements Precedence3, BinaryOperation
  {}

  /** Expressions at precedence level 2. */
  sealed interface Precedence2 extends Precedence3 {}

  /** Expression that yields the multiplication of its operands. */
  record Multiplication(Precedence2 left_operand, Precedence2 right_operand)
    implements Precedence2, BinaryOperation
  {}

  /** Expression that yields the division of its operands. */
  record Division(Precedence2 left_operand, Precedence2 right_operand)
    implements Precedence2, BinaryOperation
  {}

  /** Expression that yields the floor of the division of its operands. */
  record IntegerDivision(Precedence2 left_operand, Precedence2 right_operand)
    implements Precedence2, BinaryOperation
  {}

  /** Expression that yields the left operand in mod right operand. */
  record Modulus(Precedence2 left_operand, Precedence2 right_operand)
    implements Precedence2, BinaryOperation
  {}

  /** Expressions at precedence level 1. */
  sealed interface Precedence1 extends Precedence2 {}

  /** Expression that keeps the sign of a number. */
  record Promotion(Precedence1 operand)
    implements Precedence1, UnaryOperation
  {}

  /** Expression that flips the sign of a number. */
  record Negation(Precedence1 operand) implements Precedence1, UnaryOperation {}

  /** Expression that flips every bit when the value is taken as an 53-bit
   * signed two's complement integer. */
  record BitwiseNot(Precedence1 operand)
    implements Precedence1, UnaryOperation
  {}

  /** Expression that yields one when the operand is zero, and zero
   * otherwise. */
  record LogicalNot(Precedence1 operand)
    implements Precedence1, UnaryOperation
  {}

  /** Expressions at precedence level 0. */
  sealed interface Precedence0 extends Precedence1 {}

  /** Expression that directly denotes a compile-time known number value. */
  record NumberConstant(int first, double value) implements Precedence0 {
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
