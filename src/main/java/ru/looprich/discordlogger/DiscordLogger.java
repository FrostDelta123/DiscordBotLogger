package ru.looprich.discordlogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.frostdelta.discord.BotCommand;
import ru.frostdelta.discord.FakePlayerPermissionManager;
import ru.looprich.discordlogger.authentication.GameAuthentication;
import ru.looprich.discordlogger.deauthentication.GameDeauthentication;
import ru.looprich.discordlogger.event.EventListener;
import ru.looprich.discordlogger.module.DiscordBot;

import java.util.ArrayList;
import java.util.List;

public class DiscordLogger extends JavaPlugin {

    private static DiscordLogger plugin;
    public DiscordBot discordBot;
    private Network network;
    public List<GameAuthentication> gameAuthenticationUsers = null;
    public List<GameDeauthentication> gameDeauthenticationPlayers = null;
    private EventListener eventHandler;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        this.reloadConfig();

        boolean isEnabled = getConfig().getBoolean("bot.enabled");
        if (isEnabled) {
            checkDatabase();
            getLogger().info("DiscordBotLogging enabled!");
            getLogger().info("Loading...");
            regEvents();
            loadDiscordBot();
            BotCommand.reg();
            gameAuthenticationUsers = new ArrayList<>();
            gameDeauthenticationPlayers = new ArrayList<>();
        } else getLogger().info("DiscordBotLogging disabled!");
        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "Authors: " + getDescription().getAuthors());
        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "WebSite: " + getDescription().getWebsite());

        FakePlayerPermissionManager.load();
    }

    public void loadDiscordBot() {
        String token = getConfig().getString("bot.token");
        String channel = getConfig().getString("bot.channel-id");
        discordBot = new DiscordBot(token, channel);
        if (!discordBot.createBot()) {
            getLogger().severe("PLUGIN DISABLE! YOU HAVE PROBLEMS WITH DISCORD BOT!");
            getPluginLoader().disablePlugin(this);
        } else getLogger().info("Bot v" + this.getDescription().getVersion() + " successful loaded!");
    }

    private void checkDatabase() {
        String url = getConfig().getString("network.url");
        String username = getConfig().getString("network.username");
        String password = getConfig().getString("network.password");
        network = new Network(getLogger(), url, username, password);
        if (network.init()) {
            getLogger().info("Database found!");
            network.createDB();
        } else {
            getLogger().severe("Database not found!");
            getPluginLoader().disablePlugin(this);
        }
    }

    private void regEvents() {
        eventHandler = new EventListener();
        if (getConfig().getBoolean("tracing.player-quit")) {
            Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onPlayerQuitEvent((PlayerQuitEvent) event), this);
            getLogger().info("Tracing on player quit enabled.");
        }
        if (getConfig().getBoolean("tracing.player-login")) {
            Bukkit.getPluginManager().registerEvent(PlayerLoginEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onPlayerLoginEvent((PlayerLoginEvent) event), this);
            getLogger().info("Tracing on player login enabled.");
        }
        if (getConfig().getBoolean("tracing.player-join")) {
            Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onPlayerJoinEvent((PlayerJoinEvent) event), this);
            getLogger().info("Tracing on player join enabled.");
        }
        if (getConfig().getBoolean("tracing.player-command")) {
            Bukkit.getPluginManager().registerEvent(PlayerCommandPreprocessEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onPlayerCommandPreprocessEvent((PlayerCommandPreprocessEvent) event), this);
            getLogger().info("Tracing on player command enabled.");
        }
        if (getConfig().getBoolean("tracing.player-chat")) {
            Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onAsyncPlayerChatEvent((AsyncPlayerChatEvent) event), this);
            getLogger().info("Tracing on player chat enabled.");
        }
        if (getConfig().getBoolean("tracing.player-achievement")) {
            Bukkit.getPluginManager().registerEvent(PlayerAchievementAwardedEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onPlayerAchievementAwardedEvent((PlayerAchievementAwardedEvent) event), this);
            getLogger().info("Tracing on player achievement enabled.");
        }
        if (getConfig().getBoolean("tracing.server-broadcast")) {
            Bukkit.getPluginManager().registerEvent(BroadcastMessageEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onBroadcastMessageEvent((BroadcastMessageEvent) event), this);
            getLogger().info("Tracing on server broadcast enabled.");
        }
        if (getConfig().getBoolean("tracing.server-command")) {
            Bukkit.getPluginManager().registerEvent(ServerCommandEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onServerCommandEvent((ServerCommandEvent) event), this);
            getLogger().info("Tracing on server command enabled.");
        }
        if (getConfig().getBoolean("tracing.server-remote-command")) {
            Bukkit.getPluginManager().registerEvent(RemoteServerCommandEvent.class, eventHandler, EventPriority.MONITOR,
                    (listener, event) -> eventHandler.onRemoteServerCommandEvent((RemoteServerCommandEvent) event), this);
            getLogger().info("Tracing on server remote command enabled.");
        }
    }

    public Network getNetwork() {
        return network;
    }

    public static DiscordLogger getInstance() {
        return plugin;
    }

    @Override
    public void onDisable() {
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        if (DiscordBot.isEnabled()) {
            DiscordBot.sendImportantMessage("Я выключился!");
            DiscordBot.shutdown();
        }
        network.close();
        this.saveConfig();
        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "Authors: " + getDescription().getAuthors());
        getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "WebSite: " + getDescription().getWebsite());
    }
}
