package net.lerariemann.infinity;

import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.poi.ModPoi;
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
		ConfigManager.registerAllConfigs();
		//RandomDimension d = new RandomDimension(5, new RandomProvider("config/"+ InfinityMod.MOD_ID + "/"), "saves/New World/datapacks");
	}
}
