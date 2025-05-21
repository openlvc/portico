package org.portico2.common.services.time.msg;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class FederationLBTS extends PorticoMessage {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double federationLBTS;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederationLBTS(double federationLBTS) {
		this.federationLBTS = federationLBTS;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@Override
	public MessageType getType() {
		return MessageType.FederationLBTS;
	}

	public double getFederationLBTS() {
		return federationLBTS;
	}

	public void setFederationLBTS(double federationLBTS) {
		this.federationLBTS = federationLBTS;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.federationLBTS = input.readDouble();
	}

	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );

		output.writeDouble(this.federationLBTS);
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
