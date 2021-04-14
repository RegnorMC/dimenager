package net.regnormc.dimenager.mixin;

import net.regnormc.dimenager.dimensions.DimensionRepository;
import net.regnormc.dimenager.dimensiontypes.DimensionTypeRepository;
import net.regnormc.dimenager.generators.GeneratorRepository;
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
import net.regnormc.dimenager.Dimenager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.Collection;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private void onServerInit(Thread thread, DynamicRegistryManager.Impl registryHolder, LevelStorage.Session levelStorageAccess, SaveProperties worldData, ResourcePackManager packRepository, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResources, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache gameProfileCache, WorldGenerationProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
		Dimenager.registryReadOps = RegistryOps.of(JsonOps.INSTANCE, serverResources.getResourceManager(), registryHolder);
		Dimenager.dimensionRepository = new DimensionRepository(serverResources.getResourceManager(), levelStorageAccess, ((MinecraftServerAccessor) this).getWorlds());
		Dimenager.dimensionTypeRepository = new DimensionTypeRepository(serverResources.getResourceManager(), levelStorageAccess, registryHolder.getDimensionTypes());
		Dimenager.generatorRepository = new GeneratorRepository(levelStorageAccess, registryHolder);
		Dimenager.generatorRepository.reload();
		Dimenager.dimensionTypeRepository.reload();
	}

	@Inject(
			method = "createWorlds",
			at = @At("TAIL")
	)
	private void onLevelsLoad(WorldGenerationProgressListener chunkProgressListener, CallbackInfo ci) {
		Dimenager.generatorRepository.addDimensionMirrorGenerators(((MinecraftServerAccessor) this).getWorlds());
		Dimenager.dimensionRepository.reload();
		Dimenager.dimensionRepository.createLevels(chunkProgressListener, (MinecraftServer) (Object) this);
	}

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(
			method = "method_29440",
			at = @At("TAIL")
	)
	private void onResourcesReload(Collection<String> collection, ServerResourceManager resources, CallbackInfo ci) {
		Dimenager.dimensionRepository.resourceManagerReload(resources.getResourceManager());
		Dimenager.dimensionTypeRepository.resourceManagerReload(resources.getResourceManager());
		Dimenager.generatorRepository.resourceManagerReload(resources.getResourceManager());
	}
}
