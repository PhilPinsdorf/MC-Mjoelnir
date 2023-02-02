package de.rexituz.mjoelnir;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rexituz.mjoelnir.commands.MjoelnirCommand;
import de.rexituz.mjoelnir.listener.PlayerConnectionAttemptListener;
import de.rexituz.mjoelnir.sqlite.DatabaseHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

@Plugin(
        id = "mjoelnir",
        name = "Mjoelnir",
        version = "0.2",
        description = "A simple moderation plugin for the Game Night.",
        authors = {"rexituz"}
)
public class Mjoelnir {
    private final ProxyServer server;
    private final Logger logger;
    private static Mjoelnir instance;
    public final static TextComponent PREFIX = Component.text("[").color(NamedTextColor.DARK_GRAY).append(
                                                Component.text("Mjoelnir").color(NamedTextColor.AQUA).append(
                                                Component.text("] ").color(NamedTextColor.DARK_GRAY)));

    @Inject
    public Mjoelnir(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        instance = this;

        logger.info("Mjoelnir is charged and ready to ban!");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        DatabaseHandler.connect();

        server.getEventManager().register(this, new PlayerConnectionAttemptListener());

        CommandManager commandManager = server.getCommandManager();
        SimpleCommand commandToRegister = new MjoelnirCommand();
        CommandMeta commandMeta = commandManager.metaBuilder("mjoelnir").plugin(this).build();
        commandManager.register(commandMeta, commandToRegister);
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public static Mjoelnir getInstance() {
        return instance;
    }
}
