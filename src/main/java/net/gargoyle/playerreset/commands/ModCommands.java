package net.gargoyle.playerreset.commands;

import com.mojang.brigadier.CommandDispatcher;
import iskallia.vault.command.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.function.Supplier;

public class ModCommands {
    public static ResetPlayerCommand RESET_PLAYER;

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection env) {
        RESET_PLAYER = registerCommand(ResetPlayerCommand::new, dispatcher, env);
    }

    public static void registerArgumentTypes() {
//        ArgumentTypes.register(
//                VaultMod.id("backup_list_player").toString(), BackupListArgument.Player.class, new EmptyArgumentSerializer(BackupListArgument.Player::new)
//        );
//        ArgumentTypes.register(
//                VaultMod.id("backup_list_uuid").toString(), BackupListArgument.UUIDRef.class, new EmptyArgumentSerializer(BackupListArgument.UUIDRef::new)
//        );
    }

    public static <T extends Command> T registerCommand(Supplier<T> supplier, CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection env) {
        T command = (T)supplier.get();
        if (!command.isDedicatedServerOnly() || env == Commands.CommandSelection.DEDICATED || env == Commands.CommandSelection.ALL) {
            command.registerCommand(dispatcher);
        }

        return command;
    }
}


//private int getPlayerStats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//    ServerPlayer player = EntityArgument.getPlayer(context, "player");
//    PlayerVaultStatsData data = PlayerVaultStatsData.get(player.getLevel());
//    PlayerVaultStats vaultStats = data.getVaultStats(player);
//    int vaultLevel = vaultStats.getVaultLevel();
//    int exp = vaultStats.getExp();
//    int spentSkillPoints = vaultStats.getTotalSpentSkillPoints();
//    int spentKnowledgePoints = vaultStats.getTotalSpentKnowledgePoints();
//    int spentExpertisePoints = vaultStats.getTotalSpentExpertisePoints();
//    int unspentSkillPoints = vaultStats.getUnspentSkillPoints();
//    int unspentKnowledgePoints = vaultStats.getUnspentKnowledgePoints();
//    int unspentExpertisePoints = vaultStats.getUnspentExpertisePoints();
//    List<TextComponent> messages = new ArrayList<>();
//    messages.add(new TextComponent("======================================"));
//    messages.add(new TextComponent(String.format("Player Stats: %s%s", ChatFormatting.DARK_AQUA, player.getDisplayName().getString())));
//    messages.add(new TextComponent(String.format("Vault Level: %s%s", ChatFormatting.YELLOW, vaultLevel)));
//    messages.add(new TextComponent(String.format("Vault Exp: %s%s", ChatFormatting.YELLOW, exp)));
//    messages.add(
//            new TextComponent(
//                    String.format(
//                            "Skill Points: %s%s%s used / %s%s%s available",
//                            ChatFormatting.YELLOW,
//                            spentSkillPoints,
//                            ChatFormatting.RESET,
//                            ChatFormatting.YELLOW,
//                            unspentSkillPoints,
//                            ChatFormatting.RESET
//                    )
//            )
//    );
//    messages.add(
//            new TextComponent(
//                    String.format(
//                            "Knowledge Points: %s%s%s used / %s%s%s available",
//                            ChatFormatting.AQUA,
//                            spentKnowledgePoints,
//                            ChatFormatting.RESET,
//                            ChatFormatting.AQUA,
//                            unspentKnowledgePoints,
//                            ChatFormatting.RESET
//                    )
//            )
//    );
//    messages.add(
//            new TextComponent(
//                    String.format(
//                            "Expertise Points: %s%s%s used / %s%s%s available",
//                            ChatFormatting.LIGHT_PURPLE,
//                            spentExpertisePoints,
//                            ChatFormatting.RESET,
//                            ChatFormatting.LIGHT_PURPLE,
//                            unspentExpertisePoints,
//                            ChatFormatting.RESET
//                    )
//            )
//    );
//    messages.add(new TextComponent("======================================"));
//    messages.forEach(message -> ((CommandSourceStack)context.getSource()).sendSuccess(message, true));
//    return 0;
//}

