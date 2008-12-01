import java.util.HashMap;
import java.util.TreeMap;

/*
 * Screw style, all this class does is save typing!
 */
public class PaxosRoundState
{
	public long highestProposalAccepted = -1L;
	public PaxosValue highestProposalAcceptedValue = null;

	public long highestPrepareRequest = -1L;  //The value promised

	public HashMap<Long, Boolean> hasAcceptMajority = null;

	public HashMap<Long, Long>       numPrepareResp = null;
	public HashMap<Long, Long>       highestPrepareResp = null;
	public HashMap<Long, PaxosValue> highestPrepareRespValue = null;

	public HashMap<Long, HashMap<PaxosValue, Long>> acceptInformsMap = null;

	public boolean accepted = false;

	public PaxosValue clientValue;

	public PaxosRoundState()
	{
		highestPrepareResp = new HashMap<Long, Long>();
		highestPrepareRespValue = new HashMap<Long, PaxosValue>();
		numPrepareResp = new HashMap<Long, Long>();
		acceptInformsMap = new HashMap<Long, HashMap<PaxosValue, Long>>();
		hasAcceptMajority = new HashMap<Long, Boolean>();
	}

	public long getNumPrepareResp( long propNum )
        {
		Long ret = numPrepareResp.get( propNum );
		if( ret == null )
		{
			ret = new Long( 0L );
			numPrepareResp.put( new Long( propNum ), ret);
		}
		return ret;
	}

	public void incrementNumPrepareResp( long propNum )
	{
	    
		Long numPrepResp = numPrepareResp.get( propNum );
                if( numPrepResp == null )
			numPrepareResp.put( new Long( propNum ), new Long( 1L ));    
                else
			numPrepareResp.put( new Long( propNum ), new Long( numPrepResp.longValue() + 1L ) );
	}

	public long getHighestPrepareResp( long propNum )
        {
	    Long l = highestPrepareResp.get( propNum );
	    if( l == null )
		return -1;
	    else
		return l.longValue();
	}

	public PaxosValue getHighestPrepareRespValue( long propNum )
        {
	    return highestPrepareRespValue.get( propNum );
	}

    
	public void setHighestPrepareResp( long propNum, long highestNum, PaxosValue highestValue )
        {
	    Long propN = new Long( propNum );
	    highestPrepareResp.put( propN, new Long( highestNum ));
	    highestPrepareRespValue.put( propN, highestValue );
	}

	public void incrementAcceptInforms( long propN, PaxosValue value )
	{
	    Long propNum = new Long( propN );
	    HashMap<PaxosValue, Long> acceptInforms = acceptInformsMap.get( propNum );
	    if( acceptInforms == null )
	    {
	        acceptInforms = new HashMap< PaxosValue, Long>();
		acceptInforms.put( value, new Long( 1 ));
		acceptInformsMap.put( propNum, acceptInforms );
	    }
	    else
	    {
		Long l = acceptInforms.get( value );
		if( l == null )
		    l = new Long( 0L );
		acceptInforms.put( value, new Long( l.longValue() + 1L ));
	    }
	    
	}

	public long getNumAcceptInforms( long propNum, PaxosValue value)
	{
	    HashMap<PaxosValue, Long> acceptInforms = acceptInformsMap.get( new Long( propNum) );
	    if( acceptInforms == null )
		return 0L;
	    else
		return acceptInforms.get( value ).longValue();
	
	}

	public void setHasAcceptMajority( long propNum, boolean majority )
	{
	    hasAcceptMajority.put( new Long( propNum ), new Boolean( majority ));
	}

	public boolean hasAcceptMajority( long propNum )
	{
		Boolean b = hasAcceptMajority.get( propNum );
		if( b == null )
			return false;
		return b.booleanValue();
	}
}
