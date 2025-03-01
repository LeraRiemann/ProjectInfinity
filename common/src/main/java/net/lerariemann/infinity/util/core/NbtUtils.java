package net.lerariemann.infinity.util.core;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Random;

import static net.lerariemann.infinity.util.core.RandomProvider.genBounds;

/** Contains various common use methods for working with {@link NbtCompound} objects. */
public interface NbtUtils {
    static String test(NbtCompound data, String key, String def) {
        return data.contains(key, NbtElement.STRING_TYPE) ? data.getString(key) : def;
    }
    static NbtCompound test(NbtCompound data, String key, NbtCompound def) {
        return data.contains(key, NbtElement.COMPOUND_TYPE) ? data.getCompound(key) : def;
    }
    static float test(NbtCompound data, String key, float def) {
        return data.contains(key, NbtElement.DOUBLE_TYPE) ? data.getFloat(key) : def;
    }
    static int test(NbtCompound data, String key, int def) {
        return data.contains(key, NbtElement.INT_TYPE) ? data.getInt(key) : def;
    }
    static double test(NbtCompound data, String key, double def) {
        return data.contains(key, NbtElement.DOUBLE_TYPE) ? data.getDouble(key) : def;
    }
    static boolean test(NbtCompound data, String key, boolean def) {
        return data.contains(key) ? data.getBoolean(key) : def;
    }

    static String elementToName(NbtElement e) {
        if (e instanceof NbtCompound) return ((NbtCompound)e).getString("Name");
        else return e.asString();
    }
    static NbtCompound nameToElement(String block) {
        NbtCompound res = new NbtCompound();
        res.putString("Name", block);
        return res;
    }
    static NbtCompound nameToFluid(String block) {
        NbtCompound res = new NbtCompound();
        res.putString("Name", block);
        res.putString("fluidName", block);
        return res;
    }

    static NbtCompound blockToSimpleStateProvider(NbtCompound block) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:simple_state_provider");
        res.put("state", block);
        return res;
    }

    static void addBounds(NbtCompound res, int lbound, int bound) {
        NbtCompound value = new NbtCompound();
        value.putInt("min_inclusive", lbound);
        value.putInt("max_inclusive", bound);
        res.put("value", value);
    }

    static void addBounds(NbtCompound res, Random random, int lbound, int bound) {
        int a = random.nextInt(lbound, bound);
        int b = random.nextInt(lbound, bound);
        addBounds(res, Math.min(a, b), Math.max(a, b));
    }

    static NbtElement randomIntProvider(Random random, int bound, boolean acceptDistributions) {
        return randomIntProvider(random, 0, bound, acceptDistributions);
    }

    static NbtElement randomIntProvider(Random random, int lbound, int bound, boolean acceptDistributions) {
        int i = random.nextInt(acceptDistributions ? 6 : 4);
        NbtCompound res = new NbtCompound();
        switch(i) {
            case 0 -> {
                res.putString("type", "constant");
                res.putInt("value", random.nextInt(lbound, bound));
                return res;
            }
            case 1, 2 -> {
                res.putString("type", i==1 ? "uniform" : "biased_to_bottom");
                addBounds(res, random, lbound, bound);
                return res;
            }
            case 4 -> {
                res.putString("type", "clamped");
                NbtCompound value = genBounds(lbound, bound);
                value.put("source", randomIntProvider(random, lbound, bound, false));
                res.put("value", value);
                return res;
            }
            case 3 -> {
                res.putString("type", "clamped_normal");
                addBounds(res, random, lbound, bound);
                NbtCompound value = genBounds(lbound, bound);
                value.putDouble("mean", lbound + random.nextDouble()*(bound-lbound));
                value.putDouble("deviation", random.nextExponential());
                res.put("value", value);
                return res;
            }
            case 5 -> {
                res.putString("type", "weighted_list");
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", randomIntProvider(random, lbound, bound, false));
                    element.putInt("weight", random.nextInt(100));
                    list.add(element);
                }
                res.put("distribution", list);
                return res;
            }
        }
        return res;
    }

    static NbtCompound randomHeightProvider(Random random, int lbound, int bound, boolean acceptDistributions, boolean trim) {
        int i = random.nextInt(acceptDistributions ? 6 : 5);
        String[] types = new String[]{"uniform", "biased_to_bottom", "very_biased_to_bottom", "trapezoid", "constant", "weighted_list"};
        NbtCompound res = new NbtCompound();
        res.putString("type", types[i]);
        switch (i) {
            case 4 -> {
                NbtCompound value = new NbtCompound();
                value.putInt("absolute", random.nextInt(lbound, bound));
                res.put("value", value);
            }
            case 0, 1, 2, 3 -> {
                NbtCompound min_inclusive = new NbtCompound();
                NbtCompound max_inclusive = new NbtCompound();
                int min, max;
                if (!trim && (i == 3)) {
                    int center = random.nextInt(lbound, bound);
                    int sigma = random.nextInt(bound-lbound);
                    min = center - sigma;
                    max = center + sigma;
                }
                else {
                    int k = random.nextInt(lbound, bound);
                    int j = random.nextInt(lbound, bound);
                    min = Math.min(k, j);
                    max = Math.max(k, j);
                }
                min_inclusive.putInt("absolute", min);
                max_inclusive.putInt("absolute", max);
                res.put("min_inclusive", min_inclusive);
                res.put("max_inclusive", max_inclusive);
                int randomBound = max - min;
                if (randomBound <= 1) {
                    InfinityMod.LOGGER.debug("Corrected random bound of: {} to 2!", randomBound);
                    randomBound = 2;
                }
                if (i==3 && random.nextBoolean()) res.putInt("plateau", random.nextInt(1, randomBound));
                else if (i!=0) res.putInt("inner", 1 + (int)Math.floor(random.nextExponential()));
            }
            case 5 -> {
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", randomHeightProvider(random, lbound, bound, false, trim));
                    element.putInt("weight", random.nextInt(100));
                    list.add(element);
                }
                res.put("distribution", list);
            }
        }
        return res;
    }

    static NbtCompound randomFloatProvider(Random random, float lbound, float bound) {
        int i = random.nextInt(3);
        String[] types = new String[]{"uniform", "clamped_normal", "trapezoid"};
        NbtCompound res = new NbtCompound();
        res.putString("type", types[i]);
        float a = random.nextFloat(lbound, bound);
        float b = random.nextFloat(lbound, bound);
        float min = Math.min(a, b);
        float max = Math.max(a, b);
        switch (i) {
            case 0 -> {
                res.putFloat("max_exclusive", max);
                res.putFloat("min_inclusive", min);
            }
            case 1 -> {
                res.putFloat("max", max);
                res.putFloat("min", min);
                res.putFloat("mean", random.nextFloat(min, max));
                res.putFloat("deviation", random.nextFloat(max - min));
            }
            case 2 -> {
                res.putFloat("max", max);
                res.putFloat("min", min);
                res.putFloat("plateau", random.nextFloat(max - min));
            }
        }
        return res;
    }
}
