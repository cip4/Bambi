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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.mail.Multipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.MessageSender.MessageResponseHandler;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.resource.JDFResource.EnumResourceClass;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

/**
 * factory for creating JMF messages
 * 
 * @author boegerni
 * 
 */
public class JMFFactory
{
	/**
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 * 
	 */
	public static class CallURL implements Comparable<CallURL>
	{
		String url;

		/**
		 * get the base url that is used to define equal senders
		 * @return the base url
		 */
		public String getBaseURL()
		{
			if (url == null)
			{
				return null;
			}
			final VString v = StringUtil.tokenize(url, "/?", true);
			int len = v.size();
			if (len > 6)
			{
				len = 6; // 6= host / / root </?> last
			}
			final StringBuffer b = new StringBuffer();
			for (int i = 0; i < len; i++)
			{
				b.append(v.get(i));
			}
			return b.toString();
		}

		/**
		 * @param _callback the conversion callback for this sender
		 * @param _url the url
		 * 
		 */
		public CallURL(final String _url)
		{
			url = _url;
			url = getBaseURL();
		}

		@Override
		public int hashCode()
		{
			final String baseUrl = getBaseURL();
			return (baseUrl == null ? 0 : baseUrl.hashCode());
		}

		@Override
		public String toString()
		{
			return "[CallURL: " + getBaseURL() + "]";
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (!(obj instanceof CallURL))
			{
				return false;
			}
			final CallURL other = (CallURL) obj;
			return ContainerUtil.equals(getBaseURL(), other.getBaseURL());
		}

		/**
		 * compares based on url values
		 * 
		 * @param o the other callURL to compare to
		 * @return -1 if this is smaller
		 */
		public int compareTo(final CallURL o)
		{
			return ContainerUtil.compare(url, o == null ? null : o.url);
		}
	}

	// end of inner class CallURL

	// ////////////////////////////////////////////////////////////

	private static Log log = LogFactory.getLog(JMFFactory.class.getName());
	private static JMFFactory theFactory = null;
	private static Object mutex = new Object();
	HashMap<CallURL, MessageSender> senders = new HashMap<CallURL, MessageSender>();
	private int nThreads = 0;
	private final boolean shutdown = false;

	private JMFFactory() // all static
	{
		super();
	}

	public static JMFFactory getJMFFactory()
	{
		synchronized (mutex)
		{
			if (theFactory == null)
			{
				theFactory = new JMFFactory();
			}
		}
		return theFactory;
	}

