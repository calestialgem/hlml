package hlml.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import hlml.loader.LoadedSource;

/** Transforms a source file to a list of tokens. */
public final class Lexer {
  /** Lexes a source file.. */
  public static LexedSource lex(LoadedSource source) {
    Lexer lexer = new Lexer(source);
    return lexer.lex();
  }

  /** Source file that is lexed. */
  private final LoadedSource source;

  /** Tokens that were lexed. */
  private List<Token> tokens;

  /** Index of the currently lexed character. */
  private int current;

  /** Currently lexed token's first character. */
  private int initial;

  /** Currently lexed token's first character's first byte's index. */
  private int start;

  /** Constructor. */
  private Lexer(LoadedSource source) {
    this.source = source;
  }

  /** Lexes the source file. */
  private LexedSource lex() {
    tokens = new ArrayList<Token>();
    current = 0;
    while (has_current()) {
      initial = get_current();
      start = current;
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
        case '{' -> lex_single(Token.OpeningBrace::new);
        case '}' -> lex_single(Token.ClosingBrace::new);
        case '(' -> lex_single(Token.OpeningParenthesis::new);
        case ')' -> lex_single(Token.ClosingParenthesis::new);
        case ';' -> lex_single(Token.Semicolon::new);
        case '.' -> lex_single(Token.Dot::new);
        case ',' -> lex_single(Token.Comma::new);
        case '~' -> lex_single(Token.Tilde::new);
        case ':' -> lex_repeatable(Token.Colon::new, Token.ColonColon::new);
        case '*' -> lex_extensible(Token.Star::new, Token.StarEqual::new);
        case '%' -> lex_extensible(Token.Percent::new, Token.PercentEqual::new);
        case '&' ->
          lex_extensible(Token.Ampersand::new, Token.AmpersandEqual::new);
        case '^' -> lex_extensible(Token.Caret::new, Token.CaretEqual::new);
        case '|' -> lex_extensible(Token.Pipe::new, Token.PipeEqual::new);
        case '!' ->
          lex_extensible(Token.Exclamation::new, Token.ExclamationEqual::new);
        case '+' ->
          lex_repeatable_or_extensible(
            Token.Plus::new,
            Token.PlusPlus::new,
            Token.PlusEqual::new);
        case '-' ->
          lex_repeatable_or_extensible(
            Token.Minus::new,
            Token.MinusMinus::new,
            Token.MinusEqual::new);
        case '/' ->
          lex_repeatable_and_extensible(
            Token.Slash::new,
            Token.SlashSlash::new,
            Token.SlashEqual::new,
            Token.SlashSlashEqual::new);
        case '<' ->
          lex_repeatable_and_extensible(
            Token.Left::new,
            Token.LeftLeft::new,
            Token.LeftEqual::new,
            Token.LeftLeftEqual::new);
        case '>' ->
          lex_repeatable_and_extensible(
            Token.Right::new,
            Token.RightRight::new,
            Token.RightEqual::new,
            Token.RightRightEqual::new);
        case '=' ->
          lex_repeatable_and_extensible(
            Token.Equal::new,
            Token.EqualEqual::new,
            Token.EqualEqual::new,
            Token.EqualEqualEqual::new);
        default -> {
          if (initial >= 'a' && initial <= 'z'
            || initial >= 'A' && initial <= 'Z')
          {
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
            String text = source.contents.substring(start, current);
            Token token;
            switch (text) {
              case "entrypoint" -> { token = new Token.Entrypoint(start); }
              case "link" -> { token = new Token.Link(start); }
              case "using" -> { token = new Token.Using(start); }
              case "as" -> { token = new Token.As(start); }
              case "proc" -> { token = new Token.Proc(start); }
              case "const" -> { token = new Token.Const(start); }
              case "var" -> { token = new Token.Var(start); }
              case "if" -> { token = new Token.If(start); }
              case "else" -> { token = new Token.Else(start); }
              case "while" -> { token = new Token.While(start); }
              case "break" -> { token = new Token.Break(start); }
              case "continue" -> { token = new Token.Continue(start); }
              case "return" -> { token = new Token.Return(start); }
              default -> { token = new Token.Identifier(start, text); }
            }
            tokens.add(token);
            break;
          }
          if (initial >= '0' && initial <= '9') {
            Natural128 value = Natural128.zero;
            value =
              value
                .add(initial - '0')
                .orElseThrow(
                  () -> source
                    .subject(start, current)
                    .to_diagnostic("error", "Huge number!")
                    .to_exception());
            while (has_current()) {
              int character = get_current();
              if (character == '_') { continue; }
              boolean is_digit = character >= '0' && character <= '9';
              if (!is_digit) { break; }
              advance();
              value =
                value
                  .multiply(10)
                  .orElseThrow(
                    () -> source
                      .subject(start, current)
                      .to_diagnostic("error", "Huge number!")
                      .to_exception());
              value =
                value
                  .add(character - '0')
                  .orElseThrow(
                    () -> source
                      .subject(start, current)
                      .to_diagnostic("error", "Huge number!")
                      .to_exception());
            }
            Token.NumberConstant number =
              new Token.NumberConstant(
                start,
                current,
                value
                  .to_double()
                  .orElseThrow(
                    () -> source
                      .subject(start, current)
                      .to_diagnostic(
                        "error",
                        "Number is not representable as a binary64 floating point!")
                      .to_exception()));
            tokens.add(number);
            break;
          }
          throw source
            .subject(start)
            .to_diagnostic("error", "Unknown character `%c`!", initial)
            .to_exception();
        }
      }
    }
    return new LexedSource(source, tokens);
  }

  /** Lexes a single punctuation. */
  private void lex_single(IntFunction<Token> lexer_function) {
    Token token = lexer_function.apply(start);
    tokens.add(token);
  }

  /** Lexes a single or repeated punctuation. */
  private void lex_repeatable(
    IntFunction<Token> lexer_function,
    IntFunction<Token> repeated_lexer_function)
  {
    if (has_current() && get_current() == initial) {
      advance();
      lex_single(repeated_lexer_function);
      return;
    }
    lex_single(lexer_function);
  }

  /** Lexes a single or extended punctuation. */
  private void lex_extensible(
    IntFunction<Token> lexer_function,
    IntFunction<Token> extended_lexer_function)
  {
    if (has_current() && get_current() == '=') {
      advance();
      lex_single(extended_lexer_function);
      return;
    }
    lex_single(lexer_function);
  }

  /** Lexes a single, repeated, or extended punctuation. */
  private void lex_repeatable_or_extensible(
    IntFunction<Token> lexer_function,
    IntFunction<Token> repeated_lexer_function,
    IntFunction<Token> extended_lexer_function)
  {
    if (has_current() && get_current() == initial) {
      advance();
      lex_single(repeated_lexer_function);
      return;
    }
    if (has_current() && get_current() == '=') {
      advance();
      lex_single(extended_lexer_function);
      return;
    }
    lex_single(lexer_function);
  }

  /** Lexes a single, repeated, extended, or repeated and extended
   * punctuation. */
  private void lex_repeatable_and_extensible(
    IntFunction<Token> lexer_function,
    IntFunction<Token> repeated_lexer_function,
    IntFunction<Token> extended_lexer_function,
    IntFunction<Token> repeated_extended_lexer_function)
  {
    if (has_current() && get_current() == initial) {
      advance();
      if (has_current() && get_current() == '=') {
        advance();
        lex_single(repeated_extended_lexer_function);
        return;
      }
      lex_single(repeated_lexer_function);
      return;
    }
    if (has_current() && get_current() == '=') {
      advance();
      lex_single(extended_lexer_function);
      return;
    }
    lex_single(lexer_function);
  }

  /** Skips over the currently lexed character. */
  private void advance() {
    current = source.contents.offsetByCodePoints(current, 1);
  }

  /** Returns the currently lexed character. */
  private int get_current() {
    return source.contents.codePointAt(current);
  }

  /** Returns whether there is a character at the current index. */
  private boolean has_current() {
    return current != source.contents.length();
  }
}
