package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.Dimenager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
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

public class DimensionRepository {
	private final Map<ResourceLocation, ServerLevel> dimensions = new HashMap<>();
	private final Map<ResourceLocation, GeneratedDimension> generatedDimensions = new HashMap<>();
	private ResourceManager resourceManager;
	private final Path generatedDirectory;
	private final Map<ResourceKey<Level>, ServerLevel> serverLevels;
	private final Registry<DimensionType> dimensionTypeRegistry;

	public DimensionRepository(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, Map<ResourceKey<Level>, ServerLevel> serverLevels, Registry<DimensionType> dimensionTypeRegistry) {
		this.resourceManager = resourceManager;
		this.generatedDirectory = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
		this.serverLevels = serverLevels;
		this.dimensionTypeRegistry = dimensionTypeRegistry;
		this.reloadDimensions();
	}

	public void resourceManagerReload(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		reloadDimensions();
	}

	public void reloadDimensions() {
		dimensions.clear();
		generatedDimensions.clear();
		for (Map.Entry<ResourceKey<Level>, ServerLevel> level : serverLevels.entrySet()) {
			dimensions.put(level.getKey().location(), level.getValue());
		}
		if (generatedDirectory.toFile().isDirectory()) {
			for (File namespaceDirectory : generatedDirectory.toFile().listFiles(File::isDirectory)) {
				File dimensionDirectory = new File(namespaceDirectory, "dimensions");
				if (dimensionDirectory.isDirectory()) {
					for (File file : dimensionDirectory.listFiles(File::isFile)) {
						try {
							JsonReader jsonReader = new JsonReader(new FileReader(file));
							JsonObject json = new JsonParser().parse(jsonReader).getAsJsonObject();
							boolean enabled = GsonHelper.getAsBoolean(json, "enabled", true);
							ResourceLocation dimensionTypeIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "dimension_type"));
							ResourceLocation generatorIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "generator"));
							ResourceLocation identifier = new ResourceLocation(namespaceDirectory.getName(), FilenameUtils.removeExtension(file.getName()));
							addGeneratedDimension(identifier, enabled, dimensionTypeIdentifier, generatorIdentifier);
						} catch (IllegalStateException | JsonSyntaxException exception) {
							Dimenager.LOGGER.error("Could not read json from file " + file.getPath(), exception);
						} catch (FileNotFoundException exception) {
							throw new IllegalStateException();
						}
					}
				}
			}
		}
	}

	private void addGeneratedDimension(ResourceLocation identifier, boolean enabled, ResourceLocation dimensionType, ResourceLocation generator) {
		addGeneratedDimension(identifier, enabled, dimensionTypeRegistry.get(dimensionType), dimensionType, generator);
	}

	private void addGeneratedDimension(ResourceLocation identifier, boolean enabled, DimensionType dimensionType, ResourceLocation dimensionTypeIdentifier, ResourceLocation generator) {
		GeneratedDimension generatedDimension = new GeneratedDimension(identifier, generatedDirectory, enabled, dimensionType, dimensionTypeIdentifier, generator);
		generatedDimension.saveToFile();
		dimensions.put(identifier, null);
		generatedDimensions.put(identifier, generatedDimension);
	}

	private void removeGeneratedDimension(GeneratedDimension dimension) {
		dimension.removeFile();
	}

	public int createDimension(CommandSourceStack source, ResourceLocation identifier, DimensionType dimensionType, ResourceLocation dimensionTypeIdentifier) {
		if (dimensions.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension with id '" + identifier.toString() + "' already exists"));
			return 0;
		}
		ResourceKey<Level> resourceKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, identifier);
		addGeneratedDimension(identifier, true, dimensionType, dimensionTypeIdentifier, new ResourceLocation("minecraft:overworld"));
		/* ServerLevel creation
		MinecraftServer server = source.getServer();
		MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor) server;
		DerivedLevelData derivedLevelData = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());
		WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
		ServerLevel serverLevel = new ServerLevel(server, serverAccessor.getExecutor(), serverAccessor.getStorageSource(), derivedLevelData, resourceKey, dimensionType, null, WorldGenSettings.makeDefaultOverworld(server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), server.registryAccess().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong()), worldGenSettings.isDebug(), BiomeManager.obfuscateSeed(worldGenSettings.seed()), ImmutableList.of(), false);
		if (source.getEntity() != null) {
			source.getEntity().changeDimension(serverLevel);
		}*/
		source.sendSuccess(new TextComponent("Created a new dimension with id " + identifier.toString()), false);
		return 1;
	}

	public int deleteDimension(CommandSourceStack source, GeneratedDimension dimension) {
		dimensions.remove(dimension.getIdentifier());
		generatedDimensions.remove(dimension.getIdentifier());
		removeGeneratedDimension(dimension);
		source.sendSuccess(new TextComponent("Removed the dimension with id '" + dimension.getIdentifier() + "'."), false);
		return 1;
	}

	public int listDimensions(CommandSourceStack source) {
		Collection<ResourceLocation> identifiers = dimensions.keySet();
		if (dimensions.isEmpty())
			source.sendSuccess(new TextComponent("There are no dimensions"), false);
		else
			source.sendSuccess(new TextComponent("There are " + dimensions.size() + " dimensions: ").append(ComponentUtils.formatList(identifiers, identifier -> new TextComponent(identifier.toString()))), false);
		return dimensions.size();
	}

	public GeneratedDimension getDimension(ResourceLocation identifier) {
		return generatedDimensions.get(identifier);
	}

	public GeneratedDimension getGeneratedDimension(ResourceLocation identifier) {
		return generatedDimensions.get(identifier);
	}

	public boolean contains(ResourceLocation identifier) {
		return dimensions.containsKey(identifier);
	}

	public Collection<ResourceLocation> getDimensionIdentifiers() {
		return dimensions.keySet();
	}

	public Collection<ResourceLocation> getGeneratedDimensionIdentifiers() {
		return generatedDimensions.keySet();
	}
}
