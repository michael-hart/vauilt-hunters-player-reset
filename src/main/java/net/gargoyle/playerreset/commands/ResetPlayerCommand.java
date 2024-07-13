package net.gargoyle.playerreset.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import iskallia.vault.command.Command;
import iskallia.vault.config.PlayerTitlesConfig;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.nbt.VListNBT;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.util.AdvancementHelper;
import iskallia.vault.world.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResetPlayerCommand extends Command {
    public String getName() {
        return "reset_player";
    }

    public int getRequiredPermissionLevel() {
        // Moderator or higher
        return 1;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .then(
                    Commands.literal("all")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(this::resetAllCommand))
                )
                .then(
                        Commands.literal("level")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetLevelCommand))
                )
                .then(
                        Commands.literal("skills_and_abilities")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetSkillsAndAbilitiesCommand))
                )
                .then(
                        Commands.literal("expertise")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetExpertiseCommand))
                )
                .then(
                        Commands.literal("knowledge")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetKnowledgeCommand))
                )
                .then(
                        Commands.literal("research")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetResearchCommand))
                )
                .then(
                        Commands.literal("bounties")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetBountiesCommand))
                )
                .then(
                        Commands.literal("relics")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetRelicsCommand))
                )
                .then(
                        Commands.literal("armor_models")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetArmorModelsCommand))
                )
                .then(
                        Commands.literal("trinkets")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetTrinketsCommand))
                )
                .then(
                        Commands.literal("workbench_modifiers")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetWorkbenchModifiersCommand))
                )
                .then(
                        Commands.literal("potion_modifiers")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetPotionModifiersCommand))
                )
                .then(
                        Commands.literal("paradox")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetParadoxCommand))
                )
                .then(
                        Commands.literal("god_reputation")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetGodReputationCommand))
                )
                .then(
                        Commands.literal("quests")
                                .then(Commands.argument("player", EntityArgument.player())
                                    .executes(this::resetQuestsCommand))
                )
                .then(
                        Commands.literal("vault_history")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetVaultHistoryCommand))
                )
                .then(
                        Commands.literal("black_market")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetBlackMarketCommand))
                )
                .then(
                        Commands.literal("altar_level")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetAltarLevelCommand))
                )
                .then(
                        Commands.literal("altar_recipe")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetAltarRecipeCommand))
                )
                .then(
                        Commands.literal("ascension_title")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetAscensionTitleCommand))
                )
                .then(
                        Commands.literal("achievements")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetAchievementsCommand))
                )
                ;
    }

    public boolean isDedicatedServerOnly() {
        return false;
    }

    private int resetAllCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ServerPlayer sourcePlayer = ((CommandSourceStack)context.getSource()).getPlayerOrException();

        resetPlayerGodReputation(player);
        resetPlayerLevel(player);
        resetPlayerSkillsAndAbilities(player);
        resetPlayerExpertise(player);
        resetPlayerKnowledge(player);
        resetPlayerResearch(player);
        resetPlayerBounties(player);
        resetPlayerWorkbenchModifiers(player);
        resetPlayerRelics(player);
        resetPlayerArmorModels(player);
        resetPlayerTrinkets(player);
        resetPlayerArmorModels(player);
        resetPlayerPotionModifiers(player);
        resetPlayerParadox(player);
        resetPlayerQuests(player);
        resetPlayerVaultHistory(player);
        resetPlayerBlackMarket(player);
        resetPlayerAltarLevel(player);
        resetPlayerAltarRecipe(player);
        resetPlayerAscensionTitle(player);
        resetPlayerAchievements(sourcePlayer, player);

        TextComponent successMessage = new TextComponent(
                String.format("Player %s fully reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetLevelCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerLevel(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s level reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetSkillsAndAbilitiesCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerSkillsAndAbilities(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s skills and abilities reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetExpertiseCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerExpertise(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s expertise reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetKnowledgeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerKnowledge(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s knowledge reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetResearchCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");

        resetPlayerResearch(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s research reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetBountiesCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerBounties(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s bounties reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetRelicsCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerRelics(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s relics reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetArmorModelsCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerArmorModels(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s armor models reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetTrinketsCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerTrinkets(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s trinkets reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetWorkbenchModifiersCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerWorkbenchModifiers(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s workbench modifiers reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetPotionModifiersCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerPotionModifiers(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s potion modifiers reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetParadoxCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerParadox(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s paradox reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetGodReputationCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerGodReputation(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s god reputation reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetQuestsCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerQuests(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s quests reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetVaultHistoryCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerVaultHistory(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s vault history reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetBlackMarketCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerBlackMarket(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s black market reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetAltarLevelCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerAltarLevel(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s altar level reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetAltarRecipeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerAltarRecipe(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s altar recipe reset! Please replace the player's altar.", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetAscensionTitleCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerAscensionTitle(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s Ascension title reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private int resetAchievementsCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sourcePlayer = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        resetPlayerAchievements(sourcePlayer, targetPlayer);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s achievements reset!", targetPlayer.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
    }

    private void resetPlayerLevel(ServerPlayer player) {
        PlayerVaultStatsData data = PlayerVaultStatsData.get(player.getLevel());
        data.resetLevel(player);
        data.setDirty(true);
    }

    private void resetPlayerSkillsAndAbilities(ServerPlayer player) {
        PlayerVaultStatsData data = PlayerVaultStatsData.get(player.getLevel());
        data.resetSkills(player);
        data.setDirty(true);
    }

    private void resetPlayerExpertise(ServerPlayer player) {
        PlayerVaultStatsData data = PlayerVaultStatsData.get(player.getLevel());
        data.resetExpertises(player);
        data.setDirty(true);
    }

    private void resetPlayerKnowledge(ServerPlayer player) {
        PlayerVaultStatsData data = PlayerVaultStatsData.get(player.getLevel());
        data.resetKnowledge(player);
        data.setDirty(true);
    }

    private void resetPlayerResearch(ServerPlayer player) {
        PlayerResearchesData researchesData = PlayerResearchesData.get(player.getLevel());
        researchesData.resetResearchTree(player);
        researchesData.setDirty(true);
    }

    private void resetPlayerBounties(ServerPlayer player) {
        BountyData bountyData = BountyData.get();
        bountyData.resetAllBounties(player.getUUID());
        bountyData.setDirty(true);
    }

    private void resetPlayerRelics(ServerPlayer player) {
        DiscoveredRelicsData relicsData = DiscoveredRelicsData.get(player.server);
        relicsData.load(new CompoundTag());
        relicsData.setDirty(true);
    }

    private void resetPlayerArmorModels(ServerPlayer player) {
        DiscoveredModelsData modelsData = DiscoveredModelsData.get(player.server);
        modelsData.load(new CompoundTag());
        modelsData.setDirty(true);
        modelsData.syncTo(player);
    }

    private void resetPlayerTrinkets(ServerPlayer player) {
        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(player.server);
        trinketsData.load(new CompoundTag());
        trinketsData.setDirty(true);
        trinketsData.syncTo(player);
    }

    private void resetPlayerWorkbenchModifiers(ServerPlayer player) {
        DiscoveredWorkbenchModifiersData modifiersData = DiscoveredWorkbenchModifiersData.get(player.server);
        modifiersData.load(new CompoundTag());
        modifiersData.setDirty(true);
        modifiersData.syncTo(player);
    }

    private void resetPlayerPotionModifiers(ServerPlayer player) {
        DiscoveredAlchemyEffectsData alchemyData = DiscoveredAlchemyEffectsData.get(player.server);
        alchemyData.load(new CompoundTag());
        alchemyData.setDirty(true);
        alchemyData.syncTo(player);
    }

    private void resetPlayerParadox(ServerPlayer player) {
        ParadoxCrystalData paradoxData = ParadoxCrystalData.get(player.server);
        paradoxData.load(new CompoundTag());
        paradoxData.setDirty(true);
    }

    private void resetPlayerGodReputation(ServerPlayer player) {
        PlayerReputationData.addReputation(player.getUUID(), VaultGod.IDONA, 25);
        PlayerReputationData.addReputation(player.getUUID(), VaultGod.IDONA, -25);
    }

    private void resetPlayerQuests(ServerPlayer player) {
        QuestStatesData.get().getState(player).reset();
    }

    private void resetPlayerAchievements(ServerPlayer sourcePlayer, ServerPlayer targetPlayer) {
        // TODO see if this works via command block
        sourcePlayer.sendMessage(
                new TextComponent(
                        String.format("/advancement revoke %s everything", targetPlayer.getName().getContents()
                )),
                sourcePlayer.getUUID()
        );
    }

    private void resetPlayerVaultHistory(ServerPlayer player) {
        // TODO check this only removes the current player's history
        VaultSnapshots snapshots = VaultSnapshots.get(player.server);

        // Load snapshots and filter out those with player UUID
        List<VaultSnapshot> allSnapshots = VaultSnapshots.getAll();
        List<VaultSnapshot> snapshotsToOverwrite = allSnapshots.stream()
                .filter((snapshot) -> {
                    return !snapshot.getEnd().get(Vault.STATS).getMap().containsKey(player.getUUID());
                })
                .toList();

        // No direct way to set snapshots list, so serialize to NBT and load to VaultSnapshots
        VListNBT<VaultSnapshot, LongArrayTag> snapshotsVListNbt = new VListNBT<>(snapshotsToOverwrite, (snapshot) -> {
            return new LongArrayTag(snapshot.getCache());
        }, (nbt) -> {
            return new VaultSnapshot(ArrayBitBuffer.backing(nbt.getAsLongArray(), 0));
        });
        CompoundTag snapshotsNbt = new CompoundTag();
        snapshotsNbt.put("snapshots", snapshotsVListNbt.serializeNBT());
        snapshots.load(snapshotsNbt);

        snapshots.setDirty(true);
    }

    private void resetPlayerBlackMarket(ServerPlayer player) {
        PlayerBlackMarketData.get(player.server).getBlackMarket(player).resetTrades(player.getUUID());
    }

    private void resetPlayerAltarLevel(ServerPlayer player) {
        PlayerStatsData playerStatsData = PlayerStatsData.get();
        playerStatsData.clearCrystals(player.getUUID());
    }

    private void resetPlayerAltarRecipe(ServerPlayer player) {
        PlayerVaultAltarData altarData = PlayerVaultAltarData.get(player.getLevel());
        altarData.removeRecipe(player.getUUID());
        altarData.setDirty();
    }

    private void resetPlayerAscensionTitle(ServerPlayer player) {
        PlayerTitlesData titlesData = PlayerTitlesData.get();
        PlayerTitlesData.Entry entry = titlesData.entries.get(player.getUUID());
        entry.setPrefix(null);
        entry.setSuffix(null);
        entry.setChanged(true);
//        titlesData.entries.remove(player.getUUID());
        titlesData.setDirty(true);
//        PlayerTitlesData.setCustomName(player, "clearprefix", PlayerTitlesConfig.Affix.PREFIX);
//        PlayerTitlesData.setCustomName(player, "clearsuffix", PlayerTitlesConfig.Affix.SUFFIX);
    }

}
