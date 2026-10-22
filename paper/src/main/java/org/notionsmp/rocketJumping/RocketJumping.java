package org.notionsmp.rocketJumping;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.notionsmp.rocketJumping.commands.RocketJumpingCommand;
import org.notionsmp.rocketJumping.listeners.RocketListener;

import java.io.File;

@Getter
public final class RocketJumping extends JavaPlugin implements Listener {

    @Getter
    private static RocketJumping instance;
    private static final double CURRENT_CONFIG_VERSION = 2.0;
    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        migrateConfig();

        registerListener(new RocketListener());

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

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public boolean reloadPluginConfig() {
        try {
            reloadConfig();
            saveDefaultConfig();
            getLogger().info("Config reloaded successfully!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to reload config: " + e.getMessage());
            return false;
        }
    }
}