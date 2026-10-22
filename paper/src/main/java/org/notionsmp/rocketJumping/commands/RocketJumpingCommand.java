package org.notionsmp.rocketJumping.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.notionsmp.rocketJumping.RocketJumping;

@CommandAlias("rocketjumping|rocket")
@CommandPermission("rocketjumping.admin")
public class RocketJumpingCommand extends BaseCommand {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Default
    @Subcommand("help")
    public void onDefault(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("""
            <gradient:gold:yellow>RocketJumping Help</gradient>
            <gray>/rocket reload</gray> - Reloads the config"""));
    }

    @Subcommand("reload|rl")
    public void onReload(CommandSender sender) {
        if (RocketJumping.getInstance().reloadPluginConfig()) {
            sender.sendMessage(miniMessage.deserialize("<green>Config reloaded successfully!</green>"));
        } else {
            sender.sendMessage(miniMessage.deserialize("<red>Failed to reload config! Check console for errors.</red>"));
        }
    }
}
