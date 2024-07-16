package net.gargoyle.playerreset.mixin;

import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.world.data.PlayerProficiencyData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = PlayerProficiencyData.class, remap = false)
public interface PlayerProficiencyDataAccessor {
    @Accessor("playerProficiencies")
    Map<UUID, Map<ProficiencyType, Integer>> getPlayerProficiencies();
}
