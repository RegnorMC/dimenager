package net.regnormc.dimenager.dimensiontypes;

import net.regnormc.dimenager.Dimenager;
import net.regnormc.dimenager.GeneratedAndConfiguredRepository;
import net.regnormc.dimenager.mixin.DimensionTypeAccessor;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;

public class DimensionTypeRepository extends GeneratedAndConfiguredRepository<GeneratedDimensionType, DimensionType> {
	private final Registry<DimensionType> dimensionTypeRegistry;
	public static final JsonObject DEFAULT_OVERWORLD_JSON = DimensionType.CODEC
			.encode(DimensionTypeAccessor.getDefaultOverworld(), JsonOps.INSTANCE, new JsonObject())
			.getOrThrow(false, s -> Dimenager.LOGGER.error("Could not serialize the default dimension type: " + s))
			.getAsJsonObject();

	public DimensionTypeRepository(ResourceManager resourceManager, LevelStorage.Session levelStorageAccess, Registry<DimensionType> dimensionTypeRegistry) {
		super(resourceManager, levelStorageAccess, "dimension_type");
		this.dimensionTypeRegistry = dimensionTypeRegistry;
	}

	@Override
	protected void addConfiguredItems() {
		for (Identifier dimensionTypeIdentifier : dimensionTypeRegistry.getIds()) {
			items.put(dimensionTypeIdentifier, dimensionTypeRegistry.get(dimensionTypeIdentifier));
		}
	}

	@Override
	protected GeneratedDimensionType fromJson(Identifier identifier, JsonObject json) throws JsonSyntaxException {
		return new GeneratedDimensionType(identifier, generatedDirectory, json.getAsJsonObject("settings"));
	}

	@Override
	protected void addLoadedItem(GeneratedDimensionType item) {
		addGeneratedItem(item, item.getRealType());
	}

	public int createDimensionType(ServerCommandSource source, Identifier identifier) {
		if (items.containsKey(identifier)) {
			source.sendError(new LiteralText("A dimension type with id '" + identifier + "' already exists"));
			return 0;
		}
		GeneratedDimensionType generated = new GeneratedDimensionType(identifier, generatedDirectory, DEFAULT_OVERWORLD_JSON);
		addGeneratedItem(generated, generated.getRealType());
		source.sendFeedback(new LiteralText("Created a new dimension type with id '" + identifier + "'"), true);
		return 1;
	}

	public int createDimensionType(ServerCommandSource source, Identifier identifier, Identifier copiedIdentifier, DimensionType copied) {
		if (items.containsKey(identifier)) {
			source.sendError(new LiteralText("A dimension type with id '" + identifier + "' already exists"));
			return 0;
		}
		JsonObject settings = DimensionType.CODEC
				.encode(copied, JsonOps.INSTANCE, new JsonObject())
				.getOrThrow(false, s -> Dimenager.LOGGER.error("Could not serialize the default dimension type: " + s))
				.getAsJsonObject();
		GeneratedDimensionType generated = new GeneratedDimensionType(identifier, generatedDirectory, settings);
		addGeneratedItem(generated, generated.getRealType());
		source.sendFeedback(new LiteralText("Copied dimension type '" + copiedIdentifier + "' to a new one with id '" + identifier + "'"), true);
		return 1;
	}

	public int deleteDimensionType(ServerCommandSource source, GeneratedDimensionType dimensionType) {
		items.remove(dimensionType.getIdentifier());
		generatedItems.remove(dimensionType.getIdentifier());
		dimensionType.removeFile();
		source.sendFeedback(new LiteralText("Removed the dimension type with id '" + dimensionType.getIdentifier() + "'"), true);
		return 1;
	}

	public int listDimensionTypes(ServerCommandSource source) {
		if (items.isEmpty()) {
			source.sendFeedback(new LiteralText("There are no dimension types"), false);
		} else {
			Collection<Identifier> identifiers = items.keySet();
			source.sendFeedback(new LiteralText("There are " + items.size() + " dimension types: ").append(Texts.join(identifiers, identifier -> new LiteralText(identifier.toString()))), false);
		}
		return items.size();
	}

	public int setDimensionTypeProperty(ServerCommandSource source, GeneratedDimensionType modifiedType, String property, String value) {
		JsonObject settings = modifiedType.getSettings();
		if (DEFAULT_OVERWORLD_JSON.get(property) != null && !DEFAULT_OVERWORLD_JSON.get(property).isJsonPrimitive()) {
			source.sendError(new LiteralText("Missing property or unsupported value type"));
			return 0;
		}
		JsonPrimitive propertyJson = DEFAULT_OVERWORLD_JSON.getAsJsonPrimitive(property);
		if (propertyJson.isBoolean()) {
			if (!value.equals("false") && !value.equals("true")) {
				source.sendError(new LiteralText("This value needs to be either 'true' or 'false'"));
				return 0;
			}
			settings.addProperty(property, Boolean.valueOf(value));
		} else if (propertyJson.isNumber()) {
			try {
				settings.addProperty(property, NumberFormat.getInstance().parse(value));
			} catch (ParseException | NumberFormatException exception) {
				source.sendError(new LiteralText("This value needs to be a number"));
				return 0;
			}
		} else if (propertyJson.isString()) {
			settings.addProperty(property, value);
		} else {
			source.sendError(new LiteralText("Unknown value type for property '" + property + "'"));
			return 0;
		}
		modifiedType.setSettings(settings);
		source.sendFeedback(new LiteralText("The value of property '" + property + "' in dimension type '" + modifiedType.getIdentifier() + "' was set to '" + value + "'"), true);
		return 1;
	}
}
