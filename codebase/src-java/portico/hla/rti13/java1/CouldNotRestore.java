package hla.rti13.java1;

public class CouldNotRestore extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public CouldNotRestore( String reason )
	{
		super( reason );
	}

	public CouldNotRestore( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public CouldNotRestore()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public CouldNotRestore( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public CouldNotRestore( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
