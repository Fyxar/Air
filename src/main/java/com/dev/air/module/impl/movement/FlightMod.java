package com.dev.air.module.impl.movement;

import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.player.MoveUtil;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.block.BlockAir;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "Flight", description = "Flight in survival", category = Category.MOVEMENT)
public class FlightMod extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Velocity", "Velocity");
    private final NumberValue velocitySpeed = new NumberValue("Speed", 1.0F, 0.1F, 0F, 1F).requires(mode, "Velocity");

    @Override
    public String getPrefix() {
        return mode.getMode();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1F;
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        if (mode.is("Velocity")) {
            mc.player.motionY = mc.gameSettings.keyBindSneak.isKeyDown() ? -velocitySpeed.getValue() : mc.gameSettings.keyBindJump.isKeyDown() ? velocitySpeed.getValue() : 0;
            if (MoveUtil.isMoving()) MoveUtil.strafe(velocitySpeed.getFloat());
        }
    }

    @Target
    public void onPacketReceive(PacketReceiveEvent event) {
    }

}
