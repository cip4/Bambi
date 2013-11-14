/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2013 The International Cooperation for the Integration of 
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.Multipart;

import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.messaging.IMessageOptimizer.optimizeResult;
import org.cip4.bambi.core.messaging.JMFFactory.CallURL;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FastFiFo;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlPart;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;
import org.cip4.jdflib.util.VectorMap;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * allow a JMF message to be sent in its own thread
 * 
 * @author boegerni
 */
public class MessageSender extends BambiLogFactory implements Runnable
{
	private final CallURL callURL;
	protected JMFFactory myFactory;

	enum SendReturn
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
	protected Vector<MessageDetails> _messages = null;
	protected FastFiFo<MessageDetails> sentMessages = null;
	private static VectorMap<String, DumpDir> vDumps = new VectorMap<String, DumpDir>();
	private final MyMutex mutexDispatch = new MyMutex();
	private final MyMutex mutexPause = new MyMutex();
	private int trySend;
	private int sent;
	private boolean waitKaputt;
	protected int checked;
	protected int removedHeartbeat;
	protected int removedFireForget;
	protected int removedError;
	private int idle = 0;
	private final CPUTimer timer;
	private long created;
	private long lastQueued = 0;
	private long lastSent = 0;
	private boolean pause = false;
	private static File baseLocation = null;

	/**
	 * @return the baseLocation where all message related files are stored
	 */
	public static File getBaseLocation()
	{
		return baseLocation;
	}

	private SenderQueueOptimizer optimizer = null;
	private long startTime;

	protected class SenderQueueOptimizer extends BambiLogFactory
	{
		/**
		 * 
		 */
		public SenderQueueOptimizer()
		{
			super();
		}

		/**
		 * 
		 * @param jmf
		 */
		protected void optimize(final JDFJMF jmf)
		{
			if (jmf == null)
			{
				return;
			}
			final VElement messages = jmf.getMessageVector(null, null);
			if (messages == null)
			{
				return;
			}
			for (int i = 0; i < messages.size(); i++)
			{
				optimizeMessage((JDFMessage) messages.get(i));
			}

		}

		/**
		 * @param newMessage
		 */
		private void optimizeMessage(final JDFMessage newMessage)
		{
			if (!(newMessage instanceof JDFSignal))
			{
				return;
			}
			final EnumType typ = newMessage.getEnumType();
			if (typ == null)
			{
				return;
			}
			final IMessageOptimizer opt = myFactory.getOptimizer(typ);
			if (opt == null)
			{
				return;
			}
			for (int i = _messages.size() - 1; i >= 0; i--)
			{
				final JDFJMF jmf = _messages.get(i).jmf;
				if (jmf == null)
				{
					continue; // don't optimize mime packages
				}
				final VElement v2 = jmf.getMessageVector(null, null);
				if (v2 == null)
				{
					continue;
				}
				for (int ii = v2.size() - 1; ii >= 0; ii--)
				{
					final JDFMessage mOld = (JDFMessage) v2.get(ii);
					if (mOld instanceof JDFSignal)
					{
						final optimizeResult res = opt.optimize(newMessage, mOld);
						if (res == optimizeResult.remove)
						{
							removeMessage(mOld, i);
						}
						else if (res == optimizeResult.cont)
						{
							return; // we found a non matching message and must stop optimizing
						}
					}
				}
			}
		}

		/**
		 * @param old
		 * @param i
		 */
		private void removeMessage(final JDFMessage old, final int i)
		{
			final JDFJMF jmf = old.getJMFRoot();
			jmf.removeChild(old);
			log.info("removed redundant " + old.getType() + " " + old.getLocalName() + " Message ID= " + old.getID() + " Sender= " + old.getSenderID());
			final VElement v = jmf.getMessageVector(null, null);
			if (v.size() == 0)
			{
				removedHeartbeat++;
				log.info("removed redundant jmf # " + removedHeartbeat + " ID: " + jmf.getID() + " total checked: " + checked);
				_messages.remove(i);
			}
		}
	}

