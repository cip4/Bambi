/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2012 The International Cooperation for the Integration of 
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

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.messaging.JMFBufferHandler;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.proxy.MessageChecker.KnownMessageDetails;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFAudit;
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

	protected static int slaveThreadCount = 0;
	protected HashMap<String, SlaveSubscriber> waitingSubscribers;
	/**
	 * the url flag for incoming messages (end point of the path)
	 */
	public static final String SLAVEJMF = "slavejmf";

	/**
	 * watched hot folder for hf based communication with a device (completed)
	 */
	protected QueueHotFolder slaveJDFOutput;
	/**
	 * watched hot folder for hf based communication with a device (aborted)
	 */
	protected QueueHotFolder slaveJDFError;

	/**
	 * the list of pending subscriptions that have been sent  to the slave device
	 */
	protected SubscriptionMap mySubscriptions;

	/**
	 * @author Rainer Prosi, Heidelberger Druckmaschinen 
	 * 
	 * enumeration how to set up synchronization of status with the slave
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

	/**
	 * 
	 * @author Rainer Prosi, Heidelberger Druckmaschinen 
	 */
	protected class StopPersistantHandler extends MessageResponseHandler
	{
		/**
		 * @param jmf
		 */
		public StopPersistantHandler(JDFJMF jmf)
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
			log.warn("clearing my subscription list");
			mySubscriptions.clear();
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
		public KnownMessagesResponseHandler(JDFJMF jmf)
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
			JDFMessage m = getFinalMessage();
			Set<KnownMessageDetails> theSet = new HashSet<KnownMessageDetails>();
			VElement v = m == null ? null : m.getChildElementVector(ElementName.MESSAGESERVICE, null);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					JDFMessageService ms = (JDFMessageService) v.get(i);
					KnownMessageDetails processedMessageService = processMessageService(ms);
					if (processedMessageService != null)
						theSet.add(processedMessageService);
				}
			}
			return theSet;
		}

		/**
		 * process an individual existing messageservice and update the cache appropriately
		 * @param ms
		 * @return 
		 */
		private KnownMessageDetails processMessageService(JDFMessageService ms)
		{
			KnownMessageDetails knownMessageDetails = new KnownMessageDetails(ms);
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
		public KnownSubscriptionsHandler(JDFJMF jmf, Vector<JDFJMF> createSubscriptions)
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
			JDFMessage m = getFinalMessage();
			Vector<JDFJMF> vjmf = new Vector<JDFJMF>();
			VElement v = m == null ? null : m.getChildElementVector(ElementName.SUBSCRIPTIONINFO, null);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					JDFSubscriptionInfo si = (JDFSubscriptionInfo) v.get(i);
					processSubscription(si);
				}
			}
			vjmf.addAll(sendjmfs);
			return vjmf;
		}

		/**
		 * process an individual existing subscription and update the cache appropriately
		 * @param si
		 */
		private void processSubscription(JDFSubscriptionInfo si)
		{
			String channelID = StringUtil.getNonEmpty(si.getChannelID());
			if (channelID == null)
			{
				log.warn("SubscriptionInfo without channelID, ignore");
				return;
			}
			JDFSubscription subscription = si.getSubscription();
			String url = subscription == null ? null : subscription.getURL();
			String deviceURLForSlave = getProperties().getDeviceURLForSlave();
			if (!ContainerUtil.equals(url, deviceURLForSlave))
			{
				log.warn("SubscriptionInfo for wrong url:" + deviceURLForSlave + ", ignore");
				return;
			}
			EnumType siType = si.getEnumType();
			for (JDFJMF jdfjmf : sendjmfs)
			{
				EnumType typ = jdfjmf.getQuery(0).getEnumType();
				if (ContainerUtil.equals(typ, siType))
				{
					// may be null at startup - ignore counting
					ProxySubscription oldSub = mySubscriptions == null ? null : mySubscriptions.get(typ);
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
				//TODO System.out.print(resp);
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
		public QueueSynchronizeHandler(JDFJMF jmf)
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
									if (!ContainerUtil.equals(status, qe.getQueueEntryStatus()))
									{
										queueProcessor.updateEntry(qe, status, null, null);
										if (EnumQueueEntryStatus.Completed.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Completed);
										}
										else if (EnumQueueEntryStatus.Aborted.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Aborted);
										}
										else if (EnumQueueEntryStatus.Suspended.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Suspended);
										}
									}
								}
								else
								{
									log.info("Slave queueentry " + slaveQEID + " was removed");
									queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Removed, null, null);
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
	 * @author prosirai
	 */
	protected class XMLProxyDevice extends XMLDevice
	{

		/**
		 * XML representation of this simDevice fore use as html display using an XSLT
		 * @param addProcs - always ignored
		 * @param request BambiServletRequest http context in which this is called
		 */
		public XMLProxyDevice(final boolean addProcs, final ContainerRequest request)
		{
			// proxies never show processors
			super(false, request);
			updateSlaveProperties();
		}

		/**
		 * 
		 */
		private void updateSlaveProperties()
		{
			final KElement deviceRoot = getRoot();
			final IProxyProperties proxyProperties = getProperties();
			if (proxyProperties != null)
			{
				deviceRoot.setAttribute("SlaveURL", proxyProperties.getSlaveURL());
				deviceRoot.setAttribute("MaxPush", proxyProperties.getMaxPush(), null);
				deviceRoot.setAttribute("DeviceURLForSlave", proxyProperties.getDeviceURLForSlave());

				File hf = proxyProperties.getSlaveInputHF();
				deviceRoot.setAttribute("SlaveInputHF", hf == null ? null : hf.getPath());
				hf = proxyProperties.getSlaveOutputHF();
				deviceRoot.setAttribute("SlaveOutputHF", hf == null ? null : hf.getPath());
				hf = proxyProperties.getSlaveErrorHF();
				deviceRoot.setAttribute("SlaveErrorHF", hf == null ? null : hf.getPath());
				final String id = proxyProperties.getSlaveDeviceID();
				deviceRoot.setAttribute("SlaveDeviceID", id == null ? null : id);
				deviceRoot.setAttribute("SlaveMIMETransferExpansion", proxyProperties.getSlaveMIMEExpansion(), null);
				deviceRoot.setAttribute("SlaveMIMETransferEncoding", proxyProperties.getSlaveMIMEEncoding());
				deviceRoot.setAttribute("SlaveMIMESemicolon", proxyProperties.getSlaveMIMESemicolon(), null);
			}
			deviceRoot.setAttribute("DataURL", getDataURL(null, true));
		}
	}

	/**
	 * @param properties properties with device details
	 */
	public AbstractProxyDevice(final IDeviceProperties properties)
	{
		super(properties);
	}

	/**
	 * prepare output and error hot folders if they are specified
	 * @param proxyProperties
	 */
	private void prepareSlaveHotfolders(final IProxyProperties proxyProperties)
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
	 * @param fDeviceErrorOutput
	 */
	private void reloadSlaveErrorHF(final File fDeviceErrorOutput)
	{
		if (slaveJDFError != null)
		{
			slaveJDFError.stop();
			slaveJDFError = null;
		}
		final File hfStorage = new File(getProperties().getBaseDir() + File.separator + "HFDevTmpStorage" + File.separator + getDeviceID());
		log.info("Device error output HF:" + fDeviceErrorOutput.getPath() + " device ID= " + getSlaveDeviceID());
		final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
		slaveJDFError = new QueueHotFolder(fDeviceErrorOutput, hfStorage, null, new ReturnHFListner(this, EnumQueueEntryStatus.Aborted), rqCommand);
	}

	/**
	 * @param fDeviceJDFOutput
	 */
	private void reloadSlaveOutputHF(final File fDeviceJDFOutput)
	{
		if (slaveJDFOutput != null)
		{
			slaveJDFOutput.stop();
			slaveJDFOutput = null;
		}
		final File hfStorage = new File(getDeviceDir() + File.separator + "HFDevTmpStorage");
		log.info("Device output HF:" + fDeviceJDFOutput.getPath() + " device ID= " + getSlaveDeviceID());
		final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
		slaveJDFOutput = new QueueHotFolder(fDeviceJDFOutput, hfStorage, null, new ReturnHFListner(this, EnumQueueEntryStatus.Completed), rqCommand);
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
		_slaveCallback = proxyProperties.getSlaveCallBackClass();
		waitingSubscribers = new HashMap<String, SlaveSubscriber>();
		mySubscriptions = new SubscriptionMap();
		prepareSlaveHotfolders(proxyProperties);

		super.init();

		_jmfHandler.setFilterOnDeviceID(false);
		_theSignalDispatcher.setIgnoreURL(getDeviceURLForSlave());
		// ensure existence of vector prior to filling
		_theQueueProcessor.getQueue().resumeQueue(); // proxy queues should start up by default
		addSlaveSubscriptions(8888, null, false);
	}

	/**
	 * @param waitMillis
	 * @param slaveQEID 
	 * @param reset if true remove all existing subscriptions
	 */
	public void addSlaveSubscriptions(int waitMillis, String slaveQEID, boolean reset)
	{
		final EnumSlaveStatus slaveStatus = getSlaveStatus();
		if (!EnumSlaveStatus.JMFGLOBAL.equals(slaveStatus) && !EnumSlaveStatus.JMF.equals(slaveStatus))
			return;
		if (EnumSlaveStatus.JMFGLOBAL.equals(slaveStatus))
			slaveQEID = null;

		SlaveSubscriber slaveSubscriber = getSlaveSubscriber(waitMillis, slaveQEID);
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
	protected final SlaveSubscriber getSlaveSubscriber(final int waitMillis, String slaveQEID)
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
		String key = getKey(slaveQEID);
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
	protected String getKey(String slaveQEID)
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
		mySubscriptions.shutdown(this);
		super.shutdown();
		if (slaveJDFError != null)
		{
			slaveJDFError.stop();
		}
		if (slaveJDFOutput != null)
		{
			slaveJDFOutput.stop();
		}
	}

	/**
	 * sends messages to the slave to stop processing
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
	 * @param jmf the jmf to send
	 * @param mh the message response handler, may be null
	 * @return true if successfully queues @see sendJMF
	 */
	protected boolean sendJMFToSlave(final JDFJMF jmf, final MessageResponseHandler mh)
	{
		if (jmf == null)
		{
			log.warn("Skip sending null jmf to slave.");
			return false;
		}
		String slaveURL = StringUtil.getNonEmpty(getSlaveURL());
		if (slaveURL == null)
		{
			log.info("Skip sending jmf to slave to null DeviceID=" + getDeviceID());
			return false;
		}
		else
		{
			log.info("Sending jmf to: " + slaveURL);
			return sendJMF(jmf, slaveURL, mh);
		}
	}

	/**
	 * 
	 * get the slave URL
	 * @return the slave URL
	 */
	public String getSlaveURL()
	{
		return getProperties().getSlaveURL();
	}

	/**
	 * get the JMF Builder for messages to the slave device; also allow for asynch submission handling
	 * @return
	 */
	public JMFBuilder getBuilderForSlave()
	{
		JMFBuilder builder = JMFBuilderFactory.getJMFBuilder(getDeviceID());
		builder = builder.clone(); // we only want to set ackURL for certain messages
		final String deviceURLForSlave = getProperties().getDeviceURLForSlave();
		if (deviceURLForSlave != null)
		{
			builder.setAcknowledgeURL(deviceURLForSlave);
		}
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
		catch (IllegalArgumentException x)
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
	 * @see org.cip4.bambi.core.AbstractDevice#getCallback(java.lang.String)
	 */
	@Override
	public IConverterCallback getCallback(final String url)
	{
		IProxyProperties proxyProperties = getProperties();
		if (url != null)
		{
			if (StringUtil.hasToken(url, SLAVEJMF, "/", 0) || url.equals(proxyProperties.getDeviceURLForSlave()) || url.equals(proxyProperties.getSlaveURL()))
			{
				return _slaveCallback;
			}
		}
		return _callback;
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
		addHandler(new JMFBufferHandler(AbstractProxyDevice.this, null, new EnumFamily[] { EnumFamily.Signal }, this));
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
			boolean expand = request.getBooleanParam("SlaveMIMETransferExpansion");
			updateSlaveMIMEExpansion(expand);
		}

	}

	/**
	 * @param newHF 
	 * 
	 */
	private void updateSlaveErrorHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProperties();
		final File oldHF = properties.getSlaveErrorHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveErrorHF(newHFF);
			properties.serialize();
		}
	}

	/**
	 * @param newHF 
	 * 
	 */
	private void updateSlaveOutputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProperties();
		final File oldHF = properties.getSlaveOutputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveOutputHF(newHFF);
			properties.serialize();
		}
	}

	/**
	 * @param newHF 
	 * 
	 */
	private void updateSlaveInputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProperties();
		final File oldHF = properties.getSlaveInputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveInputHF(newHFF);
			properties.serialize();
		}
	}

	/**
	 * @param bExtendMime 
	 * 
	 */
	private void updateSlaveMIMEExpansion(boolean bExtendMime)
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
	 * overwrite to provide your favorite version string
	 * @return
	 */
	@Override
	public String getVersionString()
	{
		return "Generic Bambi Proxy Device " + JDFAudit.software();
	}

	/**
	 * @param newSlaveURL 
	 * 
	 */
	protected void updateSlaveURL(final String newSlaveURL)
	{
		if (newSlaveURL == null)
		{
			return;
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
	private void updateMaxPush(final String push)
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
	private void updateSlaveDeviceID(final String newSlave)
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
		return new XMLProxyDevice(addProcs, request);
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
	 * @see org.cip4.bambi.core.AbstractDevice#reset()
	*/
	@Override
	public void reset()
	{
		super.reset();
		waitingSubscribers.clear();
		addSlaveSubscriptions(1000, null, true);
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
	public void addMoreToXMLSubscriptions(KElement rootDevice)
	{
		super.addMoreToXMLSubscriptions(rootDevice);
		mySubscriptions.copyToXML(rootDevice);
	}

	/**
	 * 	@Override
	 * @see org.cip4.bambi.core.AbstractDevice#getDataURL(JDFQueueEntry, boolean)
	 */
	@Override
	public String getDataURL(JDFQueueEntry queueEntry, boolean bSubmit)
	{
		IProxyProperties proxyProperties = getProperties();
		if (proxyProperties.getSlaveMIMEExpansion() && proxyProperties.isSlaveMimePackaging())
			return null;
		String deviceURL = getDeviceURL();
		deviceURL = StringUtil.replaceString(deviceURL, "jmf", "data");
		return deviceURL + ((queueEntry == null) ? "" : "/" + queueEntry.getQueueEntryID());
	}

}