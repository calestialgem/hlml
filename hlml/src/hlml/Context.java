package hlml;

import java.util.Map;
import java.util.Optional;

/** Collection of global and local symbols in a program. */
sealed interface Context {
  /** Context of the whole target. */
  final class Target implements Context {
    /** Contexts of the sources included to the target. */
    private Map<String, Source> sources;

    /** Entrypoint declaration if there is one in the target. */
    private Optional<Semantic.Entrypoint> entrypoint;
  }

  /** Context in the top-level scope of a source file. */
  final class Source implements Context {
    /** Declarations in the source. */
    private Map<String, Semantic.Declaration> declarations;
  }
}
