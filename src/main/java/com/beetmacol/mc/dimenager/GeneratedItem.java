package com.beetmacol.mc.dimenager;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public abstract class GeneratedItem {
	private final ResourceLocation identifier;
	private final File file;

	protected GeneratedItem(ResourceLocation identifier, Path generatedDirectory, String generalName) {
		this.identifier = identifier;
		this.file = new File(generatedDirectory.toFile(), this.identifier.getNamespace() + File.separator + generalName + File.separator + this.identifier.getPath() + ".json");
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void saveToFile() {
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileWriter writer =  new FileWriter(file);
			JsonObject json = toJson();
			writer.write(json.toString());
			writer.close();
		} catch (IOException exception) {
			Dimenager.LOGGER.error("Could not save generated dimension information to file {}", file, exception);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void removeFile() {
		file.delete();
	}

	public ResourceLocation getIdentifier() {
		return identifier;
	}

	public abstract JsonObject toJson();
}
