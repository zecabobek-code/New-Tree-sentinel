package com.example.treesentinel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.treesentinel.common.SentinelAnimated;
import com.treesentinel.common.SentinelEffects;
import com.treesentinel.common.SentinelMeleeGoal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Tree Sentinel: cavaleiro dourado montado. Ataques (estado sincronizado ATTACK_STATE):
 *   1 = investida   2 = golpe de alabarda em arco   3 = explosão dourada (fase furiosa)   4 = preparação da investida
 * Cada estado toca uma animação do modelo novo, com o dano sincronizado ao quadro de impacto.
 */
public class TreeSentinelEntity extends Monster implements SentinelAnimated {
    private static final UUID ENRAGE_UUID = UUID.fromString("9d3b6c10-2f4e-4c7a-8a11-5e7d0c2b9f44");

    public static final int STATE_IDLE = 0, STATE_CHARGE = 1, STATE_SWEEP = 2, STATE_BURST = 3, STATE_WINDUP = 4;
    // duracoes = comprimento das animacoes (20 ticks = 1 s)
    private static final int WINDUP_TICKS = 16, CHARGE_TICKS = 18, SWEEP_TICKS = 20, BURST_TICKS = 36;
    private static final int SWEEP_HIT_LEFT = 7;    // golpe acerta 13 ticks (0.65 s) depois de comecar
    private static final int BURST_HIT_LEFT = 11;   // pisada acerta 25 ticks (1.25 s) depois de comecar

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.treesentinel.tree_sentinel"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_12);

    private final Set<UUID> chargeHit = new HashSet<>();
    private int chargeCooldown = 120;
    private int sweepCooldown = 60;
    private int burstCooldown = 300;
    private int chargeTicks = 0;
    private boolean enraged = false;

