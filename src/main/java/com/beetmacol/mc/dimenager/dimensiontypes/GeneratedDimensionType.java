package com.beetmacol.mc.dimenager.dimensiontypes;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.GeneratedItem;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.nio.file.Path;

public class GeneratedDimensionType extends GeneratedItem {
	private JsonObject settings;
	private DimensionType realType;

	protected GeneratedDimensionType(ResourceLocation identifier, Path generatedDirectory, JsonObject settings) {
		super(identifier, generatedDirectory, "dimension_type");
		this.settings = settings;
		this.realType = DimensionType.DIRECT_CODEC
				.decode(Dimenager.registryReadOps, settings)
				.getOrThrow(false, s -> Dimenager.LOGGER.error("Could not deserialize dimension type with id '" + identifier + "': " + s))
				.getFirst();
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.add("settings", settings);
		return json;
	}

	public JsonObject getSettings() {
		return settings;
	}

	public DimensionType getRealType() {
		return realType;
	}

	public void setSettings(JsonObject settings) {
		this.realType = DimensionType.DIRECT_CODEC
				.decode(Dimenager.registryReadOps, settings)
				.getOrThrow(false, s -> Dimenager.LOGGER.error("Could not deserialize dimension type with id '" + getIdentifier() + "' after settings modification: " + s))
				.getFirst();
		this.settings = settings;
		saveToFile();
	}
}
