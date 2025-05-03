package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Optional;

public class RandomBonusChestFeature extends Feature<RandomBonusChestFeature.Config> {
    public RandomBonusChestFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();

        BlockPos blockPos = context.getOrigin();
        if (structureWorldAccess.isAir(blockPos) || structureWorldAccess.getBlockState(blockPos).getCollisionShape(structureWorldAccess, blockPos).isEmpty()) {
            BlockState state = context.getConfig().block.orElseGet(Blocks.CHEST::getDefaultState);
            structureWorldAccess.setBlockState(blockPos, state, 2);
            Identifier id = context.getConfig().loot;
            if (id.toString().equals("infinity:random")) id = Identifier.of(InfinityMod.provider.randomName(context.getRandom(), ConfigType.LOOT_TABLES));
            LootableInventory.setLootTable(structureWorldAccess, random, blockPos, RegistryKey.of(RegistryKeys.LOOT_TABLE, id));
            BlockState blockState = Blocks.TORCH.getDefaultState();

            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos2 = blockPos.offset(direction);
                if (blockState.canPlaceAt(structureWorldAccess, blockPos2)) {
                    structureWorldAccess.setBlockState(blockPos2, blockState, 2);
                }
            }

            return true;
        }
        return false;
    }

    public record Config(Identifier loot, Optional<BlockState> block) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (Identifier.CODEC.fieldOf("loot")).forGetter(a -> a.loot),
                (BlockState.CODEC.optionalFieldOf("block")).forGetter(a -> a.block)).apply(
                instance, Config::new));
    }
}
