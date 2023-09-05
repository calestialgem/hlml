package hlml;

import java.io.IOException;
import java.nio.file.Files;

/** Reads a source file and stores its contents in memory. */
final class Loader {
  /** Loads a source. */
  static LoadedSource load(Source source) {
    Loader loader = new Loader(source);
    return loader.load();
  }

  /** Source that is loaded. */
  private final Source source;

  /** Constructor. */
  private Loader(Source source) { this.source = source; }

  /** Loads the source. */
  private LoadedSource load() {
    String contents;
    try {
      contents = Files.readString(source.path());
    }
    catch (IOException cause) {
      throw source
        .subject()
        .to_diagnostic("failure", "Could not read the source file!")
        .to_exception(cause);
    }
    return new LoadedSource(source, contents);
  }
}
