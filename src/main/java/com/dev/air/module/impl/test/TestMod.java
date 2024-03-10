package com.dev.air.module.impl.test;

import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.packet.update.PostMotionEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.Development;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.player.MoveUtil;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

@Development
@ModuleInfo(name = "Test", description = "For testing purpose.", category = Category.TEST)
public class TestMod extends Module {

    /* works like a placeholder, for me testing and hotswap code when needed. */

    @Target
    public void onPreMotion(PreMotionEvent event) {
        PacketUtil.sendNo(new C0FPacketConfirmTransaction(0, (short) 0, true));
    }

    @Target
    public void onPostMotion(PostMotionEvent event) {
    }

    @Target
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            event.setCancelled(true);
        }
    }

}
