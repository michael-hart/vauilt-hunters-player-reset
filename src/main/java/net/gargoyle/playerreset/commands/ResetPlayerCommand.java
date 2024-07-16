package net.gargoyle.playerreset.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.block.entity.VaultAltarTileEntity;
import iskallia.vault.command.Command;
import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.nbt.VListNBT;
import iskallia.vault.network.message.UpdateTitlesDataMessage;
import iskallia.vault.world.data.*;
import net.gargoyle.playerreset.mixin.DiscoveredAlchemyEffectsDataAccessor;
import net.gargoyle.playerreset.mixin.DiscoveredWorkbenchModifiersDataAccessor;
import net.gargoyle.playerreset.mixin.PlayerProficiencyDataAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

import java.util.*;

public class ResetPlayerCommand extends Command {
    public static final int ALL_RESET_TIMEOUT_TICKS = 10*20;  // 10 ticks
    private static final Map<String, Integer> resetAllTracker = new HashMap<>();

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
                        Commands.literal("proficiencies")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetProficienciesCommand))
                )
                ;
    }

    public boolean isDedicatedServerOnly() {
        return false;
    }

    private int resetAllCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ServerPlayer sourcePlayer = ((CommandSourceStack)context.getSource()).getPlayerOrException();

        String lookupKey = sourcePlayer.getName().getContents() + "_" + player.getName().getContents();
        Integer lastRequest = resetAllTracker.get(lookupKey);
        if (lastRequest == null || player.server.getTickCount() - lastRequest > ALL_RESET_TIMEOUT_TICKS) {
            // Time out - enter into tracker and request confirmation
            resetAllTracker.put(lookupKey, player.server.getTickCount());

            ((CommandSourceStack)context.getSource())
                    .sendSuccess(
                            new TextComponent(
                                    "WARNING: THIS CANNOT BE UNDONE! If you are absolutely sure you want to fully reset "
                                            + player.getName().getContents()
                                            + ", click here or run the command again.."
                            )
                            .setStyle(
                                    Style.EMPTY
                                            .withColor(ChatFormatting.RED)
                                            .withBold(true)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, context.getInput()))),
                            true
                    );
            return 0;
        }

        resetAllTracker.remove(lookupKey);

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
        resetPlayerPotionModifiers(player);
        resetPlayerParadox(player);
        resetPlayerQuests(player);
        resetPlayerVaultHistory(player);
        resetPlayerBlackMarket(player);
        resetPlayerAltarLevel(player);
        resetPlayerAltarRecipe(player);
        resetPlayerAscensionTitle(player);
        resetPlayerProficiencies(player);

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

    private int resetProficienciesCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerProficiencies(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s proficiencies reset!", player.getName().getContents())
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
        relicsData.getDiscoveredRelics(player.getUUID()).clear();
        relicsData.setDirty(true);
    }

    private void resetPlayerArmorModels(ServerPlayer player) {
        DiscoveredModelsData modelsData = DiscoveredModelsData.get(player.server);
        modelsData.getDiscoveredModels(player.getUUID()).clear();
        modelsData.setDirty(true);
        modelsData.syncTo(player);
    }

    private void resetPlayerTrinkets(ServerPlayer player) {
        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(player.server);
        trinketsData.getDiscoveredTrinkets(player.getUUID()).clear();
        trinketsData.setDirty(true);
        trinketsData.syncTo(player);
    }

    private void resetPlayerWorkbenchModifiers(ServerPlayer player) {
        DiscoveredWorkbenchModifiersData modifiersData = DiscoveredWorkbenchModifiersData.get(player.server);
        ((DiscoveredWorkbenchModifiersDataAccessor) modifiersData).getDiscoveredCrafts().remove(player.getUUID());
        modifiersData.setDirty(true);
        modifiersData.syncTo(player);
    }

    private void resetPlayerPotionModifiers(ServerPlayer player) {
        DiscoveredAlchemyEffectsData alchemyData = DiscoveredAlchemyEffectsData.get(player.server);
        ((DiscoveredAlchemyEffectsDataAccessor) alchemyData).getDiscoveredEffects().remove(player.getUUID());
        alchemyData.setDirty(true);
        alchemyData.syncTo(player);
    }

    private void resetPlayerParadox(ServerPlayer player) {
        ParadoxCrystalData paradoxData = ParadoxCrystalData.get(player.server);
        paradoxData.getOrCreate(player.getUUID()).reset();
        paradoxData.setDirty(true);
    }

    private void resetPlayerGodReputation(ServerPlayer player) {
        List<VaultGod> gods = Arrays.asList(VaultGod.IDONA, VaultGod.TENOS, VaultGod.VELARA, VaultGod.WENDARR);
        gods.forEach(god -> {
            int rep = PlayerReputationData.getReputation(player.getUUID(), god);
            if (rep > 0) {
                PlayerReputationData.addReputation(player.getUUID(), god, -rep);
            }
        });
    }

    private void resetPlayerQuests(ServerPlayer player) {
        QuestStatesData.get().getState(player).reset();
    }

    private void resetPlayerVaultHistory(ServerPlayer player) {
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

        // Removing favorites:
        // https://github.com/BONNePlayground/VaultHuntersExtraCommands/blob/d1de04f157d8b970e0978cdfb9b13d394ad38213/src/main/java/lv/id/bonne/vaulthunters/extracommands/commands/ClearCommand.java#L168-L171
        PlayerHistoricFavoritesData favoritesData = PlayerHistoricFavoritesData.get(player.getLevel());
        favoritesData.getPlayerMap().remove(player.getUUID());
        favoritesData.setDirty();
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

        // Removal of current recipe:
        // https://github.com/BONNePlayground/VaultHuntersExtraCommands/blob/d1de04f157d8b970e0978cdfb9b13d394ad38213/src/main/java/lv/id/bonne/vaulthunters/extracommands/commands/ClearCommand.java#L121
        List<BlockPos> altars = altarData.getAltars(player.getUUID());
        altars.stream().
                filter(pos -> player.getLevel().isLoaded(pos)).
                map(pos -> player.getLevel().getBlockEntity(pos)).
                filter(te -> te instanceof VaultAltarTileEntity).
                map(te -> (VaultAltarTileEntity)te).
                filter(altar -> (altar.getAltarState() == VaultAltarTileEntity.AltarState.ACCEPTING)).
                forEach(altar -> altar.onRemoveInput(player.getUUID()));

        altars.stream().toList().forEach(altar -> altarData.removeAltar(player.getUUID(), altar));

        altarData.setDirty();
    }

    private void resetPlayerAscensionTitle(ServerPlayer player) {
        PlayerTitlesData titlesData = PlayerTitlesData.get();
        titlesData.entries.remove(player.getUUID());
        // Reset player title network packet
        // https://github.com/BONNePlayground/VaultHuntersExtraCommands/blob/d1de04f157d8b970e0978cdfb9b13d394ad38213/src/main/java/lv/id/bonne/vaulthunters/extracommands/commands/ClearCommand.java#L295
        ModNetwork.CHANNEL.sendTo(new UpdateTitlesDataMessage(titlesData.entries),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT);
        titlesData.setDirty(true);
    }

    private void resetPlayerProficiencies(ServerPlayer player) {
        PlayerProficiencyData profData = PlayerProficiencyData.get(player.server);
        ((PlayerProficiencyDataAccessor) profData).getPlayerProficiencies().remove(player.getUUID());
        profData.sendProficiencyInformation(player);
        profData.setDirty(true);
    }

}
