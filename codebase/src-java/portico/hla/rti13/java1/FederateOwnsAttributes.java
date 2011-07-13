package hla.rti13.java1;

public class FederateOwnsAttributes extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederateOwnsAttributes( String reason )
	{
		super( reason );
	}

	public FederateOwnsAttributes( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederateOwnsAttributes()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederateOwnsAttributes( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederateOwnsAttributes( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
