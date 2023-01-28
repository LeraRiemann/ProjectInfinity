package net.lerariemann.infinity.dimensions;

import java.util.ArrayList;
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
}
