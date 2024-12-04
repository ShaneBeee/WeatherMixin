package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"unused", "NullableProblems"})
@Mixin(WallTorchBlock.class)
public abstract class WallTorchBlockMixin extends TorchBlock {

    protected WallTorchBlockMixin(SimpleParticleType particle, Properties settings) {
        super(particle, settings);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel && serverLevel.isRainingAt(pos)) return false;
        return WallTorchBlock.canSurvive(level, pos, state.getValue(WallTorchBlock.FACING));
    }

}
