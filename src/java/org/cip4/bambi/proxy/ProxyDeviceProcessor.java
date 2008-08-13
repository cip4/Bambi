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
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.proxy.AbstractProxyDevice.EnumSlaveStatus;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
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
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.resource.JDFEvent;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * 
 * @author prosirai
 */
public class ProxyDeviceProcessor extends AbstractProxyProcessor
{
	static Log log = LogFactory.getLog(ProxyDeviceProcessor.class);
	private static final long serialVersionUID = -384123582645081254L;
	private QueueEntryStatusContainer qsc;
	private final String _slaveURL;

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
		 * @param match
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
			return b;
		}

		/**
		 * updates the current status based on the data in the input status
		 * signal or response
		 * 
		 * @param jobPhase
		 *            the jobphase containing the status information
		 */
		private boolean handleStatusUpdate(JDFJobPhase jobPhase)
		{
			NodeIdentifier ni = jobPhase.getIdentifier();
			JDFNode n = currentQE.getJDF().getJDFRoot();
			VElement v = n.getMatchingNodes(ni);

			if (v == null)
				return false;

			qsc.applyPhase(jobPhase);
			_queueProcessor.updateEntry(qsc.qe, jobPhase.getQueueEntryStatus(), null, null);
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
				JMFHandler.errorResponse(resp, "missing event in Event signal", 1);
			else
			{
				_statusListener.setEvent(e.getEventID(), e.getEventValue(), n.getCommentText());
			}
			return true;
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
				_statusListener.setTotal(id, amount, true);

			map2 = new VJDFAttributeMap(map);
			map2.put(AttributeName.CONDITION, "Good");
			amount = info.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, map2);
			if (amount > 0)
				_statusListener.setTotal(id, amount, false);

			return true;
		}
	}

	/**
	 * this baby handles all status updates that are stored in the queuentry
	 * 
	 * @author prosirai
	 * 
	 */
	protected class QueueEntryStatusContainer
	{
		protected JDFQueueEntry qe;
		protected KElement theContainer;
		protected JDFNode theNode;
		protected JDFJobPhase jp;

		public QueueEntryStatusContainer()
		{
			qe = currentQE.getQueueEntry();
			theNode = currentQE.getJDF();
			theContainer = BambiNSExtension.getCreateStatusContainer(qe);
			jp = (JDFJobPhase) theContainer.appendElement(ElementName.JOBPHASE);
			jp.setStatus(EnumNodeStatus.Waiting); // TODO evaluate qe
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
			return _qe != null && qe != null && qe.getQueueEntryID().equals(_qe.getQueueEntryID());
		}

		/**
		 * return true if this processor is responsible for processing a given
		 * node as specified by ni
		 * 
		 * @param ni
		 *            the node identifier (jobid, jobpartid, part*)
		 * @return true if we are processing ni
		 */
		public boolean matchesQueueEntry(NodeIdentifier ni)
		{
			return qe.matchesNodeIdentifier(ni);
		}

		/**
		 * @param jobPhase
		 */
		private void applyPhase(JDFJobPhase jobPhase)
		{
			double deltaWaste = jobPhase.getWasteDifference(jp);
			double deltaAmount = jobPhase.getAmountDifference(jp);
			final IStatusListener statusListener = getStatusListener();
			final JDFDeviceInfo devInfo = (JDFDeviceInfo) jobPhase.getParentNode();
			statusListener.updateAmount(_trackResource, deltaAmount, deltaWaste);
			statusListener.signalStatus(devInfo.getDeviceStatus(), devInfo.getStatusDetails(), jobPhase.getStatus(), jobPhase.getStatusDetails(), false);
			log.info("Node Status :" + jobPhase.getStatus() + " " + jobPhase.getStatusDetails() + " " + deltaAmount
					+ " " + deltaWaste);
			jp = (JDFJobPhase) jp.replaceElement(jobPhase);
		}

		/**
		 * 
		 */
		public void delete()
		{
			if (theContainer != null)
				theContainer.deleteNode();
		}
	}

	/**
	 * constructor
	 * 
	 * @param queueProcessor
	 *            points to the QueueProcessor
	 * @param statusListener
	 *            points to the StatusListener
	 * @param _callBack
	 *            the converter call back too and from device
	 * @param device
	 *            the parent device that this processor does processing for
	 * @param qeToProcess
	 *            the queueentry that this processor will be working for
	 * @param doc
	 */
	public ProxyDeviceProcessor(ProxyDevice device, QueueProcessor qProc, IQueueEntry qeToProcess, String slaveURL)
	{
		super(device);
		_statusListener = new StatusListener(device.getSignalDispatcher(), device.getDeviceID());
		currentQE = qeToProcess;
		qsc = this.new QueueEntryStatusContainer();

		init(qProc, _statusListener, _parent.getProperties());

		if (slaveURL == null)
			slaveURL = getParent().getProxyProperties().getSlaveURL();
		_slaveURL = slaveURL;

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
			qes = submitToQueue(qURL, deviceOutputHF, ud, expandMime);
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
			log.error("submitting queueentry unsuccessful: " + qeToProcess.getQueueEntryID());
			shutdown();
		}
	}

	/**
	* @param hfURL
	* @param qe
	* @return
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
			_statusListener.signalStatus(EnumDeviceStatus.Running, "Submitted", EnumNodeStatus.Waiting, "Submitted", false);
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

		ProxyDevice p = getParent();
		JDFJMF jmfs[] = p.createSubscriptions(devQEID);
		JMFFactory factory = new JMFFactory(slaveCallBack);
		String deviceID = p.getDeviceID();
		for (int i = 0; i < jmfs.length; i++)
		{
			factory.send2URL(jmfs[i], slaveURL, null, deviceID);
		} // TODO handle response        
	}

	/**
	 * @return
	 */
	ProxyDevice getParent()
	{
		return (ProxyDevice) _parent;
	}

	/**
	 * @return
	 */
	private JDFQueueEntry getQueueEntry()
	{
		return qsc.qe;
	}

	/**
	 * @return the internal jobphase for this processor
	 */
	public JDFJobPhase getJobPhase()
	{
		return qsc.jp;
	}

	/**
	 * @param qe
	 * @return
	 */
	public boolean matchesQueueEntry(JDFQueueEntry qe)
	{
		return qsc.matchesQueueEntry(qe);
	}

	/**
	 * @param ni the NodeIdentifier to match against
	 * @return true if ni matches this
	 */
	public boolean matchesNode(NodeIdentifier ni)
	{
		return qsc.matchesQueueEntry(ni);
	}

	@Override
	public void init(QueueProcessor queueProcessor, StatusListener statusListener, IDeviceProperties devProperties)
	{

		super.init(queueProcessor, statusListener, devProperties);
		JDFQueueEntry qe = getQueueEntry();
		log.info("processQueueEntry queuentryID=" + qe.getQueueEntryID());
		JDFNode nod = getJDFNode();

		//		updateNISubscriptions(nod);
		if (slaveCallBack != null)
			slaveCallBack.updateJDFForExtern(nod.getOwnerDocument_JDFElement());

	}

	/**
	 * @return
	 */
	JDFNode getJDFNode()
	{
		return qsc.theNode;
	}

	@Override
	public void shutdown()
	{
		log.info("shutting down " + toString());
		super.shutdown();
		((ProxyDevice) _parent).removeProcessor(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cip4.bambi.core.AbstractDeviceProcessor#stopProcessing(org.cip4.jdflib
	 * .core.JDFElement.EnumNodeStatus)
	 */
	@Override
	public void stopProcessing(EnumNodeStatus newStatus)
	{
		final String slaveQE = getSlaveQEID();
		getParent().stopSlaveProcess(slaveQE, newStatus);
	}

	/**
	 * @param m
	 * @param resp
	 * @return
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
			JMFHandler.errorResponse(resp, errorMsg, 2);
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
		// qsc.delete(); // better to retain. remove only when removing qe
		qsc = null;
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
	private String getSlaveDeviceID()
	{
		return currentQE.getQueueEntry().getDeviceID();
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

		ProxyDevice p = getParent();
		JDFJMF jmfs[] = p.createSubscriptions(null);
		JDFNodeInfo ni = root.getCreateNodeInfo();
		for (int i = 0; i < jmfs.length; i++)
			ni.copyElement(jmfs[i], null);

		log.info("creating subscription for doc:" + root.getJobID(true) + " - " + root.getJobPartID(false) + " to "
				+ p.getSlaveURL());
	}

}