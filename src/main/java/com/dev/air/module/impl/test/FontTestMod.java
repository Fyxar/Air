package com.dev.air.module.impl.test;

import com.dev.air.event.impl.render.Render2DEvent;
import com.dev.air.font.Fonts;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import net.lenni0451.asmevents.event.Target;

@ModuleInfo(name = "FontTest", description = "Testing font renderer", category = Category.TEST)
public class FontTestMod extends Module {

    @Target
    public void onRender2D(Render2DEvent event) {
        String testText = "ABCDEFGHIJKLMNOPQRSTUVWXYZ: Air";
        Fonts.robotoRegular18.drawString(testText, 5, 5, -1);
        Fonts.smallPixel18.drawString(testText, 5, 50, -1);
        Fonts.verdana18.drawString(testText, 5, 80, -1);
    }

}
