package org.notionsmp.rocketjumping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RocketJumping {
    private static final Path FILE = Path.of("config", "rocketjumping.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile RocketConfig config = load();

    private RocketJumping() {
    }

    public static RocketConfig config() {
        return config;
    }

    private static RocketConfig load() {
        if (!Files.exists(FILE)) {
            RocketConfig defaults = new RocketConfig();
            try {
                Files.createDirectories(FILE.getParent());
                try (Writer writer = Files.newBufferedWriter(FILE)) {
                    GSON.toJson(defaults, writer);
                }
            } catch (IOException ignored) {
            }
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            RocketConfig loaded = GSON.fromJson(reader, RocketConfig.class);
            return loaded == null ? new RocketConfig() : loaded;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public static boolean reload() {
        RocketConfig loaded = load();
        if (loaded == null) {
            return false;
        }
        config = loaded;
        return true;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> reload = Commands.literal("reload")
                .executes(context -> {
                    if (reload()) {
                        context.getSource().sendSuccess(() -> Component.literal("Config reloaded successfully"), true);
                        return 1;
                    }
                    context.getSource().sendFailure(Component.literal("Failed to reload the config, check the console"));
                    return 0;
                })
                .build();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("rocketjumping")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("RocketJumping: /rocketjumping reload"), false);
                    return 1;
                })
                .then(reload)
                .then(Commands.literal("rl").redirect(reload));
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);
        dispatcher.register(Commands.literal("rocket").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).redirect(node));
    }

    public static void boost(FireworkRocketEntity rocket, ServerLevel level) {
        Fireworks fireworks = rocket.getItem().get(DataComponents.FIREWORKS);
        if (fireworks == null || fireworks.explosions().isEmpty() || fireworks.flightDuration() <= 0) {
            return;
        }
        RocketConfig settings = config;
        double scaled = settings.boostPower * fireworks.flightDuration() * settings.multiplier;
        Vec3 origin = rocket.position();
        AABB area = rocket.getBoundingBox().inflate(settings.radius);

        for (Entity entity : level.getEntities(rocket, area)) {
            double distance = entity.position().distanceTo(origin);
            if (distance > settings.radius || distance == 0.0) {
                continue;
            }
            Vec3 direction = entity.position().subtract(origin).normalize();
            double power = scaled * (1.0 - distance / settings.radius);
            entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(power)));
            entity.needsSync = true;
        }

        boolean creeper = fireworks.explosions().stream().anyMatch(explosion -> explosion.shape() == FireworkExplosion.Shape.CREEPER);
        if (settings.creeperExplodeRockets && creeper) {
            Level.ExplosionInteraction interaction = settings.explosionBreakBlocks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE;
            level.explode(rocket, origin.x, origin.y, origin.z, (float) settings.explosionPower, settings.explosionSetFire, interaction);
        }
    }
}
