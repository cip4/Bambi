/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2019 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.messaging.IMessageOptimizer.optimizeResult;
import org.cip4.bambi.core.messaging.JMFFactory.CallURL;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.ByteArrayIOStream.ByteArrayIOInputStream;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FastFiFo;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlPart;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.VectorMap;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.cip4.jdflib.util.thread.DelayedPersist;
import org.cip4.jdflib.util.thread.IPersistable;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * allow a JMF message to be sent in its own thread
 *
 * @author boegerni
 */
public class MessageSender extends BambiLogFactory implements Runnable, IPersistable
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
	protected MessageFiFo _messages;
	protected FastFiFo<MessageDetails> sentMessages = null;
	private static VectorMap<String, DumpDir> vDumps = new VectorMap<>();
	private final MyMutex mutexDispatch = new MyMutex();
	private final MyMutex mutexPause = new MyMutex();
	private int trySend;
	private int sent;
	private long firstProblem;
	private boolean waitKaputt;
	protected int checked;
	protected int checkedJMF;
	protected int removedHeartbeat;
	protected int removedHeartbeatJMF;
	protected int removedFireForget;
	protected int removedError;
	private int idle;
	private final CPUTimer timer;
	private long created;
	private long lastQueued;
	private long lastSent;
	private boolean pause;
	private static File baseLocation;

	/**
	 * @return the baseLocation where all message related files are stored
	 */
	public static File getBaseLocation()
	{
		return baseLocation;
	}

	private SenderQueueOptimizer optimizer = null;
	private long startTime;
	private boolean zappFirst;

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
			final VElement messages = jmf == null ? null : jmf.getMessageVector(null, null);
			if (messages != null)
			{
				checkedJMF++;
				for (final KElement m : messages)
				{
					optimizeMessage((JDFMessage) m);
				}
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
			checked++;
			final List<MessageDetails> tail = _messages.getTailClone();
			if (tail != null)
			{
				for (int i = tail.size() - 1; i >= 0; i--)
				{
					final MessageDetails messageDetails = tail.get(i);
					if (messageDetails == null)
					{
						log.warn("empty message in tail...");
						break;
					}
					final JDFJMF jmf = messageDetails.jmf;
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
								removeMessage(mOld, messageDetails);
							}
							else if (res == optimizeResult.cont)
							{
								return; // we found a non matching message and must stop optimizing
							}
						}
					}
				}
			}
		}

		/**
		 * @param old
		 * @param messageDetails
		 */
		private void removeMessage(final JDFMessage old, final MessageDetails messageDetails)
		{
			synchronized (_messages)
			{
				final JDFJMF jmf = old.getJMFRoot();
				jmf.removeChild(old);
				removedHeartbeat++;
				if (myFactory.isLogLots() || removedHeartbeat < 10 || removedHeartbeat % 1000 == 0)
				{
					log.info("removed redundant " + old.getType() + " " + old.getLocalName() + " Message ID= " + old.getID() + " Sender= " + old.getSenderID() + "# " + removedHeartbeat + " / "
							+ checked);
				}
				final VElement v = jmf.getMessageVector(null, null);
				if (v == null || v.size() == 0)
				{
					final boolean zapped = _messages.remove(messageDetails);
					if (zapped)
					{
						removedHeartbeatJMF++;
						if (myFactory.isLogLots() || removedHeartbeatJMF < 10 || removedHeartbeatJMF % 1000 == 0)
						{
							log.info("removed redundant jmf # " + removedHeartbeatJMF + " ID: " + jmf.getID() + " total checked: " + checkedJMF);
						}
					}
					else
					{
						log.warn("could not remove redundant jmf # " + removedHeartbeatJMF + " ID: " + jmf.getID() + " total checked: " + checkedJMF);
					}
				}
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
		firstProblem = 0;
		trySend = 0;
		sent = 0;
		lastQueued = 0;
		lastSent = 0;
		pause = false;
		removedHeartbeat = 0;
		removedHeartbeatJMF = 0;
		removedFireForget = 0;
		checkedJMF = 0;
		checked = 0;
		waitKaputt = false;
		callURL = cu;
		timer = new CPUTimer(false);
		created = System.currentTimeMillis();
		_messages = new MessageFiFo(getPersistLocation(true));
		sentMessages = new FastFiFo<>(42);
		optimizer = new SenderQueueOptimizer();
		setJMFFactory(null);
		created = System.currentTimeMillis();
		zappFirst = false;
		readFromBase();
	}

	/**
	 * the sender loop. <br/>
	 * Checks whether its vector of pending messages is empty. If it is not empty, the first message is sent and removed from the map.
	 */
	@Override
	public void run()
	{
		waitStartup();
		log.info("starting messagesender loop " + this);
		senderLoop();
		log.info("stopped messagesender loop " + this);
		write2Base(true);
	}

	/**
	 *
	 * this is the main loop!
	 */
	protected void senderLoop()
	{
		long lastLog = 0;
		while (!doShutDown)
		{
			if (pause)
			{
				log.info("senderloop to " + callURL.getBaseURL() + " is paused");
				if (!ThreadUtil.wait(mutexPause, 0) || doShutDown)
				{
					break;
				}
				log.info("senderloop to " + callURL.getBaseURL() + " is resumed");
			}
			SendReturn sentFirstMessage = null;
			try
			{
				sentFirstMessage = sendFirstMessage();
				timer.stop();
			}
			catch (final Throwable x)
			{
				sentFirstMessage = SendReturn.error;
				log.error("Error sending message: ", x);
				timer.stop();
			}

			final long ct0 = System.currentTimeMillis();
			if (sentFirstMessage == SendReturn.sent)
			{
				sent++;
				lastSent = ct0;
				idle = 0;
				if (myFactory.isLogLots() || sent < 10 || (sent % 1000) == 0)
				{
					log.info("successfully sent JMF # " + sent + " to " + callURL);
				}
			}
			else
			{
				idle++;
				int wait = 1000;
				if (_messages.size() == 0)
				{
					if (idle > 3333)
					{
						// no success or idle for an hour...
						log.info("Shutting down idle and empty thread for base url: " + callURL.getBaseURL());
						shutDown(true);
						break;
					}
				}
				else
				{ // stepwise increment - try every second 10 times, then every 15 seconds, then every 5 minutes
					final int minIdle = 10;
					wait = 15000;
					if (idle > minIdle)
					{
						wait *= (idle / minIdle);
						if (wait > 424242)
						{
							wait = 424242;
							if (_messages.size() > 0 && (ct0 - lastLog) > 60000l)
							{
								final long t0 = lastSent == 0 ? startTime : lastSent;
								final long t = (ct0 - t0) / 60000l;
								final String tmp;
								if (t < 60)
								{
									tmp = t + " minutes; size=";
								}
								else if (t < 60 * 24)
								{
									tmp = (t / 60) + " hours; size=";
								}
								else
								{
									tmp = (t / (60 * 24)) + " days; size=";
								}
								log.warn("Waiting in blocked message thread: " + callURL.getBaseURL() + " unsuccessful for " + tmp + _messages.size());
								lastLog = ct0;
							}
						}
						waitKaputt = true;
					}
					else
					{
						waitKaputt = false;
					}
				}
				if (!ThreadUtil.wait(mutexDispatch, wait))
				{
					shutDown(false);
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
		final long t = System.currentTimeMillis() - startTime;
		if (t < 12345)
		{
			ThreadUtil.wait(mutexDispatch, (int) (12345 - t));
		}
	}

	/**
	 * write all pending messages to disk
	 *
	 * @param clearMessages if true flush me
	 */
	private void write2Base(final boolean clearMessages)
	{
		final File f = getPersistLocation(false);
		if (f == null)
		{
			log.error("no persistant message file location - possible loss of pending messages");
			return;
		}
		synchronized (_messages)
		{
			if (_messages.size() > 0)
			{
				log.info("writing " + _messages.size() + " pending messages to: " + f.getAbsolutePath());
			}
			_messages.dumpHeadTail();
			final KElement root = appendToXML(null, -1, false);
			root.getOwnerDocument_KElement().write2File(f, 2, false);
			if (clearMessages)
			{
				_messages.clear();
			}
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
		idle = 0;
		ThreadUtil.notifyAll(mutexPause);
	}

	/**
	 * read all queued messages from storage, normally called at startup
	 */
	private void readFromBase()
	{
		final File f = getPersistLocation(false);
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

		final JDFDoc d = JDFDoc.parseFile(f);
		// adding existing messages prior to vTmp - they must be sent first
		if (d != null)
		{
			final KElement root = d.getRoot();
			pause = root.getBoolAttribute("pause", null, false);
			sent = root.getIntAttribute("NumSent", null, 0);
			trySend = root.getIntAttribute("NumTry", null, 0);
			removedFireForget = root.getIntAttribute("NumRemoveFireForget", null, 0);
			removedHeartbeat = root.getIntAttribute("NumRemove", null, 0);
			removedHeartbeatJMF = root.getIntAttribute("NumRemoveJMF", null, 0);
			removedError = root.getIntAttribute("NumRemoveError", null, 0);
			lastQueued = root.getLongAttribute("iLastQueued", null, 0);
			lastSent = root.getLongAttribute("iLastSent", null, 0);
			created = root.getLongAttribute("i" + AttributeName.CREATIONDATE, null, System.currentTimeMillis());
			if (created <= 0)
			{
				created = System.currentTimeMillis();
			}
		}
		else
		{
			log.warn("could not parse jmf message sender base file" + f.getAbsolutePath());
		}
	}

	/**
	 * @param bDir if true return the parent directory
	 * @return the file where we persist
	 */
	protected File getPersistLocation(final boolean bDir)
	{
		String loc = callURL.getBaseURL();
		loc = UrlUtil.removeProtocol(loc);
		loc = StringUtil.replaceCharSet(loc, ":\\", "/", 0);
		loc = StringUtil.replaceString(loc, "//", "/");
		if (loc == null)
		{
			log.error("cannot persist jmf to location; " + callURL.getBaseURL());
			return null;
		}

		File f = FileUtil.getFileInDirectory(baseLocation, new File(loc));
		f.mkdirs();
		if (!bDir)
			f = FileUtil.getFileInDirectory(f, new File("Status.xml"));
		return f;
	}

	/**
	 * send the first enqueued message and return true if all went well also update any returned responses for Bambi internally
	 *
	 * @return boolean true if the message is assumed sent false if an error was detected and the Message must remain in the queue
	 */
	private SendReturn sendFirstMessage()
	{
		MessageDetails mesDetails;

		// don't synchronize the whole thing - otherwise the get handler may be blocked
		synchronized (_messages)
		{
			if (_messages.isEmpty())
			{
				return SendReturn.empty;
			}
			timer.start();
			mesDetails = _messages.get(0);
		}
		final SendReturn details = checkDetails(mesDetails);
		if (details != null)
			return details;

		SendReturn sendReturn = sendHTTP(mesDetails);
		synchronized (_messages)
		{
			sendReturn = processMessageResponse(mesDetails, sendReturn);
		}
		return sendReturn;
	}

	public SendReturn processMessageResponse(final MessageDetails mesDetails, SendReturn sendReturn)
	{
		if (SendReturn.sent == sendReturn)
		{
			processSuccess(mesDetails);
		}
		else if (SendReturn.removed.equals(sendReturn))
		{
			_messages.remove(0);
			removedError++;
		}
		else
		{
			sendReturn = processProblem(mesDetails, sendReturn);
		}
		mesDetails.setReturn(sendReturn);
		return sendReturn;
	}

	public SendReturn processProblem(final MessageDetails mesDetails, SendReturn sendReturn)
	{
		firstProblem = System.currentTimeMillis();
		String isMime = "";
		if (mesDetails.jmf != null)
			isMime = "JMF";
		if (mesDetails.jdf != null)
			isMime += "MIME";
		if ("".equals(isMime))
			isMime = "Empty";

		boolean needLog = true;
		String warn = "Sender: " + mesDetails.senderID + " Error sending " + isMime + " message to: " + mesDetails.url + " return code=" + sendReturn;
		if (mesDetails.isFireForget())
		{
			warn += " - removing fire&forget " + mesDetails.getName() + " message #";
			_messages.remove(0);
			removedFireForget++;
			warn += removedFireForget;
			sendReturn = SendReturn.removed;
			needLog = (removedFireForget < 10) || (removedFireForget % 100) == 0;
		}
		else
		{
			if ((System.currentTimeMillis() - mesDetails.createTime) > (1000l * 3600l * 24l * 42l) && (_messages.size() > 1000))
			{
				final String warn2 = " - removing prehistoric reliable " + mesDetails.getName() + " message: creation time: " + new JDFDate(mesDetails.createTime).getDateTimeISO()
						+ " messages pending: " + _messages.size();
				warn += warn2;
				_messages.remove(0);
				removedError++;
				sendReturn = SendReturn.removed;
				needLog = (removedError < 10) || (removedError % 100) == 0;
			}
			else
			{
				final String warn2 = " - retaining " + mesDetails.getName() + " message for resend; messages pending: " + _messages.size() + " times delayed: " + idle;
				warn += warn2;
				needLog = (idle < 10) || (idle % 100) == 0;
			}
		}
		if (needLog)
		{
			log.warn(warn);
		}
		return sendReturn;
	}

	public void processSuccess(final MessageDetails mesDetails)
	{
		if (firstProblem != 0)
		{
			reactivate(mesDetails);
		}
		firstProblem = 0;
		_messages.remove(0);
		sentMessages.push(mesDetails);
		if (myFactory.isLogLots())
		{
			String msg = "Successfully sent " + mesDetails.getName() + " #" + sent + " to " + mesDetails.url;
			if (_messages.size() > 0)
			{
				msg += " waiting: " + _messages.size();
			}
			log.info(msg);
		}
	}

	SendReturn checkDetails(final MessageDetails mesDetails)
	{
		if (mesDetails == null)
		{
			_messages.remove(0);
			log.warn("removed null message in message queue ");
			return SendReturn.removed;
		}
		else if (zappFirst || KElement.isWildCard(mesDetails.url))
		{
			_messages.remove(0);
			removedError++;
			zappFirst = false;
			log.warn("removed first " + mesDetails.getName() + " message in message queue to: " + mesDetails.url);
			mesDetails.setReturn(SendReturn.removed);
			sentMessages.push(mesDetails);
			return SendReturn.removed;
		}
		else if (mesDetails.respHandler != null && mesDetails.respHandler.isAborted())
		{
			_messages.remove(0);
			removedError++;
			log.warn("removed timed out " + mesDetails.getName() + " message to: " + mesDetails.url);
			mesDetails.setReturn(SendReturn.removed);
			sentMessages.push(mesDetails);
			return SendReturn.removed;
		}
		else
		{
			return null;
		}
	}

	void reactivate(final MessageDetails mesDetails)
	{
		final long tWait = System.currentTimeMillis() - firstProblem;
		final String duration;
		if (tWait < 60000)
		{
			duration = (tWait / 1000l) + " seconds";
		}
		else if (tWait < 60l * 60000l)
		{
			duration = (tWait / 60000l) + " minutes";
		}
		else if (tWait < 3600l * 60000l)
		{
			duration = (tWait / 3600000l) + " hours";
		}
		else
		{
			duration = (tWait / (3600000l * 24l)) + " days";
		}
		log.info("successfully reactivated message sender " + mesDetails.getName() + " to: " + mesDetails.url + " after " + duration + " messages pending: " + _messages.size());
	}

	/**
	 * send a message via http
	 *
	 * @param messagedetails the messagedetails
	 * @return the success as a sendreturn enum
	 */
	private SendReturn sendHTTP(final MessageDetails messagedetails)
	{
		if (messagedetails.url == null || (!UrlUtil.isHttp(messagedetails.url) && !UrlUtil.isHttps(messagedetails.url)))
		{
			log.error("Invalid url: " + messagedetails.url + " removing message " + messagedetails.getName());
			return SendReturn.removed;
		}
		SendReturn b = SendReturn.sent;
		try
		{
			final HttpURLConnection connection = sendDetails(messagedetails);
			b = processResponse(messagedetails, connection);
		}
		catch (final IllegalArgumentException e)
		{
			log.warn("Invalid stream " + e.getMessage());
			return SendReturn.removed;

		}
		catch (final Throwable e)
		{
			log.error("Exception in sendHTTP: " + e.getClass().getSimpleName() + " Message= " + e.getMessage());
			if (messagedetails.respHandler != null)
			{
				messagedetails.respHandler.handleMessage(); // make sure we tell anyone who is waiting that the wait is over...
			}
			b = SendReturn.error;
		}

		return b;
	}

	/**
	 *
	 *
	 * @param mesDetails
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	private SendReturn processResponse(final MessageDetails mesDetails, HttpURLConnection connection) throws IOException
	{
		SendReturn sendReturn = SendReturn.sent;
		final String url = mesDetails.url;
		String header = "URL: " + url;
		int responseCode = -1;
		if (connection != null)
		{
			try
			{
				responseCode = connection.getResponseCode();

				if (mesDetails.respHandler != null)
				{
					mesDetails.respHandler.setConnection(connection);
				}
				connection.setReadTimeout(30000); // 30 seconds should suffice
				header += "\nResponse code:" + responseCode;
				header += "\nContent type:" + connection.getContentType();
				header += "\nContent length:" + connection.getContentLength();
			}
			catch (final FileNotFoundException fx)
			{
				// this happens when a server is at the url but the war is not loaded
				log.warn("Error reading response: " + fx.getMessage());
				connection = null;
				responseCode = 404;
			}
		}

		ByteArrayIOInputStream bis = null;
		InputStream stream = null;
		if (connection != null)
		{
			try
			{
				stream = connection.getInputStream();
			}
			catch (final IOException x)
			{
				// nop
			}
			if (stream == null)
			{
				stream = connection.getErrorStream();
			}
			if (stream != null)
			{
				bis = ByteArrayIOStream.getBufferedInputStream(stream);
				stream.close();
			}
		}

		if (connection == null)
		{
			sendReturn = SendReturn.error;
			if (idle == 0 || (idle % 100 == 0))
			{
				log.warn("could not send message to unavailable " + mesDetails.url + " no return; rc= " + responseCode);
			}
		}
		else if (!UrlUtil.isReturnCodeOK(responseCode))
		{
			if (isRemoveRC(responseCode))
			{
				sendReturn = SendReturn.removed;
				if (idle == 0)
				{
					log.error("removing message that causes server error at " + mesDetails.url + " rc= " + responseCode);
				}
			}
			else
			{
				sendReturn = SendReturn.error;
				if (idle == 0 || (idle % 100 == 0))
				{
					log.warn("error sending message " + mesDetails.getName() + " to " + mesDetails.url + " rc= " + responseCode);
				}
			}
		}
		if (mesDetails.respHandler != null)
		{
			mesDetails.respHandler.setConnection(connection);
			mesDetails.respHandler.setBufferedStream(bis == null ? null : new ByteArrayIOStream(bis));
			final SendReturn sr2 = mesDetails.respHandler.handleMessage() ? SendReturn.sent : SendReturn.error;
			if (!SendReturn.error.equals(sendReturn))
			{
				sendReturn = sr2;
			}
		}
		final DumpDir inDump = getInDump(mesDetails.senderID);
		if (inDump != null)
		{
			inDump.newFileFromStream(header, bis, mesDetails.getName());
		}
		return sendReturn;
	}

	/**
	 *
	 * @param responseCode
	 * @return
	 *
	 */
	protected boolean isRemoveRC(final int responseCode)
	{
		final boolean b400 = responseCode >= 400 && responseCode < 500 && responseCode != 404 && responseCode != 408 && responseCode != 429;
		boolean b500 = myFactory.isZapp500();
		if (b500)
			b500 = responseCode >= 500 && responseCode < 600 && responseCode != 503 && responseCode != 504 && responseCode != 507 && responseCode != 509;
		return b400 || b500;
	}

	/**
	 *
	 * @param mesDetails
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws MessagingException
	 */
	private HttpURLConnection sendDetails(final MessageDetails mesDetails) throws FileNotFoundException, IOException, MessagingException, IllegalArgumentException
	{
		trySend++;

		if (mesDetails == null)
			return null;
		final String url = mesDetails.url;
		final InputStream is = mesDetails.getInputStream();
		if (is == null)
		{
			throw new IllegalArgumentException("sending null stream to " + url);
		}
		final String contentType = mesDetails.getContentType();
		final HTTPDetails hd = mesDetails.mimeDet == null ? null : mesDetails.mimeDet.httpDetails;
		final String header = "URL: " + url;
		final DumpDir outDump = getOutDump(mesDetails.senderID);
		final File dump = outDump == null ? null : outDump.newFile(header, mesDetails.getName());
		if (dump != null)
		{
			final BufferedOutputStream fos = FileUtil.getBufferedOutputStream(dump, true);
			IOUtils.copy(ByteArrayIOStream.getBufferedInputStream(is), fos);
			fos.close();
		}
		final UrlPart p = UrlUtil.writeToURL(url, ByteArrayIOStream.getBufferedInputStream(is), UrlUtil.POST, contentType, hd);

		return (HttpURLConnection) (p == null ? null : p.getConnection());
	}

	/**
	 * stop sending new messages immediately and shut down
	 *
	 * @param gracefully true - process remaining messages first, then shut down. <br/>
	 *            false - shut down immediately, skip remaining messages.
	 */
	public void shutDown(final boolean gracefully)
	{
		doShutDown = true;
		myFactory.senders.remove(callURL);
		ThreadUtil.notifyAll(mutexDispatch);
		ThreadUtil.notifyAll(mutexPause);
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
		return testURL == null || new CallURL(testURL).equals(callURL);
	}

	/**
	 *
	 * get the in dump for this message
	 *
	 * @param senderID
	 * @return
	 */
	private DumpDir getInDump(final String senderID)
	{
		final BambiContainer c = BambiContainer.getInstance();
		if (c == null || !c.wantDump())
			return null;
		return vDumps.getOne(senderID, 0);
	}

	/**
	 *
	 * get the out dump for this message
	 *
	 * @param senderID
	 * @return
	 */
	private DumpDir getOutDump(final String senderID)
	{
		final BambiContainer c = BambiContainer.getInstance();
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
	public boolean queueMessage(final JDFJMF jmf, final IResponseHandler handler, final String url, final IConverterCallback _callBack, final HTTPDetails det)
	{
		if (doShutDown)
		{
			log.warn("cannot queue message during shutdown!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(jmf, handler, _callBack, det, url);
		return queueMessageDetails(messageDetails);
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

		return queueMessage(jmf, handler, url, _callBack, null);
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
	public boolean queueMessage(final JDFJMF jmf, final JDFNode node, final IResponseHandler handler, final String url, final IConverterCallback _callBack)
	{
		return queueMessage(jmf, node, handler, url, _callBack, null);
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
	public boolean queueMessage(final JDFJMF jmf, final JDFNode node, final IResponseHandler handler, final String url, final IConverterCallback _callBack, final MIMEDetails md)
	{
		if (doShutDown)
		{
			log.warn("cannot queue message during shutdown!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(jmf, node, handler, _callBack, md, url);
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
		if (doShutDown)
		{
			log.warn("cannot queue message during shutdown!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(null, handler, _callBack, null, url);
		return queueMessageDetails(messageDetails);
	}

	/**
	 * @param messageDetails
	 */
	private boolean queueMessageDetails(final MessageDetails messageDetails)
	{
		if (waitKaputt && messageDetails.isFireForget())
		{
			removedFireForget++;
			if (myFactory.isLogLots() || removedFireForget < 10 || (removedFireForget % 1000) == 0)
			{
				String warn = " not queueing fire&forget to " + callURL.url + "; message #";
				warn += removedFireForget;
				warn += " currently waiting: " + _messages.size();
				log.warn(warn);
			}
			trySend++;
			return false;
		}
		lastQueued = System.currentTimeMillis();
		if (_messages.size() > 333)
		{
			optimizer.optimize(messageDetails.jmf);
		}
		synchronized (_messages)
		{
			if (myFactory.isLogLots())
			{
				String msg = "queued " + messageDetails.getName() + " #" + sent + " to " + messageDetails.url;
				if (_messages.size() > 0)
				{
					msg += " size=" + _messages.size();
				}
				log.info(msg);
			}
			_messages.add(messageDetails);
			if (_messages.size() >= 1000)
			{
				if ((_messages.size() % 100) == 0)
				{
					log.warn("queueing message into blocked sender to " + callURL + " size=" + _messages.size());
				}
				else if (myFactory.isLogLots())
				{
					log.info("queueing message into blocked sender to " + callURL + " size=" + _messages.size());
				}
			}
		}
		if (!pause)
		{
			ThreadUtil.notifyAll(mutexDispatch);
		}
		DelayedPersist.getDelayedPersist().queue(this, 420000); // 7 minutes
		return !isBlocked(42000, 42);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string
	 */
	@Override
	public String toString()
	{
		return "MessageSender - URL: " + callURL.url + " size: " + _messages.size() + " total: " + sent + " last queued at " + XMLResponse.formatLong(lastQueued) + " last sent at "
				+ XMLResponse.formatLong(lastSent);
	}

	/**
	 * creates a descriptive xml Object for this MessageSender
	 *
	 * @param root the parent into which I append myself, if null create a new document
	 * @param posQueuedMessages
	 * @param bXJDF
	 *
	 * @return the appended element
	 */
	public KElement appendToXML(final KElement root, final int posQueuedMessages, final boolean bXJDF)
	{

		final KElement messagesRoot = root == null ? new XMLDoc("MessageSender", null).getRoot() : root.appendElement("MessageSender");
		synchronized (_messages)
		{
			messagesRoot.setAttribute(AttributeName.URL, callURL.url);
			messagesRoot.setAttribute(AttributeName.SIZE, _messages.size(), null);
			messagesRoot.setAttribute("NumSent", sent, null);
			messagesRoot.setAttribute("NumTry", trySend, null);
			messagesRoot.setAttribute("NumRemove", removedHeartbeat, null);
			messagesRoot.setAttribute("NumRemoveJMF", removedHeartbeatJMF, null);
			messagesRoot.setAttribute("NumRemoveFireForget", removedFireForget, null);
			messagesRoot.setAttribute("NumRemoveError", removedError, null);

			messagesRoot.setAttribute("LastQueued", XMLResponse.formatLong(lastQueued), null);
			messagesRoot.setAttribute("LastSent", XMLResponse.formatLong(lastSent), null);
			messagesRoot.setAttribute(AttributeName.CREATIONDATE, XMLResponse.formatLong(created), null);

			messagesRoot.setAttribute("pause", pause, null);
			messagesRoot.setAttribute("idle", idle, null);
			messagesRoot.setAttribute("Active", !doShutDown, null);
			final boolean problems = lastQueued - lastSent > 60000;
			messagesRoot.setAttribute("Problems", problems, null);
			messagesRoot.setAttribute("iLastQueued", StringUtil.formatLong(lastQueued), null);
			messagesRoot.setAttribute("iLastSent", StringUtil.formatLong(lastSent), null);
			messagesRoot.setAttribute("i" + AttributeName.CREATIONDATE, StringUtil.formatLong(created), null);
			messagesRoot.copyElement(timer.toXML(), null);

			if (posQueuedMessages == 0)
			{
				final MessageDetails[] old = sentMessages.peekArray();
				if (old != null)
				{
					for (int i = old.length - 1; i >= 0; i--)
					{
						old[i].appendToXML(messagesRoot, -1, bXJDF);
					}
				}
			}
			else if (posQueuedMessages > 0)
			{
				final MessageDetails old = sentMessages.peek(sentMessages.getFill() - posQueuedMessages);
				old.appendToXML(messagesRoot, posQueuedMessages, bXJDF);
			}
		}
		return messagesRoot;
	}

	/**
	 * set the base directory for serializing and deserializing messages
	 *
	 * @param _baseLocation the baseLocation to set
	 */
	public static void setBaseLocation(final File _baseLocation)
	{
		baseLocation = FileUtil.getFileInDirectory(_baseLocation, new File("JMFStore"));
		LogFactory.getLog(MessageSender.class).info("setting JMF Base dir to: " + _baseLocation);
	}

	/**
	 * remove all unsent messages without sending them
	 */
	public void flushMessages()
	{
		synchronized (_messages)
		{
			log.warn("Flushing " + _messages.size() + " Messages from " + toString());
			_messages.clear();
			final File pers = getPersistLocation(true);
			log.warn("Deleting message directory Messages from " + (pers == null ? "null" : pers.getAbsolutePath()));
			final boolean ok = FileUtil.deleteAll(pers);
			if (!ok)
			{
				log.error("Problems deleting message directory Messages from " + (pers == null ? "null" : pers.getAbsolutePath()));
			}
		}
	}

	/**
	 * remove first unsent message without sending it
	 */
	public void zappFirstMessage()
	{
		zappFirst = true;
		idle = 0;
		if (!pause)
		{
			ThreadUtil.notifyAll(mutexDispatch);
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
	public void setStartTime(final long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * check whether we have not sent for longer than deltaTime milliseconds
	 *
	 * @param deltaTime time in milliseconds that we need to be blocked
	 * @param blockSize size below which we never consider ourselves blocked
	 * @return
	 */
	public boolean isBlocked(final long deltaTime, final int blockSize)
	{
		if (_messages.size() < blockSize)
			return false;
		final long last = lastSent == 0 ? created : lastSent;
		return lastQueued - last > deltaTime;
	}

	/**
	 * @see org.cip4.jdflib.util.thread.IPersistable#persist()
	 */
	@Override
	public boolean persist()
	{
		write2Base(false);
		return true;
	}
}
