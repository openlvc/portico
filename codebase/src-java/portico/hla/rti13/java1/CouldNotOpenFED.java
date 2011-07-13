package hla.rti13.java1;

public class CouldNotOpenFED extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public CouldNotOpenFED( String reason )
	{
		super( reason );
	}

	public CouldNotOpenFED( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public CouldNotOpenFED()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public CouldNotOpenFED( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public CouldNotOpenFED( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
