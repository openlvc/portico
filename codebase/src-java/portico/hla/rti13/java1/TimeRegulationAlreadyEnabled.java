package hla.rti13.java1;

public class TimeRegulationAlreadyEnabled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public TimeRegulationAlreadyEnabled( String reason )
	{
		super( reason );
	}

	public TimeRegulationAlreadyEnabled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public TimeRegulationAlreadyEnabled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public TimeRegulationAlreadyEnabled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public TimeRegulationAlreadyEnabled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
