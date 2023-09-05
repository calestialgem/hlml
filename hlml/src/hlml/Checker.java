package hlml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Semantically analyzes a parcel. */
final class Checker {
  /** Checks a parcel. */
  static Semantic.Target check(
    Subject subject,
    List<Path> parcel_sites,
    String target_parcel_name)
  {
    Checker checker = new Checker(subject, parcel_sites, target_parcel_name);
    return checker.check();
  }

  /** Subject that is reported when the target parcel is not found. */
  private final Subject subject;

  /** Ordered collection of directories to look for a parcel by its name. */
  private final List<Path> parcel_sites;

  /** Name of the checked parcel. */
  private final String target_parcel_name;

  /** Cached resolved parcels. Used for skipping the context-free stages when
   * the same parcel is mentioned multiple times. */
  private Map<String, Resolution.Parcel> resolved_parcels;

  /** Constructor. */
  private Checker(
    Subject subject,
    List<Path> parcel_sites,
    String target_parcel_name)
  {
    this.subject = subject;
    this.parcel_sites = parcel_sites;
    this.target_parcel_name = target_parcel_name;
  }

  /** Checks the parcel. */
  private Semantic.Target check() {
    resolved_parcels = new HashMap<>();
    resolve_parcel(subject, target_parcel_name);
    return null;
  }

  /** Resolve a parcel. */
  private Resolution.Parcel resolve_parcel(Subject subject, String name) {
    if (resolved_parcels.containsKey(name))
      return resolved_parcels.get(name);
    Path parcel_directory = find_parcel(subject, name);
    Resolution.Parcel parcel = Resolver.resolve(parcel_directory);
    resolved_parcels.put(name, parcel);
    return parcel;
  }

  /** Find a parcel. */
  private Path find_parcel(Subject subject, String name) {
    for (Path site : parcel_sites) {
      Path directory = site.resolve(name);
      if (Files.exists(directory))
        return directory;
    }
    throw subject
      .to_diagnostic(
        "error",
        "Could not find a parcel named `%s` in the fallowing parcel sites: `%s`!",
        name,
        parcel_sites)
      .to_exception();
  }
}
