package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomInfinityOptions {
    NbtCompound data;
    String path;
    public RandomInfinityOptions(RandomDimension parent, boolean bl) {
        data = new NbtCompound();
        RandomProvider prov = parent.PROVIDER;
        Random r = parent.random;
        if (bl && prov.easterizer.optionmap.containsKey(parent.fullname)) {
            data = prov.easterizer.optionmap.get(parent.fullname);
            return;
        }
        NbtCompound shader = new NbtCompound();
        if (prov.roll(r, "use_shaders")) {
            Object[] lst = genMatrix(r);
            shader = CommonIO.readCarefully(prov.configPath + "util/shader.json", lst);
        }
        data.put("shader", shader);
        data.putFloat("solar_size", (float)(30*r.nextExponential()));
        data.putFloat("lunar_size", (float)(20*r.nextExponential()));
        path = parent.getStoragePath();
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
}
