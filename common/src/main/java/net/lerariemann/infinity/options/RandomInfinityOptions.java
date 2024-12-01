package net.lerariemann.infinity.options;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomInfinityOptions {
    NbtCompound data;
    String path;
    public RandomInfinityOptions(RandomDimension parent, boolean isEasterDim) {
        data = new NbtCompound();
        path = parent.getStoragePath();
        RandomProvider prov = parent.PROVIDER;
        if (isEasterDim && prov.easterizer.optionmap.containsKey(parent.getName())) {
            data = prov.easterizer.optionmap.get(parent.getName());
        }
        if (isEasterDim) return;

        Random r = parent.random;
        NbtCompound shader = new NbtCompound();
        if (prov.roll(r, "use_shaders")) {
            Object[] lst = genMatrix(r);
            shader = CommonIO.readCarefully(InfinityMod.utilPath + "/shader.json", lst);
        }
        data.put("shader", shader);
        //sun
        data.putFloat("solar_size", (float)(30*r.nextExponential()));
        data.putFloat("solar_tilt", (float)(360*r.nextDouble() - 180));
        //moons
        int moons = r.nextInt(1, 9);
        NbtList moonslist = new NbtList();
        for (int i = 0; i < moons; i++) {
            NbtCompound moon = new NbtCompound();
            moon.putFloat("lunar_size", (float)(20*r.nextExponential()));
            moon.putFloat("lunar_offset", (float)(r.nextDouble()));
            moon.putFloat("lunar_velocity", (float)(r.nextDouble() * 4 - 2));
            moon.putFloat("lunar_tilt_y", (float)(r.nextDouble() * 180 - 90));
            moon.putFloat("lunar_tilt_z", (float)(r.nextDouble() * 180 - 90));
            moon.putInt("lunar_tint", r.nextInt(16777216));
            moonslist.add(moon);
        }
        data.put("moons", moonslist);
        //stars
        data.putInt("num_stars", r.nextInt(1000, 4000));
        data.putFloat("star_size_base", (float)(0.1 + r.nextDouble()*0.3));
        data.putFloat("star_size_modifier", (float)(0.03*r.nextExponential()));
        data.putFloat("stellar_velocity", (float)(r.nextDouble() * 4 - 2));
        data.putFloat("stellar_tilt_y", (float)(r.nextDouble() * 180 - 90));
        data.putFloat("stellar_tilt_z", (float)(r.nextDouble() * 180 - 90));
        float a = r.nextFloat();
        float b = r.nextFloat();
        data.putFloat("star_brightness_day", Math.min(a, b));
        data.putFloat("star_brightness_night", Math.max(a, b));
        //other stuff
        data.putDouble("time_scale", timeScale(r));
        data.putDouble("mavity", prov.roll(r, "use_mavity") ? mavity(r) : 1.0);
        if (prov.roll(r, "pitch_shift")) data.put("pitch_shift", pitchShift(r));
        if (prov.roll(r, "give_effect")) data.put("effect", effect(r, prov));
        data.put("iridescent_map", iridMap(r));
    }

    public static double timeScale(Random r) {
        double d = r.nextDouble();
        if (d < 0.1) return r.nextDouble();
        if (d < 0.5) return 1.0;
        if (d < 0.95) return r.nextExponential()*3;
        return r.nextExponential()*30;
    }

    public static double mavity(Random r) {
        double d = r.nextDouble();
        if (d < 0.8) return r.nextDouble();
        return 1 / (0.95*r.nextDouble() + 0.05);
    }

    public void save() {
        CommonIO.write(data, path, "options.json");
    }

    static Object[] genMatrix(Random r) {
        List<Float> points = new ArrayList<>();
        float scale = 2 + r.nextFloat();
        points.add(0.0f);
        points.add(scale);
        for (int i = 0; i < 8; i++) points.add(scale * r.nextFloat());
        Collections.sort(points);
        Object[] res = new Object[9];
        for (int i = 0; i < 9; i++) {
            res[i] = points.get(i+1) - points.get(i);
        }
        return res;
    }

    public static NbtCompound pitchShift(Random r) {
        NbtCompound comp = new NbtCompound();
        int i = r.nextInt(0, 2);
        switch (i) {
            case 0 -> {
                comp.putString("type", "constant");
                comp.putFloat("value", r.nextFloat(0.5f, 2.0f));
                return comp;
            }
            case 1 -> {
                comp.putString("type", "add");
                comp.putFloat("value", r.nextFloat(-1.0f, 1.0f));
                return comp;
            }
        }
        return comp;
    }

    public static NbtCompound effect(Random r, RandomProvider provider) {
        NbtCompound res = new NbtCompound();
        String effect = ((NbtCompound)provider.compoundRegistry.get("effects").getRandomElement(r)).getString("Name");
        int amplifier = Math.min(5, (int)(0.5*r.nextExponential()));
        res.putString("id", effect);
        res.putInt("amplifier", amplifier);
        return res;
    }

    public static NbtCompound iridMap(Random r) {
        NbtCompound res = new NbtCompound();
        res.putString("type", switch (r.nextInt(4)) {
            case 0 -> "static";
            case 1 -> "linear";
            case 2 -> "circles";
            default -> "noise";
        });
        return res;
    }
}
