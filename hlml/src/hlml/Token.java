package hlml;

/** Longest sequence of characters that make up a meaningful word in hlml. */
sealed interface Token {
  /** Keyword `entrypoint`. */
  record Entrypoint(int start) implements Token {
    @Override
    public int end() { return start + "entrypoint".length(); }

    @Override
    public String explanation() { return "keyword `entrypoint`"; }
  }

  /** Punctuation `{`. */
  record OpeningBrace(int start) implements Token {
    @Override
    public int end() { return start + "{".length(); }

    @Override
    public String explanation() { return "punctuation `{`"; }
  }

  /** Punctuation `}`. */
  record ClosingBrace(int start) implements Token {
    @Override
    public int end() { return start + "}".length(); }

    @Override
    public String explanation() { return "punctuation `}`"; }
  }

  /** Punctuation `;`. */
  record Semicolon(int start) implements Token {
    @Override
    public int end() { return start + ";".length(); }

    @Override
    public String explanation() { return "punctuation `;`"; }
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
  record LowercaseIdentifier(int start, String text) implements Identifier {
    @Override
    public String explanation() {
      return "lowercase identifier `%s`".formatted(text);
    }
  }

  /** A number constant. */
  record NumberConstant(int start, int end, double value) implements Token {
    @Override
    public String explanation() { return "number `%.17f`".formatted(value); }
  }

  /** Index of the token's first character's first byte from the beginning of
   * the file. Used for reporting diagnostics with a source location. */
  int start();

  /** Index of the first byte of the character after the token's last one from
   * the beginning of the file. Used for reporting diagnostics with a source
   * location. */
  int end();

  /** Returns a text to report the token to the user. */
  String explanation();
}
