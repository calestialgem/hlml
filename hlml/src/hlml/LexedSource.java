package hlml;

import java.util.List;

/** Lexical representation of a source file. */
record LexedSource(LoadedSource source, List<Token> tokens) {}
