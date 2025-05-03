package net.lerariemann.infinity.dimensions.features.top_layer;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomBonusChest extends RandomisedFeature {
    boolean isFrequent;

    public RandomBonusChest(RandomFeaturesList lst) {
        super(lst, "chest");
        id = "infinity:bonus_chest";
        isFrequent = random.nextBoolean();
        savePlacement();
    }

    @Override
    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putString("loot", random.nextBoolean() ? LootTables.SPAWN_BONUS_CHEST.getValue().toString() :
                random.nextBoolean() ? "infinity:random" : PROVIDER.randomName(random, ConfigType.LOOT_TABLES));
        config.put("block", NbtUtils.nameToElement(random.nextBoolean() ? "minecraft:chest" :
                isFrequent ? "minecraft:barrel" : PROVIDER.randomName(random, ConfigType.CHESTS)));
        return feature(config);
    }

    @Override
    public NbtList placement() {
        Placement res = new Placement();
        if (isFrequent) {
            res.addCountEveryLayer(random.nextInt(1, 8));
        } else {
            res.addCountEveryLayer(1);
            res.addRarityFilter(random.nextInt(1, 8));
        }
        res.addBiome();
        return res.data;
    }
}
