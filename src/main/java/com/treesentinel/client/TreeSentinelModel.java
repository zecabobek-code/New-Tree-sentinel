package com.treesentinel.client;

import com.treesentinel.common.SentinelAnimated;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Modelo do Tree Sentinel: cavaleiro dourado montado em cavalo blindado.
 * Gerado a partir da imagem de referencia. Todos os nomes de partes sao unicos
 * (necessario para as animacoes em {@link SentinelAnimations}).
 */
public class TreeSentinelModel<T extends Mob & SentinelAnimated> extends HierarchicalModel<T> {
    /** Troque se o modid do seu mod for outro. */
    public static final String MOD_ID = "treesentinel";
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(MOD_ID, "tree_sentinel"), "main");

    private final ModelPart root;
    private final ModelPart knightHead;
    private final ModelPart horseNeck;
    private final ModelPart horseHead;

    public TreeSentinelModel(ModelPart root) {
        super();
        this.root = root;
        this.knightHead = this.getAnyDescendantWithName("knight_head").orElseThrow();
        this.horseNeck = this.getAnyDescendantWithName("neck").orElseThrow();
        this.horseHead = this.getAnyDescendantWithName("head").orElseThrow();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_main = root.addOrReplaceChild("main", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition p_body = p_main.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-6.0F, -6.0F, -23.0F, 12.0F, 12.0F, 26.0F)
                .texOffs(159, 0).addBox(-6.0F, -6.0F, -22.0F, 12.0F, 6.0F, 24.0F, new CubeDeformation(0.8F))
                .texOffs(86, 0).addBox(-6.0F, 1.0F, -22.0F, 12.0F, 11.0F, 24.0F, new CubeDeformation(1.5F))
                .texOffs(115, 87).addBox(-5.0F, -8.0F, -14.0F, 10.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, -22.0F, 10.0F));
        PartDefinition p_front_right_leg = p_body.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(27, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                .texOffs(229, 69).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.7F))
                .texOffs(164, 101).addBox(-2.0F, 14.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(-3.5F, 6.0F, -19.0F));
        PartDefinition p_front_left_leg = p_body.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(44, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                .texOffs(0, 87).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.7F))
                .texOffs(181, 101).addBox(-2.0F, 14.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(3.5F, 6.0F, -19.0F));
        PartDefinition p_back_right_leg = p_main.addOrReplaceChild("back_right_leg", CubeListBuilder.create()
                .texOffs(61, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                .texOffs(17, 87).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.7F))
                .texOffs(198, 101).addBox(-2.0F, 14.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(-3.5F, -16.0F, 9.0F));
        PartDefinition p_back_left_leg = p_main.addOrReplaceChild("back_left_leg", CubeListBuilder.create()
                .texOffs(78, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F)
                .texOffs(34, 87).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.7F))
                .texOffs(215, 101).addBox(-2.0F, 14.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.3F)),
                PartPose.offset(3.5F, -16.0F, 9.0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-3.0F, -12.0F, -3.5F, 6.0F, 14.0F, 7.0F)
                .texOffs(95, 47).addBox(-3.0F, -12.0F, -3.5F, 6.0F, 12.0F, 7.0F, new CubeDeformation(0.8F))
                .texOffs(159, 69).addBox(-1.0F, -12.0F, 3.5F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, -22.0F, 0.55F, 0.0F, 0.0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(162, 47).addBox(-3.5F, -3.5F, -10.0F, 7.0F, 7.0F, 10.0F)
                .texOffs(103, 101).addBox(-3.0F, 0.0F, -14.0F, 6.0F, 4.0F, 4.0F)
                .texOffs(197, 47).addBox(-3.5F, -3.5F, -10.0F, 7.0F, 7.0F, 10.0F, new CubeDeformation(0.9F))
                .texOffs(232, 47).addBox(-7.0F, -3.5F, -9.0F, 2.0F, 8.0F, 9.0F)
                .texOffs(0, 69).addBox(5.0F, -3.5F, -9.0F, 2.0F, 8.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition p_ear_right = p_head.addOrReplaceChild("ear_right", CubeListBuilder.create()
                .texOffs(237, 101).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(-3.0F, -3.5F, -1.0F));
        PartDefinition p_ear_left = p_head.addOrReplaceChild("ear_left", CubeListBuilder.create()
                .texOffs(246, 101).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(3.0F, -3.5F, -1.0F));
        PartDefinition p_horn = p_head.addOrReplaceChild("horn", CubeListBuilder.create()
                .texOffs(63, 69).addBox(-1.0F, -1.0F, -14.0F, 2.0F, 2.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, -9.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(23, 69).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F)
                .texOffs(156, 87).addBox(-2.0F, 9.0F, -2.0F, 4.0F, 7.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, 0.8F, 0.0F, 0.0F));
        PartDefinition p_rider = p_body.addOrReplaceChild("rider", CubeListBuilder.create(),
                PartPose.offset(0.0F, -8.0F, -12.0F));
        PartDefinition p_torso = p_rider.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(36, 69).addBox(-4.0F, -12.0F, -2.5F, 8.0F, 12.0F, 5.0F)
                .texOffs(168, 69).addBox(-4.0F, -12.0F, -2.5F, 8.0F, 9.0F, 5.0F, new CubeDeformation(0.7F))
                .texOffs(63, 101).addBox(-4.0F, -3.0F, -2.5F, 8.0F, 4.0F, 5.0F, new CubeDeformation(0.9F))
                .texOffs(26, 112).addBox(-1.0F, -9.0F, -3.9F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition p_knight_head = p_torso.addOrReplaceChild("knight_head", CubeListBuilder.create()
                .texOffs(96, 69).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 8.0F, 7.0F)
                .texOffs(207, 87).addBox(-1.0F, -10.0F, -4.0F, 2.0F, 2.0F, 8.0F)
                .texOffs(124, 101).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition p_plume = p_knight_head.addOrReplaceChild("plume", CubeListBuilder.create()
                .texOffs(0, 112).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(51, 87).addBox(-3.5F, -9.0F, -3.5F, 7.0F, 6.0F, 7.0F)
                .texOffs(228, 87).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 5.0F, 5.0F)
                .texOffs(90, 101).addBox(-1.5F, -12.0F, -4.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.25F, 0.0F, 0.08F));
        PartDefinition p_right_arm = p_torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(125, 69).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                .texOffs(0, 101).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.6F))
                .texOffs(17, 101).addBox(-5.0F, -3.5F, -3.5F, 7.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(-5.5F, -10.0F, 0.0F, -0.7F, 0.0F, 0.0F));
        PartDefinition p_halberd = p_right_arm.addOrReplaceChild("halberd", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -26.0F, -1.0F, 2.0F, 44.0F, 2.0F)
                .texOffs(122, 47).addBox(-1.0F, -36.0F, -8.0F, 2.0F, 10.0F, 8.0F)
                .texOffs(232, 101).addBox(-0.5F, -41.0F, -0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(9, 112).addBox(-3.0F, -27.0F, -1.0F, 6.0F, 2.0F, 2.0F)
                .texOffs(149, 101).addBox(-1.0F, -33.0F, 0.0F, 2.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, -1.0F, 1.0F, 0.0F, 0.0F));
        PartDefinition p_left_arm = p_torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(142, 69).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                .texOffs(46, 101).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.6F))
                .texOffs(80, 87).addBox(-2.0F, -4.5F, -4.5F, 8.0F, 4.0F, 9.0F),
                PartPose.offsetAndRotation(5.5F, -10.0F, 0.0F, -0.5F, 0.0F, 0.05F));
        PartDefinition p_rider_right_leg = p_rider.addOrReplaceChild("rider_right_leg", CubeListBuilder.create()
                .texOffs(195, 69).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
                .texOffs(173, 87).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(-7.5F, -1.0F, -1.0F, -0.25F, 0.0F, 0.2F));
        PartDefinition p_rider_left_leg = p_rider.addOrReplaceChild("rider_left_leg", CubeListBuilder.create()
                .texOffs(212, 69).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F)
                .texOffs(190, 87).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(7.5F, -1.0F, -1.0F, -0.25F, 0.0F, -0.2F));
        PartDefinition p_cape = p_torso.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(143, 47).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 17.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 2.5F, 0.12F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float yaw = Mth.clamp(netHeadYaw, -45.0F, 45.0F) * ((float) Math.PI / 180.0F);
        float pitch = Mth.clamp(headPitch, -30.0F, 30.0F) * ((float) Math.PI / 180.0F);
        this.knightHead.yRot += yaw * 0.6F;
        this.knightHead.xRot += pitch * 0.5F;
        this.horseNeck.yRot += yaw * 0.3F;
        this.horseHead.xRot += pitch * 0.3F;

        this.animateWalk(SentinelAnimations.GALLOP, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(entity.getIdleState(), SentinelAnimations.IDLE, ageInTicks, 1.0F);
        this.animate(entity.getAttackState(), SentinelAnimations.ATTACK, ageInTicks, 1.0F);
        this.animate(entity.getRearState(), SentinelAnimations.REAR_SLAM, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
