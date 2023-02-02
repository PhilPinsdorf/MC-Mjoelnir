package de.rexituz.mjoelnir.player;

import com.velocitypowered.api.proxy.Player;
import de.rexituz.mjoelnir.Mjoelnir;
import de.rexituz.mjoelnir.sqlite.DatabaseResult;
import de.rexituz.mjoelnir.sqlite.DatabaseHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class PlayerHandler {
    public static DatabaseResult checkPlayer(Player player) {
        if(DatabaseHandler.isPlayerBanned(player)) {
            return DatabaseResult.BANNED;
        }

        if(DatabaseHandler.isPlayerTimeout(player)) {
            return DatabaseResult.TIMEOUT;
        }

        return DatabaseResult.ALLOWED;
    }

    public static TextComponent getBannedText() {
        return Component.text("Du bist von diesem Netzwerk gebannt!\nWenn du denkst, es handelt sich hierbei um einen fehler melde dich beim Minecraft-Support auf Discord.").color(NamedTextColor.RED);
    }

    public static TextComponent getTimeoutText(String uuid) {
        long until = DatabaseHandler.getPlayerTimeoutUntil(uuid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.GERMAN).withZone(ZoneId.systemDefault());
        System.out.println(uuid);
        System.out.println(until);
        String time = formatter.format(Instant.ofEpochSecond(until));
        return Component.text("Du befindest dich im Timeout!\nDu kannst um " + time + " Uhr wieder joinen.").color(NamedTextColor.RED);
    }

    public static boolean banPlayer(String name) {
        Optional<Player> optPlayer = Mjoelnir.getInstance().getServer().getPlayer(name);
        optPlayer.ifPresent(player -> player.disconnect(getBannedText()));
        return DatabaseHandler.banFromUsername(name);
    }

    public static boolean timeoutPlayer(String name, int min) {
        Optional<Player> optPlayer = Mjoelnir.getInstance().getServer().getPlayer(name);
        String uuid = DatabaseHandler.getUUID(name);

        if(uuid == null) return false;

        optPlayer.ifPresent(player -> player.disconnect(getTimeoutText(uuid)));
        return DatabaseHandler.timeoutUsername(name, min);
    }
}
