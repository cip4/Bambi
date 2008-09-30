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

package org.cip4.bambi.proxy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFFactory;
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
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.resource.JDFEvent;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * 
 * @author prosirai
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
			return !_doShutdown;
		//TODO clean up orphans
		return false;
	}

	static Log log = LogFactory.getLog(ProxyDeviceProcessor.class);
	private static final long serialVersionUID = -384123582645081254L;
	private final NotificationQueryHandler notificationQueryHandler;
	protected long stopTime = 0; // this is the stopprocessing time 0 means I'm alive

	protected class StatusSignalHandler
	{
		/**
		 * @param m
		 * @param resp
		 * @return
		 */
		protected boolean handleSignal(JDFMessage m, JDFResponse resp)
		{
			if (m == null || currentQE == null)
				return false;

			JDFStatusQuParams sqp = m.getStatusQuParams();
			NodeIdentifier ni = null;
			if (sqp != null)
			{
				String qeid = sqp.getQueueEntryID();
				boolean matches = KElement.isWildCard(qeid) || ContainerUtil.equals(getSlaveQEID(), qeid);
				if (!matches)
					return false;
				ni = sqp.getIdentifier();
			}
			VElement vMatch = currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
			if (vMatch == null)
				return false;
			VElement deviceInfos = m.getChildElementVector(ElementName.DEVICEINFO, null);
			if (deviceInfos == null)
				return false;
			boolean b = false;
			for (int i = 0; i < deviceInfos.size(); i++)
			{
				b = handleDeviceInfo((JDFDeviceInfo) deviceInfos.get(i)) || b;
			}

			return b;
		}

		/**
		 * @param info
		 * @return true if handled
		 */
		private boolean handleDeviceInfo(JDFDeviceInfo info)
		{
			String slaveDevice = getSlaveDeviceID();
			if (!info.getDeviceID().equals(slaveDevice))
				return false;
			VElement jobPhases = info.getChildElementVector(ElementName.JOBPHASE, null);
			if (jobPhases == null)
				return false;
			boolean b = false;
			for (int i = 0; i < jobPhases.size(); i++)
				b = handleStatusUpdate((JDFJobPhase) jobPhases.get(i)) || b;

			if (b)// only handle counters if anything is processed by us
			{
				final StatusCounter statusCounter = getStatusListener().getStatusCounter();
				if (info.hasAttribute(AttributeName.PRODUCTIONCOUNTER))
				{
					double pc = info.getProductionCounter();
					statusCounter.setCurrentCounter(pc);
				}
				if (info.hasAttribute(AttributeName.TOTALPRODUCTIONCOUNTER))
				{
					double pc = info.getTotalProductionCounter();
					statusCounter.setTotalCounter(pc);
				}
			}

			return b;
		}

		/**
		 * updates the current status based on the data in the input status
		 * signal or response
		 * 
		 * @param jobPhase
		 *            the jobphase containing the status information
		 * @return true if handled
		 */
		private boolean handleStatusUpdate(JDFJobPhase jobPhase)
		{
			NodeIdentifier ni = jobPhase.getIdentifier();
			JDFNode n = currentQE.getJDF().getJDFRoot();
			VElement v = n.getMatchingNodes(ni);

			if (v == null)
				return false;

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
		protected boolean handleSignal(JDFMessage m, JDFResponse resp)
		{
			if (m == null || currentQE == null || !(m instanceof JDFSignal))
				return false;

			JDFSignal s = (JDFSignal) m;
			JDFNotification n = s.getNotification();
			NodeIdentifier ni = n.getIdentifier();
			VElement vMatch = currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
			if (vMatch == null)
				return false;
			String notifType = n.getType();
			boolean b = false;
			if (ElementName.EVENT.equals(notifType))
				b = handleEvent(n, resp);

			return b;
		}

		/**
		 * @param n
		 * @return true if handled
		 */
		private boolean handleEvent(JDFNotification n, JDFResponse resp)
		{
			JDFEvent e = n.getEvent();
			if (e == null)
				JMFHandler.errorResponse(resp, "missing event in Event signal", 1, EnumClass.Error);
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
	 *
	 */
	protected class NotificationQueryHandler extends NotificationHandler
	{
		/**
		 * 
		 */
		public NotificationQueryHandler()
		{
			super(getParent().getSignalDispatcher(), _statusListener);
			families = new EnumFamily[] { EnumFamily.Query };
		}
	}

	/////////////////////////////////////////////////////////////

	protected class ResourceSignalHandler
	{
		/**
		 * @param m
		 * @param resp
		 * @return true if handled
		 */
		protected boolean handleSignal(JDFMessage m, JDFResponse resp)
		{
			if (m == null || currentQE == null)
				return false;

			JDFResourceQuParams rqp = m.getResourceQuParams();
			NodeIdentifier ni = null;
			if (rqp != null)
			{
				String qeid = rqp.getQueueEntryID();
				boolean matches = KElement.isWildCard(qeid) || ContainerUtil.equals(getSlaveQEID(), qeid);
				if (!matches)
					return false;
				ni = rqp.getIdentifier();
			}
			VElement vMatch = currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
			if (vMatch == null)
				return false;
			VElement resourceInfos = m.getChildElementVector(ElementName.RESOURCEINFO, null);
			if (resourceInfos == null)
				return false;
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
		private boolean handleResourceInfo(JDFResourceInfo info)
		{
			String id = info.getResourceID();
			VJDFAttributeMap map = info.getPartMapVector();
			VJDFAttributeMap map2 = new VJDFAttributeMap(map);
			map2.put(AttributeName.CONDITION, "Waste");
			double amount = info.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, map2);
			if (amount > 0)
				_statusListener.updateTotal(id, amount, true);

			map2 = new VJDFAttributeMap(map);
			map2.put(AttributeName.CONDITION, "Good");
			amount = info.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, map2);
			if (amount > 0)
				_statusListener.updateTotal(id, amount, false);

			return true;
		}
	}

	/**
	 * return true if this processor is responsible for processing a given
	 * queuentry as specified by qe
	 * 
	 * @param qe
	 *            the queuentry
	 * @return true if we are processing qe
	 */
	public boolean matchesQueueEntry(JDFQueueEntry _qe)
	{
		JDFQueueEntry qe = currentQE == null ? null : currentQE.getQueueEntry();
		return _qe != null && qe != null && qe.getQueueEntryID().equals(_qe.getQueueEntryID());
	}

	/**
	 * return true if this processor is responsible for processing a given
	 * node as specified by ni
	 * 
	 * @param ni the NodeIdentifier to match against
	 * @return true if ni matches this
	 */
	public boolean matchesNode(NodeIdentifier ni)
	{
		JDFQueueEntry qe = currentQE == null ? null : currentQE.getQueueEntry();
		return qe == null ? false : qe.matchesNodeIdentifier(ni);
	}

	/**
	 * apply the phase as described by jobPhase and burn it into our listener
	 * 
	 * @param jobPhase
	 */
	private void applyPhase(JDFJobPhase jobPhase)
	{
		final StatusListener statusListener = getStatusListener();
		final JDFDeviceInfo devInfo = (JDFDeviceInfo) jobPhase.getParentNode();
		double deltaAmount = jobPhase.getPhaseAmount();
		double deltaWaste = jobPhase.getPhaseWaste();
		statusListener.setAmount(_trackResource, deltaAmount, deltaWaste, jobPhase.getAmount(), jobPhase.getWaste());
		statusListener.signalStatus(devInfo.getDeviceStatus(), devInfo.getStatusDetails(), jobPhase.getStatus(), jobPhase.getStatusDetails(), false);
		double percentCompleted = jobPhase.getPercentCompleted();
		if (percentCompleted > 0)
			statusListener.setPercentComplete(percentCompleted);
		log.debug("Node Status :" + jobPhase.getStatus() + " " + jobPhase.getStatusDetails() + " " + deltaAmount + " "
				+ deltaWaste + " completed: " + percentCompleted);
	}

	/**
	 * constructor
	 * 
	 * @param qProc the devices queueprocessor
	 * @param device the parent device that this processor does processing for
	 * @param qeToProcess the queueentry that this processor will be working for
	 */
	public ProxyDeviceProcessor(ProxyDevice device, QueueProcessor qProc, IQueueEntry qeToProcess)
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
	public boolean submit(String slaveURL)
	{
		URL qURL;
		try
		{
			qURL = slaveURL == null ? null : new URL(slaveURL);
		}
		catch (MalformedURLException x)
		{
			qURL = null;
		}
		EnumQueueEntryStatus qes = null;
		if (qURL != null)
		{
			IProxyProperties proxyProperties = getParent().getProxyProperties();
			final File deviceOutputHF = proxyProperties.getSlaveOutputHF();
			MIMEDetails ud = new MIMEDetails();
			ud.httpDetails.chunkSize = proxyProperties.getSlaveHTTPChunk();
			boolean expandMime = proxyProperties.getSlaveMIMEExpansion();
			IQueueEntry iqe = submitToQueue(qURL, deviceOutputHF, ud, expandMime);
			qes = iqe == null ? null : iqe.getQueueEntry().getQueueEntryStatus();
		}
		else
		{
			File hf = getParent().getProxyProperties().getSlaveInputHF();
			if (hf != null)
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
	private EnumQueueEntryStatus submitToHF(File fHF)
	{
		JDFQueueEntry qe = currentQE.getQueueEntry();
		JDFNode nod = getCloneJDFForSlave();
		if (nod == null)
		{
			// TODO abort!
			log.error("submitToQueue - no JDFDoc at: " + BambiNSExtension.getDocURL(qe));
			_queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
		}
		else
		{
			if (slaveCallBack != null)
			{
				slaveCallBack.updateJDFForExtern(nod.getOwnerDocument_JDFElement());
			}
			File fLoc = new File(((ProxyDevice) _parent).getNameFromQE(qe));
			final File fileInHF = FileUtil.getFileInDirectory(fHF, fLoc);
			boolean bWritten = nod.getOwnerDocument_JDFElement().write2File(fileInHF, 0, true);
			if (bWritten)
			{
				submitted("qe" + JDFElement.uniqueID(0), EnumQueueEntryStatus.Running, UrlUtil.fileToUrl(fileInHF, true));
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
	public EnumQueueEntryStatus processDoc(JDFNode nod, JDFQueueEntry qe)
	{
		throw new NotImplementedException();
	}

	/**
	 * @param qe
	 * @param node
	 * @param qeR
	 */
	@Override
	protected void submitted(String devQEID, EnumQueueEntryStatus newStatus, String slaveURL)
	{
		super.submitted(devQEID, newStatus, slaveURL);
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
	 * 
	 */
	private void createSubscriptionsForQE(String slaveURL, String devQEID)
	{
		if (!UrlUtil.isHttp(slaveURL))
			return;
		if (!EnumSlaveStatus.JMF.equals(getParent().getSlaveStatus()))
			return;

		AbstractProxyDevice p = getParent();
		String deviceURL = p.getDeviceURLForSlave();

		JDFJMF jmfs[] = JMFFactory.createSubscriptions(deviceURL, devQEID, 10., 0);
		String deviceID = p.getDeviceID();
		for (int i = 0; i < jmfs.length; i++)
		{
			JMFFactory.send2URL(jmfs[i], slaveURL, null, slaveCallBack, deviceID);
		} // TODO handle response        
	}

	/**
	 * @return
	 */
	private JDFQueueEntry getQueueEntry()
	{
		return currentQE == null ? null : currentQE.getQueueEntry();
	}

	@Override
	public void init(QueueProcessor queueProcessor, StatusListener statusListener, IDeviceProperties devProperties)
	{

		super.init(queueProcessor, statusListener, devProperties);
		JDFQueueEntry qe = getQueueEntry();
		log.info("processQueueEntry queuentryID=" + qe.getQueueEntryID());
	}

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
	public void stopProcessing(EnumNodeStatus newStatus)
	{
		final String slaveQE = getSlaveQEID();
		getParent().stopSlaveProcess(slaveQE, newStatus);
		stopTime = System.currentTimeMillis();
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if all went well
	 */
	protected boolean returnFromSlave(JDFMessage m, JDFResponse resp)
	{
		JDFReturnQueueEntryParams retQEParams = m.getReturnQueueEntryParams(0);

		// get the returned JDFDoc from the incoming ReturnQE command and pack
		// it in the outgoing
		JDFDoc doc = retQEParams.getURLDoc();
		final JDFQueueEntry qe = currentQE.getQueueEntry();
		if (doc == null)
		{
			String errorMsg = "failed to parse the JDFDoc from the incoming " + "ReturnQueueEntry with QueueEntryID="
					+ currentQE.getQueueEntryID();
			JMFHandler.errorResponse(resp, errorMsg, 2, EnumClass.Error);
		}
		else
		{
			// brutally overwrite the current node with this
			_statusListener.replaceNode(doc.getJDFRoot());
		}

		BambiNSExtension.setDeviceURL(qe, null);

		VString aborted = retQEParams.getAborted();
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

	/**
	 * @return the QueuentryID as submitted to the slave device
	 */
	public String getSlaveQEID()
	{
		return currentQE == null ? null : BambiNSExtension.getSlaveQueueEntryID(currentQE.getQueueEntry());
	}

	@Override
	protected boolean finalizeProcessDoc(EnumQueueEntryStatus qes)
	{
		boolean b = super.finalizeProcessDoc(qes);
		shutdown(); // remove ourselves out of the processors list
		return b;
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleStatusSignal(JDFMessage m, JDFResponse resp)
	{
		return this.new StatusSignalHandler().handleSignal(m, resp);
	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleNotificationSignal(JDFMessage m, JDFResponse resp)
	{
		return this.new NotificationSignalHandler().handleSignal(m, resp);

	}

	/**
	 * @param m
	 * @param resp
	 * @return true if handled
	 */
	public boolean handleResourceSignal(JDFMessage m, JDFResponse resp)
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
	 * @param root the Kelement root
	 * does not fill idle (e.g. queued) elements
	 */
	@Override
	public void addToDisplayXML(KElement root)
	{
		EnumDeviceStatus deviceStatus = _statusListener.getDeviceStatus();
		if (deviceStatus == null || EnumDeviceStatus.Idle.equals(deviceStatus))
			return;
		super.addToDisplayXML(root);
	}

	/**
	 * removes all direct nodeinfo subscriptions and adds new ones to the proxy if required
	 * @param root the node to update subscriptions in
	 */
	@Override
	void updateNISubscriptions(JDFNode root)
	{
		if (root == null)
			return;

		super.updateNISubscriptions(root);
		if (!EnumSlaveStatus.NODEINFO.equals(getParent().getSlaveStatus()))
			return;

		AbstractProxyDevice p = getParent();
		JDFJMF jmfs[] = JMFFactory.createSubscriptions(p.getDeviceURLForSlave(), null, 10., 0);
		JDFNodeInfo ni = root.getCreateNodeInfo();
		String senderID = getParent().getDeviceID();
		for (int i = 0; i < jmfs.length; i++)
		{
			JDFJMF newJMF = (JDFJMF) ni.copyElement(jmfs[i], null);
			newJMF.setSenderID(senderID);
		}

		log.info("creating subscription for doc:" + root.getJobID(true) + " - " + root.getJobPartID(false) + " to "
				+ p.getSlaveURL());
	}

	/**
	 * @param m 
	 * @param resp 
	 * @return true if handled
	 */
	public boolean handleNotificationQuery(JDFMessage m, JDFResponse resp)
	{
		return notificationQueryHandler.handleMessage(m, resp);
	}

}