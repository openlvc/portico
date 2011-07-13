package hla.rti13.java1;

public class CouldNotDiscover extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public CouldNotDiscover( String reason )
	{
		super( reason );
	}

	public CouldNotDiscover( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public CouldNotDiscover()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public CouldNotDiscover( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public CouldNotDiscover( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
