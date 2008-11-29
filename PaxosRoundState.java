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
}

