/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.StatusCounter;

public class ProxyDevice extends AbstractProxyDevice
{
	/**
	 * return the jmf root of the status jmf that contains all modules, null if
	 * no modules are active
	 * 
	 * @return
	 */
	protected class MultiCounter
	{

		private final Vector<StatusListener> counters = new Vector<StatusListener>();

		private JDFDoc getStatusResponse(JDFMessage m)
		{

			JDFDoc d = null;
			JDFResponse response = null;

			for (int i = 1; i < counters.size(); i++)
			{
				StatusListener listener = counters.elementAt(i);
				if (!listener.matchesQuery(m))
					continue;

				StatusCounter counter = listener.getStatusCounter();
				if (d == null)
				{
					d = counter.getDocJMFPhaseTime();
					response = d.getJMFRoot().getResponse(0);
				}

				final JDFDoc docJMFPhaseTime = counter.getDocJMFPhaseTime();
				if (docJMFPhaseTime == null)
					continue;
				final JDFResponse response2 = docJMFPhaseTime.getJMFRoot().getResponse(0);
				JDFDeviceInfo di2 = response2.getDeviceInfo(0);
				String devID = di2.getDeviceID();
				JDFDeviceInfo di3 = KElement.isWildCard(devID) ? null : (JDFDeviceInfo) response.getChildWithAttribute(ElementName.DEVICEINFO, AttributeName.DEVICEID, null, devID, 0, true);
				if (di3 != null)
				{
					VElement phases = di2.getChildElementVector(ElementName.JOBPHASE, null, null, true, -1, false);
					for (int j = 0; j < phases.size(); j++)
					{
						di3.copyElement(phases.elementAt(j), null);
					}
				}
				else
				{
					response.copyElement(di2, null);
				}
			}
			return d;
		}

		/**
		 * @param statusCounter
		 */
		public void removeCounter(StatusListener statusCounter)
		{
			counters.remove(statusCounter);
		}

		/**
		 * @param statusCounter
		 */
		public void addCounter(StatusListener statusCounter)
		{
			counters.add(statusCounter);
		}

		/**
		 * @param m  the query input message
		 * @return JDFDoc the response message
		 */
		public boolean fillResourceResponse(JDFMessage m, JDFResponse response)
		{
			boolean bRet = false;
			for (int i = 1; i < counters.size(); i++)
			{
				StatusListener listener = counters.elementAt(i);
				if (!listener.matchesQuery(m))
					continue;

				StatusCounter counter = listener.getStatusCounter();
				final JDFDoc docJMFResource = counter.getDocJMFResource();
				if (docJMFResource == null)
					continue;
				JDFMessage response2 = docJMFResource.getJMFRoot().getResponse(0);
				if (response2 == null)
					response2 = docJMFResource.getJMFRoot().getSignal(0);
				VElement v = response2 == null ? null : response2.getChildElementVector(ElementName.RESOURCEINFO, null);
				int riSize = v == null ? 0 : v.size();
				for (int ii = 0; ii < riSize; ii++)
				{
					response.moveElement(v.get(ii), null);
					bRet = true; // we have one
				}
			}
			return bRet;
		}
	} // end of inner class MultiCounter

	//////////////////////////////////////////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(ProxyDevice.class.getName());
	protected MultiCounter statusContainer;

	/**
	 * simple dispatcher
	 * 
	 * @author prosirai
	 * 
	 */
	protected class RequestQueueEntryHandler extends AbstractHandler
	{

