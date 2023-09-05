package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** First pass of the analysis. Records down all the declarations'
 * designators. */
final class Resolver {
  /** Resolves a source. */
  static Resolution.Source resolve(Path file, Path artifacts) {
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
    Map<String, Resolution.Declaration> declarations = new HashMap<>();
    for (Node.Declaration node : parsed_source.declarations()) {
      Resolution.Declaration declaration = resolve_declaration(node);
      if (declarations.containsKey(declaration.designator())) {
        throw parsed_source
          .subject(node)
          .to_diagnostic(
            "error",
            "Redeclaration of `%s`!",
            declaration.designator())
          .to_exception();
      }
      declarations.put(declaration.designator(), declaration);
    }
    return new Resolution.Source(
      source.path(),
      loaded_source.contents(),
      lexed_source.tokens(),
      declarations);
  }

  /** Resolves a declaration in a source file in the parcel. */
  private Resolution.Declaration resolve_declaration(Node.Declaration node) {
    return switch (node) {
      case Node.Entrypoint entrypoint -> new Resolution.Entrypoint(entrypoint);
    };
  }

  /** Records a representation of a source file in the parcel. Used for
   * debugging the compiler. */
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
