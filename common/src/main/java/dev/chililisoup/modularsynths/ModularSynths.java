package dev.chililisoup.modularsynths;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.RegistrarManager;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.reg.ModBlocks;
import dev.chililisoup.modularsynths.reg.ModCreativeTabs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ModularSynths {
	public static final String MOD_ID = "modularsynths";
	public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	// grab from config
	public static final float SAMPLE_RATE = 44100; // 44.1 kHz
	public static final int SAMPLE_BUFFER_SIZE = 1024; // ~23.22ms delay (1000/(rate/buffer_size))
	public static final int MAX_DEPTH = 64; // how many synths can be chained into an output

	public static void init() {
		ModCreativeTabs.init();
		ModBlocks.init();
		ModBlockEntityTypes.init();
	}
}
