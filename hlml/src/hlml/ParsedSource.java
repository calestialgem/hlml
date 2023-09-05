package hlml;

import java.util.List;

/** Syntactical representation of a source file. */
record ParsedSource(LexedSource source, List<Node.Declaration> declarations) {}
