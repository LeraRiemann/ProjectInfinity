package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class RandomCrop extends RandomisedFeature {
    NbtCompound cropp;

    public RandomCrop(RandomFeaturesList parent) {
        super(parent, "crop");
        id = "block_column";
        NbtElement crop = PROVIDER.extraRegistry.get("crops").getRandomElement(random);
        cropp = (NbtCompound)crop;
        int start = daddy.sea_level - (cropp.getKeys().contains("offset") ? cropp.getInt("offset") : 1);
        boolean water = cropp.getBoolean("needsWater");
        if (water) {
            type = "crop_water";
            save(random.nextInt(16), start);
        } else {
            type = "crop";
            save(random.nextInt(16), parent.surface_block.getString("Name"));
        }
    }

    static NbtCompound blockToLayer(NbtCompound block) {
        NbtCompound layer = new NbtCompound();
        NbtCompound provider = RandomProvider.blockToProvider(block);
        layer.put("provider", provider);
        layer.putInt("height", 1);
        return layer;
    }

    @Override
    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putString("direction", "up");
        config.putBoolean("prioritize_tip", false);
        NbtList layers = new NbtList();
        boolean bl = cropp.getKeys().contains("blocks");
        for (int i = 0; bl ? (i < cropp.getInt("blocks")) : (cropp.getKeys().contains("block_" + i)); i++)
            layers.add(blockToLayer(cropp.getCompound("block_" + i)));
        layers.add(blockToLayer(RandomProvider.Block("minecraft:air")));
        config.put("layers", layers);
        NbtCompound allowedPlacement = new NbtCompound();
        allowedPlacement.putString("type", "true");
        config.put("allowed_placement", allowedPlacement);
        return feature(config);
    }
}
