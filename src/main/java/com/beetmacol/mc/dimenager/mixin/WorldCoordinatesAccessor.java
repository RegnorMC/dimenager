package com.beetmacol.mc.dimenager.mixin;

import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldCoordinates.class)
public interface WorldCoordinatesAccessor {
	// The warnings are appearing because the MC Dev plugin doesn't recognise `x` and `y` when they are upper case for some reason, but changing them to lowercase makes the mixin crash
	@SuppressWarnings("AccessorTarget")
	@Accessor @Final
	WorldCoordinate getX();
	@SuppressWarnings("AccessorTarget")
	@Accessor @Final
	WorldCoordinate getZ();
}
