package net.regnormc.dimenager.generators;

import net.regnormc.dimenager.Dimenager;
import net.regnormc.dimenager.GeneratedRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GeneratorRepository extends GeneratedRepository<Generator> {
	public static final Identifier VOID = new Identifier(Dimenager.MOD_ID, "void");

	private final Map<Identifier, Codec<? extends ChunkGenerator>> generatorTypes = new HashMap<>();
	private final Map<Identifier, Generator> configuredItems = new HashMap<>();
	private final Map<Identifier, Generator> items = new HashMap<>();

	public GeneratorRepository(LevelStorage.Session levelStorageAccess, DynamicRegistryManager.Impl registryHolder) {
		super(levelStorageAccess, "generator");
		for (Identifier generatorTypeIdentifier : Registry.CHUNK_GENERATOR.getIds()) {
			Codec<? extends ChunkGenerator> codec = Registry.CHUNK_GENERATOR.get(generatorTypeIdentifier);
			generatorTypes.put(generatorTypeIdentifier, codec);
		}
		configuredItems.put(VOID, new Generator(VOID, generatedDirectory, VoidGeneratorType.IDENTIFIER, VoidGeneratorType.CODEC, new VoidGeneratorType(registryHolder.get(Registry.BIOME_KEY))));
	}

	public void addDimensionMirrorGenerators(Map<RegistryKey<World>, ServerWorld> levels) {
		// The following code creates generators that reflect settings of generators in the configured dimensions.
		for (Map.Entry<RegistryKey<World>, ServerWorld> entry : levels.entrySet()) {
			ServerWorld serverLevel = entry.getValue();
			Identifier identifier = entry.getKey().getValue();
			// We want to add a generator with id of a configured dimension that will reflect settings of that dimension.
			// There is a chance that there is a generator type that is called the same as a dimension though.
			// E.g if there are a generator type and a dimension with the same id 'minecraft:noise', we will try to call
			// the generator reflecting the dimension 'minecraft:noise0', `minecraft:noise1`, ... until we find a free id.
			if (configuredItems.containsKey(identifier)) {
				Identifier newIdentifier = identifier;
				int i = 0;
				while (identifier.equals(newIdentifier)) {
					Identifier newIdentifierAttempt = new Identifier(identifier.getNamespace(), identifier.getPath() + i++);
					if (!configuredItems.containsKey(newIdentifierAttempt)) {
						newIdentifier = newIdentifierAttempt;
					}
				}
				Dimenager.LOGGER.warn("Generator type '" + identifier + "' has the same id as a dimension; calling the generator with the config of the dimension '" + newIdentifier + "' instead");
				identifier = newIdentifier;
			}
			ChunkGenerator chunkGenerator = serverLevel.getChunkManager().getChunkGenerator();
			Codec<? extends ChunkGenerator> typeCodec = chunkGenerator.getCodec();
			Identifier typeIdentifier = Registry.CHUNK_GENERATOR.getId(typeCodec);
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
	protected Generator fromJson(Identifier identifier, JsonObject json) throws JsonSyntaxException {
		Identifier typeIdentifier = new Identifier(JsonHelper.getString(json, "type"));
		try {
			return new Generator(identifier, generatedDirectory, typeIdentifier, generatorTypes.get(typeIdentifier), JsonHelper.getObject(json, "settings", null));
		} catch (RuntimeException exception) {
			throw new JsonSyntaxException(exception);
		}
	}

	public int createGenerator(ServerCommandSource source, Identifier identifier, Identifier typeIdentifier, Codec<? extends ChunkGenerator> generatorCodec) {
		return createGenerator(source, identifier, typeIdentifier, generatorCodec, source.getMinecraftServer().getSaveProperties().getGeneratorOptions().getSeed());
	}

	public int createGenerator(ServerCommandSource source, Identifier identifier, Identifier typeIdentifier, Codec<? extends ChunkGenerator> generatorCodec, long seed) {
		if (items.containsKey(identifier)) {
			source.sendError(new LiteralText("A generator with id '" + identifier + "' already exists"));
			return 0;
		}
		JsonObject typeDefaults = Dimenager.defaultGeneratorTypeLoader.getDefaults().get(typeIdentifier);
		if (typeDefaults == null) {
			source.sendError(new LiteralText("Missing default settings for generator type with id '" + typeIdentifier + "'"));
			return 0;
		}
		addGeneratedItem(new Generator(identifier, generatedDirectory, typeIdentifier, generatorCodec, typeDefaults, seed));
		source.sendFeedback(new LiteralText("Created a new generator with id '" + identifier + "'"), true);
		return 1;
	}

	public int createGenerator(ServerCommandSource source, Identifier identifier, Generator copied) {
		if (items.containsKey(identifier)) {
			source.sendError(new LiteralText("A generator with id '" + identifier + "' already exists"));
			return 0;
		}
		addGeneratedItem(copied.deepCopy(identifier, generatedDirectory));
		source.sendFeedback(new LiteralText("Copied generator '" + copied.getIdentifier() + "' to a new one with id '" + identifier + "'"), true);
		return 1;
	}

	public int deleteGenerator(ServerCommandSource source, Generator generator) {
		items.remove(generator.getIdentifier());
		generatedItems.remove(generator.getIdentifier());
		generator.removeFile();
		source.sendFeedback(new LiteralText("Removed the generator with id '" + generator.getIdentifier() + "'"), true);
		return 1;
	}

	public int printData(ServerCommandSource source, Generator generator) {
		source.sendFeedback(new LiteralText("Generator " + generator.getIdentifier() + " has the following data: " + generator.getSettings().toString()), false);
		return 1;
	}

	public int listGenerators(ServerCommandSource source) {
		if (items.isEmpty()) {
			source.sendFeedback(new LiteralText("There are no generators"), false);
		} else {
			Collection<Identifier> identifiers = items.keySet();
			source.sendFeedback(new LiteralText("There are " + items.size() + " generators: ").append(Texts.join(identifiers, identifier -> new LiteralText(identifier.toString()))), false);
		}
		return items.size();
	}

	public int listGeneratorTypes(ServerCommandSource source) {
		if (generatorTypes.isEmpty()) {
			source.sendFeedback(new LiteralText("There are no generator types"), false);
		} else {
			Collection<Identifier> identifiers = generatorTypes.keySet();
			source.sendFeedback(new LiteralText("There are " + generatorTypes.size() + " generator types: ").append(Texts.join(identifiers, identifier -> new LiteralText(identifier.toString()))), false);
		}
		return generatorTypes.size();
	}

	public boolean containsGenerated(Identifier identifier) {
		return generatedItems.containsKey(identifier);
	}

	@Override
	public boolean contains(Identifier identifier) {
		return items.containsKey(identifier);
	}

	public Generator get(Identifier identifier) {
		return items.get(identifier);
	}

	public boolean containsGeneratorType(Identifier identifier) {
		return generatorTypes.containsKey(identifier);
	}

	public Codec<? extends ChunkGenerator> getGeneratorType(Identifier identifier) {
		return generatorTypes.get(identifier);
	}

	public Collection<Identifier> getIdentifiers() {
		return items.keySet();
	}

	public Collection<Identifier> generatorTypeIdentifiers() {
		return generatorTypes.keySet();
	}
}
