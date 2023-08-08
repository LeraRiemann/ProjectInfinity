package net.lerariemann.infinity;

import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.features.ModFeatures;
import net.lerariemann.infinity.structure.ModStructureType;
import net.lerariemann.infinity.var.ModPoi;
import net.lerariemann.infinity.util.ConfigManager;
import net.lerariemann.infinity.var.ModSounds;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

public class InfinityMod implements ModInitializer {
	public static final String MOD_ID = "infinity";
	public static final Identifier WORLD_ADD = new Identifier("infinity", "reload_worlds");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModPoi.registerPoi();
		ModStructureType.registerStructures();
		ModSounds.registerSounds();
		ModFeatures.registerFeatures();
		ConfigManager.registerAllConfigs();
	}
}
