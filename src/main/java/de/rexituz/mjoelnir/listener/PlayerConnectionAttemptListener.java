package de.rexituz.mjoelnir.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.rexituz.mjoelnir.sqlite.DatabaseHandler;
import de.rexituz.mjoelnir.sqlite.DatabaseResult;
import de.rexituz.mjoelnir.player.PlayerHandler;

public class PlayerConnectionAttemptListener {
    @Subscribe
    private void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        DatabaseHandler.updateUser(player);
        DatabaseResult result = PlayerHandler.checkPlayer(player);

        switch (result) {
            case ALLOWED:
                event.setResult(ResultedEvent.ComponentResult.allowed());
                break;
            case BANNED:
                event.setResult(ResultedEvent.ComponentResult.denied(PlayerHandler.getBannedText()));
                break;
            case TIMEOUT:
                event.setResult(ResultedEvent.ComponentResult.denied(PlayerHandler.getTimeoutText(player.getUniqueId().toString())));
                break;
        }
    }
}
