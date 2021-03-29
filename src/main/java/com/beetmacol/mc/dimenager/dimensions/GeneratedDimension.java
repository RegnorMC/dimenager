package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.GeneratedItem;
import com.beetmacol.mc.dimenager.generators.Generator;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.nio.file.Path;

public class GeneratedDimension extends GeneratedItem {
	private DimensionType type;
	private ResourceLocation typeIdentifier;
	private Generator generator; // Temporarily an identifier. Will be a Generator as soon as I create the Generator class.
	private boolean enabled;

	public GeneratedDimension(ResourceLocation identifier, Path generatedDirectory, boolean enabled, DimensionType type, ResourceLocation typeIdentifier, Generator generator) {
		super(identifier, generatedDirectory, "dimension");
		this.enabled = enabled;
		this.type = type;
		this.typeIdentifier = typeIdentifier;
		this.generator = generator;
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("enabled", enabled);
		json.addProperty("dimension_type", typeIdentifier.toString());
		json.addProperty("generator", generator.getIdentifier().toString());
		return json;
	}

	public DimensionType getType() {
		return type;
	}

	public ResourceLocation getTypeIdentifier() {
		return typeIdentifier;
	}

	public Generator getGenerator() {
		return generator;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setType(DimensionType type, ResourceLocation typeIdentifier) {
		this.type = type;
		this.typeIdentifier = typeIdentifier;
		saveToFile();
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
		saveToFile();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		saveToFile();
	}
}
