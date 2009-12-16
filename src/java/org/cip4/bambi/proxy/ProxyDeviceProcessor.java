/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.NotificationHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.proxy.AbstractProxyDevice.EnumSlaveStatus;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResourceInfo;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.resource.JDFEvent;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * 16.10.2008
 */
public class ProxyDeviceProcessor extends AbstractProxyProcessor
{
	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#isActive()
	 * @return true if this processor has not yet received a stop message
	 */
	@Override
	public boolean isActive()
	{
		if (stopTime == 0)
		{
			return !_doShutdown;
		}
		// TODO clean up orphans
		return false;
	}

	private static final long serialVersionUID = -384123582645081254L;
	private final NotificationQueryHandler notificationQueryHandler;
	protected long stopTime = 0; // this is the stop-processing time 0 means I'm alive

	protected class StatusSignalHandler
	{
		/**
		 * @param m
		 * @param resp
		 * @return
		 */
		protected boolean handleSignal(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || currentQE == null)
			{
				return false;
			}
			final JDFStatusQuParams sqp = m.getStatusQuParams();
			final String qeid = sqp == null ? null : sqp.getQueueEntryID();
			NodeIdentifier ni = sqp == null ? null : sqp.getIdentifier();
			// final String qeid = getQEIDFromMessage(m);
			// NodeIdentifier ni = getNIFromMessage(m);
			if (sqp != null)
			{
				final boolean matches = KElement.isWildCard(qeid) || ContainerUtil.equals(getSlaveQEID(), qeid);
				if (!matches)
				{
					return false;
				}
				ni = sqp.getIdentifier();
			}
			final VElement vMatch = currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
			if (vMatch == null)
			{
				return false;
			}
			final VElement deviceInfos = m.getChildElementVector(ElementName.DEVICEINFO, null);
			if (deviceInfos == null)
			{
				return false;
			}
			boolean b = false;
			for (int i = 0; i < deviceInfos.size(); i++)
			{
				b = handleDeviceInfo((JDFDeviceInfo) deviceInfos.get(i)) || b;
			}

			return b;
		}

		/**
		 * @param m
		 * @return
		 */
		private String getQEIDFromMessage(final JDFMessage m)
		{
			final JDFStatusQuParams sqp = m.getStatusQuParams();
			String qeid = sqp == null ? null : sqp.getQueueEntryID();
			if (qeid == null)
			{
				for (int i = 0; true; i++)
				{
					final JDFDeviceInfo di = m.getDeviceInfo(i);
					if (di == null)
					{
						break;
					}
					for (int j = 0; true; j++)
					{
						final JDFJobPhase jp = di.getJobPhase(0);
						if (jp == null)
						{
							break;
						}
						qeid = StringUtil.getNonEmpty(jp.getQueueEntryID());
						if (qeid != null)
						{
							break; // heureka
						}

					}
				}
			}
			return qeid;
		}

		/**
		 * @param sqp
		 * @return
		 */
		private NodeIdentifier getNIFromMessage(final JDFMessage m)
		{
			final JDFStatusQuParams sqp = m.getStatusQuParams();
			NodeIdentifier ni = sqp == null ? null : sqp.getIdentifier();
			if (ni == null)
			{
				for (int i = 0; true; i++)
				{
					final JDFDeviceInfo di = m.getDeviceInfo(i);
					if (di == null)
					{
						break;
					}
					for (int j = 0; true; j++)
					{
						final JDFJobPhase jp = di.getJobPhase(0);
						if (jp == null)
						{
							break;
						}
						ni = jp.getIdentifier();
						if (ni != null)
						{
							break; // heureka
						}
					}
				}
			}
			return ni;
		}

		/**
		 * @param info
		 * @return true if handled
		 */
		private boolean handleDeviceInfo(final JDFDeviceInfo info)
		{
			final String slaveDevice = getSlaveDeviceID();
			if (!info.getDeviceID().equals(slaveDevice))
			{
				return false;
			}
			final VElement jobPhases = info.getChildElementVector(ElementName.JOBPHASE, null);
			if (jobPhases == null)
			{
				return false;
			}
			boolean b = false;
			for (int i = 0; i < jobPhases.size(); i++)
			{
				b = handleStatusUpdate((JDFJobPhase) jobPhases.get(i)) || b;
			}

			if (b)// only handle counters if anything is processed by us
			{
				final StatusCounter statusCounter = getStatusListener().getStatusCounter();
				if (info.hasAttribute(AttributeName.PRODUCTIONCOUNTER))
				{
					final double pc = info.getProductionCounter();
					statusCounter.setCurrentCounter(pc);
				}
				if (info.hasAttribute(AttributeName.TOTALPRODUCTIONCOUNTER))
				{
					final double pc = info.getTotalProductionCounter();
					statusCounter.setTotalCounter(pc);
				}
			}

			return b;
		}

