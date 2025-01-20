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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiContainer;
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
import org.cip4.jdflib.util.ListMap;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlPart;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.cip4.jdflib.util.thread.DelayedPersist;
import org.cip4.jdflib.util.thread.IPersistable;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * allow a JMF message to be sent in its own thread
 *
 * @author boegerni
 */
public class MessageSender implements Runnable, IPersistable
{

	static final int MAX_LOOP_WAIT = 424242;

	private static final Log sLog = LogFactory.getLog(MessageSender.class);

	private static final ListMap<String, DumpDir> dumpDirsMap = new ListMap<>();
	private static File baseLocation;

	protected final MessageFiFo messageFiFo;
	private final CallURL callURL;
	private final MyMutex mutexDispatch = new MyMutex();
	private final MyMutex mutexPause = new MyMutex();
	private final CPUTimer cpuTimer;
	private final SenderQueueOptimizer senderQueueOptimizer;

	SenderQueueOptimizer getSenderQueueOptimizer()
	{
		return senderQueueOptimizer;
	}

	protected FastFiFo<MessageDetails> fastFiFoMessageDetails;
	protected JMFFactory jmfFactory;
	protected int checked;
	protected int checkedJMF;
	protected int removedHeartbeat;
	protected int removedHeartbeatJMF;
	protected int removedFireForget;
	protected int removedError;
	int idle;
	private int bad;
	private boolean isShutdown = false;
	private boolean isPaused;
	private int trySend;
	private int sent;
	private long timeFirstProblem;
	boolean waitKaputt;
	private long timeCreated;
	private long timeLastQueued;
	long timeLastSent;
	private long timeStart;
	private boolean zappFirst;

	enum SendReturn
	{
		sent, empty, error, removed
	}

	/**
	 * Package private constructor. Please use the static {@link JMFFactory#getCreateMessageSender(String)} method for initializing.
	 *
	 * @param callURL the URL to send the message to
	 */
	MessageSender(final CallURL callURL)
	{
		super();

		this.timeFirstProblem = 0;
		this.trySend = 0;
		this.sent = 0;
		bad = 0;
		this.timeLastQueued = 0;
		this.timeLastSent = 0;
		this.isPaused = false;
		this.removedHeartbeat = 0;
		this.removedHeartbeatJMF = 0;
		this.removedFireForget = 0;
		this.checkedJMF = 0;
		this.checked = 0;
		this.waitKaputt = false;
		this.callURL = callURL;
		this.cpuTimer = new CPUTimer(false);
		this.timeCreated = System.currentTimeMillis();
		this.messageFiFo = new MessageFiFo(getPersistLocation(true));
		this.fastFiFoMessageDetails = new FastFiFo<>(42);
		this.senderQueueOptimizer = new SenderQueueOptimizer();
		this.timeCreated = System.currentTimeMillis();
		this.zappFirst = false;

		setJMFFactory(null);
		readFromBase();
	}

	/**
	 * @return the callURL associated with this
	 */
	public CallURL getCallURL()
	{
		return callURL;
	}

	protected class SenderQueueOptimizer
	{
		/**
		 * Default constructor.
		 */
		public SenderQueueOptimizer()
		{
			super();
		}

		/**
		 * Optimize a full JMF Message.
		 *
		 * @param jmfMessage The JMF Message to be optimized.
		 */
		protected void optimize(final JDFJMF jmfMessage)
		{
			final VElement messages = jmfMessage == null ? null : jmfMessage.getMessageVector(null, null);
			if (messages != null)
			{
				checkedJMF++;

				for (final KElement message : messages)
				{
					optimizeMessage((JDFMessage) message);
				}
			}
		}

		/**
		 * Optimize a single message.
		 *
		 * @param message The message to be optimized.
		 */
		void optimizeMessage(final JDFMessage message)
		{
			if (!(message instanceof JDFSignal))
			{
				return;
			}

			final EnumType messageType = message.getEnumType();
			if (messageType == null)
			{
				return;
			}

			final IMessageOptimizer messageOptimizer = jmfFactory.getOptimizer(messageType);
			if (messageOptimizer == null)
			{
				return;
			}

			optimizeSingle(message, messageOptimizer);
		}

