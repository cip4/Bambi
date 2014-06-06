/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2014 The International Cooperation for the Integration of 
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

import java.util.HashMap;
import java.util.Vector;

import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.DataExtractor;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.messaging.SignalHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoRequestQueueEntryParams.EnumSubmitPolicy;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumActivation;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil.URLProtocol;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * 04.12.2008
 */
public class ProxyDevice extends AbstractProxyDevice
{
	/**
	 * class that handles merging of messages
	 */
	protected class ResponseMerger
	{

		/**
		 * 
		 * @param m the message to process
		 * @return
		 */
		protected JDFDoc getStatusResponse(final JDFMessage m)
		{
			final Vector<StatusListener> counters = getStatusListeners();
			if (counters == null)
			{
				return null;
			}
			JDFDoc dReturnPhaseTime = null;
			JDFResponse response = null;
			boolean first = true;
			for (int i = 0; i < counters.size(); i++)
			{
				final StatusListener listener = counters.elementAt(i);
				if (!listener.matchesQuery(m))
				{
					continue;
				}
				final EnumDeviceStatus subStatus = listener.getDeviceStatus();
				if (subStatus == null || EnumDeviceStatus.Idle.equals(subStatus))
				{
					continue; // skip waiting in queue or idle
				}

				final StatusCounter counter = listener.getStatusCounter();
				if (dReturnPhaseTime == null)
				{
					dReturnPhaseTime = counter.getDocJMFPhaseTime();
					response = dReturnPhaseTime.getJMFRoot().getResponse(0);
				}

				final JDFDoc docJMFPhaseTime = counter.getDocJMFPhaseTime();
				if (docJMFPhaseTime == null || response == null)
				{
					continue;
				}
				if (!first)
				{
					final JDFResponse response2 = docJMFPhaseTime.getJMFRoot().getResponse(0);
					final JDFDeviceInfo di2 = response2.getDeviceInfo(0);
					if (di2 == null)
					{
						getLog().warn("Counter Status Response with null deviceInfo???");
					}
					else
					{
						final String devID = di2.getDeviceID();
						final JDFDeviceInfo di3 = KElement.isWildCard(devID) ? null : (JDFDeviceInfo) response.getChildWithAttribute(ElementName.DEVICEINFO, AttributeName.DEVICEID, null, devID, 0, true);
						if (di3 != null)
						{
							final VElement phases = di2.getChildElementVector(ElementName.JOBPHASE, null, null, true, -1, false);
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
				}
				first = false;
			}
			return dReturnPhaseTime;
		}

		/**
		 * @param m the query input message
		 * @param response 
		 * @return JDFDoc the response message
		 */
		public boolean fillResourceResponse(final JDFMessage m, final JDFResponse response)
		{
			final Vector<StatusListener> counters = getStatusListeners();
			if (counters == null)
			{
				return false;
			}
			boolean bRet = false;
			for (int i = 0; i < counters.size(); i++)
			{
				final StatusListener listener = counters.elementAt(i);
				if (!listener.matchesQuery(m))
				{
					continue;
				}

				final StatusCounter counter = listener.getStatusCounter();
				final JDFDoc docJMFResource = counter.getDocJMFResource();
				if (docJMFResource == null)
				{
					continue;
				}
				JDFMessage response2 = docJMFResource.getJMFRoot().getResponse(0);
				JDFResourceQuParams signalRQP = null;
				if (response2 == null)
				{
					response2 = docJMFResource.getJMFRoot().getSignal(0);
					if (response2 != null)
					{
						signalRQP = response2.getResourceQuParams();
						if (signalRQP != null)
						{
							final JDFResourceQuParams queryRQP = m.getResourceQuParams();
							if (queryRQP == null)
							{
								m.copyElement(signalRQP, null);
							}
							else
							{
								queryRQP.mergeElement(signalRQP, false);
							}
						}
					}
				}
				final VElement v = response2 == null ? null : response2.getChildElementVector(ElementName.RESOURCEINFO, null);
				if (v != null)
				{
					final int riSize = v.size();
					for (int ii = 0; ii < riSize; ii++)
					{
						response.moveElement(v.get(ii), null);
						bRet = true; // we have one
					}
				}
			}
			return bRet;
		}

		/**
		 * @param m
		 * @param resp
		 * @return true if a notification was filled
		 */
		public boolean fillNotifications(final JDFMessage m, final JDFResponse resp)
		{
			final Vector<ProxyDeviceProcessor> procs = getProxyProcessors();
			if (procs == null)
			{
				return false;
			}
			boolean bRet = false;
			final JDFJMF jmfm = m.getJMFRoot();
			//			final JDFJMF jmfr = resp.getJMFRoot();
			for (int i = 0; i < procs.size(); i++)
			{
				bRet = procs.get(i).handleNotificationQuery(m, resp) || bRet;
				// undo handler delete
				jmfm.moveElement(m, null);
				jmfm.moveElement(resp, null);
			}
			// final zapp om m and r from list of signals
			m.deleteNode();
			resp.deleteNode();
			return bRet;

		}
	} // end of inner class MultiCounter

	// ////////////////////////////////////////////////////////////////////////////////

	protected ResponseMerger statusContainer;

	/**
	 * simple dispatcher
	 * @author prosirai
	 */
	protected class RequestQueueEntryHandler extends AbstractHandler
	{
		protected int numSubmitThread;
		protected HashMap<String, SubmitThread> submitThreadMap;

		private class SubmitThread extends Thread
		{
			private final EnumActivation activation;
			private final long start;

			/**
			 * @param iqe the iqe to submit
			 * @param queueURL the url to submit to
			 * @param activation the queuentry activation
			 */
			public SubmitThread(IQueueEntry iqe, String queueURL, EnumActivation activation)
			{
				super("RequestQE_" + getDeviceID() + "_" + numSubmitThread++);
				this.iqe = iqe;
				this.queueURL = queueURL;
				this.activation = activation;
				this.start = System.currentTimeMillis();
			}

			private final IQueueEntry iqe;
			private final String queueURL;

			/**
			 * @see java.lang.Thread#run()
			*/
			@Override
			public void run()
			{
				log.info("submitting for RequestQE");
				try
				{
					submitQueueEntry(iqe, queueURL, activation);
				}
				catch (Throwable x)
				{
					log.error("Error submitting to proxy for qe= " + iqe == null ? "null" : iqe.getQueueEntryID(), x);
				}
				ThreadUtil.notifyAll(this);
				submitThreadMap.remove(iqe.getQueueEntryID());
			}

			/**
			 * 
			 * @see java.lang.Thread#toString()
			 */
			@Override
			public String toString()
			{
				return "SubmitThread: " + iqe;
			}

			/**
			 * 
			 * isAlive and no timeout
			 * @return true if we should be alive
			 */
			public boolean isRunning()
			{
				return isAlive() && System.currentTimeMillis() - start < 1000 * 60 * 2;
			}
		}

		public RequestQueueEntryHandler()
		{
			super(EnumType.RequestQueueEntry, new EnumFamily[] { EnumFamily.Command });
			numSubmitThread = 0;
			submitThreadMap = new HashMap<String, SubmitThread>();
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param m
		 * @param resp
		 * @return
		 */
		@Override
		public synchronized boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			// check for valid RequestQueueEntryParams
			final JDFRequestQueueEntryParams requestQEParams = m.getRequestQueueEntryParams(0);
			if (requestQEParams == null)
			{
				JMFHandler.errorResponse(resp, "QueueEntryParams missing in RequestQueueEntry message", 7, EnumClass.Error);
				return true;
			}
			final String queueURL = requestQEParams.getQueueURL();
			if (StringUtil.getNonEmpty(queueURL) == null)
			{
				JMFHandler.errorResponse(resp, "QueueURL is missing", 7, EnumClass.Error);
				return true;
			}

			final NodeIdentifier nid = requestQEParams.getIdentifier();
			EnumActivation activation = EnumActivation.getEnum(requestQEParams.getAttribute(AttributeName.ACTIVATION));
			// submit a specific QueueEntry
			IQueueEntry iqe = _theQueueProcessor.getWaitingQueueEntry(nid);
			EnumSubmitPolicy subPolicy = requestQEParams.getSubmitPolicy();
			if (iqe == null && (EnumActivation.Informative.equals(activation) || EnumSubmitPolicy.Force.equals(subPolicy)))
			{
				log.info("submitting non-standard busy qe");
				JDFQueueEntry qe = _theQueueProcessor.getQueueEntry(null, nid);
				iqe = _theQueueProcessor.getIQueueEntry(qe);
			}
			final JDFQueueEntry qe = iqe == null ? null : iqe.getQueueEntry();
			if (qe != null)
			{
				if (!EnumActivation.Informative.equals(activation))
				{
					qe.setDeviceID(m.getSenderID());
					waitSubmitThread(iqe.getQueueEntryID(), resp);
					SubmitThread submitThread = new SubmitThread(iqe, queueURL, activation);
					submitThreadMap.put(iqe.getQueueEntryID(), submitThread);
					submitThread.start();
				}
				else
				// informative needs no synch...
				{
					new SubmitThread(iqe, queueURL, activation).start();
				}
			}
			else if (qe == null)
			{
				JMFHandler.errorResponse(resp, "No QueueEntry is available for request: " + nid, 108, EnumClass.Error);
			}
			return true;
		}

		/**
		 * 
		 * wait for any previous submissions
		 * @param qeID 
		 * @param resp
		 */
		private void waitSubmitThread(String qeID, final JDFResponse resp)
		{
			SubmitThread submitThread = submitThreadMap.get(qeID);
			if (submitThread != null && submitThread.isRunning())
			{
				log.info("waiting for previous submit to complete");
				ThreadUtil.wait(submitThread, 30000);
				if (submitThread != null && submitThread.isRunning())
				{
					JMFHandler.errorResponse(resp, "Currently handling requestQueueEntry, try again later", 10, EnumClass.Warning);
				}
				else
				{
					log.info("finished waiting for previous submit to complete");
					submitThreadMap.remove(qeID);
				}
			}
		}
	}

	protected class ReturnQueueEntryHandler extends AbstractHandler
	{

		public ReturnQueueEntryHandler()
		{
			super(EnumType.ReturnQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param m the message to handle
		 * @param resp the response to fill
		 * @return true if handled
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null)
			{
				return false;
			}
			if (log.isDebugEnabled())
				log.debug("Handling " + m.getType());
			final JDFReturnQueueEntryParams retQEParams = m.getReturnQueueEntryParams(0);
			if (retQEParams == null)
			{
				JMFHandler.errorResponse(resp, "ReturnQueueEntryParams missing in ReturnQueueEntry message", 7, EnumClass.Error);
				return true;
			}
			final String slaveQueueEntryID = StringUtil.getNonEmpty(retQEParams.getQueueEntryID());
			log.info("Handling ReturnQueueEntry: " + slaveQueueEntryID);
			JDFDoc theDoc = retQEParams.getURLDoc();
			if (_slaveCallback != null)
			{
				theDoc = _slaveCallback.prepareJDFForBambi(theDoc);
			}
			if (theDoc == null)
			{
				log.error("No returned JDF in ReturnQueueEntry message: for slave queueentry: " + slaveQueueEntryID);
			}

			final AbstractProxyProcessor proc = getProcessorForReturnQE(retQEParams, resp, theDoc);
			if (proc != null)
			{
				final JDFQueueEntry qeBambi = getQueueProcessor().getQueueEntry(slaveQueueEntryID, null);
				getDataExtractor().extractFiles(qeBambi, theDoc);
				proc.returnFromSlave(m, resp, theDoc);
			}
			return true;
		}

		/**
		 * this one also does http, in case the slave provides files with http
		 * we want to have these files under control
		 *  
		 * @return the data extractor
		 */
		private DataExtractor getDataExtractor()
		{
			DataExtractor ex = ProxyDevice.this.getDataExtractor(false);
			ex.addProtocol(URLProtocol.http);
			return ex;
		}

		/**
		 * @param rqp returnqueueentryparams
		 * @param resp the response to fill
		 * @param theDoc the jdf doc that is returned
		 * @return the ProxyDeviceProcessor that handles messages from slaveQEID
		 */
		protected AbstractProxyProcessor getProcessorForReturnQE(final JDFReturnQueueEntryParams rqp, final JDFResponse resp, JDFDoc theDoc)
		{
			final String slaveQEID = rqp == null ? null : StringUtil.getNonEmpty(rqp.getQueueEntryID());
			AbstractProxyProcessor proc = getProcessorForSlaveQEID(slaveQEID);

			if (proc == null && theDoc != null)
			{
				final JDFNode node = theDoc.getJDFRoot();
				NodeIdentifier nid = node == null ? null : node.getIdentifier();
				proc = getProcessorForNID(nid);
				if (proc != null)
				{
					log.info("cannot find processor for qe: " + slaveQEID + " fallback to node: " + nid);
				}
			}
			if (proc == null)
			{
				final JDFNode node = theDoc.getJDFRoot();
				NodeIdentifier nid = node == null ? null : node.getIdentifier();
				final String errorMsg = "QueueEntry with slave QueueEntryID = " + slaveQEID + ", job identifier: " + nid == null ? " - " : nid + " is not being processed";
				JMFHandler.errorResponse(resp, errorMsg, 2, EnumClass.Error);
			}
			return proc;
		}
	}

	/**
	 * 
	 *  
	 * @author rainer prosi
	 * @date Jan 17, 2013
	 */
	protected class StatusQueryHandler extends StatusHandler
	{
		public StatusQueryHandler()
		{
			super();
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.AbstractDevice.StatusHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (statusContainer == null)
			{
				return false;
			}

			final JDFDoc docJMF = statusContainer.getStatusResponse(m);

			if (docJMF == null)
			{
				return super.handleMessage(m, resp); // idle is handled by super
			}

			final boolean bOK = copyPhaseTimeFromCounter(resp, docJMF);
			if (bOK)
			{
				addQueueToStatusResponse(m, resp);
			}
			return bOK;
		}

	}

	protected class NotificationQueryHandler extends AbstractHandler
	{
		public NotificationQueryHandler()
		{
			super(EnumType.Notification, new EnumFamily[] { EnumFamily.Query });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf. JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (statusContainer == null)
			{
				return false;
			}

			return statusContainer.fillNotifications(m, resp);
		}
	}// end of inner class NotificationQueryHandler

	protected class ResourceQueryHandler extends ResourceHandler
	{
		public ResourceQueryHandler()
		{
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf. JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (statusContainer == null)
			{
				return false;
			}

			final boolean bHandeled = statusContainer.fillResourceResponse(m, resp);
			return bHandeled ? bHandeled : super.handleMessage(m, resp);
		}
	}// end of inner class ResourceQueryHandler

	// //////////////////////////////////////////////////////////////////
	protected class NotificationSignalHandler extends SignalHandler
	{
		public NotificationSignalHandler()
		{
			super(ProxyDevice.this, EnumType.Notification, new EnumFamily[] { EnumFamily.Signal });
		}

		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (!super.handleMessage(m, resp))
			{
				return false;
			}
			log.debug("Handling " + m.getType() + " " + m.getID());
			final Vector<ProxyDeviceProcessor> v = getProxyProcessors();
			boolean b = false;
			if (v != null)
			{
				final int size = v.size();
				for (int i = 0; i < size; i++)
				{
					b = v.get(i).handleNotificationSignal(m, resp) || b;
				}
			}
			return true; // handled if any was ok
		}
	}// end of inner class ResourceQueryHandler

