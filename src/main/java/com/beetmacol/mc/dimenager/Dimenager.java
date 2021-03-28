package com.beetmacol.mc.dimenager;

import com.beetmacol.mc.dimenager.config.DimenagerConfiguration;
import com.beetmacol.mc.dimenager.dimensions.DimensionRepository;
import com.beetmacol.mc.dimenager.dimensiontypes.DimensionTypeRepository;
import com.beetmacol.mc.dimenager.generators.GeneratorRepository;
import com.beetmacol.mc.dimenager.generators.VoidGeneratorType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dimenager {
	public static final String MOD_ID = "dimenager";
	public static final Logger LOGGER = LogManager.getLogger("Dimenager Mod");
	public static DimenagerConfiguration dimenagerConfiguration;
	public static DimensionRepository dimensionRepository;
	public static DimensionTypeRepository dimensionTypeRepository;
	public static GeneratorRepository generatorRepository;

	public static void init() {
		dimenagerConfiguration = DimenagerConfiguration.readConfiguration();
		Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "void"), VoidGeneratorType.CODEC);
	}
}
