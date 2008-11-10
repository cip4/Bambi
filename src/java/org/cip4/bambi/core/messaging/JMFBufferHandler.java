/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.SignalDispatcher;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
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
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.VectorMap;

/**
 * Class that buffers messages for subscriptions and integrates the results over time
 * @author rainer prosi
 */
public class JMFBufferHandler extends AbstractHandler implements IMessageHandler
{
	/**
	 * class that identifies messages. if equal, messages are integrated, else they are retained independently
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected static class MessageIdentifier implements Cloneable
	{
		protected String misChannelID = null;
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

	protected static final Log log = LogFactory.getLog(JMFBufferHandler.class.getName());
	protected VString ignoreSenderIDs = null;

	protected VectorMap<MessageIdentifier, JDFSignal> messageMap = new VectorMap<MessageIdentifier, JDFSignal>();
	protected SignalDispatcher _theDispatcher;

	/**
	 * @param _type
	 * @param _families
	 */
	public JMFBufferHandler(final String typ, final EnumFamily[] _families, final SignalDispatcher dispatcher)
	{
		super(typ, _families);
		_theDispatcher = dispatcher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage , org.cip4.jdflib.jmf.JDFMessage)
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
			if (ignore(inputMessage))
			{
				return true;
			}
			return handleSignal((JDFSignal) inputMessage, response);
		}
		else if (EnumFamily.Query.equals(family))
		{
			final JDFJMF jmf = getSignals(inputMessage, response);
			return true;
		}
		return false;
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
		for (int i = 0; i < ignoreSenderIDs.size(); i++)
		{
			if (senderID.indexOf(ignoreSenderIDs.get(i)) >= 0)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param ignoreSenderIDs
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
			final Iterator<MessageIdentifier> it = keySet.iterator();
			JDFJMF jmf = response.getJMFRoot();
			final Vector<MessageIdentifier> v = new Vector<MessageIdentifier>();

			while (it.hasNext())
			{
				final MessageIdentifier mi = it.next();
				if (mi == null)
				{
					log.error("null mi");
					continue;
				}
				if (mi.matches(messageIdentifier))
				{
					v.add(mi);
					final Vector<JDFSignal> sis = getSignalsFromMap(mi);
					final int size = sis == null ? 0 : sis.size();
					for (int i = 0; i < size; i++)
					{
						// copy the potentially inherited senderID
						final JDFSignal signal = sis.get(i);
						final JDFSignal sNew = (JDFSignal) jmf.copyElement(signal, null);
						sNew.setSenderID(signal.getSenderID());
						sNew.copyAttribute(AttributeName.REFID, inputMessage, AttributeName.ID, null, null);
					}
				}
			}
			if (v.size() > 0)
			{
				for (int i = 0; i < v.size(); i++)
				{
					messageMap.remove(v.get(i));
				}
			}
			else
			{
				jmf = null;
			}

			if (!response.getSubscribed())
			{
				response.deleteNode();// always zapp the dummy response except
				// in a subscription
			}

			inputMessage.deleteNode(); // also zapp the query
			return jmf;
		}
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
		final Vector<JDFSignal> sis = messageMap.get(mi);
		return sis;
	}

	/**
	 * @param inSignal
	 * @param response
	 * @return true if handled
	 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#handleSignal(org.cip4.jdflib.jmf.JDFSignal, org.cip4.jdflib.jmf.JDFResponse)
	 */
	protected boolean handleSignal(final JDFSignal inSignal, final JDFResponse response)
	{
		final Set<String> requests = _theDispatcher.getChannels(inSignal.getEnumType(), inSignal.getSenderID());
		final MessageIdentifier[] mi = new MessageIdentifier(inSignal, null).cloneChannels(requests);

		if (mi != null)
		{
			synchronized (messageMap)
			{
				for (int i = 0; i < mi.length; i++)
				{
					messageMap.putOne(mi[i], inSignal);
					_theDispatcher.triggerChannel(mi[i].misChannelID, null, null, -1, i + 1 == mi.length, true);
				}
				return true;
			}
		}
		return false;
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
			keySet.addAll(messageMap.keySet());
			keySet.addAll(lastSent.keySet());
			return keySet;
		}

		private final HashMap<MessageIdentifier, JDFSignal> lastSent;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#getSignals(org.cip4 .jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		protected JDFJMF getSignals(final JDFMessage inputMessage, final JDFResponse response)
		{
			final JDFStatusQuParams sqp = inputMessage.getStatusQuParams();
			boolean queueInfo = false;
			if (sqp != null)
			{
				queueInfo = sqp.getQueueInfo();
			}

			final JDFJMF jmf = super.getSignals(inputMessage, response);
			if (jmf == null)
			{
				return jmf;
			}
			final VElement sigs = jmf.getMessageVector(EnumFamily.Signal, EnumType.Status);
			final int sigSize = sigs == null ? 0 : sigs.size();
			for (int i = 0; i < sigSize; i++)
			{
				final JDFSignal s = (JDFSignal) sigs.get(i);
				if (sqp != null)
				{
					final JDFStatusQuParams sqpSig = s.getStatusQuParams();
					final NodeIdentifier sqpIdentifier = sqpSig == null ? null : sqpSig.getIdentifier();
					if (sqpSig != null && !sqpIdentifier.matches(sqp.getIdentifier()))
					{
						s.deleteNode();
						continue;
					}
					else if (sqpIdentifier == null || sqpIdentifier.equals(new NodeIdentifier()))
					{
						continue; // no filter
					}
					final JDFDeviceInfo di = s.getDeviceInfo(0);
					if (di != null)
					{
						final VElement vjp = di.getChildElementVector(ElementName.JOBPHASE, null);
						final int siz = vjp == null ? 0 : vjp.size();
						boolean bMatch = false;
						for (int j = 0; j < siz; j++)
						{
							final JDFJobPhase jp = (JDFJobPhase) vjp.get(j);
							if (jp.getIdentifier().matches(sqp.getIdentifier()) || ContainerUtil.equals(sqp.getQueueEntryID(), jp.getQueueEntryID()))
							{
								bMatch = true;
								break;
							}

						}
						if (!bMatch)
						{
							s.deleteNode();
							continue;
						}
					}
				}
				// remove unwanted queue elements
				if (!queueInfo)
				{
					final JDFQueue signalQueue = s.getQueue(0);
					if (signalQueue != null)
					{
						signalQueue.deleteNode();
					}
				}
			}
			return jmf;
		}

		/**
		 * @param dispatcher the message dispatcher that sends out signals
		 */
		public StatusBufferHandler(final SignalDispatcher dispatcher)
		{
			super("Status", new EnumFamily[]
			{ EnumFamily.Signal, EnumFamily.Query }, dispatcher);
			lastSent = new HashMap<MessageIdentifier, JDFSignal>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFBufferHandler#handleSignal(org.cip4 .jdflib.jmf.JDFSignal, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		protected boolean handleSignal(final JDFSignal theSignal, final JDFResponse response)
		{
			final Set<String> requests = _theDispatcher.getChannels(theSignal.getEnumType(), theSignal.getSenderID());
			final VElement vSigs = splitSignals(theSignal);
			for (int i = 0; i < vSigs.size(); i++)
			{
				final JDFSignal inSignal = (JDFSignal) vSigs.get(i);
				final MessageIdentifier[] mi = new MessageIdentifier(inSignal, null).cloneChannels(requests);
				if (mi != null)
				{
					for (int ii = 0; ii < mi.length; ii++)
					{
						JDFSignal lastSignal;
						synchronized (lastSent) // we don't need any races here
						{
							lastSignal = lastSent.get(mi[ii]);
						}
						handleSingleSignal(inSignal, mi[ii]);
						_theDispatcher.triggerChannel(mi[ii].misChannelID, null, null, -1, false, isSameStatusSignal(inSignal, lastSignal));
						synchronized (lastSent)
						{
							lastSent.put(mi[ii], inSignal);

						}
					}
				}
			}

			return true;
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
					final boolean bAllSame = isSameStatusSignal(inSignal, last);
					if (bAllSame)
					{
						mergeStatusSignal(inSignal, last);
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
						di.setIdleStartTime(null);
					}
				}
			}
		}

		/**
		 * @param mi
		 * @return
		 */
		@Override
		protected Vector<JDFSignal> getSignalsFromMap(final MessageIdentifier mi)
		{
			Vector<JDFSignal> sis = messageMap.get(mi);
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
		 * @param inSignal
		 * @param last
		 */
		private void mergeStatusSignal(final JDFSignal inSignal, final JDFSignal last)
		{
			for (int i = 0; true; i++)
			{
				final JDFDeviceInfo di = inSignal.getDeviceInfo(i);
				if (di == null)
				{
					break;
				}
				boolean bSameDI = false;
				for (int j = 0; !bSameDI; j++)
				{
					final JDFDeviceInfo diLast = last.getDeviceInfo(j);
					if (diLast == null)
					{
						break;
					}
					bSameDI = di.mergeLastPhase(diLast);
				}
			}
		}

		/**
		 * @param inSignal
		 * @param last
		 * @return true if the signals are equivalent
		 */
		private boolean isSameStatusSignal(final JDFSignal inSignal, final JDFSignal last)
		{
			if (last == null)
			{
				return inSignal == null;
			}
			boolean bAllSame = true;
			for (int i = 0; bAllSame; i++)
			{
				final JDFDeviceInfo di = inSignal.getDeviceInfo(i);
				if (di == null)
				{
					break;
				}
				boolean bSameDI = false;
				for (int j = 0; !bSameDI; j++)
				{
					final JDFDeviceInfo diLast = last.getDeviceInfo(j);
					if (diLast == null)
					{
						break;
					}
					bSameDI = di.isSamePhase(diLast, false);
				}
				bAllSame = bAllSame && bSameDI;
			}
			return bAllSame;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////

	public static class NotificationBufferHandler extends JMFBufferHandler
	{

		public NotificationBufferHandler(final SignalDispatcher dispatcher)
		{
			super("Notification", new EnumFamily[]
			{ EnumFamily.Signal, EnumFamily.Query }, dispatcher);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////

	public static class ResourceBufferHandler extends JMFBufferHandler
	{

		public ResourceBufferHandler(final SignalDispatcher dispatcher)
		{
			super("Resource", new EnumFamily[]
			{ EnumFamily.Signal, EnumFamily.Query }, dispatcher);
		}
	}

	/**
	 * handler for the Status Query
	 */
	public static class NotificationHandler extends NotificationBufferHandler
	{

		StatusListener theStatusListener;

		public NotificationHandler(final SignalDispatcher dispatcher, final StatusListener listener)
		{
			super(dispatcher);
			theStatusListener = listener;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf. JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
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
				final int siz = v == null ? 0 : v.size();
				for (int i = 0; i < siz; i++)
				{
					super.handleMessage((JDFMessage) v.get(i), jmf.getCreateResponse(0));
				}
			}
			return super.handleMessage(inputMessage, response);
		}
	}
}
