/*
 *   Copyright 2010 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL) 
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *   
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
package org.portico.bindings.ptalk.channel;

import org.portico.bindings.ptalk.LrcConnection;
import org.portico.bindings.ptalk.transport.ITransport;

/**
 * When a packet is received from a {@link ITransport} and then passed through a {@link Pipeline},
 * it is ready to be handed off to the {@link LrcConnection} for processing. However, inside each
 * LrcConnection, there can be a number of channels open.
 * <p/>
 * Any time a federate makes reference to a federation (such as a create, destroy or join call), a
 * new connection to that channel is opened. This channel doesn't necessarily have to be the
 * channel representing the federation to which the federate is joined. However, when a packet is
 * received on a channel, it has to go somewhere once it has been processed by the Pipeline. *IF*
 * the channel is that of the federation the federate is connected to, this makes sense that the
 * LrcConnection be that target (which then passes it into the LRC infrastructure). However, if it
 * is just one of the other open channels, the packets might need to go somewhere else.
 * <p/>
 * To top this all off. All LrcConnections have an open connection to a group management channel
 * that is used to pass administrative information back and forth. All these packets need a final
 * ultimate target.
 * <p/>
 * For this reason, the {@link IPacketReceiver} interface exists. A receive is placed inside the
 * {@link Channel} and incoming packets are passed to it. Different implementations can route the
 * packets to different locations. For messages from the channel we're joined to, this might be
 * the LRC. For other channels we happen to be joined to, this might be nowhere at all!
 */
public interface IPacketReceiver
{
	public void receive( Packet packet );
}