	/**
	 * constructor -use the static {@link JMFFactory#getCreateMessageSender(String)}
	 * 
	 * @param cu the URL to send the message to
	 */
	MessageSender(final CallURL cu)
	{
		super();

		trySend = 0;
		sent = 0;
		removedHeartbeat = 0;
		removedFireForget = 0;
		checked = 0;
		waitKaputt = false;

		_messages = new Vector<MessageDetails>();
		sentMessages = new FastFiFo<MessageDetails>(42);
		callURL = cu;
		timer = new CPUTimer(false);
		optimizer = new SenderQueueOptimizer();
		setJMFFactory(null);
	}

	/**
	 * the sender loop. <br/>
	 * Checks whether its vector of pending messages is empty. If it is not empty, the first message is sent and removed from the map.
	 */
	@Override
	public void run()
	{
		readFromBase();
		waitStartup();
		log.info("starting messagesender loop " + this);
		senderLoop();
		log.info("stopped messagesender loop " + this);

		write2Base();
	}

	/**
	 * 
	 * this is the main loop!
	 */
	protected void senderLoop()
	{
		while (!doShutDown)
		{
			if (pause)
			{
				if (!ThreadUtil.wait(mutexPause, 0) || doShutDown)
					break;
			}
			SendReturn sentFirstMessage;
			try
			{
				sentFirstMessage = sendFirstMessage();
				timer.stop();
				if (sentFirstMessage == SendReturn.sent)
				{
					synchronized (_messages)
					{
						sent++;
						lastSent = System.currentTimeMillis();
						idle = 0;
					}
				}

				if (doShutDownGracefully && (_messages.isEmpty() || idle > 10)) // idle>10 kills - we are having problems...
				{
					doShutDown = true;
				}
			}
			catch (final Throwable x)
			{
				sentFirstMessage = SendReturn.error;
				log.error("Error sending message: ", x);
				timer.stop();
			}
			if (sentFirstMessage != SendReturn.sent && sentFirstMessage != SendReturn.removed)
			{
				if (idle++ > 3600)
				{
					// no success or idle for an hour...
					doShutDownGracefully = true;
					log.info("Shutting down thread for base url: " + callURL.getBaseURL());
				}
				else
				{ // stepwise increment - try every second 10 times, then every 15 seconds, then every 5 minutes
					int minIdle = 10;
					int wait = (SendReturn.error == sentFirstMessage && idle > minIdle && !doShutDownGracefully && !doShutDown) ? 15000 : 1000;
					if (wait == 15000 && idle > minIdle)
					{
						wait *= (idle / minIdle);
						waitKaputt = true;
					}
					else
					{
						waitKaputt = false;
					}
					if (!ThreadUtil.wait(mutexDispatch, wait))
					{
						shutDown(true);
					}
				}
			}
		}
	}

	/**
	 * 
	 * make sure we don't spam during startup
	 */
	protected void waitStartup()
	{
		// wait a while before sending messages so that all processors are alive before we start throwing messages
		long t = System.currentTimeMillis() - startTime;
		if (t < 12345)
			ThreadUtil.sleep((int) (12345 - t));
	}

	/**
	 * write all pending messages to disk
	 */
	private void write2Base()
	{
		final File f = getPersistLocation();
		if (f == null)
		{
			log.error("no persistant message file location - possible loss of pending messages");
			return;
		}
		synchronized (_messages)
		{

			if (_messages.size() == 0)
			{
				log.info("no pending messages to write, ciao");
				f.delete(); // it's empty we can zapp it
				return;
			}
			else
			{
				log.info("writing " + _messages.size() + " pending messages to: " + f.getAbsolutePath());
			}
			final KElement root = appendToXML(null, true, -1, false);
			root.getOwnerDocument_KElement().write2File(f, 2, false);
			_messages.clear();
		}

	}

	/**
	 * pause this sender until resume is called the thread still exists
	 */
	public void pause()
	{
		pause = true;
	}

	/**
	 * resume this sender after pause was called
	 */
	public void resume()
	{
		pause = false;
		waitKaputt = false;
		ThreadUtil.notifyAll(mutexPause);
	}

