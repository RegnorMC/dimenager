package net.regnormc.dimenager.mixin;

import net.regnormc.dimenager.Dimenager;
import net.regnormc.dimenager.commands.DimensionCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
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
	private static int execute(ServerCommandSource commandSourceStack, Collection<? extends Entity> collection, ServerWorld serverLevel, PosArgument coordinates, @Nullable PosArgument coordinates2, @Nullable TeleportCommand.LookTarget facingLocation) {
		throw new IllegalStateException();
	}

	@Inject(
			method = "register",
			at = @At("TAIL"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CallbackInfo ci, LiteralCommandNode<ServerCommandSource> teleportCommandNode) {
		if (Dimenager.configuration.isModifyTpCommand()) {
			// tp <dimension> ...
			teleportCommandNode
					.addChild(CommandManager.argument("dimension", DimensionArgumentType.dimension())
							.suggests(DimensionCommand::loadedDimensionSuggestions)
							.executes(context -> teleport(context.getSource(), DimensionArgumentType.getDimensionArgument(context, "dimension"), null, null))
							.then(CommandManager.argument("location", Vec3ArgumentType.vec3())
									.executes(context -> teleport(context.getSource(), DimensionArgumentType.getDimensionArgument(context, "dimension"), Vec3ArgumentType.getPosArgument(context, "location"), null))
									.then(CommandManager.argument("rotation", RotationArgumentType.rotation())
											.executes(context -> teleport(context.getSource(), DimensionArgumentType.getDimensionArgument(context, "dimension"), Vec3ArgumentType.getPosArgument(context, "location"), RotationArgumentType.getRotation(context, "rotation")))
									)
							)
							.build());

			// tp <targets> <dimension> ...
			teleportCommandNode
					.getChild("targets").addChild(CommandManager.argument("dimension", DimensionArgumentType.dimension())
							.suggests(DimensionCommand::loadedDimensionSuggestions)
							.executes(context -> teleport(context.getSource(), EntityArgumentType.getEntities(context, "targets"), DimensionArgumentType.getDimensionArgument(context, "dimension"), null, null))
							.then(CommandManager.argument("location", Vec3ArgumentType.vec3())
									.executes(context -> teleport(context.getSource(), EntityArgumentType.getEntities(context, "targets"), DimensionArgumentType.getDimensionArgument(context, "dimension"), Vec3ArgumentType.getPosArgument(context, "location"), null))
									.then(CommandManager.argument("rotation", RotationArgumentType.rotation())
											.executes(context -> teleport(context.getSource(), EntityArgumentType.getEntities(context, "targets"), DimensionArgumentType.getDimensionArgument(context, "dimension"), Vec3ArgumentType.getPosArgument(context, "location"), RotationArgumentType.getRotation(context, "rotation")))
									)
							)
					.build());

			// tp <location> <rotation>
			teleportCommandNode
					.getChild("location").addChild(CommandManager.argument("rotation", RotationArgumentType.rotation())
					.executes(context -> teleport(context.getSource(), context.getSource().getWorld(), Vec3ArgumentType.getPosArgument(context, "location"), RotationArgumentType.getRotation(context, "rotation")))
					.build());

			Dimenager.LOGGER.info("Added the 'dimension' arguments to the `/teleport` command; You can disable this in the mod's configuration file");
		}
	}

	private static int teleport(ServerCommandSource source, Collection<? extends Entity> targets, ServerWorld dimension, @Nullable PosArgument location, @Nullable PosArgument rotation) {
		PosArgument coordinates = location != null ? location : getRelative0();
		if (coordinates instanceof DefaultPosArgument) {
			double scale = DimensionType.method_31109(source.getWorld().getDimension(), dimension.getDimension());
			if (scale != 1 && (coordinates.isXRelative() || coordinates.isZRelative())) {
				// This position is wrong because it doesn't include the scale. We will replace the coordinates with absolute ones calculated below.
				Vec3d wrongPos = coordinates.toAbsolutePos(source);
				// We scale the source's position, and sum it with the untouched relative position.
				coordinates = new DefaultPosArgument(
						new CoordinateArgument(false, !coordinates.isXRelative() ? wrongPos.x : source.getPosition().x * scale + ((WorldCoordinatesAccessor) coordinates).getX().toAbsoluteCoordinate(0)),
						new CoordinateArgument(false, wrongPos.y),
						new CoordinateArgument(false, !coordinates.isZRelative() ? wrongPos.z : source.getPosition().z * scale + ((WorldCoordinatesAccessor) coordinates).getZ().toAbsoluteCoordinate(0))
				);
			}
		}
		return execute(source, targets, dimension, coordinates, rotation, null);
	}

	private static int teleport(ServerCommandSource source, ServerWorld dimension, @Nullable PosArgument location, @Nullable PosArgument rotation) throws CommandSyntaxException {
		return teleport(source, Collections.singleton(source.getEntityOrThrow()), dimension, location, rotation);
	}

	private static PosArgument getRelative0() {
		CoordinateArgument relative = new CoordinateArgument(true, 0);
		return new DefaultPosArgument(relative, relative, relative);
	}
}
