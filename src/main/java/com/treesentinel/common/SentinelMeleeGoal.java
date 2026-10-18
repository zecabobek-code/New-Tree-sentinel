package com.treesentinel.common;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Ataque corpo a corpo sincronizado com a animação ATTACK: o golpe só acerta no momento em que
 * a alabarda desce (tick 11). Use no lugar do MeleeAttackGoal.
 */
public class SentinelMeleeGoal extends Goal {
    private static final int HIT_TICK = 11;   // 0.55 s
    private static final int END_TICK = 18;   // 0.9 s
    private final PathfinderMob mob;
    private final double speed;
    private int phase = -1;
    private int cooldown = 0;
    private int repath = 0;

    public SentinelMeleeGoal(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = mob.getTarget();
        return t != null && t.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return phase >= 0 || canUse();
    }

    @Override
    public void stop() {
        phase = -1;
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean inReach(LivingEntity t) {
        double reach = mob.getBbWidth() * 2.0F + t.getBbWidth() + 1.5;
        return mob.distanceToSqr(t) <= reach * reach;
    }

    @Override
    public void tick() {
        LivingEntity t = mob.getTarget();
        if (t == null) return;
        mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
        if (cooldown > 0) cooldown--;

        if (phase < 0) {
            if (--repath <= 0) {
                repath = 4 + mob.getRandom().nextInt(7);
                mob.getNavigation().moveTo(t, speed);
            }
            if (cooldown <= 0 && inReach(t)) {
                phase = 0;
                mob.getNavigation().stop();
                mob.level().broadcastEntityEvent(mob, SentinelAnimated.EVENT_ATTACK);
            }
        } else {
            phase++;
            if (phase == HIT_TICK) {
                if (inReach(t)) mob.doHurtTarget(t);
                if (mob.level() instanceof ServerLevel sl) SentinelEffects.swingTrail(sl, mob);
            }
            if (phase >= END_TICK) {
                phase = -1;
                cooldown = 10;
            }
        }
    }
}