	/**
	 * read all queued messages from storage, normally called at startup
	 */
	private void readFromBase()
	{
		final File f = getPersistLocation();
		if (f == null)
		{
			log.error("cannot read persistant message file, bailing out");
			return;
		}
		if (!f.exists()) // nothing queued ,ciao
		{
			log.info("no persistant message file exists to read, bailing out! " + f);
			return;
		}

		final JDFDoc d = new JDFParser().parseFile(f);
		synchronized (_messages)
		{
			final Vector<MessageDetails> vTmp = new Vector<MessageDetails>();
			vTmp.addAll(_messages);
			_messages.clear();
			// adding existing messages prior to vTmp - they must be sent first
			if (d != null)
			{
				final KElement root = d.getRoot();
				pause = root.getBoolAttribute("pause", null, false);
				sent = root.getIntAttribute("NumSent", null, 0);
				trySend = root.getIntAttribute("NumTry", null, 0);
				removedFireForget = root.getIntAttribute("NumRemoveFireForget", null, 0);
				removedHeartbeat = root.getIntAttribute("NumRemove", null, 0);
				removedError = root.getIntAttribute("NumRemoveError", null, 0);
				lastQueued = root.getLongAttribute("iLastQueued", null, 0);
				lastSent = root.getLongAttribute("iLastSent", null, 0);
				created = root.getLongAttribute("i" + AttributeName.CREATIONDATE, null, 0);
				final VElement v = root.getChildElementVector("Message", null);
				int zapp = 0;
				for (KElement e : v)
				{
					MessageDetails messageDetails = new MessageDetails(e);
					if (System.currentTimeMillis() - messageDetails.createTime < 1000 * 3600 * 24 * 7)
					{
						_messages.add(messageDetails);
					}
					else
					{
						zapp++;
						log.warn("removing stale message " + messageDetails + " created on " + new JDFDate(messageDetails.createTime).getDateTimeISO());
					}
				}
				log.info(" read " + v.size() + " messages from " + f.getAbsolutePath() + "removed messages: " + zapp);
			}
			_messages.addAll(vTmp);
		}
	}

	/**
	 * @return the file where we persist
	 */
	protected File getPersistLocation()
	{
		String loc = callURL.getBaseURL();
		if (loc == null)
		{
			log.error("cannot persist jmf to null location");
			return null;
		}
		loc = UrlUtil.removeProtocol(loc);
		loc = StringUtil.replaceCharSet(loc, ":\\", "/", 0);
		loc = StringUtil.replaceString(loc, "//", "/");
		loc += ".xml";

		final File f = FileUtil.getFileInDirectory(baseLocation, new File(loc));
		final File locParent = f.getParentFile();
		if (locParent != null)
		{
			locParent.mkdirs();
		}
		if (locParent == null || !locParent.isDirectory())
		{
			log.error("cannot create directory to persist jmf: " + f.getAbsolutePath());
			return null;
		}
		return f;
	}

	/**
	 * send the first enqueued message and return true if all went well
	 * also update any returned responses for Bambi internally
	 * 
	 * @return boolean true if the message is assumed sent false if an error was detected and the Message must remain in the queue
	 */
	private SendReturn sendFirstMessage()
	{
		MessageDetails mesDetails;
		JDFJMF jmf;
		Multipart mp;

		// don't synchronize the whole thing - otherwise the get handler may be blocked
		synchronized (_messages)
		{
			if (_messages.isEmpty())
			{
				return SendReturn.empty;
			}
			timer.start();
			mesDetails = _messages.get(0);
			if (mesDetails == null)
			{
				_messages.remove(0);
				removedError++;
				log.warn("removed null queued message in message queue");
				return SendReturn.removed; // should never happen
			}

			jmf = mesDetails.jmf;
			mp = mesDetails.mime;
			if (KElement.isWildCard(mesDetails.url))
			{
				log.error("Sending to bad url - bailing out! " + mesDetails.url);
				_messages.remove(0);
				removedError++;
				mesDetails.setReturn(SendReturn.error);
				sentMessages.push(mesDetails);
				return SendReturn.error; // snafu anyhow but not sent but no retry useful
			}

			if (mesDetails.respHandler != null && mesDetails.respHandler.isAborted())
			{
				_messages.remove(0);
				removedError++;
				log.warn("removed aborted message to: " + mesDetails.url);
				mesDetails.setReturn(SendReturn.removed);
				sentMessages.push(mesDetails);
				return SendReturn.removed;
			}
		}
		final SendReturn sendReturn = sendHTTP(mesDetails);
		mesDetails.setReturn(sendReturn);
		if (SendReturn.sent == sendReturn)
		{
			_messages.remove(0);
		}
		else
		{
			String isMime = "";
			if (jmf != null)
				isMime = "JMF";
			if (mp != null)
				isMime += "MIME";
			if ("".equals(isMime))
				isMime = "Empty";

			String warn = "Sender: " + mesDetails.senderID + " Error sending " + isMime + " message to: " + mesDetails.url + " return code=" + sendReturn;
			if (mesDetails.isFireForget())
			{
				warn += " - removing fire&forget message #";
				_messages.remove(0);
				removedFireForget++;
				warn += removedFireForget;
			}
			else
			{
				if (_messages.size() > 4242 || System.currentTimeMillis() - mesDetails.createTime > 1000 * 3600 * 24 * 7)
				{
					warn += " - removing prehistoric reliable message: creation time: " + new JDFDate(mesDetails.createTime) + " messages pending: " + _messages.size();
				}
				else
				{
					warn += " - retaining message for resend; messages pending: " + _messages.size();
				}
			}
			log.warn(warn);
		}
		sentMessages.push(mesDetails);
		return sendReturn;
	}

