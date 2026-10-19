package com.treesentinel.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.treesentinel.common.SentinelAnimated;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class TreeSentinelRenderer<T extends Mob & SentinelAnimated> extends MobRenderer<T, TreeSentinelModel<T>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TreeSentinelModel.MOD_ID, "textures/entity/tree_sentinel.png");

    /** Escala visual do modelo (o modelo tem ~4 blocos de altura com o penacho a 1.0). */
    private static final float SCALE = 1.1F;

    public TreeSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new TreeSentinelModel<>(context.bakeLayer(TreeSentinelModel.LAYER)), 1.5F);
        this.addLayer(new SentinelGlowLayer<>(this));
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
