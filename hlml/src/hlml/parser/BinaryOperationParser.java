package hlml.parser;

import java.util.function.BiFunction;

import hlml.lexer.Token;

/** Stores information on how to parse a binary operation. */
record BinaryOperationParser<PrecedenceType extends Node.Expression>(
  Class<? extends Token> operator_class,
  BiFunction<PrecedenceType, PrecedenceType, PrecedenceType> initializer,
  String name)
{}
