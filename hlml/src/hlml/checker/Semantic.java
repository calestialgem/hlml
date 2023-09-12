package hlml.checker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Meaningful constructs in the program. */
public sealed interface Semantic {
  /** Scope that the built-in symbols are defined into. */
  String built_in_scope = "mlog";

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
    /** Name of the definition. */
    Name name();
  }

  /** Defining a symbol as building linked to the processor. */
  record Link(Name name, String building) implements Definition {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Definition of a procedure that could be built-in or user-defined. */
  sealed interface Procedure extends Definition {
    /** Number of parameters this procedure takes. */
    int parameter_count();
  }

  /** Procedures that are user-defined. */
  record UserDefinedProcedure(
    Name name,
    List<Parameter> parameters,
    Statement body) implements Procedure
  {
    @Override
    public int parameter_count() { return parameters.size(); }

    @Override
    public Set<Name> dependencies() { return body.dependencies(); }
  }

  /** Definition of a procedure's parameter. */
  record Parameter(String identifier, boolean in_out) implements Semantic {}

  /** Procedures that directly map to instructions. */
  record BuiltinProcedure(
    String identifier,
    String instruction_text,
    int parameter_count) implements Procedure
  {
    @Override
    public Name name() { return new Name(built_in_scope, identifier); }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Procedures that directly map to instructions with a dummy argument at the
   * second place. */
  record BuiltinProcedureWithDummy(
    String identifier,
    String instruction_text,
    String dummy_argument,
    int parameter_count) implements Procedure
  {
    @Override
    public Name name() { return new Name(built_in_scope, identifier); }

    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Definition of a constant. */
  sealed interface Constant extends Definition {
    /** Value of the constant. */
    Known value();

    @Override
    default Set<Name> dependencies() { return Set.of(); }
  }

  /** Constants that are defined by the user. */
  record UserDefinedConstant(Name name, Known value) implements Constant {}

  /** Constants that directly map to a processors variables. */
  record BuiltinConstant(String identifier, KnownBuiltin value)
    implements Constant
  {
    @Override
    public Name name() { return new Name(built_in_scope, identifier); }
  }

  /** Definition of a global variable. */
  record GlobalVar(Name name, Optional<Expression> initial_value)
    implements Definition
  {
    @Override
    public Set<Name> dependencies() {
      return initial_value.map(Expression::dependencies).orElseGet(Set::of);
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

  /** Statements that branch the control flow. */
  record If(
    List<LocalVar> variables,
    Expression condition,
    Statement true_branch,
    Optional<Statement> false_branch) implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return Sets
        .union(
          Sets.union(variables.stream().map(LocalVar::dependencies)),
          condition.dependencies(),
          true_branch.dependencies(),
          false_branch.map(Statement::dependencies).orElseGet(Set::of));
    }
  }

  /** Statements that loop the control flow. */
  record While(
    List<LocalVar> variables,
    Expression condition,
    Optional<Statement> interleaved,
    Statement loop,
    Optional<Statement> zero_branch) implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return Sets
        .union(
          Sets.union(variables.stream().map(LocalVar::dependencies)),
          condition.dependencies(),
          interleaved.map(Statement::dependencies).orElseGet(Set::of),
          loop.dependencies(),
          zero_branch.map(Statement::dependencies).orElseGet(Set::of));
    }
  }

  /** Statements that exit a loop. */
  record Break(int loop) implements Statement {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Statements that skip the remaining in a loop. */
  record Continue(int loop) implements Statement {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Statements that provide a value to the procedures caller. */
  record Return(Optional<Expression> value) implements Statement {
    @Override
    public Set<Name> dependencies() {
      return value.map(Expression::dependencies).orElseGet(Set::of);
    }
  }

  /** Definition of a local variable. */
  record LocalVar(String identifier, Optional<Expression> initial_value)
    implements Statement
  {
    @Override
    public Set<Name> dependencies() {
      return initial_value.map(Expression::dependencies).orElseGet(Set::of);
    }
  }

  /** Statements that affect the processors context. Useful for parsing as all
   * the initial tokens of these statements are same as expressions. */
  sealed interface Affect extends Statement {}

  /** Statements that mutate a variable. */
  sealed interface Mutate extends Affect {
    /** The mutated variable. */
    VariableAccess target();

    @Override
    default Set<Name> dependencies() { return target().dependencies(); }
  }

  /** Statements that increment the value hold in a variable. */
  record Increment(VariableAccess target) implements Mutate {}

  /** Statements that decrement the value hold in a variable. */
  record Decrement(VariableAccess target) implements Mutate {}

  /** Statements that set the value hold in a variable. */
  sealed interface Assign extends Affect {
    /** The changed variable. */
    VariableAccess target();

    /** The new value or the right operand. */
    Expression source();

    @Override
    default Set<Name> dependencies() {
      return Sets.union(target().dependencies(), source().dependencies());
    }
  }

  /** Statements that set the target to be the same as the source. */
  record DirectlyAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the multiplication of the target and
   * the source. */
  record MultiplyAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the division of the target and the
   * source. */
  record DivideAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the integer division of the target
   * and the source. */
  record DivideIntegerAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the modulus of the target and the
   * source. */
  record ModulusAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the addition of the target and the
   * source. */
  record AddAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the subtract of the target and the
   * source. */
  record SubtractAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the left shift of the target and the
   * source. */
  record ShiftLeftAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the right shift of the target and the
   * source. */
  record ShiftRightAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise and of the target and the
   * source. */
  record AndBitwiseAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise xor of the target and the
   * source. */
  record XorBitwiseAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise or of the target and the
   * source. */
  record OrBitwiseAssign(VariableAccess target, Expression source)
    implements Assign
  {}

  /** Statements that evaluate an expression and discard its value. Useful for
   * side effects, not the value. */
  record Discard(Expression source) implements Affect {
    @Override
    public Set<Name> dependencies() { return source.dependencies(); }
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

  /** Expression that evaluates to the value held by a symbol. */
  sealed interface SymbolAccess extends Expression {}

  /** Expression that has a known value. */
  sealed interface Known extends SymbolAccess {
    @Override
    default Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to a built-in constant. */
  record KnownBuiltin(String name) implements Known {}

  /** Expression that evaluates to a hard-coded number value. */
  record KnownNumber(double value) implements Known {}

  /** Expression that evaluates to a hard-coded color value. */
  record KnownColor(int value) implements Known {}

  /** Expression that evaluates to a hard-coded string value. */
  record KnownString(String value) implements Known {}

  /** Expression that evaluates to a link. */
  record LinkAccess(String building) implements SymbolAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to the value held by a variable. */
  sealed interface VariableAccess extends SymbolAccess {}

  /** Expression that evaluates to a global variable's value. */
  record GlobalVariableAccess(Name name) implements VariableAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(name); }
  }

  /** Expression that evaluates to a local variable's value. */
  record LocalVariableAccess(String identifier) implements VariableAccess {
    @Override
    public Set<Name> dependencies() { return Set.of(); }
  }

  /** Expression that evaluates to the return value of executing a procedure
   * with a given argument list. */
  record Call(Name procedure, List<Expression> arguments)
    implements Expression
  {
    @Override
    public Set<Name> dependencies() {
      return Sets
        .union(
          Set.of(procedure),
          Sets.union(arguments.stream().map(Expression::dependencies)));
    }
  }
}
