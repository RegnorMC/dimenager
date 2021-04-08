package com.beetmacol.mc.dimenager.mixin;

import com.beetmacol.mc.dimenager.dimensions.DimensionRepository;
import com.beetmacol.mc.dimenager.dimensiontypes.DimensionTypeRepository;
import com.beetmacol.mc.dimenager.generators.GeneratorRepository;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Collection;

import static com.beetmacol.mc.dimenager.Dimenager.*;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private void onServerInit(Thread thread, DynamicRegistryManager.Impl registryHolder, LevelStorage.Session levelStorageAccess, SaveProperties worldData, ResourcePackManager packRepository, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResources, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache gameProfileCache, WorldGenerationProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
		registryReadOps = RegistryOps.of(JsonOps.INSTANCE, serverResources.getResourceManager(), registryHolder);
		dimensionRepository = new DimensionRepository(serverResources.getResourceManager(), levelStorageAccess, ((MinecraftServerAccessor) this).getWorlds());
		dimensionTypeRepository = new DimensionTypeRepository(serverResources.getResourceManager(), levelStorageAccess, registryHolder.getDimensionTypes());
		generatorRepository = new GeneratorRepository(levelStorageAccess, registryHolder);
		generatorRepository.reload();
		dimensionTypeRepository.reload();
	}

	@Inject(
			method = "createWorlds",
			at = @At("TAIL")
	)
	private void onLevelsLoad(WorldGenerationProgressListener chunkProgressListener, CallbackInfo ci) {
		generatorRepository.addDimensionMirrorGenerators(((MinecraftServerAccessor) this).getWorlds());
		dimensionRepository.reload();
		dimensionRepository.createLevels(chunkProgressListener, (MinecraftServer) (Object) this);
	}

	@Inject(
			method = "method_29440",
			at = @At("TAIL")
	)
	private void onResourcesReload(Collection<String> collection, ServerResourceManager resources, CallbackInfo ci) {
		dimensionRepository.resourceManagerReload(resources.getResourceManager());
		dimensionTypeRepository.resourceManagerReload(resources.getResourceManager());
		generatorRepository.resourceManagerReload(resources.getResourceManager());
	}
}
