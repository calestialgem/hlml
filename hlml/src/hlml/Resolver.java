package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/** Combines the source files in a parcel together and resolves the
 * designators. */
final class Resolver {
  /** Resolves a parcel. */
  static Resolution.Parcel resolve(Path directory) {
    Resolver resolver = new Resolver(directory);
    return resolver.resolve();
  }

  /** Path to the resolved parcel's directory. */
  private final Path directory;

  /** Resolved parcel's artifact directory. */
  private Path artifact_directory;

  /** Resolved sources. */
  private Map<String, Resolution.Source> sources;

  /** Constructor. */
  private Resolver(Path directory) {
    this.directory = directory;
  }

  /** Resolves the parcel. */
  private Resolution.Parcel resolve() {
    sources = new HashMap<>();
    Path source_directory = directory.resolve("src");
    try (Stream<Path> list = Files.list(source_directory)) {
      artifact_directory = directory.resolve("artifacts");
      if (Files.exists(artifact_directory)) {
        try {
          Files.walkFileTree(artifact_directory, new Deletor());
        }
        catch (IOException cause) {
          throw Subject
            .of(artifact_directory)
            .to_diagnostic(
              "failure",
              "Could not delete the existing artifact directory!")
            .to_exception(cause);
        }
      }
      try {
        Files.createDirectory(artifact_directory);
      }
      catch (IOException cause) {
        throw Subject
          .of(artifact_directory)
          .to_diagnostic("failure", "Could not create the artifact directory!")
          .to_exception(cause);
      }
      list.forEach(this::resolve_source);
    }
    catch (IOException cause) {
      throw Subject
        .of(directory)
        .to_diagnostic(
          "failure",
          "Could not list the parcel's source directory!")
        .to_exception(cause);
    }
    Resolution.Parcel parcel = new Resolution.Parcel(directory, sources);
    return parcel;
  }

  /** Resolves a file in the parcel. */
  private void resolve_source(Path file) {
    Source source = Source.of(file);
    if (sources.containsKey(source.name()))
      throw Subject
        .of(file)
        .to_diagnostic("error", "Reinclusion of `%s`!", source.name())
        .to_exception();
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
    Resolution.Source resolved_source =
      new Resolution.Source(
        source.path(),
        loaded_source.contents(),
        lexed_source.tokens(),
        declarations);
    sources.put(source.name(), resolved_source);
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
      artifact_directory
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
