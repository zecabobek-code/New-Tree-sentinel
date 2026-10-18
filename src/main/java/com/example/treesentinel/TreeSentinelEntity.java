package com.example.treesentinel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

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
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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

public class TreeSentinelEntity extends Monster {
    private static final UUID ENRAGE_UUID = UUID.fromString("9d3b6c10-2f4e-4c7a-8a11-5e7d0c2b9f44");

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.treesentinel.tree_sentinel"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_12);

    private final Set<UUID> chargeHit = new HashSet<>();
    private int chargeCooldown = 100;
    private int sweepCooldown = 40;
    private int burstCooldown = 240;
    private int chargeTicks = 0;
    private boolean enraged = false;

    // --- animação: estado sincronizado servidor -> cliente ---
    // 0 = parado, 1 = galope, 2 = golpe de alabarda, 3 = explosão dourada
    private static final EntityDataAccessor<Integer> ATTACK_STATE =
            SynchedEntityData.defineId(TreeSentinelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED_SYNC =
            SynchedEntityData.defineId(TreeSentinelEntity.class, EntityDataSerializers.BOOLEAN);
    private int animTimer = 0;      // servidor: ticks restantes da animação
    public int animTime = 0;        // cliente: ticks desde o início da animação
    private int lastState = 0;      // cliente

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
            } else if (st != 0) {
                animTime++;
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
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

    // Escudo gigante: projéteis causam bem menos dano.
    @Override
    public boolean hurt(DamageSource src, float amount) {
        if (src.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.4F;
        return super.hurt(src, amount);
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
        }
        if (enraged && tickCount % 6 == 0) {
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0, getZ(), 3, 0.6, 1.0, 0.6, 0.02);
        }

        if (chargeCooldown > 0) chargeCooldown--;
        if (sweepCooldown > 0) sweepCooldown--;
        if (burstCooldown > 0) burstCooldown--;

        // Dano durante a investida
        if (chargeTicks > 0) {
            chargeTicks--;
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.0D),
                    x -> x != this && !(x instanceof TreeSentinelEntity) && !chargeHit.contains(x.getUUID()))) {
                chargeHit.add(e.getUUID());
                e.hurt(damageSources().mobAttack(this), enraged ? 40.0F : 30.0F);
                Vec3 push = e.position().subtract(position()).multiply(1, 0, 1).normalize();
                e.push(push.x * 1.5D, 0.6D, push.z * 1.5D);
                e.hurtMarked = true;
            }
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.3, getZ(), 4, 0.5, 0.1, 0.5, 0.05);
            sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1, getZ(), 6, 0.9, 0.05, 0.9, 0.02);
        }

        // Temporizador da animação (o dano sai no meio dela, dando tempo de reagir)
        if (animTimer > 0) {
            animTimer--;
            int st = getAttackState();
            if (st == 2 && animTimer == 7) sweep(sl);
            if (st == 3 && animTimer == 8) burst(sl);
            if (st == 2 || st == 3) getNavigation().stop();
            if (animTimer == 0) this.entityData.set(ATTACK_STATE, 0);
        }

        LivingEntity target = getTarget();
        if (target == null) return;
        double distSqr = distanceToSqr(target);

        // Galope: investida em linha reta
        if (chargeCooldown == 0 && onGround() && getAttackState() == 0 && distSqr > 49.0D && distSqr < 625.0D) {
            Vec3 dir = target.position().subtract(position()).multiply(1, 0, 1).normalize();
            setDeltaMovement(dir.x * 1.6D, 0.3D, dir.z * 1.6D);
            this.hasImpulse = true;
            chargeHit.clear();
            chargeTicks = 18;
            setAttackState(1, 18);
            chargeCooldown = enraged ? 80 : 130;
            playSound(SoundEvents.HORSE_GALLOP, 2.0F, 0.6F);
        }

        // Golpe de alabarda em arco, na frente dele
        if (sweepCooldown == 0 && distSqr < 25.0D && getAttackState() == 0) {
            setAttackState(2, 14);
            playSound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.6F);
            sweepCooldown = enraged ? 40 : 65;
        }

        // Explosão dourada (só na fase furiosa)
        if (enraged && burstCooldown == 0 && distSqr < 64.0D && getAttackState() == 0) {
            setAttackState(3, 20);
            playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 0.6F);
            burstCooldown = 200;
        }
    }

    private void sweep(ServerLevel sl) {
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0F, 0.6F);
        Vec3 look = getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 c = position().add(look.scale(2.0D));
        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, getY() + 1.5, c.z, 3, 1.0, 0.2, 1.0, 0.0);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.5D),
                x -> x != this && !(x instanceof TreeSentinelEntity))) {
            Vec3 to = e.position().subtract(position()).multiply(1, 0, 1);
            if (to.lengthSqr() < 0.01D || to.normalize().dot(look) < 0.2D) continue;
            e.hurt(damageSources().mobAttack(this), enraged ? 36.0F : 28.0F);
            Vec3 push = to.normalize();
            e.push(push.x * 1.0D, 0.5D, push.z * 1.0D);
            e.hurtMarked = true;
        }
    }

    private void burst(ServerLevel sl) {
        playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 0.7F);
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.0, getZ(), 1, 0, 0, 0, 0);
        for (int i = 0; i < 48; i++) {
            double ang = i * (Math.PI * 2.0D / 48.0D);
            for (double r = 2.0D; r <= 6.0D; r += 2.0D) {
                sl.sendParticles(ParticleTypes.CLOUD, getX() + Math.cos(ang) * r, getY() + 0.1,
                        getZ() + Math.sin(ang) * r, 1, 0.0, 0.05, 0.0, 0.02);
            }
        }
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 60, 3.0, 0.5, 3.0, 0.2);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0D),
                x -> x != this && !(x instanceof TreeSentinelEntity))) {
            e.hurt(damageSources().mobAttack(this), 22.0F);
            Vec3 push = e.position().subtract(position()).multiply(1, 0, 1).normalize();
            e.push(push.x * 1.4D, 0.8D, push.z * 1.4D);
            e.hurtMarked = true;
        }
    }

    private void applyEnrage() {
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(ENRAGE_UUID);
            speed.addPermanentModifier(new AttributeModifier(ENRAGE_UUID, "enrage", 0.06D,
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
