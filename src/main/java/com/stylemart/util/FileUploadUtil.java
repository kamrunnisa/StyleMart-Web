package com.stylemart.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FileUploadUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    public static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private FileUploadUtil() {}

    public static boolean isAllowedExtension(String filename) {
        String ext = extensionOf(filename);
        return ext != null && ALLOWED_EXTENSIONS.contains(ext);
    }

    public static String extensionOf(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Strips path separators and anything outside a safe charset from a
     * client-supplied filename, so a name like "../../evil.jsp" or
     * "..\\..\\web.xml" can never be used to escape the target directory.
     */
    public static String sanitizeBaseName(String originalFilename) {
        String name = originalFilename == null ? "file" : originalFilename;
        // Drop any directory components the browser/client might have sent.
        name = name.replace("\\", "/");
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        base = base.replaceAll("[^a-zA-Z0-9_-]", "-");
        if (base.isBlank()) {
            base = "product";
        }
        return base;
    }

    /**
     * Builds a filename that (a) can't collide with an existing file and
     * (b) can't be used for directory traversal, then verifies the final
     * resolved path is still inside targetDir before returning it.
     */
    public static Path resolveSafeUniquePath(Path targetDir, String originalFilename) throws java.io.IOException {
        String ext = extensionOf(originalFilename);
        String base = sanitizeBaseName(originalFilename);

        String candidateName = base + "." + ext;
        Path candidate = targetDir.resolve(candidateName).normalize();

        if (!candidate.startsWith(targetDir.normalize())) {
            // Should be unreachable given sanitizeBaseName(), but fail closed anyway.
            throw new SecurityException("Invalid upload path");
        }

        // Prevent duplicate filenames by appending a short unique suffix.
        while (Files.exists(candidate)) {
            String suffixed = base + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            candidate = targetDir.resolve(suffixed).normalize();
        }

        return candidate;
    }

    /**
     * Directory where admin-uploaded product photos actually live on disk.
     *
     * Deliberately NOT inside the deployed webapp folder: Tomcat wipes/re-extracts
     * that folder every time a new WAR is deployed, which would silently delete
     * anything uploaded through the admin panel. This resolves to a fixed folder
     * in the user's home directory instead, so it survives every rebuild/redeploy.
     * A Tomcat <Resources> entry (see context.xml) mounts this same folder at the
     * /assets/img/products URL path so it's served exactly like a bundled image.
     *
     * Override with -Dstylemart.uploads.dir=<path> if a different location is needed.
     */
    public static Path resolveUploadDir() throws java.io.IOException {
        String override = System.getProperty("stylemart.uploads.dir");
        Path dir = (override != null && !override.isBlank())
                ? Path.of(override)
                : Path.of(System.getProperty("user.home"), "stylemart-uploads", "products");
        Files.createDirectories(dir);
        return dir;
    }
}
