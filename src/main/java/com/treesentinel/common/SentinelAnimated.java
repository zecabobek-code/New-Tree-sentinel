package com.treesentinel.common;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;

/**
 * Implemente na sua entidade do Tree Sentinel. Guarda os 3 estados de animacao e trata os
 * eventos de entidade que o servidor envia (ataque e empinada).
 */
public interface SentinelAnimated {
    byte EVENT_ATTACK = 4;
    byte EVENT_REAR = 61;

    AnimationState getIdleState();
    AnimationState getAttackState();
    AnimationState getRearState();

    /** Chame dentro de handleEntityEvent. Retorna true se o evento era do Sentinel. */
    default boolean handleSentinelEvent(Entity self, byte id) {
        if (id == EVENT_ATTACK) {
            getAttackState().start(self.tickCount);
            return true;
        }
        if (id == EVENT_REAR) {
            getAttackState().stop();
            getRearState().start(self.tickCount);
            return true;
        }
        return false;
    }

    /** Chame dentro de tick(). So faz algo no cliente. */
    default void tickSentinelClient(Entity self) {
        if (self.level().isClientSide) {
            getIdleState().startIfStopped(self.tickCount);
        }
    }
}
