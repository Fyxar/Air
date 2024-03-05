package com.dev.air.module.impl.test;

import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.Development;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.player.MoveUtil;
import net.lenni0451.asmevents.event.Target;

@Development
@ModuleInfo(name = "Test", description = "For testing purpose.", category = Category.TEST)
public class TestMod extends Module {

    /* works like a placeholder, for me testing and hotswap code when needed. */

    @Target
    public void onPreMotion(PreMotionEvent event) {
        if (MoveUtil.isMoving()) {
            MoveUtil.strafe(0.25F);
        }
    }

}
