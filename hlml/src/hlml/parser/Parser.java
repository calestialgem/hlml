package hlml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import hlml.lexer.LexedSource;
import hlml.lexer.Token;

/** Transforms tokens to a list of declarations. */
public final class Parser {
  /** Parses a source file. Returns the declarations in the file. */
  public static ParsedSource parse(LexedSource source) {
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
    return first_of(this::parse_entrypoint, this::parse_definition);
  }

  /** Parses a entrypoint. */
  private Optional<Node.Entrypoint> parse_entrypoint() {
    if (parse_token(Token.Entrypoint.class).isEmpty()) {
      return Optional.empty();
    }
    Node.Statement body =
      expect(this::parse_block, "body of the entrypoint declaration");
    Node.Entrypoint entrypoint = new Node.Entrypoint(body);
    return Optional.of(entrypoint);
  }

  /** Parses a definition. */
  private Optional<Node.Definition> parse_definition() {
    return first_of(this::parse_var);
  }

  /** Parses a var. */
  private Optional<Node.Var> parse_var() {
    if (parse_token(Token.Var.class).isEmpty()) { return Optional.empty(); }
    Token.LowercaseIdentifier identifier =
      expect_token(
        Token.LowercaseIdentifier.class,
        "identifier of the variable declaration");
    Optional<Node.Expression> initial_value = Optional.empty();
    if (parse_token(Token.Equal.class).isPresent()) {
      Node.Expression given_initial_value =
        expect(
          this::parse_expression,
          "initial value of the variable declaration");
      initial_value = Optional.of(given_initial_value);
    }
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the variable declaration");
    Node.Var var = new Node.Var(identifier, initial_value);
    return Optional.of(var);
  }

  /** Parses a statement. */
  private Optional<Node.Statement> parse_statement() {
    return first_of(
      this::parse_block,
      this::parse_var,
      this::parse_expression_based);
  }

  /** Parses a block. */
  private Optional<Node.Block> parse_block() {
    int first = current;
    if (parse_token(Token.OpeningBrace.class).isEmpty()) {
      return Optional.empty();
    }
    List<Node.Statement> body = repeats_of(this::parse_statement);
    expect_token(
      Token.ClosingBrace.class,
      "inner statement list closer `}` of the block statement");
    Node.Block block = new Node.Block(first, body);
    return Optional.of(block);
  }

