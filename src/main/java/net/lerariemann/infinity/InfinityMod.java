package net.lerariemann.infinity;

import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.features.ModFeatures;
import net.lerariemann.infinity.structure.ModStructureType;
import net.lerariemann.infinity.var.*;
import net.lerariemann.infinity.util.ConfigManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

public class InfinityMod implements ModInitializer {
	public static final String MOD_ID = "infinity";
	public static final Identifier WORLD_ADD = getId("reload_worlds");
	public static final Identifier SHADER_RELOAD = getId("reload_shader");
	public static final Identifier STARS_RELOAD = getId("reload_stars");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier getId(String value){
		return new Identifier(MOD_ID, value);
	}

	@Override
	public void onInitialize() {
		ConfigManager.registerAllConfigs();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModEntities.registerEntities();
		ModEntities.registerSpawnRestrictions();
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
