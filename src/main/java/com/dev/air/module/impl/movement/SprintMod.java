package com.dev.air.module.impl.movement;

import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import net.lenni0451.asmevents.event.Target;

@ModuleInfo(name = "Sprint", description = "Holds down your sprint key", category = Category.MOVEMENT)
public class SprintMod extends Module {

    @Target
    public void onPreMotion(PreMotionEvent event) {
        mc.gameSettings.keyBindSprint.setPressed(true);
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindSprint.setPressed(false);
    }

}