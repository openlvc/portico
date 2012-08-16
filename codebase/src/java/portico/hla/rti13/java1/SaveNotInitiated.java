package hla.rti13.java1;

public class SaveNotInitiated extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public SaveNotInitiated( String reason )
	{
		super( reason );
	}

	public SaveNotInitiated( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public SaveNotInitiated()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public SaveNotInitiated( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public SaveNotInitiated( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
