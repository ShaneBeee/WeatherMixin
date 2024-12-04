package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings({"NullableProblems", "unused"})
@Mixin(BaseTorchBlock.class)
public abstract class BaseTorchBlockMixin extends Block {

    public BaseTorchBlockMixin(Properties settings) {
        super(settings);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos pos) {
        if (levelReader instanceof ServerLevel serverLevel && serverLevel.isRainingAt(pos)) {
            return false;
        }
        return Block.canSupportCenter(levelReader, pos.below(), Direction.UP);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isRainingAt(pos) && random.nextInt(5) == 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

}
