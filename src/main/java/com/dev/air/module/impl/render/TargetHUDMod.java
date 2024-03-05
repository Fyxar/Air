package com.dev.air.module.impl.render;

import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.event.impl.render.Render2DEvent;
import com.dev.air.font.Fonts;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.render.RenderUtil;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleInfo(name = "TargetHUD", description = "Render info about the current target", category = Category.RENDER)
public class TargetHUDMod extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Air", "Air");
    private final NumberValue paddingX = new NumberValue("Padding X", 0, 1, 0, 100);
    private final NumberValue paddingY = new NumberValue("Padding Y", 0, 1, 0, 100);

    private EntityPlayer player;
    private int ticksSinceAttack;

    @Override
    public void onEnable() {
        player = null;
    }

    @Override
    public String getPrefix() {
        return mode.getMode();
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        ticksSinceAttack++;

        if (ticksSinceAttack > 20) {
            player = null;
        }
    }

    @Target
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity wrapper = (C02PacketUseEntity) event.getPacket();
            if (wrapper.getEntityFromWorld(mc.world) instanceof EntityPlayer && wrapper.getAction() == C02PacketUseEntity.Action.ATTACK) {
                ticksSinceAttack = 0;
                player = (EntityPlayer) wrapper.getEntityFromWorld(mc.world);
            }
        }
    }

    @Target
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        int x = (sr.getScaledWidth() / 2) + paddingX.getInt(), y = (sr.getScaledHeight() / 2) + paddingY.getInt();
        if (player == null) return;
        switch (mode.getMode()) {
            case "Air":
                RenderUtil.drawRect(x, y, 120, 40, new Color(0, 0, 0, 120).getRGB());
                Fonts.robotoRegular18.drawString(player.getName(), x + 45, y + 8, -1);
                double offset = -(player.hurtTime * 20);
                Color color = new Color(255, (int) (255 + offset), (int) (255 + offset));
                GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
                mc.getTextureManager().bindTexture(((AbstractClientPlayer)player).getLocationSkin());
                Gui.drawScaledCustomSizeModalRect(x + 5, y + 5, 3, 3, 3, 3, 30, 30, 24, 24);
                GlStateManager.color(1, 1, 1, 1);
                RenderUtil.drawRect(x + 45, y + 20, 70, 15, new Color(255, 255, 255, 120).getRGB());
                RenderUtil.drawRect(x + 45, y + 20, (int) (70 * (player.getHealth() / player.getMaxHealth())), 15, new Color(243, 118, 91).darker().getRGB());
                String s = (int) ((player.getHealth() / player.getMaxHealth()) * 100) + "%";
                Fonts.robotoRegular18.drawString(s, x + 45 + (70 / 2) - (Fonts.robotoRegular18.getStringWidth(s) / 2), y + 20 + (15 / 2) - (Fonts.robotoRegular18.getHeight() / 2) + 1, -1);
                break;
        }
    }

}
