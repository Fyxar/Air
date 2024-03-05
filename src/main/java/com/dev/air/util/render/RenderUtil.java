package com.dev.air.util.render;

import net.minecraft.client.gui.Gui;

public class RenderUtil {

    public static void drawRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }

}
