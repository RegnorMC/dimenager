package com.beetmacol.mc.dimenager.mixin;

import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {
	@Accessor("DEFAULT_OVERWORLD")
	static DimensionType getDefaultOverworld() {
		throw new AssertionError();
	}
}
