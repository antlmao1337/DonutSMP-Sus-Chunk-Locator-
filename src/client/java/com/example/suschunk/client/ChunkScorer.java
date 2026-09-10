package com.example.suschunk.client;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;
import java.util.Set;

public class ChunkScorer {

    // ===== Strong signal weights =====
    private static final int WEIGHT_CHEST        = 14;
    private static final int WEIGHT_BARREL       = 13;
    private static final int WEIGHT_SHULKER      = 22;
    private static final int WEIGHT_HOPPER       = 11;
    private static final int WEIGHT_FURNACE      = 8;
    private static final int WEIGHT_DISPENSER    = 9;
    private static final int WEIGHT_ENDER        = 4;

    private static final int BONUS_DEEP_STORAGE  = 55;
    private static final int BONUS_MANY_SHULKER  = 65;
    private static final int BONUS_MANY_HOPPER   = 50;
    private static final int BONUS_HIGH_STORAGE  = 70;

    private static final Set<Block> TRIAL_COPPER = Set.of(
            Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_COPPER_BULB, Blocks.WAXED_COPPER_GRATE,
            Blocks.WAXED_CUT_COPPER, Blocks.WAXED_CHISELED_COPPER,
            Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_GRATE,
            Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER,
            Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_GRATE,
            Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER,
            Blocks.WAXED_OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_GRATE,
            Blocks.WAXED_OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER,
            Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB
    );

    private static final Set<Block> TRIAL_TUFF = Set.of(
            Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF, Blocks.CHISELED_TUFF_BRICKS, Blocks.POLISHED_TUFF
    );

    private static final Set<Block> TRIAL_CORE = Set.of(
            Blocks.TRIAL_SPAWNER, Blocks.VAULT, Blocks.HEAVY_CORE
    );

    private static final Set<Block> STRUCTURAL = Set.of(
            Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE,
            Blocks.CHISELED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE,
            Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
            Blocks.CHISELED_STONE_BRICKS, Blocks.SMOOTH_STONE,
            Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN,
            Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
            Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE,
            Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
            Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE,
            Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL,
            Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL,
            Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
            Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL,
            Blocks.GLASS, Blocks.GLASS_PANE, Blocks.TINTED_GLASS,
            Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR,
            Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS,
            Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
            Blocks.GILDED_BLACKSTONE, Blocks.GOLD_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.IRON_BLOCK,
            Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK
    );

    private static final Set<Block> MINESHAFT = Set.of(
            Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL,
            Blocks.OAK_FENCE, Blocks.DARK_OAK_FENCE, Blocks.COBWEB,
            Blocks.SPAWNER // cave spider spawners
    );

    private static final Set<Block> LIGHT_SOURCES = Set.of(
            Blocks.TORCH, Blocks.WALL_TORCH, Blocks.LANTERN, Blocks.SOUL_LANTERN,
            Blocks.GLOWSTONE, Blocks.SHROOMLIGHT, Blocks.SEA_LANTERN,
            Blocks.END_ROD, Blocks.JACK_O_LANTERN
    );

