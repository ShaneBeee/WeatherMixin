package com.shanebeestudios.weathermixin.mixins;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"unused", "UnusedReturnValue", "ConstantValue", "DataFlowIssue", "OptionalGetWithoutIsPresent"})
@Mixin(Biome.class)
public class BukkitBiomeMixin {

    @Shadow
    @Final
    @Mutable
    private static Biome[] $VALUES;

    static {
        List<Biome> biomeValues = new ArrayList<>(Arrays.asList(BukkitBiomeMixin.$VALUES));

        Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = MinecraftServer.getServer().registryAccess().get(Registries.BIOME).get().value();
        for (Map.Entry<ResourceKey<net.minecraft.world.level.biome.Biome>, net.minecraft.world.level.biome.Biome> resourceKeyBiomeEntry : biomeRegistry.entrySet()) {
            ResourceKey<net.minecraft.world.level.biome.Biome> key = resourceKeyBiomeEntry.getKey();
            ResourceLocation location = key.location();
            if (location.getNamespace().equalsIgnoreCase("minecraft") && !location.getPath().contains("/")) continue;

            NamespacedKey namespacedKey = CraftNamespacedKey.fromMinecraft(location);
            String name = location.toString().replace(":", "_").replace("/", "_").toUpperCase(Locale.ROOT);
            Biome biome = addBiome(name, namespacedKey, biomeValues.getLast().ordinal() + 1);
            biomeValues.add(biome);
        }

        BukkitBiomeMixin.$VALUES = biomeValues.toArray(new Biome[0]);
    }

    @Invoker("<init>")
    public static Biome invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    private static Biome addBiome(String internalName, NamespacedKey key, int internalId) {
        Biome biome = invokeInit(internalName, internalId);
        try {
            Field keyField = Biome.class.getDeclaredField("key");
            keyField.setAccessible(true);
            keyField.set(biome, key);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return biome;
    }

}
