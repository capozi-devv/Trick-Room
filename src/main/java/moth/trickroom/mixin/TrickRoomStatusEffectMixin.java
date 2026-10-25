package moth.trickroom.mixin;

import moth.trickroom.TrickRoomMod;
import moth.trickroom.entity.TrickRoomCollision;
import moth.trickroom.effect.ModStatusEffects;
import moth.trickroom.item.TrickRoomItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class TrickRoomStatusEffectMixin {
    @Shadow
    @Nullable
    public abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void trickroom$discardBoundRoomsOnDeath(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            for (ItemStack stack : player.getInventory().getMainStacks()) {
                TrickRoomItem.discardBoundRoom(player.getEntityWorld(), stack);
            }
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float trickroom$increaseVulnerabilityDamage(float amount) {
        StatusEffectInstance vulnerability = getStatusEffect(ModStatusEffects.VULNERABILITY);
        if (vulnerability == null) {
            return amount;
        }
        return amount + amount * (0.4F * (vulnerability.getAmplifier() + 1));
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void trickroom$stopTravelWhenImmobilized(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (getStatusEffect(ModStatusEffects.IMMOBILIZED) == null) {
            return;
        }

        entity.setVelocity(Vec3d.ZERO);
        entity.velocityDirty = true;
        entity.setSprinting(false);
        entity.setSneaking(false);
        ci.cancel();
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void trickroom$stopJumpWhenImmobilized(CallbackInfo ci) {
        if (getStatusEffect(ModStatusEffects.IMMOBILIZED) != null) {
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private StatusEffectInstance trickroom$reverseStatusEffectInsideRoom(StatusEffectInstance effect) {
        if (TrickRoomMod.trickRoomEntities.isEmpty()) return effect;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!ModStatusEffects.isRoomReversalDisabled() && TrickRoomCollision.entityInsideAnyRoom(entity)) {
            return ModStatusEffects.reverse(effect);
        }
        return effect;
    }
}