	// //////////////////////////////////////////////////////////////////
	protected class ResourceSignalHandler extends SignalHandler
	{
		public ResourceSignalHandler()
		{
			super(ProxyDevice.this, EnumType.Resource, new EnumFamily[] { EnumFamily.Signal });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf. JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (!super.handleMessage(m, resp))
			{
				return false;
			}
			log.debug("Handling " + m.getType() + " " + m.getID());
			final Vector<ProxyDeviceProcessor> v = getProxyProcessors();
			boolean b = false;
			if (v != null)
			{
				final int size = v.size();
				for (int i = 0; i < size; i++)
				{
					b = v.get(i).handleResourceSignal(m, resp) || b;
				}
			}
			return true; // handled if any was ok
		}
	}// end of inner class ResourceQueryHandler

	// //////////////////////////////////////////////////////////////////
	/**
	 * for now a dummy
	 */
	protected class QueueStatusSignalHandler extends SignalHandler
	{

		public QueueStatusSignalHandler()
		{
			super(ProxyDevice.this, EnumType.QueueStatus, new EnumFamily[] { EnumFamily.Signal });
		}

		/**
		 * @see SignalHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param inputMessage
		 * @param response
		 * @return
		 * TODO implement...
		*/
		@Override
		public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
		{
			super.handleMessage(inputMessage, response);
			response.setReturnCode(0);
			return true;
		}
	}

