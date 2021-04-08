package com.beetmacol.mc.dimenager;

import com.beetmacol.mc.dimenager.config.DimenagerConfiguration;
import com.beetmacol.mc.dimenager.dimensions.DimensionRepository;
import com.beetmacol.mc.dimenager.dimensiontypes.DimensionTypeRepository;
import com.beetmacol.mc.dimenager.generators.DefaultGeneratorTypeLoader;
import com.beetmacol.mc.dimenager.generators.GeneratorRepository;
import com.beetmacol.mc.dimenager.generators.VoidGeneratorType;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dimenager {
	public static final String MOD_ID = "dimenager";
	public static final Logger LOGGER = LogManager.getLogger("Dimenager");

	public static DimenagerConfiguration dimenagerConfiguration;

	public static DimensionRepository dimensionRepository;
	public static DimensionTypeRepository dimensionTypeRepository;
	public static GeneratorRepository generatorRepository;

	public static DefaultGeneratorTypeLoader defaultGeneratorTypeLoader;
	public static RegistryReadOps<JsonElement> registryReadOps;

	public static void init() {
		dimenagerConfiguration = DimenagerConfiguration.readConfiguration();
		Registry.register(Registry.CHUNK_GENERATOR, VoidGeneratorType.IDENTIFIER, VoidGeneratorType.CODEC);

		defaultGeneratorTypeLoader = new DefaultGeneratorTypeLoader();
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(defaultGeneratorTypeLoader);
	}
}
