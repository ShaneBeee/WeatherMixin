package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"NullableProblems", "deprecation", "unused"})
@Mixin(CropBlock.class)
public abstract class CropBlockMixin extends Block {

    protected CropBlockMixin(Properties settings) {
        super(settings);
    }

    @Shadow
    public abstract int getAge(BlockState state);

    @Shadow
    public abstract BlockState getStateForAge(int age);

    @Shadow
    public abstract int getMaxAge();

    @Shadow
    protected static float getGrowthSpeed(Block block, BlockGetter world, BlockPos pos) {
        return 0;
    }

    @Shadow
    public abstract boolean isMaxAge(BlockState state);

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = this.getAge(state);
            Biome biome = level.getBiome(pos).value();
            if (biome.getTemperature(pos, level.getSeaLevel()) < 1 && level.getBrightness(LightLayer.BLOCK, pos) < 9) {
                if (biome.coldEnoughToSnow(pos, level.getSeaLevel())) {
                    if (age > 0) {
                        if (random.nextInt(8 - age) == 0) {
                            level.setBlock(pos, this.getStateForAge(age - 1), 2);
                        }
                    } else if (age == 0 && random.nextInt(200) == 0) {
                        level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 2);
                        level.setBlock(pos.below(), Blocks.COARSE_DIRT.defaultBlockState(), 2);
                    }
                }
            } else {
                if (age < this.getMaxAge()) {
                    float growthSpeed = getGrowthSpeed(this, level, pos);

                    // Spigot start
                    int modifier;
                    if (this == Blocks.BEETROOTS) {
                        modifier = level.spigotConfig.beetrootModifier;
                    } else if (this == Blocks.CARROTS) {
                        modifier = level.spigotConfig.carrotModifier;
                    } else if (this == Blocks.POTATOES) {
                        modifier = level.spigotConfig.potatoModifier;
                        // Paper start - Fix Spigot growth modifiers
                    } else if (this == Blocks.TORCHFLOWER_CROP) {
                        modifier = level.spigotConfig.torchFlowerModifier;
                        // Paper end - Fix Spigot growth modifiers
                    } else {
                        modifier = level.spigotConfig.wheatModifier;
                    }

                    if (random.nextFloat() < (modifier / (100.0f * (Math.floor((25.0F / growthSpeed) + 1))))) { // Spigot - SPIGOT-7159: Better modifier resolution
                        // Spigot end
                        CraftEventFactory.handleBlockGrowEvent(level, pos, this.getStateForAge(age + 1), 2); // CraftBukkit
                    }
                }
            }
        }
    }

}
