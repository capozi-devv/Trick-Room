package moth.trickroom.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import moth.trickroom.TrickRoomMod;
import moth.trickroom.entity.TrickRoomCollision;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.TridentItem;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @WrapMethod(method = "use")
    private ActionResult use(World world, PlayerEntity user, Hand hand, Operation<ActionResult> original) {
        if (TrickRoomMod.trickRoomEntities.isEmpty()) return original.call(world,user,hand);
        if (TrickRoomCollision.entityInsideAnyRoom(user)) {
            user.sendMessage(Text.of("Your trident feels too heavy to use...").copy().formatted(Formatting.ITALIC, Formatting.GRAY), true);
            user.getItemCooldownManager().set(user.getStackInHand(hand), 200);
            return ActionResult.FAIL;
        }
        return original.call(world, user, hand);
    }
}
