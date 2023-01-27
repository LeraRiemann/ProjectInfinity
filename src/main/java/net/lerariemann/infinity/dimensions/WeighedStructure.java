package net.lerariemann.infinity.dimensions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WeighedStructure<T> {
    public List<T> keys;
    public List<Double> weights;
    private double statsum;

    WeighedStructure() {
        keys = new ArrayList<T>();
        weights = new ArrayList<Double>();
        statsum = 0;
    }

    public WeighedStructure(List<Pair<T, Double>> values) {
        for (Pair<T, Double> value: values) {
            add(value);
        }
    }

    public void add(Pair<T, Double> value) {
        add(value.getLeft(), value.getRight());
    }

    public void add(T key, double weight) {
        statsum += weight;
        keys.add(key);
        weights.add(weight);
    }

    double sum() {
        return statsum;
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
}
