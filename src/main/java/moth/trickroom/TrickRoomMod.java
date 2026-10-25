package moth.trickroom;

import moth.trickroom.effect.ModStatusEffects;
import moth.trickroom.entity.ModEntityTypes;
import moth.trickroom.entity.TrickRoomCollision;
import moth.trickroom.entity.TrickRoomEntity;
import moth.trickroom.item.DataComponentTypeInit;
import moth.trickroom.item.ModItems;
import moth.trickroom.network.TrickRoomFlashPacket;
import moth.trickroom.network.TrickRoomModePacket;
import moth.trickroom.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.apostasy.apostle.api.event.MagicCallbacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public final class TrickRoomMod implements ModInitializer {
    public static final String MOD_ID = "trickroom";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ArrayList<TrickRoomEntity> trickRoomEntities = new ArrayList<>();

    @Override
    public void onInitialize() {
        ModItems.register();
        ModSounds.register();
        ModStatusEffects.register();
        ModEntityTypes.register();
        DataComponentTypeInit.init();
        PayloadTypeRegistry.playC2S().register(TrickRoomModePacket.ID, TrickRoomModePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TrickRoomFlashPacket.ID, TrickRoomFlashPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TrickRoomModePacket.ID, TrickRoomModePacket::receive);
        AttackEntityCallback.EVENT.register(this::preventFrozenAttacks);
        LOGGER.info("Initialized {}", MOD_ID);
        try {
            MagicCallbacks.CAN_CAST_SPELL.register( (user, world, spell) -> {
                if (TrickRoomCollision.entityInsideAnyRoom(user)) {
                    return false;
                }
                return true;
            });
        } catch (Error e) {
            e.printStackTrace();
        }

        ServerLifecycleEvents.SERVER_STARTED.register((s)->{
            trickRoomEntities.clear(); // resets the entity cache for singleplayer
        });
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    private ActionResult preventFrozenAttacks(PlayerEntity player, World world, Hand hand, Entity target,
                                              EntityHitResult hitResult) {
        return player.hasStatusEffect(ModStatusEffects.IMMOBILIZED) ? ActionResult.FAIL : ActionResult.PASS;
    }
}