		void optimizeSingle(final JDFMessage message, final IMessageOptimizer messageOptimizer)
		{
			checked++;
			final List<MessageDetails> messagesTail = messageFiFo.getTailClone();
			if (messagesTail != null)
			{
				for (int i = messagesTail.size() - 1; i >= 0; i--)
				{
					final MessageDetails messageDetails = messagesTail.get(i);
					if (messageDetails == null)
					{
						sLog.warn("empty message in tail...");
						break;
					}
					final JDFJMF jmf = messageDetails.jmf;
					if (jmf == null)
					{
						continue; // don't optimize mime packages
					}

					final VElement messages = jmf.getMessageVector(null, null);
					if (messages == null)
					{
						continue;
					}

					for (int n = messages.size() - 1; n >= 0; n--)
					{
						final JDFMessage oldMessage = (JDFMessage) messages.get(n);
						if (oldMessage instanceof JDFSignal)
						{
							final optimizeResult optimization = messageOptimizer.optimize(message, oldMessage);
							if (optimization == optimizeResult.remove)
							{
								removeMessage(oldMessage, messageDetails);
							}
							else if (optimization == optimizeResult.cont)
							{
								break; // we found a non matching message and must stop optimizing
							}
						}
					}
				}
			}
		}

		/**
		 * Remove a JMF Message.
		 */
		void removeMessage(final JDFMessage oldJmfMessage, final MessageDetails messageDetails)
		{
			synchronized (messageFiFo)
			{
				final JDFJMF jmf = oldJmfMessage.getJMFRoot();
				jmf.removeChild(oldJmfMessage);

				removedHeartbeat++;

				if (jmfFactory.isLogLots() || removedHeartbeat < 10 || removedHeartbeat % 1000 == 0)
				{
					sLog.info("removed redundant " + oldJmfMessage.getType() + " " + oldJmfMessage.getLocalName() + " Message ID= " + oldJmfMessage.getID() + " Sender= "
							+ oldJmfMessage.getSenderID() + "# " + removedHeartbeat + " / " + checked);
				}

				final VElement messages = jmf.getMessageVector(null, null);
				if (messages == null || messages.size() == 0)
				{
					messageFiFo.remove(messageDetails);
					removedHeartbeatJMF++;
					if (jmfFactory.isLogLots() || removedHeartbeatJMF < 10 || removedHeartbeatJMF % 1000 == 0)
					{
						sLog.info("removed redundant jmf # " + removedHeartbeatJMF + " ID: " + jmf.getID() + " total checked: " + checkedJMF);
					}
				}
			}
		}
	}

	/**
	 * The sender loop. <br/>
	 * Checks whether its vector of pending messages is empty. If it is not empty, the first message is sent and removed from the map.
	 */
	@Override
	public void run()
	{
		waitStartup();
		sLog.info("starting message sender loop " + this);
		senderLoop();
		sLog.info("stopped message sender loop " + this);
		write2Base(true);
	}

	/**
	 * This is the main loop!
	 */
	protected void senderLoop()
	{
		long lastLog = 0;

		while (!isShutdown)
		{
			if (isPaused)
			{
				sLog.info("senderloop to " + callURL.getBaseURL() + " is paused");
				if (!ThreadUtil.wait(mutexPause, 0) || isShutdown)
				{
					break;
				}

				sLog.info("senderloop to " + callURL.getBaseURL() + " is resumed");
			}

			lastLog = trySingle(lastLog);
		}

	}

	protected long trySingle(long lastLog)
	{
		final SendReturn sendReturn = trySendSingle();

		if (SendReturn.sent.equals(sendReturn))
		{
			postSent();
		}
		else
		{
			lastLog = postSendReturn(lastLog, sendReturn);
		}
		return lastLog;
	}

	final private static int MIN_IDLE = 10;

	protected long postSendReturn(long lastLog, final SendReturn sendReturn)
	{
		if (SendReturn.removed.equals(sendReturn))
		{
			idle = 0;
			waitKaputt = false;
		}
		else
		{
			idle++;
		}
		final int wait;
		checkShutdownIdle();

		// stepwise increment - try every second 10 times, then gradually increase
		if (idle > MIN_IDLE)
		{
			wait = Math.min(MAX_LOOP_WAIT, (15000 * idle / MIN_IDLE));
			lastLog = maxWait(lastLog, wait);
		}
		else
		{
			waitKaputt = false;
			wait = 1000;
		}

		if (!ThreadUtil.wait(mutexDispatch, wait))
		{
			shutDown();
		}
		return lastLog;
	}

	protected long maxWait(long lastLog, final int wait)
	{
		if (wait == MAX_LOOP_WAIT)
		{
			final long currentTime_0 = System.currentTimeMillis();
			if (messageFiFo.size() > 0 && (currentTime_0 - lastLog) > 60000L)
			{
				final String tmp = getReadableTime();
				sLog.warn("Waiting in blocked message thread: " + callURL.getBaseURL() + " unsuccessful for " + tmp + messageFiFo.size());
				lastLog = currentTime_0;
			}
		}
		waitKaputt = messageFiFo.size() > 420;
		return lastLog;
	}

