package net.gargoyle.playerreset.mixin;

import iskallia.vault.world.data.DiscoveredAlchemyEffectsData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(value = DiscoveredAlchemyEffectsData.class, remap = false)
public interface DiscoveredAlchemyEffectsDataAccessor {
    @Accessor("discoveredEffects")
    Map<UUID, Map<Item, Set<ResourceLocation>>> getDiscoveredEffects();
}
