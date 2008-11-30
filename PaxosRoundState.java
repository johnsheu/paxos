import java.util.HashMap;
import java.util.TreeMap;

/*
 * Screw style, all this class does is save typing!
 */
public class PaxosRoundState
{
	public PaxosValue highestProposalAcceptedValue = null;
	public long highestProposalAccepted = -1L;
	public long highestPrepareRequest = -1L;
	public long numPrepareResp = -1L;
	public HashMap<Long, TreeMap<Long, PaxosValue>> highestPrepareResp = null;

	public void PaxosRoundState()
	{
		highestPrepareResp = new HashMap<Long, TreeMap<Long, PaxosValue>>();
	}

	public long getNumPrepareResp( Long propNum )
        {
		return -1L;
	}

	public void incrementNumPrepareResp( Long propNum )
	{
	}

	public Long getHighestPrepareResp( Long propNum )
        {
	    return new Long( -1L );
	}

	public PaxosValue getHighestPrepareRespValue( Long propNum )
        {
	    return new PaxosValue();
	}

    
	public void setHighestPrepareResp( Long propNum, Long highestPreepareRequest, PaxosValue value )
        {
	}
}
