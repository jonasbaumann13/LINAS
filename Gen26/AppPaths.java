import java.io.File;
import java.nio.file.*;
import java.io.IOException;

/** Game files are located relative to the JAR or the IntelliJ project. */
public final class AppPaths {
    private static final Path ROOT = findRoot();
    private static Path temporary;
    private AppPaths() {}

    public static String asset(String relative) {
        return ROOT.resolve(relative).toString();
    }

    public static File units() { return ROOT.resolve("Units").toFile(); }

    public static synchronized String temporary(String relative) throws IOException {
        if (temporary == null) {
            temporary = Files.createTempDirectory("linas-");
            temporary.toFile().deleteOnExit();
            Path seed = ROOT.resolve("tmp");
            if (Files.isDirectory(seed)) {
                try (var paths = Files.walk(seed)) {
                    for (Path source : paths.toList()) {
                        Path target = temporary.resolve(seed.relativize(source));
                        if (Files.isDirectory(source)) Files.createDirectories(target);
                        else Files.copy(source, target);
                        target.toFile().deleteOnExit();
                    }
                }
            }
            temporary.toFile().deleteOnExit();
        }
        Path target = temporary.resolve(relative).normalize();
        if (!target.startsWith(temporary)) throw new IOException("Invalid temporary file path");
        Files.createDirectories(target.getParent());
        target.toFile().deleteOnExit();
        return target.toString();
    }

    private static Path findRoot() {
        try {
            Path code = Path.of(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(code)) return code.getParent();
            for (Path parent = code; parent != null; parent = parent.getParent()) {
                Path source = parent.resolve("Gen26");
                if (Files.isDirectory(source.resolve("Pics"))) return source;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot locate LINAS data", e);
        }
        throw new IllegalStateException("LINAS game data not found");
    }
}
