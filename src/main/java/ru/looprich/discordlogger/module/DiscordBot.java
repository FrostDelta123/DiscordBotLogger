package ru.looprich.discordlogger.module;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import ru.frostdelta.discord.BotCommandAdapter;
import ru.frostdelta.discord.RemoteConfigControl;
import ru.frostdelta.discord.Util;
import ru.looprich.discordlogger.DiscordLogger;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

public class DiscordBot {

    private static DiscordBot bot;
    private static boolean localEnabled;
    private static String tokenBot;
    public static String channel;
    private static TextChannel loggerChannel = null;
    private static JDA jda = null;
    private static boolean enable;
    public static String prefix;
    public static boolean commandOnlyOneChannel;
    private static boolean isWhitelistEnabled;

    public DiscordBot(String tokenBot, String channel) {
        DiscordBot.tokenBot = tokenBot;
        DiscordBot.channel = channel;
    }

    private static String getServerName() {
        File file = new File("server.properties");
        BufferedReader read = null;
        try {
            read = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        try {
            while ((line = read.readLine()) != null) {
                if (line.contains("motd=")) {
                    return line.substring(5);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "вашим сервером.";
    }

    public static void shutdown() {
        bot = null;
        localEnabled = true;
        tokenBot = null;
        channel = null;
        loggerChannel = null;
        enable = false;
        DiscordLogger.getInstance().discordBot = null;
        jda.shutdown();
    }

    public static void sendMessageChannel(String message) {
        if (!isEnabled()) return;
        String msg = Util.cancelFormatMessage(message);
        loggerChannel.sendMessage(getDate() + msg).queue();
    }

    public static void sendServerResponse(List<String> msgs) {
        EmbedBuilder message = new EmbedBuilder();
        StringBuilder formatted = new StringBuilder();
        for (String msg : msgs) {
            formatted.append(msg).append("\n");
        }
        message.setTitle("Ответ сервера.");
        message.setDescription(formatted.toString());
        message.setColor(0xffa500);
        loggerChannel.sendTyping().queue();
        loggerChannel.sendMessage(message.build()).queue();
    }

    public static void sendServerResponse(String msg) {
        EmbedBuilder message = new EmbedBuilder();
        message.setTitle("Ответ сервера.");
        message.setDescription(msg);
        message.setColor(0xffa500);
        loggerChannel.sendTyping().queue();
        loggerChannel.sendMessage(message.build()).queue();
    }

    public static void sendImportantMessage(String msg) {
        EmbedBuilder message = new EmbedBuilder();
        message.setTitle("Что-то произошло с ботом!");
        message.setDescription(msg);
        message.setColor(0xf45642);
        loggerChannel.sendTyping().queue();
        loggerChannel.sendMessage(message.build()).queue();
    }

    private static String getDate() {
        LocalDateTime time = LocalDateTime.now();
        String hours, minutes, seconds;
        if (time.getHour() < 10) hours = "0" + time.getHour();
        else hours = String.valueOf(time.getHour());
        if (time.getMinute() < 10) minutes = "0" + time.getMinute();
        else minutes = String.valueOf(time.getMinute());
        if (time.getSecond() < 10) seconds = "0" + time.getSecond();
        else seconds = String.valueOf(time.getSecond());
        return "**[" + hours + ":" + minutes + ":" + seconds + "]:** ";
    }

    public boolean createBot() {
        try {
            jda = new JDABuilder(tokenBot).build().awaitReady();
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.watching("за " + getServerName()));
            prefix = DiscordLogger.getInstance().getConfig().getString("bot.prefix");
            jda.addEventListener(new RemoteConfigControl());
            jda.addEventListener(new BotCommandAdapter());
        } catch (LoginException e) {
            DiscordLogger.getInstance().getLogger().severe("Invalid token!");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loggerChannel = jda.getTextChannelById(channel);
        if (loggerChannel == null) {
            DiscordLogger.getInstance().getLogger().severe("Invalid channel!");
            return false;
        }
        bot = this;
        localEnabled = DiscordLogger.getInstance().getConfig().getBoolean("bot.local-chat");
        commandOnlyOneChannel = DiscordLogger.getInstance().getConfig().getBoolean("bot.command-only-channel");
        setIsWhitelistEnabled(DiscordLogger.getInstance().getConfig().getBoolean("enable-whitelist"));
        enable = true;
        sendImportantMessage("Я включился v" + DiscordLogger.getInstance().getDescription().getVersion() + "!");
        return true;
    }

    public static void sendVerifyMessage(String msg) {
        EmbedBuilder message = new EmbedBuilder();
        message.setTitle("Верификация аккаунтов.");
        message.setDescription(msg);
        message.setColor(0x800080);
        loggerChannel.sendTyping().queue();
        loggerChannel.sendMessage(message.build()).queue();
    }

    public static TextChannel getLoggerChannel() {
        return loggerChannel;
    }

    public static JDA getJDA() {
        return jda;
    }

    public static boolean isIsWhitelistEnabled() {
        return isWhitelistEnabled;
    }

    public static void setIsWhitelistEnabled(boolean isWhitelistEnabled) {
        DiscordBot.isWhitelistEnabled = isWhitelistEnabled;
    }

    public static boolean isEnabled() {
        return jda.getStatus().isInit();
    }

    public static boolean isLocalEnabled() {
        return localEnabled;
    }

    public static void setLocalEnabled(boolean localEnabled) {
        DiscordBot.localEnabled = localEnabled;
    }

}
