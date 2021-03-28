package com.beetmacol.mc.dimenager.generators;

import com.beetmacol.mc.dimenager.GeneratedRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GeneratorRepository extends GeneratedRepository<Generator> {
	private final Map<ResourceLocation, Codec<? extends ChunkGenerator>> generatorTypes = new HashMap<>();
	private final Map<ResourceLocation, Generator> configuredItems = new HashMap<>();

	private final Map<ResourceLocation, Generator> items = new HashMap<>();

	public GeneratorRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		super(levelStorageAccess, "generator");
		for (ResourceLocation generatorTypeIdentifier : Registry.CHUNK_GENERATOR.keySet()) {
			Codec<? extends ChunkGenerator> codec = Registry.CHUNK_GENERATOR.get(generatorTypeIdentifier);
			generatorTypes.put(generatorTypeIdentifier, codec);
			items.put(generatorTypeIdentifier, new Generator(generatorTypeIdentifier, generatedDirectory, generatorTypeIdentifier, codec, null));
		}
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
		return new Generator(identifier, generatedDirectory, typeIdentifier, generatorTypes.get(typeIdentifier), GsonHelper.getAsJsonObject(json, "settings", null));
	}

	public int createGenerator(CommandSourceStack source, ResourceLocation identifier, ResourceLocation generatorIdentifier, Codec<? extends ChunkGenerator> generatorCodec) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A generator with id '" + identifier + "' already exists"));
			return 0;
		}
		addGeneratedItem(new Generator(identifier, generatedDirectory, generatorIdentifier, generatorCodec, null));
		source.sendSuccess(new TextComponent("Created a new generator with id '" + identifier + "'"), false);
		return 1;
	}

	public int createGenerator(CommandSourceStack source, ResourceLocation identifier, Generator copied) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A generator with id '" + identifier + "' already exists"));
			return 0;
		}
		Gson gson = new Gson();
		addGeneratedItem(new Generator(identifier, generatedDirectory, copied.getTypeIdentifier(), copied.getTypeCodec(), copied.getSettings() != null ? gson.fromJson(gson.toJson(copied.getSettings(), JsonObject.class), JsonObject.class) : null));
		// settings are deep copied with that weir way because `JsonObject#deepCopy` is public since Gson 2.8.2 and Minecraft uses 2.8.0...
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
