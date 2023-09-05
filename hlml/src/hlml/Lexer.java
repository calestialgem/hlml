package hlml;

import java.util.ArrayList;
import java.util.List;

/** Transforms a source file to a list of tokens. */
final class Lexer {
  /** Lexes a source file.. */
  static LexedSource lex(LoadedSource source) {
    Lexer lexer = new Lexer(source);
    return lexer.lex();
  }

  /** Source file that is lexed. */
  private final LoadedSource source;

  /** Contents of the lexed source file. */
  private String contents;

  /** Tokens that were lexed. */
  private List<Token> tokens;

  /** Index of the currently lexed character. */
  private int current;

  /** Constructor. */
  private Lexer(LoadedSource source) {
    this.source = source;
  }

  /** Lexes the source file. */
  private LexedSource lex() {
    contents = source.contents();
    tokens = new ArrayList<Token>();
    current = 0;
    while (has_current()) {
      int start = current;
      int initial = get_current();
      advance();
      switch (initial) {
        case ' ', '\t', '\r', '\n' -> {}
        case '#' -> {
          while (has_current()) {
            int character = get_current();
            advance();
            if (character == '\n') { break; }
          }
        }
        case '{' -> tokens.add(new Token.OpeningBrace(start));
        case '}' -> tokens.add(new Token.ClosingBrace(start));
        default -> {
          if (initial >= 'a' && initial <= 'z') {
            while (has_current()) {
              int character = get_current();
              boolean is_word_part =
                character >= 'a' && character <= 'z'
                  || character >= 'A' && character <= 'Z'
                  || character >= '0' && character <= '9'
                  || character == '_';
              if (!is_word_part) { break; }
              advance();
            }
            String text = contents.substring(start, current);
            Token token;
            switch (text) {
              case "entrypoint" -> { token = new Token.Entrypoint(start); }
              default -> { token = new Token.LowercaseIdentifier(start, text); }
            }
            tokens.add(token);
            break;
          }
          throw source
            .subject(start, current)
            .to_diagnostic("error", "Unknown character `%c`!", initial)
            .to_exception();
        }
      }
    }
    return new LexedSource(source, tokens);
  }

  /** Skips over the currently lexed character. */
  private void advance() {
    current = contents.offsetByCodePoints(current, 1);
  }

  /** Returns the currently lexed character. */
  private int get_current() {
    return contents.codePointAt(current);
  }

  /** Returns whether there is a character at the current index. */
  private boolean has_current() {
    return current != contents.length();
  }
}
