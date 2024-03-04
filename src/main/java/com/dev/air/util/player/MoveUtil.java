package com.dev.air.util.player;

import com.dev.air.event.impl.tick.movement.MoveInputEvent;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

import static com.dev.air.util.MinecraftInstance.*;

public class MoveUtil {

    public static float getSpeed(double speed) {
        if (mc.player.isPotionActive(Potion.moveSpeed)) {
            speed += (mc.player.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.1F;
        }

        return (float) speed;
    }

    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0;
    }

    public static void boost(float speed) {
        boost(mc.player.rotationYaw, mc.player.moveForward, mc.player.moveStrafing, speed);
    }

    public static void boost(float forward, float strafe, float speed) {
        boost(mc.player.rotationYaw, forward, strafe, speed);
    }

    public static void strafe(float speed) {
        strafe(mc.player.rotationYaw, mc.player.moveForward, mc.player.moveStrafing, speed);
    }

    public static void strafe(float forward, float strafe, float speed) {
        strafe(mc.player.rotationYaw, forward, strafe, speed);
    }

    public static void strafe(float yaw, float forward, float strafe, float speed) {
        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) f = 1.0F;
            f = speed / f;

            strafe = strafe * f;
            forward = forward * f;

            float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0F);
            mc.player.motionX = strafe * f2 - forward * f1;
            mc.player.motionZ = forward * f2 + strafe * f1;
        }
    }

    public static void boost(float yaw, float forward, float strafe, float speed) {
        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) f = 1.0F;
            f = speed / f;

            strafe = strafe * f;
            forward = forward * f;

            float f1 = MathHelper.sin(yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(yaw * (float) Math.PI / 180.0F);
            mc.player.motionX += strafe * f2 - forward * f1;
            mc.player.motionZ += forward * f2 + strafe * f1;
        }
    }

    public static void correctInput(MoveInputEvent event, float yaw) {
        float f1 = MathHelper.sin((mc.player.rotationYaw - yaw) * (float) Math.PI / 180.0F);
        float f2 = MathHelper.cos((mc.player.rotationYaw - yaw) * (float) Math.PI / 180.0F);
        float x = event.getStrafe() * f2 - event.getForward() * f1;
        float z = event.getForward() * f2 + event.getStrafe() * f1;

        event.setStrafe(Math.round(x));
        event.setForward(Math.round(z));
    }

    public static void stop() {
        mc.player.motionX = mc.player.motionZ = 0;
    }

}
