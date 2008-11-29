public class PaxosValue
{
	private String key;
	private String value;

	public PaxosValue()
	{
	}

	public PaxosValue( String key, String value )
	{
		this.key = key;
		this.value = value;
	}

	public void setKey( String key )
	{
		this.key = key;
	}

	public String getKey()
	{
		return key;
	}
	public void setValue( String value )
	{
		this.value = value;
	}
	public String getValue()
	{
		return value;
	}
}

