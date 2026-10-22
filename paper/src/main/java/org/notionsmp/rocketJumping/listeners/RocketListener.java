package org.notionsmp.rocketJumping.listeners;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.notionsmp.rocketJumping.RocketJumping;

public class RocketListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Firework firework) {
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            if (fireworkMeta.getEffects().isEmpty()) {
                return;
            }
            int flightDuration = fireworkMeta.getPower();
            if (flightDuration <= 0) {
                return;
            }

            FileConfiguration config = RocketJumping.getInstance().getConfig();
            double baseBoostPower = config.getDouble("boostPower", 4.0);
            double multiplier = config.getDouble("multiplier", 0.8);
            boolean damageShooter = config.getBoolean("damageShooter", false);
            boolean creeperExplodeRockets = config.getBoolean("creeperExplodeRockets", false);
            float explosionPower = (float) config.getDouble("explosionPower", 2.0);
            boolean explosionSetFire = config.getBoolean("explosionSetFire", false);
            boolean explosionBreakBlocks = config.getBoolean("explosionBreakBlocks", false);
            double effectRadius = config.getDouble("radius", 5.0);

            double scaledBoostPower = baseBoostPower * (flightDuration * multiplier);
            Entity shooter = (Entity) firework.getShooter();

            Location fireworkLoc = firework.getLocation();

            for (Entity entity : firework.getNearbyEntities(effectRadius, effectRadius, effectRadius)) {
                double distance = entity.getLocation().distance(fireworkLoc);
                if (distance > effectRadius) continue;

                double distanceFactor = 1.0 - (distance / effectRadius);

                Vector boostDirection = entity.getLocation().toVector()
                        .subtract(fireworkLoc.toVector())
                        .normalize();

                double finalBoostPower = scaledBoostPower * distanceFactor;

                entity.setVelocity(entity.getVelocity().add(boostDirection.multiply(finalBoostPower)));

                RocketJumping.getInstance().getLogger().info("damageShooter config value: " + damageShooter);
                if (entity instanceof Player player && !damageShooter && player.equals(shooter)) {
                    RocketJumping.getInstance().getLogger().info("Passed damageShooter checks:");
                    player.setNoDamageTicks(5);
                }
            }

            if (creeperExplodeRockets) {
                boolean hasCreeperEffect = fireworkMeta.getEffects().stream()
                        .anyMatch(effect -> effect.getType() == FireworkEffect.Type.CREEPER);
                if (hasCreeperEffect) {
                    World world = fireworkLoc.getWorld();
                    if (world != null) {
                        world.createExplosion(fireworkLoc, explosionPower, explosionSetFire, explosionBreakBlocks);
                    }
                }
            }
        }
    }
}