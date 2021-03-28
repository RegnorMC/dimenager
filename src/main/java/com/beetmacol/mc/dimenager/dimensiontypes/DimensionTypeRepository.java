package com.beetmacol.mc.dimenager.dimensiontypes;

import com.beetmacol.mc.dimenager.GeneratedAndConfiguredRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Collection;

public class DimensionTypeRepository extends GeneratedAndConfiguredRepository<GeneratedDimensionType, DimensionType> {
	private final Registry<DimensionType> dimensionTypeRegistry;

	public DimensionTypeRepository(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, Registry<DimensionType> dimensionTypeRegistry) {
		super(resourceManager, levelStorageAccess, "dimension_type");
		this.dimensionTypeRegistry = dimensionTypeRegistry;
	}

	@Override
	protected void addConfiguredItems() {
		for (ResourceLocation dimensionTypeIdentifier : dimensionTypeRegistry.keySet()) {
			items.put(dimensionTypeIdentifier, dimensionTypeRegistry.get(dimensionTypeIdentifier));
		}
	}

	@Override
	protected GeneratedDimensionType fromJson(ResourceLocation identifier, JsonObject json) throws JsonSyntaxException {
		return new GeneratedDimensionType(identifier, generatedDirectory, json.getAsJsonObject("settings"));
	}

	public int createDimensionType(CommandSourceStack source, ResourceLocation identifier) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension type with id '" + identifier + "' already exists"));
			return 0;
		}
		addGeneratedItem(new GeneratedDimensionType(identifier, generatedDirectory, new JsonObject()));
		source.sendSuccess(new TextComponent("Created a new dimension type with id '" + identifier + "'"), false);
		return 1;
	}

	public int createDimensionType(CommandSourceStack source, ResourceLocation identifier, ResourceLocation copiedIdentifier, DimensionType copied) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension type with id '" + identifier + "' already exists"));
			return 0;
		}
		//Gson gson = new Gson();
		addGeneratedItem(new GeneratedDimensionType(identifier, generatedDirectory, new JsonObject()/*gson.fromJson(gson.toJson(DimensionType.DIRECT_CODEC.encode(copied, ), JsonObject.class), JsonObject.class)*/));
		source.sendSuccess(new TextComponent("Copied dimension type '" + copiedIdentifier + "' to a new one with id '" + identifier + "'"), false);
		return 1;
	}

	public int deleteDimensionType(CommandSourceStack source, GeneratedDimensionType dimensionType) {
		items.remove(dimensionType.getIdentifier());
		generatedItems.remove(dimensionType.getIdentifier());
		dimensionType.removeFile();
		source.sendSuccess(new TextComponent("Removed the dimension type with id '" + dimensionType.getIdentifier() + "'"), false);
		return 1;
	}

	public int listDimensionTypes(CommandSourceStack source) {
		if (items.isEmpty()) {
			source.sendSuccess(new TextComponent("There are no dimension types"), false);
		} else {
			Collection<ResourceLocation> identifiers = items.keySet();
			source.sendSuccess(new TextComponent("There are " + items.size() + " dimension types: ").append(ComponentUtils.formatList(identifiers, identifier -> new TextComponent(identifier.toString()))), false);
		}
		return items.size();
	}
}