	// //////////////////////////////////////////////////////////////////
	protected class StatusSignalHandler extends SignalHandler
	{

		public StatusSignalHandler()
		{
			super(ProxyDevice.this, EnumType.Status, new EnumFamily[] { EnumFamily.Signal });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.SignalHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (!super.handleMessage(m, resp))
			{
				return false;
			}
			log.debug("Handling " + m.getType() + " " + m.getID());
			final Vector<ProxyDeviceProcessor> proxyProcessors = getProxyProcessors();
			boolean b = false;
			if (proxyProcessors != null)
			{
				for (ProxyDeviceProcessor devProc : proxyProcessors)
				{
					b = devProc.handleStatusSignal(m, resp) || b;
				}
			}
			if (!b)
			{
				final JDFStatusQuParams sqp = m.getStatusQuParams();
				String qeid = (sqp != null) ? sqp.getQueueEntryID() : null;
				NodeIdentifier ni = sqp == null ? null : sqp.getIdentifier();
				if (ni == null)
				{
					JDFDeviceInfo di = m.getDeviceInfo(0);
					JDFJobPhase jp = di == null ? null : di.getJobPhase(0);
					if (jp != null)
					{
						ni = jp.getIdentifier();
						if (qeid == null)
							qeid = jp.getQueueEntryID();
					}
				}
				if (KElement.isWildCard(qeid) && new NodeIdentifier().equals(ni))
				{
					b = handleIdle(m, resp);
				}

				if (!b)
				{
					final ProxyDeviceProcessor dp = getProcessorForSignal(qeid, ni);
					if (dp != null)
					{
						b = dp.handleStatusSignal(m, resp) || b;
					}
				}
				if (!b)
				{
					JMFHandler.errorResponse(resp, "Unknown QueueEntry: " + qeid, 103, EnumClass.Error);
				}
			}
			else
			{
				resp.setReturnCode(0);
			}

			return true; // handled if any was ok
		}