		public RequestQueueEntryHandler()
		{
			super(EnumType.RequestQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			// TODO retain rqe in case we cannot submit now
			// check for valid RequestQueueEntryParams
			JDFRequestQueueEntryParams qep = m.getRequestQueueEntryParams(0);
			if (qep == null)
			{
				JMFHandler.errorResponse(resp, "QueueEntryParams missing in RequestQueueEntry message", 7);
				return true;
			}
			final String queueURL = qep.getQueueURL();
			if (queueURL == null || queueURL.length() < 1)
			{
				JMFHandler.errorResponse(resp, "QueueURL is missing", 7);
				return true;
			}

			final NodeIdentifier nid = qep.getNodeIdentifier();
			// submit a specific QueueEntry
			IQueueEntry iqe = _theQueueProcessor.getQueueEntry(nid);
			JDFQueueEntry qe = iqe == null ? null : iqe.getQueueEntry();
			if (qe != null && EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus())
					&& KElement.isWildCard(qe.getDeviceID()))
			{
				qe.setDeviceID(m.getSenderID());
				submitQueueEntry(iqe, queueURL);
			}
			else if (qe == null)
			{
				JMFHandler.errorResponse(resp, "No QueueEntry is available for request", 108);
			}
			else
			{
				String qeStatus = qe.getQueueEntryStatus().getName();
				JMFHandler.errorResponse(resp, "requested QueueEntry is " + qeStatus, 106);
			}
			return true;
		}
	}

	protected class ReturnQueueEntryHandler extends AbstractHandler
	{

		public ReturnQueueEntryHandler()
		{
			super(EnumType.ReturnQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			log.info("Handling " + m.getType());
			JDFReturnQueueEntryParams retQEParams = m.getReturnQueueEntryParams(0);
			if (retQEParams == null)
			{
				JMFHandler.errorResponse(resp, "ReturnQueueEntryParams missing in ReturnQueueEntry message", 7);
				return true;
			}

			final String outQEID = retQEParams.getQueueEntryID();
			ProxyDeviceProcessor proc = getProcessorForSlaveQE(resp, outQEID);
			if (proc != null)
				proc.returnFromSlave(m, resp);
			return true;
		}
	}

	protected class StatusQueryHandler extends StatusHandler
	{
		public StatusQueryHandler()
		{
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (statusContainer == null)
				return false;

			JDFDoc docJMF = statusContainer.getStatusResponse(m);

			if (docJMF == null)
				return super.handleMessage(m, resp); // idle is handled by super

			boolean bOK = copyPhaseTimeFromCounter(resp, docJMF);
			if (bOK)
				addQueueToStatusResponse(m, resp);
			return bOK;
		}

	}

	protected class ResourceQueryHandler extends ResourceHandler
	{
		public ResourceQueryHandler()
		{
			super();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (statusContainer == null)
				return false;

			boolean bHandeled = statusContainer.fillResourceResponse(m, resp);
			return bHandeled ? bHandeled : super.handleMessage(m, resp);
		}
	}// end of inner class ResourceQueryHandler

	////////////////////////////////////////////////////////////////////	
	protected class NotificationSignalHandler extends AbstractHandler
	{
		public NotificationSignalHandler()
		{
			super(EnumType.Notification, new EnumFamily[] { EnumFamily.Signal });
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			log.info("Handling " + m.getType() + " " + m.getID());
			Vector<ProxyDeviceProcessor> v = getProxyProcessors();
			boolean b = false;
			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{
				b = v.get(i).handleNotificationSignal(m, resp) || b;
			}
			return true; // handled if any was ok
		}
	}// end of inner class ResourceQueryHandler

	////////////////////////////////////////////////////////////////////	
	protected class ResourceSignalHandler extends AbstractHandler
	{
		public ResourceSignalHandler()
		{
			super(EnumType.Resource, new EnumFamily[] { EnumFamily.Signal });
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			log.info("Handling " + m.getType() + " " + m.getID());
			Vector<ProxyDeviceProcessor> v = getProxyProcessors();
			boolean b = false;
			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{
				b = v.get(i).handleResourceSignal(m, resp) || b;
			}
			return true; // handled if any was ok
		}
	}// end of inner class ResourceQueryHandler

	////////////////////////////////////////////////////////////////////
	protected class StatusSignalHandler extends AbstractHandler
	{

