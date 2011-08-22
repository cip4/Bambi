/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2011 The International Cooperation for the Integration of 
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.messaging.JMFBufferHandler;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.proxy.MessageChecker.KnownMessageDetails;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFMessageService;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFSubscriptionInfo;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.hotfolder.QueueHotFolder;
import org.cip4.jdflib.util.hotfolder.QueueHotFolderListener;

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
		NODEINFO
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
		 * @param jmfs 
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
			String deviceURLForSlave = getProxyProperties().getDeviceURLForSlave();
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
	 * class to manage subscriptions to the slave device
	  * @author Rainer Prosi, Heidelberger Druckmaschinen *
	 */
	protected class ProxySubscription
	{
		long lastReceived;
		long created;
		int numReceived;
		String channelID;
		String url;
		JDFJMF subscribedJMF;
		String type;

		/**
		 * 
		 * @param jmf
		 * @throws IllegalArgumentException
		 */
		public ProxySubscription(JDFJMF jmf) throws IllegalArgumentException
		{
			subscribedJMF = (JDFJMF) jmf.clone();
			type = subscribedJMF.getMessageElement(null, null, 0).getType();
			channelID = StringUtil.getNonEmpty(jmf.getQuery(0).getID());
			if (channelID == null)
			{
				getLog().error("Subscription with no channelID");
				throw new IllegalArgumentException("Subscription with no channelID");
			}
			lastReceived = 0;
			numReceived = StringUtil.parseInt(BambiNSExtension.getMyNSAttribute(jmf, "numReceived"), 0);
			created = StringUtil.parseLong(BambiNSExtension.getMyNSAttribute(jmf, AttributeName.CREATIONDATE), System.currentTimeMillis());
			url = BambiNSExtension.getMyNSAttribute(jmf, AttributeName.URL);
		}

		/**
		 * @param channelID
		 */
		public void setChannelID(String channelID)
		{
			if (this.channelID.equals(channelID))
				return; //nop

			getLog().info("updating proxy subscription channelID to: " + channelID);
			this.channelID = channelID;
			subscribedJMF.getMessageElement(null, null, 0).setID(channelID);
		}

		/**
		 * @see java.lang.Object#toString()
		 * @return
		*/
		@Override
		public String toString()
		{
			return "ProxySubscription: " + subscribedJMF;
		}

		/**
		 * 
		 */
		public void incrementHandled()
		{
			lastReceived = System.currentTimeMillis();
			numReceived++;
		}

		/**
		 * @param subs
		 */
		public void copyToXML(KElement subs)
		{
			subs = subs.appendElement("ProxySubscription");
			subs.copyElement(subscribedJMF, null);
			subs.setAttribute(AttributeName.CHANNELID, channelID);
			subs.setAttribute(AttributeName.URL, url);
			subs.setAttribute(AttributeName.TYPE, subscribedJMF.getMessageElement(null, null, 0).getType());
			subs.setAttribute(AttributeName.CREATIONDATE, XMLResponse.formatLong(created));
			subs.setAttribute("LastReceived", XMLResponse.formatLong(lastReceived));
			subs.setAttribute("NumReceived", StringUtil.formatInteger(numReceived));
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

	protected class ReturnHFListner implements QueueHotFolderListener
	{
		private final EnumQueueEntryStatus hfStatus;

		/**
		 * @param status
		 */
		public ReturnHFListner(final EnumQueueEntryStatus status)
		{
			hfStatus = status;
		}

		public boolean submitted(final JDFJMF submissionJMF)
		{
			getLog().info("ReturnHFListner:submitted");
			final JDFCommand command = submissionJMF.getCommand(0);
			final JDFReturnQueueEntryParams rqp = command.getReturnQueueEntryParams(0);

			final JDFDoc doc = rqp == null ? null : rqp.getURLDoc();
			if (doc == null || rqp == null)
			{
				getLog().warn("could not process JDF File");
				return false;
			}
			if (getJMFHandler() != null)
			{
				final KElement n = doc.getRoot();
				if (n == null)
				{
					getLog().warn("could not process JDF File");
					return false;
				}

				// assume the rootDev was the executed baby...
				rqp.setAttribute(hfStatus.getName(), n.getAttribute(AttributeName.ID));
				// let the standard returnqe handler do the work
				final JDFDoc responseJMF = getJMFHandler().processJMF(submissionJMF.getOwnerDocument_JDFElement());
				try
				{
					final JDFJMF jmf = responseJMF.getJMFRoot();
					final JDFResponse r = jmf.getResponse(0);
					if (r != null && r.getReturnCode() == 0)
					{
						final File urlToFile = UrlUtil.urlToFile(rqp.getURL());
						boolean byebye = false;
						if (urlToFile != null)
						{
							byebye = urlToFile.delete();
						}
						if (!byebye)
						{
							getLog().error("could not delete JDF File: " + urlToFile);
						}
					}
					else
					{
						getLog().error("could not process JDF File");
					}
				}
				catch (final Exception e)
				{
					handleError(submissionJMF);
					return false;
				}
			}
			return true;
		}

		/**
		 * @param submissionJMF
		 */
		private void handleError(final JDFJMF submissionJMF)
		{
			getLog().error("error handling hf return");
		}
	}

	/**
	  * @author Rainer Prosi, Heidelberger Druckmaschinen *
	 */
	public class SubscriptionMap extends HashMap<EnumType, ProxySubscription>
	{

		/**
		 * 
		 */
		protected SubscriptionMap()
		{
			super();
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param refID
		 */
		public void incrementHandled(String refID)
		{
			ProxySubscription ps = getSubscription(refID);
			if (ps != null)
				ps.incrementHandled();

		}

		/**
		 * @param refID the refID or type of the message
		 * @return
		 */
		private ProxySubscription getSubscription(String refID)
		{
			if (refID == null)
				return null;

			Collection<ProxySubscription> v = values();
			Iterator<ProxySubscription> it = v.iterator();
			while (it.hasNext())
			{
				ProxySubscription ps = it.next();
				if (refID.equals(ps.channelID) || refID.equals(ps.type))
					return ps;
			}
			return null;
		}

		/**
		 * @param deviceRoot
		 */
		protected void copyToXML(KElement deviceRoot)
		{
			Collection<ProxySubscription> v = values();
			Iterator<ProxySubscription> it = v.iterator();
			KElement subs = deviceRoot.appendElement("ProxySubscriptions");
			while (it.hasNext())
				it.next().copyToXML(subs);
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
			final IProxyProperties proxyProperties = getProxyProperties();
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
		final File hfStorage = new File(_devProperties.getBaseDir() + File.separator + "HFDevTmpStorage" + File.separator + _devProperties.getDeviceID());
		log.info("Device error output HF:" + fDeviceErrorOutput.getPath() + " device ID= " + getSlaveDeviceID());
		final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
		slaveJDFError = new QueueHotFolder(fDeviceErrorOutput, hfStorage, null, new ReturnHFListner(EnumQueueEntryStatus.Aborted), rqCommand);
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
		slaveJDFOutput = new QueueHotFolder(fDeviceJDFOutput, hfStorage, null, new ReturnHFListner(EnumQueueEntryStatus.Completed), rqCommand);
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#init()
	 */
	@Override
	protected void init()
	{
		knownSlaveMessages = new MessageChecker();
		final IProxyProperties proxyProperties = getProxyProperties();
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

		String key = getKey(slaveQEID);
		synchronized (waitingSubscribers)
		{
			SlaveSubscriber slaveSubscriber = waitingSubscribers.get(key);
			if (slaveSubscriber != null)
				return null;
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
		return new SlaveSubscriber(waitMillis, slaveQEID);
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
	 * class to asynchronously subscribe to messages at the slaves
	 * @author Rainer Prosi, Heidelberger Druckmaschinen *
	 */
	protected class SlaveSubscriber extends Thread
	{
		/**
		 * @param b
		 */
		public void setReset(final boolean b)
		{
			reset = b;
		}

		/**
		 * @param waitBefore the time to wait prior to subscribing
		 * @param slaveQEID
		 */
		protected SlaveSubscriber(final int waitBefore, final String slaveQEID)
		{
			super("SlaveSubscriber_" + getDeviceID() + "_" + slaveThreadCount++);
			this.waitBefore = waitBefore;
			this.slaveQEID = slaveQEID;
			reset = false;
			waitingSubscribers.put(getKey(slaveQEID), this);
		}

		private final int waitBefore;
		private final String slaveQEID;
		private boolean reset;

		/**
		 * add global subscriptions at startup
		 */
		@Override
		public void run()
		{
			final String slaveURL = getProxyProperties().getSlaveURL();
			getLog().info("Updating global subscriptions to :" + slaveURL);
			final String deviceURL = getDeviceURLForSlave();
			if (deviceURL == null)
			{
				getLog().error("Device feedback url is not specified in subscription, proxy device " + getDeviceID() + " is not subscribing at slave device: " + getSlaveDeviceID());
				return;
			}

			ThreadUtil.sleep(waitBefore); // wait for other devices to start prior to subscribing
			if (!knownSlaveMessages.isInitialized())
				updateKnownMessages();
			final Vector<JDFJMF> vJMFS = prepare(deviceURL);

			// reduce currently known subscriptions
			sendSubscriptions(vJMFS);

			cleanup();
		}

		/**
		 * update the knownmessages list 
		 */
		private void updateKnownMessages()
		{
			final JMFBuilder builder = getBuilderForSlave();
			final JDFJMF knownMessages = builder.buildKnownMessagesQuery();
			KnownMessagesResponseHandler handler = new KnownMessagesResponseHandler(knownMessages);
			sendJMFToSlave(knownMessages, handler);
			handler.waitHandled(20000, 30000, true);
			knownSlaveMessages.setMessages(handler.completeHandling());
		}

		/**
		 * @param deviceURL
		 * @return
		 */
		private Vector<JDFJMF> prepare(final String deviceURL)
		{
			resetSubscriptions();
			final JMFBuilder builder = getBuilderForSlave();
			final JDFJMF knownSubscriptions = builder.buildKnownSubscriptionsQuery(deviceURL, slaveQEID);
			final Vector<JDFJMF> createSubscriptions = createSubscriptions(deviceURL);
			if (createSubscriptions != null)
			{
				// remove duplicates
				if (knownSlaveMessages.knows(EnumType.KnownSubscriptions))
				{
					final KnownSubscriptionsHandler handler = new KnownSubscriptionsHandler(knownSubscriptions, createSubscriptions);
					sendJMFToSlave(knownSubscriptions, handler);
					handler.waitHandled(20000, 30000, true);
					return handler.completeHandling();
				}
				else
				{
					return createSubscriptions;
				}
			}
			else
			{
				return null;
			}
		}

		/**
		 * @param vJMFS
		 */
		private void sendSubscriptions(final Vector<JDFJMF> vJMFS)
		{
			if (vJMFS != null)
			{
				for (JDFJMF jmf : vJMFS)
				{
					createNewSubscription(jmf);
				}
			}
		}

		/**
		 * 
		 */
		private void cleanup()
		{
			ThreadUtil.sleep(30000);
			synchronized (waitingSubscribers)
			{
				waitingSubscribers.remove(getKey(slaveQEID));
			}
		}

		/**
		 * 
		 */
		private void resetSubscriptions()
		{
			if (knownSlaveMessages.knows(EnumType.StopPersistentChannel))
			{
				if (reset)
				{
					final JMFBuilder builder = getBuilderForSlave();
					final JDFJMF stopPersistant = builder.buildStopPersistentChannel(null, null, getDeviceURLForSlave());
					final MessageResponseHandler waitHandler = new StopPersistantHandler(stopPersistant);
					sendJMFToSlave(stopPersistant, waitHandler);
					waitHandler.waitHandled(10000, 30000, true);
				}
			}
		}

		/**
		 * @param deviceURL
		 * 
		 * @return
		 */
		protected Vector<JDFJMF> createSubscriptions(final String deviceURL)
		{
			final JMFBuilder builder = getBuilderForSlave();
			final JDFJMF[] createSubscriptions = builder.createSubscriptions(deviceURL, slaveQEID, 10, 0);
			Vector<JDFJMF> vRet = ContainerUtil.toVector(createSubscriptions);
			vRet = removeUnknown(vRet);

			return vRet.size() > 0 ? vRet : null;
		}

		protected Vector<JDFJMF> removeUnknown(Vector<JDFJMF> vRet)
		{
			if (vRet == null)
				return vRet;
			for (int i = vRet.size() - 1; i >= 0; i--)
			{
				JDFJMF jmf = vRet.elementAt(i);
				JDFMessage m = jmf.getMessageElement(null, null, 0);
				if (!knownSlaveMessages.knows(m.getType()))
					vRet.remove(jmf);
			}
			return vRet;
		}

		/**
		 * @param jmf the subscribable jmf
		 */
		private void createNewSubscription(final JDFJMF jmf)
		{
			if (jmf == null)
			{
				return;
			}
			final ProxySubscription ps = new ProxySubscription(jmf);
			final EnumType t = jmf.getQuery(0).getEnumType();
			final ProxySubscription psOld = mySubscriptions.get(t);
			if (psOld != null)
			{
				getLog().warn("updating dropped subscription; type: " + t.getName());
			}
			final MessageResponseHandler rh = new MessageResponseHandler(jmf);
			sendJMFToSlave(jmf, rh);
			rh.waitHandled(10000, 30000, false);
			final int rc = rh.getJMFReturnCode();
			if (rc == 0)
			{
				mySubscriptions.remove(t);
				mySubscriptions.put(t, ps);
			}
			else
			{
				getLog().warn("error updating subscription; type: " + t.getName() + " rc=" + rc);
			}
		}

		/**
		 * 
		 * @see java.lang.Thread#toString()
		 */
		@Override
		public String toString()
		{
			return "SlaveSubscriber: qeid=" + slaveQEID + " " + knownSlaveMessages;
		}
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
		return getProxyProperties().getSlaveDeviceID();
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#shutdown()
	*/
	@Override
	public void shutdown()
	{
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
			return false;
		String slaveURL = getProxyProperties().getSlaveURL();
		log.info("Sending jmf to: " + slaveURL);
		return sendJMF(jmf, slaveURL, mh);
	}

	/**
	 * get the JMF Builder for messages to the slave device; also allow for asynch submission handling
	 * @return
	 */
	protected JMFBuilder getBuilderForSlave()
	{
		final JMFBuilder builder = new JMFBuilder();
		final String deviceURLForSlave = getProxyProperties().getDeviceURLForSlave();
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
		return EnumSlaveStatus.valueOf(s.toUpperCase());
	}

	/**
	 * @return the url of this proxy that the slave sends messages to
	 */
	public String getDeviceURLForSlave()
	{
		return getProxyProperties().getDeviceURLForSlave();
	}

	/**
	 * get the correct callback assuming that all slave urls contain the string "/slavejmf"
	 * @see org.cip4.bambi.core.AbstractDevice#getCallback(java.lang.String)
	 */
	@Override
	public IConverterCallback getCallback(final String url)
	{
		IProxyProperties proxyProperties = getProxyProperties();
		if (StringUtil.hasToken(url, SLAVEJMF, "/", 0) || ContainerUtil.equals(proxyProperties.getDeviceURLForSlave(), url)
				|| ContainerUtil.equals(proxyProperties.getSlaveURL(), url))
		{
			return _slaveCallback;
		}
		return _callback;
	}

	/**
	 * @return the proxyProperties
	 */
	public IProxyProperties getProxyProperties()
	{
		return (IProxyProperties) _devProperties;
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
		final IProxyProperties properties = getProxyProperties();
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
		final IProxyProperties properties = getProxyProperties();
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
		final IProxyProperties properties = getProxyProperties();
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
		final IProxyProperties properties = getProxyProperties();
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
	protected void updateSlaveURL(final String newSlaveURL)
	{
		if (newSlaveURL == null)
		{
			return;
		}
		final IProxyProperties properties = getProxyProperties();
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
		final IProxyProperties properties = getProxyProperties();
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
		final IProxyProperties properties = getProxyProperties();
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
	 * @see org.cip4.bambi.core.AbstractDevice#getDataURL(java.lang.String, boolean)
	 */
	@Override
	public String getDataURL(JDFQueueEntry queueEntry, boolean bSubmit)
	{
		IProxyProperties proxyProperties = getProxyProperties();
		if (proxyProperties.getSlaveMIMEExpansion() && proxyProperties.isSlaveMimePackaging())
			return null;
		String deviceURL = getDeviceURL();
		deviceURL = StringUtil.replaceString(deviceURL, "jmf", "data");
		return deviceURL + ((queueEntry == null) ? "" : "/" + queueEntry.getQueueEntryID());
	}

}