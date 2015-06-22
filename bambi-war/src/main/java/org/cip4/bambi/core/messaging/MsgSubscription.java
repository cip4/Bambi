/**
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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.ifaces.IJMFSubscribable;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FastFiFo;
import org.cip4.jdflib.util.StringUtil;

/**
 * 
 */
public class MsgSubscription implements Cloneable
{
	/**
	 * 
	 */
	private final SignalDispatcher signalDispatcher;
	protected static final String SUBSCRIPTION_ELEMENT = "MsgSubscription";
	protected String channelID;
	protected String queueEntry;
	protected String url;
	protected int repeatAmount, lastAmount;
	/**
	 * the last successful submission
	 */
	protected long lastTime;
	/**
	 * the last submission attempt
	 */
	protected long lastTry;
	protected long repeatTime;
	protected JDFMessage theMessage = null;
	protected FastFiFo<JDFJMF> lastSentJMF;
	protected Trigger trigger = null;
	protected int sentMessages = 0;
	protected String jmfDeviceID = null; // the senderID of the incoming (subscribed) jmf
	final Log log;

	/**
	 * 
	 * @param m
	 * @param qeid
	 * @param signalDispatcher TODO
	 */
	MsgSubscription(SignalDispatcher signalDispatcher, final IJMFSubscribable m, final String qeid)
	{
		this(signalDispatcher);
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

		repeatAmount = sub.getRepeatStep();
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
		if ("".equals(jmfDeviceID) || ContainerUtil.equals(jmfDeviceID, this.signalDispatcher.device.getDeviceID()))
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

		jmf.setDeviceID(jmfDeviceID);
		// this is the handling of the actual message
		q.setAttribute(JMFHandler.subscribed, "true");
		final boolean b = signalDispatcher.handleMessage(q, r);
		q.removeAttribute(JMFHandler.subscribed);
		if (!b && log.isDebugEnabled())
		{
			log.debug("Unhandled message: " + q.getType());
			return null;
		}
		final int nResp = jmf.numChildElements(ElementName.RESPONSE, null);
		final int nSignals = jmf.numChildElements(ElementName.SIGNAL, null);
		JDFJMF jmfOut = (nResp + nSignals > 0) ? new JDFDoc(ElementName.JMF).getJMFRoot() : null;
		for (int i = 0; i < nResp; i++)
		{
			final JDFSignal s = jmfOut.getCreateSignal(i);
			r = jmf.getResponse(i);
			s.convertResponse(r, q);
		}
		for (int i = 0; i < nSignals; i++)
		{
			JDFSignal s = jmf.getSignal(i);
			jmfOut.copyElement(s, null);
		}
		jmfOut = filterSenders(jmfOut);
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
		updateChannelMode(jmfOut);
	}

	protected void updateChannelMode(final JDFJMF jmfOut)
	{
		JDFSubscription subscription = ((JDFQuery) theMessage).getSubscription();
		String channelMode = subscription.getAttribute(AttributeName.CHANNELMODE, null, null);
		if (channelMode != null)
		{
			Vector<JDFSignal> signals = jmfOut.getChildrenByClass(JDFSignal.class, false, 0);
			for (JDFSignal s : signals)
			{
				s.setAttribute(AttributeName.CHANNELMODE, channelMode);
			}
		}
	}

	/**
	 * @param jmfOut
	 * @return
	 */
	private JDFJMF filterSenders(final JDFJMF jmfOut)
	{
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
			if (!StringUtil.matchesSimple(s.getSenderID(), jmfDeviceID) || signalDispatcher.device.deleteSignal(s))
			{
				if (s != null)
				{
					s.deleteNode();
				}
				v.remove(i);
			}
		}
		return v.size() == 0 ? null : jmfOut;
	}

	/**
	 * 
	 *  
	 * @return
	 */
	public String getURL()
	{
		return url;
	}

	/**
	 * 
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MsgSubscription clone()
	{
		MsgSubscription c = new MsgSubscription(this.signalDispatcher);
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
		sub.setAttribute("LastTime", sentMessages == 0 ? " - " : XMLResponse.formatLong((lastTime * 1000)));
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
						//							final XJDF20 x2 = new XJDF20();
						//							x2.setUpdateVersion(false);
						//							final KElement newJMF = message.copyElement(x2.makeNewJMF(sentArray[i]), null);
						final KElement newJMF = message.copyElement(sentArray[i], null);
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
		sub.setAttribute(AttributeName.SENDERID, this.signalDispatcher.device == null ? "test" : this.signalDispatcher.device.getDeviceID());

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
	 * creates a MsgSubscription  
	 * 
	 * - must be maintained in synch with @see setXML (duh...)
	 * @param signalDispatcher TODO
	 *  
	 */
	MsgSubscription(SignalDispatcher signalDispatcher)
	{
		this.signalDispatcher = signalDispatcher;
		log = LogFactory.getLog(getClass());
		channelID = null;
		jmfDeviceID = null;
		queueEntry = null;
		url = null;
		repeatTime = 0;
		repeatAmount = 0;
		sentMessages = 0;
		lastAmount = 0;
		lastTime = 0;
		lastTime = 0;
		lastSentJMF = new FastFiFo<JDFJMF>(10);
	}

	/**
	 * creates a MsgSubscription from an XML element
	 * 
	 * - must be maintained in synch with @see setXML (duh...)
	 * @param sub
	 * @param signalDispatcher 
	 */
	MsgSubscription(SignalDispatcher signalDispatcher, final KElement sub)
	{
		this(signalDispatcher);
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
		else
		{
			theMessage = null;
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