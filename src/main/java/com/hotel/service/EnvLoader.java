package com.hotel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EnvLoader {
    private static final Map<String, String> env = new HashMap<>();

    static {
        try (Stream<String> lines = Files.lines(Paths.get(".env"))) {
            lines.filter(line -> line.contains("="))
                 .forEach(line -> {
                     String[] parts = line.split("=", 2);
                     env.put(parts[0].trim(), parts[1].trim());
                 });
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger le fichier .env", e);
        }
    }

    public static String get(String key) {
        return env.get(key);
    }
}
