package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.GeneratedAndConfiguredRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;
import java.util.Map;

public class DimensionRepository extends GeneratedAndConfiguredRepository<GeneratedDimension, ServerLevel> {
	private final Map<ResourceKey<Level>, ServerLevel> serverLevels;
	private final Registry<DimensionType> dimensionTypeRegistry;

	public DimensionRepository(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, Map<ResourceKey<Level>, ServerLevel> serverLevels, Registry<DimensionType> dimensionTypeRegistry) {
		super(resourceManager, levelStorageAccess, "dimension");
		this.serverLevels = serverLevels;
		this.dimensionTypeRegistry = dimensionTypeRegistry;
	}

	private void removeGeneratedDimension(GeneratedDimension dimension) {
		dimension.removeFile();
	}

	@Override
	protected GeneratedDimension fromJson(ResourceLocation identifier, JsonObject json) throws JsonSyntaxException {
		boolean enabled = GsonHelper.getAsBoolean(json, "enabled", true);
		ResourceLocation dimensionTypeIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "dimension_type"));
		ResourceLocation generatorIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "generator"));
		return new GeneratedDimension(identifier, generatedDirectory, enabled, dimensionTypeRegistry.get(dimensionTypeIdentifier), dimensionTypeIdentifier, generatorIdentifier);
	}

	@Override
	protected void addConfiguredItems() {
		for (Map.Entry<ResourceKey<Level>, ServerLevel> level : serverLevels.entrySet()) {
			items.put(level.getKey().location(), level.getValue());
		}
	}

	public int createDimension(CommandSourceStack source, ResourceLocation identifier, DimensionType dimensionType, ResourceLocation dimensionTypeIdentifier) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension with id '" + identifier + "' already exists"));
			return 0;
		}
		ResourceKey<Level> resourceKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, identifier);
		addGeneratedItem(new GeneratedDimension(identifier, generatedDirectory, true, dimensionType, dimensionTypeIdentifier, new ResourceLocation("minecraft:overworld")));
		/* ServerLevel creation
		MinecraftServer server = source.getServer();
		MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor) server;
		DerivedLevelData derivedLevelData = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());
		WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
		ServerLevel serverLevel = new ServerLevel(server, serverAccessor.getExecutor(), serverAccessor.getStorageSource(), derivedLevelData, resourceKey, dimensionType, null, WorldGenSettings.makeDefaultOverworld(server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), server.registryAccess().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong()), worldGenSettings.isDebug(), BiomeManager.obfuscateSeed(worldGenSettings.seed()), ImmutableList.of(), false);
		if (source.getEntity() != null) {
			source.getEntity().changeDimension(serverLevel);
		}*/
		source.sendSuccess(new TextComponent("Created a new dimension with id '" + identifier + "'"), false);
		return 1;
	}

	public int deleteDimension(CommandSourceStack source, GeneratedDimension dimension) {
		items.remove(dimension.getIdentifier());
		generatedItems.remove(dimension.getIdentifier());
		removeGeneratedDimension(dimension);
		source.sendSuccess(new TextComponent("Removed the dimension with id '" + dimension.getIdentifier() + "'"), false);
		return 1;
	}

	public int listDimensions(CommandSourceStack source) {
		if (items.isEmpty()) {
			source.sendSuccess(new TextComponent("There are no dimensions"), false);
		} else {
			Collection<ResourceLocation> identifiers = items.keySet();
			source.sendSuccess(new TextComponent("There are " + items.size() + " dimensions: ").append(ComponentUtils.formatList(identifiers, identifier -> new TextComponent(identifier.toString()))), false);
		}
		return items.size();
	}
}
