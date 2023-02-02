package de.rexituz.mjoelnir.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.rexituz.mjoelnir.Mjoelnir;
import de.rexituz.mjoelnir.player.PlayerHandler;
import de.rexituz.mjoelnir.sqlite.DatabaseHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MjoelnirCommand implements SimpleCommand {
    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(args.length == 0) {
            source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Please use: /mjoelnir pardon|ban|timeout").color(NamedTextColor.RED)));
            return;
        }

        if(Objects.equals(args[0], "ban")){
            if(args.length != 2) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Please use: /mjoelnir ban <username>").color(NamedTextColor.RED)));
                return;
            }

            String name = args[1];

            if(PlayerHandler.banPlayer(name)) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Player " + name + " was successfully banned!").color(NamedTextColor.GREEN)));
                return;
            }

            source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Something went wrong. Did the Player " + name + " already entered the Network once?").color(NamedTextColor.RED)));
            return;
        }

        if(Objects.equals(args[0], "pardon")){
            if(args.length != 2) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Please use: /mjoelnir pardon <username>").color(NamedTextColor.RED)));
                return;
            }

            String name = args[1];

            if (DatabaseHandler.removeBanFromUsername(name) && DatabaseHandler.removeTimeoutFromUsername(name)) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Player " + name + " can now enter the Network again!").color(NamedTextColor.GREEN)));
                return;
            }

            source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Something went wrong. Did the Player " + name + " already entered the Network once?").color(NamedTextColor.RED)));
            return;
        }

        if(Objects.equals(args[0], "timeout")){
            if(args.length != 3) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Please use: /mjoelnir timeout <username> <time in min>").color(NamedTextColor.RED)));
                return;
            }

            String name = args[1];
            String sMin = args[2];
            int min;

            try {
                min = Integer.parseInt(sMin);
            } catch (NumberFormatException e) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Please use: /mjoelnir timeout <username> <time in min>").color(NamedTextColor.RED)));
                return;
            }

            if(min < 0) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Please use: /mjoelnir timeout <username> <time in min>").color(NamedTextColor.RED)));
                return;
            }

            if(PlayerHandler.timeoutPlayer(name, min)) {
                source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Player " + name + " in timeout for " + min + " Minutes!").color(NamedTextColor.GREEN)));
                return;
            }

            source.sendMessage(Mjoelnir.PREFIX.append(Component.text("Something went wrong. Did the Player " + name + " already entered the Network once?").color(NamedTextColor.RED)));
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("proxy.mjoelnir.moderate");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        String[] args = invocation.arguments();

        if(args.length <= 1) {
            return CompletableFuture.completedFuture(List.of("ban", "timeout", "pardon"));
        }

        if(args.length <= 2) {
            if (Objects.equals(args[0], "ban") || Objects.equals(args[0], "pardon") || Objects.equals(args[0], "timeout")){
                // All known names
                List<String> names = DatabaseHandler.getAllKnownUsernames();
                return CompletableFuture.completedFuture(names);
            }
        }

        if(args.length <= 3) {
            if (Objects.equals(args[0], "timeout")){
                // Numbers
                List<String> minutes = new ArrayList<>(List.of("5", "10", "15", "30", "60", "120", "180"));
                return CompletableFuture.completedFuture(minutes);
            }
        }

        return CompletableFuture.completedFuture(List.of());
    }
}
