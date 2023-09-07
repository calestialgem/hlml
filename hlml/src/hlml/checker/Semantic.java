package hlml.checker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Meaningful constructs in the program. */
public sealed interface Semantic {
  /** Collective understanding of a piece of code. */
  record Target(String name, Map<String, Source> sources) implements Semantic {}

  /** Files that hold the code. */
  record Source(
    Optional<Entrypoint> entrypoint,
    Map<String, Definition> globals) implements Semantic
  {}

  /** Asserting a fact about the program. */
  sealed interface Declaration extends Semantic {
    /** Returns the names of definitions this entity needs before it can be
     * understood. */
    Set<Name> dependencies();
  }

  /** First instructions that are executed by the processor. */
  record Entrypoint(Statement body) implements Declaration {
    @Override
    public Set<Name> dependencies() { return body.dependencies(); }
  }

  /** Definition of a construct in code. */
  sealed interface Definition extends Declaration {
    /** Word that designates this definition. */
    String identifier();
  }

  /** Definition of a constant. */
  record Const(String identifier, double value) implements Definition {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Definition of a variable. */
  record Var(String identifier, Optional<Expression> initial_value)
    implements Definition, Statement
  {
    @Override
    public Set<Name> dependencies() {
      return initial_value.map(Expression::dependencies).orElse(Set.of());
    }
  }

  /** Instructions to be executed by the processor. */
  sealed interface Statement extends Semantic {
    /** Returns the names of definitions this entity needs before it can be
     * understood. */
    Set<Name> dependencies();
  }

  /** Statements that are sequentially executed. */
  record Block(List<Statement> inner_statements) implements Statement {
    @Override
    public Set<Name> dependencies() {
      return Sets.union(inner_statements.stream().map(Statement::dependencies));
    }
  }

  /** Statements that mutate a variable's value. */
  record Assignment(VariableAccess variable, Expression new_value)
    implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return Sets.union(variable.dependencies(), new_value.dependencies());
    }
  }

  /** Statements that evaluate an expression and discard the value. */
  record Discard(Expression discarded) implements Statement {
    @Override
    public Set<Name> dependencies() { return discarded.dependencies(); }
  }

  /** Value calculations to be evaluated by the processor. */
  sealed interface Expression extends Semantic {
    /** Returns the names of definitions this entity needs before it can be
     * understood. */
    Set<Name> dependencies();
  }

  /** Expression made up of one operand and an operator at the left. */
  sealed interface UnaryOperation extends Expression {
    /** Operand of the operator. */
    Expression operand();

    @Override
    default Set<Name> dependencies() { return operand().dependencies(); }
  }

  /** Expression made up of two operands and an operator in the middle. */
  sealed interface BinaryOperation extends Expression {
    /** Operand that is at the left of the operator. */
    Expression left_operand();

    /** Operand that is at the right of the operator. */
    Expression right_operand();

    @Override
    default Set<Name> dependencies() {
      return Sets
        .union(left_operand().dependencies(), right_operand().dependencies());
    }
  }

  /** Expression that yields one when the left operand is equal to the right
   * operand, and zero otherwise. */
  record EqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is not equal to the right
   * operand, and zero otherwise. */
  record NotEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is equal to the right
   * operand without any operand undergoing implicit conversions, and zero
   * otherwise. */
  record StrictlyEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is less than the right
   * operand, and zero otherwise. */
  record LessThan(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is less than or equal to
   * the right operand, and zero otherwise. */
  record LessThanOrEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than the right
   * operand, and zero otherwise. */
  record GreaterThan(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than or equal
   * to the right operand, and zero otherwise. */
  record GreaterThanOrEqualTo(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields a number that has the bit pattern that is the OR'ed
   * version of its operands matching bits. */
  record BitwiseOr(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields a number that has the bit pattern that is the
   * XOR'ed version of its operands matching bits. */
  record BitwiseXor(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields a number that has the bit pattern that is the
   * AND'ed version of its operands matching bits. */
  record BitwiseAnd(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the left operand's bits shifted left by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record LeftShift(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the left operand's bits shifted right by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record RightShift(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the addition of its operands. */
  record Addition(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the subtraction of its operands. */
  record Subtraction(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the multiplication of its operands. */
  record Multiplication(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the division of its operands. */
  record Division(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the floor of the division of its operands. */
  record IntegerDivision(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that yields the left operand in mod right operand. */
  record Modulus(Expression left_operand, Expression right_operand)
    implements BinaryOperation
  {}

  /** Expression that keeps the sign of a number. */
  record Promotion(Expression operand) implements UnaryOperation {}

  /** Expression that flips the sign of a number. */
  record Negation(Expression operand) implements UnaryOperation {}

  /** Expression that flips every bit when the value is taken as an 53-bit
   * signed two's complement integer. */
  record BitwiseNot(Expression operand) implements UnaryOperation {}

  /** Expression that yields one when the operand is zero, and zero
   * otherwise. */
  record LogicalNot(Expression operand) implements UnaryOperation {}

  /** Expression that evaluates to a hard-coded numeric value. */
  record NumberConstant(double value) implements Expression {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to an unknown. */
  sealed interface VariableAccess extends Expression {}

  /** Expression that evaluates to an unknown in the global scope. */
  record GlobalVariableAccess(Name name) implements VariableAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(name); }
  }

  /** Expression that evaluates to an unknown in the local scope. */
  record LocalVariableAccess(String identifier) implements VariableAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }
}
