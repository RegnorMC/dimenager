package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.GeneratedItem;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.nio.file.Path;

public class GeneratedDimension extends GeneratedItem {
	private DimensionType type;
	private ResourceLocation typeIdentifier;
	private ResourceLocation generatorIdentifier; // Temporarily an identifier. Will be a Generator as soon as I create the Generator class.
	private boolean enabled;

	public GeneratedDimension(ResourceLocation identifier, Path generatedDirectory, boolean enabled, DimensionType type, ResourceLocation typeIdentifier, ResourceLocation generatorIdentifier) {
		super(identifier, generatedDirectory, "dimension");
		this.enabled = enabled;
		this.type = type;
		this.typeIdentifier = typeIdentifier;
		this.generatorIdentifier = generatorIdentifier;
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("enabled", enabled);
		json.addProperty("dimension_type", typeIdentifier.toString());
		json.addProperty("generator", generatorIdentifier.toString());
		return json;
	}

	public DimensionType getType() {
		return type;
	}

	public ResourceLocation getTypeIdentifier() {
		return typeIdentifier;
	}

	public ResourceLocation getGeneratorIdentifier() {
		return generatorIdentifier;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setType(DimensionType type, ResourceLocation typeIdentifier) {
		this.type = type;
		this.typeIdentifier = typeIdentifier;
		saveToFile();
	}

	public void setGenerator(ResourceLocation generatorIdentifier) {
		this.generatorIdentifier = generatorIdentifier;
		saveToFile();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		saveToFile();
	}
}
