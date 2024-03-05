package com.dev.air.module.impl.world;

import com.dev.air.event.impl.client.ValueUpdateEvent;
import com.dev.air.event.impl.packet.PacketSendEvent;
import com.dev.air.event.impl.packet.update.PostMotionEvent;
import com.dev.air.event.impl.packet.update.PreMotionEvent;
import com.dev.air.event.impl.tick.movement.MoveInputEvent;
import com.dev.air.event.impl.tick.movement.PlayerJumpEvent;
import com.dev.air.event.impl.tick.movement.PlayerStrafeEvent;
import com.dev.air.event.impl.update.PostUpdateEvent;
import com.dev.air.event.impl.update.PreUpdateEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.util.math.MathUtil;
import com.dev.air.util.other.Stopwatch;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.player.ItemUtil;
import com.dev.air.util.player.MoveUtil;
import com.dev.air.util.rotation.RotationUtil;
import com.dev.air.util.rotation.other.Rotation;
import com.dev.air.util.world.BlockUtil;
import com.dev.air.value.impl.BooleanValue;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import com.dev.air.value.impl.RangeValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "Scaffold", description = "Place block under your feet.", category = Category.WORLD)
public class ScaffoldMod extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Normal", "Normal", "Watchdog");
    private final ModeValue pick = new ModeValue("Pick", "Always", "Always", "Switch", "Spoof");
    private final ModeValue sprint = new ModeValue("Sprint", "Off", "Off", "Normal", "Spoof");
    private final ModeValue eagle = new ModeValue("Eagle", "Off", "Off", "Sneak");
    private final ModeValue rotationMode = new ModeValue("Rotation Mode", "Smooth", "Normal", "Smooth", "1.17 Snap");
    private final ModeValue rotationType = new ModeValue("Rotation Type", "Center", "Center", "Simple");
    private final RangeValue smoothValue = new RangeValue("Smooth Value", 0.4,  0.6, 0.1, 0.1, 1).requires(rotationMode, "Smooth");
    private final ModeValue randomization = new ModeValue("Randomise (rot)", "Simple", "None", "Simple");
    private final RangeValue randomiseValue = new RangeValue("Randomise Value", 0,  20, 1, 0, 30).requires(randomization,
            "Time");
    private final NumberValue delayValue = new NumberValue("Delay Value", 0, 1, 1, 5000);
    private final BooleanValue sameY = new BooleanValue("Same Y", false);
    private final BooleanValue swing = new BooleanValue("Swing", false);
    private final BooleanValue fixVelocity = new BooleanValue("Move Correction", false);
    private final BooleanValue rayCast = new BooleanValue("Ray Cast", false);

    private BlockUtil.BlockData blockData;
    private Stopwatch stopwatch = new Stopwatch();
    private Rotation prevRotation, rotation;
    private int lastSlot, playerY;


    @Override
    public String getPrefix() {
        return mode.getMode();
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
        playerY = -1;
        blockData = null;
        stopwatch.reset();
        if (mc.player != null) {
            lastSlot = mc.player.inventory.currentItem;

            if (sprint.is("Spoof") && mc.player.isSprinting())
                PacketUtil.sendNo(new C0BPacketEntityAction(mc.player, C0BPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    @Target
    public void onPreUpdate(PreUpdateEvent event) {
        if (playerY == -1 || mc.player.onGround) playerY = (int) mc.player.posY - 1;
        if (lastSlot == -1) lastSlot = mc.player.inventory.currentItem;
        BlockPos pos = sameY.isEnabled() ? new BlockPos(mc.player.posX, playerY, mc.player.posZ) : new BlockPos(mc.player).down();
        this.blockData = BlockUtil.getBlockData(pos);
        int slot = ItemUtil.searchBlock();
        if (this.blockData == null) return;
        updateRotation();
        if (slot == -1) return;
        int lastSlot = mc.player.inventory.currentItem ;
        mc.player.inventory.currentItem = slot - 36;
        placeBlock();
        if (!pick.is("Always")) mc.player.inventory.currentItem = lastSlot;

        if (mode.is("Watchdog")) {
            mc.player.motionX *= 0.98F;
            mc.player.motionZ *= 0.98F;
        }

        if (rotationMode.is("1.17 Snap"))
            PacketUtil.sendNo(new C03PacketPlayer.C06PacketPlayerPosLook(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
    }



    @Target
    public void onPostUpdate(PostUpdateEvent event) {
        if (sprint.is("Off")) {
            mc.player.setSprinting(false);
            mc.gameSettings.keyBindSprint.setPressed(false);
        }

        if (eagle.is("Sneak")) {
            boolean isOnEdge = mc.world.getBlockState(new BlockPos(mc.player).down()).getBlock() instanceof BlockAir && mc.player.onGround;
            mc.gameSettings.keyBindSneak.setPressed(isOnEdge || GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        } else {
            mc.gameSettings.keyBindSneak.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        }
    }

    @Target
    public void onPlayerStrafe(PlayerStrafeEvent event) {
        if (rotation != null && fixVelocity.isEnabled()) {
            event.setYaw(rotation.getYaw());
        }
    }

    @Target
    public void onPlayerJump(PlayerJumpEvent event) {
        if (rotation != null && fixVelocity.isEnabled()) {
            event.setYaw(rotation.getYaw());
        }
    }

    @Target
    public void onMoveInput(MoveInputEvent event) {
        if (rotation != null && fixVelocity.isEnabled()) {
            MoveUtil.correctInput(event, rotation.getYaw());
        }
    }

    @Target
    public void onPreMotion(PreMotionEvent event) {
        if (rotation == null) {
            prevRotation = new Rotation(event.getYaw(), event.getPitch());
            return;
        }

        event.setYaw(rotation.getYaw());
        event.setPitch(rotation.getPitch());
        mc.player.renderYawOffset = mc.player.renderYawHead = rotation.getYaw();
        mc.player.renderPitchHead = rotation.getPitch();

        prevRotation = new Rotation(event.getYaw(), event.getPitch());
    }

    @Target
    public void onPacketSend(PacketSendEvent event) {
        if(pick.is("Spoof") && event.getPacket() instanceof C09PacketHeldItemChange) {
            C09PacketHeldItemChange wrapper = (C09PacketHeldItemChange) event.getPacket();
            if (lastSlot == wrapper.getSlotId()) event.setCancelled(true);
            if(mc.player.inventory.getStackInSlot(wrapper.getSlotId()) == null
                    || !(mc.player.inventory.getStackInSlot(wrapper.getSlotId()).getItem() instanceof ItemBlock)) if (MoveUtil.isMoving()) event.setCancelled(true);

            if(!event.isCancelled()) lastSlot = wrapper.getSlotId();
        }

        if(sprint.is("Spoof") && event.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction wrapper = (C0BPacketEntityAction) event.getPacket();

            if (wrapper.getAction() == C0BPacketEntityAction.Action.START_SPRINTING || wrapper.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING)
                event.setCancelled(true);
        }
    }

    @Target
    public void onValueUpdate(ValueUpdateEvent event) {
        if (event.getValue() == sprint && sprint.is("Spoof")) {
            if (mc.player.isSprinting())
                PacketUtil.sendNo(new C0BPacketEntityAction(mc.player, C0BPacketEntityAction.Action.STOP_SPRINTING));
        }
    }

    public ModeValue getSprint() {
        return sprint;
    }

    private void placeBlock() {
        if(blockData == null) return;
        if(delayValue.getValue() != 0 && !stopwatch.hasReached(delayValue.getInt())) return;
        if(!(mc.world.getBlockState(blockData.position.offset(blockData.direction)).getBlock() instanceof BlockAir)) return;
        Vec3 hitVec = new Vec3(blockData.position.offset(blockData.direction)).addVector(0.5, 0.5, 0.5)
                .add(blockData.direction.getOpposite().getDirectionVec().multiply(0.5));
        mc.playerController.onPlayerRightClick(mc.player, mc.world, mc.player.getHeldItem(), blockData.position, blockData.direction, hitVec);
        if (swing.isEnabled()) mc.player.swingItem();
        else PacketUtil.sendNo(new C0APacketAnimation());

        stopwatch.reset();
    }

    private void updateRotation() {
        Vec3 hitVec = new Vec3(blockData.position.offset(blockData.direction)).addVector(0.5, 0.5, 0.5)
                .add(blockData.direction.getOpposite().getDirectionVec().multiply(0.5));
        Rotation targetRotation = RotationUtil.calculateRotationTo(hitVec);
        if (rotationMode.is("1.17 Snap")) {
            PacketUtil.sendNo(new C03PacketPlayer.C06PacketPlayerPosLook(mc.player.posX, mc.player.posY, mc.player.posZ, targetRotation.getYaw(), targetRotation.getPitch(), mc.player.onGround));
            rotation = null;
            return;
        }

        if (rotationType.is("Simple")) {
            switch (blockData.direction) {
                case EAST:
                    if(mc.player.getPosition().getZ() > blockData.position.getZ()) targetRotation.setYaw(135);
                    else targetRotation.setYaw(45);
                    break;
                case WEST:
                    if(mc.player.getPosition().getZ() > blockData.position.getZ()) targetRotation.setYaw(-135);
                    else targetRotation.setYaw(-45);
                    break;
                case NORTH:
                    if(mc.player.getPosition().getX() > blockData.position.getX()) targetRotation.setYaw(45);
                    else targetRotation.setYaw(-45);
                    break;
                case SOUTH:
                    if(mc.player.getPosition().getX() > blockData.position.getX()) targetRotation.setYaw(135);
                    else targetRotation.setYaw(-135);
                    break;
                default:
                    break;
            }
        }

        targetRotation.setPitch(81);
        if (prevRotation == null) prevRotation = new Rotation(mc.player.rotationYaw, mc.player.rotationPitch);

        updateRotation(targetRotation);
    }

    private void updateRotation(Rotation targetRotation) {
        Rotation cacheRotation = null;
        /* default randomization */
        if (mc.player.ticksExisted % 5 == 0 && MoveUtil.isMoving()) {
            targetRotation.setPitch(targetRotation.getPitch() + (float) MathUtil.randomNormal(-5, 5));

            if (randomization.is("Simple")) {
                targetRotation.setYaw(targetRotation.getYaw() + (float) Math.random());
                targetRotation.setPitch(targetRotation.getPitch() + (float) Math.random());
            }
        }

        switch (rotationMode.getMode()) {
            case "Smooth":
                double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.getYaw() - prevRotation.getYaw());
                double deltaPitch = targetRotation.getPitch() - prevRotation.getPitch();
                double smoothValue = MathUtil.randomNormal(this.smoothValue.getFirst(), this.smoothValue.getSecond());
                float smoothYaw = (float) (deltaYaw * smoothValue);
                float smoothPitch =(float) (deltaPitch * smoothValue);

                cacheRotation = new Rotation(prevRotation.getYaw() + smoothYaw, prevRotation.getPitch() + smoothPitch);
                break;

            case "Normal":
                cacheRotation = targetRotation;
                break;
        }

        if (cacheRotation != null)
            rotation = RotationUtil.patchGCD(prevRotation, cacheRotation);
    }

}
