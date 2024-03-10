package com.dev.air.module.impl.combat;

import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.update.PreUpdateEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.value.impl.BooleanValue;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;

@ModuleInfo(name = "Velocity", description = "Handle player velocity nicely :D ", category = Category.COMBAT)
public class VelocityMod extends Module {

    private ModeValue mode = new ModeValue("Mode", "Normal", "Normal", "Grim 1.17", "Skip Tick");
    private NumberValue horizontally = new NumberValue("Horizontally", 0, 1, 0, 100).requires(mode, "Normal");
    private NumberValue vertically = new NumberValue("Vertically", 0, 1, 0, 100).requires(mode, "Normal");
    private BooleanValue damageOnly = new BooleanValue("Damage Only", true);

    private boolean canSpoof, canCancel;
    private int ticks;

    @Override
    public void onEnable() {
        ticks = 0;
        canSpoof = canCancel = false;
    }

    @Override
    public String getPrefix() {
        return mode.getMode();
    }

    @Target
    public void onPreUpdate(PreUpdateEvent event) {
        if (ticks > 0) ticks--;

        if (canSpoof) {
            PacketUtil.sendNo(new C03PacketPlayer.C06PacketPlayerPosLook(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.renderYawHead,
                    mc.player.renderPitchHead, mc.player.onGround));
            PacketUtil.sendNo(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.player).down(),
                    Direction.DOWN));
            canSpoof = false;
        }
    }

    @Target
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;

        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            if (mode.is("Skip Tick")) {
                if (ticks > 0) {
                    event.setCancelled(true);
                    ticks--;
                }
            } else {
                ticks = 0;
            }
        }

        if (event.getPacket() instanceof S19PacketEntityStatus) {
            S19PacketEntityStatus wrapper = (S19PacketEntityStatus) event.getPacket();

            if (wrapper.getOpCode() == 2 && wrapper.getEntity(mc.world) == mc.player) {
                canCancel = true;
            }
        }

        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) event.getPacket();
            if (wrapper.getEntityID() != mc.player.getEntityId()) return;

            if (mode.is("Normal") && (!damageOnly.isEnabled() || canCancel)) {
                if (horizontally.getInt() == 100 && vertically.getInt() == 100) return;
                event.setCancelled(true);
                if (horizontally.getInt() == 0 && vertically.getInt() == 0) return;
                if (horizontally.getInt() == 0 && vertically.getInt() != 0) {
                    mc.player.motionY = (wrapper.getMotionY() / 8000.0D) * (vertically.getFloat() / 100.0F);
                    return;
                }
                if (horizontally.getInt() != 0 && vertically.getInt() == 0) {
                    mc.player.motionX = (wrapper.getMotionX() / 8000.0D) * (horizontally.getFloat() / 100.0F);
                    mc.player.motionZ = (wrapper.getMotionZ() / 8000.0D) * (horizontally.getFloat() / 100.0F);
                    return;
                }

                mc.player.motionX = (wrapper.getMotionX() / 8000.0D) * (horizontally.getFloat() / 100.0D);
                mc.player.motionY = (wrapper.getMotionY() / 8000.0D) * (vertically.getFloat() / 100.0F);
                mc.player.motionZ = (wrapper.getMotionZ() / 8000.0D) * (horizontally.getFloat() / 100.0D);

                canCancel = false;
            }

            if (mode.is("Grim 1.17") && canCancel) {
                canCancel = false;
                canSpoof = true;
                event.setCancelled(true);
            }

            if (mode.is("Skip Tick") && canCancel) {
                ticks = 6;
                canCancel = false;
                event.setCancelled(true);
            }
        }
    }

}

