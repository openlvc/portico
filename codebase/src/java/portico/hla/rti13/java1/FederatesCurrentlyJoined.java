package hla.rti13.java1;

public class FederatesCurrentlyJoined extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public FederatesCurrentlyJoined( String reason )
	{
		super( reason );
	}

	public FederatesCurrentlyJoined( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public FederatesCurrentlyJoined()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public FederatesCurrentlyJoined( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public FederatesCurrentlyJoined( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