	/**
	 * send a message via http
	 * 
	 * @param mh the messagedetails
	 * @return the success as a sendreturn enum
	 */
	private SendReturn sendHTTP(final MessageDetails mh)
	{
		JDFJMF jmf = mh.jmf;
		Multipart mp = mh.mime;
		SendReturn b = SendReturn.sent;
		final URL url = UrlUtil.stringToURL(mh.url);
		if (url == null || (!UrlUtil.isHttp(mh.url) && !UrlUtil.isHttps(mh.url)))
		{
			log.error("Invalid url: " + url + " removing message");
			return SendReturn.removed;
		}
		try
		{
			trySend++;
			HttpURLConnection connection = null;
			String header = "URL: " + url;
			if (jmf != null && mp != null)
			{
				log.warn("Both mime package and JMF specified - sending both");
			}

			final DumpDir outDump = getOutDump(mh.senderID);
			if (jmf != null)
			{
				connection = sendJMF(mh, jmf, url, outDump);
			}
			if (mp != null) // mime package
			{
				connection = sendMime(mh, mp, url, outDump);
			}
			if (jmf == null && mp == null)
			{
				connection = sendEmpty(mh, url, outDump);
			}

			if (connection != null)
			{
				try
				{
					if (mh.respHandler != null)
					{
						mh.respHandler.setConnection(connection);
					}
					connection.setReadTimeout(30000); // 30 seconds should suffice
					header += "\nResponse code:" + connection.getResponseCode();
					header += "\nContent type:" + connection.getContentType();
					header += "\nContent length:" + connection.getContentLength();
				}
				catch (FileNotFoundException fx)
				{
					// this happens when a server is at the url but the war is not loaded
					getLog().warn("Error reading response: " + fx.getMessage());
					connection = null;
				}
			}

			final DumpDir inDump = getInDump(mh.senderID);
			if (connection != null && connection.getResponseCode() == 200)
			{
				InputStream inputStream = connection.getInputStream();
				ByteArrayIOStream bis = new ByteArrayIOStream(inputStream);
				inputStream.close(); // copy and close so that the connection stream can be reused by keep-alive
				if (inDump != null)
				{
					inDump.newFileFromStream(header, bis.getInputStream(), mh.getName());
				}
				if (mh.respHandler != null)
				{
					mh.respHandler.setConnection(connection);
					mh.respHandler.setBufferedStream(bis);
					b = mh.respHandler.handleMessage() ? SendReturn.sent : SendReturn.error;
				}
			}
			else
			{
				b = SendReturn.error;
				if (idle == 0)
				{
					log.warn("could not send message to " + mh.url + " rc= " + ((connection == null) ? -1 : connection.getResponseCode()));
				}
				if (connection != null)
				{
					if (inDump != null)
					{
						inDump.newFile(header, mh.getName());
					}
					InputStream inputStream = connection.getInputStream();
					if (inputStream != null)
						inputStream.close();
				}
				if (mh.respHandler != null)
				{
					mh.respHandler.setConnection(connection);
					mh.respHandler.handleMessage(); // make sure we tell anyone who is waiting that the wait is over...
				}
			}
		}
		catch (final Throwable e)
		{
			log.error("Exception in sendHTTP: " + e.getMessage());
			if (mh.respHandler != null)
			{
				mh.respHandler.handleMessage(); // make sure we tell anyone who is waiting that the wait is over...
			}
			b = SendReturn.error;
		}
		return b;
	}

