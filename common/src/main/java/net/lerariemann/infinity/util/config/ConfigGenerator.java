package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.CosmicAltarBlockEntity;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.structure.Structure;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Methods for generating modular config files (scanning all registries for usable content) and writing them to the disk.</p>
 * <p>Generating most content types is done via {@link ConfigFactory}.</p>
 * <p>Used by the {@link CosmicAltarBlockEntity} performing an invocation.</p>
 * @author LeraRiemann
 */
public interface ConfigGenerator {
    static void generateAll(MinecraftServer server) {
        ConfigFactory.of(Registries.ITEM).generate("misc", "items");
        ConfigFactory.of(Registries.PARTICLE_TYPE, ConfigGenerator::extractParticle).generate("misc", "particles");
        ConfigFactory.of(Registries.ENTITY_TYPE, ConfigGenerator::extractMob).generate("extra", "mobs");
        ConfigFactory.of(Registries.STATUS_EFFECT, ConfigGenerator::extractEffect).generate("extra", "effects");
        generateSounds();
        generateBlockTags();
        SurfaceRuleScanner.scan(server);
        DynamicRegistryManager manager = server.getRegistryManager();
        ConfigFactory.of(manager.get(RegistryKeys.JUKEBOX_SONG)).generate("misc", "jukeboxes");
        ConfigFactory.of(manager.get(RegistryKeys.BIOME), ConfigGenerator::extractBiome).generate("extra", "biomes");
        ConfigFactory.of(manager.get(RegistryKeys.STRUCTURE), ConfigGenerator::extractStructure).generate("extra", "structures");
        ConfigFactory.of(manager.get(RegistryKeys.CONFIGURED_FEATURE), ConfigGenerator::extractFeature).generate("extra", "trees");
        ConfigFactory.of(server.getReloadableRegistries().getRegistryManager().get(RegistryKeys.LOOT_TABLE), ConfigGenerator::extractLootTable)
                .generate("extra", "loot_tables");
    }

    static void generateSounds() {
        Registry<SoundEvent> r = Registries.SOUND_EVENT;
        DataCollection.Logged<String> music = new DataCollection.Logged<>("misc", "music", "music tracks");
        DataCollection.Logged<String> sounds = new DataCollection.Logged<>("misc", "sounds");
        r.getKeys().forEach(a -> {
            Identifier id = a.getValue();
            if (id.toString().contains("music")) DataCollection.addIdentifier(music, id);
            else DataCollection.addIdentifier(sounds, id);
        });
        sounds.save();
        music.save();
    }

    static void generateBlockTags() {
        DataCollection.Logged<String> tagMap = new DataCollection.Logged<>("misc", "tags", "block tags");
        Registries.BLOCK.streamTags().forEach(tagKey -> DataCollection.addIdentifier(tagMap, tagKey.id()));
        tagMap.save();
    }

    static Set<String> generateFluids() {
        DataCollection.Logged<NbtCompound> fluidMap = new DataCollection.Logged<>("blocks", "fluids");
        Registry<Fluid> r = Registries.FLUID;
        Set<String> fluidBlockNames = new HashSet<>();
        r.getKeys().forEach(a -> {
            NbtCompound data = extractFluid(a);
            fluidBlockNames.add(data.getString("Name"));
            Fluid f = r.get(a.getValue());
            if (f instanceof FlowableFluid fl && fl.equals(fl.getStill())) {
                String modId = a.getValue().getNamespace();
                fluidMap.add(modId, data);
            }
        });
        fluidMap.save();
        return fluidBlockNames;
    }

    static void generateBlocks(ServerWorld serverWorld, BlockPos inAir, BlockPos onAltar, Set<String> excludedBlockNames) {
        DataCollection.Logged<NbtCompound> blockMap = new DataCollection.Logged<>("blocks", "blocks");
        DataCollection<NbtList> colorPresetMap = new DataCollection<>("extra", "color_presets");
        DataCollection<String> airMap = new DataCollection<>("blocks", "airs");
        DataCollection<String> flowerMap = new DataCollection<>("blocks", "flowers");
        Registry<Block> r = Registries.BLOCK;
        r.getKeys().forEach(key -> {
            String blockName = key.getValue().toString();
            if(excludedBlockNames.contains(blockName)) return;
            Block block = r.get(key);
            assert block != null;
            String modId = key.getValue().getNamespace();
            blockMap.add(modId, extractBlock(key, serverWorld, inAir, onAltar));
            if (blockName.contains("magenta") && !isLaggy(block) && isFloat(block.getDefaultState(), serverWorld, inAir)) {
                NbtList lst = checkColorSet(blockName);
                if (lst != null) colorPresetMap.add(modId, lst);
            }
            if (block.getDefaultState().isIn(BlockTags.AIR)) DataCollection.addIdentifier(airMap, key.getValue());
            if (block.getDefaultState().isIn(BlockTags.SMALL_FLOWERS)) DataCollection.addIdentifier(flowerMap, key.getValue());
        });
        blockMap.save();
        colorPresetMap.save();
        airMap.save();
        flowerMap.save();
    }

