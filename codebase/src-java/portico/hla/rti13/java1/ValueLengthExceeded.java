package hla.rti13.java1;

/**
 * This class will <b>NEVER BE THROWN BY Portico</b> (knowingly :P). I include it here only because
 * it is part of the DMSO rti13.java1 pacakge and people may be catching it for some reason.
 */
public class ValueLengthExceeded extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public ValueLengthExceeded( String reason )
	{
		super( reason );
	}

	public ValueLengthExceeded( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public ValueLengthExceeded()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public ValueLengthExceeded( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public ValueLengthExceeded( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
