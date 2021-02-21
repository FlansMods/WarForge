package com.flansmod.warforge.common;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class DimBlockPos extends BlockPos
{
	public int mDim;
	
	public DimBlockPos(int dim, int x, int y, int z)
    {
        super(x, y, z);
        mDim = dim;
    }

    public DimBlockPos(int dim, double x, double y, double z)
    {
        super(x, y, z);
        mDim = dim;
    }

    public DimBlockPos(Entity source)
    {
        super(source);
        mDim = source.dimension;
    }
    
    public DimBlockPos(TileEntity source)
    {
        super(source.getPos().getX(), source.getPos().getY(), source.getPos().getZ());
        mDim = source.getWorld().provider.getDimension();
    }

    public DimBlockPos(int dim, Vec3d vec)
    {
        super(vec);
        mDim = dim;       
    }

    public DimBlockPos(int dim, Vec3i source)
    {
    	super(source);
    	mDim = dim;
    }
    
	@Override
	public int hashCode()
    {
		return super.hashCode() ^ (155225 * this.mDim + 140501023);
    }

	@Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;

        if (!(other instanceof DimBlockPos))
            return false;

        DimBlockPos dcpos = (DimBlockPos)other;
        return this.mDim == dcpos.mDim 
        		&& this.getX() == dcpos.getX()
        		&& this.getY() == dcpos.getY()
        		&& this.getZ() == dcpos.getZ();
    }
    
	@Override
    public String toString()
    {
        return "[" + this.mDim + ": " + this.getX() + ", " + this.getY() + ", " + this.getZ() + "]";
    }
}
