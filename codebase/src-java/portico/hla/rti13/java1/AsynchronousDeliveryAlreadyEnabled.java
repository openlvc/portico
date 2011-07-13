package hla.rti13.java1;

public class AsynchronousDeliveryAlreadyEnabled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AsynchronousDeliveryAlreadyEnabled( String reason )
	{
		super( reason );
	}

	public AsynchronousDeliveryAlreadyEnabled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AsynchronousDeliveryAlreadyEnabled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AsynchronousDeliveryAlreadyEnabled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AsynchronousDeliveryAlreadyEnabled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
