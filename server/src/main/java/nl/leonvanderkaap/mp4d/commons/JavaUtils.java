package nl.leonvanderkaap.mp4d.commons;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JavaUtils {

    public static Optional<String> getProperty(String propertyKey) {
        Class<?> clazz = JavaUtils.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return Optional.empty();
        }
        String manifestPath = classPath.substring(0, classPath.indexOf(".jar!") + 5) +
                "/META-INF/MANIFEST.MF";
        Manifest manifest = null;
        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        Attributes attr = manifest.getMainAttributes();
        return Optional.ofNullable(attr.getValue(propertyKey));
    }

    public static List<String> getProcessOutput(Process process) throws IOException {
        try (InputStream is = process.getInputStream();
             Reader isr = new InputStreamReader(is);
             BufferedReader r = new BufferedReader(isr)) {
            return r.lines().toList();
        }
    }
}