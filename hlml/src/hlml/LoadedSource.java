package hlml;

/** Raw representation of the source file. */
record LoadedSource(Source source, String contents) {
  /** Returns the source file's name. */
  String name() { return source.name(); }

  /** Returns a subject as a token in this source file. */
  Subject subject(Token token) {
    return subject(token.start(), token.end());
  }

  /** Returns a subject as a range of characters in this source file. */
  Subject subject(int start, int end) {
    return Subject.of(source.path(), contents, start, end);
  }
}
