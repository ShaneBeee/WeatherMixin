package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Shadow
    public boolean hasPrecipitation() {
        return false;
    }

    @Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
    public void shouldSnow(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        // Prevent snow forming in a biome that doesn't have precipitation
        // This fixes a Minecraft bug
        if (!hasPrecipitation()) info.setReturnValue(false);
    }

}
