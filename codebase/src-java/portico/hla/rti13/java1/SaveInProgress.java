package hla.rti13.java1;

public class SaveInProgress extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public SaveInProgress( String reason )
	{
		super( reason );
	}

	public SaveInProgress( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public SaveInProgress()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public SaveInProgress( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public SaveInProgress( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
