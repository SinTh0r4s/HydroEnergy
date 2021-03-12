package com.sinthoras.hydroenergy.blocks;

import com.sinthoras.hydroenergy.server.HEBlockQueue;
import com.sinthoras.hydroenergy.server.HEServer;
import com.sinthoras.hydroenergy.client.renderer.HEWaterRenderer;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class HEWaterStill extends HEWater {

	public HEWaterStill(int id) {
		super(id);
		setHardness(100.0F);
		setLightOpacity(3);
		setBlockName("water");
		setTickRandomly(false);
	}
	
	@Override
    public int getRenderType() {
        return HEWaterRenderer.instance.getRenderId();
    }
	
	@Override
    public boolean shouldSideBeRendered(IBlockAccess world, int blockX, int blockY, int blockZ, int side) {
		Block block = world.getBlock(blockX, blockY, blockZ);
        if (block != this) {
            return !block.isOpaqueCube();
        }
        return false;
    }
	
	@Override
	public void onNeighborBlockChange(World world, int blockX, int blockY, int blockZ, Block block) {
		spread(world, blockX, blockY, blockZ);
    }

	@Override
	public void onBlockAdded(World world, int blockX, int blockY, int blockZ) {
		spread(world, blockX, blockY, blockZ);
	}

	private void spread(World world, int blockX, int blockY, int blockZ) {
		final int waterId = getWaterId();
		if(HEServer.instance.canSpread(waterId)) {
			if (blockY < HEServer.instance.getWaterLimitUp(waterId)) {
				HEBlockQueue.enqueueBlock(blockX, blockY + 1, blockZ, waterId);
			}

			if(blockY > HEServer.instance.getWaterLimitDown(waterId)) {
				HEBlockQueue.enqueueBlock(blockX, blockY - 1, blockZ, waterId);
			}

			if(blockX < HEServer.instance.getWaterLimitEast(waterId)) {
				HEBlockQueue.enqueueBlock(blockX + 1, blockY, blockZ, waterId);
			}

			if(blockX > HEServer.instance.getWaterLimitWest(waterId)) {
				HEBlockQueue.enqueueBlock(blockX - 1, blockY, blockZ, waterId);
			}

			if(blockZ < HEServer.instance.getWaterLimitSouth(waterId)) {
				HEBlockQueue.enqueueBlock(blockX, blockY, blockZ + 1, waterId);
			}

			if(blockZ > HEServer.instance.getWaterLimitNorth(waterId)) {
				HEBlockQueue.enqueueBlock(blockX, blockY, blockZ - 1, waterId);
			}
		}
		else if(!HEServer.instance.canSpread(waterId)
					|| HEServer.instance.isBlockOutOfBounds(waterId, blockX, blockY, blockZ)) {
			HEBlockQueue.enqueueBlock(blockX, blockY, blockZ, waterId);
		}
	}
}