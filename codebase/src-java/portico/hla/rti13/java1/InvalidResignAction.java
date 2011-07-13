package hla.rti13.java1;

public class InvalidResignAction extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidResignAction( String reason )
	{
		super( reason );
	}

	public InvalidResignAction( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidResignAction()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidResignAction( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidResignAction( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
