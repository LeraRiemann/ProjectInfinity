package net.lerariemann.infinity;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.dimensions.RandomText;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.features.ModFeatures;
import net.lerariemann.infinity.iridescence.ModStatusEffects;
import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.structure.ModStructureTypes;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.var.*;
import net.lerariemann.infinity.util.ConfigManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

import java.nio.file.Path;

public class InfinityMod {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger("Infinite Dimensions");
	public static Path invocationLock = Path.of("config/infinity/modular/invocation.lock");
	public static Path rootConfigPath;
	public static Path utilPath = Path.of("config/infinity/.util");
	public static RandomProvider provider;

	public static void updateProvider(MinecraftServer server) {
		RandomProvider p = new RandomProvider("config/" + InfinityMod.MOD_ID + "/",
				server.getSavePath(WorldSavePath.DATAPACKS).toString() + "/" + InfinityMod.MOD_ID);
		p.kickGhostsOut(server.getRegistryManager());
		provider = p;
		if (!((MinecraftServerAccess)server).infinity$needsInvocation()) ModMaterialRules.RandomBlockMaterialRule.setProvider(p);
	}

	public static Identifier getId(String value){
		return Identifier.of(MOD_ID, value);
	}

	public static void init() {
		rootConfigPath = PlatformMethods.getRootConfigPath();
		ConfigManager.updateInvocationLock();
		ConfigManager.unpackDefaultConfigs();
		ModItemFunctions.registerItemFunctions();
		ModEntities.registerEntities();
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModStatusEffects.registerModEffects();
		ModBlockEntities.registerBlockEntities();
		ModPoi.registerPoi();
		ModCommands.registerCommands();
		ModDensityFunctionTypes.registerFunctions();
		ModMaterialConditions.registerConditions();
		ModMaterialRules.registerRules();
		ModPlacementModifiers.registerModifiers();
		ModStructureTypes.registerStructures();
		ModSounds.registerSounds();
		ModFeatures.registerFeatures();
		ModStats.registerStats();
		ModCriteria.registerCriteria();
		RandomText.walkPaths();
		provider = new RandomProvider("config/" + InfinityMod.MOD_ID + "/");
		InfinityOptions.defaultShader = CommonIO.read(InfinityMod.utilPath + "/default_shader.json");
	}

	public static boolean isInfinity(World w) {
		return isInfinity(w.getRegistryKey());
	}
	public static boolean isInfinity(RegistryKey<World> key) {
		return key.getValue().getNamespace().equals(MOD_ID);
	}
}