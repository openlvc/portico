package hla.rti13.java1;

public class InvalidTransportationHandle extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public InvalidTransportationHandle( String reason )
	{
		super( reason );
	}

	public InvalidTransportationHandle( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public InvalidTransportationHandle()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public InvalidTransportationHandle( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public InvalidTransportationHandle( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
