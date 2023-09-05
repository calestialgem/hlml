package hlml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Holds the entrypoint. */
final class Main {
  /** Entrypoint of the compiler. */
  public static void main(String... arguments) {
    Main main = new Main();
    main.launch_tests();
  }

  /** Subject that is reported when the launcher fails. */
  private final Subject subject;

  /** Directory for the executable test parcels. */
  private final Path test_parcel_site;

  /** Parcel sites to be passed to the compiler when compiling tests. */
  private final List<Path> parcel_sites;

  /** Constructor. */
  private Main() {
    subject = Subject.of("compiler launcher");
    test_parcel_site = Path.of("tests");
    parcel_sites = List.of(test_parcel_site);
  }

  /** Tests the compiler by building the executable tests. */
  private void launch_tests() {
    try {
      Files
        .list(test_parcel_site)
        .map(Path::getFileName)
        .map(Path::toString)
        .forEach(p -> Checker.check(subject, parcel_sites, p));
    }
    catch (IOException cause) {
      throw subject
        .to_diagnostic(
          "failure",
          "Could not list the test parcel site's entries!")
        .to_exception(cause);
    }
  }
}
