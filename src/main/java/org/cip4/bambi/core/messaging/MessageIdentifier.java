/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core.messaging;

import java.util.Collection;
import java.util.Objects;

import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.util.ContainerUtil;

/**
 * class that identifies messages. if equal, messages are integrated, else they are retained independently
 *
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public class MessageIdentifier implements Cloneable
{
	protected String misChannelID;

	/**
	 * Getter for misChannelID attribute.
	 *
	 * @return the misChannelID
	 */
	public String getMisChannelID()
	{
		return misChannelID;
	}

	protected String slaveChannelID;
	protected String msgType;
	protected String deviceID;

	/**
	 * @param m the message
	 * @param jmfSenderID the senderID of the jmf package; if null extract if from the message
	 */
	public MessageIdentifier(final JDFMessage m, final String jmfSenderID)
	{
		if (m == null)
		{
			return;
		}
		msgType = m.getType();
		slaveChannelID = m.getrefID();
		if (KElement.isWildCard(slaveChannelID))
		{
			slaveChannelID = null;
		}
		misChannelID = slaveChannelID == null ? m.getID() : null;
		if (!KElement.isWildCard(jmfSenderID))
		{
			deviceID = jmfSenderID;
		}
		else
		{
			deviceID = null;
		}
	}

	/**
	 * clone this as many times as misChannels has entries
	 *
	 * @param misChannels set of channelIDS
	 * @return array of cloned MessageIdentifier, one with ech channelID
	 */
	public MessageIdentifier[] cloneChannels(final Collection<String> misChannels)
	{
		if (ContainerUtil.isEmpty(misChannels))
		{
			return null;
		}
		final MessageIdentifier[] ret = new MessageIdentifier[misChannels.size()];
		int n = 0;
		for (String key : misChannels)
		{
			ret[n] = clone();
			ret[n].misChannelID = key;
			n++;
		}
		return ret;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MessageIdentifier clone()
	{
		final MessageIdentifier clone = new MessageIdentifier(null, null);

		clone.misChannelID = misChannelID;
		clone.slaveChannelID = slaveChannelID;
		clone.msgType = msgType;
		clone.deviceID = deviceID;
		return clone;
	}

	/**
	 * if obj matches, i.e. any null element of object is also considered matching
	 *
	 * @param msg
	 * @return true if msg matches this
	 */
	public boolean matches(final MessageIdentifier msg)
	{
		if (msg.deviceID != null && !ContainerUtil.equals(deviceID, msg.deviceID))
		{
			return false;
		}
		if (msg.misChannelID != null && !ContainerUtil.equals(misChannelID, msg.misChannelID))
		{
			return false;
		}
		if (msg.slaveChannelID != null && !ContainerUtil.equals(slaveChannelID, msg.slaveChannelID))
		{
			return false;
		}
		if (!ContainerUtil.equals(msgType, msg.msgType))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "MessageIdentifier [misChannelID=" + misChannelID + ", slaveChannelID=" + slaveChannelID + ", msgType=" + msgType + ", senderID=" + deviceID + "]";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(deviceID, misChannelID, msgType, slaveChannelID);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageIdentifier other = (MessageIdentifier) obj;
		return Objects.equals(deviceID, other.deviceID) && Objects.equals(misChannelID, other.misChannelID) && Objects.equals(msgType, other.msgType)
				&& Objects.equals(slaveChannelID, other.slaveChannelID);
	}
}