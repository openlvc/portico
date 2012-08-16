package hla.rti13.java1;

public class FederateNotExecutionMember extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederateNotExecutionMember( String reason )
	{
		super( reason );
	}

	public FederateNotExecutionMember( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederateNotExecutionMember()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederateNotExecutionMember( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederateNotExecutionMember( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
