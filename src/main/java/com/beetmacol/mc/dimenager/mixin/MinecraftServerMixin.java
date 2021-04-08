package com.beetmacol.mc.dimenager.mixin;

import com.beetmacol.mc.dimenager.Dimenager;
import com.beetmacol.mc.dimenager.dimensions.DimensionRepository;
import com.beetmacol.mc.dimenager.dimensiontypes.DimensionTypeRepository;
import com.beetmacol.mc.dimenager.generators.GeneratorRepository;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Collection;

import static com.beetmacol.mc.dimenager.Dimenager.*;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Shadow private ServerResources resources;

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private void onServerInit(Thread thread, RegistryAccess.RegistryHolder registryHolder, LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldData worldData, PackRepository packRepository, Proxy proxy, DataFixer dataFixer, ServerResources serverResources, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, GameProfileCache gameProfileCache, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
		Dimenager.registryReadOps = RegistryReadOps.create(JsonOps.INSTANCE, resources.getResourceManager(), registryHolder);
		dimensionRepository = new DimensionRepository(serverResources.getResourceManager(), levelStorageAccess, ((MinecraftServerAccessor) this).getLevels());
		dimensionTypeRepository = new DimensionTypeRepository(resources.getResourceManager(), levelStorageAccess, registryHolder.dimensionTypes());
		generatorRepository = new GeneratorRepository(levelStorageAccess, registryHolder);
		generatorRepository.reload();
		dimensionTypeRepository.reload();
	}

	@Inject(
			method = "createLevels",
			at = @At("TAIL")
	)
	private void onLevelsLoad(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
		generatorRepository.addDimensionMirrorGenerators(((MinecraftServerAccessor) this).getLevels());
		dimensionRepository.reload();
		dimensionRepository.createLevels(chunkProgressListener, (MinecraftServer) (Object) this);
	}

	// MC Dev plugin doesn't recognise lambdas
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(
			method = "lambda$reloadResources$10",
			at = @At("TAIL")
	)
	private void onResourcesReload(Collection<String> collection, ServerResources resources, CallbackInfo ci) {
		dimensionRepository.resourceManagerReload(resources.getResourceManager());
		dimensionTypeRepository.resourceManagerReload(resources.getResourceManager());
		generatorRepository.resourceManagerReload(resources.getResourceManager());
	}
}
