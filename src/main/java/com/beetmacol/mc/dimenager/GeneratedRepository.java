package com.beetmacol.mc.dimenager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class GeneratedRepository<T extends GeneratedItem> {
	protected final Map<ResourceLocation, T> generatedItems = new HashMap<>();
	protected final Path generatedDirectory;
	private final String itemGeneralName;

	protected GeneratedRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess, String itemGeneralName) {
		this.generatedDirectory = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
		this.itemGeneralName = itemGeneralName;
	}

	public void addGeneratedItem(T item) {
		item.saveToFile();
		generatedItems.put(item.getIdentifier(), item);
	}

	public void resourceManagerReload(ResourceManager resourceManager) {
		reload();
	}

	public void reload() {
		generatedItems.clear();
		if (generatedDirectory.toFile().isDirectory()) {
			for (File namespaceDirectory : generatedDirectory.toFile().listFiles(File::isDirectory)) {
				File itemDirectory = new File(namespaceDirectory, itemGeneralName);
				if (itemDirectory.isDirectory()) {
					for (File file : itemDirectory.listFiles(File::isFile)) {
						ResourceLocation identifier = new ResourceLocation(namespaceDirectory.getName(), FilenameUtils.removeExtension(file.getName()));
						try {
							JsonReader jsonReader = new JsonReader(new FileReader(file));
							JsonObject json = new JsonParser().parse(jsonReader).getAsJsonObject();
							addGeneratedItem(fromJson(identifier, json));
						} catch (IllegalStateException | JsonSyntaxException exception) {
							Dimenager.LOGGER.error("Could not read JSON data of a " + itemGeneralName + " with id '" + identifier + "'", exception);
						} catch (FileNotFoundException exception) {
							throw new IllegalStateException();
						}
					}
				}
			}
		}
	}

	protected abstract T fromJson(ResourceLocation identifier, JsonObject json) throws JsonSyntaxException;

	public T getGenerated(ResourceLocation identifier) {
		return generatedItems.get(identifier);
	}

	public Collection<ResourceLocation> getGeneratedIdentifiers() {
		return generatedItems.keySet();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean contains(ResourceLocation identifier) {
		return generatedItems.containsKey(identifier);
	}
}