		/**
		 * @param m 
		 * @param resp 
		 * @return true if handled
		 */
		private boolean handleIdle(final JDFMessage m, final JDFResponse resp)
		{
			log.debug("handling idle status signal...");
			return true;
		}
	}// end of inner class StatusSignalHandler

	// //////////////////////////////////////////////////////////////////////////
	// ///////

	// ////////////////////////////////////////////////////////////////

	/**
	 * @param properties
	 */
	public ProxyDevice(final IDeviceProperties properties)
	{
		super(properties);
		final IProxyProperties proxyProperties = getProperties();
		statusContainer = new ResponseMerger();
		_jmfHandler.setFilterOnDeviceID(false);
		final int maxPush = proxyProperties.getMaxPush();
		if (maxPush > 0)
		{
			_theQueueProcessor.getQueue().setMaxRunningEntries(maxPush);
			// TODO correctly dispatch them
		}
	}

	/**
	 * @return vector of status listeners
	 */
	public Vector<StatusListener> getStatusListeners()
	{
		final Vector<ProxyDeviceProcessor> procs = getProxyProcessors();
		if (procs == null)
		{
			return null;
		}
		final int size = procs.size();
		if (size == 0)
		{
			return null;
		}
		final Vector<StatusListener> v = new Vector<StatusListener>(size);
		for (int i = 0; i < size; i++)
		{
			final ProxyDeviceProcessor pd = procs.get(i);
			final StatusListener l = pd.getStatusListener();
			if (l != null)
			{
				v.add(l);
			}
		}
		return v.size() == 0 ? null : v;

	}

