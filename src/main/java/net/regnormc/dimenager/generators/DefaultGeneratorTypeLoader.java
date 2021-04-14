package net.regnormc.dimenager.generators;

import net.regnormc.dimenager.Dimenager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class DefaultGeneratorTypeLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
	private final Map<Identifier, JsonObject> defaults = new HashMap<>();

	public DefaultGeneratorTypeLoader() {
		super(new Gson(), "dimenager_gen_type_defaults");
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> loader, ResourceManager resourceManager, Profiler profilerFiller) {
		defaults.clear();
		loader.forEach((identifier, json) -> defaults.put(identifier, json.getAsJsonObject()));
	}

	public Map<Identifier, JsonObject> getDefaults() {
		return defaults;
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(Dimenager.MOD_ID, "dimenager_gen_type_defaults");
	}
}
