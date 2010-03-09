/**
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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
package org.cip4.bambi.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFSubscriptionFilter;
import org.cip4.jdflib.jmf.JDFSubscriptionInfo;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.pool.JDFAncestorPool;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FastFiFo;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.RollingBackupFile;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.ThreadUtil.MyMutex;

/**
 * this class handles subscriptions <br>
 * class should remain final, because if it is ever subclassed the dispactcher thread would be started before the constructor from the subclass has a chance to
 * fire off.
 * @author prosirai
 */
public final class SignalDispatcher extends BambiLogFactory
{
	protected HashMap<String, MsgSubscription> subscriptionMap = null; // map of slaveChannelID / Subscription
	private final SubscriptionStore storage;
	IMessageHandler messageHandler = null;
	protected Vector<Trigger> triggers = null;
	protected MyMutex mutex = null;
	protected boolean doShutdown = false;
	protected AbstractDevice device = null;
	private int lastCalled = 0;
	protected Dispatcher theDispatcher;
	private String ignoreURL = null;

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
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(org.cip4.bambi.core.BambiServletRequest, org.cip4.bambi.core.BambiServletResponse)
		 * @param request
		 * @param response
		 * @return
		 */
		public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
		{
			final boolean bStopChannel = request.getBooleanParam("StopChannel");
			if (bStopChannel)
			{
				stopChannel(request, response);
			}
			final boolean bStopSender = request.getBooleanParam("StopSender");
			if (bStopSender)
			{
				stopSender(request, response);
			}
			final String pause = request.getParameter("pause");
			if (StringUtil.isBoolean(pause))
			{
				pauseResumeSender(request, response);
			}
			final boolean bFlushSender = request.getBooleanParam("FlushSender");
			if (bFlushSender)
			{
				flushSender(request, response);
			}
			final String details = request.getParameter("DetailID");

			final int pos = request.getIntegerParam("pos");
			if (details == null)
			{
				final boolean bListSenders = request.getBooleanParam("ListSenders");
				setXMLRoot(request);
				if (bListSenders)
				{
					listDispatchers(request, true, pos);
					if (pos > 0)
					{
						setXSLTURL("/" + BambiServlet.getBaseServletName(request) + "/subscriptionDetails.xsl");
					}
					else
					{
						setXSLTURL("/" + BambiServlet.getBaseServletName(request) + "/subscriptionList.xsl");
					}
				}
				else
				{
					listChannels(request, details);
					device.addMoreToXMLSubscriptions(root);
					listDispatchers(request, false, -1);
					setXSLTURL("/" + BambiServlet.getBaseServletName(request) + "/subscriptionList.xsl");
				}
			}
			else
			{
				showDetails(request, details, pos);
				setXSLTURL("/" + BambiServlet.getBaseServletName(request) + "/subscriptionDetails.xsl");
			}

			try
			{
				write2Stream(response.getBufferedOutputStream(), 2, true);
			}
			catch (final IOException x)
			{
				return false;
			}
			response.setContentType(UrlUtil.TEXT_XML);
			return true;

		}

