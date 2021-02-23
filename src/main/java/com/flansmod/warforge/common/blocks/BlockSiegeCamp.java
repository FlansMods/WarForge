package com.flansmod.warforge.common.blocks;

import java.util.UUID;

import com.flansmod.warforge.common.CommonProxy;
import com.flansmod.warforge.common.DimChunkPos;
import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSiegeCamp extends Block implements ITileEntityProvider
{
	public BlockSiegeCamp(Material materialIn) 
	{
		super(materialIn);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntitySiegeCamp();
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		// Can't claim a chunk claimed by another faction
		UUID existingClaim = WarForgeMod.INSTANCE.GetClaim(new DimChunkPos(world.provider.getDimension(), pos));
		if(!existingClaim.equals(Faction.NULL))
			return false;
		
		// TODO: Verify there is a valid attack place
		
		// Can only place on a solid surface
		if(!world.getBlockState(pos.add(0, -1, 0)).isSideSolid(world, pos.add(0, -1, 0), EnumFacing.UP))
			return false;
		
		return true;
	}
	
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
		TileEntity te = world.getTileEntity(pos);
		if(te != null)
		{
			TileEntitySiegeCamp citadel = (TileEntitySiegeCamp)te;
			citadel.OnPlacedBy(placer);
		}
    }

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9)
	{
		if(player.isSneaking())
			return false;
		if(!world.isRemote)
			player.openGui(WarForgeMod.INSTANCE, CommonProxy.GUI_TYPE_SIEGE_CAMP, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
}