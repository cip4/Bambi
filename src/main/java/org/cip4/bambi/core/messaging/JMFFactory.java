/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */

package org.cip4.bambi.core.messaging;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * Factory class for sending JMF Messages.
 */
public class JMFFactory
{

	protected final Log log = BambiLogFactory.getLog(JMFFactory.class);

	/**
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	public static class CallURL implements Comparable<CallURL>
	{
		final String url;

		/**
		 * Returns the base url that is used to define equal senders.
		 *
		 * @return The base url
		 */
		public String getBaseURL(String inputUrl)
		{
			if (inputUrl == null)
			{
				return null;
			}
			VString tokenizedUrl = StringUtil.tokenize(inputUrl, "?", false);
			int len = tokenizedUrl.size();

			while (len-- > 1)
			{
				inputUrl = StringUtil.removeToken(inputUrl, -1, "?");
			}
			tokenizedUrl = StringUtil.tokenize(inputUrl, "/", false);
			len = tokenizedUrl.size();
			while (len-- > 3)
			{
				inputUrl = StringUtil.removeToken(inputUrl, -1, "/");
			}

			return UrlUtil.normalize(inputUrl);
		}

		/**
		 * Custom constructor. Accepting an url for initializing.
		 *
		 * @param url The call url
		 */
		public CallURL(final String url)
		{
			this.url = getBaseURL(url);
		}

