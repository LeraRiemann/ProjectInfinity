package net.lerariemann.infinity.util.core;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum ConfigType implements StringIdentifiable {
    BLOCKS("blocks", "minecraft:stone"),
    ALL_BLOCKS("all_blocks", "minecraft:stone"),
    TOP_BLOCKS("top_blocks", "minecraft:stone"),
    BLOCKS_FEATURES("blocks_features", "minecraft:stone"),
    FULL_BLOCKS("full_blocks", "minecraft:stone"),
    FULL_BLOCKS_WG("full_blocks_worldgen", "minecraft:stone"),
    FLOWERS("flowers", "minecraft:poppy"),
    CHESTS("chests", "minecraft:chest"),
    FLUIDS("fluids", "minecraft:water", NbtUtils::nameToFluid),
    ITEMS("items", "minecraft:stick"),
    SOUNDS("sounds", "minecraft:block.stone.step"),
    MUSIC("music", "minecraft:music.game"),
    JUKEBOXES("jukeboxes", "minecraft:cat"),
    PARTICLES("particles", "minecraft:heart"),
    BIOMES("biomes", "minecraft:plains"),
    TAGS("tags", "minecraft:air"),
    TREES("trees", "minecraft:oak"),
    LOOT_TABLES("loot_tables", "minecraft:blocks/stone"),
    STRUCTURES("structures", "minecraft:jungle_pyramid"),
    EFFECTS("effects", "minecraft:speed"),
    COLOR_PRESETS("color_presets", "minecraft:$_wool"),
    MOBS("mobs", "minecraft:pig"),
    MONSTER("monster", "minecraft:zombie"),
    CREATURE("creature", "minecraft:pig"),
    AMBIENT("ambient", "minecraft:bat"),
    WATER_CREATURE("water_creature", "minecraft:squid"),
    UNDERGROUND_WATER_CREATURE("underground_water_creature", "minecraft:glow_squid"),
    WATER_AMBIENT("water_ambient", "minecraft:cod"),
    AXOLOTLS("axolotls", "minecraft:axolotl"),
    BIOME_SOURCE_TYPES("biome_source_types", "minecraft:multi_noise"),
    CARVERS("carvers", "minecraft:cave"),
    DIMENSION_EFFECTS("dimension_effects", "minecraft:overworld"),
    FLORAL_DISTRIBUTION("floral_distribution", "trees"),
    FOLIAGE_PLACERS("foliage_placers", "blob_foliage_placer"),
    GENERATOR_TYPES("generator_types", "minecraft:noise"),
    MOB_CATEGORIES("mob_categories", "creature"),
    MULTINOISE_PRESETS("multinoise_presets", "none"),
    NOISE_PRESETS("noise_presets", "minecraft:overworld"),
    SHAPE_TYPES("shape_types", "star"),
    STRUCTURE_PLACEMENT_TYPES("structure_placement_types", "minecraft:random_spread"),
    TREE_DECORATORS("tree_decorators", "attached_to_leaves"),
    TRUNK_PLACERS("trunk_placers", "straight_trunk_placer"),
    GRASS("grass", "minecraft:patch_grass_normal"),
    VEG1("vegetation_part1", "minecraft:patch_sunflower"),
    VEG2("vegetation_part2", "minecraft:patch_pumpkin"),
    LOCAL_MOD("localmodifications", "minecraft:large_dripstone"),
    TOP_LAYER("toplayermodification", "minecraft:freeze_top_layer"),
    UNDERGROUND_DEC("undergrounddecoration", "minecraft:pointed_dripstone"),
    UNDERGROUND_ORES("undergroundores", "minecraft:ore_dirt");

    public static final ConfigType[] normalModular = new ConfigType[]{COLOR_PRESETS, EFFECTS, STRUCTURES,
            LOOT_TABLES, TAGS, PARTICLES, MUSIC, SOUNDS, ITEMS, TREES, JUKEBOXES};

    public static final ConfigType[] mobCategories = new ConfigType[]{MONSTER, CREATURE, AMBIENT,
            WATER_CREATURE, UNDERGROUND_WATER_CREATURE, WATER_AMBIENT, AXOLOTLS};

    public static final ConfigType[] hardcoded = new ConfigType[]{BIOME_SOURCE_TYPES, CARVERS, DIMENSION_EFFECTS,
            FLORAL_DISTRIBUTION, FOLIAGE_PLACERS, GENERATOR_TYPES, MOB_CATEGORIES, MULTINOISE_PRESETS, NOISE_PRESETS,
            SHAPE_TYPES, STRUCTURE_PLACEMENT_TYPES, TREE_DECORATORS, TRUNK_PLACERS};

    public static final ConfigType[] vegetation = new ConfigType[]{GRASS, VEG1, VEG2};

    public static final ConfigType[] features = new ConfigType[]{LOCAL_MOD, TOP_LAYER, UNDERGROUND_DEC, UNDERGROUND_ORES};

    public static boolean addsName(ConfigType type) {
        return type != STRUCTURES;
    }

    private final String key;
    private final String def;
    private final Function<String, NbtCompound> converter;
    public static final StringIdentifiable.EnumCodec<ConfigType> CODEC = StringIdentifiable.createCodec(ConfigType::values);

    ConfigType(String key, String def) {
        this (key, def, NbtUtils::nameToElement);
    }
    ConfigType(String key, String def, Function<String, NbtCompound> converter) {
        this.key = key;
        this.def = def;
        this.converter = converter;
    }

    @Nullable
    public static ConfigType byName(String name) {
        return CODEC.byId(name);
    }
    @Override
    public String asString() {
        return this.key;
    }
    public String getKey() {
        return this.key;
    }
    public String getDef() {
        return this.def;
    }

    public Function<String, NbtCompound> getConverter() {
        return converter;
    }
    public NbtCompound fromName(String name) {
        return converter.apply(name);
    }
}
