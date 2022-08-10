package org.cip4.bambi.core.messaging;

import java.util.Collection;

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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof MessageIdentifier))
		{
			return false;
		}
		final MessageIdentifier msg = (MessageIdentifier) obj;

		if (!ContainerUtil.equals(deviceID, msg.deviceID))
		{
			return false;
		}
		if (!ContainerUtil.equals(slaveChannelID, msg.slaveChannelID))
		{
			return false;
		}
		if (!ContainerUtil.equals(misChannelID, msg.misChannelID))
		{
			return false;
		}
		if (!ContainerUtil.equals(msgType, msg.msgType))
		{
			return false;
		}
		return true;
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

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int hc = deviceID == null ? 0 : deviceID.hashCode();
		hc += msgType == null ? 0 : msgType.hashCode();
		hc += slaveChannelID == null ? 0 : slaveChannelID.hashCode();
		hc += misChannelID == null ? 0 : misChannelID.hashCode();
		return hc;
	}

	@Override
	public String toString()
	{
		return "MessageIdentifier [misChannelID=" + misChannelID + ", slaveChannelID=" + slaveChannelID + ", msgType=" + msgType + ", senderID=" + deviceID + "]";
	}
}