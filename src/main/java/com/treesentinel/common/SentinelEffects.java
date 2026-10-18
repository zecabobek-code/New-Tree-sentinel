package com.treesentinel.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Efeitos visuais e sonoros do Tree Sentinel (partículas douradas, rastro do golpe, onda de choque). */
public final class SentinelEffects {
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.80F, 0.25F), 1.1F);
    private static final DustParticleOptions GOLD_BIG = new DustParticleOptions(new Vector3f(1.0F, 0.85F, 0.30F), 1.9F);

    private SentinelEffects() {}

    /** Aura dourada constante. Chame em aiStep() (roda só no cliente). */
    public static void ambient(LivingEntity e) {
        Level level = e.level();
        if (!level.isClientSide || !e.isAlive()) return;
        RandomSource r = e.getRandom();

        if (e.tickCount % 2 == 0) {
            // centelhas saindo do penacho
            double px = e.getX() + r.nextGaussian() * 0.30;
            double py = e.getY() + e.getBbHeight() * 0.80 + r.nextDouble() * 0.7;
            double pz = e.getZ() + r.nextGaussian() * 0.30;
            level.addParticle(GOLD, px, py, pz, 0.0, 0.01, 0.0);
            if (r.nextInt(4) == 0) level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0.0, 0.02, 0.0);
        }
        // poeira dourada nos cascos ao galopar
        Vec3 v = e.getDeltaMovement();
        if (v.horizontalDistanceSqr() > 0.008 && e.onGround() && e.tickCount % 2 == 0) {
            double side = (r.nextBoolean() ? 1 : -1) * 0.6;
            level.addParticle(GOLD, e.getX() + side + r.nextGaussian() * 0.2, e.getY() + 0.1,
                    e.getZ() + r.nextGaussian() * 0.8, 0.0, 0.03, 0.0);
        }
    }

    /** Rastro do golpe de alabarda: arco dourado do alto até o chão, na frente da entidade. */
    public static void swingTrail(ServerLevel level, LivingEntity e) {
        float yaw = e.yBodyRot * ((float) Math.PI / 180.0F);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double h = e.getBbHeight();
        int steps = 16;
        for (int i = 0; i <= steps; i++) {
            double a = (i / (double) steps) * Math.PI * 0.6;
            double forward = 0.4 + 3.4 * Math.sin(a);
            double height = h * 0.95 - (h * 0.90) * (1.0 - Math.cos(a));
            double x = e.getX() + fx * forward, y = e.getY() + height, z = e.getZ() + fz * forward;
            level.sendParticles(GOLD_BIG, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            if (i % 3 == 0) level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.05, 0.05, 0.05, 0.02);
        }
        double ix = e.getX() + fx * 3.6, iz = e.getZ() + fz * 3.6;
        level.sendParticles(ParticleTypes.CRIT, ix, e.getY() + 0.5, iz, 14, 0.4, 0.3, 0.4, 0.3);
        level.playSound(null, e.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.4F, 0.6F);
    }

    /** Onda de choque da pisada do cavalo: anéis dourados e fumaça se expandindo + detritos do bloco. */
    public static void slamShockwave(ServerLevel level, double x, double y, double z, float radius) {
        for (int ring = 1; ring <= 3; ring++) {
            double rr = radius * ring / 3.0;
            int count = 14 + ring * 8;
            for (int i = 0; i < count; i++) {
                double ang = (Math.PI * 2.0) * i / count;
                double dx = Math.cos(ang), dz = Math.sin(ang);
                level.sendParticles(ParticleTypes.CLOUD, x + dx * rr * 0.4, y + 0.1, z + dz * rr * 0.4, 0, dx, 0.02, dz, 0.18 + ring * 0.08);
                if (ring != 2) level.sendParticles(ParticleTypes.END_ROD, x + dx * rr * 0.4, y + 0.2, z + dz * rr * 0.4, 0, dx, 0.04, dz, 0.30 + ring * 0.10);
                level.sendParticles(GOLD_BIG, x + dx * rr, y + 0.2, z + dz * rr, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        BlockPos below = BlockPos.containing(x, y - 0.5, z);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(below)), x, y + 0.3, z, 60, radius * 0.4, 0.2, radius * 0.4, 0.25);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 3, 0.8, 0.1, 0.8, 0.0);
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.6F, 0.55F);
        level.playSound(null, x, y, z, SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2F, 0.5F);
    }
}
