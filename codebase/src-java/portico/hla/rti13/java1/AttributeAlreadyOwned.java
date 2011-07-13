package hla.rti13.java1;

public class AttributeAlreadyOwned extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeAlreadyOwned( String reason )
	{
		super( reason );
	}

	public AttributeAlreadyOwned( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeAlreadyOwned()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeAlreadyOwned( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeAlreadyOwned( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
