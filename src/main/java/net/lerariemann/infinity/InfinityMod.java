package net.lerariemann.infinity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.poi.ModPoi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.ModBlockEntities;

import java.io.IOException;

public class InfinityMod implements ModInitializer {
	public static final String MOD_ID = "infinity";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModPoi.registerPoi();
		try {
			RandomDimension d = new RandomDimension(2, new RandomProvider("config/"+InfinityMod.MOD_ID + "/"), "saves/New World");
		} catch (IOException | CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
