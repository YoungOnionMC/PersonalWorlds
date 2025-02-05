package personalworlds;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import personalworlds.world.DimensionConfig;

@Config(modid = PWValues.modID)
public class PWConfig {

    public static class Values {

        public static String[] allowedBlocks;

        public static String[] allowedBiomes;

        public static String[] presets;
    }

    @Config.Comment("Blocks allowed to be used in an Flat World.")
    public static String[] allowedBlocks = {
            "minecraft:stone",
            "minecraft:dirt",
            "minecraft:grass",
            "minecraft:cobblestone",
            "minecraft:stonebrick",
            "minecraft:stone_slab",
            "minecraft:stained_hardened_clay",
            "minecraft:stained_hardened_clay:1",
            "minecraft:stained_hardened_clay:2",
            "minecraft:stained_hardened_clay:3",
            "minecraft:stained_hardened_clay:4",
            "minecraft:stained_hardened_clay:5",
            "minecraft:stained_hardened_clay:6",
            "minecraft:stained_hardened_clay:7",
            "minecraft:stained_hardened_clay:8",
            "minecraft:stained_hardened_clay:9",
            "minecraft:stained_hardened_clay:10",
            "minecraft:stained_hardened_clay:11",
            "minecraft:stained_hardened_clay:12",
            "minecraft:stained_hardened_clay:13",
            "minecraft:stained_hardened_clay:14",
            "minecraft:stained_hardened_clay:15",
            "minecraft:hardened_clay",
            "minecraft:concrete",
            "minecraft:concrete:1",
            "minecraft:concrete:2",
            "minecraft:concrete:3",
            "minecraft:concrete:4",
            "minecraft:concrete:5",
            "minecraft:concrete:6",
            "minecraft:concrete:7",
            "minecraft:concrete:8",
            "minecraft:concrete:9",
            "minecraft:concrete:10",
            "minecraft:concrete:11",
            "minecraft:concrete:12",
            "minecraft:concrete:13",
            "minecraft:concrete:14",
            "minecraft:concrete:15"
    };

    @Config.Comment("Biomes allowed to be used in an Personal World.")
    public static String[] allowedBiomes = {
            "minecraft:plains",
            "minecraft:void",
            "minecraft:river",
            "minecraft:desert"
    };

    @Config.Comment("Default Presets used for generating a world")
    public static String[] presets = new String[] {
            DimensionConfig.PRESET_FLAT, DimensionConfig.PRESET_MINING
    };

    public static Object2ObjectOpenHashMap<String, String> getPresets() {
        Object2ObjectOpenHashMap<String, String> map = new Object2ObjectOpenHashMap<>();
        for (String string : Values.presets) {
            String[] stringArray = string.split(";");
            if (stringArray.length != 2) {
                PersonalWorlds.log.error("Preset {} is invalid!", string);
                continue;
            }
            String presetName = stringArray[0];
            String preset = stringArray[1];
            if (!DimensionConfig.PRESET_VALIDATION_PATTERN.matcher(preset).matches()) {
                PersonalWorlds.log.error("Preset {} is invalid!", string);
                continue;
            }
            map.put(presetName, preset);
        }
        return map;
    }

    public static List<IBlockState> getAllowedBlocks() {
        int metaData = 0;
        List<IBlockState> allowedBlocks = new ArrayList<>();
        for (String string : Values.allowedBlocks) {
            String[] stringArray = string.split(":");
            if (stringArray.length == 3) {
                metaData = Integer.parseInt(stringArray[2]);
            }
            Block block = Block.REGISTRY.getObject(new ResourceLocation(stringArray[0], stringArray[1]));
            if (block == null) {
                continue;
            }
            if (metaData == 0) {
                allowedBlocks.add(block.getDefaultState());
            } else {
                allowedBlocks.add(block.getStateFromMeta(metaData));
            }
        }
        return allowedBlocks;
    }

    public static List<Biome> getAllowedBiomes() {
        List<Biome> allowedBiomes = new ArrayList<>();
        for (String string : Values.allowedBiomes) {
            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(string));
            if (biome == null) {
                continue;
            }
            allowedBiomes.add(biome);
        }
        return allowedBiomes;
    }
}
