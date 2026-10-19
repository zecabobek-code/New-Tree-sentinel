package com.treesentinel.common;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

/**
 * Implemente na entidade do Tree Sentinel. Guarda os estados de animacao do modelo novo
 * (os nomes dos metodos nao colidem com o getAttackState() int que a entidade ja tem).
 */
public interface SentinelAnimated {
    /** Evento enviado pelo servidor a cada golpe do ataque basico (SentinelMeleeGoal). */
    byte EVENT_ATTACK = 4;
    byte EVENT_REAR = 61;
    /** Evento vanilla de "levou dano". */
    byte EVENT_HURT = 2;

    AnimationState idleAnimation();
    AnimationState meleeAnimation();
    AnimationState attackAnimation();
    AnimationState chargeWindupAnimation();
    AnimationState chargeAnimation();
    AnimationState rearAnimation();
    AnimationState hurtAnimation();
    AnimationState enragedAnimation();

    /** Fase furiosa (no cliente). */
    default boolean isSentinelEnraged() {
        return false;
    }

    /** Chame dentro de handleEntityEvent ANTES do super. Retorna true se o evento foi consumido. */
    default boolean handleSentinelEvent(Entity self, byte id) {
        if (id == EVENT_ATTACK) {
            meleeAnimation().start(self.tickCount);
            return true;
        }
        if (id == EVENT_HURT) {
            hurtAnimation().start(self.tickCount);   // o super ainda toca o som/efeito de dano
        }
        return false;
    }

    /** Chame dentro de tick() no cliente. */
    default void tickSentinelClient(Entity self) {
        if (!self.level().isClientSide) return;
        idleAnimation().startIfStopped(self.tickCount);
        if (isSentinelEnraged()) enragedAnimation().startIfStopped(self.tickCount);
        else enragedAnimation().stop();
    }
}
