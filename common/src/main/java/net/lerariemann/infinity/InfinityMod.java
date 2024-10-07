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

public class InfinityMod {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier getId(String value){
		return Identifier.of(MOD_ID, value);
	}

	public static void init() {
		ConfigManager.registerAllConfigs();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModEntities.registerEntities();
		ModEntities.registerSpawnRestrictions();
//		ModPoi.registerPoi();
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
		ModPayloads.registerPayloadsServer();
	}
}
