package com.example.treesentinel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Cavaleiro dourado montado, modelado peça por peça, com animações de ataque. */
public class TreeSentinelModel extends EntityModel<TreeSentinelEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TreeSentinelMod.MODID, "tree_sentinel"), "main");

    private final ModelPart root, body, legFR, legFL, legBR, legBL, neck, head, tail,
            rider, rhead, rarm, larm, weapon, shield;

    public TreeSentinelModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.legFR = body.getChild("legFR");
        this.legFL = body.getChild("legFL");
        this.legBR = body.getChild("legBR");
        this.legBL = body.getChild("legBL");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.tail = body.getChild("tail");
        this.rider = body.getChild("rider");
        this.rhead = rider.getChild("rhead");
        this.rarm = rider.getChild("rarm");
        this.larm = rider.getChild("larm");
        this.weapon = rarm.getChild("weapon");
        this.shield = larm.getChild("shield");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-6F, -6F, -13F, 12F, 12F, 26F)
                .texOffs(76, 40).addBox(-7F, -8F, -11F, 14F, 7F, 22F)
                .texOffs(148, 40).addBox(-7F, -1F, -11F, 14F, 4F, 22F)
                .texOffs(0, 40).addBox(-7F, 3F, -12F, 14F, 6F, 24F)
                .texOffs(60, 93).addBox(-5F, -5F, -16F, 10F, 10F, 3F)
                .texOffs(86, 93).addBox(-6F, -6F, 13F, 12F, 10F, 3F),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));
        PartDefinition legFR = body.addOrReplaceChild("legFR", CubeListBuilder.create()
                .texOffs(142, 70).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(116, 93).addBox(-3F, 0F, -3F, 6F, 7F, 6F)
                .texOffs(168, 109).addBox(-3F, 12F, -3F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-4F, 6F, -9F, 0F, 0F, 0F));
        PartDefinition legFL = body.addOrReplaceChild("legFL", CubeListBuilder.create()
                .texOffs(158, 70).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(140, 93).addBox(-3F, 0F, -3F, 6F, 7F, 6F)
                .texOffs(192, 109).addBox(-3F, 12F, -3F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(4F, 6F, -9F, 0F, 0F, 0F));
        PartDefinition legBR = body.addOrReplaceChild("legBR", CubeListBuilder.create()
                .texOffs(174, 70).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(164, 93).addBox(-3F, 0F, -3F, 6F, 7F, 6F)
                .texOffs(216, 109).addBox(-3F, 12F, -3F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(-4F, 6F, 9F, 0F, 0F, 0F));
        PartDefinition legBL = body.addOrReplaceChild("legBL", CubeListBuilder.create()
                .texOffs(190, 70).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(188, 93).addBox(-3F, 0F, -3F, 6F, 7F, 6F)
                .texOffs(0, 122).addBox(-3F, 12F, -3F, 6F, 2F, 6F),
                PartPose.offsetAndRotation(4F, 6F, 9F, 0F, 0F, 0F));
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(22, 70).addBox(-4F, -11F, -5F, 8F, 13F, 9F),
                PartPose.offsetAndRotation(0F, -5F, -11F, 0F, 0F, 0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(56, 70).addBox(-3F, -4F, -12F, 6F, 7F, 14F)
                .texOffs(64, 109).addBox(-1F, -7F, -14F, 2F, 2F, 10F)
                .texOffs(40, 122).addBox(-3F, -7F, -1F, 2F, 3F, 2F)
                .texOffs(48, 122).addBox(1F, -7F, -1F, 2F, 3F, 2F),
                PartPose.offsetAndRotation(0F, -9F, -3F, 0.5F, 0F, 0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(128, 70).addBox(-2F, 0F, 0F, 4F, 14F, 3F),
                PartPose.offsetAndRotation(0F, -4F, 13F, 0.35F, 0F, 0F));
        PartDefinition rider = body.addOrReplaceChild("rider", CubeListBuilder.create()
                .texOffs(96, 70).addBox(-5F, -14F, -3F, 10F, 14F, 6F)
                .texOffs(212, 93).addBox(-6F, -1F, -4F, 12F, 5F, 8F)
                .texOffs(0, 70).addBox(-5F, -13F, 3F, 10F, 22F, 1F),
                PartPose.offsetAndRotation(0F, -6F, 3F, 0F, 0F, 0F));
        PartDefinition rhead = rider.addOrReplaceChild("rhead", CubeListBuilder.create()
                .texOffs(206, 70).addBox(-4F, -8F, -4F, 8F, 8F, 8F)
                .texOffs(36, 93).addBox(-3F, -16F, -3F, 6F, 8F, 6F)
                .texOffs(24, 122).addBox(-2F, -20F, -2F, 4F, 4F, 4F)
                .texOffs(88, 109).addBox(-2F, -14F, 3F, 4F, 8F, 4F),
                PartPose.offsetAndRotation(0F, -14F, 0F, 0F, 0F, 0F));
        PartDefinition rarm = rider.addOrReplaceChild("rarm", CubeListBuilder.create()
                .texOffs(238, 70).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(0, 109).addBox(-4F, -3F, -4F, 8F, 5F, 8F)
                .texOffs(104, 109).addBox(-3F, 5F, -3F, 6F, 6F, 6F),
                PartPose.offsetAndRotation(-7F, -12F, 0F, -0.9F, 0F, 0F));
        PartDefinition weapon = rarm.addOrReplaceChild("weapon", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -1F, -26F, 2F, 2F, 38F)
                .texOffs(220, 40).addBox(-1F, -9F, -32F, 2F, 14F, 10F)
                .texOffs(152, 109).addBox(-1F, -2F, -38F, 2F, 3F, 6F),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0.75F, 0F, 0F));
        PartDefinition larm = rider.addOrReplaceChild("larm", CubeListBuilder.create()
                .texOffs(0, 93).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(32, 109).addBox(-4F, -3F, -4F, 8F, 5F, 8F)
                .texOffs(128, 109).addBox(-3F, 5F, -3F, 6F, 6F, 6F),
                PartPose.offsetAndRotation(7F, -12F, 0F, -0.5F, 0F, 0F));
        PartDefinition shield = larm.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(156, 0).addBox(3F, -12F, -9F, 3F, 20F, 18F)
                .texOffs(16, 93).addBox(6F, -4F, -4F, 2F, 8F, 8F),
                PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static float lerp(float a, float b, float k) {
        return a + (b - a) * Mth.clamp(k, 0.0F, 1.0F);
    }

    @Override
    public void setupAnim(TreeSentinelEntity e, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        int state = e.getAttackState();
        float t = e.animTime + Minecraft.getInstance().getPartialTick();
        float rad = (float) Math.PI / 180.0F;

        // O cavaleiro olha para o alvo
        rhead.yRot += netHeadYaw * rad * 0.6F;
        rhead.xRot += headPitch * rad * 0.6F;

        // Respiração e cauda
        head.xRot += Mth.sin(ageInTicks * 0.08F) * 0.04F;
        tail.xRot += Mth.sin(ageInTicks * 0.1F) * 0.1F;
        rider.xRot += Mth.sin(ageInTicks * 0.07F) * 0.02F;

        if (state == 1) {
            // GALOPE
            float g = ageInTicks * 0.95F;
            legFR.xRot += Mth.cos(g) * 1.1F;
            legFL.xRot += Mth.cos(g + 0.3F) * 1.1F;
            legBR.xRot += Mth.cos(g + 1.5F) * 1.1F;
            legBL.xRot += Mth.cos(g + 1.8F) * 1.1F;
            body.xRot += 0.08F;
            body.y += Mth.cos(g * 2.0F) * 0.9F;
            head.xRot += 0.25F;
            rider.xRot += 0.35F;
            rarm.xRot -= 0.6F;
            larm.xRot -= 0.3F;
            tail.xRot += 0.5F;

        } else if (state == 2) {
            // GOLPE DE ALABARDA (14 ticks): levanta, gira e corta em arco
            float p = t / 14.0F;
            if (p < 0.5F) {
                float k = p / 0.5F;
                rarm.xRot = lerp(-0.9F, -2.7F, k);
                rarm.yRot = lerp(0.0F, 0.9F, k);
                rider.yRot = lerp(0.0F, 0.6F, k);
            } else {
                float k = (p - 0.5F) / 0.3F;
                rarm.xRot = lerp(-2.7F, -1.4F, k);
                rarm.yRot = lerp(0.9F, -1.3F, k);
                rider.yRot = lerp(0.6F, -0.7F, k);
            }
            head.xRot += 0.15F;

        } else if (state == 3) {
            // EXPLOSÃO DOURADA (20 ticks): cavalo empina, braços sobem e martelam
            float p = t / 20.0F;
            float rear;
            float arms;
            if (p < 0.6F) {
                rear = lerp(0.0F, -0.75F, p / 0.6F);
                arms = lerp(-0.9F, -3.0F, p / 0.6F);
            } else {
                rear = lerp(-0.75F, 0.1F, (p - 0.6F) / 0.15F);
                arms = lerp(-3.0F, -0.4F, (p - 0.6F) / 0.15F);
            }
            body.xRot += rear;
            legBR.xRot -= rear;
            legBL.xRot -= rear;
            float paw = p < 0.6F ? -0.9F + Mth.sin(ageInTicks * 1.2F) * 0.3F : 0.0F;
            legFR.xRot += paw;
            legFL.xRot += paw;
            rider.xRot -= rear * 0.5F;
            rarm.xRot = arms;
            larm.xRot = arms;
            head.xRot += p < 0.6F ? -0.3F : 0.2F;

        } else {
            // Parado ou andando
            float s = limbSwing * 0.6662F;
            float a = limbSwingAmount * 1.1F;
            legFR.xRot += Mth.cos(s) * a;
            legBL.xRot += Mth.cos(s) * a;
            legFL.xRot += Mth.cos(s + (float) Math.PI) * a;
            legBR.xRot += Mth.cos(s + (float) Math.PI) * a;
            rider.xRot += Mth.cos(s * 2.0F) * 0.04F * limbSwingAmount;
            body.y -= Mth.abs(Mth.cos(s)) * limbSwingAmount * 0.8F;
        }

        if (e.isEnragedClient()) {
            float shake = Mth.sin(ageInTicks * 1.3F) * 0.06F;
            rarm.zRot += shake;
            larm.zRot -= shake;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
