package com.dev.air.module.impl.render;

import com.dev.air.Client;
import com.dev.air.event.impl.render.Render2DEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.value.impl.ModeValue;
import net.lenni0451.asmevents.event.Target;

import java.text.SimpleDateFormat;
import java.util.Date;

@ModuleInfo(name = "Watermark", description = "Show client watermark", category = Category.RENDER, autoEnable = true)
public class WatermarkMod extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Minecraft", "Minecraft");

    @Target
    public void onRender2D(Render2DEvent event) {
        switch (mode.getMode()) {
            case "Minecraft":
                String time = new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()));
                mc.fontRenderer.drawStringWithShadow(Client.NAME + " §7(" + time + ")", 5, 5, -1);
                break;
        }
    }

}