    static NbtList checkColorSet(String block) {
        AtomicInteger successCounter = new AtomicInteger();
        NbtList colorSet = new NbtList();
        Arrays.stream(ColorLogic.vanillaColors).forEach(color -> {
            int i = block.lastIndexOf("magenta");
            String blockColored = block.substring(0, i) + color + block.substring(i+7);
            if (Registries.BLOCK.containsId(Identifier.of(blockColored))) {
                successCounter.addAndGet(1);
                NbtCompound c = new NbtCompound();
                c.putString("Name", blockColored);
                colorSet.add(c);
            }
        });
        return (successCounter.get() == ColorLogic.vanillaColors.length) ? colorSet : null;
    }

    static NbtCompound extractFluid(RegistryKey<Fluid> key) {
        Fluid b = Registries.FLUID.get(key);
        assert b!= null;
        NbtCompound res = new NbtCompound();
        String name = Registries.BLOCK.getId(b.getDefaultState().getBlockState().getBlock()).toString();
        res.putString("Name", name);
        res.putString("fluidName", key.getValue().toString());
        return res;
    }

    static NbtCompound extractBlock(RegistryKey<Block> key, WorldView w, BlockPos inAir, BlockPos onStone) {
        Block b = Registries.BLOCK.get(key);
        assert b!= null;
        BlockState bs = b.getDefaultState();
        NbtCompound res = new NbtCompound();
        res.putString("Name", key.getValue().toString());
        res.putBoolean("laggy", isLaggy(b));
        res.putBoolean("full", isFull(bs, w, inAir));
        res.putBoolean("float", isFloat(bs, w, inAir));
        NbtCompound properties = new NbtCompound();
        if (bs.contains(Properties.PERSISTENT)) properties.putString("persistent", "true");
        if (bs.contains(Properties.LIT)) properties.putString("lit", "false");
        if (bs.contains(Properties.BLOCK_FACE)) {
            properties.putString("face", "floor");
            bs = bs.with(Properties.BLOCK_FACE, BlockFace.FLOOR);
        }
        res.putBoolean("top", isTop(bs, w, onStone));
        if (!properties.isEmpty()) res.put("Properties", properties);
        return res;
    }

    static boolean isLaggy(Block b) {
        return (b.getDefaultState().hasBlockEntity());
    }

    static boolean isTop(BlockState bs, WorldView w, BlockPos onStone) {
        try {
            return bs.canPlaceAt(w, onStone);
        }
        catch (Exception e) {
            return false;
        }
    }
    static boolean isFloat(BlockState bs, WorldView w, BlockPos inAir) {
        try {
            return bs.canPlaceAt(w, inAir) && !(bs.getBlock() instanceof FallingBlock);
        }
        catch (Exception e) {
            return false;
        }
    }
    static boolean isFull(BlockState bs, WorldView w, BlockPos inAir) {
        return Block.isShapeFullCube(bs.getCollisionShape(w, inAir)) && Block.isShapeFullCube(bs.getOutlineShape(w, inAir));
    }

    static String extractParticle(RegistryKey<ParticleType<?>> key) {
        Identifier id = key.getValue();
        if (!id.getNamespace().equals("minecraft") && !(Registries.PARTICLE_TYPE.get(id) instanceof SimpleParticleType)) return null;
        return id.toString();
    }

    static NbtCompound extractMob(RegistryKey<EntityType<?>> key) {
        if (key.getValue().getNamespace().equals("minecolonies")) return null; //this mod's mobs crash the game when spawned from the biome
        SpawnGroup sg = Registries.ENTITY_TYPE.get(key.getValue()).getSpawnGroup();
        if (sg == SpawnGroup.MISC) return null; //minecarts n stuff
        NbtCompound mob = new NbtCompound();
        mob.putString("Name", key.getValue().toString());
        mob.putString("Category", sg.asString());
        return mob;
    }

    static NbtCompound extractEffect(RegistryKey<StatusEffect> key) {
        NbtCompound res = new NbtCompound();
        Optional<StatusEffect> o = Registries.STATUS_EFFECT.getOrEmpty(key);
        if (o.isEmpty()) return null;
        StatusEffect effect = o.get();
        res.putString("Name", key.getValue().toString());
        res.putString("Category", switch (effect.getCategory()) { //this data is unused for now but might be important later
            case HARMFUL -> "harmful";
            case BENEFICIAL -> "beneficial";
            case NEUTRAL -> "neutral";
        });
        res.putInt("Color", effect.getColor());
        res.putBoolean("Instant", effect.isInstant());
        return res;
    }

