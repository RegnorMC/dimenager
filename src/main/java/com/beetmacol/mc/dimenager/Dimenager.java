package com.beetmacol.mc.dimenager;

import com.beetmacol.mc.dimenager.config.DimenagerConfiguration;
import com.beetmacol.mc.dimenager.dimensions.DimensionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dimenager {
	public static final String MOD_ID = "dimenager";
	public static final Logger LOGGER = LogManager.getLogger("Dimeanger Mod");
	public static DimenagerConfiguration dimenagerConfiguration;
	public static DimensionRepository dimensionRepository;

	public static void init() {
		dimenagerConfiguration = DimenagerConfiguration.readConfiguration();
	}
}
