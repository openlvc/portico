package hla.rti13.java1;

public class FederateInternalError extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederateInternalError( String reason )
	{
		super( reason );
	}

	public FederateInternalError( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederateInternalError()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederateInternalError( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederateInternalError( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
