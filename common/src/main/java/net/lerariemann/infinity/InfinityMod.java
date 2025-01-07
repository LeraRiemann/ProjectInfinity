package net.lerariemann.infinity;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.dimensions.RandomText;
import net.lerariemann.infinity.registry.core.*;
import net.lerariemann.infinity.registry.var.*;

import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.lerariemann.infinity.util.config.ConfigManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Random;

public class InfinityMod {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger("Infinite Dimensions");
	public static Path configPath = PlatformMethods.getConfigPath()
	public static Path utilPath = configPath.resolve(".util");
	public static Path invocationLock = configPath.resolve("modular/invocation.lock");

	public static Path rootConfigPathInJar;
	public static RandomProvider provider;
	public static Random random = new Random(); //do not use this in dimgen, only in emergent block behaviour

	public static void updateProvider(MinecraftServer server) {
		RandomProvider p = new RandomProvider(server.getSavePath(WorldSavePath.DATAPACKS).resolve(MOD_ID));
		p.kickGhostsOut(server.getRegistryManager());
		provider = p;
		if (!((MinecraftServerAccess)server).infinity$needsInvocation()) ModMaterialRules.RandomBlockMaterialRule.setProvider(p);
	}

	public static void init() {
		rootConfigPathInJar = PlatformMethods.getRootConfigPath();
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
	}
}