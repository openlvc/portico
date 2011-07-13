package hla.rti13.java1;

public class AsynchronousDeliveryAlreadyDisabled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public AsynchronousDeliveryAlreadyDisabled( String reason )
	{
		super( reason );
	}

	public AsynchronousDeliveryAlreadyDisabled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public AsynchronousDeliveryAlreadyDisabled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public AsynchronousDeliveryAlreadyDisabled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public AsynchronousDeliveryAlreadyDisabled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
