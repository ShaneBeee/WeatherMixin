package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"unused", "deprecation"})
@Mixin(IceBlock.class)
public abstract class IceBlockMixin {

    @Shadow
    protected abstract void melt(BlockState state, Level world, BlockPos pos);

    @Overwrite
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.above()).canOcclude()) return;

        Biome biome = level.getBiome(pos).value();
        if (biome.getTemperature(pos, level.getSeaLevel()) >= 1.3f) {
            melt(state, level, pos);
        } else if (!biome.coldEnoughToSnow(pos, level.getSeaLevel()) && level.isRainingAt(pos.above())) {
            melt(state, level, pos);
        } else if (level.getBrightness(LightLayer.BLOCK, pos) > 11 - state.getLightBlock()) {
            melt(state, level, pos);
        }
    }

}
