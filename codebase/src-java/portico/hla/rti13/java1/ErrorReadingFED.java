package hla.rti13.java1;

public class ErrorReadingFED extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ErrorReadingFED( String reason )
	{
		super( reason );
	}

	public ErrorReadingFED( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ErrorReadingFED()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ErrorReadingFED( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ErrorReadingFED( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
