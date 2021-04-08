package com.beetmacol.mc.dimenager.dimensiontypes;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.GeneratedAndConfiguredRepository;
import com.beetmacol.mc.dimenager.mixin.DimensionTypeAccessor;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;

public class DimensionTypeRepository extends GeneratedAndConfiguredRepository<GeneratedDimensionType, DimensionType> {
	private final Registry<DimensionType> dimensionTypeRegistry;
	public static final JsonObject DEFAULT_OVERWORLD_JSON = DimensionType.DIRECT_CODEC
			.encode(DimensionTypeAccessor.getDefaultOverworld(), JsonOps.INSTANCE, new JsonObject())
			.getOrThrow(false, s -> Dimenager.LOGGER.error("Could not serialize the default dimension type: " + s))
			.getAsJsonObject();

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

	@Override
	protected void addLoadedItem(GeneratedDimensionType item) {
		addGeneratedItem(item, item.getRealType());
	}

	public int createDimensionType(CommandSourceStack source, ResourceLocation identifier) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension type with id '" + identifier + "' already exists"));
			return 0;
		}
		GeneratedDimensionType generated = new GeneratedDimensionType(identifier, generatedDirectory, DEFAULT_OVERWORLD_JSON);
		addGeneratedItem(generated, generated.getRealType());
		source.sendSuccess(new TextComponent("Created a new dimension type with id '" + identifier + "'"), false);
		return 1;
	}

	public int createDimensionType(CommandSourceStack source, ResourceLocation identifier, ResourceLocation copiedIdentifier, DimensionType copied) {
		if (items.containsKey(identifier)) {
			source.sendFailure(new TextComponent("A dimension type with id '" + identifier + "' already exists"));
			return 0;
		}
		JsonObject settings = DimensionType.DIRECT_CODEC
				.encode(copied, JsonOps.INSTANCE, new JsonObject())
				.getOrThrow(false, s -> Dimenager.LOGGER.error("Could not serialize the default dimension type: " + s))
				.getAsJsonObject();
		GeneratedDimensionType generated = new GeneratedDimensionType(identifier, generatedDirectory, settings);
		addGeneratedItem(generated, generated.getRealType());
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

	public int setDimensionTypeProperty(CommandSourceStack source, GeneratedDimensionType modifiedType, String property, String value) {
		JsonObject settings = modifiedType.getSettings();
		if (DEFAULT_OVERWORLD_JSON.get(property) != null && !DEFAULT_OVERWORLD_JSON.get(property).isJsonPrimitive()) {
			source.sendFailure(new TextComponent("Missing property or unsupported value type"));
			return 0;
		}
		JsonPrimitive propertyJson = DEFAULT_OVERWORLD_JSON.getAsJsonPrimitive(property);
		if (propertyJson.isBoolean()) {
			if (!value.equals("false") && !value.equals("true")) {
				source.sendFailure(new TextComponent("This value needs to be either 'true' or 'false'"));
				return 0;
			}
			settings.addProperty(property, Boolean.valueOf(value));
		} else if (propertyJson.isNumber()) {
			try {
				settings.addProperty(property, NumberFormat.getInstance().parse(value));
			} catch (ParseException | NumberFormatException exception) {
				source.sendFailure(new TextComponent("This value needs to be a number"));
				return 0;
			}
		} else if (propertyJson.isString()) {
			settings.addProperty(property, value);
		} else {
			source.sendFailure(new TextComponent("Unknown value type for property '" + property + "'"));
			return 0;
		}
		modifiedType.setSettings(settings);
		source.sendSuccess(new TextComponent("The value of property '" + property + "' in dimension type '" + modifiedType.getIdentifier() + "' was set to '" + value + "'"), false);
		return 1;
	}
}
