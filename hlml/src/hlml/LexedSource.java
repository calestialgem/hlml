package hlml;

import java.util.List;

/** Lexical representation of a source file. */
record LexedSource(Source source, List<Token> tokens) {}
