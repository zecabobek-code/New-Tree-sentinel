package com.example.treesentinel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TreeSentinelMod.MODID);

    public static final RegistryObject<Item> SENTINEL_HALBERD = ITEMS.register("sentinel_halberd",
            () -> new SwordItem(Tiers.NETHERITE, 12, -2.9F,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> TREE_SENTINEL_SPAWN_EGG = ITEMS.register("tree_sentinel_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TREE_SENTINEL, 0xC9A227, 0x3B2A10, new Item.Properties()));
}
