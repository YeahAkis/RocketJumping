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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.notionsmp.rocketJumping.RocketJumping;

public class RocketListener implements Listener {

    private static final double MIN_TRACKED_BOOST = 0.25;
    private static final long MIN_AIR_MILLIS = 250L;
    private static final long LANDING_TAIL_MILLIS = 250L;

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
            double baseBoostPower = config.getDouble("boostPower", 1.5);
            double multiplier = config.getDouble("multiplier", 0.8);
            boolean creeperExplodeRockets = config.getBoolean("creeperExplodeRockets", true);
            float explosionPower = (float) config.getDouble("explosionPower", 2.0);
            boolean explosionSetFire = config.getBoolean("explosionSetFire", false);
            boolean explosionBreakBlocks = config.getBoolean("explosionBreakBlocks", false);
            double effectRadius = config.getDouble("radius", 5.0);

            double scaledBoostPower = baseBoostPower * (flightDuration * multiplier);

            Location fireworkLoc = firework.getLocation();

            for (Entity entity : firework.getNearbyEntities(effectRadius, effectRadius, effectRadius)) {
                double distance = entity.getLocation().distance(fireworkLoc);
                if (distance > effectRadius) continue;

                double distanceFactor = 1.0 - (distance / effectRadius);

                Vector boostDirection = entity.getLocation().toVector()
                        .subtract(fireworkLoc.toVector())
                        .normalize();

                double finalBoostPower = scaledBoostPower * distanceFactor;

                if (entity instanceof Player player && finalBoostPower >= MIN_TRACKED_BOOST) {
                    long graceMillis = config.getLong("vulcanGraceMillis", 3000L);
                    long maxMillis = config.getLong("vulcanMaxMillis", 8000L);
                    RocketJumping.getInstance().getJumpTracker().mark(player.getUniqueId(), graceMillis, maxMillis);
                }

                entity.setVelocity(entity.getVelocity().add(boostDirection.multiply(finalBoostPower)));
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework firework
                && firework.getShooter() == event.getEntity()
                && !RocketJumping.getInstance().getConfig().getBoolean("damageShooter", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOnGround()) {
            RocketJumping.getInstance().getJumpTracker().land(player.getUniqueId(), MIN_AIR_MILLIS, LANDING_TAIL_MILLIS);
        }
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        RocketJumping.getInstance().getJumpTracker().clear(event.getPlayer().getUniqueId());
    }
}