	protected boolean checkShutdownIdle()
	{
		if ((idle > 3333) && messageFiFo.isEmpty())
		{
			// no success or idle for an hour...
			sLog.info("Shutting down idle and empty thread for base url: " + callURL.getBaseURL());
			shutDown();
			return true;
		}
		return false;
	}

	protected void postSent()
	{
		sent++;
		timeLastSent = System.currentTimeMillis();
		idle = 0;
		waitKaputt = false;
		if (jmfFactory.isLogLots() || sent < 10 || (sent % 1000) == 0)
		{
			sLog.info("successfully sent JMF #" + sent + " to " + callURL + " pending: " + messageFiFo.size());
		}
	}

	protected SendReturn trySendSingle()
	{
		SendReturn sendReturn;
		try
		{
			sendReturn = sendFirstMessage();
			cpuTimer.stop();
		}
		catch (final Throwable x)
		{
			sendReturn = SendReturn.error;
			sLog.error("Error sending message: ", x);
			cpuTimer.stop();
		}
		return sendReturn;
	}

	String getReadableTime()
	{
		final long currentTime_0 = System.currentTimeMillis();
		final long t0 = timeLastSent == 0 ? timeStart : timeLastSent;
		final long t = (currentTime_0 - t0) / 60000L;
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
		return tmp;
	}

	/**
	 * Make sure we don't spam during startup
	 */
	protected void waitStartup()
	{
		// wait a while before sending messages so that all processors are alive before we start throwing messages
		final long t = System.currentTimeMillis() - timeStart;
		if (t < 12500)
		{
			ThreadUtil.wait(mutexDispatch, (int) (12500 - t));
		}
	}

	/**
	 * Write all pending messages to disk.
	 *
	 * @param clearMessages if true flush me
	 */
	void write2Base(final boolean clearMessages)
	{
		final File persistLocation = getPersistLocation(false);
		if (persistLocation == null)
		{
			sLog.error("no persistant message file location - possible loss of pending messages");
		}
		else
		{
			synchronized (messageFiFo)
			{
				if (messageFiFo.size() > 0)
				{
					sLog.info("writing " + messageFiFo.size() + " pending messages to: " + persistLocation.getAbsolutePath());
				}

				messageFiFo.dumpHeadTail();

				final KElement messageSenderXml = appendToXML(null, -1, false);
				messageSenderXml.getOwnerDocument_KElement().write2File(persistLocation, 2, false);
				if (clearMessages)
				{
					messageFiFo.clear();
				}
			}
		}
	}

	/**
	 * Pause this sender until resume is called the thread still exists.
	 */
	public void pause()
	{
		isPaused = true;
	}

	/**
	 * Resume this sender after pause was called.
	 */
	public void resume()
	{
		isPaused = false;
		waitKaputt = false;
		idle = 0;
		ThreadUtil.notifyAll(mutexPause);
	}

	/**
	 * Read all queued messages from storage, normally called at startup.
	 */
	void readFromBase()
	{
		final File persistLocation = getPersistLocation(false);
		if (persistLocation == null)
		{
			sLog.error("cannot read persistant message file, bailing out");
			return;
		}
		if (!persistLocation.exists()) // nothing queued ,ciao
		{
			sLog.info("no persistant message file exists to read, bailing out! " + persistLocation);
			return;
		}

		final JDFDoc messageSenderXmlDoc = JDFDoc.parseFile(persistLocation);
		// adding existing messages prior to vTmp - they must be sent first
		if (messageSenderXmlDoc != null)
		{
			final KElement messageSenderXml = messageSenderXmlDoc.getRoot();
			isPaused = messageSenderXml.getBoolAttribute("pause", null, false);
			sent = messageSenderXml.getIntAttribute("NumSent", null, 0);
			trySend = messageSenderXml.getIntAttribute("NumTry", null, 0);
			removedFireForget = messageSenderXml.getIntAttribute("NumRemoveFireForget", null, 0);
			removedHeartbeat = messageSenderXml.getIntAttribute("NumRemove", null, 0);
			removedHeartbeatJMF = messageSenderXml.getIntAttribute("NumRemoveJMF", null, 0);
			removedError = messageSenderXml.getIntAttribute("NumRemoveError", null, 0);
			timeLastQueued = messageSenderXml.getLongAttribute("iLastQueued", null, 0);
			timeLastSent = messageSenderXml.getLongAttribute("iLastSent", null, 0);
			timeCreated = messageSenderXml.getLongAttribute("i" + AttributeName.CREATIONDATE, null, System.currentTimeMillis());
			if (timeCreated <= 0)
			{
				timeCreated = System.currentTimeMillis();
			}
		}
		else
		{
			sLog.warn("could not parse jmf message sender base file" + persistLocation.getAbsolutePath());
		}
	}

