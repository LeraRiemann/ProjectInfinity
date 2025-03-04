package net.lerariemann.infinity.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.BiomeBottleBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/** Common mod methods that work identically on Fabric and NeoForge.
 * @see PlatformMethods */
public interface InfinityMethods {
    String ofRandomDim = "infinity:random";
    DoublePerlinNoiseSampler sampler =
            DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -5, genOctaves(2));

    static double[] genOctaves(int octaves){
        double[] a = new double[octaves];
        Arrays.fill(a, 1);
        return a;
    }
    static double sample(BlockPos pos) {
        return sampler.sample(pos.getX(), pos.getY(), pos.getZ());
    }

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
        AtomicBoolean bl = new AtomicBoolean(false);
        world.getBiome(pos).getKey().ifPresent(key ->
                        bl.set(key.getValue().getNamespace().equals("infinity")));
        return bl.get();
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

    static void sendUnexpectedError(ServerPlayerEntity player, String type) {
        player.sendMessage(Text.translatable("error.infinity." + type + ".unexpected"));
    }

    /**
     * Convert a provided string into a dimension ID.
     * This also checks if it matches an Easter Egg dimension.
     */
    static Identifier dimTextToId(String text) {
        if (text.equals("abatised redivides"))
            return World.END.getValue();
        if (text.isEmpty())
            return InfinityMethods.getId("missingno");
        String easterId = InfinityMod.provider.easterizer.getAsEaster(text);
        if (easterId != null)
            return InfinityMethods.getId(easterId);
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

    private static float bookBoxSample(BlockPos pos, int offset) {
        return MathHelper.clamp(0.5f * (1f + (float)sampler.sample(4*pos.getX(), 4*(pos.getY() + offset), 4*pos.getZ())), 0f, 1f);
    }
    static int getBookBoxColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (pos != null) {
            float r = bookBoxSample(pos, -1000);
            float g = bookBoxSample(pos, 0);
            float b = bookBoxSample(pos, 1000);
            return MathHelper.packRgb(r, g, b);
        }
        return 16777215;
    }

    static int getOverlayColorFromComponents(ItemStack stack, int layer) {
        if (stack.getNbt() != null && layer == 1) {
            if (stack.getItem().equals(ModItems.TRANSFINITE_KEY.get()))
                return stack.getNbt().getInt("key_color");
            else if (stack.getItem().equals(ModItems.BIOME_BOTTLE_ITEM.get())) {
                return stack.getNbt().getCompound("BlockEntityTag").getInt("Color");
            }
            else if (stack.getItem().equals(ModItems.F4.get())) {
                return stack.getNbt().getInt(ModComponentTypes.KEY_COLOR);
            }
        }
        return 0xFFFFFF;
    }

    static int getPortalItemColor(ItemStack stack, int layer) {
        return 0x00FFFF;
    }

    /**
     * For use in color providers with blocks which the block entity sets color for.
     */
    static int getBlockEntityColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity be) {
                Object j = be.getTint();
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

    static int getKeyColorFromId(Identifier id) {
        if(id.getNamespace().equals(InfinityMod.MOD_ID) && id.getPath().contains("generated_"))
            return Math.toIntExact(getNumericFromId(id));
        return 0;
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

    static MutableText getDimensionNameAsText(Identifier dimension) {
        String name = dimension.toString();
        // Randomly generated dimensions.
        if (name.contains("infinity:generated_"))
            return Text.translatable("tooltip.infinity.key.generated")
                    .append(name.replace("infinity:generated_", ""));
        if (name.equals(InfinityMethods.ofRandomDim))
            return Text.translatable("tooltip.infinity.key.randomise");
        // All other dimensions.
        return Text.translatableWithFallback(
                Util.createTranslationKey("dimension", dimension),
                InfinityMethods.formatAsTitleCase(dimension.getPath()));
    }

    /**
    * Creates a fallback for texts without translation by replacing underscores
     * with spaces and formatting the text as Title Case.
     */
    static String formatAsTitleCase(String text) {
        text = text.replaceAll("[_./]", " ");
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
        return RandomProvider.rule("chaosMobsEnabled");
    }

    static boolean longArithmeticEnabled() {
        return RandomProvider.rule("longArithmeticEnabled");
    }

    static boolean isCreateLoaded() {
        if (Platform.isModLoaded("create")) {
            return Platform.getMod("create").getVersion().charAt(0) != '5';
        }
        return false;
    }
}
