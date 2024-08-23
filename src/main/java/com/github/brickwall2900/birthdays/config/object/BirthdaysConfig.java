package com.github.brickwall2900.birthdays.config.object;

import com.github.brickwall2900.birthdays.adapters.LocalDateAdapter;
import com.github.brickwall2900.birthdays.adapters.PathAdapter;
import com.github.brickwall2900.birthdays.adapters.PathTypeAdapter;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BirthdaysConfig {
    public static final Path PATH = BirthdayNotifierConfig.GLOBAL_PATH.resolve("birthdays.json");
    public static final List<BirthdayObject> BIRTHDAY_LIST = new ArrayList<>();
    private static boolean loaded;

    public static void load() throws IOException {
        if (!loaded) {
            if (!Files.exists(PATH)) {
                Files.createFile(PATH);
                Files.writeString(PATH, new JsonArray().toString());
            }
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
            Gson gson = gsonBuilder.create();
            try (BufferedReader reader = Files.newBufferedReader(PATH);
                 JsonReader jsonReader = new JsonReader(reader)) {
                BirthdayObject[] objects = gson.fromJson(jsonReader, BirthdayObject[].class);
                BIRTHDAY_LIST.addAll(Arrays.asList(objects));
            }
            loaded = true;
        }
    }

    public static void save() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        Gson gson = gsonBuilder.create();
        String output = gson.toJson(BIRTHDAY_LIST.toArray(new BirthdayObject[0]));
        Files.writeString(PATH, output); // prevent file cutoffs
    }

}
