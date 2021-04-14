package net.regnormc.dimenager.dimensions;

import net.regnormc.dimenager.GeneratedItem;
import net.regnormc.dimenager.generators.Generator;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class GeneratedDimension extends GeneratedItem {
	private DimensionType type;
	private Identifier typeIdentifier;
	private Generator generator; // Temporarily an identifier. Will be a Generator as soon as I create the Generator class.
	private boolean enabled;

	public GeneratedDimension(Identifier identifier, Path generatedDirectory, boolean enabled, DimensionType type, Identifier typeIdentifier, Generator generator) {
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

	public Identifier getTypeIdentifier() {
		return typeIdentifier;
	}

	public Generator getGenerator() {
		return generator;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setType(DimensionType type, Identifier typeIdentifier) {
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
