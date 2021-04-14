package net.regnormc.dimenager.mixin;

import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.command.argument.DefaultPosArgument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DefaultPosArgument.class)
public interface WorldCoordinatesAccessor {
	@Accessor @Final
	CoordinateArgument getX();
	@Accessor @Final
	CoordinateArgument getZ();
}
