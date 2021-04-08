package com.beetmacol.mc.dimenager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;

public abstract class GeneratedRepository<T extends GeneratedItem> {
	protected final Map<Identifier, T> generatedItems = new HashMap<>();
	protected final Path generatedDirectory;
	private final String itemGeneralName;

	protected GeneratedRepository(LevelStorage.Session levelStorageAccess, String itemGeneralName) {
		this.generatedDirectory = levelStorageAccess.getDirectory(WorldSavePath.GENERATED).normalize();
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
						Identifier identifier = new Identifier(namespaceDirectory.getName(), FilenameUtils.removeExtension(file.getName()));
						try {
							JsonReader jsonReader = new JsonReader(new FileReader(file));
							JsonObject json = new JsonParser().parse(jsonReader).getAsJsonObject();
							addLoadedItem(fromJson(identifier, json));
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

	protected abstract T fromJson(Identifier identifier, JsonObject json) throws JsonSyntaxException;

	protected void addLoadedItem(T item) {
		addGeneratedItem(item);
	}

	public T getGenerated(Identifier identifier) {
		return generatedItems.get(identifier);
	}

	public Collection<Identifier> getGeneratedIdentifiers() {
		return generatedItems.keySet();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean contains(Identifier identifier) {
		return generatedItems.containsKey(identifier);
	}
}
