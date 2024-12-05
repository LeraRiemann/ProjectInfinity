package net.lerariemann.infinity.util;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.entity.BiomeBottleBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;

import static net.lerariemann.infinity.InfinityModClient.sampler;

// Methods that are not dependent on ExpectPlatform, and work identically on both Fabric and NeoForge.
public interface InfinityMethods {
    /**
     * Converts a string to an identifier in the Infinite Dimensions namespace.
     */
    static Identifier getId(String value){
		return Identifier.of(InfinityMod.MOD_ID, value);
	}

    /**
     * Converts a dimension's long value to an identifier in the Infinite Dimensions namespace.
     */
    static Identifier getDimId(long value){
		return getId("generated_" + value);
	}

    /**
     * Checks if a dimension is an Infinite Dimension.
     */
    static boolean isInfinity(World w) {
        return isInfinity(w.getRegistryKey());
    }

    /**
     * Checks if a dimension is an Infinite Dimension.
     */
    static boolean isInfinity(RegistryKey<World> key) {
        return key.getValue().getNamespace().equals(InfinityMod.MOD_ID);
    }

    /**
     * Checks if a mod designed for Fabric (using - instead of _) is loaded.
     * This is most commonly used for FFAPI modules, but also applies to mods
     * like Item Descriptions or Cloth Config.
     */
    static boolean isFabricApiLoaded(String modID) {
        if (Platform.isFabric()) return Platform.isModLoaded(modID.replace("_", "-"));
        else return Platform.isModLoaded(modID.replace("-", "_"));
    }

    static double sample(int x, int y, int z) {
        return sampler.sample(x, y, z);
    }

    static int properMod(int a, int b) {
        int res = a%b;
        return (res >= 0) ? res : b + res;
    }

    /**
     * Converts a coordinate value to a "random" color.
     */
    static int posToColor(BlockPos pos) {
        double r = sample(pos.getX(), pos.getY() - 10000, pos.getZ());
        double g = sample(pos.getX(), pos.getY(), pos.getZ());
        double b = sample(pos.getX(), pos.getY() + 10000, pos.getZ());
        return (int)(256 * ((r + 1)/2)) + 256*((int)(256 * ((g + 1)/2)) + 256*(int)(256 * ((b + 1)/2)));
    }


    static int getBookBoxColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (pos != null) {
            return posToColor(pos);
        }
        return 16777215;
    }

    static int getOverlayColorFromComponents(ItemStack stack, int layer) {
        int color = 0xFFFFFF;
        if (stack.getNbt() != null) {
            if (stack.getItem().equals(ModItems.TRANSFINITE_KEY.get()))
                color = stack.getNbt().getInt("key_color");
            else if (stack.getItem().equals(ModItems.BIOME_BOTTLE_ITEM.get())) {
                color = stack.getNbt().getCompound("BlockEntityTag").getInt("Color");
            }
            if (layer == 1) {
                return color;
            }
        }
        return 0xFFFFFF;
    }

    /**
     * Gets an Infinity Portal's item colour - hard set as a light blue.
     */
    static int getInfinityPortalColor(ItemStack stack, int layer) {
        return -16717057;
    }

    /**
     * Gets an Infinity Portal's color from its block entity - for use in color providers.
     */
    static int getInfinityPortalColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity be) {
                Object j = be.getRenderData();
                if (j == null) return 0;
                return (int)j & 0xFFFFFF;
            }
        }
        return 0xFFFFFF;
    }

    /**
     * Gets a Biome Bottle's color from its block entity - for use in color providers.
     */
    static int getBiomeBottleColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BiomeBottleBlockEntity be) {
                int j = be.color;
                return j & 0xFFFFFF;
            }
        }
        return 0xFFFFFF;
    }

    static long getRandomSeed(java.util.Random random) {
        return InfinityMod.longArithmeticEnabled ? random.nextLong() : random.nextInt();
    }

    static long getRandomSeed(Random random) {
        return InfinityMod.longArithmeticEnabled ? random.nextLong() : random.nextInt();
    }

    static Identifier getRandomId(java.util.Random random) {
        return getDimId(getRandomSeed(random));
    }
    static Identifier getRandomId(Random random) {
        return getDimId(getRandomSeed(random));
    }

    /**
    * Creates a fallback for texts without translation by replacing underscores
     * with spaces and formatting the text as Title Case.
     */
    static String fallback(String text) {
        text = text.replace("_", " ");
        //i am sure java has a smarter way to do title case, but this works too
        StringBuilder newText = new StringBuilder();
        int i = 0;
        for (Character c : text.toCharArray()) {
            if (i == 0) {
                c = c.toString().toUpperCase().charAt(0);
            }
            newText.append(c);
            i++;
            if (c == ' ') {
                i = 0;
            }
        }
        return newText.toString();
    }
}
