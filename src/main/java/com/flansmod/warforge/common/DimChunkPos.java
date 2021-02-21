package com.flansmod.warforge.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class DimChunkPos extends ChunkPos
{
	public int mDim;
	
	public DimChunkPos(int dim, int x, int z) 
	{
		super(x, z);
		mDim = dim;
	}
	
	public DimChunkPos(int dim, BlockPos pos)
	{
		super(pos);
		mDim = dim;
	}

	public boolean InSameDimension(DimChunkPos other)
	{
		return other.mDim == mDim;
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

        if (!(other instanceof DimChunkPos))
            return false;

        DimChunkPos dcpos = (DimChunkPos)other;
        return this.mDim == dcpos.mDim && this.x == dcpos.x && this.z == dcpos.z;
    }
    
	@Override
    public String toString()
    {
        return "[" + this.mDim + ": " + this.x + ", " + this.z + "]";
    }
}
