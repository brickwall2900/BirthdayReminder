package com.github.brickwall2900.birthdays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    public static final Path PATH = Paths.get(System.getProperty("user.home"), "birthdays.json");
    public static final List<BirthdayObject> BIRTHDAY_LIST = new ArrayList<>();
    private static boolean loaded;

    public static void load() throws IOException {
        if (!loaded) {
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
        try (BufferedWriter writer = Files.newBufferedWriter(PATH)) {
            gson.toJson(BIRTHDAY_LIST.toArray(new BirthdayObject[0]), writer);
        }
    }

}
