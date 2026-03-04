package org.portico2.lrc.services.time.incoming;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.msg.FederationLBTS;
import org.portico2.lrc.LRCMessageHandler;

import java.util.Map;

public class FederationLBTSHandler extends LRCMessageHandler {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process(MessageContext context) throws JException {
		FederationLBTS message = context.getRequest(FederationLBTS.class, this);

		double federationLBTS = message.getFederationLBTS();

		lrcState.setFederationLbts(federationLBTS);

		if (logger.isDebugEnabled()) {
			logger.debug("FEDERATION LBTS advanced to time ["+federationLBTS+"]");
		}
	}
}
