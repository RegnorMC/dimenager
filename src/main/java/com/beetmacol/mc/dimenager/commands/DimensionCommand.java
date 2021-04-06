package com.beetmacol.mc.dimenager.commands;

import com.beetmacol.mc.dimenager.dimensions.GeneratedDimension;
import com.beetmacol.mc.dimenager.dimensiontypes.GeneratedDimensionType;
import com.beetmacol.mc.dimenager.generators.Generator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.concurrent.CompletableFuture;

import static com.beetmacol.mc.dimenager.Dimenager.*;

@SuppressWarnings("SameParameterValue")
public class DimensionCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("dimension")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("worlds")
						.then(Commands.literal("add")
								.then(Commands.argument("identifier", ResourceLocationArgument.id())
										.then(Commands.argument("type", ResourceLocationArgument.id())
												.suggests(DimensionCommand::dimensionTypeSuggestions)
												.then(Commands.argument("generator", ResourceLocationArgument.id())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> dimensionRepository.createDimension(context.getSource(), ResourceLocationArgument.getId(context, "identifier"), getDimensionType(context, "type"), ResourceLocationArgument.getId(context, "type"), getGenerator(context, "generator")))
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("dimension", DimensionArgument.dimension())
										.suggests(DimensionCommand::customDimensionSuggestions)
										.executes(context -> dimensionRepository.deleteDimension(context.getSource(), getGeneratedDimension(context, "dimension")))
								)
						)
						.then(Commands.literal("list")
								.executes(context -> dimensionRepository.listDimensions(context.getSource()))
						)
				)
				.then(Commands.literal("types")
						.then(Commands.literal("add")
								.then(Commands.argument("identifier", ResourceLocationArgument.id())
										.executes(context -> dimensionTypeRepository.createDimensionType(context.getSource(), ResourceLocationArgument.getId(context, "identifier")))
										.then(Commands.literal("copy")
												.then(Commands.argument("other", ResourceLocationArgument.id())
														.suggests(DimensionCommand::dimensionTypeSuggestions)
														.executes(context -> dimensionTypeRepository.createDimensionType(context.getSource(), ResourceLocationArgument.getId(context, "identifier"), ResourceLocationArgument.getId(context, "copied"), getDimensionType(context, "other")))
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("type", ResourceLocationArgument.id())
										.suggests(DimensionCommand::customDimensionTypeSuggestions)
										.executes(context -> dimensionTypeRepository.deleteDimensionType(context.getSource(), getGeneratedDimensionType(context, "type")))
								)
						)
						.then(Commands.literal("list")
								.executes(context -> dimensionTypeRepository.listDimensionTypes(context.getSource()))
						)
						.then(Commands.literal("set")
								.then(Commands.argument("property", StringArgumentType.string())
										.then(Commands.argument("value", StringArgumentType.string()) // TODO suggestions
												.executes(context -> 0)
										)
								)
						)
				)
				.then(Commands.literal("generators")
						.then(Commands.literal("add")
								.then(Commands.argument("identifier", ResourceLocationArgument.id())
										.then(Commands.literal("new")
												.then(Commands.argument("type", ResourceLocationArgument.id())
														.suggests(DimensionCommand::generatorTypeSuggestions)
														.executes(context -> generatorRepository.createGenerator(context.getSource(), ResourceLocationArgument.getId(context, "identifier"), ResourceLocationArgument.getId(context, "type"), getGeneratorCodec(context, "type")))
														.then(Commands.argument("seed", LongArgumentType.longArg())
																.executes(context -> generatorRepository.createGenerator(context.getSource(), ResourceLocationArgument.getId(context, "identifier"), ResourceLocationArgument.getId(context, "type"), getGeneratorCodec(context, "type"), LongArgumentType.getLong(context, "seed")))
														)
												)
										)
										.then(Commands.literal("copy")
												.then(Commands.argument("other", ResourceLocationArgument.id())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> generatorRepository.createGenerator(context.getSource(), ResourceLocationArgument.getId(context, "identifier"), getGenerator(context, "other")))
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("generator", ResourceLocationArgument.id())
										.suggests(DimensionCommand::customGeneratorSuggestions)
										.executes(context -> generatorRepository.deleteGenerator(context.getSource(), getGeneratedGenerator(context, "generator")))
								)
						)
						.then(Commands.literal("data")
								.then(Commands.literal("get")
										.then(Commands.argument("generator", ResourceLocationArgument.id())
												.suggests(DimensionCommand::generatorSuggestions)
												.executes(context -> generatorRepository.printData(context.getSource(), getGenerator(context, "generator")))
										)
								)
								.then(Commands.literal("modify")
										.executes(context -> {
											context.getSource().sendFailure(
													new TextComponent("Dimenager doesn't handle generator " +
															"editing yet; please edith the generator file manually " +
															"and reload the resources")
											);
											return 0;
										})
								)
						)
						.then(Commands.literal("list")
								.executes(context -> generatorRepository.listGenerators(context.getSource()))
						)
						.then(Commands.literal("types")
								.executes(context -> generatorRepository.listGeneratorTypes(context.getSource()))
						)
				)
		);
	}

	private static CompletableFuture<Suggestions> customDimensionSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		for (ResourceLocation identifier : dimensionRepository.getGeneratedIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> dimensionTypeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		for (ResourceLocation identifier : dimensionTypeRepository.getIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> customDimensionTypeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		for (ResourceLocation identifier : dimensionTypeRepository.getGeneratedIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> generatorSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		for (ResourceLocation identifier : generatorRepository.getIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> customGeneratorSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		for (ResourceLocation identifier : generatorRepository.getGeneratedIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> generatorTypeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		for (ResourceLocation identifier : generatorRepository.generatorTypeIdentifiers())
			builder.suggest(identifier.toString());
		return builder.buildFuture();
	}

	private static final DynamicCommandExceptionType INVALID_DIMENSION = new DynamicCommandExceptionType(identifier -> new TextComponent("Unknown dimension '" + identifier + "'"));
	private static final DynamicCommandExceptionType CONFIGURED_DIMENSION = new DynamicCommandExceptionType(identifier -> new TextComponent("Dimension '" + identifier + "' is a configured dimension - please provide a dimension created using Dimenager"));
	private static GeneratedDimension getGeneratedDimension(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!dimensionRepository.contains(identifier))
			throw INVALID_DIMENSION.create(identifier);
		GeneratedDimension dimension = dimensionRepository.getGenerated(identifier);
		if (dimension == null)
			throw CONFIGURED_DIMENSION.create(identifier);
		return dimension;
	}

	private static final DynamicCommandExceptionType INVALID_DIMENSION_TYPE = new DynamicCommandExceptionType(identifier -> new TextComponent("Unknown dimension type '" + identifier + "'"));
	private static DimensionType getDimensionType(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!dimensionTypeRepository.contains(identifier))
			throw INVALID_DIMENSION_TYPE.create(identifier);
		return dimensionTypeRepository.get(identifier);
	}

	private static final DynamicCommandExceptionType CONFIGURED_DIMENSION_TYPE = new DynamicCommandExceptionType(identifier -> new TextComponent("Dimension type '" + identifier + "' is a configured dimension type - please provide a type created using Dimenager"));
	private static GeneratedDimensionType getGeneratedDimensionType(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		getDimensionType(context, argument);
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!dimensionTypeRepository.containsGenerated(identifier))
			throw CONFIGURED_GENERATOR.create(identifier);
		return dimensionTypeRepository.getGenerated(identifier);
	}

	private static final DynamicCommandExceptionType INVALID_GENERATOR = new DynamicCommandExceptionType(identifier -> new TextComponent("Unknown generator '" + identifier + "'"));
	private static Generator getGenerator(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!generatorRepository.contains(identifier))
			throw INVALID_GENERATOR.create(identifier);
		return generatorRepository.get(identifier);
	}

	private static final DynamicCommandExceptionType CONFIGURED_GENERATOR = new DynamicCommandExceptionType(identifier -> new TextComponent("Generator '" + identifier + "' is a default generator for its type - please provide a generated created using Dimenager"));
	private static Generator getGeneratedGenerator(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		Generator generator = getGenerator(context, argument);
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!generatorRepository.containsGenerated(identifier))
			throw CONFIGURED_GENERATOR.create(identifier);
		return generator;
	}

	private static final DynamicCommandExceptionType INVALID_GENERATOR_TYPE = new DynamicCommandExceptionType(identifier -> new TextComponent("Unknown generator type '" + identifier + "'"));
	private static Codec<? extends ChunkGenerator> getGeneratorCodec(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!generatorRepository.containsGeneratorType(identifier)) {
			throw INVALID_GENERATOR_TYPE.create(identifier);
		}
		return generatorRepository.getGeneratorType(identifier);
	}
}
