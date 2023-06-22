package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.apache.logging.log4j.LogManager;

public class RandomDisk extends RandomisedFeature {
    String target;
    public RandomDisk(RandomFeaturesList parent) {
        super(parent, "disk");
        for (String s: daddy.underwater.keySet()) LogManager.getLogger().info(s);
        target = daddy.underwater.get(parent.parent.fullname).getString("Name");
        //if (target == null) target = "minecraft:dirt";
        id = type = "disk";
        save(daddy.default_fluid.getString("Name"));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound block = PROVIDER.randomBlock(random, "full_blocks");
        config.putInt("half_height", random.nextInt(5));
        addRandomIntProvider(config, "radius", 0, 9);
        NbtCompound targets = new NbtCompound();
        boolean awt = PROVIDER.roll(random, "ores_spawn_everywhere");
        targets.putString("type", awt ? "true" : "matching_blocks");
        if (!awt) {
            NbtList blocks = new NbtList();
            blocks.add(NbtString.of(target));
            blocks.add(NbtString.of(block.getString("Name")));
            targets.put("blocks", blocks);
        }
        config.put("target", targets);
        NbtCompound state_provider = new NbtCompound();
        state_provider.put("fallback", RandomProvider.blockToProvider(block));
        state_provider.put("rules", new NbtList());
        config.put("state_provider", state_provider);
        return feature(config);
    }
}
