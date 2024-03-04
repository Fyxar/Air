package com.dev.air.util.rotation;

import com.dev.air.util.rotation.other.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import static com.dev.air.util.MinecraftInstance.*;

public class RotationUtil {

    public static Rotation calculateRotationTo(Entity entity) {
        return calculateRotationTo(entity, true);
    }

    public static Rotation calculateRotationTo(Entity entity, boolean head) {
        return calculateRotationTo(entity, head ? 1f : 2f);
    }

    public static Rotation calculateRotationTo(Vec3 vec3) {
        double d0 = vec3.xCoord - mc.player.posX;
        double d1 = vec3.yCoord - (mc.player.posY + mc.player.getEyeHeight());
        double d2 = vec3.zCoord - mc.player.posZ;
        double d3 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        float f = (float) (MathHelper.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float) (-(MathHelper.atan2(d1, d3) * 180.0D / Math.PI));

        return new Rotation(f, f1);
    }

    public static Rotation calculateRotationTo(Entity entity, double value) {
        double d0 = (entity.posX - (entity.posX - entity.lastTickPosX)) - mc.player.posX;
        double d1 = (entity.posY - (entity.posY - entity.lastTickPosY) + entity.getEyeHeight() / value - (mc.player.posY + mc.player.getEyeHeight()));
        double d2 = (entity.posZ - (entity.posZ - entity.lastTickPosZ)) - mc.player.posZ;
        double d3 = MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        float f = (float) (MathHelper.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float) (-(MathHelper.atan2(d1, d3) * 180.0D / Math.PI));

        return new Rotation(f, f1);
    }

    public static Rotation patchGCD(Rotation prevRotation, Rotation currentRotation) {
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 8.0F * 0.15F;
        final float deltaYaw = currentRotation.getYaw() - prevRotation.getYaw(),
                    deltaPitch = currentRotation.getPitch() - prevRotation.getPitch();
        final float yaw = prevRotation.getYaw() + Math.round(deltaYaw / gcd) * gcd,
                    pitch = prevRotation.getPitch() + Math.round(deltaPitch / gcd) * gcd;

        return new Rotation(yaw, pitch);
    }


}
