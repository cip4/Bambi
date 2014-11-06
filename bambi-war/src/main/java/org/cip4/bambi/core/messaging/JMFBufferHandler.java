/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.core.messaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.proxy.AbstractProxyDevice;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.VectorMap;

/**
 * Class that buffers messages for subscriptions and integrates the results over time
 * @author rainer prosi
 */
public class JMFBufferHandler extends SignalHandler implements IMessageHandler
{
	/**
	 * class that identifies messages. if equal, messages are integrated, else they are retained independently
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected static class MessageIdentifier implements Cloneable
	{
		protected String misChannelID = null;

		/**
		 * Getter for misChannelID attribute.
		 * @return the misChannelID
		 */
		public String getMisChannelID()
		{
			return misChannelID;
		}

		protected String slaveChannelID = null;
		protected String msgType = null;
		protected String senderID = null;

		/**
		 * @param m the message
		 * @param jmfSenderID the senderID of the jmf package; if null extract if from the message
		 */
		MessageIdentifier(final JDFMessage m, final String jmfSenderID)
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
				senderID = jmfSenderID;
			}
			else
			{
				senderID = m.getSenderID();
				if (KElement.isWildCard(senderID))
				{
					senderID = null;
				}
			}
		}

		/**
		 * clone this as many times as misChannels has entries
		 * @param misChannels set of channelIDS
		 * @return array of cloned MessageIdentifier, one with ech channelID
		 */
		protected MessageIdentifier[] cloneChannels(final Set<String> misChannels)
		{
			if (misChannels == null || misChannels.size() == 0)
			{
				return null;
			}
			final Iterator<String> it = misChannels.iterator();
			final MessageIdentifier[] ret = new MessageIdentifier[misChannels.size()];
			int n = 0;
			while (it.hasNext())
			{
				ret[n] = (MessageIdentifier) clone();
				ret[n].misChannelID = it.next();
				n++;
			}
			return ret;
		}

		/**
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone()
		{
			MessageIdentifier c;
			try
			{
				c = (MessageIdentifier) super.clone();
			}
			catch (final CloneNotSupportedException x)
			{
				return null;
			}
			c.misChannelID = misChannelID;
			c.slaveChannelID = slaveChannelID;
			c.msgType = msgType;
			c.senderID = senderID;
			return c;
		}

		@Override
		public String toString()
		{
			return "[MessageIdentifier: slaveChannelID=" + slaveChannelID + " Type=" + msgType + " SenderID=" + senderID + "]";
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

			if (!ContainerUtil.equals(senderID, msg.senderID))
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
		 * @param msg
		 * @return true if msg matches this
		 */
		public boolean matches(final MessageIdentifier msg)
		{
			if (msg.senderID != null && !ContainerUtil.equals(senderID, msg.senderID))
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
			int hc = senderID == null ? 0 : senderID.hashCode();
			hc += msgType == null ? 0 : msgType.hashCode();
			hc += slaveChannelID == null ? 0 : slaveChannelID.hashCode();
			hc += misChannelID == null ? 0 : misChannelID.hashCode();
			return hc;
		}
	}

	protected VString ignoreSenderIDs = null;
	private static int logCounter = 0;
	protected VectorMap<MessageIdentifier, JDFSignal> messageMap = new VectorMap<MessageIdentifier, JDFSignal>();

	/**
	 * @return the _theDispatcher
	 */
	protected SignalDispatcher getDispatcher()
	{
		return _theDevice.getSignalDispatcher();
	}

	protected AbstractDevice _theDevice; // required for mapping to queueentries

	/**
	 * @param dev 
	 * @param _type
	 * @param _families
	 * @param device the device that this buffer handles - used to dispatch qe specific subscriptions
	 */
	public JMFBufferHandler(AbstractDevice dev, final EnumType _type, final EnumFamily[] _families, final AbstractDevice device)
	{
		super(dev, _type, _families);
		_theDevice = device;
	}

	/**
	 * @return
	 */
	protected QueueProcessor getQueueProcessor()
	{
		return _theDevice.getQueueProcessor();
	}

	/**
	 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#isSubScribable()
	 */
	@Override
	public boolean isSubScribable()
	{
		return true;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
	 */
	@Override
	public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
	{
		if (inputMessage == null)
		{
			return false;
		}
		final EnumFamily family = inputMessage.getFamily();
		if (EnumFamily.Signal.equals(family))
		{
			super.handleMessage(inputMessage, response);
			if (ignore(inputMessage))
			{
				return true;
			}
			return handleSignal((JDFSignal) inputMessage, response);
		}
		else if (EnumFamily.Query.equals(family))
		{
			return handleQuery(inputMessage, response);
		}
		return false;
	}

	/**
	 * @param inputMessage
	 * @param response
	 * @return true if handled
	 */
	protected boolean handleQuery(final JDFMessage inputMessage, final JDFResponse response)
	{
		getSignals(inputMessage, response);
		if (!response.getSubscribed())
		{
			response.deleteNode();// always zapp the dummy response except
			// in a subscription
		}

		inputMessage.deleteNode(); // also zapp the query
		return true;
	}

	/**
	 * @param inputMessage
	 * @return
	 */
	protected boolean ignore(final JDFMessage inputMessage)
	{
		if (ignoreSenderIDs == null)
		{
			return false;
		}
		if (inputMessage == null)
		{
			return true;
		}

		final String senderID = inputMessage.getSenderID();
		return ignoreContains(senderID);
	}

	/**
	 * check for the substring senderID in ignoreSenderIDs
	 * @param senderID
	 * @return 
	 */
	protected boolean ignoreContains(final String senderID)
	{
		if (ignoreSenderIDs == null)
		{
			return false;
		}
		for (String ignoreSenderID : ignoreSenderIDs)
		{
			if (senderID.indexOf(ignoreSenderID) >= 0)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param _ignoreSenderIDs
	 */
	public void setIgnoreSendersIDs(final VString _ignoreSenderIDs)
	{
		if (_ignoreSenderIDs == null || _ignoreSenderIDs.size() == 0)
		{
			ignoreSenderIDs = null;
		}
		else
		{
			ignoreSenderIDs = _ignoreSenderIDs;
		}
	}

	/**
	 * @param inputMessage
	 * @param response
	 * @return
	 */
	protected JDFJMF getSignals(final JDFMessage inputMessage, final JDFResponse response)
	{
		synchronized (messageMap)
		{
			final MessageIdentifier messageIdentifier = new MessageIdentifier(inputMessage, inputMessage.getJMFRoot().getDeviceID());
			final Set<MessageIdentifier> keySet = getMessageIdentifierSet();
			JDFJMF jmf = response.getJMFRoot();
			final Vector<MessageIdentifier> messageIdentifiers = new Vector<MessageIdentifier>();

			for (MessageIdentifier mi : keySet)
			{
				if (mi == null)
				{
					log.error("null mi in keyset");
					continue;
				}
				if (mi.matches(messageIdentifier))
				{
					messageIdentifiers.add(mi);
					final Vector<JDFSignal> sis = getSignalsFromMap(mi);
					if (sis != null)
					{
						for (JDFSignal signal : sis)
						{
							// copy the potentially inherited senderID
							final JDFSignal sNew = (JDFSignal) jmf.copyElement(signal, null);
							// make sure we only update copies
							if (isMySignal(inputMessage, sNew))
							{
								sNew.setSenderID(signal.getSenderID());
								sNew.copyAttribute(AttributeName.REFID, inputMessage, AttributeName.ID, null, null);
							}
							else
							{
								sNew.deleteNode();
							}
						}
					}
				}
			}
			for (MessageIdentifier mi : messageIdentifiers)
			{
				messageMap.remove(mi);
			}
			if (messageIdentifiers.size() == 0)
			{
				jmf = null;
			}
			return jmf;
		}
	}

	/**
	 * return true if the signal corresponds to the input query<br/>
	 * works on the copy. Thus any overwriting methods should or may modify signal
	 * 
	 * @param inputMessage the query to check against
	 * @param signal the signal to check
	 * @return true if matches; if false, the copy of signal will be deleted
	 */
	protected boolean isMySignal(final JDFMessage inputMessage, final JDFSignal signal)
	{
		return true;
	}

	/**
	 * get the first queueentryID that this signal applies to, return null if none was found
	 * 
	 * @param inSignal the incoming signal to check
	 * @return
	 */
	protected String getQueueEntryIDForSignal(final JDFSignal inSignal)
	{
		return null;
	}

	/**
	 * @return
	 */
	protected Set<MessageIdentifier> getMessageIdentifierSet()
	{
		final Set<MessageIdentifier> keySet = messageMap.keySet();
		return keySet;
	}

	/**
	 * @param mi
	 * @return
	 */
	protected Vector<JDFSignal> getSignalsFromMap(final MessageIdentifier mi)
	{
		synchronized (messageMap)
		{
			Vector<JDFSignal> sis = messageMap.get(mi);
			if (sis != null)
			{
				Vector<JDFSignal> clone = new Vector<JDFSignal>();
				clone.addAll(sis);
				sis = clone;
			}
			return sis;
		}

	}

	/**
	 * @param inSignal
	 * @param response
	 * @return true if handled
	 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#handleSignal(org.cip4.jdflib.jmf.JDFSignal, org.cip4.jdflib.jmf.JDFResponse)
	 */
	protected boolean handleSignal(final JDFSignal inSignal, final JDFResponse response)
	{
		final String qeID = getQueueEntryIDForSignal(inSignal);
		final Set<String> requests = getDispatcher().getChannels(inSignal.getEnumType(), inSignal.getSenderID(), qeID);
		final MessageIdentifier[] mi = new MessageIdentifier(inSignal, null).cloneChannels(requests);

		if (mi != null)
		{
			if (logCounter < 10 || logCounter % 100 == 0)
			{
				log.info("broadcasting buffered Signal# " + logCounter + " " + inSignal.getType() + " to " + mi.length + " receivers");
			}
			synchronized (messageMap)
			{
				for (int i = 0; i < mi.length; i++)
				{
					messageMap.putOne(mi[i], inSignal);
					boolean last = i + 1 == mi.length;
					getDispatcher().triggerChannel(mi[i].misChannelID, qeID, null, -1, last, true);
				}
			}
		}
		else
		{
			if (logCounter < 10 || logCounter % 100 == 0)
			{
				log.info("No consumer for buffered Signal# " + logCounter + " " + inSignal.getType() + " ID=" + inSignal.getID());
			}
		}
		logCounter++;
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////

	/**
	 * buffers status messages and consolidates the amounts
	 */
	public static class StatusBufferHandler extends JMFBufferHandler
	{
		/**
		 * @return
		 */
		@Override
		protected Set<MessageIdentifier> getMessageIdentifierSet()
		{
			final Set<MessageIdentifier> keySet = new HashSet<MessageIdentifier>();
			synchronized (messageMap)
			{
				keySet.addAll(messageMap.keySet());
			}
			synchronized (lastSent)
			{
				keySet.addAll(lastSent.keySet());
			}
			return keySet;
		}

		private final HashMap<MessageIdentifier, JDFSignal> lastSent;

		/**
		 * return true if the signal corresponds to the input query
		 * @param inputMessage the query to check against
		 * @param signal the signal to check
		 * @return true if matches
		 */
		@Override
		protected boolean isMySignal(final JDFMessage inputMessage, final JDFSignal signal)
		{
			final JDFStatusQuParams sqp = inputMessage.getStatusQuParams();

			if (sqp == null) // no filter just keep it
			{
				return true;
			}
			JDFStatusQuParams sqpSig = signal.getStatusQuParams();
			if (sqpSig == null)
			{
				sqpSig = sqp;
			}

			final NodeIdentifier sqpIdentifier = sqpSig.getIdentifier();
			if (!sqpIdentifier.matches(sqp.getIdentifier()))
			{
				return false;
			}

			final JDFDeviceInfo di = signal.getDeviceInfo(0);
			if (di != null)
			{
				if (!sqpIdentifier.equals(new NodeIdentifier()))
				{
					final Vector<JDFJobPhase> vjp = di.getChildrenByClass(JDFJobPhase.class, false, 0);
					boolean bMatch = false;
					if (vjp != null)
					{
						for (JDFJobPhase jp : vjp)
						{
							if (jp.getIdentifier().matches(sqp.getIdentifier()) || ContainerUtil.equals(sqp.getQueueEntryID(), jp.getQueueEntryID()))
							{
								bMatch = true;
								break;
							}
						}
					}
					if (!bMatch)
					{
						return false;
					}
				}
			}
			updateQueue(sqp, signal);
			return true;
		}

		/**
		 * @param sqp
		 * @param s
		 */
		private void updateQueue(final JDFStatusQuParams sqp, final JDFSignal s)
		{
			// remove unwanted queue elements
			boolean queueInfo = false;
			if (sqp != null)
			{
				queueInfo = sqp.getQueueInfo();
			}
			if (!queueInfo)
			{
				final JDFQueue signalQueue = s.getQueue(0);
				if (signalQueue != null)
				{
					signalQueue.deleteNode();
				}
			}
		}

		/**
		 * @param dev
		 */
		public StatusBufferHandler(final AbstractProxyDevice dev)
		{
			super(dev, EnumType.Status, new EnumFamily[] { EnumFamily.Signal, EnumFamily.Query }, dev);
			lastSent = new HashMap<MessageIdentifier, JDFSignal>();
		}

		/**
		 * @param inputMessage
		 * @param response
		 * @return true if handled
		 */
		@Override
		protected boolean handleQuery(final JDFMessage inputMessage, final JDFResponse response)
		{
			boolean isSubscription = response.getSubscribed();
			boolean deleteResponse = !isSubscription;
			if (!isSubscription)
			{
				getSignals(inputMessage, response);
			}
			JDFStatusQuParams sqp = inputMessage.getStatusQuParams();
			if (sqp != null && sqp.getQueueInfo())
			{
				deleteResponse = false;
				_theDevice.addQueueToStatusResponse(inputMessage, response);
			}

			if (deleteResponse)
			{
				response.deleteNode();// zapp the dummy response except in a subscription
			}

			if (isSubscription)
			{
				inputMessage.deleteNode(); // also zapp the query
			}
			return true;
		}

		@Override
		protected boolean handleSignal(final JDFSignal theSignal, final JDFResponse response)
		{
			if (theSignal == null)
			{
				return false;
			}
			final VElement vSigs = splitSignals(theSignal);
			for (int i = 0; i < vSigs.size(); i++)
			{
				final JDFSignal inSignal = (JDFSignal) vSigs.get(i);
				final String qeID = getQueueEntryIDForSignal(inSignal);
				final Set<String> requests = getDispatcher().getChannels(theSignal.getEnumType(), inSignal.getSenderID(), qeID);
				final MessageIdentifier[] messageIdentifiers = new MessageIdentifier(inSignal, null).cloneChannels(requests);
				if (messageIdentifiers != null)
				{
					for (MessageIdentifier mi : messageIdentifiers)
					{
						dispatchSingleSignal(inSignal, qeID, mi);
					}
				}
			}

			return true;
		}

		protected void dispatchSingleSignal(final JDFSignal inSignal, final String qeID, MessageIdentifier mi)
		{
			JDFSignal lastSignal;
			synchronized (lastSent) // we don't need any races here
			{
				lastSignal = lastSent.get(mi);
			}
			handleSingleSignal(inSignal, mi);
			StatusSignalComparator comparator = getComparator();
			boolean sameStatusSignal = comparator != null && comparator.isSameStatusSignal(inSignal, lastSignal);
			getDispatcher().triggerChannel(mi.misChannelID, qeID, null, -1, false, sameStatusSignal);
			synchronized (lastSent)
			{
				lastSent.put(mi, inSignal);
			}
		}

		protected void handleSingleSignal(final JDFSignal inSignal, final MessageIdentifier mi)
		{
			synchronized (messageMap)
			{
				final JDFSignal last = messageMap.getOne(mi, -1);
				if (last == null)
				{
					fixIdleTime(inSignal);
					messageMap.putOne(mi, inSignal);
				}
				else
				{
					final StatusSignalComparator comparator = getComparator();
					final boolean bAllSame = comparator != null && comparator.isSameStatusSignal(inSignal, last);
					if (bAllSame)
					{
						comparator.mergeStatusSignal(inSignal, last);
						messageMap.setOne(mi, inSignal, last);
					}
					else
					{
						fixIdleTime(inSignal);
						messageMap.putOne(mi, inSignal);
					}
				}
			}
		}

		protected StatusSignalComparator getComparator()
		{
			return new StatusSignalComparator();
		}

		/**
		 * @param inSignal
		 */
		private void fixIdleTime(final JDFSignal inSignal)
		{
			final int n = inSignal.numChildElements(ElementName.DEVICEINFO, null);
			for (int i = n - 1; i >= 0; i--)
			{
				final JDFDeviceInfo di = inSignal.getDeviceInfo(i);
				final EnumDeviceStatus st = di.getDeviceStatus();
				if (EnumDeviceStatus.Idle.equals(st))
				{
					final String idle = StringUtil.getNonEmpty(di.getAttribute(AttributeName.IDLESTARTTIME));
					if (idle == null)
					{
						di.setIdleStartTime(inSignal.getTime());
					}
				}
			}
		}

		/**
		 * @param mi
		 * 
		 * @return
		 */
		@Override
		protected Vector<JDFSignal> getSignalsFromMap(final MessageIdentifier mi)
		{
			Vector<JDFSignal> sis = messageMap.get(mi);
			// only add ever so often...
			if (sis == null || sis.size() == 0)
			{
				synchronized (lastSent)
				{
					final JDFSignal lastSig = lastSent.get(mi);
					if (lastSig != null)
					{
						final int n = lastSig.numChildElements(ElementName.DEVICEINFO, null);
						boolean bIdle = n > 0;
						for (int i = n - 1; i >= 0; i--)
						{
							final JDFDeviceInfo di = lastSig.getDeviceInfo(i);
							final EnumDeviceStatus st = di.getDeviceStatus();
							if (!EnumDeviceStatus.Idle.equals(st))
							{
								bIdle = false;
								break;
							}
						}
						if (bIdle)
						{

							if (sis == null)
							{
								sis = new Vector<JDFSignal>();
							}
							lastSig.setTime(new JDFDate());
							lastSig.appendAnchor(null);
							sis.add(lastSig);
						}
					}
				}
			}
			if (sis != null && sis.size() == 0)
			{
				sis = null;
			}
			if (sis != null)
			{
				Vector<JDFSignal> clone = new Vector<JDFSignal>();
				clone.addAll(sis);
				sis = clone;
			}
			return sis;
		}

		/**
		 * split signals by originating device (senderID)
		 * @param theSignal
		 * @return the vector of signals
		 */
		protected VElement splitSignals(final JDFSignal theSignal)
		{
			final VElement devInfos = theSignal.getChildElementVector(ElementName.DEVICEINFO, null);
			final VElement sigs = new VElement();
			sigs.add(theSignal);
			if (devInfos.size() == 1)
			{
				theSignal.setSenderID(((JDFDeviceInfo) devInfos.get(0)).getDeviceID());
			}
			else
			{
				final String senderID = theSignal.getSenderID();
				for (int i = 0; i < devInfos.size(); i++)
				{
					final JDFDeviceInfo di = (JDFDeviceInfo) devInfos.get(i);
					final String did = di.getDeviceID();
					if (!ContainerUtil.equals(did, senderID))
					{
						JDFSignal s = null;
						for (int ii = 1; ii < sigs.size(); ii++)
						{
							final JDFSignal s2 = (JDFSignal) sigs.get(ii);
							if (ContainerUtil.equals(s2.getSenderID(), did))
							{
								s = s2;
								break;
							}
						}
						if (s == null)
						{
							s = JDFJMF.createJMF(EnumFamily.Signal, EnumType.Status).getSignal(0);
							s.copyElement(theSignal.getQueue(0), null);
							sigs.add(s);
						}
						s.setSenderID(did);
						s.setTime(theSignal.getTime());
						s.moveElement(di, null);
					}
				}
			}
			if (theSignal.numChildElements(ElementName.DEVICEINFO, null) == 0)
			{
				sigs.remove(0);
			}
			return sigs;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#getQueueEntryIDForSignal(org.cip4.jdflib.jmf.JDFSignal)
		 */
		@Override
		protected String getQueueEntryIDForSignal(final JDFSignal inSignal)
		{
			String qeid = null;
			NodeIdentifier ni = null;
			final JDFStatusQuParams sqp = inSignal.getStatusQuParams();
			if (sqp != null)
			{
				qeid = StringUtil.getNonEmpty(sqp.getQueueEntryID());
				ni = sqp.getIdentifier();
			}
			if (qeid == null && ni == null)
			{
				final JDFDeviceInfo di = inSignal.getDeviceInfo(0);
				for (int i = 0; true; i++)
				{
					final JDFJobPhase jp = di.getJobPhase(i);
					if (jp == null)
					{
						break;
					}
					qeid = StringUtil.getNonEmpty(jp.getQueueEntryID());
					ni = jp.getIdentifier();
					if (qeid != null || ni != null)
					{
						break;
					}
				}
			}
			final JDFQueueEntry qe = getQueueProcessor().getQueueEntry(qeid, ni);
			return qe == null ? null : qe.getQueueEntryID();
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 04.12.2008
	 */
	public static class NotificationBufferHandler extends JMFBufferHandler
	{

		/**
		 * @param dev
		 */
		public NotificationBufferHandler(final AbstractDevice dev)
		{
			super(dev, EnumType.Notification, new EnumFamily[] { EnumFamily.Signal, EnumFamily.Query }, dev);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#getQueueEntryIDForSignal(org.cip4.jdflib.jmf.JDFSignal)
		 */
		@Override
		protected String getQueueEntryIDForSignal(final JDFSignal inSignal)
		{
			NodeIdentifier ni = null;
			final JDFNotification not = inSignal.getNotification();
			if (not != null)
			{
				ni = not.getIdentifier();
			}
			final JDFQueueEntry qe = _theDevice.getQueueProcessor().getQueueEntry(null, ni);
			return qe == null ? null : qe.getQueueEntryID();
		}

	}

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 04.12.2008
	 */
	public static class ResourceBufferHandler extends JMFBufferHandler
	{

		/**
		 * @param dev
		 */
		public ResourceBufferHandler(final AbstractDevice dev)
		{
			super(dev, EnumType.Resource, new EnumFamily[] { EnumFamily.Signal, EnumFamily.Query }, dev);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#isMySignal(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFSignal)
		 */
		@Override
		protected boolean isMySignal(final JDFMessage inputMessage, final JDFSignal signal)
		{
			final JDFResourceQuParams rqp = inputMessage.getResourceQuParams();
			if (rqp == null)
			{
				return true;
			}

			final JDFResourceQuParams rqpSig = signal.getResourceQuParams();

			final NodeIdentifier niSig = rqpSig == null ? null : rqpSig.getIdentifier();
			final NodeIdentifier ni = rqp.getIdentifier();
			final NodeIdentifier nullID = new NodeIdentifier();
			if (niSig == null || nullID.equals(niSig))
			{
				return nullID.equals(ni);
			}
			return niSig.matches(ni);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#getQueueEntryIDForSignal(org.cip4.jdflib.jmf.JDFSignal)
		 */
		@Override
		protected String getQueueEntryIDForSignal(final JDFSignal inSignal)
		{
			String qeid = null;
			NodeIdentifier ni = null;
			final JDFResourceQuParams rqp = inSignal.getResourceQuParams();
			if (rqp != null)
			{
				qeid = StringUtil.getNonEmpty(rqp.getQueueEntryID());
				ni = rqp.getIdentifier();
			}
			final JDFQueueEntry qe = _theDevice.getQueueProcessor().getQueueEntry(qeid, ni);
			return qe == null ? null : qe.getQueueEntryID();
		}
	}

	/**
	 * handler for the Notification Query
	 */
	public static class NotificationHandler extends NotificationBufferHandler
	{

		StatusListener theStatusListener;

		/**
		 * @param listener
		 * @param dev
		 */
		public NotificationHandler(final AbstractDevice dev, StatusListener listener)
		{
			super(dev);
			theStatusListener = listener;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			if (theStatusListener == null)
			{
				return false;
			}
			if (!theStatusListener.matchesQuery(inputMessage))
			{
				return false;
			}

			final JDFDoc notification = theStatusListener.getStatusCounter().getDocJMFNotification(true);
			if (notification != null) // fills the buffer
			{
				final JDFJMF jmf = notification.getJMFRoot();
				final VElement v = jmf.getMessageVector(EnumFamily.Signal, EnumType.Notification);
				if (v != null)
				{
					final int siz = v.size();
					for (int i = 0; i < siz; i++)
					{
						super.handleMessage((JDFMessage) v.get(i), jmf.getCreateResponse(0));
					}
				}
			}
			return super.handleMessage(inputMessage, response);
		}
	}
}
