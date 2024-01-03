package personalworlds.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class PWChunkGenerator implements IChunkGenerator {

    private final World world;
    private final Random random;
    private final DimensionConfig dimensionConfig;

    public PWChunkGenerator(World world) {
        this.world = world;
        this.random = world.rand;
        this.dimensionConfig = ((PWWorldProvider) world.provider).getConfig();
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        int y = 0;
        if (!dimensionConfig.getLayers().isEmpty()) {
            for (FlatLayerInfo fli : dimensionConfig.getLayers()) {
                Block block = fli.getLayerMaterial().getBlock();
                if (block == null || block == Blocks.AIR) {
                    y += fli.getLayerCount();
                    continue;
                }
                for (; y < fli.getMinY() + fli.getLayerCount() && y < world.getHeight(); ++y) {
                    for (int i = 0; i < 16; i++) {
                        for (int j = 0; j < 16; j++) {
                            chunkPrimer.setBlockState(i, y, j, fli.getLayerMaterial());
                        }
                    }
                }
            }
        }
        Chunk chunk = new Chunk(world, chunkPrimer, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        if (dimensionConfig.generateVegetation()) {
            world.provider.getBiomeProvider().getBiome(new BlockPos(0, 0, 0)).decorate(world, random,
                    new BlockPos(x * 16, 0, z * 16));
        }

        if (dimensionConfig.generateTrees() && TerrainGen.decorate(
                world,
                random,
                new ChunkPos(x, z),
                DecorateBiomeEvent.Decorate.EventType.TREE)) {
            x = x + random.nextInt(16) + 8;
            z = z + random.nextInt(16) + 8;
            int y = this.world.getHeight(x, z);
            WorldGenAbstractTree worldgenabstracttree = Biomes.PLAINS.getRandomTreeFeature(random);
            if (worldgenabstracttree.generate(world, random, new BlockPos(x, y, z))) {
                worldgenabstracttree.generate(world, random, new BlockPos(x, y, z));
            }
        }
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (dimensionConfig.passiveSpawn()) {
            return world.provider.getBiomeProvider().getBiome(new BlockPos(0, 0, 0)).getSpawnableList(creatureType);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position,
                                           boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {}

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }
}
