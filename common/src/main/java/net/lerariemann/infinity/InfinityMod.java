package net.lerariemann.infinity;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.dimensions.RandomText;
import net.lerariemann.infinity.registry.core.*;
import net.lerariemann.infinity.registry.var.*;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.util.ConfigManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Random;

public class InfinityMod {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger("Infinite Dimensions");
	public static Path invocationLock = Path.of("config/infinity/modular/invocation.lock");
	public static Path rootConfigPath;
	public static Path utilPath = Path.of("config/infinity/.util");
	public static RandomProvider provider;
	public static Random random = new Random(); //do not use this in dimgen, only in emergent block behaviour

	public static void updateProvider(MinecraftServer server) {
		RandomProvider p = new RandomProvider("config/" + InfinityMod.MOD_ID + "/",
				server.getSavePath(WorldSavePath.DATAPACKS).toString() + "/" + InfinityMod.MOD_ID);
		p.kickGhostsOut(server.getRegistryManager());
		provider = p;
		if (!((MinecraftServerAccess)server).infinity$needsInvocation()) ModMaterialRules.RandomBlockMaterialRule.setProvider(p);
	}

	public static void init() {
		rootConfigPath = PlatformMethods.getRootConfigPath();
		ConfigManager.updateInvocationLock();
		ConfigManager.unpackDefaultConfigs();
		ModComponentTypes.registerComponentTypes();
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
		ModPayloads.registerPayloadsServer();
		RandomText.walkPaths();
		provider = new RandomProvider("config/" + InfinityMod.MOD_ID + "/");
	}
}
