package lostboy.seedyplace;

import lostboy.seedyplace.events.PlantDropEventListener;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedyPlaceMod implements ModInitializer {
	public static final String MOD_ID = "seedyplace";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		new PlantDropEventListener().initialize();
		LOGGER.debug("Initialized Plant Drop Event");
	}
}