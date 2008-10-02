/**
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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
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
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.pool.JDFAncestorPool;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * 
 * this class handles subscriptions <br>
 * class should remain final, because if it is ever subclassed the dispactcher thread would be started 
 * before the constructor from the subclass has a chance to fire off.
 * 
 * @author prosirai
 * 
 */
public final class SignalDispatcher
{
	protected static final Log log = LogFactory.getLog(SignalDispatcher.class.getName());
	private HashMap<String, MsgSubscription> subscriptionMap = null; // map of slaveChannelID / Subscription
	IMessageHandler messageHandler = null;
	private Vector<Trigger> triggers = null;
	protected Object mutex = null;
	protected boolean doShutdown = false;
	protected String deviceID = null;
	private int lastCalled = 0;
	final IConverterCallback callback;
	protected Dispatcher theDispatcher;
	private String ignoreURL = null;

	/**
	 * set the case insensitive url pattern to be ignored for subscriptions
	 * @param _ignoreURL the pattern to ignore; 
	 */
	public void setIgnoreURL(String _ignoreURL)
	{
		this.ignoreURL = _ignoreURL != null ? _ignoreURL.toLowerCase() : null;
	}

	/**
	 * 
	 * @author prosirai
	 *
	 */
	protected class XMLSubscriptions extends XMLDoc implements IGetHandler
	{
		private final KElement root;

		/**
		 * XML representation of this simDevice
		 * for use as html display using an XSLT
		 */
		public XMLSubscriptions()
		{
			super("SubscriptionList", null);
			root = getRoot();
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
		 */
		public boolean handleGet(BambiServletRequest request, BambiServletResponse response)
		{
			boolean bStopChannel = request.getBooleanParam("StopChannel");
			if (bStopChannel)
			{
				stopChannel(request, response);
			}
			else
			{
				boolean bStopSender = request.getBooleanParam("StopSender");
				if (bStopSender)
					stopSender(request, response);
				boolean bFlushSender = request.getBooleanParam("FlushSender");
				if (bFlushSender)
					flushSender(request, response);

			}

			listChannels(request);
			listDispatchers(request);

			setXSLTURL("/" + BambiServlet.getBaseServletName(request) + "/subscriptionList.xsl");

			try
			{
				write2Stream(response.getBufferedOutputStream(), 2, true);
			}
			catch (IOException x)
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
		private void stopSender(BambiServletRequest request, BambiServletResponse response)
		{
			String url = request.getParameter(AttributeName.URL);
			URL myURL = UrlUtil.StringToURL(url);
			if (myURL == null)
				return;
			url = myURL.toExternalForm();
			Vector<MessageSender> v = JMFFactory.getAllMessageSenders();
			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{
				MessageSender messageSender = v.get(i);
				if (messageSender.matchesURL(url))
				{
					messageSender.shutDown(true);
				}
			}
		}

		/**
		 * @param request
		 * @param response
		 */
		private void flushSender(BambiServletRequest request, BambiServletResponse response)
		{
			String url = request.getParameter(AttributeName.URL);
			URL myURL = UrlUtil.StringToURL(url);
			if (myURL == null)
				return;
			url = myURL.toExternalForm();
			Vector<MessageSender> v = JMFFactory.getAllMessageSenders();
			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{
				MessageSender messageSender = v.get(i);
				if (messageSender.matchesURL(url))
				{
					messageSender.flushMessages();
				}
			}
		}

		/**
		 * @param request
		 */
		private void listDispatchers(BambiServletRequest request)
		{
			Vector<MessageSender> v = JMFFactory.getAllMessageSenders();
			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{

				v.get(i).appendToXML(root, false);
			}
		}

		/**
		 * @param request
		 */
		private void listChannels(BambiServletRequest request)
		{
			Vector<MsgSubscription> v = ContainerUtil.toValueVector(subscriptionMap, true);
			root.setAttribute(AttributeName.DEVICEID, deviceID);
			root.setAttribute(AttributeName.CONTEXT, "/" + BambiServlet.getBaseServletName(request));

			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{
				v.get(i).appendToXML(root);
			}
		}

		/**
		 * @param request
		 * @param response
		 */
		private void stopChannel(BambiServletRequest request, BambiServletResponse response)
		{
			String channelID = request.getParameter(AttributeName.CHANNELID);
			if (channelID == null)
				return;
			MsgSubscription sub = removeSubScription(channelID);
			if (sub != null)
			{
				KElement e = root.appendElement("RemovedChannel");
				sub.setXML(e);
			}
		}
	}

	/////////////////////////////////////////////////////////////
	protected static class Trigger
	{
		protected String queueEntryID;
		protected NodeIdentifier nodeIdentifier;
		protected String channelID;
		protected int amount;
		private Object mutex;

		public Trigger(String _queueEntryID, NodeIdentifier _workStepID, String _channelID, int _amount)
		{
			super();
			queueEntryID = _queueEntryID;
			nodeIdentifier = _workStepID;
			channelID = _channelID;
			amount = _amount;
			mutex = new Object();
		}

		/**
		 * equals ignores the value of Amount!
		 */
		@Override
		public boolean equals(Object t1)
		{
			if (!(t1 instanceof Trigger))
				return false;
			Trigger t = (Trigger) t1;
			boolean b = ContainerUtil.equals(channelID, t.channelID);
			b = b && ContainerUtil.equals(queueEntryID, t.queueEntryID);
			b = b && ContainerUtil.equals(nodeIdentifier, t.nodeIdentifier);
			return b;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "Trigger: queueEntryID: " + queueEntryID + " nodeIdentifier: " + nodeIdentifier + " amount: "
					+ amount + nodeIdentifier + " slaveChannelID: " + channelID;
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
			if (mutex == null)
			{
				return; // pure time subscriptions are reused and never block!
			}
			synchronized (mutex)
			{
				mutex.notifyAll();
			}
			mutex = null;
		}

		/**
		 * wait for all trigger to be queued by the dispatcher
		 */
		public static void waitQueued(Trigger[] triggers, int milliseconds)
		{
			if (triggers == null)
				return;
			for (int i = 0; i < triggers.length; i++)
				triggers[i].waitQueued(milliseconds);
		}

		/**
		 * wait for this to be queued 
		 */
		public void waitQueued(int milliseconds)
		{
			if (mutex == null)
				return;
			synchronized (mutex)
			{
				try
				{
					mutex.wait(milliseconds);
				}
				catch (InterruptedException x)
				{
					//nop
				}
			}
			mutex = null;
		}

	}

	/////////////////////////////////////////////////////////////
	protected class Dispatcher implements Runnable
	{
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
				catch (Exception x)
				{
					log.error("unhandled Exception in flush", x);
				}
				try
				{
					synchronized (mutex)
					{
						mutex.wait(1000);
					}
				}
				catch (InterruptedException x)
				{
					//nop
				}
			}
		}

