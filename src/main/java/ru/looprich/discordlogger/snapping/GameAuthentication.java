package ru.looprich.discordlogger.snapping;


import net.dv8tion.jda.core.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.looprich.discordlogger.DiscordLogger;
import ru.looprich.discordlogger.modules.DiscordBot;

import java.util.Random;

public class GameAuthentication {

    private final String[] allLetter = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J",
            "K", "L", "Z", "X", "C", "V", "B", "N", "M", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    private Player player = null;
    private String playerName;
    private User user;
    private final Random random = new Random();
    private String code;
    private boolean confirm;

    public GameAuthentication(User user, String playerName) {
        this.playerName = playerName;
        this.user = user;
    }

    private void sendCode() {
        StringBuilder newCode = new StringBuilder();
        for (int i = 1; i < 7; i++)
            newCode.append(allLetter[random.nextInt(allLetter.length)]);
        code = newCode.toString();
        user.openPrivateChannel().queue((channel) ->
                channel.sendMessage("Код подтверждения: " + code +
                        "\nВы или кто-то за Вас, пытались привязать Discord к Minecraft аккаунту." +
                        "\nЕсли это не Вы обратитесь к администрации сервера!").queue());
        DiscordBot.sendVerifyMessage("Код подтверждения отправлен!" +
                "\nЕсли код подтверждения не пришел, необходимо разрешить личные сообщения от участников сервера.");
        confirm = false;
    }

    public void regPlayer() {
        for (Player var1 : Bukkit.getOnlinePlayers()) {
            if (playerName.equalsIgnoreCase(var1.getName())) {
                player = var1;
                break;
            }
        }
        if (player == null) {
            DiscordBot.sendVerifyMessage("Игрок оффлайн!");
            return;
        }
        if (DiscordLogger.getInstance().getNetwork().existPlayer(player)) {
            DiscordBot.sendVerifyMessage("Игрок " + player.getName() + " уже привязал Minecraft к Discord аккаунту!");
            return;
        }
        if (DiscordLogger.getInstance().getNetwork().existUser(user)) {
            DiscordBot.sendVerifyMessage("Пользователь " + user.getAsTag() + " уже привязал Discord к Minecraft аккаунту!");
            return;
        }
        sendCode();
        AuthenticationTimer timer = new AuthenticationTimer(this);
        player.sendMessage(ChatColor.GOLD + "Вы или кто-то за Вас, пытаетесь привязать свой Discord(" + user.getAsTag() + ") к Minecraft аккаунту.");
        player.sendMessage(ChatColor.AQUA + "/verify accept <code>" + ChatColor.GREEN + "- принять предложение");
        player.sendMessage(ChatColor.AQUA + "/verify reject " + ChatColor.GREEN + "- отклонить предложение");
        DiscordLogger.getInstance().verifyUsers.add(this);
        timer.runTaskTimer(DiscordLogger.getInstance(), 0L, 20L);
    }


    public void reject() {
        confirm = true;
        DiscordBot.sendVerifyMessage("Игрок " + player.getName() + " отказался привязать свой Minecraft к Discord аккаунту!");
        player.sendMessage(ChatColor.RED + "Вы отказались привязать свой Minecraft к Discord аккаунту!");
        DiscordLogger.getInstance().verifyUsers.remove(this);
    }

    public void accept() {
        confirm = true;
        DiscordBot.sendVerifyMessage("Игрок " + player.getName() + " согласился привязать свой Minecraft к Discord аккаунту!");
        player.sendMessage(ChatColor.GREEN + "Вы согласились привязать свой Minecraft к Discord аккаунту!");
        DiscordLogger.getInstance().getNetwork().verifyPlayer(player, user, code);
        DiscordLogger.getInstance().verifyUsers.remove(this);
    }

    void fail() {
        DiscordBot.sendVerifyMessage("Игрок " + player.getName() + " не успел подтвердить код в игре!");
        player.sendMessage(ChatColor.RED + "Вы не успели подтвердить код!");
        DiscordLogger.getInstance().verifyUsers.remove(this);
    }

    boolean isConfirm() {
        return confirm;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getCode() {
        return code;
    }

}
