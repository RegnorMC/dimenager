package com.beetmacol.mc.dimenager.generators;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.GeneratedRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.beetmacol.mc.dimenager.Dimenager.defaultGeneratorTypeLoader;

public class GeneratorRepository extends GeneratedRepository<Generator> {
	public static final ResourceLocation VOID = new ResourceLocation(Dimenager.MOD_ID, "void");

	private final Map<ResourceLocation, Codec<? extends ChunkGenerator>> generatorTypes = new HashMap<>();
	private final Map<ResourceLocation, Generator> configuredItems = new HashMap<>();
	private final Map<ResourceLocation, Generator> items = new HashMap<>();

	public GeneratorRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess, RegistryAccess.RegistryHolder registryHolder) {
		super(levelStorageAccess, "generator");
		for (ResourceLocation generatorTypeIdentifier : Registry.CHUNK_GENERATOR.keySet()) {
			Codec<? extends ChunkGenerator> codec = Registry.CHUNK_GENERATOR.get(generatorTypeIdentifier);
			generatorTypes.put(generatorTypeIdentifier, codec);
		}
		configuredItems.put(VOID, new Generator(VOID, generatedDirectory, VoidGeneratorType.IDENTIFIER, VoidGeneratorType.CODEC, new VoidGeneratorType(registryHolder.registryOrThrow(Registry.BIOME_REGISTRY))));
	}

	public void addDimensionMirrorGenerators(Map<ResourceKey<Level>, ServerLevel> levels) {
		// The following code creates generators that reflect settings of generators in the configured dimensions.
		for (Map.Entry<ResourceKey<Level>, ServerLevel> entry : levels.entrySet()) {
			ServerLevel serverLevel = entry.getValue();
			ResourceLocation identifier = entry.getKey().location();
			// We want to add a generator with id of a configured dimension that will reflect settings of that dimension.
			// There is a chance that there is a generator type that is called the same as a dimension though.
			// E.g if there are a generator type and a dimension with the same id 'minecraft:noise', we will try to call
			// the generator reflecting the dimension 'minecraft:noise0', `minecraft:noise1`, ... until we find a free id.
			if (configuredItems.containsKey(identifier)) {
				ResourceLocation newIdentifier = identifier;
				int i = 0;
				while (identifier.equals(newIdentifier)) {
					ResourceLocation newIdentifierAttempt = new ResourceLocation(identifier.getNamespace(), identifier.getPath() + i++);
					if (!configuredItems.containsKey(newIdentifierAttempt)) {
						newIdentifier = newIdentifierAttempt;
					}
				}
				Dimenager.LOGGER.warn("Generator type '" + identifier + "' has the same id as a dimension; calling the generator with the config of the dimension '" + newIdentifier + "' instead");
				identifier = newIdentifier;
			}
			ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
			Codec<? extends ChunkGenerator> typeCodec = chunkGenerator.codec();
			ResourceLocation typeIdentifier = Registry.CHUNK_GENERATOR.getKey(typeCodec);
			if(typeIdentifier == null) {
				Dimenager.LOGGER.warn("Could not find the generator type identifier of the configured dimension '" + entry.getKey() + "', skipping its generator type");
				continue;
			}
			try {
				configuredItems.put(identifier, new Generator(identifier, generatedDirectory, typeIdentifier, typeCodec, chunkGenerator));
			} catch (RuntimeException exception) {
				Dimenager.LOGGER.error("Failed to serialize reflection generator, skipping it");
			}
		}
		items.putAll(configuredItems);
	}

	@Override
	public void addGeneratedItem(Generator item) {
		super.addGeneratedItem(item);
		items.put(item.getIdentifier(), item);
	}

	@Override
	public void reload() {
		super.reload();
		items.putAll(configuredItems);
	}

	@Override
	protected Generator fromJson(ResourceLocation identifier, JsonObject json) throws JsonSyntaxException {
		ResourceLocation typeIdentifier = new ResourceLocation(GsonHelper.getAsString(json, "type"));
		try {
			return new Generator(identifier, generatedDirectory, typeIdentifier, generatorTypes.get(typeIdentifier), GsonHelper.getAsJsonObject(json, "settings", null));
		} catch (RuntimeException exception) {
			throw new JsonSyntaxException(exception);
		}
	}

	public int createGenerator(CommandSourceStack source, ResourceLocation identifier, ResourceLocation typeIdentifier, Codec<? extends ChunkGenerator> generatorCodec) {
		return createGenerator(source, identifier, typeIdentifier, generatorCodec, source.getServer().getWorldData().worldGenSettings().seed());
	}

	public int createGenerator(CommandSourceStack source, ResourceLocation identifier, ResourceLocation typeIdentifier, Codec<? extends ChunkGenerator> generatorCodec, long seed) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A generator with id '" + identifier + "' already exists"));
			return 0;
		}
		JsonObject typeDefaults = defaultGeneratorTypeLoader.getDefaults().get(typeIdentifier);
		if (typeDefaults == null) {
			source.sendFailure(new TextComponent("Missing default settings for generator type with id '" + typeIdentifier + "'"));
			return 0;
		}
		addGeneratedItem(new Generator(identifier, generatedDirectory, typeIdentifier, generatorCodec, typeDefaults, seed));
		source.sendSuccess(new TextComponent("Created a new generator with id '" + identifier + "'"), false);
		return 1;
	}

	public int createGenerator(CommandSourceStack source, ResourceLocation identifier, Generator copied) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A generator with id '" + identifier + "' already exists"));
			return 0;
		}
		addGeneratedItem(copied.deepCopy(identifier, generatedDirectory));
		source.sendSuccess(new TextComponent("Copied generator '" + copied.getIdentifier() + "' to a new one with id '" + identifier + "'"), false);
		return 1;
	}

	public int deleteGenerator(CommandSourceStack source, Generator generator) {
		items.remove(generator.getIdentifier());
		generatedItems.remove(generator.getIdentifier());
		generator.removeFile();
		source.sendSuccess(new TextComponent("Removed the generator with id '" + generator.getIdentifier() + "'"), false);
		return 1;
	}

	public int printData(CommandSourceStack source, Generator generator) {
		source.sendSuccess(new TextComponent("Generator " + generator.getIdentifier() + " has the following data: " + generator.getSettings().toString()), false);
		return 1;
	}

	public int listGenerators(CommandSourceStack source) {
		if (items.isEmpty()) {
			source.sendSuccess(new TextComponent("There are no generators"), false);
		} else {
			Collection<ResourceLocation> identifiers = items.keySet();
			source.sendSuccess(new TextComponent("There are " + items.size() + " generators: ").append(ComponentUtils.formatList(identifiers, identifier -> new TextComponent(identifier.toString()))), false);
		}
		return items.size();
	}

	public int listGeneratorTypes(CommandSourceStack source) {
		if (generatorTypes.isEmpty()) {
			source.sendSuccess(new TextComponent("There are no generator types"), false);
		} else {
			Collection<ResourceLocation> identifiers = generatorTypes.keySet();
			source.sendSuccess(new TextComponent("There are " + generatorTypes.size() + " generator types: ").append(ComponentUtils.formatList(identifiers, identifier -> new TextComponent(identifier.toString()))), false);
		}
		return generatorTypes.size();
	}

	public boolean containsGenerated(ResourceLocation identifier) {
		return generatedItems.containsKey(identifier);
	}

	@Override
	public boolean contains(ResourceLocation identifier) {
		return items.containsKey(identifier);
	}

	public Generator get(ResourceLocation identifier) {
		return items.get(identifier);
	}

	public boolean containsGeneratorType(ResourceLocation identifier) {
		return generatorTypes.containsKey(identifier);
	}

	public Codec<? extends ChunkGenerator> getGeneratorType(ResourceLocation identifier) {
		return generatorTypes.get(identifier);
	}

	public Collection<ResourceLocation> getIdentifiers() {
		return items.keySet();
	}

	public Collection<ResourceLocation> generatorTypeIdentifiers() {
		return generatorTypes.keySet();
	}
}
