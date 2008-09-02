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
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.MessageSender.MessageResponseHandler;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
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
				return null;
			VString v = StringUtil.tokenize(url, "/?", true);
			int len = v.size();
			if (len > 6)
				len = 6; //6= host / / root </?> last 
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < len; i++)
				b.append(v.get(i));
			return b.toString();
		}

		/**
		 * @param _callback the conversion callback for this sender
		 * @param _url the url
		 * 
		 */
		public CallURL(String _url)
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
		public boolean equals(Object obj)
		{
			if (!(obj instanceof CallURL))
				return false;
			CallURL other = (CallURL) obj;
			return ContainerUtil.equals(getBaseURL(), other.getBaseURL());
		}

		/**
		 * compares based on url values
		 * 
		 * @param o the other callURL to compare to
		 * @return -1 if this is smaller
		 */
		public int compareTo(CallURL o)
		{
			return ContainerUtil.compare(url, o == null ? null : o.url);
		}
	}

	// end of inner class CallURL

	//////////////////////////////////////////////////////////////

	private static Log log = LogFactory.getLog(JMFFactory.class.getName());
	static HashMap<CallURL, MessageSender> senders = new HashMap<CallURL, MessageSender>();
	private static int nThreads = 0;

	public JMFFactory()
	{
		super();
	}

	/**
	 * build a JMF SuspendQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to suspend
	 * @return the message
	 */
	public static JDFJMF buildSuspendQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.SuspendQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF HoldQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to hold
	 * @return the message
	 */
	public static JDFJMF buildHoldQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.HoldQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF ResumeQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to resume
	 * @return the message
	 */
	public static JDFJMF buildResumeQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.ResumeQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF AbortQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to abort
	 * @return the message
	 */
	public static JDFJMF buildAbortQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.AbortQueueEntry);
		return jmf;
	}

	/**
	 * @param queueEntryId
	 * @return
	 */
	private static JDFJMF buildQueueEntryCommand(String queueEntryId, EnumType typ)
	{
		if (queueEntryId == null)
			return null;
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, typ);
		jmf.getCommand(0).appendQueueEntryDef().setQueueEntryID(queueEntryId);
		return jmf;
	}

	/**
	 * build a JMF RemoveQueueEntry command
	 * @param queueEntryId queue entry ID of the queue to remove
	 * @return the message
	 */
	public static JDFJMF buildRemoveQueueEntry(String queueEntryId)
	{
		JDFJMF jmf = buildQueueEntryCommand(queueEntryId, EnumType.RemoveQueueEntry);
		return jmf;
	}

	/**
	 * build a JMF Status query
	 * @return the message
	 */
	public static JDFJMF buildStatus()
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
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
	public static JDFJMF buildStatusSubscription(String subscriberURL, double repeatTime, int repeatStep, String queueEntryID)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Status, subscriberURL, repeatTime, repeatStep);
		if (queueEntryID != null)
			jmf.getQuery(0).getCreateStatusQuParams(0).setQueueEntryID(queueEntryID);
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
	public static JDFJMF buildResourceSubscription(String subscriberURL, double repeatTime, int repeatStep, String queueEntryID)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Resource, subscriberURL, repeatTime, repeatStep);
		if (queueEntryID != null)
			jmf.getQuery(0).getCreateResourceQuParams(0).setQueueEntryID(queueEntryID);
		return jmf;
	}

	/**
	 * build a JMF Notification subscription
	 * @param subscriberURL 
	 * @param repeatTime 
	 * @param repeatStep 
	 * @return the message
	 */
	public static JDFJMF buildNotificationSubscription(String subscriberURL, double repeatTime, int repeatStep)
	{
		final JDFJMF jmf = buildSubscription(EnumType.Notification, subscriberURL, repeatTime, repeatStep);
		return jmf;
	}

	/**
	 * build a generic subscription for a given type
	 * @return the message
	 */
	private static JDFJMF buildSubscription(EnumType typ, String subscriberURL, double repeatTime, int repeatStep)
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, typ);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setURL(subscriberURL);
		if (repeatTime > 0)
			s.setRepeatTime(repeatTime);
		if (repeatStep > 0)
			s.setRepeatStep(repeatStep);
		s.appendObservationTarget().setObservationPath("*");

		return jmf;
	}

	/**
	 * build a JMF QueueStatus query
	 * @return the message
	 */
	public static JDFJMF buildQueueStatus()
	{
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.QueueStatus);
		return jmf;
	}

	/**
	 * build a JMF RequestQueueEntry command <br/>
	 *    default: JMFFactory.buildRequestQueueEntry(theQueueURL,null)
	 * @param queueURL the queue URL of the device sending the command
	 * 				   ("where do you want your SubmitQE's delivered to?")
	 * @param deviceID the DeviceID of the worker requesting the QE (default=null)
	 * @return the message
	 */
	public static JDFJMF buildRequestQueueEntry(String queueURL, String deviceID)
	{
		// maybe replace DeviceID with DeviceType, just to be able to decrease the 
		// Proxy's knowledge about querying devices?
		JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Command, EnumType.RequestQueueEntry);
		JDFRequestQueueEntryParams qep = jmf.getCommand(0).appendRequestQueueEntryParams();
		qep.setQueueURL(queueURL);
		BambiNSExtension.setDeviceID(qep, deviceID);
		return jmf;
	}

	/**
	 * sends a JMF message to a given URL
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @return the response if successful, otherwise null
	 */
	public void send2URL(Multipart mp, String url, IResponseHandler handler, IConverterCallback callback, MIMEDetails md, String deviceID)
	{

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

		MessageSender ms = getCreateMessageSender(url);
		ms.queueMimeMessage(mp, handler, callback, md, deviceID, url);
	}

	/**
	 * sends a JMF message to a given URL
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param senderID the senderID of the caller
	 * @param milliSeconds timout to wait
	 * @return the response if successful, otherwise null
	 */
	public void send2URL(JDFJMF jmf, String url, IResponseHandler handler, IConverterCallback callback, String senderID)
	{

		if (jmf == null || url == null)
		{
			if (log != null)
			{
				// this method is prone for crashing on shutdown, thus checking for 
				// log!=null is important
				log.error("failed to send JDFMessage, message and/or URL is null");
			}
			return;
		}

		MessageSender ms = getCreateMessageSender(url);
		if (senderID != null)
			jmf.setSenderID(senderID);
		ms.queueMessage(jmf, handler, url, callback);
	}

	/**
	 * sends a JMF message to a given URL sychronusly
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param senderID the senderID of the caller
	 * @param milliSeconds timout to wait
	 * @return the response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(JDFJMF jmf, String url, IConverterCallback callback, String senderID, int milliSeconds)
	{
		MessageResponseHandler handler = new MessageResponseHandler();
		send2URL(jmf, url, handler, callback, senderID);
		handler.waitHandled(milliSeconds, true);
		return handler.getConnection();
	}

	/**
	 * sends a JMF message to a given URL sychronusly
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param senderID the senderID of the caller
	 * @param milliSeconds timout to wait
	 * @return the response if successful, otherwise null
	 */
	public JDFResponse send2URLSynchResp(JDFJMF jmf, String url, IConverterCallback callback, String senderID, int milliSeconds)
	{
		MessageResponseHandler handler = new MessageResponseHandler();
		send2URL(jmf, url, handler, callback, senderID);
		handler.waitHandled(milliSeconds, true);
		HttpURLConnection uc = handler.getConnection();
		if (uc != null)
		{
			JDFDoc d = new JDFParser().parseStream(handler.getBufferedStream());
			if (d != null)
			{
				final JDFJMF root = d.getJMFRoot();
				return root == null ? null : root.getResponse(0);
			}
		}
		return null;
	}

	/**
	 * sends a JMF message to a given URL sychronusly
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @return the response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(Multipart mp, String url, IConverterCallback callback, MIMEDetails md, String senderID, int milliSeconds)
	{
		MessageResponseHandler handler = new MessageResponseHandler();
		send2URL(mp, url, handler, callback, md, senderID);
		handler.waitHandled(milliSeconds, true);
		return handler.getConnection();
	}

	/**
	 * 
	 * @param url
	 */
	public static void shutDown(CallURL cu, boolean graceFully)
	{
		if (cu == null) // null = all
		{
			final Set<CallURL> keySet = senders.keySet();
			CallURL[] as = keySet.toArray(new CallURL[keySet.size()]);
			for (int i = 0; i < as.length; i++)
			{
				CallURL s = as[i];
				if (s != null)
				{
					shutDown(s, graceFully);
				}
				senders.remove(s);
			}
		}
		else
		// individual url
		{
			MessageSender ms = senders.get(cu);
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
	public static Vector<MessageSender> getAllMessageSenders()
	{
		return ContainerUtil.toValueVector(senders, true);
	}

	/**
	 * get an existing MessageSender for a given url or callback
	 * 
	 * @param url the URL to send a message to
	 * @param callBack the callback to use
	 * 
	 * @return the MessageSender that will queue and dispatch the message
	 * 
	 */
	public static MessageSender getCreateMessageSender(String url)
	{
		if (url == null)
			return null;
		CallURL cu = new CallURL(url);

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
				// cleanup idle threads
				Iterator<MessageSender> it = senders.values().iterator();
				Vector<MessageSender> v = new Vector<MessageSender>();
				while (it.hasNext())
				{
					MessageSender ms2 = it.next();
					if (!ms2.isRunning())
						v.add(ms2);
				}
				for (int i = 0; i < v.size(); i++)
					senders.remove(v.get(i));

				ms = new MessageSender(cu);
				senders.put(cu, ms);
				new Thread(ms, "MessageSender_" + nThreads++ + "_" + cu.getBaseURL()).start();

			}

			return ms;
		}
	}
}