	/**
	 * @param mh
	 * @param url
	 * @param outDump
	 * @return
	 */
	private HttpURLConnection sendEmpty(MessageDetails mh, URL url, DumpDir outDump)
	{
		String header = "URL: " + url;

		log.debug(" sending empty content to: " + url.toExternalForm());
		final HTTPDetails hd = mh.mimeDet == null ? null : mh.mimeDet.httpDetails;
		UrlPart p = UrlUtil.writeToURL(url.toExternalForm(), null, UrlUtil.POST, UrlUtil.TEXT_UNKNOWN, hd);
		HttpURLConnection connection = p == null ? null : p.getConnection();
		if (outDump != null)
		{
			outDump.newFile(header, mh.getName());
		}
		return connection;
	}

	/**
	 * @param mh
	 * @param mp
	 * @param url
	 * @param outDump
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 * @throws FileNotFoundException
	 */
	private HttpURLConnection sendMime(final MessageDetails mh, Multipart mp, final URL url, final DumpDir outDump) throws IOException, MessagingException, FileNotFoundException
	{
		String header = "URL: " + url;
		log.info("sending mime to: " + url.toExternalForm());
		HttpURLConnection connection = MimeUtil.writeToURL(mp, mh.url, mh.mimeDet);
		if (outDump != null)
		{
			final File dump = outDump.newFile(header, mh.getName());
			if (dump != null)
			{
				final FileOutputStream fos = new FileOutputStream(dump, true);
				MimeUtil.writeToStream(mp, fos, mh.mimeDet);
				fos.close();
			}
		}
		return connection;
	}

	/**
	 * @param mh
	 * @param jmf
	 * @param url
	 * @param outDump
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private HttpURLConnection sendJMF(final MessageDetails mh, JDFJMF jmf, final URL url, final DumpDir outDump) throws FileNotFoundException, IOException
	{
		if (log.isDebugEnabled())
			log.debug("sending jmf ID=" + jmf.getID() + " to: " + url.toExternalForm());
		final JDFDoc jmfDoc = jmf.getOwnerDocument_JDFElement();
		final HTTPDetails hd = mh.mimeDet == null ? null : mh.mimeDet.httpDetails;
		HttpURLConnection connection = jmfDoc.write2HTTPURL(url, hd);
		if (outDump != null)
		{
			String header = "URL: " + url;
			final File dump = outDump.newFile(header, mh.getName());
			if (dump != null)
			{
				final FileOutputStream fos = new FileOutputStream(dump, true);
				jmfDoc.write2Stream(fos, 0, true);
				fos.close();
			}
		}
		return connection;
	}

	/**
	 * stop sending new messages immediately and shut down
	 * 
	 * @param gracefully true - process remaining messages first, then shut down. <br/>
	 * false - shut down immediately, skip remaining messages.
	 */
	public void shutDown(final boolean gracefully)
	{
		if (gracefully)
		{
			doShutDownGracefully = true;
		}
		else
		{
			doShutDown = true;
		}
		myFactory.senders.remove(callURL);
		ThreadUtil.notifyAll(mutexDispatch);
	}

	/**
	 * return true if the thread is still running
	 * 
	 * @return true if running
	 */
	public boolean isRunning()
	{
		return !doShutDown;
	}

	/**
	 * return true if tesatURL fits this url
	 * 
	 * @param testURL the url to check against
	 * @return true if running
	 */
	public boolean matchesURL(final String testURL)
	{
		return ContainerUtil.equals(testURL, callURL.getBaseURL());
	}

	/**
	 * 
	 * get the in dump for this message
	 * @param senderID
	 * @return
	 */
	private DumpDir getInDump(final String senderID)
	{
		BambiContainer c = BambiContainer.getInstance();
		if (c == null || !c.wantDump())
			return null;
		return vDumps.getOne(senderID, 0);
	}

