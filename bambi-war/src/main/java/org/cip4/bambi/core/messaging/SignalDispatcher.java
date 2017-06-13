/**
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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
 * copyright (c) 1999-2006, Heidelberger Druckmaschinen AG
 * copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 *
 * For more information on The International Cooperation for the
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core.messaging;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFSubscriptionFilter;
import org.cip4.jdflib.jmf.JDFSubscriptionInfo;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.pool.JDFAncestorPool;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * this class handles subscriptions <br>
 *
 * @author rainer prosi
 */
public class SignalDispatcher extends BambiLogFactory
{
	protected final HashMap<String, MsgSubscription> subscriptionMap; // map of slaveChannelID / Subscription
	private final SubscriptionStore storage;
	protected final Vector<Trigger> triggers;
	protected final MyMutex mutex;
	private boolean doShutdown;
	protected final AbstractDevice device;
	private int lastCalled;
	protected final Dispatcher theDispatcher;
	private String ignoreURL;

	/**
	 * set the case insensitive url pattern to be ignored for subscriptions
	 * @param _ignoreURL the pattern to ignore;
	 */
	public void setIgnoreURL(final String _ignoreURL)
	{
		this.ignoreURL = _ignoreURL != null ? _ignoreURL.toLowerCase() : null;
	}

	/**
	 * @author prosirai
	 */
	protected class XMLSubscriptions extends XMLDoc implements IGetHandler
	{
		private final KElement root;

		/**
		 * XML representation of this simDevice for use as html display using an XSLT
		 */
		public XMLSubscriptions()
		{
			super("SubscriptionList", null);
			root = getRoot();
		}

		/**
		 *
		 * @param request
		 * @return
		 */
		@Override
		public XMLResponse handleGet(final ContainerRequest request)
		{
			final boolean bStopChannel = request.getBooleanParam("StopChannel");
			if (bStopChannel)
			{
				stopChannel(request);
			}
			final boolean bStopSender = request.getBooleanParam("StopSender");
			if (bStopSender)
			{
				stopSender(request);
			}
			final String pause = request.getParameter("pause");
			if (StringUtil.isBoolean(pause))
			{
				pauseResumeSender(request);
			}
			final boolean bFlushSender = request.getBooleanParam("FlushSender");
			if (bFlushSender)
			{
				flushSender(request);
			}
			final boolean bZappFirst = request.getBooleanParam("ZappFirst");
			if (bZappFirst)
			{
				zappFirstMessage(request);
			}
			final String details = request.getParameter("DetailID");

			final int pos = request.getIntegerParam("pos");
			if (details == null)
			{
				final boolean bListSenders = request.getBooleanParam("ListSenders");
				setXMLRoot(request);
				if (bListSenders)
				{
					listMessageSenders(request, true, pos);
					if (pos > 0)
					{
						setXSLTURL(device.getXSLTBaseFromContext(request.getContextRoot()) + "/subscriptionDetails.xsl");
					}
					else
					{
						setXSLTURL(device.getXSLTBaseFromContext(request.getContextRoot()) + "/subscriptionList.xsl");
					}
				}
				else
				{
					listChannels(request, details);
					device.addMoreToXMLSubscriptions(root);
					listMessageSenders(request, false, -1);
					setXSLTURL(device.getXSLTBaseFromContext(request.getContextRoot()) + "/subscriptionList.xsl");
				}
			}
			else
			{
				showDetails(request, details, pos);
				setXSLTURL(device.getXSLTBaseFromContext(device.getContext(request)) + "/subscriptionDetails.xsl");
			}
			XMLResponse r = new XMLResponse(getRoot());
			return r;

		}

