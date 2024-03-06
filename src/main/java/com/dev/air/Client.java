package com.dev.air;

import com.dev.air.command.api.Command;
import com.dev.air.command.api.manager.CommandManager;
import com.dev.air.event.impl.other.KeyInputEvent;
import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.imgui.ImGuiImpl;
import com.dev.air.module.api.manager.ModuleManager;
import com.dev.air.module.impl.movement.FlightMod;
import com.dev.air.rotation.RotationManager;
import com.dev.air.ui.clickgui.imgui.ImGuiClickUiScreen;
import com.dev.air.util.MinecraftInstance;
import com.dev.air.viamcp.handlers.AdditionPacketHandler;
import com.dev.air.viamcp.handlers.CooldownHandler;
import com.dev.air.viamcp.handlers.ViaPacketTranslator;
import de.florianmichael.viamcp.ViaMCP;
import net.lenni0451.asmevents.EventManager;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.lwjglx.input.Keyboard;
import org.lwjglx.opengl.Display;

import java.awt.*;

public enum Client {

    instance;

    public static Color MAIN_COLOR = new Color(180, 210, 231);
    public static Color ALT_COLOR = new Color(255, 255, 255);
    public final static String NAME = "Air", VERSION = "1.0", BRANCH = "Development";
    public final static boolean isDevelopment = BRANCH.toUpperCase().contains("DEV");
    private final ModuleManager moduleManager = new ModuleManager();
    private final CommandManager commandManager = new CommandManager();
    private final ImGuiClickUiScreen imGuiScreen = new ImGuiClickUiScreen();

    public void init() {
        System.out.println("Starting " + NAME + " " + VERSION + " " + BRANCH);
        ImGuiImpl.create(Display.getWindow());
        setupRegisters();
        setupViaMCP();

        moduleManager.forEach(module -> { module.setup(); });
    }

    private void setupRegisters() {
        EventManager.register(this);
        EventManager.register(new AdditionPacketHandler());
        EventManager.register(new ViaPacketTranslator());
        EventManager.register(new CooldownHandler());

        EventManager.register(new RotationManager());
    }

    private void setupViaMCP() {
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
    }

    @Target
    public void onKeyInput(KeyInputEvent event) {
        moduleManager.forEach(module -> {
            if (event.getKey() == module.getKey() || event.getKey() == Keyboard.KEY_F && module instanceof FlightMod) module.setEnable(!module.isEnable());
        });
    }

    @Target
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPacket() instanceof C01PacketChatMessage)) return;
        C01PacketChatMessage wrapper = (C01PacketChatMessage) event.getPacket();
        if (wrapper.getMessage().startsWith(".")) {
            event.setCancelled(true);
            String[] args = wrapper.getMessage().replace(".", "").split(" ");

            for (Command command : commandManager) {
                if (wrapper.getMessage().startsWith("." + command.getName().toLowerCase())) {
                    command.handleCommand(args);
                    break;
                }
            }
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
    public ImGuiClickUiScreen getImGuiScreen() {
        return imGuiScreen;
    }

}
