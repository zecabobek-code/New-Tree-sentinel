package com.example.treesentinel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class TreeSentinelRenderer extends MobRenderer<TreeSentinelEntity, TreeSentinelModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TreeSentinelMod.MODID, "textures/entity/tree_sentinel.png");
    private static final ResourceLocation GLOW =
            new ResourceLocation(TreeSentinelMod.MODID, "textures/entity/tree_sentinel_glow.png");

    public TreeSentinelRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TreeSentinelModel(ctx.bakeLayer(TreeSentinelModel.LAYER)), 1.5F);
        this.addLayer(new EyesLayer<TreeSentinelEntity, TreeSentinelModel>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(GLOW);
            }
        });
    }

    @Override
    protected void scale(TreeSentinelEntity e, PoseStack stack, float partial) {
        stack.scale(1.15F, 1.15F, 1.15F);
    }

    @Override
    public ResourceLocation getTextureLocation(TreeSentinelEntity e) {
        return TEXTURE;
    }
}
