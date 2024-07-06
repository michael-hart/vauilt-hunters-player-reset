package net.gargoyle.playerreset;

import com.mojang.logging.LogUtils;
import net.gargoyle.playerreset.commands.ModCommands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("playerreset")
public class PlayerReset
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public PlayerReset()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, this::onCommandRegister);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("Vault Hunters Player Reset setup.");
    }

    public void onCommandRegister(RegisterCommandsEvent event) {
        ModCommands.registerCommands(event.getDispatcher(), event.getEnvironment());
    }
}
