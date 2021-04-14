package com.beetmacol.mc.dimenager.dimensions;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.GeneratedAndConfiguredRepository;
import com.beetmacol.mc.dimenager.generators.Generator;
import com.beetmacol.mc.dimenager.mixin.MinecraftServerAccessor;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class DimensionRepository extends GeneratedAndConfiguredRepository<GeneratedDimension, ServerWorld> {
	private final Map<RegistryKey<World>, ServerWorld> serverLevels;

	public DimensionRepository(ResourceManager resourceManager, LevelStorage.Session levelStorageAccess, Map<RegistryKey<World>, ServerWorld> serverLevels) {
		super(resourceManager, levelStorageAccess, "dimension");
		this.serverLevels = serverLevels;
	}

	@Override
	protected GeneratedDimension fromJson(Identifier identifier, JsonObject json) throws JsonSyntaxException {
		boolean enabled = JsonHelper.getBoolean(json, "enabled", true);
		Identifier dimensionTypeIdentifier = new Identifier(JsonHelper.getString(json, "dimension_type"));
		Identifier generatorIdentifier = new Identifier(JsonHelper.getString(json, "generator"));
		Generator generator = Dimenager.generatorRepository.get(generatorIdentifier);
		if (generator == null) throw new JsonSyntaxException("Unknown generator '" + generatorIdentifier + "'");
		return new GeneratedDimension(identifier, generatedDirectory, enabled, Dimenager.dimensionTypeRepository.get(dimensionTypeIdentifier), dimensionTypeIdentifier, generator);
	}

	@Override
	protected void addConfiguredItems() {
		for (Map.Entry<RegistryKey<World>, ServerWorld> level : serverLevels.entrySet()) {
			items.put(level.getKey().getValue(), level.getValue());
		}
	}

	public void createLevels(WorldGenerationProgressListener chunkProgressListener, MinecraftServer server) {
		for (GeneratedDimension generatedDimension : generatedItems.values()) {
			if (generatedDimension.getType() == null) {
				Dimenager.LOGGER.error("Could not load dimension '" + generatedDimension.getIdentifier() + "': dimension type is not loaded");
			} else if (generatedDimension.isEnabled()) {
				createLevel(generatedDimension, server, chunkProgressListener);
			}
		}
	}

	private void createLevel(GeneratedDimension generatedDimension, MinecraftServer server, WorldGenerationProgressListener chunkProgressListener) {
		RegistryKey<World> resourceKey = RegistryKey.of(Registry.DIMENSION, generatedDimension.getIdentifier());
		MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor) server;
		UnmodifiableLevelProperties derivedLevelData = new UnmodifiableLevelProperties(server.getSaveProperties(), server.getSaveProperties().getMainWorldProperties());
		GeneratorOptions worldGenSettings = server.getSaveProperties().getGeneratorOptions();
		ChunkGenerator chunkGenerator = generatedDimension.getGenerator().getChunkGenerator();
		ServerWorld serverLevel = new ServerWorld(server, serverAccessor.getWorkerExecutor(),
				serverAccessor.getSession(), derivedLevelData, resourceKey, generatedDimension.getType(),
				chunkProgressListener, chunkGenerator, worldGenSettings.isDebugWorld(),
				BiomeAccess.hashSeed(worldGenSettings.getSeed()), ImmutableList.of(), false);
		items.replace(generatedDimension.getIdentifier(), serverLevel);
		serverLevels.put(resourceKey, serverLevel);
	}

	private void createLevel(GeneratedDimension dimension, ServerCommandSource source) {
		createLevel(dimension, source.getMinecraftServer(), new WorldGenerationProgressListener() {
			@Override
			public void start(ChunkPos spawnPos) {
			}

			@Override
			public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
			}

			@Override
			public void stop() {
			}
		});
	}

	public int createDimension(ServerCommandSource source, Identifier identifier, DimensionType dimensionType, Identifier dimensionTypeIdentifier, Generator generator) {
		if (items.containsKey(identifier)) {
			source.sendError(new LiteralText("A dimension with id '" + identifier + "' already exists"));
			return 0;
		}
		GeneratedDimension dimension = new GeneratedDimension(identifier, generatedDirectory, true, dimensionType, dimensionTypeIdentifier, generator);
		addGeneratedItem(dimension);
		createLevel(dimension, source);
		source.sendFeedback(new LiteralText("Created a new dimension with id '" + identifier + "'"), true);
		return 1;
	}

	public int deleteDimension(ServerCommandSource source, GeneratedDimension dimension) {
		items.remove(dimension.getIdentifier());
		generatedItems.remove(dimension.getIdentifier());
		dimension.removeFile();
		source.sendFeedback(new LiteralText("Removed the dimension with id '" + dimension.getIdentifier() + "'"), true);
		return 1;
	}

	public int listDimensions(ServerCommandSource source) {
		if (items.isEmpty()) {
			source.sendFeedback(new LiteralText("There are no dimensions"), false);
		} else {
			Collection<Identifier> identifiers = items.keySet();
			source.sendFeedback(new LiteralText("There are " + items.size() + " dimensions: ").append(Texts.join(identifiers, identifier -> new LiteralText(identifier.toString()))), false);
		}
		return items.size();
	}

	public int setEnabled(ServerCommandSource source, GeneratedDimension dimension, boolean value) {
		dimension.setEnabled(value);
		source.sendFeedback(new LiteralText((value ? "Enabled" : "Disabled") + " dimension '" + dimension.getIdentifier() + "; it will now be loaded on startup"), true);
		return 1;
	}

	public int setType(ServerCommandSource source, GeneratedDimension dimension, DimensionType type, Identifier typeIdentifier) {
		dimension.setType(type, typeIdentifier);
		source.sendFeedback(new LiteralText("The dimension type of '" + dimension.getIdentifier() + "' dimension was set to '" + typeIdentifier + "'"), true);
		return 1;
	}

	public int setGenerator(ServerCommandSource source, GeneratedDimension dimension, Generator generator) {
		dimension.setGenerator(generator);
		source.sendFeedback(new LiteralText("The generator of '" + dimension.getIdentifier() + "' dimension was set to '" + generator.getIdentifier() + "'"), true);
		return 1;
	}

	public int load(ServerCommandSource source, GeneratedDimension dimension) {
		if (items.get(dimension.getIdentifier()) != null) {
			source.sendError(new LiteralText("Dimension " + dimension.getIdentifier() + " is already loaded"));
			return 0;
		}
		source.sendFeedback(new LiteralText("Loading dimension '" + dimension.getIdentifier() + "'..."), true);
		createLevel(dimension, source);
		return 1;
	}

	public int unload(ServerCommandSource source, GeneratedDimension dimension) {
		if (items.get(dimension.getIdentifier()) == null) {
			source.sendError(new LiteralText("Dimension " + dimension.getIdentifier() + " isn't loaded"));
			return 0;
		}

		ServerWorld level = items.get(dimension.getIdentifier());

		int playersAmount = level.getPlayers().size();
		for (int i = 0; i < playersAmount; i++) {
			BlockPos spawnPos = level.getServer().getOverworld().getSpawnPos();
			float spawnAngle = level.getServer().getOverworld().getSpawnAngle();
			level.getPlayers().get(i).teleport(level.getServer().getOverworld(), spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), spawnAngle, 0f);
		}
		if (playersAmount > 0) {
			source.sendFeedback(new LiteralText("Teleported " + playersAmount + " to the Overworld's spawn, to allow dimension unloading"), true);
		}

		Dimenager.LOGGER.info("Saving chunks for level '{}'/{}", level, dimension.getIdentifier());
		level.save(null, true, true);
		try {
			level.close();
		} catch (IOException exception) {
			source.sendError(new LiteralText("Could not close the dimension " + dimension.getIdentifier() + "! See the server console for more information"));
			Dimenager.LOGGER.error("Closing dimension '" + dimension.getIdentifier() + "' failed with an exception", exception);
			return 0;
		}
		serverLevels.remove(level.getRegistryKey());
		items.replace(dimension.getIdentifier(), null);
		source.sendFeedback(new LiteralText("Unloaded dimension '" + dimension.getIdentifier() + "'"), true);
		return 1;
	}
}
