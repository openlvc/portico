package hla.rti13.java1;

public class TimeRegulationWasNotEnabled extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public TimeRegulationWasNotEnabled( String reason )
	{
		super( reason );
	}

	public TimeRegulationWasNotEnabled( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public TimeRegulationWasNotEnabled()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public TimeRegulationWasNotEnabled( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public TimeRegulationWasNotEnabled( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