		public String getURL()
		{
			return url;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return (url == null ? 0 : url.hashCode());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "[CallURL: " + url + "]";
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
			return ContainerUtil.equals(url, other.url);
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

	private static JMFFactory INSTANCE = null;
	private static MyMutex factoryMutex = new MyMutex();

	final HashMap<CallURL, MessageSender> senders = new HashMap<>();
	private int numberOfThreads = 0;
	private final boolean shutdown = false;
	private final HashMap<EnumType, IMessageOptimizer> messageOptimizers;
	private final long startTime;
	private boolean zapp500;
	private boolean logLots;

	/**
	 * @return the logLots
	 */
	public boolean isLogLots()
	{
		return logLots;
	}

	/**
	 * @param logLots the logLots to set
	 */
	public void setLogLots(final boolean logLots)
	{
		this.logLots = logLots;
	}

	/**
	 * Private default constructor. This class cannot be instantiated form outside.
	 */
	private JMFFactory() // all static
	{
		super();
		messageOptimizers = new HashMap<>();
		startTime = System.currentTimeMillis();
		zapp500 = false;
		logLots = false;
	}

	/**
	 * @return the zapp500
	 */
	public boolean isZapp500()
	{
		return zapp500;
	}

	/**
	 * @param zapp500 the zapp500 to set
	 */
	public void setZapp500(final boolean zapp500)
	{
		this.zapp500 = zapp500;
	}

	/**
	 * Returns the only instance of JMFFactory.
	 *
	 * @return The only JMFFactory instance.
	 */
	public static JMFFactory getInstance()
	{
		synchronized (factoryMutex)
		{
			if (INSTANCE == null)
			{
				INSTANCE = new JMFFactory();
			}
		}
		return INSTANCE;
	}

	/**
	 * Returns the only instance of JMFFactory.
	 *
	 * @return The only JMFFactory instance.
	 * @deprecated use getInstance
	 */
	@Deprecated
	public static JMFFactory getJMFFactory()
	{
		return getInstance();
	}

	/**
	 *
	 */
	public static void shutdown()
	{
		if (INSTANCE != null)
		{
			INSTANCE.shutDown(null, true);
			if (INSTANCE.senders.size() > 0)
			{
				ThreadUtil.sleep(1234);
				INSTANCE.shutDown(null, false);
			}
			INSTANCE = null;
		}
	}

	/**
	 * sends a mime or zip multipart message to a given URL
	 *
	 * @param jmf
	 * @param jdf
	 * @param url      the URL to send the JMF to
	 * @param handler
	 * @param callback
	 * @param md
	 * @param deviceID
	 * @return
	 */
	public boolean send2URL(final JDFJMF jmf, final JDFNode jdf, final String url, final IResponseHandler handler, final IConverterCallback callback,
			final MIMEDetails md, final String deviceID)
	{
		if (shutdown)
		{
			return false;
		}

		boolean ok = true;
		if (jmf == null)
		{
			log.error("failed to send JDFMessage, jmf is null");
			ok = false;
		}
		else if (jdf == null)
		{
			log.error("failed to send JDFMessage, jdf is null");
			ok = false;
		}
		else if (url == null)
		{
			log.error("failed to send JDFMessage, URL is null");
			ok = false;
		}
		if (!ok)
		{
			return false;
		}

		if (deviceID != null)
		{
			jmf.setSenderID(deviceID);
		}

		final MessageSender messageSender = getCreateMessageSender(url);
		return messageSender.queueMessage(jmf, jdf, handler, url, callback, md);
	}

	/**
	 * POSTS a empty url message to a given URL
	 *
	 * @param url      the URL to send the JMF to
	 * @param handler  The message repsonse handler.
	 * @param callback The calback converter.
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

		final MessageSender messageSender = getCreateMessageSender(url);
		return messageSender.queuePost(handler, url, callback);
	}

	/**
	 * Sends a JMF message to a given URL
	 *
	 * @param jmf      the message to send
	 * @param url      the URL to send the JMF to
	 * @param handler  The response handler.
	 * @param callback The callback converter.
	 * @param senderID The senderID of the caller
	 * @return True, in case message has been successfully queued
	 */
	public boolean send2URL(final JDFJMF jmf, final String url, final IResponseHandler handler, final IConverterCallback callback, final String senderID)
	{
		return send2URL(jmf, url, handler, callback, senderID, null);
	}

	/**
	 * Sends a JMF message to a given URL synchronously
	 *
	 * @param jmf          The message to send
	 * @param url          The URL to send the JMF to
	 * @param callback     The callback converter
	 * @param senderID     The senderID of the caller
	 * @param milliSeconds Timeout to wait
	 * @return The response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler messageResponseHandler = new MessageResponseHandler((String) null);
		send2URL(jmf, url, messageResponseHandler, callback, senderID);
		messageResponseHandler.waitHandled(milliSeconds, 10000, true);
		return messageResponseHandler.getConnection();
	}

	/**
	 * Sends a JMF message to a given URL synchronously
	 *
	 * @param jmf          The JMF Message to send
	 * @param url          The target URL to send the JMF to
	 * @param callback     The callback converter.
	 * @param senderID     The senderID of the caller
	 * @param milliSeconds timeout to wait
	 * @return the response if successful, otherwise null
	 */
	public JDFResponse send2URLSynchResp(final JDFJMF jmf, final String url, final IConverterCallback callback, final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler messageResponseHandler = new MessageResponseHandler((String) null);
		send2URL(jmf, url, messageResponseHandler, callback, senderID);
		messageResponseHandler.waitHandled(milliSeconds, 10000, true);
		final HttpURLConnection connection = messageResponseHandler.getConnection();

		if (connection != null)
		{
			final JDFDoc jdfDoc = JDFDoc.parseStream(messageResponseHandler.getBufferedStream());

			if (jdfDoc != null)
			{
				final JDFJMF jmfRoot = jdfDoc.getJMFRoot();
				return jmfRoot == null ? null : jmfRoot.getResponse(0);
			}
		}
		return null;
	}

	/**
	 * Sends a mime multipart package to a given URL synchronously
	 *
	 * @return the response if successful, otherwise null
	 */
	public HttpURLConnection send2URLSynch(final JDFJMF jmf, final JDFNode jdf, final String url, final IConverterCallback callback, final MIMEDetails md,
			final String senderID, final int milliSeconds)
	{
		final MessageResponseHandler messageResponseHandler = new MessageResponseHandler((String) null);
		send2URL(jmf, jdf, url, messageResponseHandler, callback, md, senderID);
		messageResponseHandler.waitHandled(milliSeconds, milliSeconds, true);
		return messageResponseHandler.getConnection();
	}

	/**
	 * Shut down all message senders.
	 *
	 * @param graceFully True, in case the message sender should be shut down gracefully. Otherwise false.
	 */
	public void shutDown(final boolean graceFully)
	{
		shutDown(null, graceFully);
	}

	/**
	 * Shut down one or multiple message sender.
	 *
	 * @param callURL    The callURL to shut down, if null all of them
	 * @param graceFully True, in case the message sender should be shut down gracefully. Otherwise false.
	 */
	public void shutDown(final CallURL callURL, final boolean graceFully)
	{
		if (callURL == null)
		{
			// shutdown all (callURL = null)
			log.info("shutting down all senders ");
			final Vector<CallURL> callURLs = ContainerUtil.getKeyVector(senders);

			if (callURLs != null)
			{
				for (final CallURL s : callURLs)
				{
					shutDown(s, graceFully);
				}
			}

			log.info("completed shutting down all senders ");
		}
		else
		{
			// shut down an message sender for a specific url
			final MessageSender messageSender = senders.get(callURL);

			if (messageSender != null)
			{
				log.info("shutting down sender " + callURL.getURL() + (graceFully ? " gracefully" : " forced"));
				messageSender.shutDown();
			}
			else
			{
				log.warn("no sender to shut down: " + callURL.getURL() + (graceFully ? " gracefully" : " forced"));
			}

			senders.remove(callURL);
		}
	}

	/**
	 * Add and message optimizer object.
	 *
	 * @param type             message type
	 * @param messageOptimizer The actual optimizer object to add
	 */
	public void addOptimizer(final EnumType type, final IMessageOptimizer messageOptimizer)
	{
		if (type != null && messageOptimizer != null)
		{
			messageOptimizers.put(type, messageOptimizer);
		}
	}

	/**
	 * Returns the message optimizer object.
	 *
	 * @param type The type of the optimizer object.
	 * @return The Message optimizer of a defined type.
	 */
	public IMessageOptimizer getOptimizer(final EnumType type)
	{
		return type == null ? null : messageOptimizers.get(type);
	}

	/**
	 * Returns a vector of all known message senders matching a specific url - null if none matches.
	 *
	 * @param url the url to match, if null all
	 * @return Vector of all known message senders matching url; null if none match
	 */
	public Vector<MessageSender> getMessageSenders(String url)
	{
		if (url == null)
		{
			return ContainerUtil.toValueVector(senders, true);
		}
		else
		{
			CallURL cu = new CallURL(url);
			Vector<MessageSender> v = new Vector<>();
			ContainerUtil.add(v, senders.get(cu));
			return v;
		}

	}

	/**
	 * Returns an existing MessageSender or create it if it does not exist for a given url or callback.
	 *
	 * @param url The URL to send a message to, if null use the fire&forget sender
	 * @return The MessageSender that will queue and dispatch the message
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

		final CallURL callURL = new CallURL(url);

		synchronized (senders)
		{
			MessageSender messageSender = senders.get(callURL);

			if (messageSender != null && !messageSender.isRunning())
			{
				shutDown(callURL, false);
				log.info("removed idle message sender " + callURL.url);
				messageSender = null;
			}

			if (messageSender == null)
			{

				// clean up senders
				cleanIdleSenders();

				// create new message sender for callUrl
				messageSender = new MessageSender(callURL);
				messageSender.setStartTime(startTime);
				messageSender.setJMFFactory(this);
				senders.put(callURL, messageSender);

				// increment number of threads
				numberOfThreads++;

				// set up new message sender thread
				final String threadName = "MessageSender_" + numberOfThreads + "_" + callURL.getURL();
				final Thread thread = new Thread(messageSender, threadName);
				log.info("creating new message sender: " + threadName);
				thread.setDaemon(false);
				thread.start();
			}

			return messageSender;
		}
	}

	/**
	 * Cleanup idle message senders.
	 */
	private void cleanIdleSenders()
	{
		synchronized (senders)
		{
			final Vector<MessageSender> idleMessageSenders = new Vector<>();

			// find idle message senders
			for (final MessageSender messageSender : senders.values())
			{
				if (!messageSender.isRunning())
				{
					idleMessageSenders.add(messageSender);
				}
			}

			// clean up idle message senders
			for (final MessageSender messageSender : idleMessageSenders)
			{
				messageSender.shutDown();
				log.info("removing idle message sender " + messageSender.getCallURL().getURL());
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "JMFFactory : threads=" + numberOfThreads + " zapp500=" + zapp500 + " logLots=" + logLots + " Senders: " + senders;
	}

	/**
	 * Sends a JMF message to a given URL.
	 *
	 * @param jmf      The message to send
	 * @param url      The URL to send the JMF to
	 * @param handler  The response handler.
	 * @param callback The callback converter.
	 * @param senderID The senderID of the caller
	 * @return true if message has been successfully queued
	 */
	public boolean send2URL(final JDFJMF jmf, final String url, final IResponseHandler handler, final IConverterCallback callback, final String senderID,
			final HTTPDetails det)
	{
		if (shutdown)
		{
			return false;
		}

		if (jmf == null)
		{
			log.error("Failed to send JDFMessage, message is null");
			return false;
		}
		else if (url == null)
		{
			log.error("Failed to send JDFMessage, target URL is null");
			return false;
		}

		final MessageSender messageSender = getCreateMessageSender(url);

		if (senderID != null)
		{
			jmf.setSenderID(senderID);
		}

		// return the return value of queueMessage method (true / false)
		return messageSender.queueMessage(jmf, handler, url, callback, det);
	}
}