		/**
		 * @param request
		 */
		private void pauseResumeSender(final ContainerRequest request)
		{
			final String url = request.getParameter(AttributeName.URL);
			final Vector<MessageSender> v = device.getJMFFactory().getMessageSenders(url);
			final boolean pause = request.getBooleanParam("pause");
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final MessageSender messageSender = v.get(i);
					if (pause)
					{
						messageSender.pause();
					}
					else
					{
						messageSender.resume();
					}
				}
			}
		}

		/**
		 * @param request
		 */
		private void stopSender(final ContainerRequest request)
		{
			final String url = request.getParameter(AttributeName.URL);
			final JMFFactory factory = device.getJMFFactory();
			final Vector<MessageSender> v = factory.getMessageSenders(url);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final MessageSender messageSender = v.get(i);

					messageSender.shutDown(true);
				}
			}
		}

		/**
		 * @param request
		 */
		private void flushSender(final ContainerRequest request)
		{
			final String url = request.getParameter(AttributeName.URL);
			final Vector<MessageSender> v = device.getJMFFactory().getMessageSenders(url);
			if (v != null)
			{
				for (MessageSender messageSender : v)
				{
					messageSender.flushMessages();
				}
			}
		}

		/**
		 * @param request
		 */
		private void zappFirstMessage(final ContainerRequest request)
		{
			final String url = request.getParameter(AttributeName.URL);
			final Vector<MessageSender> v = device.getJMFFactory().getMessageSenders(url);
			if (v != null)
			{
				for (MessageSender messageSender : v)
				{
					messageSender.zappFirstMessage();
				}
			}
		}

		/**
		 * @param request
		 * @param bListSenders
		 * @param pos
		 */
		private void listMessageSenders(final ContainerRequest request, final boolean bListSenders, final int pos)
		{
			final String url = request.getParameter(AttributeName.URL);
			final URL myURL = UrlUtil.stringToURL(url);
			if (myURL == null || pos < 0)
			{
				final Vector<MessageSender> v = device.getJMFFactory().getMessageSenders(null);
				if (v != null)
				{
					for (final MessageSender ms : v)
					{
						ms.appendToXML(root, pos, true);
					}
				}
			}
			else
			{
				final MessageSender ms = device.getJMFFactory().getCreateMessageSender(url);
				if (ms != null)
				{
					ms.appendToXML(root, pos, false);
				}
				else
				{
					log.warn("No MessageSender for URL: " + url);
				}
			}
		}

		/**
		 * @param request
		 * @param details
		 */
		public void listChannels(final ContainerRequest request, final String details)
		{
			final Vector<MsgSubscription> v = ContainerUtil.toValueVector(subscriptionMap, true);
			if (v != null)
			{
				for (final MsgSubscription ms : v)
				{
					ms.appendToXML(root, details, -1);
				}
			}
		}

		/**
		 * @param request
		 */
		protected void setXMLRoot(final ContainerRequest request)
		{
			if (device != null) // may be null in test scenarios
				root.setAttribute(AttributeName.DEVICEID, device.getDeviceID());
			root.setAttribute(AttributeName.CONTEXT, ((request == null) ? null : device.getContext(request)));
		}

		/**
		 * @param request the http request to handle
		 * @param details
		 * @param pos
		 */
		private void showDetails(final ContainerRequest request, final String details, final int pos)
		{
			final MsgSubscription sub = subscriptionMap.get(details);
			setXMLRoot(request);
			if (sub != null)
			{
				sub.appendToXML(root, details, pos);
			}
		}

		/**
		 * @param request
		 */
		private void stopChannel(final ContainerRequest request)
		{
			final String channelID = request.getParameter(AttributeName.CHANNELID);
			if (channelID == null)
			{
				return;
			}
			final MsgSubscription sub = removeSubScription(channelID);
			if (sub != null)
			{
				final KElement e = root.appendElement("RemovedChannel");
				sub.setXML(e, null, -1);
			}
		}
	}

	// ///////////////////////////////////////////////////////////
	protected class Dispatcher implements Runnable
	{
		int sentMessages;
		int sentTime;
		int sentTrigger;
		private final CPUTimer timer;

		/**
		 *
		 */
		public Dispatcher()
		{
			super();
			sentMessages = 0;
			sentTime = 0;
			sentTrigger = 0;
			timer = new CPUTimer(false);
		}

		/**
		 * this is the time clocked dispatcher thread
		 */
		@Override
		public void run()
		{
			while (!doShutdown)
			{
				try
				{
					flush();
				}
				catch (final Throwable x)
				{
					log.error("unhandled Exception in flush", x);
					timer.stop();
				}
				if (!ThreadUtil.wait(mutex, 10000))
				{
					doShutdown = true;
				}
			}
		}

		/**
		 *
		 */
		void flush()
		{
			timer.start();
			final Vector<MsgSubscription> triggerVector = getTriggerSubscriptions();
			// spam them out
			for (MsgSubscription sub : triggerVector)
			{
				if ((sentTrigger++ < 10) || ((sentTrigger % 1000) == 0))
				{
					log.info("Trigger Signalling : slaveChannelID=" + sub.channelID + " #" + sentTrigger);
				}
				queueMessageInSender(sub);
			}
			// select pending time subscriptions
			final Vector<MsgSubscription> subVector = getTimeSubscriptions();
			// spam them out
			for (MsgSubscription sub : subVector)
			{
				if ((sentTime++ < 10) || ((sentTime % 1000) == 0))
				{
					log.info("Time Signalling: slaveChannelID=" + sub.channelID + " #" + sentTime);
				}

				queueMessageInSender(sub);
			}
			timer.stop();
		}

		/**
		 * queue a message in the appropriate sender
		 * @param sub
		 */
		private void queueMessageInSender(final MsgSubscription sub)
		{
			try
			{
				final String url = sub.getURL();
				final JDFJMF signalJMF = sub.getSignal();
				if (signalJMF != null)
				{
					boolean ok = device.getJMFFactory().send2URL(signalJMF, url, null, sub.getCallback(), device.getDeviceID());
					if (!ok)
					{
						checkStaleSubscription(sub);
					}
					final MsgSubscription realSubSubscription = subscriptionMap.get(sub.channelID);
					if (realSubSubscription != null)
					{
						realSubSubscription.lastTime = System.currentTimeMillis() / 1000;
						realSubSubscription.lastSentJMF.push(signalJMF);
						realSubSubscription.sentMessages++;
					}
					sentMessages++;
					if ((sentMessages < 10) || ((sentMessages % 1000) == 0))
					{
						log.info("Sent message# " + sentMessages + " " + timer.getSingleSummary());
					}
				}
			}
			catch (Throwable t)
			{
				log.error("Exception while queueing message", t);
				//cool down
				ThreadUtil.sleep(4242);
			}

			// also notify that the trigger was processed in case of failure - else we wait a long time...
			if (sub.trigger != null)
			{
				sub.trigger.setQueued();
			}
		}

		/**
		 * get the triggered subscriptions, either forced (amount=-1) or by amount
		 * @return the vector of triggered subscriptions
		 */
		private Vector<MsgSubscription> getTriggerSubscriptions()
		{
			synchronized (triggers)
			{
				final Vector<MsgSubscription> v = new Vector<MsgSubscription>();
				final Vector<Trigger> vSnafu = new Vector<Trigger>();
				final Iterator<Trigger> it = triggers.iterator(); // active triggers
				while (it.hasNext())
				{
					final Trigger t = it.next();
					final String channelID = t.channelID;
					final MsgSubscription sub = subscriptionMap.get(channelID);
					if (sub == null)
					{
						vSnafu.add(t);
						continue; // snafu
					}
					final MsgSubscription subClone = sub.clone();
					subClone.trigger = t;

					if (t.amount < 0)
					{
						v.add(subClone);
					}
					else if (t.amount > 0)
					{
						if (subClone.repeatAmount > 0)
						{
							final int last = subClone.lastAmount;
							final int next = last + t.amount;
							if (next / sub.repeatAmount > last / sub.repeatAmount)
							{
								sub.lastAmount = next; // not a typo - modify of the original subscription
								v.add(subClone);
							}
						}
					}
				}
				// remove active triggers that will be returned
				for (Trigger t : vSnafu)
				{
					final boolean b = triggers.remove(t);
					if (!b)
					{
						log.error("Snafu removing trigger");
					}
					else
					{
						log.info("removing spurious trigger");
					}

				}
				for (MsgSubscription sub : v)
				{
					final boolean b = triggers.remove(sub.trigger);
					if (!b)
					{
						log.error("Snafu removing trigger");
					}
				}
				return v;
			}
		}

		private Vector<MsgSubscription> getTimeSubscriptions()
		{
			final Vector<MsgSubscription> subVector = new Vector<MsgSubscription>();
			synchronized (subscriptionMap)
			{
				final Iterator<Entry<String, MsgSubscription>> it = subscriptionMap.entrySet().iterator();
				final long now = System.currentTimeMillis() / 1000;
				while (it.hasNext())
				{
					final Entry<String, MsgSubscription> next = it.next();
					MsgSubscription sub = next.getValue();
					if (sub.repeatTime > 0)
					{
						if (now - sub.lastTry >= sub.repeatTime)
						{
							sub.lastTry += sub.repeatTime;
							// we had a snafu with a long break or are in setup - synchronize to now
							if (sub.lastTry < now)
							{
								sub.lastTry = now;
							}
							sub = sub.clone();
							sub.trigger = null;
							subVector.add(sub);
						}
					}
				}
			} // end synch map
			return subVector;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "Dispatcher [sentMessages=" + sentMessages + ", sentTime=" + sentTime + ", sentTrigger=" + sentTrigger + "]";
		}
	}

	/**
	 * handler for the StopPersistentChannel command
	 */
	public class StopPersistentChannelHandler extends AbstractHandler
	{
		/**
		 *
		 */
		public StopPersistentChannelHandler()
		{
			super(EnumType.StopPersistentChannel, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param inputMessage
		 * @param response
		 * @return true if handled
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			if (!EnumType.StopPersistentChannel.equals(inputMessage.getEnumType()))
			{
				return false;
			}
			final JDFStopPersChParams spcp = inputMessage.getStopPersChParams(0);
			if (spcp == null)
			{
				JMFHandler.errorResponse(response, "No StopPersistentChannelParams specified", 7, EnumClass.Error);
				return true;
			}
			final String channel = spcp.getChannelID();
			log.info("Handling StopPersistantChannel JMF for channel: " + channel);
			if (!KElement.isWildCard(channel))
			{
				final MsgSubscription mSub = removeSubScription(channel);
				if (response != null)
				{
					addToResponse(response, mSub);
				}
				return true;
			}
			final String url = spcp.getURL();
			if (KElement.isWildCard(url))
			{
				JMFHandler.errorResponse(response, "No URL specified", 7, EnumClass.Error);
				return true;
			}
			String queueEntryID = spcp.getQueueEntryID();
			if (KElement.isWildCard(queueEntryID))
			{
				queueEntryID = null;
			}

			final Vector<MsgSubscription> vSubs = removeSubScriptions(queueEntryID, url, spcp.getMessageType());
			if (vSubs == null)
			{
				JMFHandler.errorResponse(response, "No matching subscriptions found", 111, EnumClass.Error);
			}
			else
			{
				for (MsgSubscription sub : vSubs)
				{
					addToResponse(response, sub);
				}
			}

			return true;
		}

		/**
		 * @param response
		 * @param sub
		 */
		private void addToResponse(final JDFResponse response, final MsgSubscription sub)
		{
			if (response == null)
			{
				return;
			}
			if (sub == null)
			{
				JMFHandler.errorResponse(response, "No matching subscriptions found", 111, EnumClass.Error);
			}
			else
			{
				final VString vs = StringUtil.tokenize(response.getDescriptiveName(), ":", false);
				if (vs.size() < 2)
				{
					response.setDescriptiveName("Removed Channels: 1");
				}
				else
				{
					int i = StringUtil.parseInt(vs.elementAt(1), -1);
					if (i > 0)
					{
						i++;
						response.setDescriptiveName("Removed Channels: " + i);
					}
				}
			}
		}
	}

	/**
	 * handler for the StopPersistentChannel command
	 */
	public class KnownSubscriptionsHandler extends AbstractHandler
	{
		/**
		 *
		 */
		public KnownSubscriptionsHandler()
		{
			super(EnumType.KnownSubscriptions, new EnumFamily[] { EnumFamily.Query });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param inputMessage
		 * @param response
		 * @return true if handled
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			final JDFSubscriptionFilter sf = inputMessage.getSubscriptionFilter();
			final String senderID = sf == null ? null : StringUtil.getNonEmpty(sf.getDeviceID());
			final String qeID = sf == null ? null : StringUtil.getNonEmpty(sf.getQueueEntryID());

			final Set<String> ss = getChannels(null, senderID, qeID);
			final Vector<MsgSubscription> v = ContainerUtil.toValueVector(subscriptionMap);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final MsgSubscription sub = v.get(i);
					if (!ss.contains(sub.channelID))
					{
						continue;
					}
					final JDFSubscriptionInfo subscriptionInfo = response.appendSubscriptionInfo();
					sub.setSubscriptionInfo(subscriptionInfo);
					final JDFSubscription subscription = subscriptionInfo.appendSubscription();
					sub.setSubscription(subscription);
				}
			}
			return true;
		}

	}

	/**
	 * constructor
	 * @param
	 * @param dev device for this ID of the device this SignalHandler is working for.
	 */
	public SignalDispatcher(final AbstractDevice dev)
	{
		device = dev;
		if (dev == null)
		{
			log.error("Creating SignalDispatcher for null device");
		}
		subscriptionMap = new HashMap<String, MsgSubscription>();
		storage = new SubscriptionStore(this, dev == null ? null : dev.getDeviceDir());
		triggers = new Vector<Trigger>();
		mutex = new MyMutex();
		theDispatcher = new Dispatcher();
		doShutdown = false;
		lastCalled = 0;
		ignoreURL = null;
	}

	/**
	 * check any prehistoric subscriptions that no longer work and zapp them
	 * @param sub the subscription
	 */
	protected boolean checkStaleSubscription(MsgSubscription sub)
	{
		boolean zapped = false;
		String url = sub.getURL();
		if (StringUtil.getNonEmpty(url) == null)
		{
			log.error("deleting subscription with null url " + sub.channelID + " " + sub.getMessageType());
			removeSubScription(sub.channelID);
			zapped = true;
		}
		else
		{
			Vector<MessageSender> vMesSend = JMFFactory.getJMFFactory().getMessageSenders(url);
			if (vMesSend != null)
			{
				for (MessageSender m : vMesSend)
				{
					if (m.isBlocked(1000l * 24l * 60l * 60l * 42l * 2l, 420))
					{
						removeSubScription(sub.channelID);
						m.flushMessages();
						JMFFactory.getJMFFactory().shutDown(m.getCallURL(), true);
						log.error("removed stale subscription " + sub.channelID + " " + sub.getMessageType() + " url=" + sub.getURL());
						zapped = true;
					}
				}
			}
		}
		return zapped;
	}

	/**
	 * find subscriptions in a message and add them if appropriate
	 * @param m
	 * @param resp
	 */
	public void findSubscription(final JDFMessage m, final JDFResponse resp)
	{
		if (!(m instanceof IJMFSubscribable))
		{
			return;
		}
		final IJMFSubscribable query = (IJMFSubscribable) m;
		final JDFSubscription sub = query.getSubscription();
		if (sub == null)
		{
			return;
		}
		final String channelID = addSubscription(query, findQueueEntryID(m));
		if (resp != null && channelID != null)
		{
			resp.setSubscribed(true);
		}
	}

	/**
	 * @param m
	 * @return
	 */
	private String findQueueEntryID(final JDFMessage m)
	{
		if (m == null)
		{
			return null;
		}
		try
		{
			final EnumType messageType = m.getEnumType();
			if (EnumType.Status.equals(messageType))
			{
				final JDFStatusQuParams sqp = m.getStatusQuParams();
				final String qeid = sqp == null ? null : StringUtil.getNonEmpty(sqp.getQueueEntryID());
				return qeid;
			}
			else if (EnumType.Resource.equals(messageType))
			{
				final JDFResourceQuParams rqp = m.getResourceQuParams();
				final String qeid = rqp == null ? null : StringUtil.getNonEmpty(rqp.getQueueEntryID());
				return qeid;
			}
		}
		catch (final JDFException x)
		{ /* nop */
		}
		return null;
	}

	/**
	 * add a subscription - returns the slaveChannelID of the new subscription, null if snafu
	 * @param subMess the subscription message - one of query or registration
	 * @param queueEntryID the associated QueueEntryID, may be null.
	 * @return the slaveChannelID of the subscription, if successful, else null
	 */
	public synchronized String addSubscription(final IJMFSubscribable subMess, String queueEntryID)
	{
		if (subMess == null)
		{
			log.error("adding null subscription" + queueEntryID);
			return null;
		}
		if (isIgnoreURL(subMess))
		{
			return null;
		}

		if (!MsgSubscription.isSpecific())
		{
			queueEntryID = null;
		}
		final MsgSubscription sub = new MsgSubscription(this, subMess, queueEntryID);
		final String url = sub.getURL();
		if (!UrlUtil.isURL(url))
		{
			log.error("Attempting to subscribe to invalid URL: " + url);
			return null;
		}
		sub.setQueueEntryID(queueEntryID);
		if (sub.channelID == null)
		{
			log.error("Null ChannelID while attempting to subscribe to URL: " + url);
			return null;
		}
		if (subscriptionMap.containsKey(sub.channelID))
		{
			log.warn("subscription already exists for:" + sub.channelID);
			return null;
		}
		if (subscriptionMap.containsValue(sub))
		{
			log.info("ignoring identical subscription already exists for:" + sub.channelID);
			return null;
		}
		synchronized (subscriptionMap)
		{
			log.info("adding subscription " + sub);
			subscriptionMap.put(sub.channelID, sub);
		}
		sub.trigger.queueEntryID = queueEntryID;
		storage.persist();
		sub.setCallback(device.getCallback(url, sub));
		return sub.channelID;
	}

	/**
	 * @param subMess
	 * @return true if the message comes from a spam url that should be ignored
	 */
	private boolean isIgnoreURL(final IJMFSubscribable subMess)
	{
		final JDFSubscription sub = subMess.getSubscription();
		if (sub == null)
		{
			return true;
		}
		final String url = sub.getURL();
		if (url == null)
		{
			return true;
		}
		if (ignoreURL != null && url.toLowerCase().indexOf(ignoreURL) >= 0)
		{
			return true;
		}
		return false;

	}

	/**
	 * add a subscription returns the slaveChannelID of the new subscription, null if snafu
	 * @param node the node to search for inline jmfs
	 * @param queueEntryID the associated QueueEntryID, may be null.
	 * @return the channelIDs of the subscriptions, if successful, else null
	 */
	public VString addSubscriptions(final JDFNode node, final String queueEntryID)
	{
		if (node == null)
		{
			return null;
		}
		JDFNodeInfo nodeInfo = node.getNodeInfo();
		VString vs = nodeInfo == null ? null : addSubscriptions(nodeInfo, queueEntryID);
		if (vs == null)
		{
			nodeInfo = (JDFNodeInfo) node.getAncestorElement(ElementName.NODEINFO, null);
			vs = nodeInfo == null ? null : addSubscriptions(nodeInfo, queueEntryID);
		}
		// look in depth
		if (vs == null)
		{
			final JDFAncestorPool ap = node.getRoot().getAncestorPool();
			if (ap != null)
			{
				nodeInfo = (JDFNodeInfo) ap.getAncestorElement(ElementName.NODEINFO, null, "JMF");
			}
			vs = nodeInfo == null ? null : addSubscriptions(nodeInfo, queueEntryID);
		}

		return vs;
	}

	/**
	 * @param nodeInfo
	 * @param queueEntryID
	 * @return the channelIDs of the subscriptions, if successful, else null
	 */
	private VString addSubscriptions(final JDFNodeInfo nodeInfo, final String queueEntryID)
	{
		final VElement vJMF = nodeInfo.getChildElementVector(ElementName.JMF, null, null, true, 0, true);
		final int siz = vJMF == null ? 0 : vJMF.size();
		if (siz == 0)
		{
			return null;
		}
		final VString vs = new VString();
		for (int i = 0; i < siz; i++)
		{
			final JDFJMF jmf = nodeInfo.getJMF(i);
			// TODO registrations
			final VElement vMess = jmf.getMessageVector(EnumFamily.Query, null);
			if (vMess != null)
			{
				for (KElement mess : vMess)
				{
					final JDFQuery q = (JDFQuery) mess;
					final String channelID = addSubscription(q, queueEntryID);
					if (channelID != null)
					{
						vs.add(channelID);
					}
				}
			}
		}
		return vs;
	}

	/**
	 *
	 * get the subscription for a given channelID
	 *
	 * @param channelID the channelID of the subscription
	 * @return
	 */
	public JDFMessage getSubscriptionMessage(final String channelID)
	{
		MsgSubscription msgSubscription = subscriptionMap.get(channelID);
		return msgSubscription == null ? null : msgSubscription.theMessage;
	}

	/**
	 * remove a know subscription by channelid
	 * @param channelID the channelID of the subscription to remove
	 * @return the removed subscription, null if nothing was removed
	 */
	public MsgSubscription removeSubScription(final String channelID)
	{
		theDispatcher.flush();
		if (channelID == null)
		{
			return null;
		}
		MsgSubscription ret = null;
		synchronized (triggers)
		{
			final Trigger triggerByChannel = getTrigger(channelID);
			if (triggerByChannel != null)
			{
				triggers.remove(triggerByChannel);
			}
		}
		synchronized (subscriptionMap)
		{
			ret = subscriptionMap.remove(channelID);
		}
		log.info("removing subscription for channelid=" + channelID);
		storage.persist();
		return ret;
	}

	private Trigger getTrigger(final String channelID)
	{
		for (final Trigger trigger : triggers)
		{
			if (channelID.equals(trigger.channelID))
			{
				return trigger;
			}
		}
		return null;
	}

	/**
	 * remove a know subscription by queueEntryID
	 * @param queueEntryID the queueEntryID of the subscriptions to remove
	 * @param url url of subscriptions to zapp
	 * @param messageType TODO
	 * @return the vector of remove subscriptions
	 */
	public Vector<MsgSubscription> removeSubScriptions(final String queueEntryID, final String url, final String messageType)
	{
		final Vector<MsgSubscription> vSubs = new Vector<MsgSubscription>();
		synchronized (subscriptionMap)
		{
			final VString v = getSubscriptionKeys(queueEntryID, url, messageType);
			if (v != null && v.size() > 0)
			{
				log.info("removing multiple subscriptions for qe=" + queueEntryID + " URL=" + url);
				for (String key : v)
				{
					final MsgSubscription mSub = removeSubScription(key);
					if (mSub != null)
					{
						vSubs.add(mSub);
					}
				}
			}
		}
		return vSubs.size() == 0 ? null : vSubs;
	}

	/**
	 *
	 * returns the channelIDs of all matching subscriptions
	 *
	 * @param queueEntryID the queueEntryID of the subscriptions to remove
	 * @param url url of subscriptions to zapp
	 * @param messageType TODO
	 * @return
	 */
	private VString getSubscriptionKeys(final String queueEntryID, final String url, final String messageType)
	{
		final Iterator<String> it = subscriptionMap.keySet().iterator();
		final boolean allURL = KElement.isWildCard(url);
		final boolean allQE = KElement.isWildCard(queueEntryID);
		final boolean allType = KElement.isWildCard(messageType);
		final VString v = new VString();
		while (it.hasNext())
		{
			final String channelID = it.next();
			if (!allURL || !allQE)
			{
				final MsgSubscription sub = subscriptionMap.get(channelID);
				if (!allURL && !url.equals(sub.getURL()))
				{
					continue; // non-matching URL
				}
				if (!allQE && !queueEntryID.equals(sub.queueEntry))
				{
					continue; // non-matching qeid
				}
				if (!allType)
				{
					final JDFMessage mess = sub.theMessage;
					if (mess != null)
					{
						final String typ = mess.getType();
						if (!messageType.equals(typ))
						{
							continue; // non-matching type
						}
					}
				}
			}
			// illegal to remove while iterating - must store list
			v.add(channelID);
		}
		return v;
	}

	/**
	 * trigger a subscription based on slave ChannelID
	 * @param channelID the channelid of the channel to trigger
	 * @param queueEntryID the queuentryid of the active queueentry
	 * @param nodeIdentifier the nodeIdentifier of the active task
	 * @param amount the amount produced since the last call, 0 if unknown, -1 for a global trigger
	 * @param ignoreIfTime if true, don't trigger if a time subscription exists
	 * @param last if true this is the last call and we notify the mutex
	 * @return the Trigger
	 */
	public Trigger triggerChannel(final String channelID, final String queueEntryID, final NodeIdentifier nodeIdentifier, final int amount, final boolean last, final boolean ignoreIfTime)
	{
		final MsgSubscription subscription = subscriptionMap.get(channelID);
		Trigger tNew = null;
		if (subscription != null)
		{
			if (!ignoreIfTime || subscription.repeatTime <= 0)
			{
				tNew = new Trigger(queueEntryID, nodeIdentifier, channelID, amount);
				synchronized (triggers)
				{
					final Trigger t = getTrigger(tNew);

					if (t == null)
					{
						triggers.add(tNew);
					}
					else if (amount >= 0 && t.amount >= 0) // -1 always forces a trigger
					{
						t.amount += amount;
						tNew = t;
					}
					else if (t.amount > 0 && amount < 0)
					{
						t.amount = amount;
						tNew = t;
					}
					else if (t.amount < 0 && amount < 0)// always add a trigger if amount<0
					{
						triggers.add(tNew);
					}
				}
				if (amount != 0)
				{
					lastCalled++;
				}
			}
		}
		if (last && lastCalled > 0)
		{
			synchronized (mutex)
			{
				flush();
				lastCalled = 0;
			}
		}
		return tNew;
	}

	/**
	 * get a trigger from triggers, if it is in there, else null
	 * @param newTrigger
	 * @return
	 */
	private Trigger getTrigger(final Trigger newTrigger)
	{
		if (triggers == null || newTrigger == null)
		{
			return null;
		}
		for (Trigger t : triggers)
		{
			if (newTrigger.equals(t))
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * trigger a subscription based on queuentryID
	 * @param queueEntryID the queuentryid of the active queueentry
	 * @param nodeID the nodeIdentifier of the active task
	 * @param amount the amount produced since the last call, 0 if unknown, -1 for a global trigger
	 * @param msgType
	 * @return the list of actual triggers
	 */

	public Trigger[] triggerQueueEntry(final String queueEntryID, final NodeIdentifier nodeID, final int amount, final String msgType)
	{
		final Vector<MsgSubscription> v = ContainerUtil.toValueVector(subscriptionMap, false);
		final int size = v == null ? 0 : v.size();
		if (size == 0 || v == null)
		{
			return null;
		}
		Trigger[] triggers = new Trigger[size];
		int n = 0;
		for (int i = 0; i < size; i++)
		{
			final MsgSubscription sub = v.get(i);
			if (sub.matchesQueueEntry(queueEntryID) && sub.matchesType(msgType))
			{
				triggers[n++] = triggerChannel(sub.channelID, queueEntryID, nodeID, amount, i + 1 == size, false);
			}
		}
		if (n == 0)
		{
			return null;
		}
		if (n < size)
		{
			triggers = Arrays.copyOf(triggers, n);
		}
		return triggers;
	}

	/**
	 * add all JMF handlers that this dispatcher can handle
	 * @param jmfHandler
	 */
	public void addHandlers(final IJMFHandler jmfHandler)
	{
		jmfHandler.addHandler(new StopPersistentChannelHandler());
		jmfHandler.addHandler(new KnownSubscriptionsHandler());
	}

	/**
	 * start the dispatcher thread
	 */
	public void startup()
	{
		final String deviceID = device.getDeviceID();
		Thread thread = new Thread(theDispatcher, "SignalDispatcher_" + deviceID);
		thread.setDaemon(true);
		thread.start();
		log.info("dispatcher thread 'SignalDispatcher_" + deviceID + "' started");
		storage.load();
	}

	/**
	 * the number of currently active subscriptions
	 * @return
	 */
	public int size()
	{
		return subscriptionMap.size();
	}

	/**
	 * stop the dispatcher thread
	 */
	public void shutdown()
	{
		doShutdown = true;
		ThreadUtil.notifyAll(mutex);
	}

	/**
	 * @param request
	 * @return
	 */
	public XMLResponse handleGet(final ContainerRequest request)
	{
		return this.new XMLSubscriptions().handleGet(request);
	}

	/**
	 * return all subscription channels for a given message type and device id
	 * @param typ the message type filter
	 * @param senderID the senderid filter
	 * @param queueEntryID
	 * @return set of channelID values that match the filters
	 */
	public Set<String> getChannels(final EnumType typ, final String senderID, final String queueEntryID)
	{
		final Set<String> keySet2 = new HashSet<String>();
		final String typNam = typ == null ? null : typ.getName();
		synchronized (subscriptionMap)
		{
			final Set<String> keySet = subscriptionMap.keySet();
			final Iterator<String> it = keySet.iterator();
			while (it.hasNext())
			{
				final String key = it.next();
				final MsgSubscription sub = subscriptionMap.get(key);
				boolean bMatch = sub.matchesQueueEntry(queueEntryID);
				bMatch = bMatch && (typNam == null || typNam.equals(sub.getMessageType()));
				bMatch = bMatch && (senderID == null || sub.jmfDeviceID == null || sub.jmfDeviceID.equals(senderID));

				if (bMatch)
				{
					keySet2.add(key);
				}
			}
		}
		return keySet2;
	}

	/**
	 *
	 * @param typ
	 * @return
	 */
	public boolean hasSubscription(EnumType typ)
	{
		return !getChannels(typ, null, null).isEmpty();
	}

	/**
	 * flush any waiting messages by notifying the dispatcher thread
	 */
	public void flush()
	{
		ThreadUtil.notifyAll(mutex);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	*/
	@Override
	public String toString()
	{
		return "SubscriptionMap; device= " + device.getDeviceID() + " : " + subscriptionMap;
	}

	/**
	 * remove all subscriptions
	 */
	public void reset()
	{
		flush();
		removeSubScriptions(null, null, null);
	}

	/**
	 * loop over all handlers, trying to handle a message
	 *
	 * @param q
	 * @param r
	 * @return
	 */
	public boolean handleMessage(JDFQuery q, JDFResponse r)
	{
		Vector<JMFHandler> v = device.getJMFHandlers();
		for (JMFHandler h : v)
		{
			boolean handled = h.handleMessage(q, r);
			if (handled)
			{
				return handled;
			}
		}
		return false;
	}
}
