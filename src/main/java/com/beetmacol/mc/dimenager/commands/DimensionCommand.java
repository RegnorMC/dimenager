package com.beetmacol.mc.dimenager.commands;

import com.beetmacol.mc.dimenager.dimensions.GeneratedDimension;
import com.beetmacol.mc.dimenager.dimensiontypes.DimensionTypeRepository;
import com.beetmacol.mc.dimenager.dimensiontypes.GeneratedDimensionType;
import com.beetmacol.mc.dimenager.generators.Generator;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import static com.beetmacol.mc.dimenager.Dimenager.*;

@SuppressWarnings("SameParameterValue")
public class DimensionCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("dimension")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("worlds")
						.then(CommandManager.literal("add")
								.then(CommandManager.argument("identifier", IdentifierArgumentType.identifier())
										.then(CommandManager.argument("type", IdentifierArgumentType.identifier())
												.suggests(DimensionCommand::dimensionTypeSuggestions)
												.then(CommandManager.argument("generator", IdentifierArgumentType.identifier())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> dimensionRepository.createDimension(context.getSource(), IdentifierArgumentType.getIdentifier(context, "identifier"), getDimensionType(context, "type"), IdentifierArgumentType.getIdentifier(context, "type"), getGenerator(context, "generator")))
												)
										)
								)
						)
						.then(CommandManager.literal("remove")
								.then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
										.suggests(DimensionCommand::customDimensionSuggestions)
										.executes(context -> dimensionRepository.deleteDimension(context.getSource(), getGeneratedDimension(context, "dimension")))
								)
						)
						.then(CommandManager.literal("list")
								.executes(context -> dimensionRepository.listDimensions(context.getSource()))
						)
				)
				.then(CommandManager.literal("types")
						.then(CommandManager.literal("add")
								.then(CommandManager.argument("identifier", IdentifierArgumentType.identifier())
										.executes(context -> dimensionTypeRepository.createDimensionType(context.getSource(), IdentifierArgumentType.getIdentifier(context, "identifier")))
										.then(CommandManager.literal("copy")
												.then(CommandManager.argument("other", IdentifierArgumentType.identifier())
														.suggests(DimensionCommand::dimensionTypeSuggestions)
														.executes(context -> dimensionTypeRepository.createDimensionType(context.getSource(), IdentifierArgumentType.getIdentifier(context, "identifier"), IdentifierArgumentType.getIdentifier(context, "copied"), getDimensionType(context, "other")))
												)
										)
								)
						)
						.then(CommandManager.literal("remove")
								.then(CommandManager.argument("type", IdentifierArgumentType.identifier())
										.suggests(DimensionCommand::customDimensionTypeSuggestions)
										.executes(context -> dimensionTypeRepository.deleteDimensionType(context.getSource(), getGeneratedDimensionType(context, "type")))
								)
						)
						.then(CommandManager.literal("list")
								.executes(context -> dimensionTypeRepository.listDimensionTypes(context.getSource()))
						)
						.then(CommandManager.literal("set")
								.then(CommandManager.argument("type", IdentifierArgumentType.identifier())
										.suggests(DimensionCommand::customDimensionTypeSuggestions)
										.then(CommandManager.argument("property", StringArgumentType.string())
												.suggests(DimensionCommand::dimensionTypePropertySuggestions)
												.then(CommandManager.argument("value", StringArgumentType.string())
														.suggests((context, builder) ->  dimensionTypePropertyValueSuggestions(context, builder, "property"))
														.executes(context -> dimensionTypeRepository.setDimensionTypeProperty(context.getSource(), getGeneratedDimensionType(context, "type"), StringArgumentType.getString(context, "property"), StringArgumentType.getString(context, "value")))
												)
										)
								)
						)
				)
				.then(CommandManager.literal("generators")
						.then(CommandManager.literal("add")
								.then(CommandManager.argument("identifier", IdentifierArgumentType.identifier())
										.then(CommandManager.literal("new")
												.then(CommandManager.argument("type", IdentifierArgumentType.identifier())
														.suggests(DimensionCommand::generatorTypeSuggestions)
														.executes(context -> generatorRepository.createGenerator(context.getSource(), IdentifierArgumentType.getIdentifier(context, "identifier"), IdentifierArgumentType.getIdentifier(context, "type"), getGeneratorCodec(context, "type")))
														.then(CommandManager.argument("seed", LongArgumentType.longArg())
																.executes(context -> generatorRepository.createGenerator(context.getSource(), IdentifierArgumentType.getIdentifier(context, "identifier"), IdentifierArgumentType.getIdentifier(context, "type"), getGeneratorCodec(context, "type"), LongArgumentType.getLong(context, "seed")))
														)
												)
										)
										.then(CommandManager.literal("copy")
												.then(CommandManager.argument("other", IdentifierArgumentType.identifier())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> generatorRepository.createGenerator(context.getSource(), IdentifierArgumentType.getIdentifier(context, "identifier"), getGenerator(context, "other")))
												)
										)
								)
						)
						.then(CommandManager.literal("remove")
								.then(CommandManager.argument("generator", IdentifierArgumentType.identifier())
										.suggests(DimensionCommand::customGeneratorSuggestions)
										.executes(context -> generatorRepository.deleteGenerator(context.getSource(), getGeneratedGenerator(context, "generator")))
								)
						)
						.then(CommandManager.literal("data")
								.then(CommandManager.literal("get")
										.then(CommandManager.argument("generator", IdentifierArgumentType.identifier())
												.suggests(DimensionCommand::generatorSuggestions)
												.executes(context -> generatorRepository.printData(context.getSource(), getGenerator(context, "generator")))
										)
								)
								.then(CommandManager.literal("modify")
										.executes(context -> {
											context.getSource().sendError(
													new LiteralText("Dimenager doesn't handle generator " +
															"editing yet; please edith the generator file manually " +
															"and reload the resources")
											);
											return 0;
										})
								)
						)
						.then(CommandManager.literal("list")
								.executes(context -> generatorRepository.listGenerators(context.getSource()))
						)
						.then(CommandManager.literal("types")
								.executes(context -> generatorRepository.listGeneratorTypes(context.getSource()))
						)
				)
		);
	}

	private static CompletableFuture<Suggestions> customDimensionSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier identifier : dimensionRepository.getGeneratedIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> dimensionTypeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier identifier : dimensionTypeRepository.getIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> customDimensionTypeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier identifier : dimensionTypeRepository.getGeneratedIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> generatorSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier identifier : generatorRepository.getIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> customGeneratorSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier identifier : generatorRepository.getGeneratedIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> generatorTypeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Identifier identifier : generatorRepository.generatorTypeIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> dimensionTypePropertySuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		for (Map.Entry<String, JsonElement> entry : DimensionTypeRepository.DEFAULT_OVERWORLD_JSON.entrySet())
			builder.suggest(entry.getKey());
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> dimensionTypePropertyValueSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder, String propertyArgument) {
		if (propertyArgument == null)
			return builder.buildFuture();
		for (Map.Entry<String, JsonElement> entry : DimensionTypeRepository.DEFAULT_OVERWORLD_JSON.entrySet())
			if (entry.getKey().equals(StringArgumentType.getString(context, propertyArgument))) {
				if (entry.getValue().isJsonPrimitive() && (((JsonPrimitive) entry.getValue()).isBoolean())) {
					// If the value type is boolean we suggest 'false' and 'true'.
					builder.suggest("false");
					builder.suggest("true");
				} else {
					// Otherwise, we suggest the default overworld value
					builder.suggest(entry.getValue().toString());
				}
				break;
			}
		return builder.buildFuture();
	}


	private static final DynamicCommandExceptionType INVALID_DIMENSION = new DynamicCommandExceptionType(identifier -> new LiteralText("Unknown dimension '" + identifier + "'"));
	private static final DynamicCommandExceptionType CONFIGURED_DIMENSION = new DynamicCommandExceptionType(identifier -> new LiteralText("Dimension '" + identifier + "' is a configured dimension - please provide a dimension created using Dimenager"));
	private static GeneratedDimension getGeneratedDimension(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
		Identifier identifier = context.getArgument(argument, Identifier.class);
		if (!dimensionRepository.contains(identifier))
			throw INVALID_DIMENSION.create(identifier);
		GeneratedDimension dimension = dimensionRepository.getGenerated(identifier);
		if (dimension == null)
			throw CONFIGURED_DIMENSION.create(identifier);
		return dimension;
	}

	private static final DynamicCommandExceptionType INVALID_DIMENSION_TYPE = new DynamicCommandExceptionType(identifier -> new LiteralText("Unknown dimension type '" + identifier + "'"));
	private static DimensionType getDimensionType(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
		Identifier identifier = context.getArgument(argument, Identifier.class);
		if (!dimensionTypeRepository.contains(identifier))
			throw INVALID_DIMENSION_TYPE.create(identifier);
		return dimensionTypeRepository.get(identifier);
	}

	private static final DynamicCommandExceptionType CONFIGURED_DIMENSION_TYPE = new DynamicCommandExceptionType(identifier -> new LiteralText("Dimension type '" + identifier + "' is a configured dimension type - please provide a type created using Dimenager"));
	private static GeneratedDimensionType getGeneratedDimensionType(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
		getDimensionType(context, argument);
		Identifier identifier = context.getArgument(argument, Identifier.class);
		if (!dimensionTypeRepository.containsGenerated(identifier))
			throw CONFIGURED_DIMENSION_TYPE.create(identifier);
		return dimensionTypeRepository.getGenerated(identifier);
	}

	private static final DynamicCommandExceptionType INVALID_GENERATOR = new DynamicCommandExceptionType(identifier -> new LiteralText("Unknown generator '" + identifier + "'"));
	private static Generator getGenerator(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
		Identifier identifier = context.getArgument(argument, Identifier.class);
		if (!generatorRepository.contains(identifier))
			throw INVALID_GENERATOR.create(identifier);
		return generatorRepository.get(identifier);
	}

	private static final DynamicCommandExceptionType CONFIGURED_GENERATOR = new DynamicCommandExceptionType(identifier -> new LiteralText("Generator '" + identifier + "' is a default generator for its type - please provide a generated created using Dimenager"));
	private static Generator getGeneratedGenerator(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
		Generator generator = getGenerator(context, argument);
		Identifier identifier = context.getArgument(argument, Identifier.class);
		if (!generatorRepository.containsGenerated(identifier))
			throw CONFIGURED_GENERATOR.create(identifier);
		return generator;
	}

	private static final DynamicCommandExceptionType INVALID_GENERATOR_TYPE = new DynamicCommandExceptionType(identifier -> new LiteralText("Unknown generator type '" + identifier + "'"));
	private static Codec<? extends ChunkGenerator> getGeneratorCodec(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
		Identifier identifier = context.getArgument(argument, Identifier.class);
		if (!generatorRepository.containsGeneratorType(identifier)) {
			throw INVALID_GENERATOR_TYPE.create(identifier);
		}
		return generatorRepository.getGeneratorType(identifier);
	}
}
