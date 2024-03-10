package com.dev.air.module.impl.render;

import com.dev.air.Client;
import com.dev.air.event.impl.render.Render2DEvent;
import com.dev.air.font.Fonts;
import com.dev.air.font.impl.CustomFontRenderer;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.math.ColorUtil;
import com.dev.air.util.render.RenderUtil;
import com.dev.air.value.impl.ModeValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@ModuleInfo(name = "Arraylist", description = "Show the current enabled mod", category = Category.RENDER, autoEnable = true)
public class ArraylistMod extends Module {

    private ModeValue mode = new ModeValue("Mode", "Classic","Classic");
    private ModeValue colorMode = new ModeValue("Color", "Static","Gradient", "Static", "Rainbow", "Astolfo");

    @Target
    public void onRender2D(Render2DEvent event) {
        ScaledResolution resolution = new ScaledResolution(mc);

        int renderY = 5, index = 0, count = 0;
        switch (mode.getMode()) {
            case "Classic":
                for (Module module : Client.instance.getModuleManager().sort((CustomFontRenderer) null)) {
                    if (!module.isEnable() || module.isHidden()) continue;
                    String display = module.getPrefix().isEmpty() ? module.getName() :  module.getName() + " §7" + module.getPrefix();
                    int renderX = resolution.getScaledWidth() - 5 - mc.fontRenderer.getStringWidth(display);
                    mc.fontRenderer.drawStringWithShadow(display, renderX, renderY, getColor(index, count));
                    index += 15;
                    renderY += mc.fontRenderer.FONT_HEIGHT;
                    count += 1;
                }
                break;
        }

    }

    private int getColor(int index, int count) {
        int color = ColorUtil.interpolateColorsBackAndForth(10, index, Client.MAIN_COLOR, Client.ALT_COLOR, false).getRGB();
        if (colorMode.is("Static")) color = Client.MAIN_COLOR.getRGB();
        if (colorMode.is("Rainbow")) color = ColorUtil.getRainbowWave(7000, index * 20, 0.4F, 1F);
        if (colorMode.is("Astolfo")) color = ColorUtil.getAstolfoWave(5000, 1, 4, count * 100, 0.6F, 0.7F);
        return color;
    }

}