	@Override
	protected void addHandlers()
	{
		super.addHandlers();
		_jmfHandler.addHandler(this.new RequestQueueEntryHandler());
		_jmfHandler.addHandler(this.new ReturnQueueEntryHandler());
		_jmfHandler.addHandler(this.new QueueStatusSignalHandler());
		_jmfHandler.addHandler(this.new StatusSignalHandler());
		_jmfHandler.addHandler(this.new StatusQueryHandler());
		_jmfHandler.addHandler(this.new ResourceQueryHandler());
		_jmfHandler.addHandler(this.new ResourceSignalHandler());
		_jmfHandler.addHandler(this.new NotificationSignalHandler());
		_jmfHandler.addHandler(this.new NotificationQueryHandler());
	}

	/**
	 * @param iqe
	 * @param slaveQueueURL
	 * @param activation 
	 * @return true if the processor is added
	 */
	public ProxyDeviceProcessor submitQueueEntry(final IQueueEntry iqe, final String slaveQueueURL, final EnumActivation activation)
	{
		ProxyDeviceProcessor pdp = new ProxyDeviceProcessor(this, iqe);
		pdp.setActivation(activation);
		if (EnumActivation.Active.equals(activation))
		{
			iqe.getQueueEntry().setStatusDetails(AbstractProxyDevice.SUBMITTING);
		}
		final boolean submit = pdp.submit(slaveQueueURL);
		if (submit && pdp.isActive())
		{
			// not needed - the dispache processor does the return stuff addProcessor(pdp);
		}
		else
		{
			pdp = null;
		}
		return pdp;
	}

