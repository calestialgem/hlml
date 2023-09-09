package hlml.checker;

import hlml.reporter.Subject;

/** Function that finds a global with the given name. */
@FunctionalInterface
interface GlobalFinder {
  /** Returns the global with the given name if it exists. Otherwise reports a
   * diagnostic with to the given subject. */
  Semantic.Definition find(Subject subject, Name name);
}