	/**
	 * 
	 * get the out dump for this message
	 * @param senderID
	 * @return
	 */
	private DumpDir getOutDump(final String senderID)
	{
		BambiContainer c = BambiContainer.getInstance();
		if (c == null || !c.wantDump())
			return null;
		return vDumps.getOne(senderID, 1);
	}

	/**
	 * add debug dump directories for a given senderID
	 * 
	 * @param senderID
	 * @param inDump
	 * @param outDump
	 */
	public static void addDumps(final String senderID, final DumpDir inDump, final DumpDir outDump)
	{
		vDumps.putOne(senderID, inDump);
		vDumps.putOne(senderID, outDump);
	}

	/**
	 * queues a message for the URL that this MessageSender belongs to also updates the message for a given recipient if required
	 * 
	 * @param jmf the message to send
	 * @param handler
	 * @param url
	 * @param _callBack
	 * @return true, if the message is successfully queued. false, if this MessageSender is unable to accept further messages (i. e. it is shutting down).
	 */
	public boolean queueMessage(final JDFJMF jmf, final IResponseHandler handler, final String url, final IConverterCallback _callBack)
	{
		if (doShutDown || doShutDownGracefully)
		{
			log.warn("cannot queue message during shutdown!");
			return false;
		}

		if (_callBack != null)
		{
			try
			{
				_callBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());
			}
			catch (Throwable x)
			{
				log.error("exception modifying message: ", x);
			}
		}
		if (log.isDebugEnabled())
		{
			log.debug("Queueing jmf message, ID=" + jmf.getID() + " to: " + url);
		}