	/**
	 * @param parentDir if true return the parent directory
	 * @return the file where we persist
	 */
	protected File getPersistLocation(final boolean parentDir)
	{
		String filename = callURL.getBaseURL();
		filename = UrlUtil.removeProtocol(filename);
		filename = StringUtil.replaceCharSet(filename, ":\\", "/", 0);
		filename = StringUtil.replaceString(filename, "//", "/");
		if (filename == null)
		{
			sLog.error("cannot persist jmf to location; " + callURL.getBaseURL());
			return null;
		}

		File persistLocation = FileUtil.getFileInDirectory(baseLocation, new File(filename));
		persistLocation.mkdirs();

		if (!parentDir)
			persistLocation = FileUtil.getFileInDirectory(persistLocation, new File("Status.xml"));

		return persistLocation;
	}

	/**
	 * Send the first enqueued message and return true if all went well also update any returned responses for Bambi internally
	 *
	 * @return boolean true if the message is assumed sent false if an error was detected and the Message must remain in the queue
	 */
	SendReturn sendFirstMessage()
	{
		MessageDetails messageDetails;
		SendReturn sendReturn;

		synchronized (messageFiFo)
		{
			if (messageFiFo.isEmpty())
			{
				return SendReturn.empty;
			}
			cpuTimer.start();
			messageDetails = messageFiFo.get(0);
		}

		sendReturn = checkDetails(messageDetails);
		if (sendReturn != null)
		{
			return sendReturn;
		}

		sendReturn = sendHTTP(messageDetails);
		synchronized (messageFiFo)
		{
			sendReturn = processMessageResponse(messageDetails, sendReturn);
		}
		return sendReturn;
	}

	SendReturn processMessageResponse(final MessageDetails messageDetails, SendReturn sendReturn)
	{
		if (SendReturn.sent == sendReturn)
		{
			processSuccess(messageDetails);
		}
		else if (SendReturn.removed.equals(sendReturn))
		{
			messageFiFo.remove(0);
			removedError++;
		}
		else
		{
			sendReturn = processProblem(messageDetails, sendReturn);
		}
		messageDetails.setReturn(sendReturn);
		return sendReturn;
	}

	public SendReturn processProblem(final MessageDetails messageDetails, SendReturn sendReturn)
	{
		if (timeFirstProblem == 0)
			timeFirstProblem = System.currentTimeMillis();

		String isMime = "";

		if (messageDetails.jmf != null)
			isMime = "JMF";

		if (messageDetails.jdf != null)
			isMime += "MIME";

		if ("".equals(isMime))
			isMime = "Empty";

		boolean logsRequired;
		String textWarning = "Sender: " + messageDetails.senderID + " Error sending " + isMime + " message to: " + messageDetails.url + " return code=" + sendReturn;

		if (messageDetails.isFireForget())
		{
			textWarning += " - removing fire&forget " + messageDetails.getName() + " message #";
			messageFiFo.remove(0);
			removedFireForget++;
			textWarning += removedFireForget;
			sendReturn = SendReturn.removed;
			logsRequired = (removedFireForget < 10) || (removedFireForget % 100) == 0;
		}
		else
		{
			if ((System.currentTimeMillis() - messageDetails.createTime) > (1000L * 3600L * 24L * 42L) && (messageFiFo.size() > 1000))
			{
				textWarning += " - removing prehistoric reliable " + messageDetails.getName() + " message: creation time: "
						+ new JDFDate(messageDetails.createTime).getDateTimeISO() + " messages pending: " + messageFiFo.size();
				messageFiFo.remove(0);
				removedError++;
				sendReturn = SendReturn.removed;
				logsRequired = (removedError < 10) || (removedError % 100) == 0;
			}
			else
			{
				textWarning += " - retaining " + messageDetails.getName() + " message for resend; messages pending: " + messageFiFo.size() + " times delayed: " + bad;
				logsRequired = (bad < 10) || ((bad % 100) == 0);
			}
		}
		if (logsRequired)
		{
			sLog.warn(textWarning);
		}
		return sendReturn;
	}

	public void processSuccess(final MessageDetails messageDetails)
	{
		if (timeFirstProblem != 0)
		{
			reactivate(messageDetails);
		}
		timeFirstProblem = 0;
		bad = 0;
		messageFiFo.remove(0);
		fastFiFoMessageDetails.push(messageDetails);

		if (jmfFactory.isLogLots())
		{
			String textInfo = "Successfully sent " + messageDetails.getName() + " #" + sent + " to " + messageDetails.url;
			if (messageFiFo.size() > 0)
			{
				textInfo += " waiting: " + messageFiFo.size();
			}
			sLog.info(textInfo);
		}
	}

