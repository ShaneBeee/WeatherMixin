package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings({"unused", "NullableProblems"})
@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerBlockMixin extends Block {

    public SnowLayerBlockMixin(Properties settings) {
        super(settings);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.scheduleTick(pos, this, 2);
        }
        return super.updateShape(state, level, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinY()) {
            FallingBlockEntity fall = FallingBlockEntity.fall(level, pos, state);
            fall.dropItem = false;
        }
    }

    @Unique
    private static boolean isFree(BlockState state) {
        if (state.is(Blocks.SNOW) && state.getValue(SnowLayerBlock.LAYERS) == SnowLayerBlock.MAX_HEIGHT) {
            return false;
        }
        return FallingBlock.isFree(state);
    }

    @Overwrite
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Biome biome = level.getBiome(pos).value();
        if (biome.getBaseTemperature() >= 1.3f) {
            meltSnow(state, level, pos);
        } else if (!biome.coldEnoughToSnow(pos, level.getSeaLevel()) && level.isRainingAt(pos.above())) {
            meltSnow(state, level, pos);
        } else if (level.getBrightness(LightLayer.BLOCK, pos) > 7) {
            meltSnow(state, level, pos);
        }
    }

    @Unique
    private static void meltSnow(BlockState state, ServerLevel level, BlockPos pos) {
        int layers = state.getValue(SnowLayerBlock.LAYERS);
        if (layers > 1) {
            level.setBlockAndUpdate(pos, state.setValue(SnowLayerBlock.LAYERS, layers - 1));
            return;
        }
        // CraftBukkit start
        if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockFadeEvent(level, pos, Blocks.AIR.defaultBlockState()).isCancelled()) {
            return;
        }
        // CraftBukkit end
        dropResources(state, level, pos);
        level.removeBlock(pos, false);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        int layers = state.getValue(SnowLayerBlock.LAYERS);
        ItemStack itemInHand = context.getItemInHand();
        if (itemInHand.is(this.asItem()) && layers < 8) {
            if (context.replacingClickedOnBlock()) {
                return context.getClickedFace() == Direction.UP;
            } else {
                return true;
            }
        } else if (layers == 1 && itemInHand != ItemStack.EMPTY) {
            return !itemInHand.is(this.asItem());
        } else {
            return false;
        }
    }

}
