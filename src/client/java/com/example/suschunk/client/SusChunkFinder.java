package com.example.suschunk.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SusChunkFinder implements ClientModInitializer {

    public static final int THRESHOLD = 95;
    private static final int CLUSTER_RADIUS = 3;
    private static final double RENDER_DISTANCE = 96.0;

    public static boolean ENABLED = true;

    @Override
    public void onInitializeClient() {
        ChunkStorage.load();

        // Commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("scf")
                    .executes(ctx -> {
                        toggle();
                        return 1;
                    })
                    .then(ClientCommandManager.literal("clear")
                        .executes(ctx -> {
                            ChunkStorage.PEAKS.clear();
                            ChunkStorage.save();
                            msg("§7All saved peaks cleared.");
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("list")
                        .executes(ctx -> {
                            listNearest(5);
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("help")
                        .executes(ctx -> {
                            msg("§d§lSusChunk Commands");
                            msg("§b/scf §7or §b.scf §8- §7Toggle the mod");
                            msg("§b/scf list §8- §7Show nearest peaks");
                            msg("§b/scf clear §8- §7Clear all saved peaks");
                            msg("§b/scf help §8- §7Show this help");
                            return 1;
                        }))
            );
        });

        // Chat fallback
        ClientSendMessageEvents.ALLOW_CHAT.register((message) -> {
            String msg = message.toLowerCase().trim();
            if (msg.equals(".scf") || msg.equals(".suschunk")) {
                toggle();
                return false;
            }
            if (msg.equals(".scf list")) {
                listNearest(5);
                return false;
            }
            if (msg.equals(".scf clear")) {
                ChunkStorage.PEAKS.clear();
                ChunkStorage.save();
                msg("§7All saved peaks cleared.");
                return false;
            }
            return true;
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!ENABLED) return;

            int score = ChunkScorer.score(chunk);
            if (score < THRESHOLD) return;

            ChunkPos pos = chunk.getPos();
            boolean isNewPeak = ChunkStorage.updatePeak(pos, score);
            if (!isNewPeak) return;

            ChunkStorage.markClusters(CLUSTER_RADIUS);
            ChunkStorage.save();

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            FlaggedChunk f = ChunkStorage.PEAKS.get(pos.toLong());
            boolean clustered = f != null && f.clustered;

            int blockX = pos.getStartX() + 8;
            int blockZ = pos.getStartZ() + 8;

            if (clustered) {
                msg("§d§l✦ CLUSTER DETECTED");
                msg("§b  " + blockX + " §7, §b" + blockZ + "  §8│ §7Score §f" + score);
            } else {
                msg("§d✦ Suspicious chunk");
                msg("§b  " + blockX + " §7, §b" + blockZ + "  §8│ §7Score §f" + score);
            }

            double dx = mc.player.getX() - blockX;
            double dz = mc.player.getZ() - blockZ;
            if (Math.sqrt(dx * dx + dz * dz) < 16 * 12) {
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.15f);
            }
        });

        // Dense highlight with hard safety limit
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ENABLED) return;
            if (client.player == null || client.world == null) return;
            if (ChunkStorage.PEAKS.isEmpty()) return;
            if (client.player.age % 2 != 0) return;

            double px = client.player.getX();
            double py = client.player.getY();
            double pz = client.player.getZ();

            List<FlaggedChunk> toRender = new ArrayList<>(ChunkStorage.PEAKS.values());
            toRender.sort(Comparator.comparingDouble(f -> {
                double dx = (f.x * 16 + 8) - px;
                double dz = (f.z * 16 + 8) - pz;
                return dx * dx + dz * dz;
            }));

            int rendered = 0;
            for (FlaggedChunk f : toRender) {
                if (rendered >= 12) break;
                if (f.score < THRESHOLD) continue;

                double cx = f.x * 16 + 8;
                double cz = f.z * 16 + 8;
                double distSq = (cx - px) * (cx - px) + (cz - pz) * (cz - pz);
                if (distSq > RENDER_DISTANCE * RENDER_DISTANCE) continue;

                DustParticleEffect colour = f.clustered
                        ? new DustParticleEffect(0xFF1F1F, 5.5f)
                        : new DustParticleEffect(0xA855F7, 4.0f);

                double x1 = f.x * 16.0;
                double z1 = f.z * 16.0;
                double x2 = x1 + 16.0;
                double z2 = z1 + 16.0;

                // Dense ground plane
                for (double x = x1; x <= x2; x += 1.8) {
                    for (double z = z1; z <= z2; z += 1.8) {
                        client.world.addParticleClient(colour, x, py + 0.12, z, 0, 0, 0);
                    }
                }

                // Tall pillars
                for (int h = 0; h <= 48; h += 2) {
                    client.world.addParticleClient(colour, x1, py + h, z1, 0, 0, 0);
                    client.world.addParticleClient(colour, x2, py + h, z1, 0, 0, 0);
                    client.world.addParticleClient(colour, x1, py + h, z2, 0, 0, 0);
                    client.world.addParticleClient(colour, x2, py + h, z2, 0, 0, 0);

                    client.world.addParticleClient(colour, (x1 + x2) / 2, py + h, z1, 0, 0, 0);
                    client.world.addParticleClient(colour, (x1 + x2) / 2, py + h, z2, 0, 0, 0);
                    client.world.addParticleClient(colour, x1, py + h, (z1 + z2) / 2, 0, 0, 0);
                    client.world.addParticleClient(colour, x2, py + h, (z1 + z2) / 2, 0, 0, 0);
                }

                rendered++;
            }
        });

        System.out.println("Sus Chunk Finder loaded");
    }

    private static void toggle() {
        ENABLED = !ENABLED;
        msg(ENABLED ? "§aEnabled" : "§cDisabled");
    }

    private static void listNearest(int limit) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        List<FlaggedChunk> list = new ArrayList<>(ChunkStorage.PEAKS.values());
        list.sort(Comparator.comparingDouble(f -> {
            double dx = (f.x * 16 + 8) - mc.player.getX();
            double dz = (f.z * 16 + 8) - mc.player.getZ();
            return dx * dx + dz * dz;
        }));

        if (list.isEmpty()) {
            msg("§7No peaks saved yet.");
            return;
        }

        msg("§d§lNearest peaks");
        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            FlaggedChunk f = list.get(i);
            int bx = f.x * 16 + 8;
            int bz = f.z * 16 + 8;
            double dist = Math.sqrt(mc.player.squaredDistanceTo(bx, mc.player.getY(), bz));
            String tag = f.clustered ? " §6●" : "";
            msg("§b" + bx + " §7, §b" + bz + "  §8│ §f" + f.score + " §8│ §7" + (int) dist + "m" + tag);
        }
    }

    private static void msg(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§d✦ §f" + text), false);
        }
    }
}