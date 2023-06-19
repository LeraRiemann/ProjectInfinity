package net.lerariemann.infinity;

import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.features.ModFeatures;
import net.lerariemann.infinity.poi.ModPoi;
import net.lerariemann.infinity.util.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

public class InfinityMod implements ModInitializer {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModPoi.registerPoi();
		ModFeatures.registerFeatures();
		ConfigManager.registerAllConfigs();
	}
}
