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

	public long getNumPrepareResp( long propNum )
        {
		return -1L;
	}

	public void incrementNumPrepareResp( long propNum )
	{
	    /*
                    HashMap<Long, Long> prepResponses = numPrepareRespMap.get( msg.getRound() );
                    if( prepResponses == null )
                    {
                        prepResponses = new HashMap<Long, Long>();
                        prepResponses.put( propNum, new Long( 1L ) );
                        prepareResponses.put( round, prepResponses );
                    }
                    else
                        prepResponses.put( propNum, Long.valueOf( prepResponses.get( propNum ).longValue() + 1L ) );
	    */

	}

	public long getHighestPrepareResp( long propNum )
        {
	    return -1L;
	}

	public PaxosValue getHighestPrepareRespValue( long propNum )
        {
	    return new PaxosValue();
	}

    
	public void setHighestPrepareResp( long propNum, long highestPreepareRequest, PaxosValue value )
        {
	    /*
                    HashMap< Long, TreeMap<Long, PaxosValue>> highestPrepRespMap = highestPrepareRespMap.get( round );
                    TreeMap<Long, PaxosValue> highestPrepResp;
                    if( highestPrepRespMap == null )
                    {
                        highestPrepRespMap = new HashMap<Long, TreeMap<Long, PaxosValue>>();
                        highestPrepareRespMap.put( round, highestPrepRespMap );
                    }
                    highestPrepResp = highestPrepRespMap.get( propNum );
                    if( highestPrepResp == null )
                    {
                        highestPrepResp = new TreeMap<Long, PaxosValue> ();
                        highestPrepRespMap.put( propNum, highestPrepResp );
                    }

                    highestPrepResp.put( msg.getHighestAcceptedNumber, msg.getValue() );
	    */

	}
}
