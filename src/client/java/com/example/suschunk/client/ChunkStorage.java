package com.example.suschunk.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.math.ChunkPos;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Path.of("config/suschunkfinder/flagged.json");

    public static final Map<Long, FlaggedChunk> PEAKS = new ConcurrentHashMap<>();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE);
                Type type = new TypeToken<Map<String, FlaggedChunk>>(){}.getType();
                Map<String, FlaggedChunk> loaded = GSON.fromJson(json, type);
                if (loaded != null) {
                    loaded.forEach((k, v) -> PEAKS.put(Long.parseLong(k), v));
                }
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, FlaggedChunk> out = new HashMap<>();
            PEAKS.forEach((k, v) -> out.put(Long.toString(k), v));
            Files.writeString(FILE, GSON.toJson(out));
        } catch (IOException ignored) {}
    }

    public static boolean updatePeak(ChunkPos pos, int score) {
        long key = pos.toLong();
        FlaggedChunk existing = PEAKS.get(key);
        if (existing == null || score > existing.score) {
            PEAKS.put(key, new FlaggedChunk(pos.x, pos.z, score, System.currentTimeMillis()));
            return true;
        }
        return false;
    }

    public static void markClusters(int radius) {
        PEAKS.values().forEach(f -> f.clustered = false);
        List<FlaggedChunk> list = new ArrayList<>(PEAKS.values());
        for (FlaggedChunk a : list) {
            int neighbours = 0;
            for (FlaggedChunk b : list) {
                if (a == b) continue;
                if (Math.abs(a.x - b.x) <= radius && Math.abs(a.z - b.z) <= radius) {
                    neighbours++;
                }
            }
            if (neighbours >= 1) a.clustered = true;
        }
    }
}