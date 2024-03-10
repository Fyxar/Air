package com.dev.air.module.impl.combat;

import com.dev.air.event.impl.render.ItemRendererEvent;
import com.dev.air.event.impl.update.PreUpdateEvent;
import com.dev.air.module.api.Category;
import com.dev.air.module.api.Module;
import com.dev.air.module.api.annotation.ModuleInfo;
import com.dev.air.rotation.RotationManager;
import com.dev.air.util.rotation.other.Rotation;
import com.dev.air.util.math.MathUtil;
import com.dev.air.util.other.Stopwatch;
import com.dev.air.util.packet.PacketUtil;
import com.dev.air.util.rotation.RotationUtil;
import com.dev.air.util.rotation.raycast.RayCastUtil;
import com.dev.air.value.impl.BooleanValue;
import com.dev.air.value.impl.ModeValue;
import com.dev.air.value.impl.NumberValue;
import com.dev.air.value.impl.RangeValue;
import net.lenni0451.asmevents.event.Target;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import org.lwjglx.input.Keyboard;

import java.util.Comparator;

/* the settings hurt my eyes */
@ModuleInfo(name = "Kill Aura", description = "Attack targets for you", category = Category.COMBAT, key = Keyboard.KEY_R)
public class KillAuraMod extends Module {

    private final ModeValue targetMode = new ModeValue("Target", "Single", "Single");
    private final ModeValue filterMode = new ModeValue("Priority", "Health", "Distance", "Health");
    private final NumberValue range = new NumberValue("Range", 3.1, 0.1, 1.0, 6.0);
    private final NumberValue hurtTime = new NumberValue("Hurt Time", 10, 1, 1, 10);
    private final ModeValue autoBlock = new ModeValue("Auto Block", "Off", "Off", "Fake");
    private final ModeValue rotationMode = new ModeValue("Rotation", "Smooth", "Normal", "Smooth");
    private final RangeValue smoothValue = new RangeValue("Smooth Value", 180,  180, 1, 1, 180).requires(rotationMode, "Smooth");
    private final ModeValue randomization = new ModeValue("Randomise (rot)", "Simple", "None", "Simple", "Time");
    private final RangeValue randomiseValue = new RangeValue("Randomise Value", 0,  20, 1, 0, 30).requires(randomization,
            "Time");
    private final ModeValue pitchAim = new ModeValue("Pitch Aim", "Head", "Body", "Switch", "Head", "Random");
    private final ModeValue cpsMode = new ModeValue("CPS Mode", "Gaussian", "Randomization", "Gaussian", "1.9");
    private final RangeValue cps = new RangeValue("CPS", 12, 15, 1, 1, 30).requires(cpsMode, "Randomization");
    private final NumberValue mean = new NumberValue("Mean", 12, 1, 1, 30).requires(cpsMode, "Gaussian");
    private final NumberValue deviation = new NumberValue("Deviation", 4.0, 0.25, 1.0, 15.0).requires(cpsMode, "Gaussian");
    private final NumberValue failRate = new NumberValue("Fail Rate", 0, 0.1, 0, 0.9);
    private final BooleanValue keepSprint = new BooleanValue("Keep Sprint", false);
    private final BooleanValue fixVelocity = new BooleanValue("Move Correction", false);
    private final BooleanValue rayCast = new BooleanValue("Ray Cast", false);

    private long clickDelay;
    private EntityLivingBase target;
    private Stopwatch stopwatch = new Stopwatch();
    private boolean last;
    private long lastAttackMS;

    @Override
    public String getPrefix() {
        return targetMode.getMode();
    }

    @Override
    public void onEnable() {
        lastAttackMS = System.currentTimeMillis();
    }

    @Target
    public void onPreUpdate(PreUpdateEvent event) {
        if (clickDelay == 0) resetClickDelay();
        updateRotation();

        Object[] objects = mc.world.playerEntities.stream().filter(entity -> entity instanceof EntityLivingBase &&
                entity.getEntityId() != -9999 && !entity.getName().contains("renegotiableDis") && entity != mc.player && entity.deathTime == 0 && entity.getDistanceToEntity(mc.player) <= range.getValue()).sorted(sort()).toArray();
        if(objects.length < 1) return;

        if (targetMode.is("Single")) {
            target = (EntityLivingBase) objects[0];

            if (!cpsMode.is("1.9") && stopwatch.hasReached(clickDelay) ||
                    cpsMode.is("1.9") && mc.player.getCooledAttackStrength(0.5F) == 1) {
                if (canAttack(target)) attack(target);

                resetClickDelay();
                lastAttackMS = System.currentTimeMillis();
                stopwatch.reset();
            }
        }
    }

