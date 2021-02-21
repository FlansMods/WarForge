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

public class TileEntityCitadel extends TileEntity implements IInventory
{
	public static final int NUM_YIELD_STACKS = 9;
	public static final int BANNER_SLOT_INDEX = 9;
	public static final int NUM_SLOTS = BANNER_SLOT_INDEX + 1;
	
	private UUID mPlacer = Faction.NULL;
	private UUID mFactionUUID = Faction.NULL;
	// The banner stack is an optional slot that sets all banners in owned chunks to copy the design
	private ItemStack mBannerStack;
	// The yield stacks are where items arrive when your faction is above a deposit
	private ItemStack[] mYieldStacks = new ItemStack[NUM_YIELD_STACKS];
	
	public UUID GetPlacer() { return mPlacer; }
	public UUID GetFactionID() { return mFactionUUID; }
	
	public TileEntityCitadel()
	{
		mBannerStack = ItemStack.EMPTY;
		for(int i = 0; i < NUM_YIELD_STACKS; i++)
		{
			mYieldStacks[i] = ItemStack.EMPTY;
		}
	}
	
	public void OnPlacedBy(EntityLivingBase placer) 
	{
		// This locks in the placer as the only person who can create a faction using the interface on this citadel
		mPlacer = placer.getUniqueID();
	}
	
	public void SetFaction(UUID factionID)
	{
		mFactionUUID = factionID;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		
		nbt.setUniqueId("faction", mFactionUUID);
		
		// Write all our stacks out
		NBTTagCompound bannerStackTags = new NBTTagCompound();
		mBannerStack.writeToNBT(bannerStackTags);
		nbt.setTag("banner", bannerStackTags);
		
		for(int i = 0; i < NUM_YIELD_STACKS; i++)
		{
			NBTTagCompound yieldStackTags = new NBTTagCompound();
			mYieldStacks[i].writeToNBT(yieldStackTags);
			nbt.setTag("yield_" + i, yieldStackTags);
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		
		mFactionUUID = nbt.getUniqueId("faction");
		Faction faction = WarForgeMod.INSTANCE.GetFaction(mFactionUUID);
		if(faction == null)
		{
			WarForgeMod.logger.error("Faction " + mFactionUUID + " could not be found for citadel at " + pos);
		}
		
		// Read inventory, or as much as we can find
		mBannerStack = new ItemStack(nbt.getCompoundTag("banner"));
		for(int i = 0; i < NUM_YIELD_STACKS; i++)
		{
			if(nbt.hasKey("yield_" + i))
				mYieldStacks[i] = new ItemStack(nbt.getCompoundTag("yield_" + i));
			else 
				mYieldStacks[i] = ItemStack.EMPTY;
		}
	}
	
	// ----------------------------------------------------------
	// The GIGANTIC amount of IInventory methods...
	@Override
	public String getName() { return "citadel_" + mFactionUUID.toString(); } // TODO: Proper display name?
	@Override
	public boolean hasCustomName() { return false; }
	@Override
	public int getSizeInventory() { return NUM_SLOTS; }
	@Override
	public boolean isEmpty() 
	{
		for(int i = 0; i < NUM_YIELD_STACKS; i++)
			if(!mYieldStacks[i].isEmpty())
				return false;
		return mBannerStack.isEmpty();
	}
	// In terms of indexing, the yield stacks are 0 - 8
	@Override
	public ItemStack getStackInSlot(int index) 
	{
		if(index < NUM_YIELD_STACKS)
			return mYieldStacks[index];
		else if(index == BANNER_SLOT_INDEX)
			return mBannerStack;
		return ItemStack.EMPTY;
	}
	@Override
	public ItemStack decrStackSize(int index, int count) 
	{
		if(index < NUM_YIELD_STACKS)
		{
			int numToTake = Math.max(count, mYieldStacks[index].getCount());
			ItemStack result = mYieldStacks[index].copy();
			result.setCount(numToTake);
			mYieldStacks[index].setCount(mYieldStacks[index].getCount() - numToTake);
			return result;
		}
		else if(index == BANNER_SLOT_INDEX)
		{
			int numToTake = Math.max(count, mBannerStack.getCount());
			ItemStack result = mBannerStack.copy();
			result.setCount(numToTake);
			mBannerStack.setCount(mBannerStack.getCount() - numToTake);
			return result;
		}
		return ItemStack.EMPTY;
	}
	@Override
	public ItemStack removeStackFromSlot(int index) 
	{
		ItemStack result = ItemStack.EMPTY;
		if(index < NUM_YIELD_STACKS)
		{
			result = mYieldStacks[index];
			mYieldStacks[index] = ItemStack.EMPTY;			
		}
		else if(index == BANNER_SLOT_INDEX)
		{
			result = mBannerStack;
			mBannerStack = ItemStack.EMPTY;		
		}
		return result;
	}
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) 
	{
		if(index < NUM_YIELD_STACKS)
		{
			mYieldStacks[index] = stack;
		}
		else if(index == BANNER_SLOT_INDEX)
		{
			mBannerStack = stack;
		}
	}
	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}
	@Override
	public boolean isUsableByPlayer(EntityPlayer player) 
	{
		return mFactionUUID == Faction.NULL || WarForgeMod.INSTANCE.IsPlayerInFaction(player.getUniqueID(), mFactionUUID);
	}
	@Override
	public void openInventory(EntityPlayer player) { }
	@Override
	public void closeInventory(EntityPlayer player) { }
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) 
	{
		if(index < NUM_YIELD_STACKS)
		{
			return true;
		}
		else if(index == BANNER_SLOT_INDEX)
		{
			return stack.getItem() instanceof ItemBanner || stack.getItem() instanceof ItemShield;
		}
		return false;
	}
	@Override
	public int getField(int id)  { return 0; }
	@Override
	public void setField(int id, int value) { }
	@Override
	public int getFieldCount() { return 0; }
	@Override
	public void clear() 
	{ 
		for(int i = 0; i < NUM_YIELD_STACKS; i++)
			mYieldStacks[i] = ItemStack.EMPTY;
		mBannerStack = ItemStack.EMPTY;
	}
	// ----------------------------------------------------------
}
