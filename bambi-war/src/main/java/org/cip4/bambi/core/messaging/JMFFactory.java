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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Vector;

import javax.mail.Multipart;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * factory for sending JMF messages
 * 
 * @author boegerni
 * 
 */
public class JMFFactory extends BambiLogFactory
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
		 * @param _url the url
		 * 
		 */
		public CallURL(final String _url)
		{
			url = _url;
			url = getBaseURL();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			final String baseUrl = getBaseURL();
			return (baseUrl == null ? 0 : baseUrl.hashCode());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "[CallURL: " + getBaseURL() + "]";
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
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
		@Override
		public int compareTo(final CallURL o)
		{
			return ContainerUtil.compare(url, o == null ? null : o.url);
		}
	}

	// end of inner class CallURL

	// ////////////////////////////////////////////////////////////

	private static JMFFactory theFactory = null;
	private static MyMutex factoryMutex = new MyMutex();
	final HashMap<CallURL, MessageSender> senders = new HashMap<CallURL, MessageSender>();
	private int nThreads = 0;
	private final boolean shutdown = false;
	private final HashMap<EnumType, IMessageOptimizer> optimizers;
	private final String devID;
	private final long startTime;

	/**
	 * @param deviceID
	 */
	public JMFFactory(final String deviceID) // all static
	{
		super();
		optimizers = new HashMap<EnumType, IMessageOptimizer>();
		devID = deviceID;
		startTime = System.currentTimeMillis();
	}

	/**
	 * @return
	 */
	public static JMFFactory getJMFFactory()
	{
		synchronized (factoryMutex)
		{
			if (theFactory == null)
			{
				theFactory = new JMFFactory("static");
			}
		}
		return theFactory;
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
	 * @return 
	 */
	public boolean send2URL(final Multipart mp, final String url, final IResponseHandler handler, final IConverterCallback callback, final MIMEDetails md, final String deviceID)
	{
		if (shutdown)
		{
			return false;
		}

		if (mp == null || url == null)
		{
			log.error("failed to send JDFMessage, message and/or URL is null");
			return false;
		}

		final MessageSender ms = getCreateMessageSender(url);
		return ms.queueMimeMessage(mp, handler, callback, md, deviceID, url);
	}

	/**
	 * POSTS a empty url message to a given URL
	 * 
	 * @param url the URL to send the JMF to
	 * @param handler
	 * @param callback
	 * @return 
	 */
	public boolean send2URL(final String url, final IResponseHandler handler, final IConverterCallback callback)
	{
		if (shutdown)
		{
			return false;
		}

		if (url == null)
		{
			log.error("failed to send empty post, URL is null");
			return false;
		}

		final MessageSender ms = getCreateMessageSender(url);
		return ms.queuePost(handler, url, callback);
	}

	/**
	 * sends a JMF message to a given URL
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param handler
	 * @param callback
	 * @param senderID the senderID of the caller
	 * @return true if successfully queued
	 */
	public boolean send2URL(final JDFJMF jmf, final String url, final IResponseHandler handler, final IConverterCallback callback, final String senderID)
	{
		if (shutdown)
		{
			return false;
		}
		if (jmf == null || url == null)
		{
			log.error("failed to send JDFMessage, message and/or URL is null");
			return false;
		}

		final MessageSender ms = getCreateMessageSender(url);
		if (senderID != null)
		{
			jmf.setSenderID(senderID);
		}
		return ms.queueMessage(jmf, handler, url, callback);
	}

	/**
	 * sends a JMF message to a given URL synchronously
	 * @param jmf the message to send
	 * @param url the URL to send the JMF to
	 * @param callback
	 * @param senderID the senderID of the caller
	 * @param milliSeconds timeout to wait
	 * @return the response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler handler = new MessageResponseHandler((String) null);
		send2URL(jmf, url, handler, callback, senderID);
		handler.waitHandled(milliSeconds, 10000, true);
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
		final MessageResponseHandler handler = new MessageResponseHandler((String) null);
		send2URL(jmf, url, handler, callback, senderID);
		handler.waitHandled(milliSeconds, 10000, true);
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
		final MessageResponseHandler handler = new MessageResponseHandler((String) null);
		send2URL(mp, url, handler, callback, md, senderID);
		handler.waitHandled(milliSeconds, 10000, true);
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
			log.info("shutting down all senders " + (graceFully ? "gracefully" : "forced"));
			final Vector<CallURL> keySet = ContainerUtil.getKeyVector(senders);
			if (keySet != null)
			{
				for (CallURL s : keySet)
				{
					shutDown(s, graceFully);
				}
			}
			log.info("completed shutting down all senders " + (graceFully ? "gracefully" : "forced"));
		}
		else
		// individual url
		{
			final MessageSender ms = senders.get(cu);
			if (ms != null)
			{
				log.info("shutting down sender " + cu.getBaseURL() + (graceFully ? " gracefully" : " forced"));
				ms.shutDown(graceFully);
			}
			else
			{
				log.warn("no sender to shut down: " + cu.getBaseURL() + (graceFully ? " gracefully" : " forced"));
			}
			senders.remove(cu);
		}
	}

	/**
	 * @param typ message type
	 * @param opt the optimizer to call
	 */
	public void addOptimizer(final EnumType typ, final IMessageOptimizer opt)
	{
		if (typ != null && opt != null)
		{
			optimizers.put(typ, opt);
		}
	}

	/**
	 * @param typ
	 * @return
	 */
	public IMessageOptimizer getOptimizer(final EnumType typ)
	{
		return typ == null ? null : optimizers.get(typ);
	}

	/**
	 * @return Vector of all known message senders matching url; null if none match
	 * @param url the url to match, if null all
	 */
	public Vector<MessageSender> getMessageSenders(String url)
	{
		url = UrlUtil.normalize(url);
		Vector<MessageSender> v = ContainerUtil.toValueVector(senders, true);
		if (StringUtil.getNonEmpty(url) == null)
		{
			return v;
		}

		if (v != null)
		{
			for (int i = v.size() - 1; i >= 0; i--)
			{
				final MessageSender messageSender = v.get(i);
				if (!messageSender.matchesURL(url))
				{
					v.remove(i);
				}
			}
			if (v.size() == 0)
			{
				v = null;
			}
		}
		return v;
	}

	/**
	 * get an existing MessageSender or create it if it does not exist for a given url or callback
	 * 
	 * @param url the URL to send a message to, if null use the fire&forget sender
	 * 
	 * @return the MessageSender that will queue and dispatch the message
	 * 
	 */
	public MessageSender getCreateMessageSender(final String url)
	{
		if (url == null)
		{
			log.warn("attempting to retrieve MessageSender for null");
			return null;
		}
		if (shutdown)
		{
			log.warn("attempting to retrieve MessageSender after shutdown for " + url);
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
				ms.setStartTime(startTime);
				ms.setJMFFactory(this);
				senders.put(cu, ms);
				String name = "MessageSender_" + devID + "_" + nThreads++ + "_" + cu.getBaseURL();
				Thread thread = new Thread(ms, name);
				log.info("creating new message sender " + name);
				thread.setDaemon(false);
				thread.start();
			}
			return ms;
		}
	}

	/**
	 * cleanup idle threads
	 */
	private void cleanIdleSenders()
	{
		synchronized (senders)
		{
			final Vector<MessageSender> v = new Vector<MessageSender>();
			for (MessageSender ms : senders.values())
			{
				if (!ms.isRunning())
				{
					v.add(ms);
				}
			}
			for (MessageSender ms : v)
			{
				senders.remove(ms);
				log.info("removing idle message sender " + ms.getCallURL().getBaseURL());
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "JMFFactory " + devID + " threads=" + nThreads;
	}
}