package hla.rti13.java1;

public class FederationExecutionDoesNotExist extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederationExecutionDoesNotExist( String reason )
	{
		super( reason );
	}

	public FederationExecutionDoesNotExist( String reason, int serial )
	{
		super( reason, serial );
	}

	public FederationExecutionDoesNotExist()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederationExecutionDoesNotExist( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederationExecutionDoesNotExist( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
