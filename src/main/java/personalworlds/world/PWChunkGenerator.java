package personalworlds.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class PWChunkGenerator implements IChunkGenerator {

    private final World world;
    private final Random random = new Random();

    public PWChunkGenerator(World world) {
        this.world = world;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        for (int y = 0; y < ((PWWorldProvider)world.provider).getBlocks().length; y++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    chunkPrimer.setBlockState(i, 4 + y, j, ((PWWorldProvider)world.provider).getBlocks()[y].getDefaultState());
                }
            }
        }
        Chunk chunk = new Chunk(world, chunkPrimer, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        if (((PWWorldProvider)world.provider).getConfig().isPopulate()) {
            world.provider.biomeProvider.getBiome(new BlockPos(0, 0, 0)).decorate(world, random, new BlockPos(x * 16, 0, z * 16));
        }
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        if (((PWWorldProvider)world.provider).getConfig().isPassiveSpawn()) {
            return world.provider.biomeProvider.getBiome(new BlockPos(0, 0, 0)).getSpawnableList(creatureType);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }
}
