/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of
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

package org.cip4.bambi.proxy;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.XMLDevice;
import org.cip4.bambi.core.messaging.CommandProxyHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.NotificationBufferHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.ResourceBufferHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.StatusBufferHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.proxy.MessageChecker.KnownMessageDetails;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFSubscriptionInfo;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.hotfolder.QueueHotFolder;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public abstract class AbstractProxyDevice extends AbstractDevice
{
	private final static Log log = LogFactory.getLog(AbstractProxyDevice.class);

	protected static int slaveThreadCount = 0;
	protected HashMap<String, SlaveSubscriber> waitingSubscribers;
	/**
	 * the url flag for incoming messages (end point of the path)
	 */
	public static final String SLAVEJMF = "slavejmf";
	/**
	 * statusdetails flag for sent to slave but not yet confirmed
	 */
	public static final String SUBMITTING = "Submitting";
	/**
	 * statusdetails flag for successfully sent to slave
	 */
	public static final String SUBMITTED = "Submitted";

	/**
	 * watched hot folder for hf based communication with a device (completed)
	 */
	protected QueueHotFolder slaveJDFOutput;
	/**
	 * watched hot folder for hf based communication with a device (aborted)
	 */
	protected QueueHotFolder slaveJDFError;

	/**
	 * the list of pending subscriptions that have been sent to the slave device
	 */
	protected SubscriptionMap mySubscriptions;

	/**
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 *
	 *         enumeration how to set up synchronization of status with the slave
	 */
	public enum EnumSlaveStatus
	{
		/**
		 * update status via single global JMF
		 */
		JMFGLOBAL,
		/**
		 * update status via JMF
		 */
		JMF,

		/**
		 * update status via NodeInfo/JMF
		 */
		NODEINFO,
		/**
		 * don't update status
		 */
		NONE
	}

	/**
	 * the callback between this device (internal) and the slave device (external)
	 */
	protected IConverterCallback _slaveCallback;
	protected MessageChecker knownSlaveMessages;
	private JMFHandler _slaveJmfHandler;

	/**
	 *
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected class StopPersistantHandler extends MessageResponseHandler
	{
		/**
		 * @param jmf
		 */
		public StopPersistantHandler(final JDFJMF jmf)
		{
			super(jmf);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.MessageResponseHandler#finalizeHandling()
		 */
		@Override
		protected void finalizeHandling()
		{
			super.finalizeHandling();
			if (finalMessage == null || finalMessage.getReturnCode() != 0)
			{
				log.warn("aborted StopPersistantHandler");
				return;
			}
			if (mySubscriptions != null)
			{
				log.warn("clearing my subscription list");
				mySubscriptions.clear();
			}
		}
	}

	/**
	 *
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected class KnownMessagesResponseHandler extends MessageResponseHandler
	{

		/**
		 * @param jmf the jmf containing the query to handle as first query
		 *
		 */
		public KnownMessagesResponseHandler(final JDFJMF jmf)
		{
			super(jmf);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.MessageResponseHandler#finalizeHandling()
		 */
		@Override
		protected void finalizeHandling()
		{
			super.finalizeHandling();
			getLog().info("Finalized handling of KnownMessages");
		}

		/**
		 * @return vector of all jmfs that still need to be sent
		 *
		 */
		public Set<KnownMessageDetails> completeHandling()
		{
			final JDFMessage m = getFinalMessage();
			final Set<KnownMessageDetails> theSet = new HashSet<KnownMessageDetails>();
			final VElement v = m == null ? null : m.getChildElementVector(ElementName.MESSAGESERVICE, null);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final JDFMessageService ms = (JDFMessageService) v.get(i);
					final KnownMessageDetails processedMessageService = processMessageService(ms);
					if (processedMessageService != null)
						theSet.add(processedMessageService);
				}
			}
			return theSet;
		}

		/**
		 * process an individual existing messageservice and update the cache appropriately
		 * 
		 * @param ms
		 * @return
		 */
		private KnownMessageDetails processMessageService(final JDFMessageService ms)
		{
			final KnownMessageDetails knownMessageDetails = new KnownMessageDetails(ms);
			return knownMessageDetails.getType() == null ? null : knownMessageDetails;
		}
	}

	/**
	 *
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected class KnownSubscriptionsHandler extends MessageResponseHandler
	{
		// list of any missing subscriptions
		private final Vector<JDFJMF> sendjmfs;

		/**
		 * @param jmf the jmf containing the query to handle as first query
		 * @param createSubscriptions
		 */
		public KnownSubscriptionsHandler(final JDFJMF jmf, final Vector<JDFJMF> createSubscriptions)
		{
			super(jmf);
			sendjmfs = createSubscriptions;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.MessageResponseHandler#finalizeHandling()
		 */
		@Override
		protected void finalizeHandling()
		{
			super.finalizeHandling();
			getLog().info("Finalized handling of KnownSubscriptions");
		}

		/**
		 * @return vector of all jmfs that still need to be sent
		 *
		 */
		public Vector<JDFJMF> completeHandling()
		{
			if (sendjmfs == null)
				return null;
			final JDFMessage m = getFinalMessage();
			final Vector<JDFJMF> vjmf = new Vector<JDFJMF>();
			final VElement v = m == null ? null : m.getChildElementVector(ElementName.SUBSCRIPTIONINFO, null);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final JDFSubscriptionInfo si = (JDFSubscriptionInfo) v.get(i);
					processSubscription(si);
				}
			}
			vjmf.addAll(sendjmfs);
			return vjmf;
		}

		/**
		 * process an individual existing subscription and update the cache appropriately
		 * 
		 * @param si
		 */
		private void processSubscription(final JDFSubscriptionInfo si)
		{
			final String channelID = StringUtil.getNonEmpty(si.getChannelID());
			if (channelID == null)
			{
				log.warn("SubscriptionInfo without channelID, ignore");
				return;
			}
			final JDFSubscription subscription = si.getSubscription();
			final String url = subscription == null ? null : subscription.getURL();
			final String deviceURLForSlave = getDeviceURLForSlave();
			if (!ContainerUtil.equals(url, deviceURLForSlave))
			{
				log.warn("SubscriptionInfo for wrong url:" + deviceURLForSlave + ", ignore");
				return;
			}
			final EnumType siType = si.getEnumType();
			for (final JDFJMF jdfjmf : sendjmfs)
			{
				final EnumType typ = jdfjmf.getQuery(0).getEnumType();
				if (ContainerUtil.equals(typ, siType))
				{
					// may be null at startup - ignore counting
					final ProxySubscription oldSub = mySubscriptions == null ? null : mySubscriptions.get(typ);
					if (oldSub == null)
					{
						getLog().info("adding existing subscription to list; type=" + typ.getName() + " channelID=" + channelID);
						jdfjmf.getMessageElement(null, null, 0).setID(channelID);
						if (mySubscriptions != null) // may be null at startup or shutdown - ignore we'll only be off by a few messages
						{
							mySubscriptions.put(typ, new ProxySubscription(jdfjmf));
						}
					}
					else
					{
						oldSub.setChannelID(channelID);
					}
					sendjmfs.remove(jdfjmf);
					break;
				}
			}
		}
	}

	/**
	 *
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected class QueueEntryAbortHandler extends MessageResponseHandler
	{
		private final EnumNodeStatus newStatus;

		/**
		 * @param newStatus the status type of the original message, e.g Aborted for AbortQE
		 * @param refID the refID of the outgoing message
		 */
		public QueueEntryAbortHandler(final EnumNodeStatus newStatus, final String refID)
		{
			super(refID);
			this.newStatus = newStatus;
		}

		@Override
		public boolean handleMessage()
		{
			super.handleMessage();
			if (resp != null)
			{
				// TODO System.out.print(resp);
			}
			// TODO actually handle the queue updates in here, rather than downstream
			return true;
		}

		/**
		 * @return the final status, null if the message failed
		 */
		public EnumNodeStatus getFinalStatus()
		{
			final JDFMessage response = getFinalMessage();
			if (response != null)
			{
				if (response.getReturnCode() == 0)
				{
					return newStatus;
				}
			}
			return null;
		}
	}

	/**
	 *
	 * @author Rainer Prosi, Heidelberger Druckmaschinen *
	 */
	protected class QueueSynchronizeHandler extends MessageResponseHandler
	{
		/**
		 * @param jmf the jmf containing the query to handle as first query
		 *
		 */
		public QueueSynchronizeHandler(final JDFJMF jmf)
		{
			super(jmf);
		}

		@Override
		public boolean handleMessage()
		{
			super.handleMessage();
			final JDFMessage r = getFinalMessage();
			if (r != null)
			{
				final JDFQueue q = r.getQueue(0);
				if (q != null)
				{
					final Map<String, JDFQueueEntry> slaveMap = q.getQueueEntryIDMap();
					final QueueProcessor queueProcessor = getQueueProcessor();
					final JDFQueue myQueue = queueProcessor.getQueue();
					synchronized (myQueue)
					{
						final VElement vQ = myQueue.getQueueEntryVector();
						for (int i = 0; i < vQ.size(); i++)
						{
							final JDFQueueEntry qe = (JDFQueueEntry) vQ.get(i);
							final String slaveQEID = BambiNSExtension.getSlaveQueueEntryID(qe);
							if (slaveQEID != null)
							{
								final JDFQueueEntry slaveQE = slaveMap == null ? null : slaveMap.get(slaveQEID);
								if (slaveQE != null)
								{
									final EnumQueueEntryStatus status = slaveQE.getQueueEntryStatus();
									final String statusDetails = StringUtil.getNonEmpty(slaveQE.getStatusDetails());
									if (!ContainerUtil.equals(status, qe.getQueueEntryStatus()))
									{
										queueProcessor.updateEntry(qe, status, null, null, slaveQE.getStatusDetails());
										if (EnumQueueEntryStatus.Completed.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Completed, statusDetails);
										}
										else if (EnumQueueEntryStatus.Aborted.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Aborted, statusDetails);
										}
										else if (EnumQueueEntryStatus.Suspended.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Suspended, statusDetails);
										}
									}
								}
								else
								{
									log.info("Slave queueentry " + slaveQEID + " was removed");
									queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Removed, null, null, null);
								}
							}
						}
					}
				}
			}
			return true; // we always assume ok
		}
	}

	/**
	 * @param properties properties with device details
	 */
	public AbstractProxyDevice(final IDeviceProperties properties)
	{
		super(properties);
		skipIdle = 1;
	}

	/**
	 * prepare output and error hot folders if they are specified
	 * 
	 * @param proxyProperties
	 */
	protected void prepareSlaveHotfolders(final IProxyProperties proxyProperties)
	{
		final File fDeviceJDFOutput = proxyProperties.getSlaveOutputHF();
		if (fDeviceJDFOutput != null)
		{
			reloadSlaveOutputHF(fDeviceJDFOutput);
		}

		final File fDeviceErrorOutput = proxyProperties.getSlaveErrorHF();
		if (fDeviceErrorOutput != null)
		{
			reloadSlaveErrorHF(fDeviceErrorOutput);
		}
	}

	/**
	 * @param deviceErrorOutput
	 */
	protected void reloadSlaveErrorHF(final File deviceErrorOutput)
	{
		if (slaveJDFError != null)
		{
			slaveJDFError.stop();
			slaveJDFError = null;
		}
		if (deviceErrorOutput != null)
		{
			final File hfStorage = new File(getBaseDir() + File.separator + "HFDevTmpStorage" + File.separator + getDeviceID());
			log.info("Device error output HF:" + deviceErrorOutput.getPath() + " device ID= " + getSlaveDeviceID());
			final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
			slaveJDFError = new QueueHotFolder(deviceErrorOutput, hfStorage, null, new ReturnHFListner(this, EnumQueueEntryStatus.Aborted), rqCommand);
			slaveJDFError.setSynchronous(true);
		}
	}

	/**
	 * @param deviceJDFOutput
	 */
	protected void reloadSlaveOutputHF(final File deviceJDFOutput)
	{
		if (slaveJDFOutput != null)
		{
			slaveJDFOutput.stop();
			slaveJDFOutput = null;
		}
		if (deviceJDFOutput != null)
		{
			final File hfStorage = new File(getDeviceDir() + File.separator + "HFDevTmpStorage");
			log.info("Device output HF:" + deviceJDFOutput.getPath() + " device ID= " + getSlaveDeviceID());
			final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
			slaveJDFOutput = new QueueHotFolder(deviceJDFOutput, hfStorage, null, new ReturnHFListner(this, EnumQueueEntryStatus.Completed), rqCommand);
			slaveJDFOutput.setSynchronous(true);
		}
	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#init()
	 */
	@Override
	protected void init()
	{
		knownSlaveMessages = new MessageChecker(this);
		final IProxyProperties proxyProperties = getProperties();
		_slaveCallback = getSlaveCallBackClass();
		waitingSubscribers = new HashMap<String, SlaveSubscriber>();
		mySubscriptions = createSubscriptionMap();
		prepareSlaveHotfolders(proxyProperties);

		super.init();

		getJMFHandler(getDeviceURL()).setFilterOnDeviceID(false);
		_theSignalDispatcher.setIgnoreURL(getDeviceURLForSlave());
		// ensure existence of vector prior to filling
		_theQueueProcessor.getQueue().resumeQueue(); // proxy queues should start up by default
		addSlaveSubscriptions(8888, null, false);
	}

	/**
	 *
	 * @return
	 */
	public IConverterCallback getSlaveCallBackClass()
	{
		return getProperties().getSlaveCallBackClass();
	}

	/**
	 *
	 *
	 * @return
	 */
	protected SubscriptionMap createSubscriptionMap()
	{
		return new SubscriptionMap();
	}

	/**
	 * @param waitMillis
	 * @param slaveQEID
	 * @param reset if true remove all existing subscriptions
	 */
	public void addSlaveSubscriptions(final int waitMillis, String slaveQEID, final boolean reset)
	{
		final EnumSlaveStatus slaveStatus = getSlaveStatus();
		if (!EnumSlaveStatus.JMFGLOBAL.equals(slaveStatus) && !EnumSlaveStatus.JMF.equals(slaveStatus))
			return;
		if (EnumSlaveStatus.JMFGLOBAL.equals(slaveStatus))
			slaveQEID = null;

		final SlaveSubscriber slaveSubscriber = getSlaveSubscriber(waitMillis, slaveQEID);
		if (slaveSubscriber != null)
		{
			slaveSubscriber.setReset(reset);
			slaveSubscriber.start();
		}
	}

	/**
	 * slave subscriber factory
	 *
	 * @param waitMillis
	 * @param slaveQEID
	 * @return
	 */
	protected final SlaveSubscriber getSlaveSubscriber(final int waitMillis, final String slaveQEID)
	{
		final String slaveURL = getSlaveURL();
		if (StringUtil.getNonEmpty(slaveURL) == null)
		{
			log.warn("cannot retrieve slave subscriber for null URL");
			return null;
		}
		else
		{
			log.info("retrieve slave subscriber for URL: " + slaveURL);
		}
		final String key = getKey(slaveQEID);
		synchronized (waitingSubscribers)
		{
			SlaveSubscriber slaveSubscriber = waitingSubscribers.get(key);
			if (slaveSubscriber != null)
			{
				log.info("retrieve duplicate slave subscriber for URL: " + slaveURL);
				return null;
			}
			slaveSubscriber = createSlaveSubscriber(waitMillis, slaveQEID);
			return slaveSubscriber;
		}
	}

	/**
	 * @param waitMillis
	 * @param slaveQEID
	 * @return
	 */
	protected SlaveSubscriber createSlaveSubscriber(final int waitMillis, final String slaveQEID)
	{
		return new SlaveSubscriber(this, waitMillis, slaveQEID);
	}

	/**
	 * @param slaveQEID
	 * @return
	 */
	protected String getKey(final String slaveQEID)
	{
		return slaveQEID == null ? "##__null__##" : slaveQEID;
	}

	/**
	 * @return
	 */
	public IConverterCallback getSlaveCallback()
	{
		return _slaveCallback;
	}

	/**
	 * @return
	 */
	public File getSlaveInputHF()
	{
		return getProperties().getSlaveInputHF();
	}

	/**
	 * @return
	 */
	public File getSlaveOutputHF()
	{
		return getProperties().getSlaveOutputHF();
	}

	/**
	 * @return
	 */
	public String getSlaveDeviceID()
	{
		// TODO - dynamically grab with knowndevices
		return getProperties().getSlaveDeviceID();
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#shutdown()
	 */
	@Override
	public void shutdown()
	{
		if (mySubscriptions != null)
		{
			mySubscriptions.shutdown(this);
		}
		super.shutdown();
	}

	/**
	 * sends messages to the slave to stop processing
	 * 
	 * @param newStatus
	 * @param slaveQE
	 * @return
	 */
	protected EnumNodeStatus stopSlaveProcess(final String slaveQE, final EnumNodeStatus newStatus)
	{
		JDFJMF jmf = null;
		final JMFBuilder builder = getBuilderForSlave();
		if (EnumNodeStatus.Aborted.equals(newStatus))
		{
			jmf = builder.buildAbortQueueEntry(slaveQE);
		}
		else if (EnumNodeStatus.Suspended.equals(newStatus))
		{
			jmf = builder.buildSuspendQueueEntry(slaveQE);
		}
		if (jmf != null)
		{
			final QueueEntryAbortHandler ah = new QueueEntryAbortHandler(newStatus, jmf.getCommand(0).getID());
			sendJMFToSlave(jmf, ah);
			ah.waitHandled(5000, 10000, false);
			return ah.getFinalStatus();
		}
		return null;
	}

	/**
	 * send a message to the slave device
	 * 
	 * @param jmf the jmf to send
	 * @param mh the message response handler, may be null
	 * @return true if successfully queues @see sendJMF
	 */
	public boolean sendJMFToSlave(final JDFJMF jmf, final MessageResponseHandler mh)
	{
		if (jmf == null)
		{
			log.warn("Skip sending null jmf to slave.");
			return false;
		}
		final String slaveURL = StringUtil.getNonEmpty(getSlaveURL());
		if (slaveURL == null)
		{
			log.info("Skip sending jmf to slave to null DeviceID=" + getDeviceID());
			return false;
		}
		else
		{
			return sendJMF(jmf, slaveURL, mh);
		}
	}

	/**
	 *
	 * get the slave URL
	 * 
	 * @return the slave URL
	 */
	public String getSlaveURL()
	{
		return getProperties().getSlaveURL();
	}

	/**
	 * get the JMF Builder for messages to the slave device; also allow for asynch submission handling
	 * 
	 * @return
	 */
	public JMFBuilder getBuilderForSlave()
	{
		final String deviceURLForSlave = getDeviceURLForSlave();
		final JMFBuilder builder = JMFBuilderFactory.getJMFBuilder(deviceURLForSlave);
		return builder;
	}

	/**
	 * @return the preferred method for querying slave status
	 */
	public EnumSlaveStatus getSlaveStatus()
	{
		final String s = getProperties().getDeviceAttribute("SlaveStatus");
		if (s == null)
		{
			return null;
		}
		try
		{
			return EnumSlaveStatus.valueOf(s.toUpperCase());
		}
		catch (final IllegalArgumentException x)
		{
			log.error("Illegal slave status value: " + s);
			return null;
		}
	}

	/**
	 * @return the url of this proxy that the slave sends messages to
	 */
	public String getDeviceURLForSlave()
	{
		return getProperties().getDeviceURLForSlave();
	}

	/**
	 * get the correct callback assuming that all slave urls contain the string "/slavejmf"
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#getCallback(java.lang.String)
	 */
	@Override
	public IConverterCallback getCallback(final String url)
	{
		if (url != null && isSlaveURI(url))
		{
			return _slaveCallback;
		}
		return _callback;
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public boolean isSlaveURI(final String url)
	{
		if (url == null)
			return false;

		final String deviceURLForSlave = getDeviceURLForSlave();
		if (deviceURLForSlave == null)
			return false;
		final IProxyProperties proxyProperties = getProperties();
		final String slaveURL = proxyProperties.getSlaveURL();
		if (slaveURL == null)
			return false;
		return StringUtil.hasToken(url, SLAVEJMF, "/", 0) || deviceURLForSlave.contains(url) || slaveURL.contains(url);
	}

	/**
	 * @return the proxyProperties
	 * @deprecated use getProperties overrides
	 */
	@Deprecated
	public IProxyProperties getProxyProperties()
	{
		return getProperties();
	}

	/**
	 * @return the proxyProperties
	 */
	@Override
	public IProxyProperties getProperties()
	{
		return (IProxyProperties) super.getProperties();
	}

	/**
	 * add a generic catch-all buffer handler that simply proxies all messages
	 */
	@Override
	protected void addHandlers()
	{
		super.addHandlers();

		addBufferHandler(new NotificationBufferHandler(this));
		addBufferHandler(new StatusBufferHandler(this));
		addBufferHandler(new ResourceBufferHandler(this));

		addHandler(new CommandProxyHandler(AbstractProxyDevice.this, "*"), null);
	}

	protected void addBufferHandler(final JMFBufferHandler bh)
	{
		final String messageType = bh.getMessageType();
		final IMessageHandler previousQueryHandler = getJMFHandler(null).getMessageHandler(messageType, EnumFamily.Query);
		if (previousQueryHandler != null)
			bh.setFallbackHandler(previousQueryHandler);

		addHandler(bh, getDeviceURLForSlave());
		addHandler(bh, null);
	}

	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#getNodeFromDoc(org.cip4.jdflib.core.JDFDoc)
	 * @param doc
	 * @return
	 */
	@Override
	public JDFNode getNodeFromDoc(final JDFDoc doc)
	{
		return null;
	}

	/**
	 * @param request
	 */
	@Override
	protected void updateDevice(final ContainerRequest request)
	{
		super.updateDevice(request);

		final JDFAttributeMap map = request.getParameterMap();
		final Set<String> s = map == null ? null : map.keySet();
		if (s == null)
			return;

		String slave = request.getParameter("SlaveURL");
		if (slave != null && s.contains("SlaveURL"))
		{
			updateSlaveURL(slave);
		}
		slave = request.getParameter("SlaveDeviceID");
		if (slave != null && s.contains("SlaveDeviceID"))
		{
			updateSlaveDeviceID(slave);
		}
		slave = request.getParameter("MaxPush");
		if (slave != null && s.contains("MaxPush"))
		{
			updateMaxPush(slave);
		}

		String hf = request.getParameter("SlaveInputHF");
		if (hf != null && s.contains("SlaveInputHF"))
		{
			updateSlaveInputHF(hf);
		}
		hf = request.getParameter("SlaveOutputHF");
		if (hf != null && s.contains("SlaveOutputHF"))
		{
			updateSlaveOutputHF(hf);
		}
		hf = request.getParameter("SlaveErrorHF");
		if (hf != null && s.contains("SlaveErrorHF"))
		{
			updateSlaveErrorHF(hf);
		}
		if (s.contains("SlaveMIMETransferExpansion"))
		{
			final boolean expand = request.getBooleanParam("SlaveMIMETransferExpansion");
			updateSlaveMIMEExpansion(expand);
		}

	}

	/**
	 * @param newHF
	 *
	 */
	protected void updateSlaveErrorHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProperties();
		final File oldHF = properties.getSlaveErrorHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveErrorHF(newHFF);
			properties.serialize();
			reloadSlaveErrorHF(newHFF);
		}
	}

	/**
	 * @param newHF
	 *
	 */
	protected void updateSlaveOutputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProperties();
		final File oldHF = properties.getSlaveOutputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveOutputHF(newHFF);
			properties.serialize();
			reloadSlaveOutputHF(newHFF);
		}
	}

	/**
	 * @param newHF
	 *
	 */
	protected void updateSlaveInputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProperties();
		final File oldHF = properties.getSlaveInputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveInputHF(newHFF);
			properties.serialize();
			// no reloading is necessary - this hf is listned to by the slave
		}
	}

	/**
	 * @param bExtendMime
	 *
	 */
	protected void updateSlaveMIMEExpansion(final boolean bExtendMime)
	{
		final IProxyProperties properties = getProperties();
		final boolean extend = properties.getSlaveMIMEExpansion();
		if (extend != bExtendMime)
		{
			properties.setSlaveMIMEExpansion(bExtendMime);
			properties.serialize();
		}
	}

	/**
	 * @param newSlaveURL
	 *
	 */
	protected void updateSlaveURL(String newSlaveURL)
	{
		if (newSlaveURL == null)
		{
			return;
		}
		else if ("-".equals(newSlaveURL))
		{
			newSlaveURL = null;
		}
		final IProxyProperties properties = getProperties();
		final String oldSlaveURL = properties.getSlaveURL();
		if (ContainerUtil.equals(oldSlaveURL, newSlaveURL))
		{
			return;
		}
		properties.setSlaveURL(newSlaveURL);
		properties.serialize();
	}

	/**
	 * @param push
	 *
	 */
	protected void updateMaxPush(final String push)
	{
		final int iPush = StringUtil.parseInt(push, -1);
		if (iPush < 0)
		{
			return;
		}
		final IProxyProperties properties = getProperties();
		final int oldPush = properties.getMaxPush();
		if (oldPush == iPush)
		{
			return;
		}
		properties.setMaxPush(iPush);
		properties.serialize();
	}

	/**
	 * @param newSlave
	 *
	 */
	protected void updateSlaveDeviceID(final String newSlave)
	{
		if (newSlave == null)
		{
			return;
		}
		final IProxyProperties properties = getProperties();
		final String oldSlave = properties.getSlaveURL();
		if (ContainerUtil.equals(oldSlave, newSlave))
		{
			return;
		}
		properties.setSlaveDeviceID(newSlave);
		properties.serialize();
	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#getXMLDevice(boolean, org.cip4.bambi.core.ContainerRequest)
	 * @param addProcs
	 * @param request
	 * @return
	 */
	@Override
	public XMLDevice getXMLDevice(final boolean addProcs, final ContainerRequest request)
	{
		return new XMLProxyDevice(this, addProcs, request);
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#reloadQueue()
	 */
	@Override
	protected void reloadQueue()
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#canAccept(org.cip4.jdflib.node.JDFNode, java.lang.String)
	 * @param doc
	 * @param queueEntryID
	 * @return
	 */
	@Override
	public VString canAccept(final JDFNode doc, final String queueEntryID)
	{
		if (queueEntryID == null)
		{
			return new VString(getDeviceID(), null);
		}
		final AbstractProxyProcessor proc = (AbstractProxyProcessor) getProcessor(queueEntryID, 0);
		if (proc == null)
		{
			return new VString(getDeviceID(), null); // no processor is working on queueEntryID - assume ok
		}
		return proc.canAccept(doc, queueEntryID);
	}

	/**
	 * reset the device, including removing and renewing all known subscriptions
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#reset()
	 */
	@Override
	public void reset()
	{
		super.reset();
		waitingSubscribers.clear();
		addSlaveSubscriptions(1000, null, true);
		reloadSlaveErrorHF(null);
		reloadSlaveOutputHF(null);
		prepareSlaveHotfolders(getProperties());
	}

	/**
	 * @return the mySubscriptions
	 */
	public SubscriptionMap getMySubscriptions()
	{
		return mySubscriptions;
	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#addMoreToXMLSubscriptions(org.cip4.jdflib.core.KElement)
	 * @param rootDevice
	 */
	@Override
	public void addMoreToXMLSubscriptions(final KElement rootDevice)
	{
		super.addMoreToXMLSubscriptions(rootDevice);
		if (mySubscriptions != null)
			mySubscriptions.copyToXML(rootDevice);
	}

	/**
	 * @Override
	 * @see org.cip4.bambi.core.AbstractDevice#getDataURL(JDFQueueEntry, boolean)
	 */
	@Override
	public String getDataURL(final JDFQueueEntry queueEntry, final boolean bSubmit)
	{
		final IProxyProperties proxyProperties = getProperties();
		if (proxyProperties.getSlaveMIMEExpansion() && proxyProperties.isSlaveMimePackaging())
			return null;
		String deviceURL = getDeviceURL();
		deviceURL = StringUtil.replaceString(deviceURL, "jmf", "data");
		return deviceURL + ((queueEntry == null) ? "" : "/" + queueEntry.getQueueEntryID());
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#wasSubmitted(org.cip4.jdflib.jmf.JDFQueueEntry)
	 */
	@Override
	public boolean wasSubmitted(final JDFQueueEntry qe)
	{
		return super.wasSubmitted(qe) && !isSubmitting(qe);
	}

	/**
	 *
	 * @param qe
	 * @return
	 */
	public boolean isSubmitting(final JDFQueueEntry qe)
	{
		return qe == null ? false : SUBMITTING.equals(qe.getStatusDetails());
	}

	/**
	 * prepare submission in proxies by setting status details to submitting
	 * 
	 * @param newQE
	 */
	@Override
	public void prepareSubmit(final JDFQueueEntry newQE)
	{
		super.prepareSubmit(newQE);
		newQE.setStatusDetails(SUBMITTING);
	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#getJMFHandler(java.lang.String)
	 */
	@Override
	public JMFHandler getJMFHandler(final String url)
	{
		return isSlaveURI(url) ? _slaveJmfHandler : super.getJMFHandler(url);
	}

	/**
	 * get all jmf handlers
	 * 
	 * @return the _jmfHandler
	 */
	@Override
	public Vector<JMFHandler> getJMFHandlers()
	{
		final Vector<JMFHandler> v = super.getJMFHandlers();
		v.add(_slaveJmfHandler);
		return v;
	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#preSetup()
	 */
	@Override
	protected void preSetup()
	{
		super.preSetup();
		_slaveJmfHandler = new JMFHandler(this);

		if (getDeviceURLForSlave() != null)
		{
			getBuilderForSlave().setAcknowledgeURL(getDeviceURLForSlave());
		}
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#shutdownHotFolders()
	 */
	@Override
	protected void shutdownHotFolders()
	{
		if (slaveJDFError != null)
		{
			log.info("Shutting down hotfolder: " + slaveJDFError.getHfDirectory());
			slaveJDFError.stop();
		}
		if (slaveJDFOutput != null)
		{
			log.info("Shutting down hotfolder: " + slaveJDFOutput.getHfDirectory());
			slaveJDFOutput.stop();
		}
		super.shutdownHotFolders();
	}

}