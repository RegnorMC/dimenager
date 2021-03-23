package com.beetmacol.mc.dimensions.dimensions;

import com.beetmacol.mc.dimensions.mixin.MinecraftServerAccessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DerivedLevelData;

import java.util.Collection;
import java.util.Random;

public class DimensionManagement {
	public static int createDimension(CommandSourceStack source, ResourceLocation identifier, DimensionType dimensionType) {
		ResourceKey<Level> resourceKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, identifier);
		MinecraftServer server = source.getServer();
		MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor) server;
		DerivedLevelData derivedLevelData = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());
		WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
		ServerLevel serverLevel = new ServerLevel(server, serverAccessor.getExecutor(), serverAccessor.getStorageSource(), derivedLevelData, resourceKey, dimensionType, null, WorldGenSettings.makeDefaultOverworld(server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), server.registryAccess().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong()), worldGenSettings.isDebug(), BiomeManager.obfuscateSeed(worldGenSettings.seed()), ImmutableList.of(), false);
		if (source.getEntity() != null) {
			source.getEntity().changeDimension(serverLevel);
		}
		return 1;
	}

	public static int listDimensions(CommandSourceStack source) {
		Collection<ServerLevel> dimensions = (Collection<ServerLevel>) source.getServer().getAllLevels();
		if (dimensions.isEmpty())
			source.sendSuccess(new TextComponent("There are no dimensions"), false);
		else
			source.sendSuccess(new TextComponent("There are " + dimensions.size() + " dimensions: ").append(ComponentUtils.formatList(dimensions, dimension -> new TextComponent(dimension.toString()))), false);
		return dimensions.size();
	}
}
