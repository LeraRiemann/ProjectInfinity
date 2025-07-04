package net.lerariemann.infinity.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.block.entity.TintableBlockEntity;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
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
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Common mod methods that work identically on Fabric and NeoForge.
 * @see PlatformMethods */
@SuppressWarnings("unused")
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

    static int properMod(int a, int b) {
        int res = a%b;
        return (res >= 0) ? res : b + res;
    }

    static void sendUnexpectedError(ServerPlayerEntity player, String type) {
        player.sendMessage(Text.translatable("error.infinity." + type + ".unexpected"));
    }

    static String dimTextPreprocess(String text) {
        if (text.isEmpty()) return "missingno";
        if (RandomProvider.rule("forceLowercase")) text = text.toLowerCase();
        text = text.replaceAll("\n", " ");
        return text;
    }

    /**
     * Convert a provided string into a dimension ID.
     * This also checks if it matches an Easter Egg dimension.
     */
    static Identifier dimTextToId(String text) {
        if (text.equals("abatised redivides"))
            return World.END.getValue();
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
    static int getDiscColorFromComponents(ItemStack stack, int layer) {
        int color = getItemColorFromComponents(stack, layer);
        return layer == 0 ? color : 0xFFFFFF ^ color;
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

    static boolean deleteLevel(LevelStorage.Session session) {
        return FileUtils.deleteQuietly(session.getDirectory().path().resolve("datapacks").toFile());
    }
}