	SendReturn checkDetails(final MessageDetails messageDetails)
	{
		if (messageDetails == null)
		{
			messageFiFo.remove(0);
			sLog.warn("removed null message in message queue ");
			return SendReturn.removed;
		}
		else if (zappFirst || KElement.isWildCard(messageDetails.url))
		{
			messageFiFo.remove(0);
			removedError++;
			zappFirst = false;
			sLog.warn("removed first " + messageDetails.getName() + " message in message queue to: " + messageDetails.url);
			messageDetails.setReturn(SendReturn.removed);
			fastFiFoMessageDetails.push(messageDetails);
			return SendReturn.removed;
		}
		else if (messageDetails.respHandler != null && messageDetails.respHandler.isAborted())
		{
			messageFiFo.remove(0);
			removedError++;
			sLog.warn("removed timed out " + messageDetails.getName() + " message to: " + messageDetails.url);
			messageDetails.setReturn(SendReturn.removed);
			fastFiFoMessageDetails.push(messageDetails);
			return SendReturn.removed;
		}
		else
		{
			return null;
		}
	}

	void reactivate(final MessageDetails mesDetails)
	{
		final long durationWait = System.currentTimeMillis() - timeFirstProblem;
		final String duration;
		if (durationWait < 60000)
		{
			duration = (durationWait / 1000L) + " seconds";
		}
		else if (durationWait < 60L * 60000L)
		{
			duration = (durationWait / 60000L) + " minutes";
		}
		else if (durationWait < 3600L * 60000L)
		{
			duration = (durationWait / 3600000L) + " hours";
		}
		else
		{
			duration = (durationWait / (3600000L * 24L)) + " days";
		}

		sLog.info("successfully reactivated message sender " + mesDetails.getName() + " to: " + mesDetails.url + " after " + duration + " messages pending: " + messageFiFo.size());
	}

	/**
	 * Send a message via http.
	 *
	 * @param messageDetails the messagedetails
	 * @return the success as a SendReturn enum
	 */
	SendReturn sendHTTP(final MessageDetails messageDetails)
	{
		if (messageDetails.url == null || (!UrlUtil.isHttp(messageDetails.url) && !UrlUtil.isHttps(messageDetails.url)))
		{
			sLog.error("Invalid url: " + messageDetails.url + " removing message " + messageDetails.getName());
			return SendReturn.removed;
		}

		try
		{
			final HttpURLConnection connection = sendDetails(messageDetails);
			return processResponse(messageDetails, connection);
		}
		catch (final IllegalArgumentException e)
		{
			sLog.warn("Cannot process " + messageDetails.getName() + " Error=" + e.getMessage());
			return SendReturn.removed;
		}
		catch (final Throwable ex)
		{
			sLog.error("Exception in sendHTTP: " + ex.getClass().getSimpleName() + " Message= " + ex.getMessage(), ex);
			if (messageDetails.respHandler != null)
			{
				messageDetails.respHandler.handleMessage(); // make sure we tell anyone who is waiting that the wait is over...
			}
			return SendReturn.error;
		}
	}

