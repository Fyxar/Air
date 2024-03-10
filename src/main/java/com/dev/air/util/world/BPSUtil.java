package com.dev.air.util.world;

import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.util.MinecraftInstance;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.util.Vec3;

public class BPSUtil {

    private static Vec3 lastPosition;
    private static double bps;

    @Target
    public void onPreMotion(PreMotionEvent event) {
        if (lastPosition == null) {
            lastPosition = MinecraftInstance.mc.player.getPositionVector();
            return;
        }

        bps = lastPosition.distanceTo(MinecraftInstance.mc.player.getPositionVector()) * (MinecraftInstance.mc.timer.timerSpeed * 20);
        lastPosition = MinecraftInstance.mc.player.getPositionVector();
    }

    public static double getBps() {
        return bps;
    }

}
