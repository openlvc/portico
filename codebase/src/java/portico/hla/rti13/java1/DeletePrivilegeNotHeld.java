package hla.rti13.java1;

public class DeletePrivilegeNotHeld extends RTIexception
{
	private static final long serialVersionUID = 98121116105109L;
	
	public DeletePrivilegeNotHeld( String reason )
	{
		super( reason );
	}

	public DeletePrivilegeNotHeld( String reason, int serial )
	{
		super( reason, serial );
	}
	
	public DeletePrivilegeNotHeld()
    {
	    super();
    }

    /**
     * @param cause The cause of the exception
     */
    public DeletePrivilegeNotHeld( Throwable cause )
    {
	    super( cause );
    }

    /**
     * @param message The message to create the exception with
     * @param cause The cause of the exception
     */
    public DeletePrivilegeNotHeld( String message, Throwable cause )
    {
	    super( message, cause );
    }
}
