package hla.rti13.java1;

public class AttributeAlreadyBeingAcquired extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AttributeAlreadyBeingAcquired( String reason )
	{
		super( reason );
	}

	public AttributeAlreadyBeingAcquired( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AttributeAlreadyBeingAcquired()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AttributeAlreadyBeingAcquired( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AttributeAlreadyBeingAcquired( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