		public StatusSignalHandler()
		{
			super(EnumType.Status, new EnumFamily[] { EnumFamily.Signal });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.
		 * JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			log.info("Handling " + m.getType() + " " + m.getID());
			Vector<ProxyDeviceProcessor> v = getProxyProcessors();
			boolean b = false;
			int size = v == null ? 0 : v.size();
			for (int i = 0; i < size; i++)
			{
				b = v.get(i).handleStatusSignal(m, resp) || b;
			}
			if (!b)
			{
				JDFStatusQuParams sqp = m.getStatusQuParams();
				String qeid = (sqp != null) ? sqp.getQueueEntryID() : null;
				if (KElement.isWildCard(qeid))
					b = handleIdle(m, resp);

				if (!b)
					JMFHandler.errorResponse(resp, "Unknown QueueEntry: " + qeid, 103);
			}
			else
			{
				resp.setReturnCode(0);
			}

			return true; // handled if any was ok
		}

		/**
		 * @return true if handled
		 */
		private boolean handleIdle(JDFMessage m, JDFResponse resp)
		{
			log.info("handling idle status signal...");
			return true;
		}
	}// end of inner class StatusSignalHandler

	////////////////////////////////////////////////////////////////////////////
	// ///////

	// ////////////////////////////////////////////////////////////////

	public ProxyDevice(IDeviceProperties properties)
	{
		super(properties);
		final IProxyProperties proxyProperties = getProxyProperties();
		statusContainer = new MultiCounter();
		_jmfHandler.setFilterOnDeviceID(false);
		int maxPush = proxyProperties.getMaxPush();
		if (maxPush > 0)
			_theQueueProcessor.getQueue().setMaxRunningEntries(maxPush);
		// TODO correctly dispatch them
	}

	@Override
	protected void addHandlers()
	{
		super.addHandlers();
		_jmfHandler.addHandler(this.new RequestQueueEntryHandler());
		_jmfHandler.addHandler(this.new ReturnQueueEntryHandler());
		_jmfHandler.addHandler(this.new StatusSignalHandler());
		_jmfHandler.addHandler(this.new StatusQueryHandler());
		_jmfHandler.addHandler(this.new ResourceSignalHandler());
		_jmfHandler.addHandler(this.new NotificationSignalHandler());
		_jmfHandler.addHandler(this.new ResourceQueryHandler());
	}

	/**
	 * @param iqe
	 * @param queueURL
	 * @param deviceID
	 */
	public void submitQueueEntry(IQueueEntry iqe, String queueURL)
	{
		ProxyDeviceProcessor pdp = new ProxyDeviceProcessor(this, _theQueueProcessor, iqe, queueURL);
		addProcessor(pdp);
	}

	/**
	 * returns null, since the ProxyDevice doesn't need a DeviceProcessor
	 */
	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		if (getProxyProperties().getMaxPush() <= 0)
			return null;
		return new ProxyDispatcherProcessor(this);
	}

	/**
	 * get a simple file name for the JDF in a queueentry
	 * 
	 * @param qe
	 *            the JDFQueueEntry to name mangle
	 * @return
	 */
	public String getNameFromQE(JDFQueueEntry qe)
	{
		return "q" + qe.getQueueEntryID() + ".jdf";
	}

	/**
	 * remove a processor from the list of active processors
	 * 
	 * @param processor
	 */
	public void removeProcessor(AbstractDeviceProcessor processor)
	{
		log.info("removing device proceesor");
		_deviceProcessors.remove(processor);
		final StatusListener statusListener = (StatusListener) processor.getStatusListener();
		if (statusListener != null)
			statusContainer.removeCounter(statusListener);
		// zapp the subscription that we added for listening to the device
		// TODO
		// _parent.getSignalDispatcher().removeSubScription(slaveChannelID);

	}

	/**
	 * gets the device processor for a given queuentry
	 * 
	 * @param queueEntryID
	 *            - if null use any
	 * @return the processor that is processing queueEntryID, null if none
	 *         matches
	 */
	protected Vector<ProxyDeviceProcessor> getProxyProcessors()
	{
		Vector<ProxyDeviceProcessor> v = new Vector<ProxyDeviceProcessor>();
		for (int i = 0; i < _deviceProcessors.size(); i++)
		{
			AbstractDeviceProcessor theDeviceProcessor = _deviceProcessors.get(i);
			if (theDeviceProcessor instanceof ProxyDeviceProcessor)
				v.add((ProxyDeviceProcessor) theDeviceProcessor);
		}
		return v.size() == 0 ? null : v;
	}

