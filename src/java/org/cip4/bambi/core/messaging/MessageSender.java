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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.mail.Multipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.JMFFactory.CallURL;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.VectorMap;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;

/**
 * allow a JMF message to be send in its own thread
 * @author boegerni
 */
public class MessageSender implements Runnable
{
	private final CallURL callURL;

	private enum sendReturn
	{
		sent, empty, error, removed
	}

	/**
	 * @return the callURL associated with this
	 */
	public CallURL getCallURL()
	{
		return callURL;
	}

	private boolean doShutDown = false;
	private boolean doShutDownGracefully = false;
	private Vector<MessageDetails> _messages = null;
	private static Log log = LogFactory.getLog(MessageSender.class.getName());
	private static VectorMap<String, DumpDir> vDumps = new VectorMap<String, DumpDir>();
	private final Object mutexDispatch = new Object();
	private int sent = 0;
	private int idle = 0;
	private long created = 0;
	private long lastQueued = 0;
	private long lastSent = 0;

	protected class MessageDetails
	{
		protected JDFJMF jmf = null;
		protected Multipart mime = null;
		protected IResponseHandler respHandler;
		protected MIMEDetails mimeDet;
		protected String senderID = null;
		protected String url = null;
		protected IConverterCallback callback;

		protected MessageDetails(JDFJMF _jmf, IResponseHandler _respHandler, IConverterCallback _callback, HTTPDetails hdet, String detailedURL)
		{
			respHandler = _respHandler;
			jmf = _jmf;
			senderID = jmf == null ? null : jmf.getSenderID();
			url = detailedURL;
			callback = _callback;
			if (hdet == null)
			{
				mimeDet = null;
			}
			else
			{
				mimeDet = new MIMEDetails();
				mimeDet.httpDetails = hdet;
			}
		}

		protected MessageDetails(Multipart _mime, IResponseHandler _respHandler, IConverterCallback _callback, MIMEDetails mdet, String _senderID, String _url)
		{
			respHandler = _respHandler;
			mime = _mime;
			mimeDet = mdet;
			senderID = _senderID;
			url = _url;
			callback = _callback;
		}
	}

	/**
	 * trivial response handler that simply grabs the response and passes it back through
	 * getResponse() / isHandled()
	 * @author Rainer Prosi
	 *
	 */
	public static class MessageResponseHandler implements IResponseHandler
	{
		private final JDFResponse resp = null;
		private HttpURLConnection connect = null;
		protected BufferedInputStream bufferedInput = null;
		private Object mutex = new Object();
		private int abort = 0; // 0 no abort handling, 1= abort on timeou, 2= has been aborted

