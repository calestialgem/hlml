package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Transforms a source file to a list of tokens. */
final class Lexer {
  /** Lexes a source file. Returns the tokens in the file. */
  static List<Token> lex(Path source_file) {
    Lexer lexer = new Lexer(source_file);
    return lexer.lex();
  }

  /** Source file that is lexed. */
  private final Path source_file;

  /** Contents of the lexed source file. */
  private String contents;

  /** Tokens that were lexed. */
  private List<Token> tokens;

  /** Index of the currently lexed character. */
  private int current;

  /** Currently lexed token's first character's first byte's index. */
  private int start;

  /** Currently lexed token's first character. */
  private int initial;

  /** Constructor. */
  private Lexer(Path source_file) {
    this.source_file = source_file;
  }

  /** Lexes the source file. Returns the tokens in the file. */
  private List<Token> lex() {
    try {
      contents = Files.readString(source_file);
    }
    catch (IOException cause) {
      throw Subject
        .of(source_file)
        .to_diagnostic("failure", "Could not read the source file!")
        .to_exception(cause);
    }
    tokens = new ArrayList<Token>();
    current = 0;
    while (has_current()) {
      start = current;
      initial = get_current();
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
          throw Subject
            .of(source_file, start)
            .to_diagnostic("error", "Unknown character `%c`!", initial)
            .to_exception();
        }
      }
    }
    return tokens;
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
