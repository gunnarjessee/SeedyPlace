package lostboy.seedyplace.events;

import lostboy.seedyplace.SeedyPlaceMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;


import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gunnar Jessee 7/1/23
 * 
 * active: at the current moment this code is not functional due to 
 * ScheduleExecutorService class not functional in the minecraft runtime. 
 */

public class PlantDropEventListener {
    // ScheduledExecutorService to be remove and replaced.
    // TODO: Add a time checked when a entity loads into the world and see if its plantable. 
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    public void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {
            if (entity instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) entity;
                ItemStack itemStack = item.getStack();
                ServerScheduler serverScheduler = ServerScheduler.INSTANCE;
                if (itemStack.getItem() == Items.OAK_SAPLING) {
                    executorService.schedule(() -> {

                        double x = item.getX();
                        double y = item.getY();
                        double z = item.getZ();
                        int blockX = (int) Math.floor(x);
                        int blockY = (int) Math.floor(y) - 1; // Check the block below the item
                        int blockZ = (int) Math.floor(z);

                        Block block = world.getBlockState(new BlockPos(blockX, blockY + 1, blockZ)).getBlock();
                        SeedyPlaceMod.LOGGER.debug("Attempting to Place Sampling");
                        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) {
                            // Place the sapling on the ground
                            world.setBlockState(new BlockPos(blockX, blockY, blockZ), Blocks.OAK_SAPLING.getDefaultState());

                            // Remove the item entity
                            item.remove(Entity.RemovalReason.DISCARDED);
                        }

                    }, 1, TimeUnit.SECONDS);

                }

            }
        }));
    }
}
