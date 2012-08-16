package hla.rti13.java1;

public class InvalidLookahead extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidLookahead( String reason )
	{
		super( reason );
	}

	public InvalidLookahead( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidLookahead()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidLookahead( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidLookahead( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
