package hlml.checker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/** Holds some helper functions for sets. */
final class Sets {
  /** Returns the union of the given sets. */
  @SafeVarargs
  static <T> Set<T> union(Set<? extends T>... sets) {
    return union(Stream.of(sets));
  }

  /** Returns the union of the given sets. */
  static <T> Set<T> union(Stream<Set<? extends T>> sets) {
    return union(sets.iterator());
  }

  /** Returns the union of the given sets. */
  static <T> Set<T> union(Iterable<Set<? extends T>> sets) {
    return union(sets.iterator());
  }

  /** Returns the union of the given sets. */
  static <T> Set<T> union(Iterator<Set<? extends T>> set_iterator) {
    Set<T> union = new HashSet<>();
    while (set_iterator.hasNext()) { union.addAll(set_iterator.next()); }
    return union;
  }
}
