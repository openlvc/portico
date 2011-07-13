package hla.rti13.java1;

public class RTIinternalError extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public RTIinternalError( String reason, int serial )
	{
		super( reason, serial );
	}
	
	/**
     * Just create an empty exception
     */
    public RTIinternalError()
    {
	    super();
    }

    /**
     * @param message The message to create the exception with
     */
    public RTIinternalError( String message )
    {
	    super( message );
    }

    /**
     * @param cause The cause of the exception
     */
    public RTIinternalError( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public RTIinternalError( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
