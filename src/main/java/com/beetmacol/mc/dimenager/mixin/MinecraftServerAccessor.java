package com.beetmacol.mc.dimenager.mixin;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	@Accessor
	LevelStorageSource.LevelStorageAccess getStorageSource();

	@Accessor
	Executor getExecutor();

	@Accessor
	Map<ResourceKey<Level>, ServerLevel> getLevels();
}
