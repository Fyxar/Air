package com.dev.air.rotation;

import com.dev.air.event.impl.packet.update.PostMotionEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.event.impl.tick.movement.MoveInputEvent;
import com.dev.air.event.impl.tick.movement.PlayerJumpEvent;
import com.dev.air.event.impl.tick.movement.PlayerStrafeEvent;
import com.dev.air.event.impl.update.PreUpdateEvent;
import com.dev.air.util.math.MathUtil;
import com.dev.air.util.player.MoveUtil;
import com.dev.air.util.rotation.RotationUtil;
import com.dev.air.util.rotation.other.Rotation;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.util.MathHelper;

import static com.dev.air.util.MinecraftInstance.*;

public class RotationManager {
    
    private static Rotation prevRotation, rotation, targetRotation;
    private static boolean fixVelocity;
    private static double turnSpeed;
    private static int tickSinceLastSet, waitForTick;
    
    public static void rotateTo(Rotation rotation, double turnSpeed, boolean fixVelocity) {
        RotationManager.turnSpeed = turnSpeed;
        RotationManager.fixVelocity = fixVelocity;
        RotationManager.targetRotation = rotation;

        updateRotation(rotation);
        tickSinceLastSet = 0;
    }

    @Target
    public void onPreUpdate(PreUpdateEvent event) {
        if (rotation != null && tickSinceLastSet > 5 && waitForTick == 0) updateRotation(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch));
        if (targetRotation != null && waitForTick == 0 && tickSinceLastSet <= 5) updateRotation(targetRotation);
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        tickSinceLastSet++;
        if (rotation == null) {
            prevRotation = new Rotation(event.getYaw(), event.getPitch());
            return;
        }

        if (waitForTick == 0) {
            event.setYaw(rotation.getYaw());
            event.setPitch(rotation.getPitch());
            mc.player.renderYawOffset = mc.player.renderYawHead = rotation.getYaw();
            mc.player.renderPitchHead = rotation.getPitch();
        }

        if (tickSinceLastSet > 5 && waitForTick == 0) {
            double distanceYaw = Math.abs(MathHelper.wrapAngleTo180_float(mc.player.rotationYaw) - MathHelper.wrapAngleTo180_float(rotation.getYaw()));
            double distancePitch = Math.abs(mc.player.rotationPitch - rotation.getPitch());

            if (distanceYaw <= 2 && distancePitch <= 2) {
                waitForTick = 1;
                return;
            }
        }

        if (waitForTick == 1) {
            waitForTick = 2;
        }

        prevRotation = new Rotation(event.getYaw(), event.getPitch());
    }

    @Target
    public void onPlayerStrafe(PlayerStrafeEvent event) {
        if (rotation != null && fixVelocity) {
            event.setYaw(rotation.getYaw());
        }
    }

    @Target
    public void onPlayerJump(PlayerJumpEvent event) {
        if (rotation != null && fixVelocity) {
            event.setYaw(rotation.getYaw());
        }
    }

    @Target
    public void onMoveInput(MoveInputEvent event) {
        if (rotation != null && fixVelocity) {
            MoveUtil.correctInput(event, rotation.getYaw());
        }
    }

    @Target
    public void onPostMotion(PostMotionEvent event) {
        if (waitForTick == 2) {
            waitForTick = 0;
            rotation = null;
        }
    }

    private static void updateRotation(Rotation targetRotation) {
        if (prevRotation == null) prevRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);

        Rotation cacheRotation = getRotation(targetRotation);

        if (cacheRotation != null)
            rotation = RotationUtil.patchGCD(prevRotation, cacheRotation);

        waitForTick = 0;
    }

    private static Rotation getRotation(Rotation targetRotation) {
        Rotation cacheRotation = null;
        if (turnSpeed < 1F) {
            double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.getYaw() - prevRotation.getYaw());
            double deltaPitch = targetRotation.getPitch() - prevRotation.getPitch();
            float smoothYaw = (float) (deltaYaw * turnSpeed);
            float smoothPitch = (float) (deltaPitch * turnSpeed);

            cacheRotation = new Rotation(prevRotation.getYaw() + smoothYaw, prevRotation.getPitch() + smoothPitch);
        } else {
            cacheRotation = targetRotation;
        }

        return cacheRotation;
    }

    public static Rotation getRotation() {
        return rotation;
    }

}
