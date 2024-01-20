package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class RandomDisk extends RandomisedFeature {
    String target;
    public RandomDisk(RandomFeaturesList parent) {
        super(parent, "disk");
        target = daddy.underwater.get(parent.parent.fullname).getString("Name");
        id = type = "disk";
        save(daddy.default_fluid.getString("fluidName"));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound blockProvider = PROVIDER.randomBlockProvider(random, "full_blocks");
        config.putInt("half_height", random.nextInt(5));
        addRandomIntProvider(config, "radius", 0, 9);
        NbtCompound targets = new NbtCompound();
        boolean awt = PROVIDER.roll(random, "ores_spawn_everywhere");
        targets.putString("type", awt ? "true" : "matching_blocks");
        if (!awt) {
            NbtList blocks = new NbtList();
            blocks.add(NbtString.of(target));
            blocks.add(NbtString.of(blockProvider.getCompound("state").getString("Name")));
            targets.put("blocks", blocks);
        }
        config.put("target", targets);
        NbtCompound state_provider = new NbtCompound();
        state_provider.put("fallback", blockProvider);
        state_provider.put("rules", new NbtList());
        config.put("state_provider", state_provider);
        return feature(config);
    }
}
