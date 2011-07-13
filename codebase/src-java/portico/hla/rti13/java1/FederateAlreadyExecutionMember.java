package hla.rti13.java1;

public class FederateAlreadyExecutionMember extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederateAlreadyExecutionMember( String reason )
	{
		super( reason );
	}

	public FederateAlreadyExecutionMember( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederateAlreadyExecutionMember()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederateAlreadyExecutionMember( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederateAlreadyExecutionMember( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
