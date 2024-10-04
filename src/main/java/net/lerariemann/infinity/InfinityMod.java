package net.lerariemann.infinity;

import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.config.ModConfig;
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

import java.nio.file.Path;

public class InfinityMod implements ModInitializer {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Path invocationLock = Path.of("config/infinity/modular/invocation.lock");

    public static Identifier getId(String value){
		return Identifier.of(MOD_ID, value);
	}

	@Override
	public void onInitialize() {
		ConfigManager.unpackDefaultConfigs();
		ModConfig.load();
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
		ModPayloads.registerPayloadsServer();
	}
}
