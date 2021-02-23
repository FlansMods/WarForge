package com.flansmod.warforge.common.blocks;

import java.util.List;
import java.util.UUID;

import com.flansmod.warforge.common.WarForgeMod;
import com.flansmod.warforge.server.Faction;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

public class TileEntityCitadel extends TileEntityYieldCollector implements ISiegeable
{
	public static final int BANNER_SLOT_INDEX = NUM_BASE_SLOTS;
	public static final int NUM_SLOTS = NUM_BASE_SLOTS + 1;
	
	private UUID mPlacer = Faction.NULL;	
	public UUID GetPlacer() { return mPlacer; }
	
	// The banner stack is an optional slot that sets all banners in owned chunks to copy the design
	protected ItemStack mBannerStack;
	
	public TileEntityCitadel()
	{
		mBannerStack = ItemStack.EMPTY;
	}
	
	public void OnPlacedBy(EntityLivingBase placer) 
	{
		// This locks in the placer as the only person who can create a faction using the interface on this citadel
		mPlacer = placer.getUniqueID();
	}
	
	// ISiegeable
	@Override
	public TileEntity GetAsTileEntity() { return this; }
	@Override
	public int GetStrengthRequiredToSiege() { return WarForgeMod.DENSE_DIAMOND_CELL_SIZE; } // TODO: Config
	//-----------
	
	
	// IInventory Overrides for banner stack
	@Override
	public int getSizeInventory() { return NUM_SLOTS; }
	@Override
	public boolean isEmpty() 
	{
		return super.isEmpty() && mBannerStack.isEmpty();
	}
	@Override
	public ItemStack getStackInSlot(int index) 
	{
		if(index == BANNER_SLOT_INDEX)
			return mBannerStack;
		return super.getStackInSlot(index);
	}
	@Override
	public ItemStack decrStackSize(int index, int count) 
	{
		if(index == BANNER_SLOT_INDEX)
		{
			int numToTake = Math.max(count, mBannerStack.getCount());
			ItemStack result = mBannerStack.copy();
			result.setCount(numToTake);
			mBannerStack.setCount(mBannerStack.getCount() - numToTake);
			return result;
		}
		return super.decrStackSize(index, count);
	}
	@Override
	public ItemStack removeStackFromSlot(int index) 
	{
		if(index == BANNER_SLOT_INDEX)
		{
			ItemStack result = mBannerStack;
			mBannerStack = ItemStack.EMPTY;		
			return result;
		}
		return super.removeStackFromSlot(index);
	}
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) 
	{
		if(index == BANNER_SLOT_INDEX)
			mBannerStack = stack;
		else 
			super.setInventorySlotContents(index, stack);
	}
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) 
	{
		if(index == BANNER_SLOT_INDEX)
		{
			return stack.getItem() instanceof ItemBanner || stack.getItem() instanceof ItemShield;
		}
		return super.isItemValidForSlot(index, stack);
	}
	@Override
	public void clear() 
	{ 
		super.clear();
		mBannerStack = ItemStack.EMPTY;
	}
	
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setUniqueId("placer", mPlacer);
		
		NBTTagCompound bannerStackTags = new NBTTagCompound();
		mBannerStack.writeToNBT(bannerStackTags);
		nbt.setTag("banner", bannerStackTags);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		mBannerStack = new ItemStack(nbt.getCompoundTag("banner"));
		mPlacer = nbt.getUniqueId("placer");
	}
}
