package com.dev.air.module.impl.combat;

import com.dev.air.Client;
import com.dev.air.event.api.SendTypeEvent;
import com.dev.air.event.impl.client.ValueUpdateEvent;
import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.event.impl.render.Render3DEvent;
import com.dev.air.event.impl.tick.PreTickEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.packet.other.TimedPacket;
import com.dev.air.util.render.Render3DUtil;
import com.dev.air.value.impl.BooleanValue;
import com.dev.air.value.impl.ColorValue;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import com.mojang.authlib.GameProfile;
import net.lenni0451.asmevents.event.Target;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.*;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//TODO: Actually backtrack :)
@ModuleInfo(name = "Back Track", description = "Hit target last position", category = Category.COMBAT)
public class BackTrackMod extends Module {

    private final NumberValue delay = new NumberValue("Delay", 200, 1, 1, 1000);
    private final ColorValue color = new ColorValue("Color", new Color(63, 150, 206));

    private Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private Vec3 vec3, lastVec3;
    private EntityLivingBase target;
    private int attackTicks;

    @Override
    public String getPrefix() {
        return delay.getInt() + "ms";
    }

    @Override
    public void onDisable() {
        if (mc.player != null && !packetQueue.isEmpty()) packetQueue.forEach(timedPacket -> { PacketUtil.receiveNo(timedPacket.getPacket()); });
        packetQueue.clear();
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        vec3 = lastVec3 = null;
        target = null;
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        attackTicks++;

        if (attackTicks > 7 || vec3.distanceTo(mc.player.getPositionVector()) > 6) {
            target = null;
            vec3 = lastVec3 = null;
        }

        lastVec3 = vec3;
    }

    @Target
    public void onPreTick(PreTickEvent event) {
        release();
    }

    @Target
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity wrapper = (C02PacketUseEntity) event.getPacket();
            if (wrapper.getAction() != C02PacketUseEntity.Action.ATTACK) return;
            attackTicks = 0;

            Entity entity = wrapper.getEntityFromWorld(mc.world);
            if (target != null && wrapper.getEntityId() == target.getEntityId()) return;
            if (!(entity instanceof EntityPlayer)) return;
            target = (EntityLivingBase) entity;
            vec3 = lastVec3 = entity.getPositionVector();
        }
    }

    @Target(priority = EnumEventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (mc.player == null || mc.player.ticksExisted < 20) {
            packetQueue.clear();
            return;
        }

        if (target == null) {
            releaseAll();
            return;
        }
        if (event.isCancelled()) return;

        if (event.getPacket() instanceof S19PacketEntityStatus || event.getPacket() instanceof S02PacketChat) return;
        if (event.getPacket() instanceof S08PacketPlayerPosLook || event.getPacket() instanceof S40PacketDisconnect) {
            releaseAll();
            target = null;
            vec3 = lastVec3 = null;
            return;
        }else if (event.getPacket() instanceof S13PacketDestroyEntities) {
            S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) event.getPacket();
            for (int id : wrapper.getEntityIDs()) {
                if (id == target.getEntityId()) {
                    target = null;
                    vec3 = lastVec3 = null;
                    releaseAll();
                    return;
                }
            }
        } else if (event.getPacket() instanceof S14PacketEntity) {
            S14PacketEntity wrapper = (S14PacketEntity) event.getPacket();
            if (wrapper.getEntityId() == target.getEntityId()) {
                vec3 = vec3.addVector(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D, wrapper.func_149064_e() / 32.0D);
            }
        }else if (event.getPacket() instanceof S18PacketEntityTeleport) {
            S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) event.getPacket();
            if (wrapper.getEntityId() == target.getEntityId()) {
                vec3 = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
            }
        }

        packetQueue.add(new TimedPacket(event.getPacket()));
        event.setCancelled(true);
    }

    @Target
    public void onRender3D(Render3DEvent event) {
        if (target == null) return;
        Render3DUtil.drawBackTrackBox(target, vec3, lastVec3, color.getColor(), false);
    }

    private void release() {
        while (!packetQueue.isEmpty()) {
            if (packetQueue.peek().getStopwatch().hasReached(delay.getInt())) {
                Packet packet = packetQueue.poll().getPacket();
                PacketUtil.receiveNo(packet);
            } else {
                break;
            }
        }

        if (packetQueue.isEmpty() && target != null) {
            vec3 = target.getPositionVector();
        }
    }

    private void releaseAll() {
        if (!packetQueue.isEmpty()) {
            packetQueue.forEach(timedPacket -> { PacketUtil.receiveNo(timedPacket.getPacket()); });
            packetQueue.clear();
        }
    }

}

