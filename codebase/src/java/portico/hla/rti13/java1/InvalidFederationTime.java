package hla.rti13.java1;

public class InvalidFederationTime extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidFederationTime( String reason )
	{
		super( reason );
	}

	public InvalidFederationTime( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidFederationTime()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidFederationTime( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidFederationTime( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
