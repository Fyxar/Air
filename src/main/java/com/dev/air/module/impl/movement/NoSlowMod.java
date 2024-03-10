package com.dev.air.module.impl.movement;

import com.dev.air.event.impl.movement.SlowDownEvent;
import com.dev.air.event.impl.packet.PacketReceiveEvent;
import com.dev.air.event.impl.update.PreUpdateEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S09PacketHeldItemChange;

@ModuleInfo(name = "No Slow", description = "Prevent item slowdown", category = Category.MOVEMENT)
public class NoSlowMod extends Module {

     private final ModeValue mode = new ModeValue("Mode", "Ignore", "Ignore", "Custom", "Blink", "Spoof");
     private final NumberValue strafe = new NumberValue("Strafe", 1.0F, 0.1F, 0.1F, 1F).requires(mode, "Custom");
     private final NumberValue forward = new NumberValue("Forward", 1.0F, 0.1F, 0.1F, 1F).requires(mode, "Custom");

     private int lastSpoofSlot;

     @Override
     public String getPrefix() {
          return mode.getMode();
     }

     @Target
     public void onSlowDown(SlowDownEvent event) {
          if (mode.is("Ignore") || mode.is("Spoof") || mode.is("Blink") || mode.is("Grim")) {
               event.setCancelled(true);

               if (mode.is("Spoof")) event.setCancelled(isHoldingSword());
          } else if (mode.is("Custom")) {
               event.setMoveForward(forward.getFloat());
               event.setMoveStrafing(strafe.getFloat());
          }
     }

     @Target
     public void onPreUpdate(PreUpdateEvent event) {
          if (mode.is("Spoof") && isHoldingSword()) {
               PacketUtil.sendNo(new C09PacketHeldItemChange(lastSpoofSlot = mc.player.inventory.currentItem % 9 + 1));
               PacketUtil.sendNo(new C09PacketHeldItemChange(mc.player.inventory.currentItem));
          }
     }

     @Target
     public void onPacketReceive(PacketReceiveEvent event) {
          if (event.getPacket() instanceof S09PacketHeldItemChange && mode.is("Spoof")) {
               S09PacketHeldItemChange wrapper = (S09PacketHeldItemChange) event.getPacket();
               if (wrapper.getHeldItemHotbarIndex() == lastSpoofSlot) event.setCancelled(true);
          }
     }

     private boolean isHoldingSword() {
          return mc.player.getHeldItem() != null && mc.player.getHeldItem().getItem() instanceof ItemSword;
     }

}
