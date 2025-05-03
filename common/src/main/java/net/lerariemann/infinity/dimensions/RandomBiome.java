package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.*;
import net.minecraft.nbt.*;

import java.util.*;

public class RandomBiome {
    public RandomDimension parent;
    public final RandomProvider PROVIDER;
    public long id;
    public String name;
    public String fullname;
    public final Random random;
    public Set<String> mobs;
    public NbtCompound data;
    public NbtCompound colors;

    RandomBiome(long i, RandomDimension dim) {
        id = i;
        parent = dim;
        random = new Random(i);
        PROVIDER = dim.PROVIDER;
        name = "biome_" +i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        mobs = new HashSet<>();
        data = new NbtCompound();
        data.putDouble("temperature", -1 + random.nextFloat()*3);
        data.putBoolean("has_precipitation", PROVIDER.roll(random, "has_precipitation"));
        data.putString("temperature_modifier", roll("temperature_modifier_frozen") ? "frozen" : "none");
        data.putDouble("downfall", random.nextDouble());
        data.put("effects", randomEffects());
        data.put("spawners", (new RandomMobsList(this)).asData());
        data.put("spawn_costs", spawnCosts());
        data.put("features", (new RandomFeaturesList(this)).data);
        data.put("carvers", carvers());
        CommonIO.write(data, dim.getStoragePath() + "/worldgen/biome", name + ".json");
    }

    boolean roll(String key) {
        return PROVIDER.roll(random, key);
    }

    void rollAndPutSafe(NbtCompound res, String key, NbtElement randomSound) {
        if (roll(key)) {
            if (randomSound != null && !randomSound.asString().isBlank())
                res.put(key, randomSound);
        }
    }

    public NbtInt randomColor() {
        return NbtInt.of(random.nextInt(16777216));
    }
    public NbtList randomDustColor() {
        NbtList res = new NbtList();
        res.add(NbtDouble.of(random.nextDouble()));
        res.add(NbtDouble.of(random.nextDouble()));
        res.add(NbtDouble.of(random.nextDouble()));
        return res;
    }
    public NbtList randomEntityEffectColor() {
        NbtList res = randomDustColor();
        res.add(NbtDouble.of(random.nextDouble()));
        return res;
    }

    NbtString randomSound(){
        return NbtString.of(PROVIDER.randomName(random, ConfigType.SOUNDS));
    }

    NbtCompound randomEffects() {
        NbtCompound res = new NbtCompound();
        res.put("fog_color", randomColor());
        res.put("sky_color", randomColor());
        res.put("water_color", randomColor());
        res.put("water_fog_color", randomColor());
        if (random.nextBoolean()) res.put("foliage_color", randomColor());
        if (random.nextBoolean()) res.put("grass_color", randomColor());
        colors = res.copy();
        if (random.nextBoolean()) res.put("grass_color_modifier", NbtString.of(random.nextBoolean() ? "dark_forest" : "swamp"));
        if (roll("use_particles")) res.put("particle", randomParticle());
        rollAndPutSafe(res, "ambient_sound", randomSound());
        rollAndPutSafe(res, "mood_sound", randomMoodSound());
        rollAndPutSafe(res, "additions_sound", randomAdditionSound());
        if (roll("music")) res.put("music", randomMusic());
        return res;
    }

    NbtCompound randomMoodSound() {
        NbtCompound res = new NbtCompound();
        var sound = randomSound();
        if (sound.asString().isEmpty()) {
            return null;
        }
        res.put("sound", sound);
        res.putInt("tick_delay", random.nextInt(6000));
        res.putInt("block_search_extent", random.nextInt(32));
        res.putDouble("offset", random.nextDouble()*8);
        return res;
    }

    NbtCompound randomAdditionSound(){
        NbtCompound res = new NbtCompound();
        var sound = randomSound();
        if (sound.asString().isEmpty()) {
            return null;
        }
        res.put("sound", sound);
        res.putDouble("tick_chance", random.nextExponential()*0.01);
        return res;
    }