  /** Parses an expression based statement. */
  private Optional<Node.ExpressionBased> parse_expression_based() {
    Optional<Node.Expression> expression = parse_expression();
    if (expression.isEmpty()) { return Optional.empty(); }
    Node.ExpressionBased result = new Node.Discard(expression.get());
    if (expression.get() instanceof Node.VariableAccess variable
      && parse_token(Token.Equal.class).isPresent())
    {
      Node.Expression new_value =
        expect(this::parse_expression, "new value of the assignment statement");
      result = new Node.Assignment(variable, new_value);
    }
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the %s statement".formatted(switch (result)
      {
        case Node.Assignment a -> "assignment";
        case Node.Discard d -> "discard";
      }));
    return Optional.of(result);
  }

  /** Parses an expression. */
  private Optional<Node.Expression> parse_expression() {
    return first_of(this::parse_precedence_9);
  }

  /** Parses an expression at precedence level 9. */
  private Optional<Node.Precedence9> parse_precedence_9() {
    return parse_binary_operations(
      this::parse_precedence_8,
      new BinaryOperationParser<>(
        Token.EqualEqual.class,
        Node.EqualTo::new,
        "equal to"),
      new BinaryOperationParser<>(
        Token.ExclamationEqual.class,
        Node.NotEqualTo::new,
        "not equal to"),
      new BinaryOperationParser<>(
        Token.EqualEqualEqual.class,
        Node.StrictlyEqualTo::new,
        "strictly equal to"));
  }

  /** Parses an expression at precedence level 8. */
  private Optional<Node.Precedence8> parse_precedence_8() {
    return parse_binary_operations(
      this::parse_precedence_7,
      new BinaryOperationParser<>(
        Token.Left.class,
        Node.LessThan::new,
        "less than"),
      new BinaryOperationParser<>(
        Token.LeftEqual.class,
        Node.LessThanOrEqualTo::new,
        "less than or equal to"),
      new BinaryOperationParser<>(
        Token.Right.class,
        Node.GreaterThan::new,
        "greater than"),
      new BinaryOperationParser<>(
        Token.RightEqual.class,
        Node.GreaterThanOrEqualTo::new,
        "greater than or equal to"));
  }

  /** Parses an expression at precedence level 7. */
  private Optional<Node.Precedence7> parse_precedence_7() {
    return parse_binary_operations(
      this::parse_precedence_6,
      new BinaryOperationParser<>(
        Token.Pipe.class,
        Node.BitwiseOr::new,
        "bitwise or"));
  }

  /** Parses an expression at precedence level 6. */
  private Optional<Node.Precedence6> parse_precedence_6() {
    return parse_binary_operations(
      this::parse_precedence_5,
      new BinaryOperationParser<>(
        Token.Caret.class,
        Node.BitwiseXor::new,
        "bitwise xor"));
  }

  /** Parses an expression at precedence level 5. */
  private Optional<Node.Precedence5> parse_precedence_5() {
    return parse_binary_operations(
      this::parse_precedence_4,
      new BinaryOperationParser<>(
        Token.Ampersand.class,
        Node.BitwiseAnd::new,
        "bitwise and"));
  }

  /** Parses an expression at precedence level 4. */
  private Optional<Node.Precedence4> parse_precedence_4() {
    return parse_binary_operations(
      this::parse_precedence_3,
      new BinaryOperationParser<>(
        Token.LeftLeft.class,
        Node.LeftShift::new,
        "left shift"),
      new BinaryOperationParser<>(
        Token.RightRight.class,
        Node.RightShift::new,
        "right shift"));
  }

  /** Parses an expression at precedence level 3. */
  private Optional<Node.Precedence3> parse_precedence_3() {
    return parse_binary_operations(
      this::parse_precedence_2,
      new BinaryOperationParser<>(
        Token.Plus.class,
        Node.Addition::new,
        "addition"),
      new BinaryOperationParser<>(
        Token.Minus.class,
        Node.Subtraction::new,
        "subtraction"));
  }

  /** Parses an expression at precedence level 2. */
  private Optional<Node.Precedence2> parse_precedence_2() {
    return parse_binary_operations(
      this::parse_precedence_1,
      new BinaryOperationParser<>(
        Token.Star.class,
        Node.Multiplication::new,
        "multiplication"),
      new BinaryOperationParser<>(
        Token.Slash.class,
        Node.Division::new,
        "division"),
      new BinaryOperationParser<>(
        Token.SlashSlash.class,
        Node.IntegerDivision::new,
        "integer division"),
      new BinaryOperationParser<>(
        Token.Percent.class,
        Node.Modulus::new,
        "modulus"));
  }

  /** Parses a group of binary operators in the same precedence level from left
   * to right. */
  @SafeVarargs
  private <PrecedenceType extends Node.Expression, OperandType extends PrecedenceType> Optional<PrecedenceType> parse_binary_operations(
    Supplier<Optional<OperandType>> operand_parser_function,
    BinaryOperationParser<PrecedenceType>... binary_operation_parsers)
  {
    Optional<OperandType> first_operand = operand_parser_function.get();
    if (first_operand.isEmpty()) { return Optional.empty(); }
    PrecedenceType result = first_operand.get();
    while (true) {
      for (BinaryOperationParser<PrecedenceType> binary_operation_parser : binary_operation_parsers) {
        if (parse_token(binary_operation_parser.operator_class()).isEmpty()) {
          continue;
        }
        OperandType right_operand =
          expect(
            operand_parser_function,
            "right operand of %s expression"
              .formatted(binary_operation_parser.name()));
        result =
          binary_operation_parser.initializer().apply(result, right_operand);
      }
      break;
    }
    return Optional.of(result);
  }

  /** Parses an expression at precedence level 1. */
  private Optional<Node.Precedence1> parse_precedence_1() {
    return parse_unary_operations(
      this::parse_precedence_0,
      new UnaryOperationParser<>(
        Token.Plus.class,
        Node.Promotion::new,
        "promotion"),
      new UnaryOperationParser<>(
        Token.Minus.class,
        Node.Negation::new,
        "negation"),
      new UnaryOperationParser<>(
        Token.Tilde.class,
        Node.BitwiseNot::new,
        "bitwise not"),
      new UnaryOperationParser<>(
        Token.Exclamation.class,
        Node.LogicalNot::new,
        "logical not"));
  }

  /** Parses a group of unary operators in the same precedence level from right
   * to left. */
  @SafeVarargs
  private <PrecedenceType extends Node.Expression, OperandType extends PrecedenceType> Optional<PrecedenceType> parse_unary_operations(
    Supplier<Optional<OperandType>> operand_parser_function,
    UnaryOperationParser<PrecedenceType>... unary_operation_parsers)
  {
    List<UnaryOperationParser<PrecedenceType>> stack = new ArrayList<>();
    while (true) {
      for (UnaryOperationParser<PrecedenceType> unary_operation_parser : unary_operation_parsers) {
        if (parse_token(unary_operation_parser.operator_class()).isEmpty()) {
          continue;
        }
        stack.add(unary_operation_parser);
      }
      break;
    }
    if (stack.isEmpty()) {
      return operand_parser_function.get().map(Function.identity());
    }
    PrecedenceType result =
      expect(
        operand_parser_function,
        "operand of %s expression"
          .formatted(stack.get(stack.size() - 1).name()));
    for (int i = stack.size(); i != 0; i--) {
      result = stack.get(i - 1).initializer().apply(result);
    }
    return Optional.of(result);
  }

  /** Parses an expression at precedence level 0. */
  private Optional<Node.Precedence0> parse_precedence_0() {
    return first_of(this::parse_number_constant, this::parse_variable_access);
  }

  /** Parses a number constant. */
  private Optional<Node.NumberConstant> parse_number_constant() {
    int first = current;
    Optional<Token.NumberConstant> token =
      parse_token(Token.NumberConstant.class);
    if (token.isEmpty()) { return Optional.empty(); }
    Node.NumberConstant number_constant =
      new Node.NumberConstant(first, token.get().value());
    return Optional.of(number_constant);
  }

  /** Parses a variable access. */
  private Optional<Node.VariableAccess> parse_variable_access() {
    int first = current;
    Optional<Token.LowercaseIdentifier> token =
      parse_token(Token.LowercaseIdentifier.class);
    if (token.isEmpty()) { return Optional.empty(); }
    Node.VariableAccess variable_access =
      new Node.VariableAccess(first, token.get().text());
    return Optional.of(variable_access);
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
  private <TokenType extends Token> TokenType expect_token(
    Class<TokenType> token_class,
    String token_explanation)
  {
    return expect(() -> parse_token(token_class), token_explanation);
  }

  /** Parses the next token if it exists and it is of the given class. */
  @SuppressWarnings("unchecked")
  private <TokenType extends Token> Optional<TokenType> parse_token(
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
