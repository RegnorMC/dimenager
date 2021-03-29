package com.beetmacol.mc.dimenager.generators;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;

import java.util.HashMap;
import java.util.Optional;

import static com.beetmacol.mc.dimenager.Dimenager.MOD_ID;

public class VoidGeneratorType extends ChunkGenerator {
	public static final Codec<VoidGeneratorType> CODEC = RegistryLookupCodec.create(Registry.BIOME_REGISTRY).xmap(VoidGeneratorType::new, VoidGeneratorType::biomes).stable().codec();
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(MOD_ID, "void");
	private final Registry<Biome> biomes;

	public VoidGeneratorType(Registry<Biome> registry) {
		super(new FixedBiomeSource(registry.getOrThrow(Biomes.THE_VOID)), new StructureSettings(false));
		this.biomes = registry;
	}

	public Registry<Biome> biomes() {
		return this.biomes;
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long l) {
		return this;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
	}

	@Override
	public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types) {
		return 0;
	}

	@Override
	public BlockGetter getBaseColumn(int i, int j) {
		return new NoiseColumn(new BlockState[0]);
	}

	@Override
	public StructureSettings getSettings() {
		return new StructureSettings(Optional.empty(), new HashMap<>());
	}

	@Override
	public void applyCarvers(long l, BiomeManager biomeManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
	}
}
