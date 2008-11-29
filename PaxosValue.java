import java.io.Serializable;

public class PaxosValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		NONE,
		ADD,
		EDIT,
		DELETE,
		READ,
	}

	private String key = "";

	private String value = "";

	private Type type = Type.NONE;

	public PaxosValue()
	{

	}

	public PaxosValue( String key, String value )
	{
		this.key = key;
		this.value = value;
	}

	public PaxosValue( String key, String value, Type type )
	{
		this.key = key;
		this.value = value;
		this.type = type;
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

	public void setType( Type type )
	{
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}
}

