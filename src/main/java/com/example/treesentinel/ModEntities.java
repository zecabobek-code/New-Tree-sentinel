package com.example.treesentinel;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TreeSentinelMod.MODID);

    public static final RegistryObject<EntityType<TreeSentinelEntity>> TREE_SENTINEL = ENTITIES.register("tree_sentinel",
            () -> EntityType.Builder.of(TreeSentinelEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 3.8F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build("tree_sentinel"));
}
