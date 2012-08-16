package hla.rti13.java1;

public class EnableTimeRegulationWasNotPending extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public EnableTimeRegulationWasNotPending( String reason )
	{
		super( reason );
	}

	public EnableTimeRegulationWasNotPending( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public EnableTimeRegulationWasNotPending()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public EnableTimeRegulationWasNotPending( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public EnableTimeRegulationWasNotPending( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
