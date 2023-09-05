package hlml;

import java.util.function.Function;

/** Stores information on how to parse a unary operation. */
record UnaryOperationParser<PrecedenceType extends Node.Expression>(
  Class<? extends Token> operator_class,
  Function<PrecedenceType, PrecedenceType> initializer,
  String name)
{}