	/**
	 * returns null, since the ProxyDevice doesn't need a DeviceProcessor
	 */
	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		if (getProperties().getMaxPush() <= 0)
		{
			return null;
		}
		return new ProxyDispatcherProcessor(this);
	}

	/**
	 * get a simple file name for the JDF in a queueentry
	 * @param qe the JDFQueueEntry to name mangle
	 * @return the file name for the jdf
	 */
	public String getNameFromQE(final JDFQueueEntry qe)
	{
		return "q" + qe.getQueueEntryID() + ".jdf";
	}

	/**
	 * gets the device processor for a given queueEntry
	 * @return the processor that is processing queueEntryID, null if none matches
	 */
	protected Vector<ProxyDeviceProcessor> getProxyProcessors()
	{
		final int size = _deviceProcessors.size();
		if (size == 0)
		{
			return null;
		}
		final Vector<ProxyDeviceProcessor> v = new Vector<ProxyDeviceProcessor>(size);
		for (int i = 0; i < size; i++)
		{
			final AbstractDeviceProcessor theDeviceProcessor = _deviceProcessors.get(i);
			if (theDeviceProcessor instanceof ProxyDeviceProcessor)
			{
				v.add((ProxyDeviceProcessor) theDeviceProcessor);
			}
		}
		return v.size() == 0 ? null : v;
	}

