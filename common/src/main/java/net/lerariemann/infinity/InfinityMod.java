package net.lerariemann.infinity;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.features.ModFeatures;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.structure.ModStructureType;
import net.lerariemann.infinity.var.*;
import net.lerariemann.infinity.util.ConfigManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

import java.nio.file.Path;

public class InfinityMod {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger("Infinite Dimensions");
	public static final Identifier WORLD_ADD = getId("reload_worlds");
	public static final Identifier SHADER_RELOAD = getId("reload_shader");
	public static final Identifier STARS_RELOAD = getId("reload_stars");
	public static Path invocationLock = Path.of("config/infinity/modular/invocation.lock");
	public static Path rootResPath;
	public static Path utilPath = Path.of("config/infinity/.util");
	static {
		ModContainer mc = FabricLoader.getInstance().getModContainer(InfinityMod.MOD_ID).orElse(null);
		assert mc != null;
		rootResPath = mc.getRootPaths().get(0);
	}

	public static Identifier getId(String value){
		return Identifier.of(MOD_ID, value);
	}

	public static void init() {
		ConfigManager.updateInvocationLock();
		ConfigManager.unpackDefaultConfigs();
		ModEntities.registerEntities();
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModBlockEntities.registerBlockEntities();
		ModPoi.registerPoi();
		ModCommands.registerCommands();
		ModDensityFunctionTypes.registerFunctions();
		ModMaterialConditions.registerConditions();
		ModMaterialRules.registerRules();
		ModPlacementModifiers.registerModifiers();
		ModStructureType.registerStructures();
		ModSounds.registerSounds();
		ModFeatures.registerFeatures();
		ModStats.registerStats();
		ModCriteria.registerCriteria();
	}

	public static boolean isInfinity(World w) {
		return isInfinity(w.getRegistryKey());
	}
	public static boolean isInfinity(RegistryKey<World> key) {
		return key.getValue().getNamespace().equals(MOD_ID);
	}
}