package com.beetmacol.mc.dimenager.commands;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.dimensions.GeneratedDimension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.concurrent.CompletableFuture;

public class DimensionCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> dimensionCommandNode = dispatcher.register(Commands.literal("dimension")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("worlds")
						.then(Commands.literal("add")
								.then(Commands.argument("identifier", ResourceLocationArgument.id())
										.then(Commands.argument("type", ResourceLocationArgument.id())
												.suggests(DimensionCommand::dimensionTypeSuggestions)
												.then(Commands.argument("generator", ResourceLocationArgument.id())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> Dimenager.dimensionRepository.createDimension(context.getSource(), ResourceLocationArgument.getId(context, "identifier"), getDimensionType(context, "type"), ResourceLocationArgument.getId(context, "type")))
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("dimension", DimensionArgument.dimension())
										.suggests(DimensionCommand::customDimensionSuggestions)
										.executes(context -> Dimenager.dimensionRepository.deleteDimension(context.getSource(), getGeneratedDimension(context, "dimension")))
								)
						)
						.then(Commands.literal("list")
								.executes(context -> Dimenager.dimensionRepository.listDimensions(context.getSource()))
						)
				)
				.then(Commands.literal("types")
						.then(Commands.literal("add")
								.then(Commands.argument("identifier", ResourceLocationArgument.id())
										.executes(context -> 0)
										.then(Commands.literal("copy")
												.then(Commands.argument("other", ResourceLocationArgument.id())
														.suggests(DimensionCommand::dimensionTypeSuggestions)
														.executes(context -> 0)
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("type", ResourceLocationArgument.id())
										.suggests(DimensionCommand::customDimensionTypeSuggestions)
										.executes(context -> 0)
								)
						)
						.then(Commands.literal("list")
								.executes(context -> 0)
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
										.then(Commands.argument("type", ResourceLocationArgument.id())
												.suggests(DimensionCommand::generatorTypeSuggestions)
												.executes(context -> 0)
										)
										.then(Commands.literal("copy")
												.then(Commands.argument("other", ResourceLocationArgument.id())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> 0)
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("generator", ResourceLocationArgument.id())
										.suggests(DimensionCommand::customGeneratorSuggestions)
										.executes(context -> 0)
								)
						)
						.then(Commands.literal("data")
								// TODO `/dimension generators data`. Here and in README.md.
						)
						.then(Commands.literal("list")
								.executes(context -> 0)
						)
						.then(Commands.literal("types")
								.executes(context -> 0)
						)
				)
		);
	}

	private static CompletableFuture<Suggestions> customDimensionSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> dimensionTypeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> customDimensionTypeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> generatorSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return builder.buildFuture();
	}

	private static CompletableFuture<Suggestions> customGeneratorSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return builder.buildFuture();
	}


	private static CompletableFuture<Suggestions> generatorTypeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		return builder.buildFuture();
	}

	private static final DynamicCommandExceptionType INVALID_DIMENSION = new DynamicCommandExceptionType(identifier -> new TextComponent("Unknown dimension '" + identifier + "'"));
	private static final DynamicCommandExceptionType CONFIGURED_DIMENSION = new DynamicCommandExceptionType(identifier -> new TextComponent("Dimension '" + identifier + "' is a configured dimension - please provide a dimension created using Dimenager"));
	private static GeneratedDimension getGeneratedDimension(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		if (!Dimenager.dimensionRepository.contains(identifier))
			throw INVALID_DIMENSION.create(identifier);
		GeneratedDimension dimension = Dimenager.dimensionRepository.getGeneratedDimension(identifier);
		if (dimension == null)
			throw CONFIGURED_DIMENSION.create(identifier);
		return dimension;
	}

	private static final DynamicCommandExceptionType INVALID_DIMENSION_TYPE = new DynamicCommandExceptionType(identifier -> new TextComponent("Unknown dimension type '" + identifier + "'"));
	private static DimensionType getDimensionType(CommandContext<CommandSourceStack> context, String argument) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(argument, ResourceLocation.class);
		Registry<DimensionType> dimensionTypeRegistry = context.getSource().getServer().registryAccess().dimensionTypes();
		if (!dimensionTypeRegistry.containsKey(identifier))
			throw INVALID_DIMENSION_TYPE.create(identifier);
		return dimensionTypeRegistry.get(identifier);
	}
}
