package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.util.WeighedStructure;
import net.minecraft.nbt.*;

import java.util.*;

public class RandomBiome {
    public RandomDimension parent;
    public final RandomProvider PROVIDER;
    public int id;
    public String name;
    public String fullname;
    public final Random random;
    public List<String> mobs;

    RandomBiome(int i, RandomDimension dim) {
        id = i;
        parent = dim;
        random = new Random(i);
        PROVIDER = dim.PROVIDER;
        name = "biome_" +i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        mobs = new ArrayList<>();
        NbtCompound res = new NbtCompound();
        res.putDouble("temperature", -1 + random.nextFloat()*3);
        res.putString("precipitation", PROVIDER.randomName(random, "precipitation"));
        res.putString("temperature_modifier", roll("temperature_modifier_frozen") ? "frozen" : "none");
        res.putDouble("downfall", random.nextDouble());
        res.put("effects", randomEffects());
        res.put("spawners", (new RandomMobsList(this)).asData());
        res.put("spawn_costs", spawnCosts());
        res.put("features", (new RandomFeaturesList(this)).data);
        res.put("carvers", carvers());
        CommonIO.write(res, dim.storagePath + "/worldgen/biome", name + ".json");
    }

    boolean roll(String key) {
        return PROVIDER.roll(random, key);
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

    NbtString randomSound(){
        return NbtString.of(PROVIDER.randomName(random, "sounds"));
    }

    NbtCompound randomEffects() {
        NbtCompound res = new NbtCompound();
        res.put("fog_color", randomColor());
        res.put("sky_color", randomColor());
        res.put("water_color", randomColor());
        res.put("water_fog_color", randomColor());
        if (random.nextBoolean()) res.put("foliage_color", randomColor());
        if (random.nextBoolean()) res.put("grass_color", randomColor());
        if (random.nextBoolean()) res.put("grass_color_modifier", NbtString.of(random.nextBoolean() ? "dark_forest" : "swamp"));
        if (roll("use_particles")) res.put("particle", randomParticle());
        if (roll("ambient_sound")) res.put("ambient_sound", randomSound());
        if (roll("mood_sound")) res.put("mood_sound", randomMoodSound());
        if (roll("additions_sound")) res.put("additions_sound", randomAdditionSound());
        if (roll("music")) res.put("music", randomMusic());
        return res;
    }

    NbtCompound randomMoodSound() {
        NbtCompound res = new NbtCompound();
        res.put("sound", randomSound());
        res.putInt("tick_delay", random.nextInt(6000));
        res.putInt("block_search_extent", random.nextInt(32));
        res.putDouble("offset", random.nextDouble()*8);
        return res;
    }

    NbtCompound randomAdditionSound(){
        NbtCompound res = new NbtCompound();
        res.put("sound", randomSound());
        res.putDouble("tick_chance", random.nextExponential()*0.01);
        return res;
    }

    NbtCompound randomMusic(){
        NbtCompound res = new NbtCompound();
        res.put("sound", NbtString.of(PROVIDER.randomName(random, "music")));
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
        String particle = PROVIDER.randomName(random, "particles");
        res.putString("type", particle);
        switch(particle) {
            case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust" -> {
                NbtCompound value = new NbtCompound();
                value.putString("Name", PROVIDER.randomName(random, "all_blocks"));
                res.put("value", value);
                return res;
            }
            case "minecraft:item" -> {
                NbtCompound value = new NbtCompound();
                value.putString("Name", PROVIDER.randomName(random, "items"));
                res.put("value", value);
                return res;
            }
            case "minecraft:dust" -> {
                res.put("color", randomDustColor());
                res.putFloat("scale", random.nextFloat());
                return res;
            }
            case "minecraft:dust_color_transition" -> {
                res.put("fromColor", randomDustColor());
                res.put("toColor", randomDustColor());
                res.putFloat("scale", random.nextFloat());
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
        }
        return res;
    }

    void addMob(String mobname) {
        mobs.add(mobname);
    }

    NbtCompound spawnCosts() {
        NbtCompound res = new NbtCompound();
        for (String mob: mobs) {
            NbtCompound mobData = new NbtCompound();
            mobData.putDouble("energy_budget", random.nextDouble()*0.3);
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
        WeighedStructure<String> carvers = PROVIDER.registry.get("carvers");
        for (int i = 0; i < carvers.keys.size(); i++) {
            if (random.nextDouble() < carvers.weights.get(i)) air.add(NbtString.of(carvers.keys.get(i)));
        }
        res.put("air", air);
        return res;
    }
}
