package lostboy.seedyplace.events;

import lostboy.seedyplace.SeedyPlaceMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;


import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gunnar Jessee 7/1/23
 * active: this code turns oak saplings into planted oak saplings after 4 seconds of tick time.
 * TODO: generalize the planting process and do all plants
 * TODO: check to see if this causes performance issues over time
 * TODO: fix item stack issues with their items combining to one item stack then the entire stack get destroyed
 */

public class PlantDropEventListener {

    public void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {


            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity) entity;

                if (isSapling(itemEntity)) {

                    ServerTickEvents.START_SERVER_TICK.register(server -> {

                        if (entity.age >= 20 * 4 && entity.isAlive()) {
                            Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
                            if (canPlantSapling(world, entityPos)) {
                                // Plant the sapling
                                entity.remove(Entity.RemovalReason.DISCARDED);

                                world.setBlockState(new BlockPos(entityPos), getBlock(itemEntity.getStack()).getDefaultState());
                            }

                        }
                    });



                }


            }
        }));
    }

    private Block getBlock(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) itemStack.getItem();
            return blockItem.getBlock();
        }
        return null;
    }

    // this requires dirt or grass block
    private boolean isSapling(ItemEntity itemEntity) {

        Item[] saplings = {Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.DARK_OAK_SAPLING, Items.ACACIA_SAPLING, Items.SPRUCE_SAPLING, Items.MANGROVE_PROPAGULE};
        for (Item sapling: saplings) {
            if (itemEntity.getStack().getItem() == sapling){
                return true;
            }
        }

        return false;

    }

    private boolean isPlant(ItemEntity itemEntity) {

        Item[] crops = {Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS};
        for (Item crop: crops) {
            if (itemEntity.getStack().getItem() == crop) {
                return true;
            }
        }
        return false;
    }

    private boolean canPlantSapling(World world, Vec3d position) {
        BlockPos blockPos = new BlockPos(position.x, position.y, position.z);
        BlockState groundState = world.getBlockState(blockPos.down());
        Block groundBlock = groundState.getBlock();

        if (groundBlock == Blocks.GRASS_BLOCK || groundBlock == Blocks.DIRT) {
            // Ensure the block above is air for sapling placement
            return world.isAir(blockPos);
        }

        return false;
    }

}
