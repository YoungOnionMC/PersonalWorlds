package personalworlds.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.FMLCommonHandler;
import personalworlds.PersonalWorlds;
import personalworlds.proxy.CommonProxy;

import java.util.Arrays;

public class PWWorldProvider extends WorldProvider {

    private Config config;

    public PWWorldProvider() {}

    public Config getConfig() {
        if (this.config == null) {
            boolean isClient = (this.world != null) ? this.world.isRemote : FMLCommonHandler.instance().getEffectiveSide().isClient();
            this.config = Config.getForDimension(this.getDimension(), isClient);
            if(this.config == null) {
                PersonalWorlds.log.fatal("PersonalSpace couldn't find dimension config for dimension {}, detected side: {}\nknown client dimension IDs: {}\nknown server dimension IDs: {}\n",
                        this.getDimension(),
                        isClient ? "CLIENT" : "SERVER",
                        Arrays.toString(CommonProxy.getDimensionConfigs(true).keys()),
                        Arrays.toString(CommonProxy.getDimensionConfigs(false).keys()),
                        new Throwable());
            }
        }
        return this.config;
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.OVERWORLD;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new PWChunkGenerator(this.world);
    }

    @Override
    public String getSaveFolder() {
        return "personal_world_" + this.getDimension();
    }

    @Override
    public float getStarBrightness(float par1) {
        return getConfig().getStarsVisibility();
    }

    @Override
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        int color = getConfig().getSkyColor();

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        return new Vec3d(red, green, blue);
    }

    @Override
    public Vec3d getSkyColor(Entity cameraEntity, float partialTicks) {
        return getFogColor(0.0F, partialTicks);
    }

    @Override
    public float getCloudHeight() {
        return getConfig().isClouds() ? 256.0F : Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isSurfaceWorld() {
        return true;
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        BlockPos blockPos = this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        return this.world.getBlockState(blockPos).getMaterial().blocksMovement();
    }

    @Override
    public void updateWeather() {
        if (getConfig().isWeather()) {
            super.updateWeather();
        }
    }
}
