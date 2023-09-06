package hlml.reporter;

/** Reports about the compilation process. */
public record Diagnostic(String message) {
  /** Returns an exception with this diagnostic's message. */
  public RuntimeException to_exception() {
    return new RuntimeException(message);
  }

  /** Returns an exception with this diagnostic's message and the given
   * cause. */
  public RuntimeException to_exception(Throwable cause) {
    return new RuntimeException(message, cause);
  }
}