		/**
		 * updates the current status based on the data in the input status signal or response
		 * @param jobPhase the jobphase containing the status information
		 * @return true if handled
		 */
		private boolean handleStatusUpdate(final JDFJobPhase jobPhase)
		{
			final NodeIdentifier ni = jobPhase.getIdentifier();
			final JDFNode n = currentQE.getJDF().getJDFRoot();
			final VElement v = n.getMatchingNodes(ni);

			if (v == null)
			{
				return false;
			}

			applyPhase(jobPhase);
			_queueProcessor.updateEntry(getQueueEntry(), jobPhase.getQueueEntryStatus(), null, null);
			return true;

		}
	}

	protected class NotificationSignalHandler
	{
		/**
		 * @param m
		 * @param resp
		 * @return true if handled
		 */
		protected boolean handleSignal(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || currentQE == null || !(m instanceof JDFSignal))
			{
				return false;
			}

			final JDFSignal s = (JDFSignal) m;
			final JDFNotification n = s.getNotification();
			final NodeIdentifier ni = n.getIdentifier();
			final VElement vMatch = currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
			if (vMatch == null)
			{
				return false;
			}
			final String notifType = n.getType();
			boolean b = false;
			if (ElementName.EVENT.equals(notifType))
			{
				b = handleEvent(n, resp);
			}

			return b;
		}

		/**
		 * @param n
		 * @return true if handled
		 */
		private boolean handleEvent(final JDFNotification n, final JDFResponse resp)
		{
			final JDFEvent e = n.getEvent();
			if (e == null)
			{
				JMFHandler.errorResponse(resp, "missing event in Event signal", 1, EnumClass.Error);
			}
			else
			{
				_statusListener.setEvent(e.getEventID(), e.getEventValue(), n.getCommentText());
			}
			return true;
		}
	}

	/**
	 * buffered notification handler that distributes notifications
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected class NotificationQueryHandler extends NotificationHandler
	{
		/**
		 * 
		 */
		public NotificationQueryHandler()
		{
			super(getParent().getSignalDispatcher(), _statusListener, getParent().getQueueProcessor());
			families = new EnumFamily[] { EnumFamily.Query };
		}
	}

	// ///////////////////////////////////////////////////////////

	protected class ResourceSignalHandler
	{
		/**
		 * @param m
		 * @param resp
		 * @return true if handled
		 */
		protected boolean handleSignal(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || currentQE == null)
			{
				return false;
			}

			final JDFResourceQuParams rqp = m.getResourceQuParams();
			NodeIdentifier ni = null;
			if (rqp != null)
			{
				final String qeid = rqp.getQueueEntryID();
				final boolean matches = KElement.isWildCard(qeid) || ContainerUtil.equals(getSlaveQEID(), qeid);
				if (!matches)
				{
					return false;
				}
				ni = rqp.getIdentifier();
			}
			final VElement vMatch = currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
			if (vMatch == null)
			{
				return false;
			}
			final VElement resourceInfos = m.getChildElementVector(ElementName.RESOURCEINFO, null);
			if (resourceInfos == null)
			{
				return false;
			}
			boolean b = false;
			for (int i = 0; i < resourceInfos.size(); i++)
			{
				b = handleResourceInfo((JDFResourceInfo) resourceInfos.get(i)) || b;
			}

			return b;
		}

		/**
		 * @param info
		 * @param match
		 */
		private boolean handleResourceInfo(final JDFResourceInfo info)
		{
			final String id = info.getResourceID();
			final VJDFAttributeMap map = info.getPartMapVector();
			VJDFAttributeMap map2 = new VJDFAttributeMap(map);
			map2.put(AttributeName.CONDITION, "Waste");
			double amount = info.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, map2);
			if (amount > 0)
			{
				_statusListener.updateTotal(id, amount, true);
			}

			map2 = new VJDFAttributeMap(map);
			map2.put(AttributeName.CONDITION, "Good");
			amount = info.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, map2);
			if (amount > 0)
			{
				_statusListener.updateTotal(id, amount, false);
			}

			return true;
		}
	}

	/**
	 * return true if this processor is responsible for processing a given queuentry as specified by qe
	 * @param qe the queuentry
	 * @return true if we are processing qe
	 */
	public boolean matchesQueueEntry(final JDFQueueEntry _qe)
	{
		final JDFQueueEntry qe = currentQE == null ? null : currentQE.getQueueEntry();
		return _qe != null && qe != null && qe.getQueueEntryID().equals(_qe.getQueueEntryID());
	}

	/**
	 * return true if this processor is responsible for processing a given node as specified by ni
	 * @param ni the NodeIdentifier to match against
	 * @return true if ni matches this
	 */
	public boolean matchesNode(final NodeIdentifier ni)
	{
		final JDFQueueEntry qe = currentQE == null ? null : currentQE.getQueueEntry();
		return qe == null ? false : qe.matchesNodeIdentifier(ni);
	}

	/**
	 * apply the phase as described by jobPhase and burn it into our listener
	 * @param jobPhase
	 */
	private void applyPhase(final JDFJobPhase jobPhase)
	{
		final StatusListener statusListener = getStatusListener();
		final JDFDeviceInfo devInfo = (JDFDeviceInfo) jobPhase.getParentNode();
		final double deltaAmount = jobPhase.getPhaseAmount();
		final double deltaWaste = jobPhase.getPhaseWaste();
		statusListener.setAmount(_trackResource, deltaAmount, deltaWaste, jobPhase.getAmount(), jobPhase.getWaste());
		statusListener.signalStatus(devInfo.getDeviceStatus(), devInfo.getStatusDetails(), jobPhase.getStatus(), jobPhase.getStatusDetails(), false);
		final double percentCompleted = jobPhase.getPercentCompleted();
		if (percentCompleted > 0)
		{
			statusListener.setPercentComplete(percentCompleted);
		}
		log.debug("Node Status :" + jobPhase.getStatus() + " " + jobPhase.getStatusDetails() + " " + deltaAmount + " " + deltaWaste + " completed: " + percentCompleted);
	}

	/**
	 * constructor
	 * @param qProc the devices queueprocessor
	 * @param device the parent device that this processor does processing for
	 * @param qeToProcess the queueentry that this processor will be working for
	 */
	public ProxyDeviceProcessor(final ProxyDevice device, final QueueProcessor qProc, final IQueueEntry qeToProcess)
	{
		super(device);

		_statusListener = new StatusListener(device.getSignalDispatcher(), device.getDeviceID(), device.getICSVersions());
		notificationQueryHandler = new NotificationQueryHandler();
		currentQE = qeToProcess;

		init(qProc, _statusListener, _parent.getProperties());
	}

	/**
	 * @param slaveURL
	 * @return true if the recipient queue end received the ticket
	 */
	public boolean submit(final String slaveURL)
	{
		URL qURL;
		try
		{
			qURL = slaveURL == null ? null : new URL(slaveURL);
		}
		catch (final MalformedURLException x)
		{
			qURL = null;
		}
		EnumQueueEntryStatus qes = null;
		final File hf = getParent().getProxyProperties().getSlaveInputHF();
		if (qURL != null)
		{
			final IProxyProperties proxyProperties = getParent().getProxyProperties();
			final File deviceOutputHF = proxyProperties.getSlaveOutputHF();
			final MIMEDetails ud = new MIMEDetails();
			ud.httpDetails.chunkSize = proxyProperties.getSlaveHTTPChunk();
			ud.transferEncoding = proxyProperties.getSlaveMIMEEncoding();
			final boolean expandMime = proxyProperties.getSlaveMIMEExpansion();
			final boolean isMime = proxyProperties.isSlaveMimePackaging();

			final IQueueEntry iqe = submitToQueue(qURL, deviceOutputHF, ud, expandMime, isMime);
			qes = iqe == null ? null : iqe.getQueueEntry().getQueueEntryStatus();
		}
		// try again in case of no url or failure
		if ((qes == null || EnumQueueEntryStatus.Aborted.equals(qes)) && hf != null)
		{
			qes = submitToHF(hf);
		}
		if (qes == null || EnumQueueEntryStatus.Aborted.equals(qes))
		{
			// TODO handle errors
			log.error("submitting queueentry unsuccessful: " + currentQE.getQueueEntryID());
			shutdown();
		}
		return qes != null;
	}

	/**
	 * @param fHF the hot folder destination
	 * @return EnumQueueEntryStatus the status of the submitted queueentry
	 */
	private EnumQueueEntryStatus submitToHF(final File fHF)
	{
		final JDFQueueEntry qe = currentQE.getQueueEntry();
		final JDFNode node = getCloneJDFForSlave();
		KElement modNode = node;
		if (node == null)
		{
			// TODO abort!
			log.error("submitToQueue - no JDFDoc at: " + BambiNSExtension.getDocURL(qe));
			_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
		}
		else
		{
			if (slaveCallBack != null)
			{
				final XMLDoc d = slaveCallBack.updateJDFForExtern(node.getOwnerDocument_JDFElement());
				modNode = d.getRoot();
			}
			final File fLoc = new File(((ProxyDevice) _parent).getNameFromQE(qe));
			final File fileInHF = FileUtil.getFileInDirectory(fHF, fLoc);
			final boolean bWritten = modNode.getOwnerDocument_KElement().write2File(fileInHF, 0, true);
			if (bWritten)
			{
				submitted("qe" + JDFElement.uniqueID(0), EnumQueueEntryStatus.Running, UrlUtil.fileToUrl(fileInHF, true), null);
			}
			else
			{
				log.error("Could not write File: " + fLoc + " to " + fHF);
				_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
			}
		}
		return qe.getQueueEntryStatus();
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#processDoc(org.cip4.jdflib.node.JDFNode, org.cip4.jdflib.jmf.JDFQueueEntry)
	 * @param nod the node to process
	 * @param qe the queueentry of the node to process
	 * @return nothing - this should never be called
	 * @throws NotImplementedException whenever called...
	 */
	@Override
	public EnumQueueEntryStatus processDoc(final JDFNode nod, final JDFQueueEntry qe)
	{
		throw new NotImplementedException();
	}

	/**
	 * @param qe
	 * @param node
	 * @param qeR
	 * @param slaveDeviceID
	 */
	@Override
	protected void submitted(final String devQEID, final EnumQueueEntryStatus newStatus, final String slaveURL, final String slaveDeviceID)
	{
		super.submitted(devQEID, newStatus, slaveURL, slaveDeviceID);
		setupStatusListener(currentQE.getJDF(), currentQE.getQueueEntry());
		if (EnumQueueEntryStatus.Waiting.equals(newStatus))
		{
			_statusListener.signalStatus(EnumDeviceStatus.Idle, "Submitted", EnumNodeStatus.Waiting, "Submitted", false);
		}
		else
		{
			_statusListener.signalStatus(EnumDeviceStatus.Running, "Submitted", EnumNodeStatus.InProgress, "Submitted", false);
		}
		createSubscriptionsForQE(slaveURL, devQEID);
	}

	/**
	 * @param slaveURL 
	 * @param devQEID 
	 * 
	 */
	private void createSubscriptionsForQE(final String slaveURL, final String devQEID)
	{
		if (!UrlUtil.isHttp(slaveURL))
		{
			return;
		}
		if (!EnumSlaveStatus.JMF.equals(getParent().getSlaveStatus()))
		{
			return;
		}

		final AbstractProxyDevice parentDevice = getParent();
		final String deviceURL = parentDevice.getDeviceURLForSlave();

		// TODO check knownsubscriptions and globalize if possible
		final JDFJMF jmfs[] = new JMFBuilder().createSubscriptions(deviceURL, devQEID, 10., 0);
		for (int i = 0; i < jmfs.length; i++)
		{
			parentDevice.sendJMF(jmfs[i], slaveURL, null);
		} // TODO handle response
	}

	/**
	 * @return
	 */
	private JDFQueueEntry getQueueEntry()
	{
		return currentQE == null ? null : currentQE.getQueueEntry();
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#init(org.cip4.bambi.core.queues.QueueProcessor, org.cip4.bambi.core.StatusListener, org.cip4.bambi.core.IDeviceProperties)
	 * @param queueProcessor
	 * @param statusListener
	 * @param devProperties
	*/
	@Override
	public void init(final QueueProcessor queueProcessor, final StatusListener statusListener, final IDeviceProperties devProperties)
	{

		super.init(queueProcessor, statusListener, devProperties);
		final JDFQueueEntry qe = getQueueEntry();
		log.info("processQueueEntry queuentryID=" + qe.getQueueEntryID());
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#shutdown()
	*/
	@Override
	public void shutdown()
	{
		log.info("shutting down " + toString());
		super.shutdown();
		((ProxyDevice) _parent).removeProcessor(this);
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#stopProcessing(org.cip4.jdflib.core.JDFElement.EnumNodeStatus)
	 * @param newStatus
	 */
	@Override
	public EnumNodeStatus stopProcessing(final EnumNodeStatus newStatus)
	{
		final String slaveQE = getSlaveQEID();
		final EnumNodeStatus status = getParent().stopSlaveProcess(slaveQE, newStatus);
		if (status != null)
		{
			stopTime = System.currentTimeMillis();
		}
		return status;
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if all went well
	 */
	protected boolean returnFromSlave(final JDFMessage m, final JDFResponse resp)
	{
		final JDFReturnQueueEntryParams retQEParams = m.getReturnQueueEntryParams(0);

		// get the returned JDFDoc from the incoming ReturnQE command and pack
		// it in the outgoing
		final JDFDoc doc = retQEParams.getURLDoc();
		final JDFQueueEntry qe = currentQE.getQueueEntry();
		if (doc == null)
		{
			final String errorMsg = "failed to parse the JDFDoc from the incoming " + "ReturnQueueEntry with QueueEntryID=" + currentQE.getQueueEntryID();
			JMFHandler.errorResponse(resp, errorMsg, 2, EnumClass.Error);
		}
		else
		{
			// brutally overwrite the current node with this
			_statusListener.replaceNode(doc.getJDFRoot());
		}

		BambiNSExtension.setDeviceURL(qe, null);

		final VString aborted = retQEParams.getAborted();
		if (aborted != null && aborted.size() != 0)
		{
			finalizeProcessDoc(EnumQueueEntryStatus.Aborted);
		}
		else
		{
			finalizeProcessDoc(EnumQueueEntryStatus.Completed);
		}

		return true;
	}

	@Override
	protected boolean finalizeProcessDoc(final EnumQueueEntryStatus qes)
	{
		final boolean b = super.finalizeProcessDoc(qes);
		shutdown(); // remove ourselves out of the processors list
		return b;
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleStatusSignal(final JDFMessage m, final JDFResponse resp)
	{
		return this.new StatusSignalHandler().handleSignal(m, resp);
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleNotificationSignal(final JDFMessage m, final JDFResponse resp)
	{
		return this.new NotificationSignalHandler().handleSignal(m, resp);

	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleResourceSignal(final JDFMessage m, final JDFResponse resp)
	{
		return this.new ResourceSignalHandler().handleSignal(m, resp);
	}

	/**
	 * @return the deviceID of the slave
	 */
	String getSlaveDeviceID()
	{
		return currentQE.getQueueEntry().getDeviceID();
	}

	/**
	 * @param root the Kelement root does not fill idle (e.g. queued) elements
	 */
	@Override
	public void addToDisplayXML(final KElement root)
	{
		final EnumDeviceStatus deviceStatus = _statusListener.getDeviceStatus();
		if (deviceStatus == null || EnumDeviceStatus.Idle.equals(deviceStatus))
		{
			return;
		}
		super.addToDisplayXML(root);
	}

	/**
	 * removes all direct nodeinfo subscriptions and adds new ones to the proxy if required
	 * @param root the node to update subscriptions in
	 */
	@Override
	void updateNISubscriptions(final JDFNode root)
	{
		if (root == null)
		{
			return;
		}

		super.updateNISubscriptions(root);
		if (!EnumSlaveStatus.NODEINFO.equals(getParent().getSlaveStatus()))
		{
			return;
		}

		final AbstractProxyDevice p = getParent();
		final JDFJMF jmfs[] = new JMFBuilder().createSubscriptions(p.getDeviceURLForSlave(), null, 10., 0);
		final JDFNodeInfo ni = root.getCreateNodeInfo();
		final String senderID = getParent().getDeviceID();
		for (int i = 0; i < jmfs.length; i++)
		{
			final JDFJMF newJMF = (JDFJMF) ni.copyElement(jmfs[i], null);
			newJMF.setSenderID(senderID);
		}

		log.info("creating subscription for doc:" + root.getJobID(true) + " - " + root.getJobPartID(false) + " to " + p.getDeviceURLForSlave());
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleNotificationQuery(final JDFMessage m, final JDFResponse resp)
	{
		return notificationQueryHandler.handleMessage(m, resp);
	}

}