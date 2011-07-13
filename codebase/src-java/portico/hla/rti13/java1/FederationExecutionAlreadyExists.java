package hla.rti13.java1;

public class FederationExecutionAlreadyExists extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederationExecutionAlreadyExists( String reason )
	{
		super( reason );
	}

	public FederationExecutionAlreadyExists( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederationExecutionAlreadyExists()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederationExecutionAlreadyExists( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederationExecutionAlreadyExists( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
