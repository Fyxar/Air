package com.dev.air.module.impl.combat;

import com.dev.air.event.api.SendTypeEvent;
import com.dev.air.event.impl.client.ValueUpdateEvent;
import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.event.impl.render.Render3DEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.packet.other.TimedPacket;
import com.dev.air.value.impl.BooleanValue;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import com.mojang.authlib.GameProfile;
import net.lenni0451.asmevents.event.Target;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.*;
import net.minecraft.util.Vec3;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//TODO: Actually backtrack :)
@ModuleInfo(name = "Back Track", description = "Hit target last position", category = Category.COMBAT)
public class BackTrackMod extends Module {

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Target
    public void onValueUpdate(ValueUpdateEvent event) {
    }

    @Target
    public void onPacketSend(PacketSendEvent event) {
    }

    @Target(priority = EnumEventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
    }

    @Target
    public void onRender3D(Render3DEvent event) {
    }

}

