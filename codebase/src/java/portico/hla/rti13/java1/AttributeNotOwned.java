package hla.rti13.java1;

public class AttributeNotOwned extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeNotOwned( String reason )
	{
		super( reason );
	}

	public AttributeNotOwned( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeNotOwned()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeNotOwned( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeNotOwned( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
