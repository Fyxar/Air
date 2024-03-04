package com.dev.air.module.impl.movement;

import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.player.MoveUtil;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;
import org.lwjglx.input.Keyboard;

import java.util.Comparator;

@ModuleInfo(name = "Speed", description = "Boost up your movement speed", category = Category.MOVEMENT)
public class SpeedMod extends Module {

    private ModeValue mode = new ModeValue("Mode", "Strafe", "Strafe", "Ground Strafe", "Grim Collide", "Vulcan");
    private NumberValue strafeSpeed = new NumberValue("Strafe Speed", 0.33F, 0.05F, 0.1F, 1F).requires(mode, "Strafe");
    private NumberValue groundStrafeSpeed = new NumberValue("Ground Strafe Speed", 0.38F, 0.05F, 0.1F, 1F).requires(mode, "Ground Strafe");
    private NumberValue grimCollideSpeed = new NumberValue("Collide Speed", 0.081F, 0.01F, 0.01F, 0.09F).requires(mode, "Grim Collide");

    private int tickSinceNear;

    @Override
    public String getPrefix() {
        return mode.getMode();
    }

    @Override
    public void onEnable() {
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        tickSinceNear++;

        if (MoveUtil.isMoving()) {
            switch (mode.getMode()) {
                case "Strafe":
                    if (mc.player.onGround) mc.player.jump();
                    MoveUtil.strafe(MoveUtil.getSpeed(strafeSpeed.getFloat()));
                    break;
                case "Ground Strafe":
                    if (mc.player.onGround) {
                        mc.player.jump();

                        MoveUtil.strafe(MoveUtil.getSpeed(groundStrafeSpeed.getFloat()));
                    }
                    break;
                case "Grim Collide":
                    Object[] objects = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityLivingBase && entity != mc.player &&
                            ((EntityLivingBase) entity).deathTime == 0 && entity.getDistanceToEntity(mc.player) <=
                            1.5).sorted(Comparator.comparingDouble(entity -> entity.getDistanceToEntity(mc.player))).toArray();
                    if (objects.length > 0) tickSinceNear = 0;
                    if (tickSinceNear < 2) MoveUtil.boost(mc.player.movementInput.originalForward,mc.player.movementInput.originalStrafe, grimCollideSpeed.getFloat());
                    break;
                case "Vulcan" :
                    if (mc.player.onGround) {
                        mc.player.jump();
                        MoveUtil.strafe(MoveUtil.getSpeed(0.38));
                    }

                    if (mc.player.offGroundTicks == 1) MoveUtil.strafe(MoveUtil.getSpeed(0.3));

                    break;
            }
        } else {
            MoveUtil.stop();
        }
    }

}
