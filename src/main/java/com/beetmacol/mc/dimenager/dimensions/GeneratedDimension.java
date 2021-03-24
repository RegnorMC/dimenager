package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.Dimenager;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class GeneratedDimension {
	private final ResourceLocation identifier;
	private final File file;
	private boolean enabled;
	private DimensionType type;
	private ResourceLocation typeIdentifier;
	private ResourceLocation generatorIdentifier; // Temporarily an identifier. Will be a Generator as soon as I create the Generator class.

	public GeneratedDimension(ResourceLocation identifier, Path generatedDirectory, boolean enabled, DimensionType type, ResourceLocation typeIdentifier, ResourceLocation generatorIdentifier) {
		file = new File(generatedDirectory.toFile(), identifier.getNamespace() + "/dimensions/" + identifier.getPath() + ".json");
		this.identifier = identifier;
		this.enabled = enabled;
		this.type = type;
		this.typeIdentifier = typeIdentifier;
		this.generatorIdentifier = generatorIdentifier;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void saveToFile() {
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileWriter writer =  new FileWriter(file);
			JsonObject json = new JsonObject();
			json.addProperty("enabled", enabled);
			json.addProperty("dimension_type", typeIdentifier.toString());
			json.addProperty("generator", generatorIdentifier.toString());
			writer.write(json.toString());
			writer.close();
		} catch (IOException exception) {
			Dimenager.LOGGER.error("Could not save generated dimension information to file {}", file, exception);
		}
	}

	public ResourceLocation getIdentifier() {
		return identifier;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public DimensionType getType() {
		return type;
	}

	public ResourceLocation getGeneratorIdentifier() {
		return generatorIdentifier;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setType(ResourceLocation typeIdentifier, DimensionType type) {
		this.typeIdentifier = typeIdentifier;
		this.type = type;
	}

	public void setGenerator(ResourceLocation generatorIdentifier) {
		this.generatorIdentifier = generatorIdentifier;
	}
}