		/**
		 * 
		 */
		void flush()
		{
			int n = 0;
			while (true)
			{
				n++;
				int size;
				synchronized (triggers)
				{
					final Vector<MsgSubscription> triggerVector = getTriggerSubscriptions();
					size = triggerVector.size();
					// spam them out
					for (int i = 0; i < size; i++)
					{
						final MsgSubscription sub = triggerVector.elementAt(i);
						log.debug("Trigger Signalling :" + i + " slaveChannelID=" + sub.channelID);
						queueMessageInSender(sub);
					}
				}
				// select pending time subscriptions
				final Vector<MsgSubscription> subVector = getTimeSubscriptions();
				final int size2 = subVector.size();
				// spam them out
				for (int i = 0; i < size2; i++)
				{
					final MsgSubscription sub = subVector.elementAt(i);
					log.debug("Time Signalling: " + i + ", slaveChannelID=" + sub.channelID);
					queueMessageInSender(sub);
				}
				if (size == 0 && size2 == 0)
					break; // flushed all
			}
		}

		/**
		 * queue a message in the appropriate sender
		 * @param sub
		 */
		private void queueMessageInSender(final MsgSubscription sub)
		{
			String url = sub.getURL();
			final JDFJMF signalJMF = sub.getSignal();
			if (signalJMF != null)
			{
				signalJMF.collectICSVersions();
				JMFFactory.send2URL(signalJMF, url, null, callback, deviceID);
			}
			else
			{
				log.debug("no Signal for subscription: " + sub);
			}
			// also notify that the trigger was processed in case of failure - else we wait a long time...
			if (sub.trigger != null)
				sub.trigger.setQueued();
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
				Vector<MsgSubscription> v = new Vector<MsgSubscription>();
				Iterator<Trigger> it = triggers.iterator(); // active triggers
				while (it.hasNext())
				{
					n++;
					Trigger t = it.next();
					String channelID = t.channelID;
					MsgSubscription sub = subscriptionMap.get(channelID);
					if (sub == null)
						continue; // snafu
					MsgSubscription subClone = (MsgSubscription) sub.clone();
					subClone.trigger = t;

					if (t.amount < 0)
					{
						v.add(subClone);
						if (sub.repeatTime <= 0) // don't update with real time in order to retain synchronized delta t
							sub.lastTime = System.currentTimeMillis() / 1000;

					}
					else if (t.amount > 0)
					{
						if (subClone.repeatAmount > 0)
						{
							int last = subClone.lastAmount;
							int next = last + t.amount;
							if (next / sub.repeatAmount > last / sub.repeatAmount)
							{
								sub.lastAmount = next; // not a typo - modify of nthe original subscription
								v.add(subClone);
								if (sub.repeatTime <= 0) // don't update with real time in order to retain synchronized delta t
									sub.lastTime = System.currentTimeMillis() / 1000;

							}
						}
					}
				}
				// remove active triggers that will be returned
				for (int j = 0; j < v.size(); j++)
				{
					MsgSubscription sub = v.elementAt(j);
					boolean b = triggers.remove(sub.trigger);
					if (!b)
						log.error("Snafu removing trigger");
				}
				return v;
			}
		}

