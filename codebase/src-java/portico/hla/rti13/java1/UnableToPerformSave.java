package hla.rti13.java1;

public class UnableToPerformSave extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public UnableToPerformSave( String reason )
	{
		super( reason );
	}

	public UnableToPerformSave( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public UnableToPerformSave()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public UnableToPerformSave( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public UnableToPerformSave( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
