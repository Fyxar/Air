package com.dev.air.module.impl.combat;

import com.dev.air.event.impl.client.ValueUpdateEvent;
import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.other.ChatUtil;
import com.dev.air.util.other.Stopwatch;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import net.lenni0451.asmevents.event.Target;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Criticals", description = "Make criticals hit without in mid-air", category = Category.COMBAT)
public class CriticalsMod extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Packet", "No Ground", "Packet", "NCP", "Lag-Based");
    private final ModeValue lagMode = new ModeValue("Lag Mode", "Legit", "Legit", "Blatant").requires(mode, "Lag-Based");
    private final NumberValue lagTime = new NumberValue("Lag Time", 200, 1, 1, 500).requires(mode, "Lag-Based");
    private final NumberValue lagChance = new NumberValue("Lag Chance", 80, 1, 1, 100).requires(mode, "Lag-Based");
    private boolean isInAirServerSided, hitGroundYet;
    private List<Packet> packets = new ArrayList<>(), attackPackets = new ArrayList<>();
    private Stopwatch stopwatch = new Stopwatch();

    @Override
    public void onEnable() {
        isInAirServerSided = false;
        hitGroundYet = false;
    }

    @Override
    public void onDisable() {
        releasePackets();
    }

    @Override
    public String getPrefix() {
        return mode.getMode();
    }

    @Target
    public void onValueUpdate(ValueUpdateEvent event) {
        if (event.getValue() == mode && !mode.is("Lag-Based") || event.getValue() == lagTime && lagTime.shouldDisplay()) {
            releasePackets();
            isInAirServerSided = false;
        }
    }

    @Target(priority = EnumEventPriority.HIGH)
    public void onPacketSend(PacketSendEvent event) {
        if (mode.is("Lag-Based")) {
            if (mc.player.onGround) hitGroundYet = true;

            if (!stopwatch.hasReached(lagTime.getInt()) && isInAirServerSided) {
                event.setCancelled(true);
                if (event.getPacket() instanceof C02PacketUseEntity && event.getPacket() instanceof C0APacketAnimation) {
                    if (lagMode.is("Blatant")) {
                        event.setCancelled(false);
                    } else attackPackets.add(event.getPacket());
                } else {
                    packets.add(event.getPacket());
                }
            }

            if (stopwatch.hasReached(lagTime.getInt()) && isInAirServerSided) {
                isInAirServerSided = false;
                releasePackets();
            }
        }

        if (event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity wrapper = (C02PacketUseEntity) event.getPacket();

            Entity entity = wrapper.getEntityFromWorld(mc.world);
            if (entity == null) return;
            if (wrapper.getAction() == C02PacketUseEntity.Action.ATTACK) {
                if (!mc.player.onGround) {
                    if (!isInAirServerSided && hitGroundYet && mc.player.fallDistance <= 1 && (lagChance.getFloat() / 100) > Math.random()) {
                        stopwatch.reset();
                        isInAirServerSided = true;
                        hitGroundYet = false;
                    }
                    return;
                }

                switch (mode.getMode()) {
                    case "Packet":
                        PacketUtil.sendNo(new C03PacketPlayer.C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 0.03, mc.player.posZ, false));
                        mc.player.onCriticalHit(entity);
                        break;
                    case "NCP":
                        if (mc.player.ticksExisted % 10 == 0) {
                            PacketUtil.sendNo(new C03PacketPlayer.C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ, false));
                            PacketUtil.sendNo(new C03PacketPlayer.C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 0.10005, mc.player.posZ, false));
                            PacketUtil.sendNo(new C03PacketPlayer.C04PacketPlayerPosition(mc.player.posX, mc.player.posY + 0.0000012, mc.player.posZ, false));
                            mc.player.onCriticalHit(entity);
                        }
                        break;
                    case "Lag-Based":
                        if (isInAirServerSided) {
                            mc.player.onCriticalHit(entity);
                        }
                        break;
                }
            }
        }
    }

    @Target(priority = EnumEventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (mc.player == null) hitGroundYet = true;
        if (event.getPacket() instanceof S08PacketPlayerPosLook) hitGroundYet = true;
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        if (mode.is("No Ground")) event.setGround(false);
    }

    private void releasePackets() {
        if (mc.world != null && mc.player != null) {
            if (!attackPackets.isEmpty()) attackPackets.forEach(PacketUtil::sendNo);
            if (!packets.isEmpty()) packets.forEach(PacketUtil::sendNo);
        }

        packets.clear();
        attackPackets.clear();
        stopwatch.reset();
    }

}

