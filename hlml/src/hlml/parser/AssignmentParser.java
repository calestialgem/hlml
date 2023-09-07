package hlml.parser;

import java.util.function.BiFunction;

/** Stores information on how to parse an assignment statement. */
record AssignmentParser(
  BiFunction<Node.SymbolAccess, Node.Expression, Node.Assign> initializer,
  String name)
{}
