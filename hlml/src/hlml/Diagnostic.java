package hlml;

/** Reports about the compilation process. */
record Diagnostic(String message) {
  /** Returns an exception with this diagnostic's message. */
  RuntimeException to_exception() {
    return new RuntimeException(message);
  }

  /** Returns an exception with this diagnostic's message and the given
   * cause. */
  RuntimeException to_exception(Throwable cause) {
    return new RuntimeException(message, cause);
  }
}