    @Target
    public void onItemRenderer(ItemRendererEvent event) {
        if (autoBlock.is("Fake") && canFakeBlock()) {
            event.setAction(EnumAction.BLOCK);
        }
    }

    private boolean canFakeBlock() {
        return canAttack(target) && mc.player.getHeldItem() != null &&(mc.player.getHeldItem().getItem() instanceof ItemSword ||
                mc.player.getHeldItem().getItem() instanceof ItemAxe);
    }

    public void attack(EntityLivingBase entity) {
        if (!canAttack(target)) return;
        if (!isRayCastHit()) return;

        mc.player.swingItem();
        if (failRate.getFloat() >= Math.random()) return;

        if (keepSprint.isEnabled()) {
            PacketUtil.send(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
            mc.player.resetCooldown();
        }
        else mc.playerController.attackEntity(mc.player, entity);
    }

    public boolean canAttack(EntityLivingBase entity) {
        if (entity == null) return false;
        if (!(entity instanceof EntityPlayer)) return false;
        EntityPlayer player = (EntityPlayer) entity;
        if (player == mc.player) return false;
        if (player.isDead) return false;
        if (player.getDistanceToEntity(mc.player) > range.getValue()) return false;
        if (player.hurtTime > hurtTime.getValue()) return false;

        return true;
    }

    public boolean isRayCastHit() {
        if (RotationManager.getRotation() != null && rayCast.isEnabled() &&
                RayCastUtil.rayCastEntity(range.getFloat(), RotationManager.getRotation().getYaw(), RotationManager.getRotation().getPitch()) != target) return false;

        return true;
    }

    private void updateRotation() {
        if (!canAttack(target)) {
            return;
        }

        if (!pitchAim.is("Switch")) last = pitchAim.is("Head") || (!pitchAim.is("Body")) || !pitchAim.is("Random");
        Rotation targetRotation = RotationUtil.calculateRotationTo(target, last);
        if (pitchAim.is("Random") && mc.player.ticksExisted % (int) MathUtil.randomNormal(5, 10) == 0D) targetRotation = RotationUtil.calculateRotationTo(target,
                MathUtil.randomNormal(1F, 5F));
        if (pitchAim.is("Switch") && mc.player.ticksExisted % (int) MathUtil.randomNormal(5, 30) == 0D) last = !last;

        updateRotation(targetRotation);
    }

    private void updateRotation(Rotation targetRotation) {
        if (mc.player.ticksExisted % 5 == 0 && canAttack(target)) {
            targetRotation.setPitch(targetRotation.getPitch() + (float) MathUtil.randomNormal(-5, 5));

            if (randomization.is("Simple")) {
                targetRotation.setYaw(targetRotation.getYaw() + (float) Math.random());
                targetRotation.setPitch(targetRotation.getPitch() + (float) Math.random());
            }

            if (randomization.is("Time")) {
                double randomYaw = MathUtil.randomLast(randomiseValue.getFirst(), randomiseValue.getSecond(), lastAttackMS), randomPitch = MathUtil.randomLast(randomiseValue.getFirst(), randomiseValue.getSecond(), lastAttackMS);
                targetRotation.setYaw(targetRotation.getYaw() + (float) randomYaw);
                targetRotation.setPitch(targetRotation.getPitch() + (float) randomPitch);
            }
        }

        float turnSpeed = 180F;
        if (rotationMode.is("Smooth")) {
            turnSpeed = (float) MathUtil.randomNormal(this.smoothValue.getFirst(), this.smoothValue.getSecond());;
        }

        RotationManager.rotateTo(targetRotation, turnSpeed, fixVelocity.isEnabled());
    }

    private Comparator<EntityLivingBase> sort() {
        if (filterMode.is("Distance")) {
            return Comparator.comparingDouble(entity -> entity.getDistanceToEntity(mc.player));
        }

        return Comparator.comparingDouble(entity -> entity.getHealth());
    }

    private void resetClickDelay() {
        if (cpsMode.is("Randomization")) {
            clickDelay = (long) (1000L / MathUtil.randomNormal(cps.getFirst(), cps.getSecond()));
        } else {
            clickDelay = (long)  (1000L / MathUtil.randomDeviated(mean.getInt(), deviation.getInt(), 1.25F));
        }
    }

}
