package net.lerariemann.infinity.util.core;

import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface WeighedStructure {
    String getName(double d);
    double getStatsum();
    List<String> getAllNames(Supplier<Double> d);

    default boolean hasData(String name) {
        return false;
    }
    default NbtCompound getData(String name) {
        NbtCompound data = new NbtCompound();
        data.putString("Name", name);
        return data;
    }
    default NbtCompound getElement(String name, Function<String, NbtCompound> converter) {
        if (hasData(name)) {
            return getData(name);
        }
        return converter.apply(name);
    }
    default List<String> getAllNames() {
        return getAllNames(() -> 0d);
    }

    class Empty implements WeighedStructure {
        String name;
        public Empty(String name) {
            this.name = name;
        }
        @Override
        public String getName(double d) {
            return name;
        }
        @Override
        public double getStatsum() {
            return 0;
        }
        @Override
        public List<String> getAllNames(Supplier<Double> d) {
            List<String> res = new ArrayList<>();
            res.add(name);
            return res;
        }
    }

    class Leaf implements WeighedStructure {
        public String name;
        double weight;

        public Leaf(NbtCompound comp) {
            name = comp.getString("key");
            weight = comp.getDouble("weight");
        }

        @Override
        public String getName(double d) {
            return name;
        }
        @Override
        public double getStatsum() {
            return weight;
        }
        @Override
        public List<String> getAllNames(Supplier<Double> d) {
            List<String> res = new ArrayList<>();
            if (d.get() < weight) res.add(name);
            return res;
        }
    }

    class Recursive implements WeighedStructure {
        WeighedStructure left;
        WeighedStructure right;
        double statsum;

        public Recursive(WeighedStructure left, WeighedStructure right) {
            this.left = left;
            this.right = right;
            this.statsum = left.getStatsum() + right.getStatsum();
        }

        @Override
        public String getName(double d) {
            double i = left.getStatsum();
            if (d < i) return left.getName(d);
            return right.getName(d - i);
        }
        @Override
        public double getStatsum() {
            return statsum;
        }
        @Override
        public List<String> getAllNames(Supplier<Double> d) {
            List<String> res = left.getAllNames(d);
            res.addAll(right.getAllNames(d));
            return res;
        }
    }

    class Simple implements WeighedStructure {
        public List<String> keys;
        public List<Double> weights;
        private double statsum;

        public Simple(List<NbtCompound> data, String def) {
            keys = new ArrayList<>();
            weights = new ArrayList<>();
            statsum = 0;
            if (data.isEmpty()) add(def, 1);
            else for (NbtCompound d : data) add(d.getString("key"), d.getDouble("weight"));
        }
        public void add(String key, double weight) {
            statsum += weight;
            keys.add(key);
            weights.add(weight);
        }
        @Override
        public String getName(double d) {
            int i;
            double r = d * statsum;
            for (i = 0; i < keys.size() - 1; ++i) {
                r -= weights.get(i);
                if (r <= 0.0) break;
            }
            return keys.get(i);
        }
        @Override
        public double getStatsum() {
            return statsum;
        }

        @Override
        public List<String> getAllNames(Supplier<Double> d) {
            List<String> res = new ArrayList<>();
            for (int i = 0; i < keys.size() - 1; ++i) {
                if (d.get() < weights.get(i)) res.add(keys.get(i));
            }
            return res;
        }
    }

    class Recursor implements WeighedStructure {
        private final List<NbtCompound> data;
        public final Map<String, NbtCompound> dataMap = new HashMap<>();
        public final String def;
        public final WeighedStructure struct;
        public final boolean addsName;

        public Recursor(List<NbtCompound> data, ConfigType type) {
            this.data = data;
            this.def = type.getDef();
            this.struct = gen(0, data.size());
            this.addsName = ConfigType.addsName(type);
        }

        Leaf getLeaf(NbtCompound comp) {
            Leaf str = new Leaf(comp);
            if (comp.contains("data")) dataMap.put(str.name, comp.getCompound("data"));
            return str;
        }

        private WeighedStructure gen(int beginInd, int len) {
            if (len < 1) return new Empty(def);
            if (len == 1) return getLeaf(data.get(beginInd));
            int newLen = len / 2;
            return new Recursive(gen(beginInd, newLen), gen(beginInd + newLen, len - newLen));
        }

        @Override
        public String getName(double d) {
            return struct.getName(d * getStatsum());
        }
        @Override
        public double getStatsum() {
            return struct.getStatsum();
        }

        @Override
        public NbtCompound getData(String name) {
            NbtCompound data = hasData(name) ? dataMap.get(name) : new NbtCompound();
            if (addsName) data.putString("Name", name);
            return data;
        }
        @Override
        public boolean hasData(String name) {
            return dataMap.containsKey(name);
        }
        @Override
        public List<String> getAllNames(Supplier<Double> d) {
            return struct.getAllNames(d);
        }
    }

    class RecursorMoreStorage extends Recursor {
        public List<String> listAll;

        public RecursorMoreStorage(List<NbtCompound> data, ConfigType type) {
            super(data, type);
            listAll = new ArrayList<>();
        }
        @Override
        Leaf getLeaf(NbtCompound comp) {
            Leaf str = super.getLeaf(comp);
            listAll.add(str.name);
            return str;
        }
        @Override
        public List<String> getAllNames() {
            if (!listAll.isEmpty()) {
                List<String> res = listAll;
                listAll.clear();
                return res;
            }
            return super.getAllNames();
        }
    }
}