	/**
	 * @param slaveQEID 
	 * @param nid 
	 * @return the ProxyDeviceProcessor that handles messages from slaveQEID
	 */
	protected ProxyDeviceProcessor getProcessorForSignal(final String slaveQEID, final NodeIdentifier nid)
	{
		AbstractProxyProcessor proc = getProcessorForSlaveQEID(slaveQEID);
		if (proc == null)
		{
			proc = getProcessorForNID(nid);
		}
		if (!(proc instanceof ProxyDeviceProcessor))
		{
			final String errorMsg = "QueueEntry with ID=" + slaveQEID + " is not being processed";
			JMFHandler.errorResponse(null, errorMsg, 2, EnumClass.Error);
			proc = null;
		}
		return (ProxyDeviceProcessor) proc;
	}

	/**
	 * @param nid
	 * @return
	 */
	protected AbstractProxyProcessor getProcessorForNID(final NodeIdentifier nid)
	{
		if (nid == null)
		{
			return null;
		}
		final JDFQueueEntry qe = _theQueueProcessor.getQueueEntry(null, nid);
		final IQueueEntry iqe = _theQueueProcessor.getIQueueEntry(qe);
		return createExistingProcessor(iqe);
	}

	/**
	 * @param slaveQEID
	 * @return
	 */
	protected AbstractProxyProcessor getProcessorForSlaveQEID(final String slaveQEID)
	{
		final JDFQueueEntry qe = _theQueueProcessor.getQueueEntry(slaveQEID, null);
		AbstractProxyProcessor proc = qe == null ? null : (AbstractProxyProcessor) getProcessor(qe.getQueueEntryID(), 0);

		// we don't have an active proc, but this might be a multiple retQE - try to generate from old
		if (proc == null)
		{
			final IQueueEntry iqe = _theQueueProcessor.getIQueueEntry(qe);
			proc = createExistingProcessor(iqe);
		}
		return proc;
	}

	/**
	 * @deprecated - looks unused
	 * @param slaveQEID
	 * @return the bambi qeid for a given slave qeid
	 */
	@Deprecated
	protected String getIncomingQEID(final String slaveQEID)
	{
		final JDFQueueEntry qe = getQueueProcessor().getQueueEntry(slaveQEID, null);
		return qe == null ? null : qe.getQueueEntryID();
	}

	/**
	 * @param bambiQEID
	 * @return the queuentryID on the slave
	 */
	private String getSlaveQEID(final String bambiQEID)
	{
		if (bambiQEID == null || _deviceProcessors == null)
		{
			return null;
		}
		for (int i = 0; i < _deviceProcessors.size(); i++)
		{
			final AbstractDeviceProcessor aProc = _deviceProcessors.get(i);
			if (!(aProc instanceof ProxyDeviceProcessor))
			{
				continue;
			}
			final ProxyDeviceProcessor proc = (ProxyDeviceProcessor) aProc;
			IQueueEntry currentQE = proc.getCurrentQE();
			final String qeID = currentQE == null ? null : currentQE.getQueueEntryID();
			if (qeID != null && bambiQEID.equals(qeID))
			{
				return proc.getSlaveQEID();
			}
		}
		final JDFQueueEntry qe = _theQueueProcessor.getQueue().getQueueEntry(bambiQEID);
		return BambiNSExtension.getSlaveQueueEntryID(qe);
	}

