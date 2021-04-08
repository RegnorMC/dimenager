package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.GeneratedAndConfiguredRepository;
import com.beetmacol.mc.dimenager.generators.Generator;
import com.beetmacol.mc.dimenager.mixin.MinecraftServerAccessor;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;
import java.util.Map;

public class DimensionRepository extends GeneratedAndConfiguredRepository<GeneratedDimension, ServerLevel> {
	private final Map<ResourceKey<Level>, ServerLevel> serverLevels;

	public DimensionRepository(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, Map<ResourceKey<Level>, ServerLevel> serverLevels) {
		super(resourceManager, levelStorageAccess, "dimension");
		this.serverLevels = serverLevels;
	}

	@Override
	protected GeneratedDimension fromJson(ResourceLocation identifier, JsonObject json) throws JsonSyntaxException {
		boolean enabled = GsonHelper.getAsBoolean(json, "enabled", true);
		ResourceLocation dimensionTypeIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "dimension_type"));
		ResourceLocation generatorIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "generator"));
		Generator generator = Dimenager.generatorRepository.get(generatorIdentifier);
		if (generator == null) throw new JsonSyntaxException("Unknown generator '" + generatorIdentifier + "'");
		return new GeneratedDimension(identifier, generatedDirectory, enabled, Dimenager.dimensionTypeRepository.get(dimensionTypeIdentifier), dimensionTypeIdentifier, generator);
	}

	@Override
	protected void addConfiguredItems() {
		for (Map.Entry<ResourceKey<Level>, ServerLevel> level : serverLevels.entrySet()) {
			items.put(level.getKey().location(), level.getValue());
		}
	}

	public void createLevels(ChunkProgressListener chunkProgressListener, MinecraftServer server) {
		for (GeneratedDimension generatedDimension : generatedItems.values()) {
			if (generatedDimension.getType() == null) {
				Dimenager.LOGGER.error("Could not load dimension '" + generatedDimension.getIdentifier() + "': dimension type is not loaded");
			} else if (generatedDimension.isEnabled()) {
				ResourceKey<Level> resourceKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, generatedDimension.getIdentifier());
				MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor) server;
				DerivedLevelData derivedLevelData = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());
				WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
				/* Default overworld
				ChunkGenerator chunkGenerator = WorldGenSettings.makeDefaultOverworld(
						server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
						server.registryAccess().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY),
						(new Random()).nextLong());
				 */
				ChunkGenerator chunkGenerator = generatedDimension.getGenerator().getChunkGenerator();
				ServerLevel serverLevel = new ServerLevel(server, serverAccessor.getExecutor(),
						serverAccessor.getStorageSource(), derivedLevelData, resourceKey, generatedDimension.getType(),
						chunkProgressListener, chunkGenerator, worldGenSettings.isDebug(),
						BiomeManager.obfuscateSeed(worldGenSettings.seed()), ImmutableList.of(), false);
				items.replace(generatedDimension.getIdentifier(), serverLevel);
				serverLevels.put(resourceKey, serverLevel);
			}
		}
	}

	public int createDimension(CommandSourceStack source, ResourceLocation identifier, DimensionType dimensionType, ResourceLocation dimensionTypeIdentifier, Generator generator) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension with id '" + identifier + "' already exists"));
			return 0;
		}
		addGeneratedItem(new GeneratedDimension(identifier, generatedDirectory, true, dimensionType, dimensionTypeIdentifier, generator));
		source.sendSuccess(new TextComponent("Created a new dimension with id '" + identifier + "'"), false);
		return 1;
	}

	public int deleteDimension(CommandSourceStack source, GeneratedDimension dimension) {
		items.remove(dimension.getIdentifier());
		generatedItems.remove(dimension.getIdentifier());
		dimension.removeFile();
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
