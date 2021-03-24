package com.beetmacol.mc.dimenager.mixin;

import com.beetmacol.mc.dimenager.Dimenager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.Collections;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandMixin {

	@Shadow
	private static int teleportToPos(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, ServerLevel serverLevel, Coordinates coordinates, @Nullable Coordinates coordinates2, @Nullable TeleportCommand.LookAt lookAt) {
		throw new IllegalStateException();
	}

	@Inject(
			method = "register",
			at = @At("TAIL"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CallbackInfo ci, LiteralCommandNode<CommandSourceStack> teleportCommandNode) {
		if (Dimenager.dimenagerConfiguration.isModifyTpCommand()) {
			// tp <dimension> ...
			teleportCommandNode
					.addChild(Commands.argument("dimension", DimensionArgument.dimension())
							.executes(context -> teleport(context.getSource(), DimensionArgument.getDimension(context, "dimension"), null, null))
							.then(Commands.argument("location", Vec3Argument.vec3())
									.executes(context -> teleport(context.getSource(), DimensionArgument.getDimension(context, "dimension"), Vec3Argument.getCoordinates(context, "location"), null))
									.then(Commands.argument("rotation", RotationArgument.rotation())
											.executes(context -> teleport(context.getSource(), DimensionArgument.getDimension(context, "dimension"), Vec3Argument.getCoordinates(context, "location"), RotationArgument.getRotation(context, "rotation")))
									)
							)
							.build());

			// tp <targets> <dimension> ...
			teleportCommandNode
					.getChild("targets").addChild(Commands.argument("dimension", DimensionArgument.dimension())
					.executes(context -> teleport(context.getSource(), EntityArgument.getEntities(context, "targets"), DimensionArgument.getDimension(context, "dimension"), null, null))
					.then(Commands.argument("location", Vec3Argument.vec3())
							.executes(context -> teleport(context.getSource(), EntityArgument.getEntities(context, "targets"), DimensionArgument.getDimension(context, "dimension"), Vec3Argument.getCoordinates(context, "location"), null))
							.then(Commands.argument("rotation", RotationArgument.rotation())
									.executes(context -> teleport(context.getSource(), EntityArgument.getEntities(context, "targets"), DimensionArgument.getDimension(context, "dimension"), Vec3Argument.getCoordinates(context, "location"), RotationArgument.getRotation(context, "rotation")))
							)
					)
					.build());

			// tp <location> <rotation>
			teleportCommandNode
					.getChild("location").addChild(Commands.argument("rotation", RotationArgument.rotation())
					.executes(context -> teleport(context.getSource(), context.getSource().getLevel(), Vec3Argument.getCoordinates(context, "location"), RotationArgument.getRotation(context, "rotation")))
					.build());

			Dimenager.LOGGER.info("Added the 'dimension' arguments to the `/teleport` command; You can disable this in the mod's configuration file");
		}
	}

	private static int teleport(CommandSourceStack source, Collection<? extends Entity> targets, ServerLevel dimension, @Nullable Coordinates location, @Nullable Coordinates rotation) {
		Coordinates coordinates = location != null ? location : getRelative0();
		if (coordinates instanceof WorldCoordinates) {
			double scale = DimensionType.getTeleportationScale(source.getLevel().dimensionType(), dimension.dimensionType());
			if (scale != 1 && (coordinates.isXRelative() || coordinates.isZRelative())) {
				// This position is wrong because it doesn't include the scale. We will replace the coordinates with absolute ones calculated below.
				Vec3 wrongPos = coordinates.getPosition(source);
				// We scale the source's position, and sum it with the untouched relative position.
				coordinates = new WorldCoordinates(
						new WorldCoordinate(false, !coordinates.isXRelative() ? wrongPos.x : source.getPosition().x * scale + ((WorldCoordinatesAccessor) coordinates).getX().get(0)),
						new WorldCoordinate(false, wrongPos.y),
						new WorldCoordinate(false, !coordinates.isZRelative() ? wrongPos.z : source.getPosition().z * scale + ((WorldCoordinatesAccessor) coordinates).getZ().get(0))
				);
			}
		}
		return teleportToPos(source, targets, dimension, coordinates, rotation, null);
	}

	private static int teleport(CommandSourceStack source, ServerLevel dimension, @Nullable Coordinates location, @Nullable Coordinates rotation) throws CommandSyntaxException {
		return teleport(source, Collections.singleton(source.getEntityOrException()), dimension, location, rotation);
	}

	private static Coordinates getRelative0() {
		WorldCoordinate relative = new WorldCoordinate(true, 0);
		return new WorldCoordinates(relative, relative, relative);
	}
}
