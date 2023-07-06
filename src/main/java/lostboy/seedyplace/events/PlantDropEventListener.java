package lostboy.seedyplace.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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

    private int tickTimer = 20 * 4;

    public void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {


            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity) entity;
                int itemCount = itemEntity.getStack().getCount();

                // checks to see if itemEntity is a valid sapling
                if (isSapling(itemEntity) && itemCount == 1) {
                    ServerTickEvents.START_SERVER_TICK.register(server -> {
                        if (itemEntity.age >= tickTimer && itemEntity.isAlive() && itemEntity.getStack().getCount() > 0) {
                            BlockPos blockPos = new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ());

                            if (canPlantSapling(world, blockPos)) {
                                // Plant the sapling
                                itemEntity.remove(Entity.RemovalReason.DISCARDED);
                                world.setBlockState(blockPos, getBlock(itemEntity.getStack()).getDefaultState());
                            }
                        }
                    });
                }

                // checks to see if itemEntity is a valid crop
                if (isCrop(itemEntity) && itemCount == 1) {
                    ServerTickEvents.START_SERVER_TICK.register(server -> {
                        if (itemEntity.age >= tickTimer && itemEntity.isAlive()) {
                            BlockPos blockPos = new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ());

                            if (canPlantCrop(world, blockPos)) {
                                // Plant the sapling
                                itemEntity.remove(Entity.RemovalReason.DISCARDED);
                                world.setBlockState(blockPos.up(), getBlock(itemEntity.getStack()).getDefaultState(), 3);
                            }
                        }
                    });
                }

                // Cactus and sugarcane needs their own special handling
                if (getBlock(itemEntity.getStack()) == Blocks.CACTUS && itemCount == 1) {
                    ServerTickEvents.START_SERVER_TICK.register(server -> {
                        if (itemEntity.age >= tickTimer && itemEntity.isAlive()) {
                            BlockPos blockPos = new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ());
                            if (canPlantCactus(world, blockPos)) {
                                itemEntity.remove(Entity.RemovalReason.DISCARDED);
                                world.setBlockState(blockPos, getBlock(itemEntity.getStack()).getDefaultState());
                            }
                        }
                    });
                }

                if (itemEntity.getStack().getItem() == Items.SUGAR_CANE && itemCount == 1) {
                    ServerTickEvents.START_SERVER_TICK.register(server -> {
                        if (itemEntity.age >= tickTimer && itemEntity.isAlive()) {
                            BlockPos blockPos = new BlockPos(itemEntity.getBlockX(), itemEntity.getBlockY(), itemEntity.getBlockZ());
                            if (canPlaceSugarCane(world, blockPos)) {
                                itemEntity.remove(Entity.RemovalReason.DISCARDED);
                                world.setBlockState(blockPos, getBlock(itemEntity.getStack()).getDefaultState());
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
        Item[] saplings = {Items.OAK_SAPLING, Items.DARK_OAK_SAPLING, Items.ACACIA_SAPLING, Items.SPRUCE_SAPLING, Items.MANGROVE_PROPAGULE,
                           Items.JUNGLE_SAPLING, Items.CHERRY_SAPLING, Items.BAMBOO, Items.WITHER_ROSE, Items.BIRCH_SAPLING};
        for (Item sapling: saplings) {
            if (itemEntity.getStack().getItem() == sapling){
                return true;
            }
        }
        return false;
    }

    // Checks to see if water is a neighbor of the base block
    private boolean hasWaterNeighbor(World world, BlockPos blockPos) {
        BlockPos groundBlock = blockPos.down();
        boolean north = world.getBlockState(groundBlock.north()).getFluidState().getFluid() == Fluids.WATER? true : false;
        boolean south = world.getBlockState(groundBlock.south()).getFluidState().getFluid() == Fluids.WATER? true : false;
        boolean east = world.getBlockState(groundBlock.east()).getFluidState().getFluid() == Fluids.WATER? true : false;
        boolean west = world.getBlockState(groundBlock.west()).getFluidState().getFluid() == Fluids.WATER? true : false;
        if (north || west || south || east) {
            return true;
        }
        return false;
    }

    private boolean canPlaceSugarCane(World world, BlockPos blockPos) {
        BlockState groundState = world.getBlockState(blockPos.down());
        Block ground = groundState.getBlock();
        if (hasWaterNeighbor(world, blockPos)) {
            if (ground == Blocks.DIRT || ground == Blocks.GRASS_BLOCK || ground == Blocks.SAND) {
                return world.isAir(blockPos);
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

    // Checks to see if sand able to plant in the spot of the entity
    private boolean canPlantCactus(World world, BlockPos blockPos) {
        BlockState groundState = world.getBlockState(blockPos.down());
        Block groundBlock = groundState.getBlock();
        if (groundBlock == Blocks.SAND && isNeighborsEmpty(world, blockPos)) {
            return world.isAir(blockPos);
        }
        return false;
    }

    // Only used for cactus
    private boolean isNeighborsEmpty(World world, BlockPos blockPos) {
        boolean north = world.getBlockState(blockPos.north()).isAir();
        boolean south = world.getBlockState(blockPos.south()).isAir();
        boolean east = world.getBlockState(blockPos.east()).isAir();
        boolean west = world.getBlockState(blockPos.west()).isAir();
        if (north && south && west && east) {
            return true;
        }
        return false;

    }

    private boolean canPlantSapling(World world, BlockPos blockPos) {
        BlockState groundState = world.getBlockState(blockPos.down());
        Block groundBlock = groundState.getBlock();

        boolean isGrass = (groundBlock == Blocks.GRASS_BLOCK) ? true : false;
        boolean isPODZOL = (groundBlock == Blocks.PODZOL) ? true : false;
        boolean isMycelium = (groundBlock == Blocks.MYCELIUM) ? true : false;
        boolean isRootedDirt = (groundBlock == Blocks.ROOTED_DIRT) ? true : false;
        boolean isDirt = (groundBlock == Blocks.DIRT) ? true : false;
        boolean isMud = (groundBlock == Blocks.MUD) ? true : false;
        boolean isCourseDirt = (groundBlock == Blocks.COARSE_DIRT) ? true : false;

        if (isGrass || isPODZOL || isMud || isMycelium || isRootedDirt || isDirt || isCourseDirt) {
            // Ensure the block above is air for sapling placement
            return world.isAir(blockPos);
        }

        return false;
    }

}
