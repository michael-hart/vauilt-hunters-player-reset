package net.gargoyle.playerreset.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.command.Command;
import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.world.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class ResetPlayerCommand extends Command {
    public ResetPlayerCommand() {

    }

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
                        Commands.literal("paradox")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(this::resetParadoxCommand))
                )
                ;
    }

    private int resetAllCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        PlayerVaultStatsData data = PlayerVaultStatsData.get(player.getLevel());
        PlayerVaultStats vaultStats = data.getVaultStats(player);

        // Can be separately reset using the_vault reset all - don't create special command
        data.resetLevelAbilitiesAndExpertise(player);
        data.resetSkills(player);
        vaultStats.resetAbilitiesTalents(player.server);
        vaultStats.resetKnowledge(player.server);
        PlayerResearchesData.get(context.getSource().getLevel()).resetResearchTree(player);

        // TODO What is an enhancement?

        resetPlayerBounties(player);
        resetPlayerWorkbenchModifiers(player);
        resetPlayerRelics(player);
        resetPlayerArmorModels(player);
        resetPlayerTrinkets(player);
        resetPlayerArmorModels(player);
        resetPlayerPotionModifiers(player);
        resetPlayerParadox(player);

        TextComponent successMessage = new TextComponent(
                String.format("Player %s fully reset!", player.getName().getContents())
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

    private int resetParadoxCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        resetPlayerParadox(player);
        TextComponent successMessage = new TextComponent(
                String.format("Player %s paradox reset!", player.getName().getContents())
        );
        ((CommandSourceStack)context.getSource()).sendSuccess(successMessage, true);
        return 0;
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
    }

    private void resetPlayerTrinkets(ServerPlayer player) {
        DiscoveredTrinketsData trinketsData = DiscoveredTrinketsData.get(player.server);
        trinketsData.load(new CompoundTag());
        trinketsData.setDirty(true);
    }

    private void resetPlayerWorkbenchModifiers(ServerPlayer player) {
        DiscoveredWorkbenchModifiersData modifiersData = DiscoveredWorkbenchModifiersData.get(player.server);
        modifiersData.load(new CompoundTag());
        modifiersData.setDirty(true);
    }

    private void resetPlayerPotionModifiers(ServerPlayer player) {
        DiscoveredAlchemyEffectsData alchemyData = DiscoveredAlchemyEffectsData.get(player.server);
        alchemyData.load(new CompoundTag());
        alchemyData.setDirty(true);
    }

    private void resetPlayerParadox(ServerPlayer player) {
        ParadoxCrystalData paradoxData = ParadoxCrystalData.get(player.server);
        paradoxData.load(new CompoundTag());
        paradoxData.setDirty(true);
    }

    public boolean isDedicatedServerOnly() {
        return false;
    }
}
