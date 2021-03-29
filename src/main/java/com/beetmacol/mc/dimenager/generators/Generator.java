package com.beetmacol.mc.dimenager.generators;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.GeneratedItem;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class Generator extends GeneratedItem {
	ResourceLocation typeIdentifier;
	private final Codec<? extends ChunkGenerator> typeCodec;
	private final ChunkGenerator chunkGenerator;
	private JsonObject settings;

	protected Generator(ResourceLocation identifier, Path generatedDirectory, ResourceLocation typeIdentifier, Codec<? extends ChunkGenerator> typeCodec, @Nullable JsonObject settings) {
		this(identifier, generatedDirectory, typeIdentifier, typeCodec, settings, typeCodec.decode(JsonOps.INSTANCE, settings != null ? settings : new JsonObject()).getOrThrow(false, s -> Dimenager.LOGGER.error("Could not deserialize generator with id '" + identifier + "': " + s)).getFirst());
	}

	protected Generator(ResourceLocation identifier, Path generatedDirectory, ResourceLocation typeIdentifier, Codec<? extends ChunkGenerator> typeCodec, @Nullable JsonObject settings, ChunkGenerator chunkGenerator) {
		super(identifier, generatedDirectory, "generator");
		this.typeIdentifier = typeIdentifier;
		this.typeCodec = typeCodec;
		this.settings = settings != null ? settings : new JsonObject();
		this.chunkGenerator = chunkGenerator;
	}

	protected Generator deepCopy(ResourceLocation identifier, Path generatedDirectory) {
		Gson gson = new Gson();
		return new Generator(identifier, generatedDirectory, getTypeIdentifier(), this.typeCodec, gson.fromJson(gson.toJson(settings, JsonObject.class), JsonObject.class));
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", typeIdentifier.toString());
		if (settings != null) json.add("settings", settings);
		return json;
	}

	public ChunkGenerator getChunkGenerator() {
		return chunkGenerator;
	}

	public ResourceLocation getTypeIdentifier() {
		return typeIdentifier;
	}

	public void setSettings(@Nullable JsonObject settings) {
		this.settings = settings;
		saveToFile();
	}
}