		final MessageDetails messageDetails = new MessageDetails(jmf, handler, _callBack, null, url);
		return queueMessageDetails(messageDetails);
	}

	/**
	 * queues a message for the URL that this MessageSender belongs to also updates the message for a given recipient if required
	 * 
	 * @param handler
	 * @param url
	 * @param _callBack
	 * @return true, if the message is successfully queued. false, if this MessageSender is unable to accept further messages (i. e. it is shutting down).
	 */
	public boolean queuePost(final IResponseHandler handler, final String url, final IConverterCallback _callBack)
	{
		if (doShutDown || doShutDownGracefully)
		{
			log.warn("cannot queue message during shutdown!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(null, handler, _callBack, null, url);
		queueMessageDetails(messageDetails);
		return true;
	}

	/**
	 * @param messageDetails
	 */
	private boolean queueMessageDetails(final MessageDetails messageDetails)
	{
		lastQueued = System.currentTimeMillis();
		if (waitKaputt && messageDetails.isFireForget())
		{
			String warn = " not queueing fire&forget to " + callURL.url + "; message #";
			removedFireForget++;
			warn += removedFireForget;
			warn += " currently waiting: " + _messages.size();
			log.warn(warn);
			trySend++;
			return false;
		}
		synchronized (_messages)
		{
			optimizer.optimize(messageDetails.jmf);
			_messages.add(messageDetails);
		}
		if (!pause)
		{
			ThreadUtil.notifyAll(mutexDispatch);
		}
		return true;
	}

	/**
	 * queues a message for the URL that this MessageSender belongs to also updates the message for a given recipient if required
	 * 
	 * @param multpart
	 * @param handler
	 * @param callback
	 * @param md
	 * @param senderID
	 * @param url
	 * @return true, if the message is successfully queued. false, if this MessageSender is unable to accept further messages (i. e. it is shutting down).
	 */
	public boolean queueMimeMessage(final Multipart multpart, final IResponseHandler handler, final IConverterCallback callback, final MIMEDetails md, final String senderID, final String url)
	{
		if (doShutDown || doShutDownGracefully)
		{
			log.warn("cannot queue message during shutdown!");
			return false;
		}
		log.info("Queueing mime message to: " + url);
		final MessageDetails messageDetails = new MessageDetails(multpart, handler, callback, md, senderID, url);
		queueMessageDetails(messageDetails);
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string
	 */
	@Override
	public String toString()
	{
		return "MessageSender - URL: " + callURL.url + " size: " + _messages.size() + " total: " + sent + " last queued at " + XMLResponse.formatLong(lastQueued)
				+ " last sent at " + XMLResponse.formatLong(lastSent) + "\n" + _messages;
	}

	/**
	 * creates a descriptive xml Object for this MessageSender
	 * 
	 * @param root the parent into which I append myself, if null create a new document
	 * @param writePendingMessages if true, write out the messages
	 * @param posQueuedMessages
	 * @param bXJDF 
	 * @return the appended element
	 */
	public KElement appendToXML(final KElement root, final boolean writePendingMessages, final int posQueuedMessages, boolean bXJDF)
	{

		final KElement ms = root == null ? new XMLDoc("MessageSender", null).getRoot() : root.appendElement("MessageSender");
		synchronized (_messages)
		{
			ms.setAttribute(AttributeName.URL, callURL.url);
			ms.setAttribute(AttributeName.SIZE, _messages.size(), null);
			ms.setAttribute("NumSent", sent, null);
			ms.setAttribute("NumTry", trySend, null);
			ms.setAttribute("NumRemove", removedHeartbeat, null);
			ms.setAttribute("NumRemoveFireForget", removedFireForget, null);
			ms.setAttribute("NumRemoveError", removedError, null);

			ms.setAttribute("LastQueued", XMLResponse.formatLong(lastQueued), null);
			ms.setAttribute("LastSent", XMLResponse.formatLong(lastSent), null);
			ms.setAttribute(AttributeName.CREATIONDATE, XMLResponse.formatLong(created), null);

			ms.setAttribute("pause", pause, null);
			ms.setAttribute("idle", idle, null);
			ms.setAttribute("Active", !doShutDown, null);
			boolean problems = lastQueued - lastSent > 60000;
			ms.setAttribute("Problems", problems, null);
			ms.setAttribute("iLastQueued", StringUtil.formatLong(lastQueued), null);
			ms.setAttribute("iLastSent", StringUtil.formatLong(lastSent), null);
			ms.setAttribute("i" + AttributeName.CREATIONDATE, StringUtil.formatLong(created), null);
			ms.copyElement(timer.toXML(), null);

			if (writePendingMessages)
			{
				for (int i = 0; i < _messages.size(); i++)
				{
					_messages.get(i).appendToXML(ms, i, bXJDF);
				}
			}
			else if (posQueuedMessages == 0)
			{
				final MessageDetails[] old = sentMessages.peekArray();
				if (old != null)
				{
					for (int i = old.length - 1; i >= 0; i--)
					{
						old[i].appendToXML(ms, -1, bXJDF);
					}
				}
			}
			else if (posQueuedMessages > 0)
			{
				final MessageDetails old = sentMessages.peek(sentMessages.getFill() - posQueuedMessages);
				old.appendToXML(ms, posQueuedMessages, bXJDF);
			}
		}
		return ms;
	}

	/**
	 * set the base directory for serializing and deserializing messages
	 * 
	 * @param _baseLocation the baseLocation to set
	 */
	public static void setBaseLocation(final File _baseLocation)
	{
		// this is static and can therefore only be set once for consistency
		if (baseLocation == null)
		{
			MessageSender.baseLocation = FileUtil.getFileInDirectory(_baseLocation, new File("JMFStore"));
			LogFactory.getLog(MessageSender.class).info("setting JMF Base dir to: " + _baseLocation);
		}
	}

	/**
	 * remove all unsent messages without sending them
	 */
	public void flushMessages()
	{
		synchronized (_messages)
		{
			_messages.clear();
			final File pers = getPersistLocation().getParentFile();
			FileUtil.deleteAll(pers);
		}
	}

	/**
	 * @param _myFactory the myFactory to set
	 */
	public void setJMFFactory(final JMFFactory _myFactory)
	{
		this.myFactory = _myFactory;
		if (myFactory == null)
		{
			myFactory = JMFFactory.getJMFFactory();
		}
	}

	/**
	 * @return the myFactory
	 */
	public JMFFactory getJMFFactory()
	{
		return myFactory;
	}

	/**
	 * set the startTime to the startTime of the device
	 * 
	 * @param startTime
	 */
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * check whether we have not sent for longer than deltaTime milliseconds
	 * @param deltaTime
	 * @return
	 */
	public boolean isBlocked(long deltaTime)
	{
		long last = lastSent == 0 ? startTime : lastSent;
		return lastQueued - last > deltaTime;
	}
}
