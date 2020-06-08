/**
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2018 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2006, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core.messaging;

import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.thread.MyMutex;

public class Trigger
{
	protected String queueEntryID;
	protected NodeIdentifier nodeIdentifier;
	protected String channelID;
	protected int amount;
	private MyMutex mutex;

	public Trigger(final String _queueEntryID, final NodeIdentifier _workStepID, final String _channelID, final int _amount)
	{
		super();
		queueEntryID = _queueEntryID;
		nodeIdentifier = _workStepID;
		channelID = _channelID;
		amount = _amount;
		mutex = new MyMutex();
	}

	/**
	 * equals ignores the value of Amount!
	 */
	@Override
	public boolean equals(final Object t1)
	{
		if (!(t1 instanceof Trigger))
		{
			return false;
		}
		final Trigger t = (Trigger) t1;
		boolean b = ContainerUtil.equals(channelID, t.channelID);
		b = b && ContainerUtil.equals(queueEntryID, t.queueEntryID);
		b = b && ContainerUtil.equals(nodeIdentifier, t.nodeIdentifier);
		return b;
	}

	/**
	 *
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Trigger: queueEntryID: " + queueEntryID + " nodeIdentifier: " + nodeIdentifier + " amount: " + amount + nodeIdentifier + " slaveChannelID: " + channelID;
	}

	/**
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return super.hashCode() + ((channelID == null) ? 0 : channelID.hashCode()) + ((queueEntryID == null) ? 0 : queueEntryID.hashCode())
				+ ((nodeIdentifier == null) ? 0 : nodeIdentifier.hashCode());
	}

	/**
	 * set this trigger as queued
	 */
	protected void setQueued()
	{
		ThreadUtil.notifyAll(mutex);
		mutex = null;
	}

	/**
	 * wait for all trigger to be queued by the dispatcher
	 *
	 * @param triggers
	 * @param milliseconds
	 * @return
	 */
	public static boolean waitQueued(final Trigger[] triggers, final int milliseconds)
	{
		if (triggers == null)
		{
			return true;
		}
		for (final Trigger trigger : triggers)
		{
			if (!trigger.waitQueued(milliseconds))
				return false;
		}
		return true;
	}

	/**
	 * wait for this to be queued
	 *
	 * @param milliseconds
	 * @return
	 */
	public boolean waitQueued(final int milliseconds)
	{
		final boolean b = ThreadUtil.wait(mutex, milliseconds);
		mutex = null;
		return b;
	}

}