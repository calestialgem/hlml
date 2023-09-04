package hlml;

/** Longest sequence of characters that make up a meaningful word in hlml. */
sealed interface Token {
  /** Keyword `entrypoint`. */
  record Entrypoint(int start) implements Token {
    @Override
    public int end() { return start + "entrypoint".length(); }
  }

  /** Punctuation `{`. */
  record OpeningBrace(int start) implements Token {
    @Override
    public int end() { return start + "{".length(); }
  }

  /** Punctuation `}`. */
  record ClosingBrace(int start) implements Token {
    @Override
    public int end() { return start + "}".length(); }
  }

  /** Any word that refers to a user-defined construct. */
  sealed interface Identifier extends Token {
    /** Characters that make up the identifier. */
    String text();

    @Override
    default int end() { return start() + text().length(); }
  }

  /** Identifiers the begin with a lowercase letter. Used for identifying
   * variables and procedures. */
  record LowercaseIdentifier(int start, String text) implements Identifier {}

  /** Index of the token's first character's first byte from the beginning of
   * the file. Used for reporting diagnostics with a source location. */
  int start();

  /** Index of the first byte of the character after the token's last one from
   * the beginning of the file. Used for reporting diagnostics with a source
   * location. */
  int end();
}
