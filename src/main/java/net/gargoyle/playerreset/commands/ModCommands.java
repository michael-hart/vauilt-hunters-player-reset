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

    public static <T extends Command> T registerCommand(Supplier<T> supplier, CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection env) {
        T command = (T)supplier.get();
        if (!command.isDedicatedServerOnly() || env == Commands.CommandSelection.DEDICATED || env == Commands.CommandSelection.ALL) {
            command.registerCommand(dispatcher);
        }

        return command;
    }
}
