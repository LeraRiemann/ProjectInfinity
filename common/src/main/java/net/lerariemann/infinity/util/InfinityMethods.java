package net.lerariemann.infinity.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.architectury.platform.Platform;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.TintableBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.nio.charset.StandardCharsets;

import static net.lerariemann.infinity.InfinityModClient.sampler;

// Methods that are not dependent on ExpectPlatform, and work identically on both Fabric and NeoForge.
public interface InfinityMethods {
    String ofRandomDim = "infinity:random";

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
    static boolean isInfinity(RegistryKey<World> key) {
        return key.getValue().getNamespace().equals(InfinityMod.MOD_ID);
    }
    static boolean isBiomeInfinity(WorldAccess world, BlockPos pos) {
        return world.getBiome(pos).getIdAsString().contains("infinity");
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

    static void sendS2CPayload(ServerPlayerEntity entity, CustomPayload payload) {
        ServerPlayNetworking.send(entity, payload);
    }

    static double sample(int x, int y, int z) {
        return sampler.sample(x, y, z);
    }

    static int properMod(int a, int b) {
        int res = a%b;
        return (res >= 0) ? res : b + res;
    }

    static void sendUnexpectedError(ServerPlayerEntity player, String type) {
        player.sendMessage(Text.translatable("error.infinity." + type + ".unexpected"));
    }

    /**
     * Convert a provided string into a dimension ID.
     * This also checks if it matches an Easter Egg dimension.
     */
    static Identifier dimTextToId(String text) {
        if (text.equals("abatised redivides")) return World.END.getValue();
        if (text.isEmpty()) return InfinityMethods.getId("missingno");
        if (InfinityMod.provider.easterizer.isEaster(text, InfinityMod.provider) && !text.equals("missingno")) return InfinityMethods.getId(text);
        return InfinityMethods.getDimId(getDimensionSeed(text));
    }

    static boolean isTimebombed(ServerWorld world) {
        return ((Timebombable)world).infinity$isTimebombed();
    }

    /**
     * Check if a dimension exists and has not been timebombed.
     */
    static boolean dimExists(ServerWorld world) {
        return (world != null && !isTimebombed(world));
    }

    /**
     * Hashes text into dimension ID.
     */
    static long getDimensionSeed(String text) {
        HashCode f = Hashing.sha256().hashString(text + InfinityMod.provider.salt, StandardCharsets.UTF_8);
        return InfinityMethods.longArithmeticEnabled() ? f.asLong() & Long.MAX_VALUE : f.asInt() & Integer.MAX_VALUE;
    }

    static long getNumericFromId(Identifier id) {
        String dimensionName = id.getPath();
        String numericId = dimensionName.substring(dimensionName.lastIndexOf("_") + 1);
        long i;
        try {
            i = Long.parseLong(numericId);
        } catch (Exception e) {
            /* Simply hash the name if it isn't of "generated_..." format. */
            i = getDimensionSeed(numericId);
        }
        return i;
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
        int color = stack.getComponents().getOrDefault(ModComponentTypes.COLOR.get(), 0xFFFFFF);
        if (layer == 1) {
            return ColorHelper.Argb.fullAlpha(color);
        }
        return ColorHelper.Argb.fullAlpha(0xFFFFFF);
    }
    static int getItemColorFromComponents(ItemStack stack, int layer) {
        int color = stack.getComponents().getOrDefault(ModComponentTypes.COLOR.get(), 0xFFFFFF);
        return ColorHelper.Argb.fullAlpha(color);
    }

    /**
     * Gets an Infinity Portal's item colour - hard set as a light blue.
     */
    static int getBlockEntityColor(ItemStack stack, int layer) {
        return ColorHelper.Argb.fullAlpha(-16717057);
    }

    /**
     * For use in color providers with blocks which the block entity sets color for.
     */
    static int getBlockEntityColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TintableBlockEntity be) {
                return be.getTint() & 0xFFFFFF;
            }
        }
        return 0xFFFFFF;
    }

    static long getRandomSeed(java.util.Random random) {
        return longArithmeticEnabled() ? random.nextLong() : random.nextInt();
    }

    static long getRandomSeed(Random random) {
        return longArithmeticEnabled() ? random.nextLong() : random.nextInt();
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

    static boolean chaosMobsEnabled() {
        return InfinityMod.provider.rule("chaosMobsEnabled");
    }

    static boolean longArithmeticEnabled() {
        return InfinityMod.provider.rule("longArithmeticEnabled");
    }
}
