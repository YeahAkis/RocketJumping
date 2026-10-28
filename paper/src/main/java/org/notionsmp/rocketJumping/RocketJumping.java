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

package org.notionsmp.rocketJumping;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.notionsmp.rocketJumping.commands.RocketJumpingCommand;
import org.notionsmp.rocketJumping.listeners.RocketListener;
import org.notionsmp.rocketJumping.listeners.VulcanListener;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public final class RocketJumping extends JavaPlugin implements Listener {

    @Getter
    private static RocketJumping instance;
    private static final double CURRENT_CONFIG_VERSION = 2.0;
    private PaperCommandManager commandManager;
    private static final List<String> DEFAULT_VULCAN_CHECKS = List.of("Flight", "Speed", "Motion", "Jump", "Velocity", "Ground Spoof", "Step", "Elytra", "Fast Climb", "Wall Climb");
    private final JumpTracker jumpTracker = new JumpTracker();
    private volatile Set<String> exemptVulcanChecks = Set.of();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        migrateConfig();

        registerListener(new RocketListener());

        loadVulcanChecks();
        if (getServer().getPluginManager().isPluginEnabled("Vulcan")) {
            registerListener(new VulcanListener());
            getLogger().info("Vulcan detected, rocket jump movement flags will be suppressed");
        }

        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new RocketJumpingCommand());
    }

    private void migrateConfig() {

        File configFile = new File(getDataFolder(), "config.yml");
        YamlConfiguration diskConfig = YamlConfiguration.loadConfiguration(configFile);

        if (!diskConfig.contains("config-version") || diskConfig.getDouble("config-version") < CURRENT_CONFIG_VERSION) {
            getLogger().info("Config migration needed - current version: " +
                    (diskConfig.contains("config-version") ? diskConfig.getDouble("config-version") : "none"));

            double oldBoostPower = diskConfig.getDouble("boostPower", 4.0);
            boolean oldDamageShooter = diskConfig.getBoolean("damageShooter", false);

            if (configFile.delete()) {
                saveDefaultConfig();
                reloadConfig();

                FileConfiguration newConfig = getConfig();
                newConfig.set("boostPower", oldBoostPower);
                newConfig.set("damageShooter", oldDamageShooter);
                newConfig.set("config-version", CURRENT_CONFIG_VERSION);
                saveConfig();

                getLogger().info("Config migrated successfully to version " + CURRENT_CONFIG_VERSION);
            } else {
                getLogger().warning("Failed to delete old config file during migration");
            }
        }
    }

    public boolean isExemptVulcanCheck(String checkName) {
        return checkName != null && exemptVulcanChecks.contains(normalizeCheckName(checkName));
    }

    private void loadVulcanChecks() {
        List<String> configured = getConfig().isList("vulcanChecks")
                ? getConfig().getStringList("vulcanChecks")
                : DEFAULT_VULCAN_CHECKS;
        exemptVulcanChecks = configured.stream()
                .map(RocketJumping::normalizeCheckName)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalizeCheckName(String name) {
        return name.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public boolean reloadPluginConfig() {
        try {
            reloadConfig();
            saveDefaultConfig();
            loadVulcanChecks();
            getLogger().info("Config reloaded successfully!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to reload config: " + e.getMessage());
            return false;
        }
    }
}