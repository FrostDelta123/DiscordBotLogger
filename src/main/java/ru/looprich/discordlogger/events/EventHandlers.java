package ru.looprich.discordlogger.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import ru.looprich.discordlogger.Core;
import ru.looprich.discordlogger.modules.DiscordBot;


public class EventHandlers implements Listener {

    public static void sendMessage(String message) {
        Core.getInstance().sendMessageDiscord(message);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLoginEvent(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        sendMessage("UUID of player " + player.getName() + " is " + player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sendMessage(player.getName() + "[" + player.getAddress().getAddress() + ":" + player.getAddress().getPort() + "] logged at (["
                + player.getLocation().getWorld().getName() + "]"
                + player.getLocation().getX() + ", "
                + player.getLocation().getY() + ", "
                + player.getLocation().getZ() + ")");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sendMessage(player.getName() + " lost connection: Disconnected");
        sendMessage(player.getName() + " left the game");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        sendMessage(player.getName() + " issued server command: " + event.getMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        sendMessage(player.getName() + " " + event.getDeathMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        sendMessage(player.getName() + " has made the advancement [" + event.getAdvancement().getKey() + "]");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage().replace("!", "");
        if (event.getMessage().startsWith("!"))
            sendMessage("[G]<" + player.getName() + "> " + msg);
        else {
            if (DiscordBot.isLocalEnabled()) {
                sendMessage("[L]<" + player.getName() + "> " + msg);
            }
        }
    }
}
