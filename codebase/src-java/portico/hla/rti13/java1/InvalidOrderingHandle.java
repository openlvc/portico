package hla.rti13.java1;

public class InvalidOrderingHandle extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidOrderingHandle( String reason )
	{
		super( reason );
	}

	public InvalidOrderingHandle( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidOrderingHandle()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidOrderingHandle( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidOrderingHandle( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
