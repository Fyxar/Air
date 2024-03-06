package com.dev.air.util.packet;

import net.minecraft.network.Packet;

import static com.dev.air.util.MinecraftInstance.*;

public class PacketUtil {

    public static void send(final Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static void sendNo(final Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().sendNo(packet);
    }

    public static void receiveNo(final Packet<?> packet) {
        mc.getNetHandler().getNetworkManager().receiveNo(packet);
    }

}
