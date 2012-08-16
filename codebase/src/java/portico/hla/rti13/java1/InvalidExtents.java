package hla.rti13.java1;

public class InvalidExtents extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidExtents( String reason )
	{
		super( reason );
	}

	public InvalidExtents( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidExtents()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidExtents( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidExtents( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
