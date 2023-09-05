package hlml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** Transforms tokens to a list of declarations. */
final class Parser {
  /** Parses a source file. Returns the declarations in the file. */
  static ParsedSource parse(LexedSource source) {
    Parser parser = new Parser(source);
    return parser.parse();
  }

  /** Source file that is parsed. */
  private LexedSource source;

  /** Index of the currently parsed token. */
  private int current;

  /** Constructor. */
  private Parser(LexedSource source) {
    this.source = source;
  }

  /** Parses the source file. */
  private ParsedSource parse() {
    current = 0;
    List<Node.Declaration> declarations = new ArrayList<Node.Declaration>();
    while (current != source.tokens().size()) {
      Node.Declaration declaration =
        expect(this::parse_declaration, "top level declaration");
      declarations.add(declaration);
    }
    return new ParsedSource(source, declarations);
  }

  /** Parses a declaration. */
  private Optional<Node.Declaration> parse_declaration() {
    return first_of(this::parse_entrypoint);
  }

  /** Parses a entrypoint. */
  private Optional<Node.Entrypoint> parse_entrypoint() {
    if (parse(Token.Entrypoint.class).isEmpty()) { return Optional.empty(); }
    Node.Statement body =
      expect(this::parse_block, "body of the entrypoint declaration");
    Node.Entrypoint entrypoint = new Node.Entrypoint(body);
    return Optional.of(entrypoint);
  }

  /** Parses a statement. */
  private Optional<Node.Statement> parse_statement() {
    return first_of(this::parse_block, this::parse_discard);
  }

  /** Parses a block. */
  private Optional<Node.Block> parse_block() {
    int first = current;
    if (parse(Token.OpeningBrace.class).isEmpty()) { return Optional.empty(); }
    List<Node.Statement> body = repeats_of(this::parse_statement);
    expect(
      Token.ClosingBrace.class,
      "inner statement list closer `}` of the block statement");
    Node.Block block = new Node.Block(first, body);
    return Optional.of(block);
  }

  /** Parses a discard. */
  private Optional<Node.Discard> parse_discard() {
    Optional<Node.Expression> discarded = parse_expression();
    if (discarded.isEmpty())
      return Optional.empty();
    expect(Token.Semicolon.class, "terminator `;` of the discard statement");
    Node.Discard discard = new Node.Discard(discarded.get());
    return Optional.of(discard);
  }

  /** Parses an expression. */
  private Optional<Node.Expression> parse_expression() {
    return first_of(this::parse_number_constant);
  }

  /** Parses a number constant. */
  private Optional<Node.NumberConstant> parse_number_constant() {
    int first = current;
    Optional<Token.NumberConstant> token = parse(Token.NumberConstant.class);
    if (token.isEmpty())
      return Optional.empty();
    Node.NumberConstant number_constant =
      new Node.NumberConstant(first, token.get().value());
    return Optional.of(number_constant);
  }

  /** Runs the given parser repeatedly and collects the parsed constructs as a
   * list. */
  private <ConstructType> List<ConstructType> repeats_of(
    Supplier<Optional<ConstructType>> parser_function)
  {
    List<ConstructType> constructs = new ArrayList<>();
    while (true) {
      Optional<ConstructType> construct = parser_function.get();
      if (construct.isEmpty()) { break; }
      constructs.add(construct.get());
    }
    return constructs;
  }

  /** Returns the first construct that successfully parsed out of the given
   * parsers. If all fail fails. */
  @SafeVarargs
  private <ConstructType> Optional<ConstructType> first_of(
    Supplier<Optional<? extends ConstructType>>... parser_functions)
  {
    for (Supplier<Optional<? extends ConstructType>> parser_function : parser_functions) {
      Optional<? extends ConstructType> construct = parser_function.get();
      if (construct.isPresent()) { return Optional.of(construct.get()); }
    }
    return Optional.empty();
  }

  /** Ensures that the given token parses. Otherwise throws a diagnostic with
   * the given explanation. Returns the parsed token. */
  private <TokenType extends Token> TokenType expect(
    Class<TokenType> token_class,
    String token_explanation)
  {
    return expect(() -> parse(token_class), token_explanation);
  }

  /** Parses the next token if it exists and it is of the given class. */
  @SuppressWarnings("unchecked")
  private <TokenType extends Token> Optional<TokenType> parse(
    Class<TokenType> token_class)
  {
    if (current == source.tokens().size()) { return Optional.empty(); }
    Token token = source.tokens().get(current);
    if (!token_class.isInstance(token)) { return Optional.empty(); }
    current++;
    return Optional.of((TokenType) token);
  }

  /** Ensures that the given parser parses. Otherwise throws a diagnostic with
   * the given explanation. Returns the parsed construct. */
  private <ConstructType> ConstructType expect(
    Supplier<Optional<ConstructType>> parse_function,
    String construct_explanation)
  {
    Optional<ConstructType> construct = parse_function.get();
    if (construct.isPresent()) { return construct.get(); }
    if (current == source.tokens().size()) {
      Token reported_token = source.tokens().get(current - 1);
      throw source
        .subject(reported_token)
        .to_diagnostic(
          "error",
          "Expected %s at the end of the file after %s!",
          construct_explanation,
          reported_token.explanation())
        .to_exception();
    }
    Token reported_token = source.tokens().get(current);
    throw source
      .subject(reported_token)
      .to_diagnostic(
        "error",
        "Expected %s instead of %s!",
        construct_explanation,
        reported_token.explanation())
      .to_exception();
  }
}