	/**
	 * reload the queue
	 */
	@Override
	protected void reloadQueue()
	{
		final JDFQueue q = _theQueueProcessor.getQueue();
		BambiNSExtension.setSlaveURL(q, "true");
		final VElement qev = q.getQueueEntryVector();

		if (qev != null)
		{
			final int qSize = qev.size();
			for (int i = 0; i < qSize; i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) qev.get(i);
				final EnumQueueEntryStatus stat = qe.getQueueEntryStatus();
				if (!qe.isCompleted() && BambiNSExtension.getSlaveQueueEntryID(qe) != null)
				{
					final IQueueEntry iqe = _theQueueProcessor.getIQueueEntry(qe);
					if (iqe == null)
					{
						log.error("no Queue entry refreshing queue " + qe.getQueueEntryID() + " Status= " + stat == null ? "unknown" : stat.getName());
					}
					else
					{
						createExistingProcessor(iqe);
					}
				}
			}
		}
		final JDFJMF jmfQS = new JMFBuilder().buildQueueStatus();
		sendJMFToSlave(jmfQS, new QueueSynchronizeHandler(jmfQS));
	}

	/**
	 * @param iqe the queueentry to check for
	 * @return 
	 */
	protected AbstractProxyProcessor createExistingProcessor(final IQueueEntry iqe)
	{
		final JDFQueueEntry qe = iqe == null ? null : iqe.getQueueEntry();
		if (qe == null) // redundant but eclipse nags about potential npa otherwise...
		{
			return null;
		}
		AbstractProxyProcessor pdp = (ProxyDeviceProcessor) getProcessor(iqe.getQueueEntryID(), 0);
		if (pdp != null)
			return pdp;

		pdp = createNewDeviceProcessor(iqe);
		pdp.submitted(BambiNSExtension.getSlaveQueueEntryID(qe), qe.getQueueEntryStatus(), BambiNSExtension.getDeviceURL(qe), qe.getDeviceID());
		addProcessor(pdp);
		return pdp;
	}

	/**
	 * 
	 *  
	 * @param iqe
	 * @return
	 */
	protected AbstractProxyProcessor createNewDeviceProcessor(final IQueueEntry iqe)
	{
		return new ProxyDeviceProcessor(this, iqe);
	}

	/**
	 * clean up any queueEntries that may still pretend to be running if only one is allowed
	 * 
	 * @param slaveDeviceID the slave device id to clean up for
	 */
	void cleanupMultipleRunning(String ignoreQEID, String slaveDeviceID)
	{
		int maxRun = getProperties().getMaxSlaveRunning();
		if (maxRun <= 1)
		{
			JDFAttributeMap attMap = new JDFAttributeMap(AttributeName.DEVICEID, slaveDeviceID);
			VElement queues = _theQueueProcessor.getQueue().getQueueEntryVector(attMap, null);
			if (queues != null && queues.size() > 1)
			{
				for (KElement e : queues)
				{
					JDFQueueEntry qe = (JDFQueueEntry) e;
					String queueEntryID = qe.getQueueEntryID();
					if (ignoreQEID.equals(queueEntryID))
						continue;
					ProxyDeviceProcessor pdp = (ProxyDeviceProcessor) getProcessor(queueEntryID, 0);
					if (pdp == null)
						pdp = new ProxyDeviceProcessor(this, new QueueEntry(null, qe));

					log.warn("cleaning up multiple running entries: " + queueEntryID);
					pdp.finalizeProcessDoc(EnumQueueEntryStatus.Waiting);
				}
			}
		}
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#getNodeFromDoc(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public JDFNode getNodeFromDoc(final JDFDoc doc)
	{
		return doc.getJDFRoot();
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#stopProcessing(java.lang.String, org.cip4.jdflib.core.JDFElement.EnumNodeStatus)
	 */
	@Override
	public JDFQueueEntry stopProcessing(final String queueEntryID, final EnumNodeStatus status)
	{
		if (status == null)
		{
			JMFBuilder jmfBuilder = JMFBuilderFactory.getJMFBuilder(getDeviceID());
			final JDFJMF jmf = jmfBuilder.buildRemoveQueueEntry(getSlaveQEID(queueEntryID));
			if (jmf != null)
			{
				final QueueEntryAbortHandler ah = new QueueEntryAbortHandler(status, jmf.getCommand(0).getID());
				sendJMFToSlave(jmf, ah);
			}
		}
		final JDFQueueEntry qe = super.stopProcessing(queueEntryID, status);
		return qe;
	}
}