/*
 * RocketJumping, fireworks that launch players and nearby entities
 * Copyright (C) 2024-2026  Richard Mikloš
 *
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
