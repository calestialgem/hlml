package hlml.resolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import hlml.Source;
import hlml.lexer.LexedSource;
import hlml.lexer.Lexer;
import hlml.loader.LoadedSource;
import hlml.loader.Loader;
import hlml.parser.Node;
import hlml.parser.ParsedSource;
import hlml.parser.Parser;
import hlml.reporter.Subject;

/** First pass of the analysis. Records down all the declarations'
 * designators. */
public final class Resolver {
  /** Resolves a source. */
  public static Resolution.Source resolve(Path file, Path artifacts) {
    Resolver resolver = new Resolver(file, artifacts);
    return resolver.resolve();
  }

  /** Path to the resolved source file. */
  private final Path file;

  /** Path to the directory where compilation artifacts can be recorded to. */
  private final Path artifacts;

  /** Constructor. */
  private Resolver(Path file, Path artifacts) {
    this.file = file;
    this.artifacts = artifacts;
  }

  /** Resolves the source. */
  private Resolution.Source resolve() {
    Source source = Source.of(file);
    LoadedSource loaded_source = Loader.load(source);
    record_representation(source.name(), "contents", loaded_source.contents());
    LexedSource lexed_source = Lexer.lex(loaded_source);
    record_representation(source.name(), "tokens", lexed_source.tokens());
    ParsedSource parsed_source = Parser.parse(lexed_source);
    record_representation(
      source.name(),
      "declarations",
      parsed_source.declarations());
    Optional<Node.Entrypoint> entrypoint = Optional.empty();
    Map<String, Node.Definition> globals = new HashMap<>();
    for (Node.Declaration node : parsed_source.declarations()) {
      switch (node) {
        case Node.Entrypoint e -> {
          if (entrypoint.isPresent()) {
            throw parsed_source
              .subject(node)
              .to_diagnostic("error", "Redeclaration of entrypoint!")
              .to_exception();
          }
          entrypoint = Optional.of(e);
        }
        case Node.Definition g -> {
          String identifier = g.identifier();
          if (globals.containsKey(identifier)) {
            throw parsed_source
              .subject(node)
              .to_diagnostic("error", "Redeclaration of `%s`!", identifier)
              .to_exception();
          }
          globals.put(identifier, g);
        }
      }
    }
    return new Resolution.Source(parsed_source, entrypoint, globals);
  }

  /** Records a representation of the source file. Used for debugging the
   * compiler. */
  private void record_representation(
    String source_name,
    String representation_name,
    Object representation)
  {
    Path representation_path =
      artifacts
        .resolve(
          "%s.%s%s"
            .formatted(source_name, representation_name, Source.extension));
    try {
      Files.writeString(representation_path, representation.toString());
    }
    catch (IOException cause) {
      throw Subject
        .of(representation_path)
        .to_diagnostic(
          "failure",
          "Could not record the %s of source `%s`",
          representation_name,
          source_name)
        .to_exception(cause);
    }
  }
}
