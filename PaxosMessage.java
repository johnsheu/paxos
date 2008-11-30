import java.io.Serializable;
import java.util.BitSet;

public class PaxosMessage extends Message implements Serializable, Cloneable,
	Comparable<PaxosMessage>
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		NONE,
		NEWLEADER,
		KEEPALIVE,
		PREP_REQ,
		PREP_RESP,
		ACC_REQ,
		ACC_INF,
	}

	private Type type = Type.NONE;
	private long proposalNumber = -1L;
	private PaxosValue value = null;
	private long highestAcceptedNumber = -1L;
	private long round = -1L;
	private BitSet chosenProposals = null;
	private long highestRound = -1L;

	public PaxosMessage()
	{
		
	}

	public Type getType()
	{
		return type;
	}

	public void setType( Type type )
	{
		this.type = type;
	}

	public long getProposalNumber()
	{
		return proposalNumber;
	}

	public void setProposalNumber( long n )
	{
		this.proposalNumber = n;
	}

	public PaxosValue getValue()
	{
		return value;
	}

	public void setValue( PaxosValue v )
	{
		this.value = v;
	}

	public long getRound()
	{
		return round;
	}

	public void setRound( long round )
	{
		this.round = round;
	}

	public long getHighestRound()
	{
		return highestRound;
	}

	public void setHighestRound( long highestRound )
	{
		this.highestRound = highestRound;
	}

	public long getHighestAcceptedNumber()
	{
		return highestAcceptedNumber;
	}

	public void setHighestAcceptedNumber( long n )
	{
		highestAcceptedNumber = n;
	}

	public BitSet getChosenProposals()
	{
		return chosenProposals;
	}

	public void setChosenProposals( BitSet chosenProposals )
	{
		this.chosenProposals = chosenProposals;
	}

	public int compareTo( PaxosMessage other )
	{
		if ( this == other )
			return 0;

		else if ( this.proposalNumber < other.proposalNumber )
			return -1;

		else if ( this.proposalNumber > other.proposalNumber )
			return 1;

		else 
			return 0;
	}
}

