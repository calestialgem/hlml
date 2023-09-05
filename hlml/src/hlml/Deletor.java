package hlml;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/** File visitor that deletes. */
public class Deletor implements FileVisitor<Path> {
  @Override
  public FileVisitResult preVisitDirectory(
    Path directory,
    BasicFileAttributes attributes)
    throws IOException
  {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
    throws IOException
  {
    Files.delete(file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException cause)
    throws IOException
  {
    throw cause;
  }

  @Override
  public FileVisitResult postVisitDirectory(
    Path directory,
    IOException entry_exception)
    throws IOException
  {
    if (entry_exception != null) { throw entry_exception; }
    Files.delete(directory);
    return FileVisitResult.CONTINUE;
  }
}