    NbtCompound randomMusic(){
        NbtCompound res = new NbtCompound();
        res.put("sound", NbtString.of(PROVIDER.randomName(random, ConfigType.MUSIC)));
        int a = random.nextInt(0, 12000);
        int b = random.nextInt(0, 24000);
        res.putInt("min_delay", Math.min(a,b));
        res.putInt("max_delay", Math.max(a,b));
        res.putBoolean("replace_current_music", random.nextBoolean());
        return res;
    }

    NbtCompound randomParticle(){
        NbtCompound res = new NbtCompound();
        res.putFloat("probability", (float)(random.nextExponential()*0.01));
        res.put("options", particleOptions());
        return res;
    }

    NbtCompound particleOptions() {
        NbtCompound res = new NbtCompound();
        String particle = PROVIDER.randomName(random, ConfigType.PARTICLES);
        res.putString("type", particle);
        switch(particle) {
            case "minecraft:block", "minecraft:block_marker", "minecraft:dust_pillar", "minecraft:falling_dust" -> {
                NbtCompound value = new NbtCompound();
                value.putString("Name", PROVIDER.randomName(random, ConfigType.ALL_BLOCKS));
                res.put("block_state", value);
                return res;
            }
            case "minecraft:item" -> {
                NbtCompound value = new NbtCompound();
                value.putString("id", PROVIDER.randomName(random, ConfigType.ITEMS));
                res.put("item", value);
                return res;
            }
            case "minecraft:dust" -> {
                res.put("color", randomDustColor());
                res.putFloat("scale", random.nextFloat());
                return res;
            }
            case "minecraft:dust_color_transition" -> {
                res.put("from_color", randomDustColor());
                res.put("to_color", randomDustColor());
                res.putFloat("scale", random.nextFloat());
                return res;
            }
            case "minecraft:entity_effect" -> {
                res.put("color", randomEntityEffectColor());
                return res;
            }
            case "minecraft:sculk_charge" -> {
                res.putFloat("roll", (float)(random.nextFloat()*Math.PI));
                return res;
            }
            case "minecraft:shriek" -> {
                res.putInt("delay", random.nextInt(500));
                return res;
            }
            case "minecraft:vibration" -> {
                NbtCompound destination = new NbtCompound();
                destination.putString("type", "block");
                NbtList pos = new NbtList();
                for (int ii = 0; ii < 3; ii+=1) pos.add(NbtInt.of(random.nextInt(20000)));
                destination.put("pos", pos);
                res.put("destination", destination);
                res.putInt("arrival_in_ticks", random.nextInt(20000));
                return res;
            }
        }
        return res;
    }

    NbtCompound addMob(String category, boolean add) {
        NbtCompound mob = new NbtCompound();
        String mobname = PROVIDER.randomName(random, ConfigType.byName(category));
        mob.putString("type", mobname);
        if (add) mobs.add(mobname);
        mob.putInt("weight", 1 + random.nextInt(20));
        int a = 1 + random.nextInt(6);
        int b = 1 + random.nextInt(6);
        mob.putInt("minCount", Math.min(a, b));
        mob.putInt("maxCount", Math.max(a, b));
        return mob;
    }

    NbtCompound spawnCosts() {
        NbtCompound res = new NbtCompound();
        for (String mob: mobs) {
            NbtCompound mobData = new NbtCompound();
            mobData.putDouble("energy_budget", random.nextDouble()*0.6);
            mobData.putDouble("charge", 0.5 + random.nextDouble()*0.4);
            res.put(mob, mobData);
        }
        return res;
    }

    NbtCompound carvers() {
        NbtCompound res = new NbtCompound();
        NbtList air = new NbtList();
        if (PROVIDER.roll(random, "use_random_cave")) air.add(NbtString.of((new RandomCarver(this, true)).fullname));
        if (PROVIDER.roll(random, "use_random_canyon")) air.add(NbtString.of((new RandomCarver(this, false)).fullname));
        PROVIDER.registry.get(ConfigType.CARVERS).getAllNames(random::nextDouble).forEach(s -> air.add(NbtString.of(s)));
        res.put("air", air);
        return res;
    }
}
