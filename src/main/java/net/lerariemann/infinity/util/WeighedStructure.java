package net.lerariemann.infinity.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeighedStructure<T> {
    public List<T> keys;
    public List<Double> weights;
    private double statsum;

    public WeighedStructure() {
        keys = new ArrayList<>();
        weights = new ArrayList<>();
        statsum = 0;
    }

    public void add(T key, double weight) {
        statsum += weight;
        keys.add(key);
        weights.add(weight);
    }

    int size() {
        return keys.size();
    }

    public T getRandomElement(Random random) {
        int i;
        double r = random.nextDouble() * statsum;
        for (i = 0; i < size() - 1; ++i) {
            r -= weights.get(i);
            if (r <= 0.0) break;
        }
        return keys.get(i);
    }

    public List<T> getAllElements(Random random) {
        List<T> res = new ArrayList<>();
        int i;
        for (i = 0; i < size() - 1; ++i) if (random.nextDouble() < weights.get(i)) res.add(keys.get(i));
        return res;
    }
}
