package hla.rti13.java1;

public class EnableTimeRegulationPending extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public EnableTimeRegulationPending( String reason )
	{
		super( reason );
	}

	public EnableTimeRegulationPending( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public EnableTimeRegulationPending()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public EnableTimeRegulationPending( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public EnableTimeRegulationPending( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
