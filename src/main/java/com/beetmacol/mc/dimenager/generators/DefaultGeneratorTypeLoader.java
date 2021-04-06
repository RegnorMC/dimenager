package com.beetmacol.mc.dimenager.generators;

import com.beetmacol.mc.dimenager.Dimenager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class DefaultGeneratorTypeLoader extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
	private final Map<ResourceLocation, JsonObject> defaults = new HashMap<>();

	public DefaultGeneratorTypeLoader() {
		super(new Gson(), "dimenager_gen_type_defaults");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		defaults.clear();
		loader.forEach((identifier, json) -> defaults.put(identifier, json.getAsJsonObject()));
	}

	public Map<ResourceLocation, JsonObject> getDefaults() {
		return defaults;
	}

	@Override
	public ResourceLocation getFabricId() {
		return new ResourceLocation(Dimenager.MOD_ID, "dimenager_gen_type_defaults");
	}
}
