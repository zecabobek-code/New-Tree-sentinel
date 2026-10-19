package com.treesentinel.common;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

/**
 * Habilidade especial: o cavalo empina, a alabarda sobe e a pisada causa dano em área com
 * onda de choque. Prioridade maior (número menor) que o SentinelMeleeGoal.
 */
public class SentinelRearSlamGoal extends Goal {
    private static final int SLAM_TICK = 25;  // 1.25 s (impacto da animação REAR_SLAM)
    private static final int END_TICK = 36;   // 1.8 s
    private final PathfinderMob mob;
    private final float radius;
    private int tick = -1;
    private int nextAllowedTick = 100;

    public SentinelRearSlamGoal(PathfinderMob mob, float radius) {
        this.mob = mob;
        this.radius = radius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = mob.getTarget();
        if (t == null || !t.isAlive() || mob.tickCount < nextAllowedTick) return false;
        double d = mob.distanceToSqr(t);
        return d >= 4.0 && d <= (radius + 1.0) * (radius + 1.0) && mob.hasLineOfSight(t);
    }

    @Override
    public boolean canContinueToUse() {
        return tick >= 0 && tick < END_TICK && mob.isAlive();
    }

    @Override
    public void start() {
        tick = 0;
        mob.getNavigation().stop();
        mob.level().broadcastEntityEvent(mob, SentinelAnimated.EVENT_REAR);
        mob.playSound(SoundEvents.HORSE_ANGRY, 2.0F, 0.6F);
    }

    @Override
    public void stop() {
        tick = -1;
        nextAllowedTick = mob.tickCount + 140 + mob.getRandom().nextInt(80);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        tick++;
        LivingEntity t = mob.getTarget();
        if (t != null) mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
        if (tick == SLAM_TICK && mob.level() instanceof ServerLevel level) {
            float damage = mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)
                    ? (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.25F : 10.0F;
            AABB area = mob.getBoundingBox().inflate(radius, 1.5, radius);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area,
                    x -> x != mob && x.isAlive() && x.getType() != mob.getType() && !mob.isAlliedTo(x))) {
                e.hurt(mob.damageSources().mobAttack(mob), damage);
                double dx = e.getX() - mob.getX(), dz = e.getZ() - mob.getZ();
                double len = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
                e.push(dx / len * 0.9, 0.55, dz / len * 0.9);
                e.hurtMarked = true;
            }
            SentinelEffects.slamShockwave(level, mob.getX(), mob.getY(), mob.getZ(), radius);
        }
    }
}
