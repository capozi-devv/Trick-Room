package moth.trickroom.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import moth.trickroom.TrickRoomMod;
import moth.trickroom.entity.TrickRoomCollision;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class TrickRoomEntityCollisionMixin {

    @Inject(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void trickroom$keepMovementInsideRoom(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        Entity entity = (Entity) (Object) this;
        cir.setReturnValue(TrickRoomCollision.clampExitMovement(entity, cir.getReturnValue()));
    }

    @WrapMethod(
            method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;"
    )
    private static Vec3d trickroom$addCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions, Operation<Vec3d> original) {
        if (TrickRoomMod.trickRoomEntities.isEmpty()) return original.call(entity,movement,entityBoundingBox,world,collisions);

        List<VoxelShape> roomShapes = TrickRoomCollision.collisionShapes(entity, entityBoundingBox, world, movement);
        if (roomShapes.isEmpty()) return original.call(entity,movement,entityBoundingBox,world,collisions);

        List<VoxelShape> combinedCollisions = new ArrayList<>(collisions.size() + roomShapes.size());
        combinedCollisions.addAll(collisions);
        combinedCollisions.addAll(roomShapes);
        return original.call(entity,movement,entityBoundingBox,world,combinedCollisions);
    }
}
