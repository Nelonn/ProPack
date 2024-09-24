package me.nelonn.propack.builder;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarBinarySource {

    public static void extractFile(@NotNull Path source, @NotNull String input, @NotNull Path output) throws FileNotFoundException {
        input = input.replaceAll("\\\\", "/");
        if (input.endsWith("/")) {
            throw new IllegalArgumentException("Directories cannot be extracted as file");
        }
        try (ZipFile zf = new ZipFile(source.toFile())) {
            ZipEntry entry = zf.getEntry(input);
            if (entry == null) {
                System.out.println("Not found");
                throw new FileNotFoundException("The embedded resource '" + input + "' cannot be found in jar");
            }
            System.out.println("Size: " + entry.getSize());
            try (InputStream is = zf.getInputStream(entry)) {
                extractFile(is, output);
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void extractFile(@NotNull InputStream in, @NotNull Path output) {
        try {
            try (OutputStream out = Files.newOutputStream(output, StandardOpenOption.TRUNCATE_EXISTING)) {
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save " + output.getFileName(), e);
        }
    }

    public static String detectOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (os.contains("win")) {
            os = "windows";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            os = "linux";
        } else if (os.contains("mac")) {
            os = "darwin";
        }

        return os;
    }

    public static String detectArch() {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);

        switch (arch) {
            case "x86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
                arch = "386";
                break;
            case "x86_64":
            case "amd64":
                arch = "amd64";
                break;
            case "arm64":
            case "aarch64":
                arch = "arm64";
                break;
            default:
                break;
        }

        return arch;
    }

    public static final String BIN_DIR = "propack-builder/";

    private final Path binaryPath;

    public JarBinarySource(Path jarPath) throws IOException {
        String os = detectOS();
        String arch = detectArch();

        String fileName = os + "_" + arch;

        if (os.equals("windows")) {
            binaryPath = Files.createTempFile(null, ".exe");
        } else {
            binaryPath = Files.createTempFile(null, null, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
        }

        try {
            extractFile(jarPath, BIN_DIR + fileName, binaryPath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Binaries not found for " + os + " " + arch + ", open issue and request it", e);
        }
    }

    public Path getBinaryPath() {
        return binaryPath;
    }
}
