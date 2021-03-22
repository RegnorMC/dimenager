package com.beetmacol.mc.dimensions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import java.util.concurrent.CompletableFuture;

public class DimensionCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("dimension")
				.then(Commands.literal("worlds")
						.then(Commands.literal("add")
								.then(Commands.argument("identifier", ResourceLocationArgument.id())
										.then(Commands.argument("type", ResourceLocationArgument.id())
												.suggests(DimensionCommand::dimensionTypeSuggestions)
												.then(Commands.argument("generator", ResourceLocationArgument.id())
														.suggests(DimensionCommand::generatorSuggestions)
														.executes(context -> 0)
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("dimension", DimensionArgument.dimension())
										.suggests(DimensionCommand::customDimensionSuggestions)
										.executes(context -> 0)
								)
						)
						.then(Commands.literal("list")
								.executes(context -> 0)
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
						.then(Commands.literal("modify")
								// TODO `/dimension generators modify`. Here and in README.md.
						)
						.then(Commands.literal("lists")
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
}