    private static final EntityDataAccessor<Integer> ATTACK_STATE =
            SynchedEntityData.defineId(TreeSentinelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED_SYNC =
            SynchedEntityData.defineId(TreeSentinelEntity.class, EntityDataSerializers.BOOLEAN);
    private int animTimer = 0;      // servidor: ticks restantes da animação
    public int animTime = 0;        // cliente: ticks desde o início da animação (usado só pelo modelo antigo)
    private int lastState = 0;      // cliente

    // --- estados de animação do modelo novo (só usados no cliente) ---
    private final AnimationState idleAnimation = new AnimationState();
    private final AnimationState meleeAnimation = new AnimationState();
    private final AnimationState attackAnimation = new AnimationState();
    private final AnimationState chargeWindupAnimation = new AnimationState();
    private final AnimationState chargeAnimation = new AnimationState();
    private final AnimationState rearAnimation = new AnimationState();
    private final AnimationState hurtAnimation = new AnimationState();
    private final AnimationState enragedAnimation = new AnimationState();

    @Override public AnimationState idleAnimation() { return idleAnimation; }
    @Override public AnimationState meleeAnimation() { return meleeAnimation; }
    @Override public AnimationState attackAnimation() { return attackAnimation; }
    @Override public AnimationState chargeWindupAnimation() { return chargeWindupAnimation; }
    @Override public AnimationState chargeAnimation() { return chargeAnimation; }
    @Override public AnimationState rearAnimation() { return rearAnimation; }
    @Override public AnimationState hurtAnimation() { return hurtAnimation; }
    @Override public AnimationState enragedAnimation() { return enragedAnimation; }
    @Override public boolean isSentinelEnraged() { return isEnragedClient(); }

    public TreeSentinelEntity(EntityType<? extends TreeSentinelEntity> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_STATE, 0);
        this.entityData.define(ENRAGED_SYNC, false);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    public boolean isEnragedClient() {
        return this.entityData.get(ENRAGED_SYNC);
    }

    private void setAttackState(int state, int duration) {
        this.entityData.set(ATTACK_STATE, state);
        this.animTimer = duration;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            int st = getAttackState();
            if (st != lastState) {
                lastState = st;
                animTime = 0;
                attackAnimation.stop();
                chargeWindupAnimation.stop();
                chargeAnimation.stop();
                rearAnimation.stop();
                switch (st) {
                    case STATE_WINDUP -> chargeWindupAnimation.start(tickCount);
                    case STATE_CHARGE -> chargeAnimation.start(tickCount);
                    case STATE_SWEEP -> attackAnimation.start(tickCount);
                    case STATE_BURST -> rearAnimation.start(tickCount);
                    default -> { }
                }
            } else if (st != 0) {
                animTime++;
            }
            tickSentinelClient(this);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        SentinelEffects.ambient(this);   // aura dourada (só no cliente)
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (!handleSentinelEvent(this, id)) super.handleEntityEvent(id);
    }

    /** Mais fraco que antes: menos vida, armadura e dano. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 650.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // ataque básico com golpe sincronizado; pausa enquanto uma habilidade especial está rodando
        this.goalSelector.addGoal(2, new SentinelMeleeGoal(this, 1.0D, () -> getAttackState() != STATE_IDLE));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance diff, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        data = super.finalizeSpawn(level, diff, reason, data, tag);
        this.populateDefaultEquipmentSlots(level.getRandom(), diff);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance diff) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SENTINEL_HALBERD.get()));
        setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0.0F);
    }

    // Escudo gigante: projéteis causam menos dano (um pouco menos que antes).
    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (src.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.6F;
        boolean hit = super.hurt(src, amount);
        if (hit && amount > 0 && level() instanceof ServerLevel sl) SentinelEffects.hitSparks(sl, this);
        return hit;
    }

    @Override
    public void die(DamageSource src) {
        super.die(src);
        if (level() instanceof ServerLevel sl) SentinelEffects.deathBurst(sl, this);
    }

    /** Gira o corpo e a cabeça em direção ao alvo, no máximo maxStep graus por tick. */
    private void faceTarget(@Nullable LivingEntity t, float maxStep) {
        if (t == null) return;
        double dx = t.getX() - getX(), dz = t.getZ() - getZ();
        float want = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float yaw = getYRot() + Mth.clamp(Mth.wrapDegrees(want - getYRot()), -maxStep, maxStep);
        setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());
        ServerLevel sl = (ServerLevel) level();

        if (!enraged && getHealth() < getMaxHealth() / 2.0F) {
            enraged = true;
            this.entityData.set(ENRAGED_SYNC, true);
            applyEnrage();
            playSound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.5F);
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.5, getZ(), 40, 0.8, 0.8, 0.8, 0.15);
            SentinelEffects.slamShockwave(sl, getX(), getY(), getZ(), 5.0F);
        }
        if (enraged && tickCount % 6 == 0) {
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0, getZ(), 3, 0.6, 1.0, 0.6, 0.02);
        }

        if (chargeCooldown > 0) chargeCooldown--;
        if (sweepCooldown > 0) sweepCooldown--;
        if (burstCooldown > 0) burstCooldown--;

        LivingEntity target = getTarget();

        // Dano durante a investida
        if (chargeTicks > 0) {
            chargeTicks--;
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.0D),
                    x -> x != this && !(x instanceof TreeSentinelEntity) && !chargeHit.contains(x.getUUID()))) {
                chargeHit.add(e.getUUID());
                e.hurt(damageSources().mobAttack(this), enraged ? 26.0F : 20.0F);
                Vec3 push = e.position().subtract(position()).multiply(1, 0, 1).normalize();
                e.push(push.x * 1.5D, 0.6D, push.z * 1.5D);
                e.hurtMarked = true;
            }
            if (chargeTicks % 2 == 0) SentinelEffects.chargeTrail(sl, this);
        }

        // Linha do tempo da animação: o dano sai no quadro de impacto
        int st = getAttackState();
        if (animTimer > 0) {
            animTimer--;
            if (st == STATE_SWEEP || st == STATE_BURST || st == STATE_WINDUP) {
                getNavigation().stop();
                setDeltaMovement(getDeltaMovement().multiply(0.4D, 1.0D, 0.4D));
                faceTarget(target, st == STATE_WINDUP ? 40.0F : 20.0F);
            }
            if (st == STATE_SWEEP && animTimer == SWEEP_HIT_LEFT) sweep(sl);
            if (st == STATE_BURST && animTimer == BURST_HIT_LEFT) burst(sl);
            if (st == STATE_WINDUP && animTimer % 4 == 0) SentinelEffects.chargeDust(sl, this);
            if (animTimer == 0) {
                if (st == STATE_WINDUP && target != null && target.isAlive()) launchCharge(target);
                else this.entityData.set(ATTACK_STATE, STATE_IDLE);
            }
        }

        if (target == null || getAttackState() != STATE_IDLE) return;
        double distSqr = distanceToSqr(target);

        // Investida: primeiro raspa o chão (aviso), depois dispara em linha reta
        if (chargeCooldown == 0 && onGround() && distSqr > 49.0D && distSqr < 625.0D && hasLineOfSight(target)) {
            setAttackState(STATE_WINDUP, WINDUP_TICKS);
            chargeCooldown = enraged ? 100 : 150;
            playSound(SoundEvents.HORSE_ANGRY, 2.0F, 0.6F);
            return;
        }

        // Golpe de alabarda em arco, na frente dele
        if (sweepCooldown == 0 && distSqr < 25.0D) {
            setAttackState(STATE_SWEEP, SWEEP_TICKS);
            playSound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.6F);
            sweepCooldown = enraged ? 50 : 75;
            return;
        }

        // Explosão dourada (só na fase furiosa)
        if (enraged && burstCooldown == 0 && distSqr < 64.0D) {
            setAttackState(STATE_BURST, BURST_TICKS);
            playSound(SoundEvents.HORSE_ANGRY, 2.2F, 0.5F);
            playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 0.6F);
            burstCooldown = 240;
        }
    }

    private void launchCharge(LivingEntity target) {
        faceTarget(target, 360.0F);
        Vec3 dir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
        setDeltaMovement(dir.x * 1.5D, 0.25D, dir.z * 1.5D);
        this.hasImpulse = true;
        chargeHit.clear();
        chargeTicks = CHARGE_TICKS;
        setAttackState(STATE_CHARGE, CHARGE_TICKS);
        playSound(SoundEvents.HORSE_GALLOP, 2.0F, 0.6F);
    }

    private void sweep(ServerLevel sl) {
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.6F);
        Vec3 look = getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 c = position().add(look.scale(2.0D));
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, getY() + 1.5, c.z, 3, 1.0, 0.2, 1.0, 0.0);
        SentinelEffects.swingTrail(sl, this);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.5D),
                x -> x != this && !(x instanceof TreeSentinelEntity))) {
            Vec3 to = e.position().subtract(position()).multiply(1, 0, 1);
            if (to.lengthSqr() < 0.01D || to.normalize().dot(look) < 0.2D) continue;
            e.hurt(damageSources().mobAttack(this), enraged ? 24.0F : 18.0F);
            Vec3 push = to.normalize();
            e.push(push.x * 1.0D, 0.5D, push.z * 1.0D);
            e.hurtMarked = true;
        }
    }

    private void burst(ServerLevel sl) {
        playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 0.7F);
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.0, getZ(), 1, 0, 0, 0, 0);
        SentinelEffects.slamShockwave(sl, getX(), getY(), getZ(), 6.0F);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 60, 3.0, 0.5, 3.0, 0.2);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0D),
                x -> x != this && !(x instanceof TreeSentinelEntity))) {
            e.hurt(damageSources().mobAttack(this), 15.0F);
            Vec3 push = e.position().subtract(position()).multiply(1, 0, 1).normalize();
            e.push(push.x * 1.4D, 0.8D, push.z * 1.4D);
            e.hurtMarked = true;
        }
    }

    private void applyEnrage() {
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(ENRAGE_UUID);
            speed.addPermanentModifier(new AttributeModifier(ENRAGE_UUID, "enrage", 0.04D,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource src, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(src, looting, recentlyHit);
        spawnAtLocation(new ItemStack(ModItems.SENTINEL_HALBERD.get()));
        spawnAtLocation(new ItemStack(Items.GOLD_BLOCK, 2 + random.nextInt(3)));
        spawnAtLocation(new ItemStack(Items.NETHERITE_INGOT, 1 + random.nextInt(2)));
        spawnAtLocation(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
    }

    // ---- barra de chefão ----
    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        bossEvent.addPlayer(p);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        bossEvent.removePlayer(p);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Enraged", enraged);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        enraged = tag.getBoolean("Enraged");
        this.entityData.set(ENRAGED_SYNC, enraged);
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.IRON_GOLEM_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.IRON_GOLEM_DEATH; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.HORSE_AMBIENT; }
}
