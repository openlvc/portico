package hla.rti13.java1;

public class InvalidRetractionHandle extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidRetractionHandle( String reason )
	{
		super( reason );
	}

	public InvalidRetractionHandle( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidRetractionHandle()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidRetractionHandle( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidRetractionHandle( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
