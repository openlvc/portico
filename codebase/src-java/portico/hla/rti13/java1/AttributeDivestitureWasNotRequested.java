package hla.rti13.java1;

public class AttributeDivestitureWasNotRequested extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeDivestitureWasNotRequested( String reason )
	{
		super( reason );
	}

	public AttributeDivestitureWasNotRequested( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeDivestitureWasNotRequested()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeDivestitureWasNotRequested( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeDivestitureWasNotRequested( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