    static NbtCompound extractStructure(Registry<Structure> registry, RegistryKey<Structure> key) {
        Identifier id = key.getValue();
        if (id.getNamespace().equals(InfinityMod.MOD_ID) && id.getPath().contains("_")) return null;
        Optional<Structure> o = registry.getOrEmpty(key);
        if (o.isEmpty()) return null;
        Structure structure = o.get();
        String step = structure.getFeatureGenerationStep().name().toLowerCase();
        String adaptation = structure.getTerrainAdaptation().name().toLowerCase();
        NbtCompound overrides = genOverrides(structure.getStructureSpawns());
        NbtCompound res = new NbtCompound();
        res.putString("id", id.toString());
        res.putString("step", step);
        res.put("spawn_overrides", overrides);
        res.putString("terrain_adaptation", adaptation);
        return res;
    }

    static NbtCompound genOverrides(Map<SpawnGroup, StructureSpawns> overrides) {
        NbtCompound res = new NbtCompound();
        overrides.forEach((key, value) -> res.put(key.name().toLowerCase(), genOverride(value)));
        return res;
    }

    static NbtCompound genOverride(StructureSpawns spawns) {
        NbtCompound res = new NbtCompound();
        StructureSpawns.BoundingBox box = spawns.boundingBox();
        res.putString("bounding_box", box == StructureSpawns.BoundingBox.PIECE ? "piece" : "full");
        res.put("spawns", genSpawns(spawns.spawns()));
        return res;
    }

    static NbtList genSpawns(Pool<SpawnSettings.SpawnEntry> entries) {
        NbtList lst = new NbtList();
        entries.getEntries().forEach((entry) -> lst.add(genEntry(entry)));
        return lst;
    }

    static NbtCompound genEntry(SpawnSettings.SpawnEntry entry) {
        NbtCompound res = new NbtCompound();
        res.putString("type", Registries.ENTITY_TYPE.getId(entry.type).toString());
        res.putInt("maxCount", entry.maxGroupSize);
        res.putInt("minCount", entry.minGroupSize);
        res.putInt("weight", entry.getWeight().getValue());
        return res;
    }

    static NbtCompound extractFeature(Registry<ConfiguredFeature<?,?>> registry, RegistryKey<ConfiguredFeature<?,?>> key) {
        Identifier id = key.getValue();
        if (id.getNamespace().equals(InfinityMod.MOD_ID) && id.getPath().contains("_")) return null; //our mod's custom trees
        if (id.toString().contains("bees")) return null; //bees lag the game -_-
        Optional<ConfiguredFeature<?,? extends Feature<?>>> o = registry.getOrEmpty(key);
        if (o.isEmpty()) return null;
        ConfiguredFeature<?,? extends Feature<?>> feature = o.get();
        String type = getFeatureType(feature.feature());
        if (type.isEmpty()) return null;
        NbtCompound res = new NbtCompound();
        res.putString("Name", id.toString());
        res.putString("Type", type); //this data is unused for now but might be important later
        return res;
    }

    static String getFeatureType(Feature<?> type) {
        if (type.equals(Feature.TREE)) return "tree";
        if (type.equals(Feature.HUGE_FUNGUS)) return "huge_fungus";
        if (type.equals(Feature.HUGE_BROWN_MUSHROOM)) return "huge_brown_mushroom";
        if (type.equals(Feature.HUGE_RED_MUSHROOM)) return "huge_red_mushroom";
        return "";
    }

    static NbtCompound extractLootTable(Registry<LootTable> registry, RegistryKey<LootTable> key) {
        Identifier id = key.getValue();
        Optional<LootTable> o = registry.getOrEmpty(key);
        if (o.isEmpty()) return null;
        LootTable table = o.get();
        Identifier type = LootContextTypes.MAP.inverse().get(table.getType());
        if (type == null) return null;
        NbtCompound res = new NbtCompound();
        res.putString("Name", id.toString());
        res.putString("Type", type.getPath()); //this data is unused for now but might be important later
        return res;
    }

    static NbtCompound extractBiome(Registry<Biome> registry, RegistryKey<Biome> key) {
        Identifier id = key.getValue();
        if (id.getNamespace().equals(InfinityMod.MOD_ID)) return null; //cull our generated biomes
        Optional<Biome> o = registry.getOrEmpty(key);
        if (o.isEmpty()) return null;
        Biome biome = o.get();
        NbtCompound res = new NbtCompound();
        res.putString("Name", id.toString());
        res.putInt("Color", biome.getFoliageColor());
        return res;
    }
}