	/**
	 * build a JMF SuspendQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to suspend
	 * @return the message
	 */
	public JDFJMF buildSuspendQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.SuspendQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF HoldQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to hold
	 * @return the message
	 */
	public JDFJMF buildHoldQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.HoldQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF ResumeQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to resume
	 * @return the message
	 */
	public JDFJMF buildResumeQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.ResumeQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF AbortQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to abort
	 * @return the message
	 */
	public JDFJMF buildAbortQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.AbortQueueEntry);
		return jmf;
	}

	/**
	 * @param queueEntryId
	 * @param typ
	 * @return the jmf
	 */
	private JDFJMF buildQueueEntryCommand(final String queueEntryId, final EnumType typ)
	{
		if (queueEntryId == null)
		{
			return null;
		}
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, typ);
		final JDFCommand command = jmf.getCommand(0);
		command.appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}

	/**
	 * build a JMF RemoveQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to remove
	 * @return the message
	 */
	public JDFJMF buildRemoveQueueEntry(final String queueEntryId)
	{
		final JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.RemoveQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF Status query
	 * @return the message
	 */
	public JDFJMF buildStatus()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
		return jmf;
	}

	/**
	 * build a JMF Status subscription
	 * @param subscriberURL
	 * @param repeatTime
	 * @param repeatStep
	 * @param queueEntryID
	 * @return the message
	 */
	public JDFJMF buildStatusSubscription(final String subscriberURL, final double repeatTime, final int repeatStep, final String queueEntryID)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Status, subscriberURL, repeatTime, repeatStep);
		final JDFQuery query = jmf.getQuery(0);
		final JDFStatusQuParams statusQuParams = query.getCreateStatusQuParams(0);
		statusQuParams.setJobDetails(EnumJobDetails.Brief);
		statusQuParams.setDeviceDetails(EnumDeviceDetails.Brief);

		if (queueEntryID != null)
		{
			statusQuParams.setQueueEntryID(queueEntryID);
		}
		return jmf;
	}

	/**
	 * build a JMF Resource subscription
	 * @param subscriberURL
	 * @param repeatTime
	 * @param repeatStep
	 * @param queueEntryID
	 * @return the message
	 */
	public JDFJMF buildResourceSubscription(final String subscriberURL, final double repeatTime, final int repeatStep, final String queueEntryID)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Resource, subscriberURL, repeatTime, repeatStep);
		final JDFQuery query = jmf.getQuery(0);
		final JDFResourceQuParams resourceQuParams = query.getCreateResourceQuParams(0);
		final Vector<EnumResourceClass> c = new Vector<EnumResourceClass>();
		c.add(EnumResourceClass.Consumable);
		c.add(EnumResourceClass.Handling);
		c.add(EnumResourceClass.Implementation);
		resourceQuParams.setClasses(c);
		if (queueEntryID != null)
		{
			resourceQuParams.setQueueEntryID(queueEntryID);
		}
		return jmf;
	}

	/**
	 * build a JMF Notification subscription
	 * @param subscriberURL
	 * @return the message
	 */
	public JDFJMF buildNotificationSubscription(final String subscriberURL)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Notification, subscriberURL, 0, 0);
		return jmf;
	}

	/**
	 * build a generic subscription for a given type
	 * @param typ
	 * @param subscriberURL
	 * @param repeatTime
	 * @param repeatStep
	 * @return the message
	 */
	private JDFJMF buildSubscription(final EnumType typ, final String subscriberURL, final double repeatTime, final int repeatStep)
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, typ);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setURL(subscriberURL);
		if (repeatTime > 0)
		{
			s.setRepeatTime(repeatTime);
		}
		if (repeatStep > 0)
		{
			s.setRepeatStep(repeatStep);
		}
		s.appendObservationTarget().setObservationPath("*");

		return jmf;
	}

	/**
	 * build a JMF QueueStatus query
	 * @return the message
	 */
	public JDFJMF buildQueueStatus()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus);
		return jmf;
	}

	/**
	 * build a JMF RequestQueueEntry command <br/>
	 * default: JMFFactory.buildRequestQueueEntry(theQueueURL,null)
	 * @param queueURL the queue URL of the device sending the command ("where do you want your SubmitQE's delivered to?")
	 * @param nid the nodeidentifier of the requested qe, default=null
	 * @return the message
	 */
	public JDFJMF buildRequestQueueEntry(final String queueURL, final NodeIdentifier nid)
	{
		// maybe replace DeviceID with DeviceType, just to be able to decrease the
		// Proxy's knowledge about querying devices?
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.RequestQueueEntry);
		final JDFRequestQueueEntryParams qep = jmf.getCommand(0).appendRequestQueueEntryParams();
		qep.setQueueURL(queueURL);
		qep.setIdentifier(nid);
		return jmf;
	}

	/**
	 * sends a mime multipart message to a given URL
	 * 
	 * @param mp
	 * @param url the URL to send the JMF to
	 * @param handler
	 * @param callback
	 * @param md
	 * @param deviceID
	 */
	public void send2URL(final Multipart mp, final String url, final IResponseHandler handler, final IConverterCallback callback, final MIMEDetails md, final String deviceID)
	{
		if (shutdown)
		{
			return;
		}

		if (mp == null || url == null)
		{
			if (log != null)
			{
				// this method is prone for crashing on shutdown, thus checking for
				// log!=null is important
				log.error("failed to send JDFMessage, message and/or URL is null");
			}
			return;
		}

		final MessageSender ms = getCreateMessageSender(url);
		ms.queueMimeMessage(mp, handler, callback, md, deviceID, url);
	}

	/**
	 * sends a JMF message to a given URL
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param handler
	 * @param callback
	 * @param senderID the senderID of the caller
	 */
	public void send2URL(final JDFJMF jmf, final String url, final IResponseHandler handler, final IConverterCallback callback, final String senderID)
	{
		if (shutdown)
		{
			return;
		}
		if (jmf == null || url == null)
		{
			if (log != null)
			{
				// this method is prone for crashing on shutdown, thus checking for log!=null is important
				log.error("failed to send JDFMessage, message and/or URL is null");
			}
			return;
		}

		final MessageSender ms = getCreateMessageSender(url);
		if (senderID != null)
		{
			jmf.setSenderID(senderID);
		}
		ms.queueMessage(jmf, handler, url, callback);
	}

	/**
	 * sends a JMF message to a given URL sychronusly
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param callback
	 * @param senderID the senderID of the caller
	 * @param milliSeconds timeout to wait
	 * @return the response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler handler = new MessageResponseHandler();
		send2URL(jmf, url, handler, callback, senderID);
		handler.waitHandled(milliSeconds, true);
		return handler.getConnection();
	}

	/**
	 * sends a JMF message to a given URL sychronusly
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param callback
	 * @param senderID the senderID of the caller
	 * @param milliSeconds timeout to wait
	 * @return the response if successful, otherwise null
	 */
	public JDFResponse send2URLSynchResp(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler handler = new MessageResponseHandler();
		send2URL(jmf, url, handler, callback, senderID);
		handler.waitHandled(milliSeconds, true);
		final HttpURLConnection uc = handler.getConnection();
		if (uc != null)
		{
			final JDFDoc d = new JDFParser().parseStream(handler.getBufferedStream());
			if (d != null)
			{
				final JDFJMF root = d.getJMFRoot();
				return root == null ? null : root.getResponse(0);
			}
		}
		return null;
	}

	/**
	 * sends a mime multipart package to a given URL synchronously
	 * @param mp the mime multipart to send
	 * @param url the URL to send the JMF to
	 * @param callback
	 * @param md
	 * @param senderID
	 * @param milliSeconds
	 * 
	 * @return the response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(final Multipart mp, final String url, final IConverterCallback callback, final MIMEDetails md, final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler handler = new MessageResponseHandler();
		send2URL(mp, url, handler, callback, md, senderID);
		handler.waitHandled(milliSeconds, true);
		return handler.getConnection();
	}

	/**
	 * 
	 * @param cu the callURL to shut down, if null all of them
	 * @param graceFully
	 */
	public void shutDown(final CallURL cu, final boolean graceFully)
	{
		if (cu == null) // null = all
		{
			final Set<CallURL> keySet = senders.keySet();
			final CallURL[] as = keySet.toArray(new CallURL[keySet.size()]);
			for (int i = 0; i < as.length; i++)
			{
				final CallURL s = as[i];
				if (s != null)
				{
					shutDown(s, graceFully);
				}
			}
		}
		else
		// individual url
		{
			final MessageSender ms = senders.get(cu);
			if (ms != null)
			{
				ms.shutDown(graceFully);
			}
			senders.remove(cu);
		}
	}

	/**
	 * @return Vector of all known message senders
	 */
	public Vector<MessageSender> getAllMessageSenders()
	{
		return ContainerUtil.toValueVector(senders, true);
	}

	/**
	 * get an existing MessageSender for a given url or callback
	 * 
	 * @param url the URL to send a message to
	 * 
	 * @return the MessageSender that will queue and dispatch the message
	 * 
	 */
	public MessageSender getCreateMessageSender(final String url)
	{
		if (url == null)
		{
			return null;
		}
		final CallURL cu = new CallURL(url);

		synchronized (senders)
		{
			MessageSender ms = senders.get(cu);
			if (ms != null && !ms.isRunning())
			{
				senders.remove(cu);
				log.info("removing idle message sender " + cu.url);
				ms = null;
			}
			if (ms == null)
			{
				cleanIdleSenders();
				ms = new MessageSender(cu);
				senders.put(cu, ms);
				new Thread(ms, "MessageSender_" + nThreads++ + "_" + cu.getBaseURL()).start();
			}
			return ms;
		}
	}

	/**
	 * cleanup idle threads
	 */
	private void cleanIdleSenders()
	{
		final Iterator<MessageSender> it = senders.values().iterator();
		final Vector<MessageSender> v = new Vector<MessageSender>();
		while (it.hasNext())
		{
			final MessageSender ms2 = it.next();
			if (!ms2.isRunning())
			{
				v.add(ms2);
			}
		}
		for (int i = 0; i < v.size(); i++)
		{
			senders.remove(v.get(i));
			log.info("removing idle message sender " + v.get(i).getCallURL().getBaseURL());
		}
	}

	/**
	 * create a set of default subscriptions
	 * @param url
	 * @param queueEntryID
	 * @param repeatTime
	 * @param repeatStep
	 *@return the array of subscriptions to be sent
	 */
	public JDFJMF[] createSubscriptions(final String url, final String queueEntryID, final double repeatTime, final int repeatStep)
	{
		final JDFJMF jmfs[] = new JDFJMF[3];
		jmfs[0] = buildStatusSubscription(url, repeatTime, repeatStep, queueEntryID);
		jmfs[1] = buildResourceSubscription(url, 0, 0, queueEntryID);
		jmfs[2] = buildNotificationSubscription(url);
		return jmfs;
	}

	/**
	 * build a stopPersistentChannelParams message
	 * @param channelID
	 * @param url the url of the subscription
	 * @return the jmf
	 */
	public JDFJMF buildStopPersistentChannel(final String channelID, final String url)
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.StopPersistentChannel);
		final JDFCommand c = jmf.getCommand(0);
		final JDFStopPersChParams scp = c.appendStopPersChParams();
		scp.setChannelID(channelID);
		scp.setURL(url);
		return jmf;

	}
}