		/**
		 * @param request
		 * @param response
		 */
		private void pauseResumeSender(final BambiServletRequest request, final BambiServletResponse response)
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
		 * @param response
		 */
		private void stopSender(final BambiServletRequest request, final BambiServletResponse response)
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
		 * @param response
		 */
		private void flushSender(final BambiServletRequest request, final BambiServletResponse response)
		{
			final String url = request.getParameter(AttributeName.URL);
			final Vector<MessageSender> v = device.getJMFFactory().getMessageSenders(url);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final MessageSender messageSender = v.get(i);
					messageSender.flushMessages();
				}
			}
		}

		/**
		 * @param request
		 * @param bListSenders 
		 * @param pos 
		 */
		private void listDispatchers(final BambiServletRequest request, final boolean bListSenders, final int pos)
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
						ms.appendToXML(root, false, pos);
					}
				}
			}
			else
			{
				final MessageSender ms = device.getJMFFactory().getCreateMessageSender(url);
				if (ms != null)
				{
					ms.appendToXML(root, false, pos);
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
		public void listChannels(final BambiServletRequest request, final String details)
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
		protected void setXMLRoot(final BambiServletRequest request)
		{
			root.setAttribute(AttributeName.DEVICEID, device.getDeviceID());
			root.setAttribute(AttributeName.CONTEXT, "/" + BambiServlet.getBaseServletName(request));
		}

		/**
		 * @param request the http request to handle
		 * @param details
		 * @param pos
		 */
		private void showDetails(final BambiServletRequest request, final String details, final int pos)
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
		 * @param response
		 */
		private void stopChannel(final BambiServletRequest request, final BambiServletResponse response)
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

	protected class SubscriptionStore
	{
		private final RollingBackupFile backup;
		private boolean loading = false;

		protected SubscriptionStore(final File dir)
		{
			backup = new RollingBackupFile(FileUtil.getFileInDirectory(dir, new File("subscriptions.xml")), 8);
		}

		/**
		 * load subscriptions from file
		 */
		public void load()
		{
			loading = true;
			final JDFParser parser = new JDFParser();
			parser.bKElementOnly = true;
			final XMLDoc d = parser.parseFile(backup);
			final KElement root = d == null ? null : d.getRoot();
			try
			{
				if (root != null)
				{
					final VElement v = root.getChildElementVector(MsgSubscription.SUBSCRIPTION_ELEMENT, null);
					for (int i = 0; i < v.size(); i++)
					{
						final MsgSubscription sub = new MsgSubscription(v.get(i));
						synchronized (subscriptionMap)
						{
							if (sub.channelID != null)
							{
								subscriptionMap.put(sub.channelID, sub);
								log.info("reloading subscription for channelID=" + sub.channelID);
							}
						}
					}
				}
			}
			catch (final Exception x)
			{
				log.error("unknown exception while loading subscriptions", x);
			}
			loading = false;
		}

		/**
		 * write all subscriptions to disk
		 */
		public void persist()
		{
			if (loading)
			{
				return;
			}
			final XMLSubscriptions xmls = new XMLSubscriptions();
			xmls.setXMLRoot(null);
			xmls.listChannels(null, "*");
			xmls.write2File(backup.getNewFile(), 2, false);
		}

	}

	// ///////////////////////////////////////////////////////////

	protected static class Trigger
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "Trigger: queueEntryID: " + queueEntryID + " nodeIdentifier: " + nodeIdentifier + " amount: " + amount + nodeIdentifier + " slaveChannelID: " + channelID;
		}

		@Override
		public int hashCode()
		{
			return super.hashCode() + channelID == null ? 0 : channelID.hashCode() + queueEntryID == null ? 0 : queueEntryID.hashCode()
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
		 * @param triggers
		 * @param milliseconds
		 */
		public static void waitQueued(final Trigger[] triggers, final int milliseconds)
		{
			if (triggers == null)
			{
				return;
			}
			for (int i = 0; i < triggers.length; i++)
			{
				triggers[i].waitQueued(milliseconds);
			}
		}

		/**
		 * wait for this to be queued
		 * @param milliseconds
		 */
		public void waitQueued(final int milliseconds)
		{
			ThreadUtil.wait(mutex, milliseconds);
			mutex = null;
		}

	}

	// ///////////////////////////////////////////////////////////
	protected class Dispatcher implements Runnable
	{
		int sentMessages;
		private final CPUTimer timer;

		/**
		 * 
		 */
		public Dispatcher()
		{
			super();
			sentMessages = 0;
			timer = new CPUTimer(false);
		}

		/**
		 * this is the time clocked dispatcher thread
		 */
		public void run()
		{
			while (!doShutdown)
			{
				try
				{
					flush();
				}
				catch (final Exception x)
				{
					log.error("unhandled Exception in flush", x);
					timer.stop();
				}
				ThreadUtil.wait(mutex, 1000);
			}
		}

		/**
		 * 
		 */
		void flush()
		{
			timer.start();
			while (true)
			{
				final Vector<MsgSubscription> triggerVector = getTriggerSubscriptions();
				final int size = triggerVector.size();
				// spam them out
				for (int i = 0; i < size; i++)
				{
					sentMessages++;
					final MsgSubscription sub = triggerVector.elementAt(i);
					log.debug("Trigger Signalling :" + i + " slaveChannelID=" + sub.channelID);
					queueMessageInSender(sub);
				}
				// select pending time subscriptions
				final Vector<MsgSubscription> subVector = getTimeSubscriptions();
				final int size2 = subVector.size();
				// spam them out
				for (int i = 0; i < size2; i++)
				{
					sentMessages++;
					final MsgSubscription sub = subVector.elementAt(i);
					log.debug("Time Signalling: " + i + ", slaveChannelID=" + sub.channelID);
					queueMessageInSender(sub);
				}
				if (size == 0 && size2 == 0)
				{
					break; // flushed all
				}
			}
			timer.stop();
		}

		/**
		 * queue a message in the appropriate sender
		 * @param sub
		 */
		private void queueMessageInSender(final MsgSubscription sub)
		{
			final String url = sub.getURL();
			final JDFJMF signalJMF = sub.getSignal();
			if (signalJMF != null)
			{
				device.sendJMF(signalJMF, url, null);
				final MsgSubscription realSubSubscription = subscriptionMap.get(sub.channelID);
				if (realSubSubscription != null)
				{
					realSubSubscription.lastTime = System.currentTimeMillis() / 1000;
					realSubSubscription.lastSentJMF.push(signalJMF);
					realSubSubscription.sentMessages++;
				}
			}
			else
			{
				log.debug("no Signal for subscription: " + sub);
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
			int n = 0;
			synchronized (triggers)
			{
				final Vector<MsgSubscription> v = new Vector<MsgSubscription>();
				final Vector<Trigger> vSnafu = new Vector<Trigger>();
				final Iterator<Trigger> it = triggers.iterator(); // active triggers
				while (it.hasNext())
				{
					n++;
					final Trigger t = it.next();
					final String channelID = t.channelID;
					final MsgSubscription sub = subscriptionMap.get(channelID);
					if (sub == null)
					{
						vSnafu.add(t);
						continue; // snafu
					}
					final MsgSubscription subClone = (MsgSubscription) sub.clone();
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
								sub.lastAmount = next; // not a typo - modify of nthe original subscription
								v.add(subClone);
							}
						}
					}
				}
				// remove active triggers that will be returned
				for (int j = 0; j < vSnafu.size(); j++)
				{
					final boolean b = triggers.remove(vSnafu.get(j));
					if (!b)
					{
						log.error("Snafu removing trigger");
					}
					else
					{
						log.info("removing spurious trigger");
					}

				}
				for (int j = 0; j < v.size(); j++)
				{
					final MsgSubscription sub = v.elementAt(j);
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
							sub = (MsgSubscription) sub.clone();
							subVector.add(sub);
						}
					}
				}
			} // end synch map
			return subVector;
		}
	}

	/**
	 * 
	 */
	private class MsgSubscription implements Cloneable
	{
		protected static final String SUBSCRIPTION_ELEMENT = "MsgSubscription";
		protected String channelID = null;
		protected String queueEntry = null;
		protected String url = null;
		protected int repeatAmount, lastAmount = 0;
		/**
		 * the last successful submission
		 */
		protected long lastTime = 0;
		/**
		 * the last submission attempt
		 */
		protected long lastTry = 0;
		protected long repeatTime = 0;
		protected JDFMessage theMessage = null;
		protected FastFiFo<JDFJMF> lastSentJMF = new FastFiFo<JDFJMF>(10);
		protected Trigger trigger = null;
		protected int sentMessages = 0;
		protected String jmfDeviceID = null; // the senderID of the incoming (subscribed) jmf

		MsgSubscription(final IJMFSubscribable m, final String qeid)
		{
			final JDFSubscription sub = m.getSubscription();
			if (sub == null)
			{
				log.error("Subscribing to non subscription ");
				channelID = null;
				return;
			}
			channelID = m.getID();
			url = sub.getURL();
			queueEntry = qeid;

			lastAmount = 0;
			repeatAmount = sub.getRepeatStep();
			lastTime = 0;
			sentMessages = 0;
			repeatTime = (long) sub.getRepeatTime();
			theMessage = (JDFMessage) m;
			trigger = new Trigger(null, null, null, 0);
			// TODO observation targets
			if (repeatTime == 0 && repeatAmount == 0 && EnumType.Status.equals(theMessage.getType())) // reasonable default
			{
				repeatAmount = 100;
				repeatTime = 15;
			}
			final JDFJMF ownerJMF = ((JDFMessage) m).getJMFRoot();
			jmfDeviceID = ownerJMF != null ? ownerJMF.getDeviceID() : null;
			if ("".equals(jmfDeviceID) || ContainerUtil.equals(jmfDeviceID, device.getDeviceID()))
			{
				// zapp any filters to myself - they represent all my kids
				jmfDeviceID = null;
			}

		}

		/**
		 * get a signal that corresponds to this subscription
		 * @return the jmf element that contains any signals generated by this subscription null if no signals were generated
		 */
		protected JDFJMF getSignal()
		{
			if (!(theMessage instanceof JDFQuery))
			{
				// TODO guess what...
				log.error("registrations not supported");
				return null;
			}
			JDFQuery q = (JDFQuery) theMessage;
			final JDFJMF jmf = q.createResponse();
			JDFResponse r = jmf.getResponse(0);
			// make a copy so that modifications do not have an effect
			q = (JDFQuery) jmf.copyElement(q, null);
			q.removeChild(ElementName.SUBSCRIPTION, null, 0);

			jmf.setDeviceID(jmfDeviceID);
			// this is the handling of the actual message
			q.setAttribute(JMFHandler.subscribed, "true");
			final boolean b = messageHandler.handleMessage(q, r);
			q.removeAttribute(JMFHandler.subscribed);
			if (!b)
			{
				log.debug("Unhandled message: " + q.getType());
				return null;
			}
			final int nResp = jmf.numChildElements(ElementName.RESPONSE, null);
			final int nSignals = jmf.numChildElements(ElementName.SIGNAL, null);
			JDFJMF jmfOut = (nResp + nSignals > 0) ? new JDFDoc("JMF").getJMFRoot() : null;
			for (int i = 0; i < nResp; i++)
			{
				final JDFSignal s = jmfOut.getCreateSignal(i);
				r = jmf.getResponse(i);
				s.convertResponse(r, q);
			}
			for (int i = 0; i < nSignals; i++)
			{
				jmfOut.copyElement(jmf.getSignal(i), null);
			}
			jmfOut = filterSenderID(jmfOut);
			finalizeSentMessages(jmfOut);
			return jmfOut;
		}

		private void finalizeSentMessages(final JDFJMF jmfOut)
		{
			if (jmfOut == null)
			{
				return;
			}
			// this is a clone - need to update "the" subscription
			jmfOut.collectICSVersions();
		}

		/**
		 * @param jmfOut
		 * @return
		 */
		private JDFJMF filterSenderID(final JDFJMF jmfOut)
		{
			if (KElement.isWildCard(jmfDeviceID))
			{
				return jmfOut; // no filtering necessary
			}
			if (jmfOut == null)
			{
				return null;
			}
			final VElement v = jmfOut.getMessageVector(EnumFamily.Signal, null);
			final int siz = v == null ? 0 : v.size();
			if (siz == 0 || v == null)
			{
				return null;
			}
			for (int i = siz - 1; i >= 0; i--)
			{
				final JDFSignal s = (JDFSignal) v.get(i);
				if (!jmfDeviceID.equals(s.getSenderID()))
				{
					s.deleteNode();
					v.remove(i);
				}
			}
			return v.size() == 0 ? null : jmfOut;
		}

		public String getURL()
		{
			return url;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone()
		{
			MsgSubscription c;
			try
			{
				c = (MsgSubscription) super.clone();
			}
			catch (final CloneNotSupportedException x)
			{
				return null;
			}
			c.channelID = channelID;
			c.lastAmount = lastAmount;
			c.repeatAmount = repeatAmount;
			c.repeatTime = repeatTime;
			c.theMessage = theMessage; // ref only NOT Cloned (!!!)
			c.url = url;
			c.trigger = trigger; // ref only NOT Cloned (!!!)
			c.queueEntry = queueEntry;
			c.lastTime = lastTime;
			c.sentMessages = sentMessages;
			return c;
		}

		@Override
		public String toString()
		{
			return "[MsgSubscription: slaveChannelID=" + channelID + " Type=" + getMessageType() + " QueueEntry=" + queueEntry + " lastAmount=" + lastAmount + "\nrepeatAmount="
					+ repeatAmount + " repeatTime=" + repeatTime + " lastTime=" + lastTime + "\nURL=" + url + " device ID=" + jmfDeviceID + " Sent=" + sentMessages + "]";
		}

		/**
		 * 
		 * @param parent
		 * @param details
		 * @param pos
		 * @return
		 */
		protected KElement appendToXML(final KElement parent, final String details, final int pos)
		{
			if (parent == null)
			{
				return null;
			}
			final KElement sub = parent.appendElement(SUBSCRIPTION_ELEMENT);
			setXML(sub, details, pos);
			return sub;

		}

		/**
		 * 
		 * @param sub
		 * @param details
		 * @param pos 
		 */
		protected void setXML(final KElement sub, final String details, int pos)
		{
			setSubscriptionInfo(sub);
			setSubscription(sub);
			sub.setAttribute(AttributeName.TYPE, theMessage.getType());
			sub.setAttribute("Sent", sentMessages, null);
			sub.setAttribute("LastTime", sentMessages == 0 ? " - " : BambiServlet.formatLong(lastTime * 1000));
			if (pos <= 0)
			{
				pos = 1;
			}
			if ("*".equals(details) || ContainerUtil.equals(channelID, details))
			{
				sub.appendElement("Sub").copyElement(theMessage, null);
				final JDFJMF[] sentArray = lastSentJMF.peekArray();
				if (sentArray != null)
				{
					for (int i = sentArray.length - 1; i >= 0; i--)
					{
						final KElement message = sub.appendElement("Message");
						if (pos == sentArray.length - i)
						{
							final XJDF20 x2 = new XJDF20();
							x2.bUpdateVersion = false;

							final KElement newJMF = message.copyElement(x2.makeNewJMF(sentArray[i]), null);
							newJMF.setAttribute(AttributeName.TIMESTAMP, sentArray[i].getTimeStamp().getDateTimeISO());
						}
						message.setAttribute(AttributeName.TIMESTAMP, sentArray[i].getTimeStamp().getDateTimeISO());
					}
				}
			}
		}

		/**
		 * @param sub
		 */
		protected void setSubscription(final KElement sub)
		{
			sub.setAttribute(AttributeName.URL, url);
			sub.setAttribute(AttributeName.REPEATTIME, repeatTime, null);
			sub.setAttribute(AttributeName.REPEATSTEP, repeatAmount, null);
		}

		/**
		 * @param sub
		 */
		protected void setSubscriptionInfo(final KElement sub)
		{
			sub.setAttribute(AttributeName.CHANNELID, channelID);
			sub.setAttribute(AttributeName.DEVICEID, jmfDeviceID);
			sub.setAttribute(AttributeName.QUEUEENTRYID, queueEntry);
			sub.setAttribute(AttributeName.SENDERID, device.getDeviceID());
			if (theMessage != null)
			{
				sub.setAttribute(AttributeName.MESSAGETYPE, theMessage.getType());
				final EnumFamily family = theMessage.getFamily();
				sub.setAttribute(AttributeName.FAMILY, family == null ? "Unknown" : family.getName());
			}
			else
			{
				log.error("No message for subscription, bailing out" + sub);
			}
		}

		/**
		 * creates a MsgSubscription from an XML element
		 * 
		 * - must be maintained in synch with @see setXML (duh...)
		 * @param sub
		 */
		MsgSubscription(final KElement sub)
		{
			channelID = sub.getAttribute(AttributeName.CHANNELID, null, null);
			jmfDeviceID = sub.getAttribute(AttributeName.DEVICEID, null, null);
			queueEntry = sub.getAttribute(AttributeName.QUEUEENTRYID, null, null);
			url = sub.getAttribute(AttributeName.URL, null, null);
			repeatTime = sub.getLongAttribute(AttributeName.REPEATTIME, null, 0);
			repeatAmount = sub.getIntAttribute(AttributeName.REPEATSTEP, null, 0);
			sentMessages = sub.getIntAttribute("Sent", null, 0);
			final KElement subsub = sub.getElement("Sub");
			if (subsub != null)
			{
				final JDFJMF jmf = new JDFDoc("JMF").getJMFRoot();
				theMessage = (JDFMessage) jmf.copyElement(subsub.getFirstChildElement(), null);
			}
		}

		/**
		 * @param queueEntryID
		 */
		protected void setQueueEntryID(final String queueEntryID)
		{
			if (queueEntryID == null)
			{
				return;
			}
			if (theMessage == null)
			{
				return;
			}
			final EnumType typ = theMessage.getEnumType();
			// TODO more message types
			if (EnumType.Status.equals(typ))
			{
				final JDFStatusQuParams sqp = theMessage.getCreateStatusQuParams(0);
				sqp.setQueueEntryID(queueEntryID);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (!(obj instanceof MsgSubscription))
			{
				return false;
			}
			final MsgSubscription msg = (MsgSubscription) obj;
			if (repeatAmount != msg.repeatAmount || repeatTime != msg.repeatTime)
			{
				return false;
			}
			if (!ContainerUtil.equals(queueEntry, msg.queueEntry))
			{
				return false;
			}
			if (!ContainerUtil.equals(url, msg.url))
			{
				return false;
			}
			if (!ContainerUtil.equals(jmfDeviceID, msg.jmfDeviceID))
			{
				return false;
			}

			if (!ContainerUtil.equals(getMessageType(), msg.getMessageType()))
			{
				return false;
			}

			return true;
		}

		protected boolean matchesChannel(final String _channelID)
		{
			return channelID.equals(_channelID);
		}

		protected boolean matchesQueueEntry(final String qeID)
		{
			return queueEntry == null || queueEntry.equals(qeID);
		}

		/**
		 * @return
		 */
		public String getMessageType()
		{
			return theMessage == null ? null : theMessage.getType();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			int hc = repeatAmount + 100000 * (int) repeatTime;
			hc += queueEntry == null ? 0 : queueEntry.hashCode();
			hc += url == null ? 0 : url.hashCode();
			final String messageType = getMessageType();
			hc += messageType == null ? 0 : messageType.hashCode();
			return hc;
		}

		/**
		 * @param msgType
		 * @return true if this subscription is for this type
		 */
		public boolean matchesType(final String msgType)
		{
			if (msgType == null)
			{
				return true;
			}
			return msgType.equals(theMessage.getType());
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
				return true;
			}
			final String channel = spcp.getChannelID();
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
			final Vector<MsgSubscription> vSubs = removeSubScriptions(queueEntryID, url, null);
			if (vSubs == null)
			{
				JMFHandler.errorResponse(response, "No matching subscriptions found", 111, EnumClass.Error);
			}
			else
			{
				final int size = vSubs.size();
				for (int i = 0; i < size; i++)
				{
					addToResponse(response, vSubs.get(i));
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
			return true;
		}

	}

	/**
	 * constructor
	 * @param _messageHandler message handler
	 * @param dev device for this ID of the device this SignalHandler is working for. Required for debugging purposes only.
	 */
	public SignalDispatcher(final IMessageHandler _messageHandler, final AbstractDevice dev)
	{
		storage = new SubscriptionStore(dev == null ? null : dev.getDeviceDir());
		device = dev;
		subscriptionMap = new HashMap<String, MsgSubscription>();
		// queueEntryMap=new VectorMap<String, String>();
		messageHandler = _messageHandler;
		triggers = new Vector<Trigger>();
		mutex = new MyMutex();
		theDispatcher = null;
		theDispatcher = new Dispatcher();
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
	public synchronized String addSubscription(final IJMFSubscribable subMess, final String queueEntryID)
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

		log.info("adding subscription ");
		final MsgSubscription sub = new MsgSubscription(subMess, queueEntryID);
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
			log.error("subscription already exists for:" + sub.channelID);
			return null;
		}
		if (subscriptionMap.containsValue(sub))
		{
			log.info("identical subscription already exists for:" + sub.channelID);
			return null;
		}
		synchronized (subscriptionMap)
		{
			subscriptionMap.put(sub.channelID, sub);
		}
		sub.trigger.queueEntryID = queueEntryID;
		storage.persist();
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
			// TODO regs
			final VElement vMess = jmf.getMessageVector(EnumFamily.Query, null);
			if (vMess != null)
			{
				final int nMess = vMess.size();
				for (int j = 0; j < nMess; j++)
				{
					final JDFQuery q = (JDFQuery) vMess.elementAt(j);
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
			triggers.remove(channelID);
		}
		synchronized (subscriptionMap)
		{
			ret = subscriptionMap.remove(channelID);
		}
		log.debug("removing subscription for channelid=" + channelID);
		storage.persist();
		return ret;
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
							if (messageType.equals(typ))
							{
								continue; // non-matching type
							}
						}
					}
				}
				// illegal to remove while iterating - must store list
				v.add(channelID);
			}
			for (int i = 0; i < v.size(); i++)
			{
				final MsgSubscription mSub = removeSubScription(v.stringAt(i));
				if (mSub != null)
				{
					vSubs.add(mSub);
				}
			}
		}
		return vSubs.size() == 0 ? null : vSubs;
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
		final MsgSubscription s = subscriptionMap.get(channelID);
		Trigger tNew = null;
		if (s != null)
		{
			if (!ignoreIfTime || s.repeatTime <= 0)
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
	 * @param new1
	 * @return
	 */
	private Trigger getTrigger(final Trigger new1)
	{
		if (triggers == null)
		{
			return null;
		}
		final int size = triggers.size();
		for (int i = 0; i < size; i++)
		{
			if (triggers.get(i).equals(new1))
			{
				return triggers.get(i);
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
		final int si = v == null ? 0 : v.size();
		if (si == 0 || v == null)
		{
			return null;
		}
		Trigger[] locTriggers = new Trigger[si];
		int n = 0;
		for (int i = 0; i < si; i++)
		{
			final MsgSubscription sub = v.get(i);
			if (sub.matchesQueueEntry(queueEntryID) && sub.matchesType(msgType))
			{
				locTriggers[n++] = triggerChannel(sub.channelID, queueEntryID, nodeID, amount, i + 1 == si, false);
			}
		}
		if (n == 0)
		{
			return null;
		}
		if (n < si)
		{
			final Trigger[] t2 = new Trigger[n];
			for (int i = 0; i < n; i++)
			{
				t2[i] = locTriggers[i];
			}
			locTriggers = t2;
		}
		return locTriggers;
	}

	/**
	 * add all JMF handlers that this dispatcher can handle
	 * @param jmfHandler
	 */
	public void addHandlers(final IJMFHandler jmfHandler)
	{
		jmfHandler.addHandler(this.new StopPersistentChannelHandler());
		jmfHandler.addHandler(this.new KnownSubscriptionsHandler());
	}

	/**
	 * start the dispatcher thread
	 */
	public void startup()
	{
		final String deviceID = device == null ? "testID" : device.getDeviceID();
		new Thread(theDispatcher, "SignalDispatcher_" + deviceID).start();
		log.info("dispatcher thread 'SignalDispatcher_" + deviceID + "' started");
		storage.load();
	}

	/**
	 * stop the dispatcher thread
	 */
	public void shutdown()
	{
		doShutdown = true;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 */
	public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
	{
		return this.new XMLSubscriptions().handleGet(request, response);
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
				bMatch = bMatch && (sub.jmfDeviceID == null || sub.jmfDeviceID.equals(senderID));

				if (bMatch)
				{
					keySet2.add(key);
				}
			}
		}
		return keySet2;
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
		return "SubscriptionMap " + device.getDeviceID() + " : " + subscriptionMap;
	}

	/**
	 * remove all subscriptions
	 */
	public void reset()
	{
		flush();
		removeSubScriptions(null, null, null);
	}

}
