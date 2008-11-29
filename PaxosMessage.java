import java.io.Serializable;

public class PaxosMessage extends Message implements Serializable, Comparable<PaxosMessage>
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		NONE,
		KEEPALIVE,
		PREP_REQ,
		PREP_RESP,
		ACC_REQ,
		ACC_INF,
	}

	private Type type = Type.NONE;
	private Long proposalNumber = null;
	private PaxosValue value = null;
	private Long round = null;

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

	public Long getProposalNumber()
	{
		return proposalNumber;
	}

	public void setProposalNumber( Long n )
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

	public Long getRound()
	{
		return round;
	}

	public void setRound( Long rount )
	{
		this.round = round;
	}

	public int compareTo( PaxosMessage other )
	{
		if( this == other )
			return 0;

		else if( this.proposalNumber < other.proposalNumber )
			return -1;

		else if( this.proposalNumber > other.proposalNumber )
			return 1;

		else 
			return 0;
	}
}

