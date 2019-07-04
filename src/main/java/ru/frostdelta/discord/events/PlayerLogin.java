package ru.frostdelta.discord.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import ru.looprich.discordlogger.modules.DiscordBot;

public class PlayerLogin implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLoginEvent(PlayerLoginEvent event) {
        DiscordBot.sendMessageChannel("UUID of player " + event.getPlayer().getName() + " is " + event.getPlayer().getUniqueId());
    }

}
