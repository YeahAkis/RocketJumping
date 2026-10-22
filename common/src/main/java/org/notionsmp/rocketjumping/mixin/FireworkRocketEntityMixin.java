package org.notionsmp.rocketjumping.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.notionsmp.rocketjumping.RocketJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void rocketjumping$boostOnEntity(EntityHitResult hit, CallbackInfo ci) {
        boost();
    }

    @Inject(method = "onHitBlock", at = @At("HEAD"))
    private void rocketjumping$boostOnBlock(BlockHitResult hit, CallbackInfo ci) {
        boost();
    }

    @Unique
    private void boost() {
        FireworkRocketEntity rocket = (FireworkRocketEntity) (Object) this;
        if (rocket.level() instanceof ServerLevel level) {
            RocketJumping.boost(rocket, level);
        }
    }

    @Redirect(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean rocketjumping$shooterDamage(LivingEntity target, ServerLevel level, DamageSource source, float amount) {
        FireworkRocketEntity rocket = (FireworkRocketEntity) (Object) this;
        if (!RocketJumping.config().damageShooter && target == rocket.getOwner()) {
            return false;
        }
        return target.hurtServer(level, source, amount);
    }
}
