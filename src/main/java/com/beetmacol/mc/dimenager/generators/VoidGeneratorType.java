package com.beetmacol.mc.dimenager.generators;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

import static com.beetmacol.mc.dimenager.Dimenager.MOD_ID;

public class VoidGeneratorType extends ChunkGenerator {
	public static final Codec<VoidGeneratorType> CODEC = RegistryLookupCodec.of(Registry.BIOME_KEY).xmap(VoidGeneratorType::new, VoidGeneratorType::biomes).stable().codec();
	public static final Identifier IDENTIFIER = new Identifier(MOD_ID, "void");
	private final Registry<Biome> biomes;

	public VoidGeneratorType(Registry<Biome> registry) {
		super(new FixedBiomeSource(registry.getOrThrow(BiomeKeys.OCEAN)), new StructuresConfig(false));
		this.biomes = registry;
	}

	public Registry<Biome> biomes() {
		return this.biomes;
	}

	@Override
	public Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long l) {
		return this;
	}

	@Override
	public void buildSurface(ChunkRegion worldGenRegion, Chunk chunkAccess) {
	}

	@Override
	public void populateNoise(WorldAccess levelAccessor, StructureAccessor structureFeatureManager, Chunk chunkAccess) {
	}

	@Override
	public int getHeight(int i, int j, Heightmap.Type types) {
		return 0;
	}

	@Override
	public BlockView getColumnSample(int i, int j) {
		return new VerticalBlockSample(new BlockState[0]);
	}

	@Override
	public StructuresConfig getStructuresConfig() {
		return new StructuresConfig(Optional.empty(), new HashMap<>());
	}

	@Override
	public void carve(long l, BiomeAccess biomeManager, Chunk chunkAccess, GenerationStep.Carver carving) {
	}
}
