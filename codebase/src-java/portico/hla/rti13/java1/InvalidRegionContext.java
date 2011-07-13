package hla.rti13.java1;

public class InvalidRegionContext extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidRegionContext( String reason )
	{
		super( reason );
	}

	public InvalidRegionContext( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidRegionContext()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidRegionContext( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidRegionContext( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
