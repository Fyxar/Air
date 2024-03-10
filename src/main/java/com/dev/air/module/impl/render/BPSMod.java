package com.dev.air.module.impl.render;

import com.dev.air.Client;
import com.dev.air.event.impl.render.Render2DEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.world.BPSUtil;
import com.dev.air.value.impl.ModeValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.client.gui.ScaledResolution;

import java.text.SimpleDateFormat;
import java.util.Date;

@ModuleInfo(name = "BPSDisplay", description = "Show your current blocks per second", category = Category.RENDER, autoEnable = true)
public class BPSMod extends Module {

    @Target
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        String bps = "BPS: " + String.format("%.2f", BPSUtil.getBps());
        int x = sr.getScaledWidth() - mc.fontRenderer.getStringWidth(bps) - 5, y = sr.getScaledHeight() - 9 - 5;
        mc.fontRenderer.drawStringWithShadow(bps, x, y, -1);
    }

}
