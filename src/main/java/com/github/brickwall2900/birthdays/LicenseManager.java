package com.github.brickwall2900.birthdays;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class LicenseManager {
    private static final String LICENSE_PATH_IN_JAR = "META-INF/licenses";

    public static void openLicense() {
        try {
            Path tempDir = Files.createTempDirectory(Main.UNIQUE_APP_ID + "license");
            tempDir.toFile().deleteOnExit();

            Path dest = extractResourcesFromJar(tempDir, LICENSE_PATH_IN_JAR);
            System.out.println(dest);
            Path index = dest.resolve("index.html");
            System.out.println(index);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteOnExit(tempDir)));

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(index.toUri());
            } else {
                JOptionPane.showMessageDialog(null, text("errors.license.desktop", index),
                        text("errors.title"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, text("errors.license", e),
                    text("errors.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteOnExit(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    System.out.println("Deleted file " + file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
                    if (!directory.equals(path)) {
                        Files.delete(directory);
                        System.out.println("Deleted directory " + directory);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            Files.delete(path);
        } catch (IOException e) {
            System.err.println("Failed to delete directory on exit!");
        }
    }

    private static Path getCodeSource(Class<?> cls) throws URISyntaxException {
        return Path.of(cls.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
    }

    private static Path extractResourcesFromJar(Path tempDirPath, String resource) throws IOException {
        ClassLoader classLoader = LicenseManager.class.getClassLoader();
        URL jarUrl = classLoader.getResource(resource);

        if (jarUrl == null) {
            throw new FileNotFoundException("Directory not found at " + resource);
        }

        try {
            Path codeSource = getCodeSource(LicenseManager.class);
            // check if we are in jar file
            if (jarUrl.getProtocol().equals("jar")) {
                try (JarFile jarFile = new JarFile(codeSource.toString())) {
                    Iterator<JarEntry> entries = jarFile.entries().asIterator();
                    while (entries.hasNext()) {
                        JarEntry entry = entries.next();
                        if (entry.getName().startsWith(resource) && !entry.isDirectory()) {
                            String relativePath = entry.getName().substring(resource.length());
                            Path outputPath = tempDirPath.resolve(relativePath.substring(1));

                            if (!outputPath.startsWith(tempDirPath)) {
                                throw new IOException("Entry is outside of the target dir: " + outputPath);
                            }

                            Files.createDirectories(outputPath.getParent());

                            try (InputStream is = jarFile.getInputStream(entry)) {
                                Files.copy(is, outputPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }
                return tempDirPath;
            } else {
                // we must be in a file system
                // waaaa
                return Path.of(jarUrl.toURI());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
