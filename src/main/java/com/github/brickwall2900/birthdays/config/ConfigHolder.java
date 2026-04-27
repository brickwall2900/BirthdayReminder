package com.github.brickwall2900.birthdays.config;

import com.github.brickwall2900.birthdays.adapters.LocalDateAdapter;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigHolder {
    public static final Path GLOBAL_PATH = Path.of(System.getProperty("user.home"), "BirthdayReminder");
    public static final Path NOTIFIER_CONFIG_PATH = GLOBAL_PATH.resolve("notifier.json");
    public static final Path APP_CONFIG_PATH = GLOBAL_PATH.resolve("application.json");
    public static final Path BIRTHDAYS_PATH = GLOBAL_PATH.resolve("birthdays.json");

    private static final Gson gson;

    private static BirthdayNotifierConfig notifierConfig = new BirthdayNotifierConfig();
    private static ApplicationConfig applicationConfig = new ApplicationConfig();
    private static final List<BirthdayObject> BIRTHDAY_LIST = new ArrayList<>();

    static {
        if (!Files.exists(GLOBAL_PATH)) {
            try {
                Files.createDirectories(GLOBAL_PATH);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot create global folder: " + GLOBAL_PATH, e);
            }
        }

        GsonBuilder builder = new GsonBuilder();
        gson = builder
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public static void loadGlobalConfig() throws IOException {
        if (!Files.exists(NOTIFIER_CONFIG_PATH)) {
            saveGlobalConfig();
        }
        try (BufferedReader reader = Files.newBufferedReader(NOTIFIER_CONFIG_PATH);
             JsonReader jsonReader = new JsonReader(reader)) {
            setNotifierConfig(gson.fromJson(jsonReader, BirthdayNotifierConfig.class));
        }
    }

    public static void saveGlobalConfig() throws IOException {
        String output = gson.toJson(getNotifierConfig());
        Files.writeString(NOTIFIER_CONFIG_PATH, output);
    }

    public static void loadApplicationConfig() throws IOException {
        if (!Files.exists(APP_CONFIG_PATH)) {
            saveApplicationConfig();
        }
        try (BufferedReader reader = Files.newBufferedReader(APP_CONFIG_PATH);
             JsonReader jsonReader = new JsonReader(reader)) {
            setApplicationConfig(gson.fromJson(jsonReader, ApplicationConfig.class));
        }
    }

    public static void saveApplicationConfig() throws IOException {
        String output = gson.toJson(getApplicationConfig());
        Files.writeString(APP_CONFIG_PATH, output);
    }

    public static void loadBirthdays() throws IOException {
        if (!Files.exists(BIRTHDAYS_PATH)) {
            saveBirthdays();
        }
        try (BufferedReader reader = Files.newBufferedReader(BIRTHDAYS_PATH);
             JsonReader jsonReader = new JsonReader(reader)) {
            BirthdayObject[] objects = gson.fromJson(jsonReader, BirthdayObject[].class);
            setBirthdayList(Arrays.asList(objects));
        }
    }

    public static void saveBirthdays() throws IOException {
        String output = gson.toJson(getBirthdayList().toArray(new BirthdayObject[0]));
        Files.writeString(BIRTHDAYS_PATH, output); // prevent file cutoffs
    }

    public static BirthdayNotifierConfig getNotifierConfig() {
        return notifierConfig;
    }

    public static void setNotifierConfig(BirthdayNotifierConfig notifierConfig) {
        ConfigHolder.notifierConfig = notifierConfig;
    }

    public static ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public static void setApplicationConfig(ApplicationConfig applicationConfig) {
        ConfigHolder.applicationConfig = applicationConfig;
    }

    /// returns an unmodifiable list of birthday objects
    public static List<BirthdayObject> getBirthdayList() {
        return Collections.unmodifiableList(BIRTHDAY_LIST);
    }

    public static void setBirthdayList(List<BirthdayObject> birthdayList) {
        BIRTHDAY_LIST.clear();
        BIRTHDAY_LIST.addAll(birthdayList);
    }
}
