package hlml.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
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
        case '^' -> lex_extensible(Token.Caret::new, Token.CaretEqual::new);
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
        case '&' ->
          lex_repeatable_or_extensible(
            Token.Ampersand::new,
            Token.AmpersandAmpersand::new,
            Token.AmpersandEqual::new);
        case '|' ->
          lex_repeatable_or_extensible(
            Token.Pipe::new,
            Token.PipePipe::new,
            Token.PipeEqual::new);
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
        case '"' -> {
          StringBuilder builder = new StringBuilder();
          while (true) {
            int character;
            if (!has_current() || (character = get_current()) == '\n') {
              throw source
                .subject(start, current)
                .to_diagnostic("error", "Incomplete string constant!")
                .to_exception();
            }
            advance();
            if (character == '"') { break; }
            builder.appendCodePoint(character);
          }
          String value = builder.toString();
          tokens.add(new Token.StringConstant(start, current, value));
        }
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
              case "public" -> { token = new Token.Public(start); }
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
          if (initial == '0'
            && has_current()
            && (get_current() == 'p' || get_current() == 'P'))
          {
            NumberBase base = NumberBase.of(16);
            NumberBuilder builder = NumberBuilder.create(base);
            advance();
            try {
              int digit_count = 1;
              builder.insert(enforce_digit(base));
              while (has_current()) {
                if (get_current() == '_') {
                  advance();
                  builder.insert(enforce_digit(base));
                }
                else {
                  OptionalInt maybe_digit = lex_digit(base);
                  if (maybe_digit.isEmpty()) { break; }
                  builder.insert(maybe_digit.getAsInt());
                }
                digit_count++;
              }
              if (digit_count != 6 && digit_count != 8) {
                throw source
                  .subject(start, current)
                  .to_diagnostic(
                    "error",
                    "Color constants must have 6 or 8 hexadecimal digits!")
                  .to_exception();
              }
              int value = builder.build_int();
              if (digit_count == 6) {
                value <<= 8;
                value |= 0xff;
              }
              tokens.add(new Token.ColorConstant(start, current, value));
            }
            catch (ArithmeticException cause) {
              throw source
                .subject(start, current)
                .to_diagnostic("error", "Could not lex the color constant!")
                .to_exception(cause);
            }
            break;
          }
          if (initial >= '0' && initial <= '9') {
            int digit = initial - '0';
            NumberBase base = NumberBase.of(10);
            if (digit == 0 && has_current()) {
              Optional<NumberBase> given_base = switch (get_current()) {
                case 'b', 'B' -> Optional.of(NumberBase.of(2));
                case 'o', 'O' -> Optional.of(NumberBase.of(8));
                case 'd', 'D' -> Optional.of(NumberBase.of(10));
                case 'x', 'X' -> Optional.of(NumberBase.of(16));
                default -> Optional.empty();
              };
              if (given_base.isPresent()) {
                base = given_base.get();
                advance();
                digit = enforce_digit(base);
              }
            }
            NumberBuilder builder = NumberBuilder.create(base);
            try {
              builder.insert(digit);
              while (has_current()) {
                if (get_current() == '_') {
                  advance();
                  builder.insert(enforce_digit(base));
                }
                else {
                  OptionalInt maybe_digit = lex_digit(base);
                  if (maybe_digit.isEmpty()) { break; }
                  builder.insert(maybe_digit.getAsInt());
                }
              }
              if (has_current() && get_current() == '.') {
                int start = current;
                advance();
                builder.fraction_separator();
                OptionalInt first_digit = lex_digit(base);
                if (first_digit.isEmpty()) {
                  current = start;
                }
                else {
                  builder.insert(first_digit.getAsInt());
                  while (has_current()) {
                    if (get_current() == '_') {
                      advance();
                      builder.insert(enforce_digit(base));
                    }
                    else {
                      OptionalInt maybe_digit = lex_digit(base);
                      if (maybe_digit.isEmpty()) { break; }
                      builder.insert(maybe_digit.getAsInt());
                    }
                  }
                }
              }
              int exponent_separator =
                base instanceof NumberBase.PowerOfTwo ? 'p' : 'e';
              if (has_current()
                && (get_current() == exponent_separator
                  || get_current() == exponent_separator + 'A' - 'a'))
              {
                advance();
                boolean is_negative = has_current() && get_current() == '-';
                if (is_negative || has_current() && get_current() == '+') {
                  advance();
                }
                builder.exponent_separator(is_negative);
                base = NumberBase.of(10);
                builder.insert(enforce_digit(base));
                while (has_current()) {
                  if (get_current() == '_') {
                    advance();
                    builder.insert(enforce_digit(base));
                  }
                  else {
                    OptionalInt maybe_digit = lex_digit(base);
                    if (maybe_digit.isEmpty()) { break; }
                    builder.insert(maybe_digit.getAsInt());
                  }
                }
              }
              double value = builder.build_double();
              tokens.add(new Token.NumberConstant(start, current, value));
            }
            catch (ArithmeticException cause) {
              throw source
                .subject(start, current)
                .to_diagnostic("error", "Could not lex the number constant!")
                .to_exception(cause);
            }
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

  /** Takes a digit or throws. */
  private int enforce_digit(NumberBase base) {
    OptionalInt digit = lex_digit(base);
    if (digit.isPresent()) { return digit.getAsInt(); }
    throw source
      .subject(start, current)
      .to_diagnostic(
        "error",
        "Expected a digit %s!",
        has_current()
          ? "instead of `%c`".formatted(get_current())
          : "at the end of the file")
      .to_exception();
  }

  /** Takes a new digit if it is of the given base. */
  private OptionalInt lex_digit(NumberBase base) {
    int start = current;
    OptionalInt digit = lex_digit();
    if (digit.isPresent() && digit.getAsInt() >= base.radix()) {
      current = start;
      return OptionalInt.empty();
    }
    return digit;
  }

  /** Takes the next character as a digit if it exists. */
  private OptionalInt lex_digit() {
    if (!has_current()) { return OptionalInt.empty(); }
    int character = get_current();
    if (character >= '0' && character <= '9') {
      advance();
      return OptionalInt.of(character - '0');
    }
    if (character >= 'a' && character <= 'f') {
      advance();
      return OptionalInt.of(character - 'a' + 10);
    }
    if (character >= 'A' && character <= 'F') {
      advance();
      return OptionalInt.of(character - 'A' + 10);
    }
    return OptionalInt.empty();
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
