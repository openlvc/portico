package hla.rti13.java1;

public class EventRetractionHandle extends java.lang.Object
{
	public int theSerialNumber;

	public int sendingFederate;

	protected EventRetractionHandle( int argtheSerialNumber, int argsendingFederate )
	{
		theSerialNumber = argtheSerialNumber;
		sendingFederate = argsendingFederate;
	}

	public boolean equals( Object obj )
	{
		EventRetractionHandle rhs = (EventRetractionHandle)obj;
		return ((rhs.theSerialNumber == theSerialNumber) &&
			   (rhs.sendingFederate == sendingFederate));
	}

	public String toString()
	{
		return new String( "[theSerialNumber=" + theSerialNumber +
		                   " sendingFederate=" + sendingFederate + "]" );
	}

	public int hashCode()
	{
		return theSerialNumber ^ sendingFederate;
	}
}
