package com.github.brickwall2900.birthdays.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class BirthdayNotifierConfig {
    public static final Path GLOBAL_PATH = Paths.get(System.getProperty("user.home"), "BirthdayReminder");
    public static final Path NOTIFIER_CONFIG_PATH = GLOBAL_PATH.resolve("notifier.json");
    public static Config globalConfig = new Config();
    public static boolean loaded;

    static {
        if (!Files.exists(GLOBAL_PATH)) {
            try {
                Files.createDirectories(GLOBAL_PATH);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create global folder: " + GLOBAL_PATH, e);
            }
        }
    }

    public static void load() throws IOException {
        if (!loaded) {
            if (!Files.exists(NOTIFIER_CONFIG_PATH)) {
                save();
            }
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create();
            try (BufferedReader reader = Files.newBufferedReader(NOTIFIER_CONFIG_PATH);
                 JsonReader jsonReader = new JsonReader(reader)) {
                globalConfig = gson.fromJson(jsonReader, Config.class);
            }
            loaded = true;
        }
    }

    public static void save() throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        try (BufferedWriter writer = Files.newBufferedWriter(NOTIFIER_CONFIG_PATH)) {
            gson.toJson(globalConfig, writer);
        }
    }

    public static class Config {
        public int daysBeforeReminder = 1;
        public String birthdaySoundPath;

        public Config(int daysBeforeReminder, String birthdaySoundPath) {
            this.daysBeforeReminder = daysBeforeReminder;
            this.birthdaySoundPath = !birthdaySoundPath.isBlank() ? birthdaySoundPath : null;
        }

        public Config() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Config config)) return false;
            return daysBeforeReminder == config.daysBeforeReminder && Objects.equals(birthdaySoundPath, config.birthdaySoundPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(daysBeforeReminder, birthdaySoundPath);
        }

        @Override
        public String toString() {
            return "Config{" +
                    "daysBeforeReminder=" + daysBeforeReminder +
                    ", birthdaySoundPath=" + birthdaySoundPath +
                    '}';
        }
    }
}
