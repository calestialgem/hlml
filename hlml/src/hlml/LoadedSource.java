package hlml;

/** Raw representation of the source file. */
record LoadedSource(Source source, String contents) {
  /** Returns a subject as a character in this source file. */
  Subject subject(int index) {
    return source.subject(index);
  }
}
