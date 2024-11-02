package net.lerariemann.infinity;

import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.features.ModFeatures;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.structure.ModStructureType;
import net.lerariemann.infinity.var.*;
import net.lerariemann.infinity.util.ConfigManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

import java.nio.file.Path;

public class InfinityMod {
	public static final String MOD_ID = "infinity";
	public static Path invocationLock = Path.of("config/infinity/modular/invocation.lock");
	public static final Identifier WORLD_ADD = getId("reload_worlds");
	public static final Identifier SHADER_RELOAD = getId("reload_shader");
	public static final Identifier STARS_RELOAD = getId("reload_stars");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	public static Identifier getId(String value){
		return Identifier.of(MOD_ID, value);
	}


	public static void init() {
		ConfigManager.unpackDefaultConfigs();
		ModBlocks.registerModBlocks();
		ModItems.registerModItems();
		ModBlockEntities.registerBlockEntities();
		ModEntities.registerEntities();
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
}