	SendReturn processResponse(final MessageDetails messageDetails, HttpURLConnection connection) throws IOException
	{
		SendReturn sendReturn = SendReturn.sent;
		final String url = messageDetails.url;

		String textHeader = "URL: " + url;
		int responseCode = -1;

		if (connection != null)
		{
			try
			{
				responseCode = connection.getResponseCode();

				if (messageDetails.respHandler != null)
				{
					messageDetails.respHandler.setConnection(connection);
				}

				connection.setReadTimeout(30000); // 30 seconds should suffice

				textHeader += "\nResponse code:" + responseCode;
				textHeader += "\nContent type:" + connection.getContentType();
				textHeader += "\nContent length:" + connection.getContentLength();
			}
			catch (final FileNotFoundException fx)
			{
				// this happens when a server is at the url but the war is not loaded
				sLog.warn("Error reading response: " + fx.getMessage());
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
			if (idle < 10 || (idle % 100 == 0))
			{
				sLog.warn("could not send message to unavailable " + messageDetails.url + " no return; rc= " + responseCode);
			}
		}
		else if (!UrlUtil.isReturnCodeOK(responseCode))
		{
			if (isRemoveResponseCode(responseCode))
			{
				sendReturn = SendReturn.removed;
				if (idle == 0)
				{
					sLog.error("removing message that causes server error at " + messageDetails.url + " rc= " + responseCode);
				}
			}
			else
			{
				sendReturn = SendReturn.error;
				if (bad == 0 || (bad % 100 == 0))
				{
					sLog.warn("error sending message " + messageDetails.getName() + " to " + messageDetails.url + " rc= " + responseCode);
				}
			}
		}
		if (messageDetails.respHandler != null)
		{
			messageDetails.respHandler.setConnection(connection);
			messageDetails.respHandler.setBufferedStream(bis == null ? null : new ByteArrayIOStream(bis));

			final SendReturn sr2 = messageDetails.respHandler.handleMessage() ? SendReturn.sent : SendReturn.error;
			if (!SendReturn.error.equals(sendReturn))
			{
				sendReturn = sr2;
			}
		}

		final DumpDir inputDumpDir = getInputDumpDir(messageDetails.senderID);
		if (inputDumpDir != null)
		{
			inputDumpDir.newFileFromStream(textHeader, bis, messageDetails.getName() + "." + responseCode);
		}
		return sendReturn;
	}

	protected boolean isRemoveResponseCode(final int responseCode)
	{
		final boolean is400 = responseCode >= 400 && responseCode < 500 && responseCode != 404 && responseCode != 408 && responseCode != 429;
		boolean is500 = jmfFactory.isZapp500();

		if (is500)
			is500 = responseCode >= 500 && responseCode < 600 && responseCode != 503 && responseCode != 504 && responseCode != 507 && responseCode != 509;

		return is400 || is500;
	}

	HttpURLConnection sendDetails(final MessageDetails messageDetails) throws IOException, IllegalArgumentException
	{
		trySend++;
		if (messageDetails == null)
		{
			throw new IllegalArgumentException("sending null message");
		}

		final String url = messageDetails.url;
		final String contentType = messageDetails.getContentType();
		final InputStream is = messageDetails.getInputStream();
		if (is == null || StringUtil.isEmpty(url))
		{
			throw new IllegalArgumentException("sending null input message stream to " + url);
		}

		final HTTPDetails httpDetails = messageDetails.mimeDet == null ? null : messageDetails.mimeDet.httpDetails;
		final long t0 = System.currentTimeMillis();
		final UrlPart p = UrlUtil.writeToURL(url, ByteArrayIOStream.getBufferedInputStream(is), UrlUtil.POST, contentType, httpDetails);
		final int rc = UrlPart.getReturnCode(p);
		final long t1 = System.currentTimeMillis();
		if (!UrlPart.isReturnCodeOK(p))
		{
			sLog.warn("Flaky RC " + rc + " in JMF " + messageDetails.getName() + " response to " + url);
		}
		if ((t1 - t0) > 1234)
		{
			sLog.warn("long processing of " + messageDetails.getName() + " JMF " + (t1 - t0) + " mS for JMF response to " + url);
		}
		final DumpDir outputDumpDir = getOuputDumpDir(messageDetails.senderID);
		final String textHeader = "URL: " + url + "\nDeltaT: " + (t1 - t0);
		final File outputDumpDirFile = outputDumpDir == null ? null : outputDumpDir.newFile(textHeader, messageDetails.getName() + "." + rc);
		FileUtil.streamToFile(ByteArrayIOStream.getBufferedInputStream(is), outputDumpDirFile);
		return (HttpURLConnection) (p == null ? null : p.getConnection());
	}

	/**
	 * Stop sending new messages immediately and shut down.
	 */
	public void shutDown()
	{
		isShutdown = true;
		jmfFactory.senders.remove(callURL);
		ThreadUtil.notifyAll(mutexDispatch);
		ThreadUtil.notifyAll(mutexPause);
	}

	/**
	 * Return true if the thread is still running.
	 *
	 * @return True if running, otherwise false
	 */
	public boolean isRunning()
	{
		return !isShutdown;
	}

	/**
	 * Return true if test URL fits this url
	 *
	 * @param otherUrl the url to check against
	 * @return true if running
	 */
	public boolean matchesURL(final String otherUrl)
	{
		return otherUrl == null || new CallURL(otherUrl).equals(callURL);
	}

	/**
	 * Get the input dump directory for this message sender.
	 */
	DumpDir getInputDumpDir(final String senderID)
	{
		final BambiContainer c = BambiContainer.getInstance();
		return (c != null && !c.wantDump()) ? null : dumpDirsMap.getOne(senderID, 0);
	}

	/**
	 * Get the output dump directory for this message sender.
	 */
	DumpDir getOuputDumpDir(final String senderID)
	{
		final BambiContainer c = BambiContainer.getInstance();
		return (c != null && !c.wantDump()) ? null : dumpDirsMap.getOne(senderID, 1);
	}

	/**
	 * Add debug dump directories for a given senderID
	 *
	 * @param senderID The senders ID.
	 * @param inputDumpDir The input dump directory.
	 * @param outputDumpDir The output dump directory.
	 */
	public static void addDumps(final String senderID, final DumpDir inputDumpDir, final DumpDir outputDumpDir)
	{
		dumpDirsMap.putOne(senderID, inputDumpDir);
		dumpDirsMap.putOne(senderID, outputDumpDir);
	}

	/**
	 * Queues a message for the URL that this MessageSender belongs to also updates the message for a given recipient if required.
	 */
	public boolean queueMessage(final JDFJMF jmf, final IResponseHandler responseHandler, final String url, final IConverterCallback converterCallback, final HTTPDetails httpDetails)
	{
		if (isShutdown)
		{
			sLog.warn("cannot queue message during shutdown!");
			return false;
		}
		else if (jmf == null)
		{
			sLog.warn("cannot queue null message!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(jmf, responseHandler, converterCallback, httpDetails, url);
		return queueMessageDetails(messageDetails);
	}

	/**
	 * Queues a message for the URL that this MessageSender belongs to also updates the message for a given recipient if required
	 */
	public boolean queueMessage(final JDFJMF jmf, final JDFNode jdfNode, final IResponseHandler responseHandler, final String url, final IConverterCallback converterCallback, final MIMEDetails mimeDetails)
	{
		if (isShutdown)
		{
			sLog.warn("cannot queue message during shutdown!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(jmf, jdfNode, responseHandler, converterCallback, mimeDetails, url);
		return queueMessageDetails(messageDetails);
	}

	/**
	 * queues a message for the URL that this MessageSender belongs to also updates the message for a given recipient if required
	 */
	public boolean queuePost(final IResponseHandler responseHandler, final String url, final IConverterCallback converterCallback)
	{
		if (isShutdown)
		{
			sLog.warn("cannot queue message during shutdown!");
			return false;
		}

		final MessageDetails messageDetails = new MessageDetails(null, responseHandler, converterCallback, null, url);
		return queueMessageDetails(messageDetails);
	}

	boolean queueMessageDetails(final MessageDetails messageDetails)
	{
		if (waitKaputt && messageDetails.isFireForget())
		{
			removedFireForget++;

			if (jmfFactory.isLogLots() || removedFireForget < 10 || (removedFireForget % 1000) == 0)
			{
				String textWarning = " not queueing fire&forget to " + callURL.url + "; message #";
				textWarning += removedFireForget;
				textWarning += " currently waiting: " + messageFiFo.size();
				sLog.warn(textWarning);
			}
			trySend++;
			return false;
		}
		timeLastQueued = System.currentTimeMillis();
		if (messageFiFo.size() > 333)
		{
			senderQueueOptimizer.optimize(messageDetails.jmf);
		}

		if (jmfFactory.isLogLots())
		{
			String textInfo = "queued " + messageDetails.getName() + " #" + sent + " to " + messageDetails.url;
			if (messageFiFo.size() > 0)
			{
				textInfo += " size=" + messageFiFo.size();
			}
			sLog.info(textInfo);
		}

		messageFiFo.add(messageDetails);

		if (messageFiFo.size() >= 1000)
		{
			if ((messageFiFo.size() % 100) == 0)
			{
				sLog.warn("queueing message into blocked sender to " + callURL + " size=" + messageFiFo.size());
			}
			else if (jmfFactory.isLogLots())
			{
				sLog.info("queueing message into blocked sender to " + callURL + " size=" + messageFiFo.size());
			}
		}
		if (!isPaused)
		{
			ThreadUtil.notifyAll(mutexDispatch);
		}
		DelayedPersist.getDelayedPersist().queue(this, 420000); // 7 minutes
		return !isBlocked(42000, 42);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MessageSender - URL: " + callURL.url + " size: " + messageFiFo.size() + " total: " + sent + " last queued at " + XMLResponse.formatLong(timeLastQueued)
				+ " last sent at " + XMLResponse.formatLong(timeLastSent);
	}

	/**
	 * Creates a descriptive xml Object for this MessageSender
	 */
	public KElement appendToXML(final KElement messageSenderXmlRoot, final int posQueuedMessages, final boolean bXJDF)
	{
		final KElement messageSenderXml = messageSenderXmlRoot == null ? new XMLDoc("MessageSender", null).getRoot() : messageSenderXmlRoot.appendElement("MessageSender");

		synchronized (messageFiFo)
		{
			messageSenderXml.setAttribute(AttributeName.URL, callURL.url);
			messageSenderXml.setAttribute(AttributeName.SIZE, messageFiFo.size(), null);
			messageSenderXml.setAttribute("NumSent", sent, null);
			messageSenderXml.setAttribute("NumTry", trySend, null);
			messageSenderXml.setAttribute("NumRemove", removedHeartbeat, null);
			messageSenderXml.setAttribute("NumRemoveJMF", removedHeartbeatJMF, null);
			messageSenderXml.setAttribute("NumRemoveFireForget", removedFireForget, null);
			messageSenderXml.setAttribute("NumRemoveError", removedError, null);

			messageSenderXml.setAttribute("LastQueued", XMLResponse.formatLong(timeLastQueued), null);
			messageSenderXml.setAttribute("LastSent", XMLResponse.formatLong(timeLastSent), null);
			messageSenderXml.setAttribute(AttributeName.CREATIONDATE, XMLResponse.formatLong(timeCreated), null);

			messageSenderXml.setAttribute("pause", isPaused, null);
			messageSenderXml.setAttribute("idle", idle, null);
			messageSenderXml.setAttribute("bad", bad, null);
			messageSenderXml.setAttribute("firstProblem", timeFirstProblem, null);
			messageSenderXml.setAttribute("Active", !isShutdown, null);
			final boolean problems = timeLastQueued - timeLastSent > 60000;
			messageSenderXml.setAttribute("Problems", problems, null);
			messageSenderXml.setAttribute("iLastQueued", StringUtil.formatLong(timeLastQueued), null);
			messageSenderXml.setAttribute("iLastSent", StringUtil.formatLong(timeLastSent), null);
			messageSenderXml.setAttribute("i" + AttributeName.CREATIONDATE, StringUtil.formatLong(timeCreated), null);
			messageSenderXml.copyElement(cpuTimer.toXML(), null);

			if (posQueuedMessages == 0)
			{
				final MessageDetails[] old = fastFiFoMessageDetails.peekArray();
				if (old != null)
				{
					for (int i = old.length - 1; i >= 0; i--)
					{
						old[i].appendToXML(messageSenderXml, -1, bXJDF);
					}
				}
			}
			else if (posQueuedMessages > 0)
			{
				final MessageDetails old = fastFiFoMessageDetails.peek(fastFiFoMessageDetails.getFill() - posQueuedMessages);
				old.appendToXML(messageSenderXml, posQueuedMessages, bXJDF);
			}
		}

		return messageSenderXml;
	}

	/**
	 * Set the base directory for serializing and de-serializing messages.
	 *
	 * @param baseLocation the baseLocation to set
	 */
	public static void setBaseLocation(final File baseLocation)
	{
		MessageSender.baseLocation = FileUtil.getFileInDirectory(baseLocation, new File("JMFStore"));
		sLog.info("setting JMF Base dir to: " + baseLocation);
	}

	/**
	 * Remove all unsent messages without sending them.
	 */
	public void flushMessages()
	{
		synchronized (messageFiFo)
		{
			sLog.warn("Flushing " + messageFiFo.size() + " Messages from " + toString());
			messageFiFo.clear();
			final File persistLocation = getPersistLocation(true);
			sLog.warn("Deleting message directory Messages from " + (persistLocation == null ? "null" : persistLocation.getAbsolutePath()));

			final boolean ok = FileUtil.deleteAll(persistLocation);
			if (!ok)
			{
				sLog.error("Problems deleting message directory Messages from " + (persistLocation == null ? "null" : persistLocation.getAbsolutePath()));
			}
		}
	}

	/**
	 * Remove first unsent message without sending it.
	 */
	public void zappFirstMessage()
	{
		zappFirst = true;
		idle = 0;
		if (!isPaused)
		{
			ThreadUtil.notifyAll(mutexDispatch);
		}
	}

	/**
	 * Set the JMFFactory instance for this message sender.
	 *
	 * @param jmfFactory the myFactory to set
	 */
	public void setJMFFactory(final JMFFactory jmfFactory)
	{
		this.jmfFactory = jmfFactory;
		if (this.jmfFactory == null)
		{
			this.jmfFactory = JMFFactory.getInstance();
		}
	}

	/**
	 * Returns the JMFFactory instance this message sender is based on.
	 *
	 * @return the JMFFactory instance.
	 */
	public JMFFactory getJMFFactory()
	{
		return jmfFactory;
	}

	/**
	 * Set the startTime to the startTime of the device.
	 *
	 * @param startTime The start time
	 */
	public void setStartTime(final long startTime)
	{
		this.timeStart = startTime;
	}

	/**
	 * Check whether we have not sent for longer than deltaTime milliseconds
	 *
	 * @param deltaTime time in milliseconds that we need to be blocked
	 * @param blockSize size below which we never consider ourselves blocked
	 * @return True, in case of a block. Otherwise false.
	 */
	public boolean isBlocked(final long deltaTime, final int blockSize)
	{
		if (messageFiFo.size() < blockSize)
		{
			return false;
		}

		final long timeLast = timeLastSent == 0 ? timeCreated : timeLastSent;
		return timeLastQueued - timeLast > deltaTime;
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
