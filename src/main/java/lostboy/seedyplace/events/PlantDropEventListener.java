package lostboy.seedyplace.events;

import lostboy.seedyplace.SeedyPlaceMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
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
 * active: this code turns oak saplings into diamonds after 4 seconds of tick time.
 * TODO: saplings will check if they are plantable
 * TODO: check to see if this causes performance issues over time
 */

public class PlantDropEventListener {

    public void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {


            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity) entity;

                if (itemEntity.getStack().getItem() == Items.OAK_SAPLING) {

                    ServerTickEvents.START_SERVER_TICK.register(server -> {

                        if (entity.age >= 20 * 4 && entity.isAlive()) {
                            entity.remove(Entity.RemovalReason.DISCARDED);
                            sTestSpawn(world, new Vec3d(entity.getX(), entity.getY(), entity.getZ()));
                        }
                    });



                }


            }
        }));
    }

    public void sTestSpawn(World world, Vec3d position) {

        ItemStack itemStack = new ItemStack(Items.DIAMOND, 1);
        ItemEntity itemEntity = new ItemEntity(world, position.x, position.y, position.z, itemStack);

        world.spawnEntity(itemEntity);

    }

}