	/**
	 * @param resp
	 * @param slaveQEID
	 * @return
	 */
	private ProxyDeviceProcessor getProcessorForSlaveQE(JDFResponse resp, final String slaveQEID)
	{
		final String inQEID = getIncomingQEID(slaveQEID);
		ProxyDeviceProcessor proc = inQEID == null ? null : (ProxyDeviceProcessor) getProcessor(inQEID);

		if (proc == null)
		{
			String errorMsg = "QueueEntry with ID=" + slaveQEID + " is not being processed";
			JMFHandler.errorResponse(resp, errorMsg, 2);
		}
		return proc;
	}

	/**
	 * remove a processor from the list of active processors
	 * 
	 * @param processor
	 */
	public void addProcessor(AbstractDeviceProcessor processor)
	{
		log.info("adding device proceesor");
		_deviceProcessors.add(processor);
		final StatusListener statusListener = (StatusListener) processor.getStatusListener();
		if (statusListener != null)
			statusContainer.addCounter(statusListener);
	}

	/**
	 * @param outQEID
	 * @return
	 */
	private String getIncomingQEID(String outQEID)
	{
		if (outQEID == null || _deviceProcessors == null)
			return null;
		for (int i = 0; i < _deviceProcessors.size(); i++)
		{
			AbstractDeviceProcessor aProc = _deviceProcessors.get(i);
			if (!(aProc instanceof ProxyDeviceProcessor))
				continue;
			ProxyDeviceProcessor proc = (ProxyDeviceProcessor) aProc;
			final String slaveQEID = proc.getSlaveQEID();
			if (outQEID.equals(slaveQEID))
				return proc.getCurrentQE().getQueueEntryID();
		}
		return null;
	}

	/**
	 * @param outQEID
	 * @return
	 */
	private String getSlaveQEID(String bambiQEID)
	{
		if (bambiQEID == null || _deviceProcessors == null)
			return null;
		for (int i = 0; i < _deviceProcessors.size(); i++)
		{
			AbstractDeviceProcessor aProc = _deviceProcessors.get(i);
			if (!(aProc instanceof ProxyDeviceProcessor))
				continue;
			ProxyDeviceProcessor proc = (ProxyDeviceProcessor) aProc;
			final String qeID = proc.getCurrentQE().getQueueEntryID();
			if (bambiQEID.equals(qeID))
				return proc.getSlaveQEID();
		}
		JDFQueueEntry qe = _theQueueProcessor.getQueue().getQueueEntry(bambiQEID);
		return BambiNSExtension.getSlaveQueueEntryID(qe);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cip4.bambi.core.AbstractDevice#canAccept(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public boolean canAccept(JDFDoc doc)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cip4.bambi.core.AbstractDevice#getNodeFromDoc(org.cip4.jdflib.core
	 * .JDFDoc)
	 */
	@Override
	public JDFNode getNodeFromDoc(JDFDoc doc)
	{
		// TODO Auto-generated method stub
		return doc.getJDFRoot();
	}

	@Override
	public JDFQueueEntry stopProcessing(String queueEntryID, EnumNodeStatus status)
	{
		if (status == null)
		{

			JDFJMF jmf = JMFFactory.buildRemoveQueueEntry(getSlaveQEID(queueEntryID));
			if (jmf != null)
			{
				new JMFFactory(_callback).send2URL(jmf, getProxyProperties().getSlaveURL(), null, getDeviceID());
			}
		}
		JDFQueueEntry qe = super.stopProcessing(queueEntryID, status);
		return qe;
	}

	@Override
	protected void init()
	{
		super.init();
		if (_theStatusListener == null)
			_theStatusListener = new StatusListener(_theSignalDispatcher, getDeviceID());

	}

}