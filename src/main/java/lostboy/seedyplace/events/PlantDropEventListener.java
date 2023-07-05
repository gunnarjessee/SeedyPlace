package lostboy.seedyplace.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

/**
 * Gunnar Jessee 7/1/23
 * TODO: check to see if this causes performance issues over time
 * TODO: fix item stack issues with their items combining to one item stack then the entire stack get destroyed
 */

public class PlantDropEventListener {

    /*
    BUG: if player picks up item, it still tries to attempt to plant it even tho the item stack is empty
     */

    private int tickTime = 4 * 20;

    public void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {


            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity) entity;
                int itemCount = itemEntity.getStack().getCount();

                // checks to see if itemEntity is a valid sapling
                if (isSapling(itemEntity) && itemCount == 1) {
                    ServerTickEvents.START_SERVER_TICK.register(server -> {
                        if (entity.age >= tickTime && entity.isAlive() && itemEntity.getStack().getCount() > 0) {
                            BlockPos blockPos = new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ());
                            if (canPlantSapling(world, blockPos)) {
                                // Plant the sapling
                                entity.remove(Entity.RemovalReason.DISCARDED);
                                world.setBlockState(blockPos, getBlock(itemEntity.getStack()).getDefaultState());
                            }
                        }
                    });
                }

                // checks to see if itemEntity is a valid crop
                if (isCrop(itemEntity) && itemCount == 1) {
                    ServerTickEvents.START_SERVER_TICK.register(server -> {
                        if (entity.age >= tickTime && entity.isAlive()) {
                            BlockPos blockPos = new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ());
                            if (canPlantCrop(world, blockPos)) {
                                // Plant the sapling
                                entity.remove(Entity.RemovalReason.DISCARDED);
                                world.setBlockState(blockPos.up(), getBlock(itemEntity.getStack()).getDefaultState(), 3);
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
        Item[] saplings = {Items.OAK_SAPLING, Items.DARK_OAK_SAPLING, Items.ACACIA_SAPLING, Items.SPRUCE_SAPLING, Items.MANGROVE_PROPAGULE, Items.JUNGLE_SAPLING};
        for (Item sapling: saplings) {
            if (itemEntity.getStack().getItem() == sapling){
                return true;
            }
        }
        return false;
    }

    // checks to see if itemEntity is a valid crop
    private boolean isCrop(ItemEntity itemEntity) {

        Item[] crops = {Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS};
        for (Item crop: crops) {
            if (itemEntity.getStack().getItem() == crop) {
                return true;
            }
        }
        return false;
    }

    private boolean canPlantCrop(World world, BlockPos blockPos) {
        BlockState groundState = world.getBlockState(blockPos);
        Block groundBlock = groundState.getBlock();

        if (groundBlock == Blocks.FARMLAND ) {
            return world.isAir(blockPos.up());
        }

        return false;
    }

    private boolean canPlantSapling(World world, BlockPos blockPos) {
        BlockState groundState = world.getBlockState(blockPos.down());
        Block groundBlock = groundState.getBlock();

        if (groundBlock == Blocks.GRASS_BLOCK || groundBlock == Blocks.DIRT) {
            // Ensure the block above is air for sapling placement
            return world.isAir(blockPos);
        }

        return false;
    }

}