		/**
		 * 
		 */
		public MessageResponseHandler()
		{
			super();
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		/**
		 * @see org.cip4.bambi.core.messaging.IResponseHandler#handleMessage()
		 * @return true if handled
		 */
		public boolean handleMessage()
		{
			finalizeHandling();
			return true;
		}

		/**
		 * 
		 */
		protected void finalizeHandling()
		{
			if (mutex == null)
				return;
			abort = 0;
			synchronized (mutex)
			{
				mutex.notifyAll();
			}
			mutex = null;
		}

		public JDFResponse getResponse()
		{
			return resp;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
		 */
		public HttpURLConnection getConnection()
		{
			return connect;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
		 */
		public void setConnection(HttpURLConnection uc)
		{
			connect = uc;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(java.io.BufferedInputStream)
		 */
		public void setBufferedStream(BufferedInputStream bis)
		{
			bufferedInput = bis;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(java.io.BufferedInputStream)
		 */
		public InputStream getBufferedStream()
		{
			if (bufferedInput != null)
				return bufferedInput;
			if (connect == null)
				return null;
			try
			{
				bufferedInput = new BufferedInputStream(connect.getInputStream());
			}
			catch (IOException x)
			{
				//nop 
			}
			return bufferedInput;
		}

		/**
		 * @param i
		 */
		public void waitHandled(int i, boolean bAbort)
		{
			if (mutex == null)
				return;
			abort = bAbort ? 1 : 0;
			synchronized (mutex)
			{
				try
				{
					mutex.wait(i);
				}
				catch (InterruptedException x)
				{
					//nop
				}
			}
			if (abort == 1)
				abort++;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.messaging.IResponseHandler#isAborted()
		 */
		public boolean isAborted()
		{
			return mutex == null ? false : abort == 2;
		}
	}

	/**
	 * constructor
	 * @param theUrl the URL to send the message to
	 */
	public MessageSender(String theUrl)
	{
		_messages = new Vector<MessageDetails>();
		callURL = new CallURL(theUrl);
		created = System.currentTimeMillis();
	}

	/**
	 * constructor
	 * @param cu the URL to send the message to
	 */

	public MessageSender(CallURL cu)
	{
		_messages = new Vector<MessageDetails>();
		callURL = cu;
		created = System.currentTimeMillis();
	}

	/**
	 * the sender loop. <br/>
	 * Checks whether its vector of pending messages is empty. If it is not empty, 
	 * the first message is sent and removed from the map.
	 */
	public void run()
	{
		while (!doShutDown)
		{
			sendReturn sentFirstMessage;
			try
			{
				synchronized (_messages)
				{
					sentFirstMessage = sendFirstMessage();
					if (sentFirstMessage == sendReturn.sent)
					{
						_messages.remove(0);
						sent++;
						lastSent = System.currentTimeMillis();
						idle = 0;
					}

					if (doShutDownGracefully && (_messages.isEmpty() || idle > 10)) // idle > 10 blasts this away if doShutDownGracefully and we are having problems
					{
						doShutDown = true;
					}
				}
			}
			catch (Exception x)
			{
				sentFirstMessage = sendReturn.error;
			}
			if (sentFirstMessage != sendReturn.sent)
			{
				if (idle++ > 3600)
				{
					// no success or idle for an hour...
					doShutDown = true;
					log.info("Shutting down thread for base url: " + callURL.getBaseURL());
				}
				else
				{ // stepwise increment - try every second 10 times, then every minute, then every 5 minutes 
					final int wait = (sendReturn.error == sentFirstMessage && idle > 10) ? (idle > 100 ? 300000 : 60000) : 1000;
					try
					{
						synchronized (mutexDispatch)
						{
							mutexDispatch.wait(wait);
						}
					}
					catch (InterruptedException x)
					{
						//nop
					}
				}
			}
		}
	}

	/**
	 * send the first enqueued message and return true if all went well
	 * also update any returned responses for Bambi internally
	 * @return boolean true if the message is assumed sent
	 *                 false if an error was detected and the Message must remain in the queue
	 */
	private sendReturn sendFirstMessage()
	{
		synchronized (_messages)
		{

			if (_messages == null || _messages.isEmpty())
				return sendReturn.empty;

			sendReturn b = sendReturn.sent;
			MessageDetails mh = _messages.get(0);
			if (mh == null)
			{
				_messages.remove(0);
				return sendReturn.removed; // should never happen
			}
			final DumpDir outDump = getOutDump(mh.senderID);
			final DumpDir inDump = getInDump(mh.senderID);

			JDFJMF jmf = mh.jmf;
			Multipart mp = mh.mime;
			if (KElement.isWildCard(mh.url))
			{
				log.error("Sending to bad url - bailing out! " + mh.url);
				return sendReturn.error; // snafu anyhow but not sent but no retry useful
			}
			if (jmf == null && mp == null)
			{
				log.error("Sending neither mime nor jmf - bailing out?");
				_messages.remove(0);
				return sendReturn.removed; // need no resend - will remove
			}

			if (mh.respHandler != null && mh.respHandler.isAborted())
			{
				_messages.remove(0);
				log.warn("removed aborted message to: " + mh.url);
				return sendReturn.removed;
			}

			try
			{
				HttpURLConnection con;
				String header = "URL: " + mh.url;

				if (jmf != null)
				{
					final JDFDoc jmfDoc = jmf.getOwnerDocument_JDFElement();
					HTTPDetails hd = mh.mimeDet == null ? null : mh.mimeDet.httpDetails;
					con = jmfDoc.write2HTTPURL(new URL(mh.url), hd);
					if (outDump != null)
					{
						File dump = outDump.newFile(header);
						if (dump != null)
						{
							FileOutputStream fos = new FileOutputStream(dump, true);
							jmfDoc.write2Stream(fos, 0, true);
						}
					}
				}
				else if (mp != null)
				{
					con = MimeUtil.writeToURL(mp, mh.url, mh.mimeDet);
					if (outDump != null)
					{
						File dump = outDump.newFile(header);
						if (dump != null)
						{
							FileOutputStream fos = new FileOutputStream(dump, true);
							MimeUtil.writeToStream(mp, fos, mh.mimeDet);
						}
					}
				}
				else
				{
					log.error("Sending neither mime nor jmf - bailing out?");
					_messages.remove(0);
					return sendReturn.removed; // nothing to send; remove it
				}

				if (con != null)
				{
					header += "\nResponse code:" + con.getResponseCode();
					header += "\nContent type:" + con.getContentType();
					header += "\nContent length:" + con.getContentLength();
				}

				if (con != null && con.getResponseCode() == 200)
				{
					BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
					bis.mark(1000000);

					if (inDump != null)
					{
						inDump.newFileFromStream(header, bis);
					}
					if (mh.respHandler != null)
					{
						mh.respHandler.setConnection(con);
						mh.respHandler.setBufferedStream(bis);
						b = mh.respHandler.handleMessage() ? sendReturn.sent : sendReturn.error;
					}
				}
				else
				{
					b = sendReturn.error;
					if (idle == 0)// only warn on first try
						log.warn("could not send message to " + mh.url + " rc= "
								+ ((con == null) ? -1 : con.getResponseCode()));
					if (con != null)
					{
						if (inDump != null)
						{
							inDump.newFile(header);
						}
					}
				}
			}
			catch (Exception e)
			{
				log.error("Exception in sendfirstmessage", e);
				b = sendReturn.error;
			}
			return b;
		}
	}

	/**
	 * stop sending new messages immediately and shut down
	 * @param gracefully true  - process remaining messages first, then shut down. <br/>
	 *                   false - shut down immediately, skip remaining messages.
	 */
	public void shutDown(boolean gracefully)
	{
		if (gracefully)
		{
			doShutDownGracefully = true;
		}
		else
		{
			doShutDown = true;
		}
	}

	/**
	 * return true if the thread is still running
	 * @return true if running
	 */
	public boolean isRunning()
	{
		return !doShutDown;
	}

	/**
	 * return true if tesatURL fits this url
	 * @param testURL the url to check against
	 * @return true if running
	 */
	public boolean matchesURL(String testURL)
	{
		return ContainerUtil.equals(testURL, callURL.getBaseURL());
	}

	private DumpDir getInDump(String senderID)
	{
		return vDumps.getOne(senderID, 0);
	}

	private DumpDir getOutDump(String senderID)
	{
		return vDumps.getOne(senderID, 1);
	}

	/**
	 * add debug dump directories for a given senderID
	 * @param senderID
	 * @param inDump
	 * @param outDump
	 */
	public static void addDumps(String senderID, DumpDir inDump, DumpDir outDump)
	{
		vDumps.putOne(senderID, inDump);
		vDumps.putOne(senderID, outDump);
	}

	/**
	 * queues a message for the URL that this MessageSender belongs to
	 * also updates the message for a given recipient if required
	 * @param jmf the message to send
	 * @param handler 
	 * @param url 
	 * @return true, if the message is successfully queued. 
	 *         false, if this MessageSender is unable to accept further messages (i. e. it is shutting down). 
	 */
	public boolean queueMessage(JDFJMF jmf, IResponseHandler handler, String url, IConverterCallback _callBack)
	{
		if (doShutDown || doShutDownGracefully)
		{
			return false;
		}
		if (_callBack != null)
			_callBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());

		MessageDetails messageDetails = new MessageDetails(jmf, handler, _callBack, null, url);
		queueMessageDetails(messageDetails);
		return true;
	}

	/**
	 * @param messageDetails
	 */
	private void queueMessageDetails(MessageDetails messageDetails)
	{
		synchronized (_messages)
		{
			_messages.add(messageDetails);
			lastQueued = System.currentTimeMillis();
		}
		synchronized (mutexDispatch)
		{
			mutexDispatch.notifyAll();
		}
	}

	/**
	 * queues a message for the URL that this MessageSender belongs to
	 * also updates the message for a given recipient if required
	 * @param multpart 
	 * @param handler 
	 * @param md 
	 * @param senderID 
	 * @param url 
	 * @return true, if the message is successfully queued. 
	 *         false, if this MessageSender is unable to accept further messages (i. e. it is shutting down). 
	 */
	public boolean queueMimeMessage(Multipart multpart, IResponseHandler handler, IConverterCallback callback, MIMEDetails md, String senderID, String url)
	{
		if (doShutDown || doShutDownGracefully)
		{
			return false;
		}

		MessageDetails messageDetails = new MessageDetails(multpart, handler, callback, md, senderID, url);
		queueMessageDetails(messageDetails);
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	@Override
	public String toString()
	{
		return "MessageSender - URL: " + callURL.url + " size: " + _messages.size() + " total: " + sent
				+ " last queued at " + BambiServlet.formatLong(lastQueued) + " last sent at "
				+ BambiServlet.formatLong(lastSent) + "\n" + _messages;
	}

	/**
	 * creates a descriptive xml Object for this MessageSender
	 * 
	 * @param root the parent into which I append myself
	 */
	public void appendToXML(KElement root)
	{
		synchronized (_messages)
		{
			KElement ms = root.appendElement("MessageSender");
			ms.setAttribute(AttributeName.URL, callURL.url);
			ms.setAttribute(AttributeName.SIZE, _messages.size(), null);
			ms.setAttribute("NumSent", sent, null);
			ms.setAttribute("LastQueued", BambiServlet.formatLong(lastQueued), null);
			ms.setAttribute("LastSent", BambiServlet.formatLong(lastSent), null);
			ms.setAttribute(AttributeName.CREATIONDATE, BambiServlet.formatLong(created), null);
			ms.setAttribute("Active", !doShutDown, null);
		}
	}

}
