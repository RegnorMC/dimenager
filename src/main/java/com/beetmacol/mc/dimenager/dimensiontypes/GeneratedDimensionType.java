package com.beetmacol.mc.dimenager.dimensiontypes;

import com.beetmacol.mc.dimenager.GeneratedItem;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public class GeneratedDimensionType extends GeneratedItem {
	private final JsonObject settings;

	protected GeneratedDimensionType(ResourceLocation identifier, Path generatedDirectory, JsonObject settings) {
		super(identifier, generatedDirectory, "dimension_type");
		this.settings = settings;
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
}