    public static int score(WorldChunk chunk) {
        int chests = 0, barrels = 0, shulkers = 0, enders = 0;
        int hoppers = 0, furnaces = 0, dispensers = 0;
        int deepStorage = 0;
        int structural = 0;
        int pistons = 0;
        int gamble = 0;
        int dungeon = 0;
        int trialCopper = 0, trialTuff = 0, trialCore = 0;
        int lightSources = 0;
        int artificialLight = 0;
        int longTunnels = 0;
        int mineshaftScore = 0;

        // ===== Tile entities (strongest signal) =====
        for (BlockEntity be : chunk.getBlockEntities().values()) {
            BlockEntityType<?> t = be.getType();
            int y = be.getPos().getY();

            if (t == BlockEntityType.CHEST || t == BlockEntityType.TRAPPED_CHEST) {
                chests++;
                if (y < 0) deepStorage++;
            } else if (t == BlockEntityType.BARREL) {
                barrels++;
                if (y < 0) deepStorage++;
            } else if (t == BlockEntityType.SHULKER_BOX) {
                shulkers++;
                if (y < 0) deepStorage++;
            } else if (t == BlockEntityType.ENDER_CHEST) {
                enders++;
            } else if (t == BlockEntityType.HOPPER) {
                hoppers++;
            } else if (t == BlockEntityType.FURNACE || t == BlockEntityType.BLAST_FURNACE || t == BlockEntityType.SMOKER) {
                furnaces++;
            } else if (t == BlockEntityType.DISPENSER || t == BlockEntityType.DROPPER) {
                dispensers++;
            }
        }

        int storageTotal = chests + barrels + shulkers + hoppers + furnaces + dispensers;

        // ===== Block scan =====
        ChunkPos pos = chunk.getPos();
        BlockPos.Mutable m = new BlockPos.Mutable();
        int bottom = chunk.getBottomY();
        int top = Math.min(chunk.getTopYInclusive(), 112);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int airRun = 0;
                for (int y = bottom; y < top; y += 2) {
                    m.set(pos.getStartX() + x, y, pos.getStartZ() + z);
                    Block b = chunk.getBlockState(m).getBlock();

                    if (TRIAL_CORE.contains(b)) trialCore++;
                    else if (TRIAL_COPPER.contains(b)) trialCopper++;
                    else if (TRIAL_TUFF.contains(b)) trialTuff++;
                    else if (b == Blocks.MOSSY_COBBLESTONE || b == Blocks.COBWEB) dungeon++;
                    else if (b == Blocks.GOLD_BLOCK || b == Blocks.NETHERITE_BLOCK || b == Blocks.GILDED_BLACKSTONE) gamble++;
                    else if (b == Blocks.PISTON || b == Blocks.STICKY_PISTON) pistons++;
                    else if (STRUCTURAL.contains(b)) structural++;
                    else if (LIGHT_SOURCES.contains(b)) lightSources++;
                    else if (MINESHAFT.contains(b)) mineshaftScore++;

                    // Strict tunnel detection
                    boolean isAir = b == Blocks.AIR || b == Blocks.CAVE_AIR;
                    if (isAir) {
                        airRun += 2;
                    } else {
                        if (airRun >= 18) longTunnels++;
                        airRun = 0;
                    }

                    // Artificial light only deep
                    if (y < 24) {
                        int bl = chunk.getWorld().getLightLevel(LightType.BLOCK, m);
                        if (bl >= 11) artificialLight++;
                    }
                }
                if (airRun >= 18) longTunnels++;
            }
        }

        // ===== Entities =====
        int armorStands = 0;
        int itemFrames = 0;
        int chestMinecarts = 0;

        Box box = new Box(pos.getStartX(), bottom, pos.getStartZ(),
                pos.getStartX() + 16, top, pos.getStartZ() + 16);

        List<Entity> entities = chunk.getWorld().getOtherEntities(null, box);
        for (Entity e : entities) {
            if (e instanceof ArmorStandEntity) armorStands++;
            else if (e instanceof ItemFrameEntity) itemFrames++;
            else if (e instanceof ChestMinecartEntity || e instanceof HopperMinecartEntity) chestMinecarts++;
        }

        int entityScore = armorStands * 20 + itemFrames * 14 + chestMinecarts * 28;

        // ===== Hard rejection filters =====
        if (trialCore >= 1) return 0;
        if (trialCopper + trialTuff >= 45) return 0;
        if (dungeon >= 7 && storageTotal < 6) return 0;
        if (mineshaftScore >= 12 && storageTotal < 5 && structural < 20) return 0; // kill mineshafts
        if (enders >= 5 && storageTotal - enders < 3 && deepStorage == 0) return 0;

        // ===== Scoring =====
        int score = 0;

        // Core storage (restored strong weights)
        score += chests * WEIGHT_CHEST;
        score += barrels * WEIGHT_BARREL;
        score += shulkers * WEIGHT_SHULKER;
        score += hoppers * WEIGHT_HOPPER;
        score += furnaces * WEIGHT_FURNACE;
        score += dispensers * WEIGHT_DISPENSER;
        score += enders * WEIGHT_ENDER;

        if (storageTotal >= 10) score += BONUS_HIGH_STORAGE;
        if (deepStorage >= 4) score += BONUS_DEEP_STORAGE;
        if (shulkers >= 4) score += BONUS_MANY_SHULKER;
        if (hoppers >= 8) score += BONUS_MANY_HOPPER;

        // Structural
        if (structural >= 30) score += 35;
        if (structural >= 70) score += 55;
        if (structural >= 140) score += 85;

        // Automation / wealth
        if (pistons >= 5) score += 45;
        if (gamble >= 3) score += 50;

        // Entities (very strong on real bases)
        score += Math.min(entityScore, 110);

        // Tunnels only as a helper when player activity already exists
        int activity = storageTotal + (structural / 8) + (entityScore / 15);
        if (activity >= 6) {
            score += Math.min(longTunnels * 7, 28);
        }

        // Light is now almost negligible
        score += Math.min(lightSources * 2, 12);
        score += Math.min(artificialLight / 10, 8);

        // Final quality gates
        if (storageTotal < 3 && entityScore < 25 && structural < 40) return 0;
        if (storageTotal < 2 && deepStorage == 0 && hoppers < 2 && entityScore < 20) return 0;

        return score;
    }
}