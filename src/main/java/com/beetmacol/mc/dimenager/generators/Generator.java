package com.beetmacol.mc.dimenager.generators;

import com.beetmacol.mc.dimenager.GeneratedItem;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class Generator extends GeneratedItem {
	ResourceLocation typeIdentifier;
	private Codec<? extends ChunkGenerator> typeCodec;
	@Nullable private JsonObject settings;

	protected Generator(ResourceLocation identifier, Path generatedDirectory, ResourceLocation typeIdentifier, Codec<? extends ChunkGenerator> typeCodec, @Nullable JsonObject settings) {
		super(identifier, generatedDirectory, "generator");
		this.typeIdentifier = typeIdentifier;
		this.typeCodec = typeCodec;
		this.settings = settings;
	}

	@Override
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", typeIdentifier.toString());
		if (settings != null) json.add("settings", settings);
		return json;
	}

	public ResourceLocation getTypeIdentifier() {
		return typeIdentifier;
	}

	public Codec<? extends ChunkGenerator> getTypeCodec() {
		return typeCodec;
	}

	public @Nullable JsonObject getSettings() {
		return settings;
	}

	public void setType(ResourceLocation typeIdentifier, Codec<? extends ChunkGenerator> typeCodec) {
		this.typeIdentifier = typeIdentifier;
		this.typeCodec = typeCodec;
		saveToFile();
	}

	public void setSettings(@Nullable JsonObject settings) {
		this.settings = settings;
		saveToFile();
	}
}
