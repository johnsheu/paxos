import java.util.BitSet;
import java.io.Serializable;

/**
 * This class implements an increasing vector of bits that grows as needed.
 * It is similar to {@link BitSet}, with the property that bits may only be
 * singly examined, or set to <code>true</code>.  In addition, it is also
 * possible to find the length of the longest contiguous prefix of set bits
 * with <code>firstClearBit</code>.
 */
public class IncreasingBitSet implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;
	
	/*
	 * The longest prefix of contiguous set bits
	 */
	private int prefix = 0;

	/*
	 * The truncated prefix of contiguous set bits
	 */
	private int truncated = 0;

	/*
	 * The discontiguous part of the bit set
	 */
	private BitSet bitset = null;

	/**
	 * Creates a new bit set.  All bits are initially <code>false</code>.
	 */
	public IncreasingBitSet()
	{
		bitset = new BitSet();
	}

	/**
	 * Creates a new bit set whose initial size is large enough to explicitly
	 * represent bits with indices in the range <code>0</code> through
	 * <code>nbits-1</code>.  All bits are initially <code>false</code>.
	 *
	 * @param nbits  the initial size of the bit set
	 *
	 * @throws NegativeArraySizeException  if the specified initial size is
	 *                                     negative
	 */
	public IncreasingBitSet( int nbits ) throws NegativeArraySizeException
	{
		bitset = new BitSet( nbits );
	}

	/**
	 * Sets the bit at the specified index to <code>true</code>.
	 *
	 * @param bitIndex  a bit index
	 *
	 * @throws IndexOutOfBoundException  if the specified index is negative
	 */
	public void set( int bitIndex )
	{
		if ( bitIndex < 0 )
		{
			//  Force an exception
			bitset.set( bitIndex );
			return;
		}

		if ( bitIndex < prefix )
			return;

		bitset.set( bitIndex - truncated );
		
		int index = prefix - truncated;
		while ( bitset.get( index++ ) )
			prefix += 1;

		if ( prefix - truncated > 256 )
		{
			bitset = bitset.get( prefix - truncated, bitset.length() );
			truncated = prefix;
		}
	}

	/**
	 * Returns the value of the bit with the specified index.  The value is
	 * <code>true</code> if the bit with the index <code>bitIndex</code> is
	 * currently set in this <code>IncreasingBitSet</code>; otherwise the
	 * result is <code>false</code>.
	 *
	 * @param bitIndex  the bit index
	 * @return  the value of the bit with the specified index
	 *
	 * @throws  IndexOutOfBoundsException if the specified index is negative
	 */
	public boolean get( int bitIndex )
	{
		if ( bitIndex < 0 )
			//  Force an exception
			return bitset.get( bitIndex );

		if ( bitIndex < prefix )
			return true;

		return bitset.get( bitIndex - truncated );
	}

	/**
	 * Returns the index of the first bit that is set to <code>false</code>.
	 *
	 * @return  the index of the first clear bit
	 */
	public int firstClearBit()
	{
		return prefix;
	}

	public IncreasingBitSet clone() throws CloneNotSupportedException
	{
		IncreasingBitSet object = (IncreasingBitSet)( super.clone() );
		object.prefix = prefix;
		object.truncated = truncated;
		object.bitset = (BitSet)( bitset.clone() );
		return object;
	}
}

