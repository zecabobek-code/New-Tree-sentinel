package com.treesentinel.client;

import com.treesentinel.common.SentinelAnimated;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** Brilho dourado: olhos do elmo, gema vermelha do peito e o penacho (textura tree_sentinel_glow.png). */
public class SentinelGlowLayer<T extends Mob & SentinelAnimated> extends EyesLayer<T, TreeSentinelModel<T>> {
    private static final RenderType GLOW = RenderType.eyes(
            new ResourceLocation(TreeSentinelModel.MOD_ID, "textures/entity/tree_sentinel_glow.png"));

    public SentinelGlowLayer(RenderLayerParent<T, TreeSentinelModel<T>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
