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