package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

public class RandomEndGateway extends RandomisedFeature {
    public RandomEndGateway(RandomFeaturesList parent) {
        super(parent, "gateway");
        id = "end_gateway";
        save_with_placement();
    }

    void placement() {
        int a = daddy.min_y + random.nextInt(daddy.height);
        int b = daddy.min_y + random.nextInt(daddy.height);
        placement_floating(random.nextInt(1,33), a, b);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putBoolean("exact", PROVIDER.roll(random, "exact_gateways"));
        NbtList exit = new NbtList();
        exit.add(NbtInt.of(random.nextInt(-2048, 2048)));
        exit.add(NbtInt.of(random.nextInt(daddy.min_y, daddy.min_y + daddy.height)));
        exit.add(NbtInt.of(random.nextInt(-2048, 2048)));
        config.put("exit", exit);
        return feature(config);
    }
}
