package com.dev.air.ui.altmanager;

import com.dev.air.ui.custom.CustomButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.Screen;

public class AltManagerScreen extends Screen {

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new CustomButton(1, this.width / 2, this.height / 2, 80, 80, "xd"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