		private Vector<MsgSubscription> getTimeSubscriptions()
		{
			Vector<MsgSubscription> subVector = new Vector<MsgSubscription>();
			synchronized (subscriptionMap)
			{
				Iterator<Entry<String, MsgSubscription>> it = subscriptionMap.entrySet().iterator();
				long now = System.currentTimeMillis() / 1000;
				while (it.hasNext())
				{
					final Entry<String, MsgSubscription> next = it.next();
					MsgSubscription sub = next.getValue();
					if (sub.repeatTime > 0)
					{
						if (now - sub.lastTime >= sub.repeatTime)
						{
							sub.lastTime += sub.repeatTime;
							// we had a snafu with a long break or are in setup - synchronize to now
							if (sub.lastTime < now)
								sub.lastTime = now;
							sub = (MsgSubscription) sub.clone();
							subVector.add(sub);
						}
					}
				}
			} // end synch map
			return subVector;
		}
	}

	private class MsgSubscription implements Cloneable
	{
		protected String channelID = null;
		protected String queueEntry = null;
		protected String url = null;
		protected int repeatAmount, lastAmount = 0;
		protected long lastTime = 0;
		protected long repeatTime = 0;
		protected JDFMessage theMessage = null;
		protected JDFJMF lastSentJMF = null;
		protected Trigger trigger = null;
		protected int sentMessages = 0;
		protected String jmfDeviceID = null;

		MsgSubscription(IJMFSubscribable m, String qeid)
		{
			JDFSubscription sub = m.getSubscription();
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
			//TODO observation targets
			if (repeatTime == 0 && repeatAmount == 0 && EnumType.Status.equals(theMessage.getType())) // reasonable default
			{
				repeatAmount = 100;
				repeatTime = 15;
			}
			JDFJMF ownerJMF = ((JDFMessage) m).getJMFRoot();
			jmfDeviceID = ownerJMF != null ? ownerJMF.getDeviceID() : null;
			if ("".equals(jmfDeviceID))
				jmfDeviceID = null;

		}

		/**
		 * get a signal that corresponds to this subscription
		 * 
		 * @return the jmf element that contains any signals generated by this subscription
		 * null if no signals were generated
		 */
		protected JDFJMF getSignal()
		{
			if (!(theMessage instanceof JDFQuery))
			{
				//TODO guess what...
				log.error("registrations not supported");
				return null;
			}
			JDFQuery q = (JDFQuery) theMessage;
			JDFJMF jmf = q.createResponse();
			JDFResponse r = jmf.getResponse(0);
			// make a copy so that modifications do not have an effect
			q = (JDFQuery) jmf.copyElement(q, null);
			q.removeChild(ElementName.SUBSCRIPTION, null, 0);
			jmf.setDeviceID(jmfDeviceID);
			// this is the handling of the actual message
			q.setAttribute(JMFHandler.subscribed, "true");
			boolean b = messageHandler.handleMessage(q, r);
			q.removeAttribute(JMFHandler.subscribed);
			if (!b)
			{
				log.debug("Unhandled message: " + q.getType());
				return null;
			}
			int nResp = jmf.numChildElements(ElementName.RESPONSE, null);
			int nSignals = jmf.numChildElements(ElementName.SIGNAL, null);
			JDFJMF jmfOut = (nResp + nSignals > 0) ? new JDFDoc("JMF").getJMFRoot() : null;
			for (int i = 0; i < nResp; i++)
			{
				JDFSignal s = jmfOut.getCreateSignal(i);
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

		private void finalizeSentMessages(JDFJMF jmfOut)
		{
			if (jmfOut == null)
				return;
			// this is a clone - need to update "the" subscription
			MsgSubscription parent = subscriptionMap.get(channelID);
			jmfOut.collectICSVersions();
			if (parent != null)
			{
				synchronized (parent)
				{
					parent.sentMessages++;
					parent.lastSentJMF = jmfOut;
				}
			}
			else
				log.error("no subscription for ChannelID=" + channelID);

		}

		/**
		 * @param jmfOut
		 * @return
		 */
		private JDFJMF filterSenderID(JDFJMF jmfOut)
		{
			if (KElement.isWildCard(jmfDeviceID))
				return jmfOut; // no filtering necessary
			if (jmfOut == null)
				return null;
			VElement v = jmfOut.getMessageVector(EnumFamily.Signal, null);
			int siz = v == null ? 0 : v.size();
			if (siz == 0)
				return null;
			for (int i = siz - 1; i >= 0; i--)
			{
				JDFSignal s = (JDFSignal) v.get(i);
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

		/* (non-Javadoc)
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
			catch (CloneNotSupportedException x)
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
			return "[MsgSubscription: slaveChannelID=" + channelID + " Type=" + getMessageType() + " QueueEntry="
					+ queueEntry + " lastAmount=" + lastAmount + "\nrepeatAmount=" + repeatAmount + " repeatTime="
					+ repeatTime + " lastTime=" + lastTime + "\nURL=" + url + " device ID=" + jmfDeviceID + " Sent="
					+ sentMessages + "]";
		}

		protected KElement appendToXML(KElement parent)
		{
			if (parent == null)
				return null;
			KElement sub = parent.appendElement("MsgSubscription");
			setXML(sub);
			return sub;

		}

		private void setXML(KElement sub)
		{
			sub.setAttribute(AttributeName.CHANNELID, channelID);
			sub.setAttribute(AttributeName.DEVICEID, jmfDeviceID);
			sub.setAttribute(AttributeName.QUEUEENTRYID, queueEntry);
			sub.setAttribute(AttributeName.URL, url);
			sub.setAttribute(AttributeName.REPEATTIME, repeatTime, null);
			sub.setAttribute(AttributeName.REPEATSTEP, repeatAmount, null);
			sub.setAttribute(AttributeName.TYPE, theMessage.getType());
			sub.setAttribute("Sent", sentMessages, null);
			sub.setAttribute("LastTime", sentMessages == 0 ? " - " : BambiServlet.formatLong(lastTime * 1000));
			sub.copyElement(theMessage, null);
		}

		/**
		 * @param queueEntryID
		 */
		protected void setQueueEntryID(String queueEntryID)
		{
			if (queueEntryID == null)
				return;
			if (theMessage == null)
				return;
			EnumType typ = theMessage.getEnumType();
			//TODO more message types
			if (EnumType.Status.equals(typ))
			{
				JDFStatusQuParams sqp = theMessage.getCreateStatusQuParams(0);
				sqp.setQueueEntryID(queueEntryID);
			}

		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof MsgSubscription))
				return false;
			MsgSubscription msg = (MsgSubscription) obj;
			if (repeatAmount != msg.repeatAmount || repeatTime != msg.repeatTime)
				return false;
			if (!ContainerUtil.equals(queueEntry, msg.queueEntry))
				return false;
			if (!ContainerUtil.equals(url, msg.url))
				return false;
			if (!ContainerUtil.equals(jmfDeviceID, msg.jmfDeviceID))
				return false;

			if (!ContainerUtil.equals(getMessageType(), msg.getMessageType()))
				return false;

			return true;
		}

		protected boolean matchesChannel(String _channelID)
		{
			return channelID.equals(_channelID);
		}

		protected boolean matchesQueueEntry(String qeID)
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

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			int hc = repeatAmount + 100000 * (int) repeatTime;
			hc += queueEntry == null ? 0 : queueEntry.hashCode();
			hc += url == null ? 0 : url.hashCode();
			String messageType = getMessageType();
			hc += messageType == null ? 0 : messageType.hashCode();
			return hc;
		}

		/**
		 * @param msgType
		 * @return true if this subscription is for this type
		 */
		public boolean matchesType(String msgType)
		{
			if (msgType == null)
				return true;
			return msgType.equals(theMessage.getType());
		}
	}

	/**
	 * 
	 * handler for the StopPersistentChannel command
	 */
	public class StopPersistentChannelHandler extends AbstractHandler
	{

		/**
		 * @param _type
		 * @param _families
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
		public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
		{
			if (!EnumType.StopPersistentChannel.equals(inputMessage.getEnumType()))
				return false;
			JDFStopPersChParams spcp = inputMessage.getStopPersChParams(0);
			if (spcp == null)
				return true;
			final String channel = spcp.getChannelID();
			if (!KElement.isWildCard(channel))
			{
				MsgSubscription mSub = removeSubScription(channel);
				if (response != null)
				{
					addToResponse(response, mSub);
				}
				return true;
			}
			String url = spcp.getURL();
			if (KElement.isWildCard(url))
			{
				JMFHandler.errorResponse(response, "No URL specified", 7, EnumClass.Error);
				return true;
			}
			String queueEntryID = spcp.getQueueEntryID();
			if (KElement.isWildCard(queueEntryID))
				queueEntryID = null;
			Vector<MsgSubscription> vSubs = removeSubScriptions(queueEntryID, url, null);
			if (vSubs == null)
			{
				JMFHandler.errorResponse(response, "No matching subscriptions found", 111, EnumClass.Error);
			}
			else
			{
				int size = vSubs.size();
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
		private void addToResponse(JDFResponse response, MsgSubscription sub)
		{
			// do nothing in this version

		}
	}

	/**
	 * constructor
	 * @param _messageHandler message handler
	 * @param devID ID of the device this SignalHandler is working for. 
	 * 			       Required for debugging purposes only. 
	 * @param cb the callback to modify any outgoing signals or incoming signal responses
	 */
	public SignalDispatcher(IMessageHandler _messageHandler, String devID, IConverterCallback cb)
	{
		deviceID = devID;
		subscriptionMap = new HashMap<String, MsgSubscription>();
		//       queueEntryMap=new VectorMap<String, String>();
		messageHandler = _messageHandler;
		triggers = new Vector<Trigger>();
		mutex = new Object();
		theDispatcher = new Dispatcher();
		new Thread(theDispatcher, "SignalDispatcher_" + deviceID).start();
		log.info("dispatcher thread 'SignalDispatcher_" + deviceID + "' started");
		callback = cb;
	}

	/**
	 * find subscriptions in a message and add them if appropriate
	 * @param m
	 * @param resp
	 */
	public void findSubscription(JDFMessage m, JDFResponse resp)
	{
		if (!(m instanceof IJMFSubscribable))
		{
			return;
		}
		IJMFSubscribable query = (IJMFSubscribable) m;
		JDFSubscription sub = query.getSubscription();
		if (sub == null)
			return;
		final String channelID = addSubscription(query, findQueueEntryID(m));
		if (resp != null && channelID != null)
			resp.setSubscribed(true);
	}

	/**
	 * @param m
	 * @return
	 */
	private String findQueueEntryID(JDFMessage m)
	{
		if (m == null)
			return null;
		try
		{
			final EnumType messageType = m.getEnumType();
			if (EnumType.Status.equals(messageType))
			{
				JDFStatusQuParams sqp = m.getStatusQuParams();
				String qeid = sqp == null ? null : sqp.getQueueEntryID();
				return qeid;
			}
			else if (EnumType.Resource.equals(messageType))
			{
				JDFResourceQuParams rqp = m.getResourceQuParams();
				String qeid = rqp == null ? null : rqp.getQueueEntryID();
				return qeid;
			}
		}
		catch (JDFException x)
		{ /* nop */
		}
		return null;
	}

	/**
	 * add a subscription
	 * returns the slaveChannelID of the new subscription, null if snafu
	 * @param subMess the subscription message - one of query or registration
	 * @param queueEntryID the associated QueueEntryID, may be null.
	 * @return the slaveChannelID of the subscription, if successful, else null
	 */
	public String addSubscription(IJMFSubscribable subMess, String queueEntryID)
	{
		if (subMess == null)
		{
			log.error("adding null subscription" + queueEntryID);
			return null;
		}
		if (isIgnoreURL(subMess))
			return null;
		log.info("adding subscription ");
		MsgSubscription sub = new MsgSubscription(subMess, queueEntryID);
		sub.setQueueEntryID(queueEntryID);
		if (sub.channelID == null || sub.url == null)
		{
			return null;
		}
		// no longer required
		//		if (ContainerUtil.equals(deviceID, BambiServlet.getDeviceIDFromURL(sub.url)))
		//		{
		//			log.warn("subscribing to self - ignore: " + deviceID);
		//			return null;
		//		}
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
		return sub.channelID;
	}

	/**
	 * @param subMess
	 * @return true if the message comes from a spam url that should be ignored
	 */
	private boolean isIgnoreURL(IJMFSubscribable subMess)
	{
		JDFSubscription sub = subMess.getSubscription();
		if (sub == null)
			return true;
		String url = sub.getURL();
		if (url == null)
			return true;
		if (ignoreURL != null && url.toLowerCase().indexOf(ignoreURL) >= 0)
			return true;
		return false;

	}

	/**
	 * add a subscription
	 * returns the slaveChannelID of the new subscription, null if snafu
	 * 
	 * @param node the node to search for inline jmfs
	 * @param queueEntryID the associated QueueEntryID, may be null.
	 * @return the channelIDs of the subscriptions, if successful, else null
	 */
	public VString addSubscriptions(JDFNode node, String queueEntryID)
	{
		if (node == null)
			return null;
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
			JDFAncestorPool ap = node.getRoot().getAncestorPool();
			if (ap != null)
				nodeInfo = (JDFNodeInfo) ap.getAncestorElement(ElementName.NODEINFO, null, "JMF");
			vs = nodeInfo == null ? null : addSubscriptions(nodeInfo, queueEntryID);
		}

		return vs;
	}

	/**
	 * @param nodeInfo
	 * @param queueEntryID
	 * @return the channelIDs of the subscriptions, if successful, else null
	 */
	private VString addSubscriptions(final JDFNodeInfo nodeInfo, String queueEntryID)
	{
		VElement vJMF = nodeInfo.getChildElementVector(ElementName.JMF, null, null, true, 0, true);
		int siz = vJMF == null ? 0 : vJMF.size();
		if (siz == 0)
			return null;
		VString vs = new VString();
		for (int i = 0; i < siz; i++)
		{
			JDFJMF jmf = nodeInfo.getJMF(i);
			// TODO regs
			VElement vMess = jmf.getMessageVector(EnumFamily.Query, null);
			if (vMess != null)
			{
				int nMess = vMess.size();
				for (int j = 0; j < nMess; j++)
				{
					JDFQuery q = (JDFQuery) vMess.elementAt(j);
					String channelID = addSubscription(q, queueEntryID);
					if (channelID != null)
						vs.add(channelID);
				}
			}
		}
		return vs;
	}

	/**
	 * remove a know subscription by channelid
	 * @param slaveChannelID the slaveChannelID of the subscription to remove
	 */
	public MsgSubscription removeSubScription(String channelID)
	{
		theDispatcher.flush();
		if (channelID == null)
			return null;
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
		return ret;
	}

	/**
	 * remove a know subscription by queueEntryID
	 * @param queueEntryID the queueEntryID of the subscriptions to remove
	 * @param url url of subscriptions to zapp
	 * @param messageType TODO
	 * @return the vector of remove subscriptions
	 */
	public Vector<MsgSubscription> removeSubScriptions(String queueEntryID, String url, String messageType)
	{
		Vector<MsgSubscription> vSubs = new Vector<MsgSubscription>();
		synchronized (subscriptionMap)
		{
			Iterator<String> it = subscriptionMap.keySet().iterator();
			boolean allURL = KElement.isWildCard(url);
			boolean allQE = KElement.isWildCard(queueEntryID);
			boolean allType = KElement.isWildCard(messageType);
			VString v = new VString();
			while (it.hasNext())
			{
				final String channelID = it.next();
				if (!allURL || !allQE)
				{
					MsgSubscription sub = subscriptionMap.get(channelID);
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
						JDFMessage mess = sub.theMessage;
						if (mess != null)
						{
							String typ = mess.getType();
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
				MsgSubscription mSub = removeSubScription(v.stringAt(i));
				if (mSub != null)
					vSubs.add(mSub);
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
	public Trigger triggerChannel(String channelID, String queueEntryID, NodeIdentifier nodeIdentifier, int amount, boolean last, boolean ignoreIfTime)
	{
		MsgSubscription s = subscriptionMap.get(channelID);
		Trigger tNew = null;
		if (s != null)
		{
			if (!ignoreIfTime || s.repeatTime <= 0)
			{

				tNew = new Trigger(queueEntryID, nodeIdentifier, channelID, amount);
				synchronized (triggers)
				{
					Trigger t = getTrigger(tNew);

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
					lastCalled++;
			}
		}
		if (last && lastCalled > 0)
		{
			synchronized (mutex)
			{
				mutex.notifyAll();
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
	private Trigger getTrigger(Trigger new1)
	{
		if (triggers == null)
			return null;
		final int size = triggers.size();
		for (int i = 0; i < size; i++)
		{
			if (triggers.get(i).equals(new1))
				return triggers.get(i);
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

	public Trigger[] triggerQueueEntry(String queueEntryID, NodeIdentifier nodeID, int amount, String msgType)
	{
		Vector<MsgSubscription> v = ContainerUtil.toValueVector(subscriptionMap, false);
		int si = v == null ? 0 : v.size();
		if (si == 0)
			return null;
		Trigger[] locTriggers = new Trigger[si];
		int n = 0;
		for (int i = 0; i < si; i++)
		{
			MsgSubscription sub = v.get(i);
			if (sub.matchesQueueEntry(queueEntryID) && sub.matchesType(msgType))
			{
				locTriggers[n++] = triggerChannel(sub.channelID, queueEntryID, nodeID, amount, i + 1 == si, false);
			}
		}
		if (n == 0)
			return null;
		if (n < si)
		{
			Trigger[] t2 = new Trigger[n];
			for (int i = 0; i < n; i++)
				t2[i] = locTriggers[i];
			locTriggers = t2;
		}
		return locTriggers;
	}

	/**
	 * @param jmfHandler
	 */
	public void addHandlers(IJMFHandler jmfHandler)
	{
		jmfHandler.addHandler(this.new StopPersistentChannelHandler());
	}

	/**
	 * stop the dispatcher thread
	 */
	public void shutdown()
	{
		doShutdown = true;
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public boolean handleGet(BambiServletRequest request, BambiServletResponse response)
	{
		return this.new XMLSubscriptions().handleGet(request, response);
	}

	public Set<String> getChannels(EnumType typ, String senderID)
	{
		Set<String> keySet = subscriptionMap.keySet();

		Iterator<String> it = keySet.iterator();
		Set<String> keySet2 = new HashSet<String>();
		String nam = typ == null ? null : typ.getName();
		while (it.hasNext())
		{
			String key = it.next();
			MsgSubscription sub = subscriptionMap.get(key);
			if (nam == null || nam.equals(sub.getMessageType())
					&& (sub.jmfDeviceID == null || sub.jmfDeviceID.equals(senderID)))
			{
				keySet2.add(key);
			}
		}
		return keySet2;
	}

	/**
	 * flush any waiting messages
	 */
	public void flush()
	{
		synchronized (mutex)
		{
			mutex.notifyAll();
		}
	}

}
