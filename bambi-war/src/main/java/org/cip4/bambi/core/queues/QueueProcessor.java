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
package org.cip4.bambi.core.queues;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.mail.Multipart;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.DataExtractor;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IDeviceProperties.QERetrieval;
import org.cip4.bambi.core.IDeviceProperties.QEReturn;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.messaging.AcknowledgeThread;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.SignalDispatcher;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumUpdateGranularity;
import org.cip4.jdflib.auto.JDFAutoSubmissionMethods.EnumPackaging;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFComment;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFFlushQueueInfo;
import org.cip4.jdflib.jmf.JDFFlushQueueParams;
import org.cip4.jdflib.jmf.JDFIDInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFNewJDFQuParams;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueue.ExecuteCallback;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueEntryDef;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFResubmissionParams;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFSubmissionMethods;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.RollingBackupFile;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;
import org.cip4.jdflib.util.thread.DelayedPersist;
import org.cip4.jdflib.util.thread.IPersistable;
import org.cip4.jdflib.util.thread.MutexMap;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * 
 * @author rainer prosi
 * 
 * 
 */
public class QueueProcessor extends BambiLogFactory implements IPersistable
{

	/**
	 * class that handles queue differences
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * July 6, 2009
	 */
	protected class QueueDelta
	{
		protected JDFQueue lastQueue;
		private final long creationTime;

		/**
		 * 
		 */
		public QueueDelta()
		{
			lastQueue = cloneQueue();
			creationTime = System.currentTimeMillis();
		}

		/**
		 * clean up stored queues that have not been touched for a while
		 */
		protected void cleanOrphans()
		{
			final Vector<String> v = ContainerUtil.getKeyVector(deltaMap);
			if (v != null)
			{
				for (int i = 0; i < v.size(); i++)
				{
					final String key = v.get(i);
					final QueueDelta delta = deltaMap.get(key);
					if (delta.isOrphan())
					{
						deltaMap.remove(key);
					}
				}
			}
		}

		/**
		 * @return true if we haven't touched this guy for a while
		 */
		private boolean isOrphan()
		{
			return System.currentTimeMillis() - creationTime > 1000 * 60 * 66; // retain a bit over an hour
		}
	}

	/**
	 * class to quickly retrieve queueentries based on slave qeids - required by proxies
	 * 
	 * @author Rainer Prosi, Heidelberger Druckmaschinen  
	 */
	protected class QueueMap
	{
		private final HashMap<String, JDFQueueEntry> qeIDMap;
		private final HashMap<NodeIdentifier, JDFQueueEntry> niMap;
		private final HashSet<NodeIdentifier> niNull;

		/**
		 * 
		 */
		public QueueMap()
		{
			qeIDMap = new HashMap<String, JDFQueueEntry>();
			niMap = new HashMap<NodeIdentifier, JDFQueueEntry>();
			niNull = new HashSet<NodeIdentifier>();
			fill(_theQueue);
		}

		/**
		 * @param queue
		 */
		protected void fill(final JDFQueue queue)
		{
			if (queue == null)
			{
				return;
			}
			synchronized (queue)
			{
				final VElement v = queue.getQueueEntryVector();
				for (KElement e : v)
				{
					final JDFQueueEntry qe = (JDFQueueEntry) e;
					addEntry(qe, false);
				}
			}
		}

		/**
		 * @param qe
		 * @param clearNull
		 */
		protected void addEntry(final JDFQueueEntry qe, final boolean clearNull)
		{
			final NodeIdentifier ni = qe.getIdentifier();
			niMap.put(ni, qe);
			final String slaveqeID = BambiNSExtension.getSlaveQueueEntryID(qe);
			if (slaveqeID != null)
			{
				qeIDMap.put(slaveqeID, qe);
			}
			if (clearNull)
			{
				niNull.clear();
			}
		}

		/**
		 * 
		 *  
		 * @param ni
		 * @return
		 */
		protected JDFQueueEntry getQEFromNI(final NodeIdentifier ni)
		{
			if (ni == null)
			{
				return null;
			}
			JDFQueueEntry qe = niMap.get(ni);
			if (qe == null)
			{
				if (niNull.contains(ni))
				{
					return null;
				}
				qe = _theQueue.getQueueEntry(ni, 0);
				if (qe != null)
				{
					niMap.put(ni, qe);
				}
				else
				{
					niNull.add(ni);
				}
			}
			return qe;
		}

		/**
		 * 
		 * @param slaveqeID the qeID in the slave system
		 * @return the local queueentry
		 */
		protected JDFQueueEntry getQEFromSlaveQEID(final String slaveqeID)
		{
			if (slaveqeID == null)
			{
				return null;
			}
			final JDFQueueEntry qe = qeIDMap.get(slaveqeID);
			return qe;
		}

		/**
		 * 
		 */
		public void reset()
		{
			qeIDMap.clear();
			niMap.clear();
			niNull.clear();
		}

		/**
		 * remove a slave qe from the map
		 * @param qe
		 */
		protected void removeEntry(JDFQueueEntry qe)
		{
			final String slaveqeID = BambiNSExtension.getSlaveQueueEntryID(qe);
			if (slaveqeID != null)
			{
				qeIDMap.remove(slaveqeID);
			}
		}

		/**
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "QueueMap [size= " + qeIDMap.size() + " null size= " + niNull.size() + " ]";
		}
	}

	/**
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * July 6, 2009
	 */
	protected class CanExecuteCallBack extends ExecuteCallback
	{

		/**
		 * 
		 * @param _deviceID
		 * @param _proxy
		 */
		protected CanExecuteCallBack(final String _deviceID, final String _proxy)
		{
			super();
			this.deviceID = _deviceID;
			this.proxy = _proxy;
		}

		String deviceID;
		String proxy;

		/** 
		 * @see org.cip4.jdflib.jmf.JDFQueue.ExecuteCallback#canExecute(org.cip4.jdflib.jmf.JDFQueueEntry)
		 */
		@Override
		public boolean canExecute(final JDFQueueEntry qe)
		{
			if (proxy != null && qe.hasAttribute(proxy))
			{
				return false;
			}
			if (deviceID != null && !KElement.isWildCard(qe.getDeviceID()) && !deviceID.equals(qe.getDeviceID()))
			{
				return false;
			}
			final String badDevices = BambiNSExtension.getMyNSAttribute(qe, BambiNSExtension.GOOD_DEVICES);
			if (!StringUtil.hasToken(badDevices, deviceID, " ", 0))
			{
				return false;
			}
			return true;
		}

		/**
		 * @see java.lang.Object#clone()
		 * @return my clone...
		 */
		@Override
		protected CanExecuteCallBack clone()
		{
			final CanExecuteCallBack cb = new CanExecuteCallBack(deviceID, proxy);
			return cb;
		}
	}

	/**
	 * 
	 * 
	 * @author rainer prosi
	 * @date before Feb 20, 2013
	 */
	protected class SubmitQueueEntryHandler extends AbstractHandler
	{
		public SubmitQueueEntryHandler()
		{
			super(EnumType.SubmitQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling  SubmitQueueEntry");
			final JDFQueueSubmissionParams qsp = m.getQueueSubmissionParams(0);
			if (qsp != null)
			{
				JDFDoc doc = qsp.getURLDoc();
				if (doc == null)
				{
					updateEntry(null, null, m, resp, null);
					String errorMsg = "failed to get JDFDoc from '" + qsp.getURL() + "' on SubmitQueueEntry";
					errorMsg += "\nin thread: " + Thread.currentThread().getName();
					JMFHandler.errorResponse(resp, errorMsg, 9, EnumClass.Error);
					return true;
				}
				final IConverterCallback callback = _parentDevice.getCallback(null);
				if (callback != null)
				{
					doc = callback.prepareJDFForBambi(doc);
				}

				final JDFQueueEntry qe = addEntry((JDFCommand) m, resp, doc);
				final int rc = resp.getReturnCode();

				if (rc != 0)
				{
					if (rc == 112)
					{
						JMFHandler.errorResponse(resp, "Submission failed - queue is not accepting new submissions", rc, EnumClass.Error);
					}
					else if (rc == 116)
					{
						JMFHandler.errorResponse(resp, "Submission failed - identical queue entry exists", rc, EnumClass.Error);
					}
					else
					{
						JMFHandler.errorResponse(resp, "failed to add entry: invalid or missing message parameters", rc, EnumClass.Error);
					}

					return true; // error was filled by handler
				}
				if (qe == null)
				{
					return true;
				}
				else
				{
					resp.removeChild(ElementName.QUEUEENTRY, null, 0);
					final JDFQueueEntry qeNew = (JDFQueueEntry) resp.copyElement(qe, null);
					BambiNSExtension.removeBambiExtensions(qeNew);
					updateEntry(qe, null, m, resp, null);
				}
				return true;
			}
			JMFHandler.errorResponse(resp, "QueueSubmissionParams are missing or invalid", 9, EnumClass.Error);
			log.error("QueueSubmissionParams are missing or invalid");
			return true;
		}
	}

	/**
	 * handler for the resubmitqueueentry message
	 * @author rainer prosi
	 * @date Nov 13, 2011
	 */
	protected class ResubmitQueueEntryHandler extends AbstractHandler
	{

		public ResubmitQueueEntryHandler()
		{
			super(EnumType.ResubmitQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling  ResubmitQueueEntry");
			final JDFResubmissionParams qsp = m.getResubmissionParams(0);
			if (qsp != null)
			{
				final String qeID = qsp.getQueueEntryID();
				final JDFQueueEntry qe = _theQueue.getQueueEntry(qeID);
				if (qe == null)
				{
					JMFHandler.errorResponse(resp, "unknown QueueEntryID: " + qeID, 105, EnumClass.Error);
				}
				else
				{
					JDFDoc doc = qsp.getURLDoc();
					if (doc == null)
					{
						handleInvalidURL(m, resp, qsp);
					}
					else
					{
						handleValidURL(m, resp, qe, doc);
					}
				}
			}
			else
			{
				JMFHandler.errorResponse(resp, "ResubmissionParams are missing or invalid", 9, EnumClass.Error);
				log.error("ResubmissionParams are missing or invalid");
			}
			return true;
		}

		/**
		 * @param m
		 * @param resp
		 * @param qe
		 * @param doc
		 */
		private void handleValidURL(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe, JDFDoc doc)
		{
			final IConverterCallback callback = _parentDevice.getCallback(null);
			if (callback != null)
			{
				doc = callback.prepareJDFForBambi(doc);
			}
			final String qeID = qe.getQueueEntryID();
			final VString canAccept = canAccept(doc.getJDFRoot(), qeID);
			if (canAccept == null)
			{
				JMFHandler.errorResponse(resp, "unable to queue request", 101, EnumClass.Error);
			}
			else
			{
				updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp, null);
				_parentDevice.fixEntry(qe, doc);
				_parentDevice.getDataExtractor(true).extractFiles(qe, doc);
				storeDoc(qe, doc, null, null);
			}
		}

		/**
		 * @param m
		 * @param resp
		 * @param qsp
		 */
		private void handleInvalidURL(final JDFMessage m, final JDFResponse resp, final JDFResubmissionParams qsp)
		{
			updateEntry(null, null, m, resp, null);
			String errorMsg = "failed to read JDFDoc from '" + qsp.getURL() + "' on SubmitQueueEntry";
			errorMsg += "\r\nin thread: " + Thread.currentThread().getName();
			JMFHandler.errorResponse(resp, errorMsg, 9, EnumClass.Error);
		}
	}

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * July 10, 2009
	 */
	public class NewJDFQueryHandler extends AbstractHandler
	{
		/**
		 * 
		 */
		public NewJDFQueryHandler()
		{
			super(EnumType.NewJDF, new EnumFamily[] { EnumFamily.Query });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			final JDFNewJDFQuParams nqParams = m.getNewJDFQuParams(0);
			if (nqParams == null || !nqParams.hasAttribute(AttributeName.JOBID))
			{
				JMFHandler.errorResponse(resp, "missing or insufficient NewJDFQuParams", 7, EnumClass.Error);
				return false;
			}
			final String qeID = StringUtil.getNonEmpty(nqParams.getQueueEntryID());
			JDFQueueEntry qe = null;
			if (qeID != null)
			{
				qe = _theQueue.getQueueEntry(qeID);
			}
			final NodeIdentifier identifier = nqParams.getIdentifier();
			if (qe == null)
			{
				qe = _theQueue.getQueueEntry(identifier, 0);
			}
			if (qe == null && nqParams.hasAttribute(AttributeName.JOBPARTID))
			{
				qe = _theQueue.getQueueEntry(new NodeIdentifier(nqParams.getJobID(), null, null), 0);
			}
			if (qe == null)
			{
				log.warn("could not find queueentry for " + identifier);
				return false;
			}
			final IQueueEntry iqe = getIQueueEntry(qe);
			JDFNode n = iqe.getJDF();
			if (n == null)
			{
				log.error("could not find JDF node in queue for " + identifier);
				return false;
			}
			n = n.getJobPart(identifier);
			if (n == null)
			{
				log.warn("could not find JDF node in JDF for " + identifier);
				return false;
			}
			final JDFIDInfo idi = JDFIDInfo.createFromJDF(n, resp);
			log.info("NewJDF handled for " + idi.getJobPartID());
			return true;
		}

	}

	/**
	 * public in order to enable reference from updating devices
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 03.12.2008
	 */
	public class QueueStatusHandler extends AbstractHandler
	{
		/**
		 * 
		 */
		public QueueStatusHandler()
		{
			super(EnumType.QueueStatus, new EnumFamily[] { EnumFamily.Query });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			final JDFQueue q = copyToMessage(m, resp);
			return (resp.getQueue(0) == q);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#isSubScribable()
		 */
		@Override
		public boolean isSubScribable()
		{
			return true;
		}
	}

	protected class RemoveQueueEntryHandler extends AbstractHandler
	{
		public RemoveQueueEntryHandler()
		{
			super(EnumType.RemoveQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			final JDFQueueEntry qe = getMessageQueueEntry(m, resp);
			if (qe == null)
			{
				return true;
			}
			final String qeid = qe.getQueueEntryID();
			EnumQueueEntryStatus status = qe.getQueueEntryStatus();

			if (EnumQueueEntryStatus.Held.equals(status) || EnumQueueEntryStatus.Waiting.equals(status))
			{
				abortQueueEntry(m, resp); // abort before removing
			}
			status = qe.getQueueEntryStatus();
			if (EnumQueueEntryStatus.Held.equals(status) || EnumQueueEntryStatus.Waiting.equals(status) || EnumQueueEntryStatus.Completed.equals(status)
					|| EnumQueueEntryStatus.Aborted.equals(status) || EnumQueueEntryStatus.Suspended.equals(status))
			{
				final String queueEntryID = qe.getQueueEntryID();
				JDFQueueEntry returnQE = _parentDevice.stopProcessing(queueEntryID, null, null); // use null to flag a removal
				if (returnQE == null)
					returnQE = qe;
				updateEntry(returnQE, EnumQueueEntryStatus.Removed, m, resp, null);
				log.info("removed QueueEntry with ID=" + qeid);
			}
			else
			{
				final String statName = status.getName();
				updateEntry(qe, status, m, resp, null);
				JMFHandler.errorResponse(resp, "cannot remove QueueEntry with ID=" + qeid + ", current Status=" + statName, 106, EnumClass.Error);
			}
			return true;
		}
	}

	protected class HoldQueueEntryHandler extends AbstractHandler
	{

		public HoldQueueEntryHandler()
		{
			super(EnumType.HoldQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			final JDFQueueEntry qe = getMessageQueueEntry(m, resp);
			if (qe == null)
			{
				return true;
			}
			final String qeid = qe.getQueueEntryID();
			final EnumQueueEntryStatus status = qe.getQueueEntryStatus();

			if (EnumQueueEntryStatus.Waiting.equals(status))
			{
				updateEntry(qe, EnumQueueEntryStatus.Held, m, resp, null);
				log.info("held QueueEntry with ID=" + qeid);
			}
			else
			{
				updateEntry(qe, status, m, resp, null);
				if (EnumQueueEntryStatus.Held.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot suspend QueueEntry with ID=" + qeid + ", it is already held", 113, EnumClass.Error);
				}
				else if (EnumQueueEntryStatus.Running.equals(status) || EnumQueueEntryStatus.Suspended.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot hold QueueEntry with ID=" + qeid + ", current Status=" + status.getName(), 106, EnumClass.Error);
				}

				else if (EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot hold QueueEntry with ID=" + qeid + ", current Status already=" + status.getName(), 114, EnumClass.Error);
				}
				else
				{
					log.error("what happened? current Status=" + (status == null ? "null" : status.getName()));
					return false; // ???
				}
			}
			return true;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	protected class HoldQueueHandler extends ModifyQueueStatusHandler
	{
		@Override
		protected EnumQueueStatus getNewStatus()
		{
			return _theQueue.holdQueue();
		}

		public HoldQueueHandler()
		{
			super(EnumType.HoldQueue);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////
	protected class CloseQueueHandler extends ModifyQueueStatusHandler
	{
		@Override
		protected EnumQueueStatus getNewStatus()
		{
			return _theQueue.closeQueue();
		}

		public CloseQueueHandler()
		{
			super(EnumType.CloseQueue);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	protected class OpenQueueHandler extends ModifyQueueStatusHandler
	{
		@Override
		protected EnumQueueStatus getNewStatus()
		{
			return _theQueue.openQueue();
		}

		public OpenQueueHandler()
		{
			super(EnumType.OpenQueue);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	protected class ResumeQueueHandler extends ModifyQueueStatusHandler
	{
		@Override
		protected EnumQueueStatus getNewStatus()
		{
			return _theQueue.resumeQueue();
		}

		public ResumeQueueHandler()
		{
			super(EnumType.ResumeQueue);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 03.12.2008
	 */
	protected abstract class ModifyQueueStatusHandler extends AbstractHandler
	{
		protected abstract EnumQueueStatus getNewStatus();

		/**
		 * @param _type
		 */
		public ModifyQueueStatusHandler(final EnumType _type)
		{
			super(_type, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			final EnumQueueStatus newStatus = getNewStatus();

			synchronized (_theQueue)
			{
				if (!ContainerUtil.equals(newStatus, _theQueue.getQueueStatus()))
				{
					_theQueue.setQueueStatus(newStatus);
				}
				updateEntry(null, null, m, resp, null);
			}
			return true;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////

	protected class AbortQueueEntryHandler extends AbstractHandler
	{

		public AbortQueueEntryHandler()
		{
			super(EnumType.AbortQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			return abortQueueEntry(m, resp);
		}
	}

	protected class ResumeQueueEntryHandler extends AbstractHandler
	{

		public ResumeQueueEntryHandler()
		{
			super(EnumType.ResumeQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			final EnumType typ = m.getEnumType();
			if (EnumType.ResumeQueueEntry.equals(typ))
			{
				final JDFQueueEntry qe = getMessageQueueEntry(m, resp);
				if (qe == null)
				{
					return true;
				}
				final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
				final String qeid = qe.getQueueEntryID();

				if (EnumQueueEntryStatus.Suspended.equals(status))
				{
					updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp, null);
					log.info("resumed QueueEntry to waiting with ID=" + qeid);
					return true;
				}
				else if (EnumQueueEntryStatus.Held.equals(status))
				{
					updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp, null);
					log.info("resumed QueueEntry with ID=" + qeid);
					return true;
				}

				if (EnumQueueEntryStatus.Running.equals(status))
				{
					updateEntry(qe, status, m, resp, null);
					JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID=" + qeid + ", it is " + status.getName(), 113, EnumClass.Error);
					return true;
				}

				if (EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
				{
					updateEntry(qe, status, m, resp, null);
					JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID=" + qeid + ", it is already " + status.getName(), 115, EnumClass.Error);
					return true;
				}
			}

			return false;
		}
	}

	protected class FlushQueueHandler extends AbstractHandler
	{

		public FlushQueueHandler()
		{
			super(EnumType.FlushQueue, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			final JDFFlushQueueParams fqp = m.getFlushQueueParams(0);
			final JDFQueueFilter qf = fqp == null ? null : fqp.getQueueFilter();
			final JDFQueueFilter qfo = m.getQueueFilter(0);
			final VElement zapped = _theQueue.flushQueue(qf);
			_theQueue.copyToResponse(resp, qfo, null);
			final JDFFlushQueueInfo flushQueueInfo = resp.appendFlushQueueInfo();
			flushQueueInfo.setQueueEntryDefsFromQE(zapped);
			persist(0);

			removeOrphanJDFs();

			return true;
		}

	}

	protected class QueueGetHandler implements IGetHandler
	{
		private int nPos;

		/**
		 * 
		 */
		public QueueGetHandler()
		{
			super();
			nPos = 0;
		}

		private static final String FILTER_DIF = "_FILTER_DIF_";

		/**
		 * @param request
		 * @return the xmlresponse to the get request
		 * 
		 */
		@Override
		public XMLResponse handleGet(final ContainerRequest request)
		{
			boolean modified = false;
			String sortBy = StringUtil.getNonEmpty(request.getParameter("SortBy"));
			final String filter = StringUtil.getNonEmpty(request.getParameter("filter"));
			nPos = request.getIntegerParam("pos");
			if (request.isMyContext("showQueue"))
			{
				modified = applyModification(request, modified);
			}
			else if (request.isMyContext("modifyQE"))
			{
				updateQE(request);
				modified = true;
				// ensure identical sorting as last time by undoing the sort inversion
				sortBy = lastSortBy;
				nextinvert = nextinvert == null ? lastSortBy : null;
			}
			else
			{
				return null;
			}
			final JDFQueue root;

			if (request.getBooleanParam("quiet") == false)
			{
				root = sortOutput(sortBy, filter);
				root.setAttribute(AttributeName.CONTEXT, request.getContextRoot());
				final QERetrieval qer = _parentDevice.getProperties().getQERetrieval();
				root.setAttribute("Pull", qer == QERetrieval.PULL || qer == QERetrieval.BOTH, null);
				if (_theQueue.numChildElements(ElementName.QUEUEENTRY, null) < 500)
				{
					root.setAttribute("Refresh", true, null);
				}
				root.setAttribute("pos", nPos, null);
				root.getOwnerDocument_JDFElement().setXSLTURL(_parentDevice.getXSLT(request));
				addOptions(root);
			}
			else
			{
				final XMLDoc doc = new JDFDoc(ElementName.QUEUE);
				root = (JDFQueue) doc.getRoot();

			}
			XMLResponse response = new XMLResponse(root);
			if (modified)
			{
				persist(300000);
			}
			return response;
		}

		/**
		 * @param request
		 * @param modified
		 * @return
		 */
		protected boolean applyModification(final ContainerRequest request, boolean modified)
		{
			final EnumQueueStatus qStatus = _theQueue.getQueueStatus();
			EnumQueueStatus qStatusNew = null;
			final boolean bHold = request.getBooleanParam("hold");
			if (bHold)
			{
				qStatusNew = applyHold();
			}
			final boolean bClose = request.getBooleanParam("close");
			if (bClose)
			{
				qStatusNew = applyClose();
			}
			final boolean bResume = request.getBooleanParam("resume");
			if (bResume)
			{
				qStatusNew = applyResume();
			}
			final boolean bOpen = request.getBooleanParam("open");
			if (bOpen)
			{
				qStatusNew = applyOpen();
			}
			final boolean bFlush = request.getBooleanParam("flush");
			if (bFlush)
			{
				final VElement v = applyFlush();
				modified = v != null;
			}
			if (qStatusNew != null)
			{
				modified = modified || !ContainerUtil.equals(qStatusNew, qStatus);
			}

			return modified;
		}

		protected VElement applyFlush()
		{
			final VElement v = _theQueue.flushQueue(null);
			return v;
		}

		protected EnumQueueStatus applyOpen()
		{
			EnumQueueStatus qStatusNew;
			qStatusNew = _theQueue.openQueue();
			return qStatusNew;
		}

		protected EnumQueueStatus applyResume()
		{
			EnumQueueStatus qStatusNew;
			qStatusNew = _theQueue.resumeQueue();
			return qStatusNew;
		}

		protected EnumQueueStatus applyClose()
		{
			EnumQueueStatus qStatusNew;
			qStatusNew = _theQueue.closeQueue();
			return qStatusNew;
		}

		protected EnumQueueStatus applyHold()
		{
			EnumQueueStatus qStatusNew;
			qStatusNew = _theQueue.holdQueue();
			return qStatusNew;
		}

		/**
		 * the filter is case insensitive
		 * @param sortBy
		 * @param filter the regexp to filter by (.)* is added before and after the filter
		 * @return
		 */
		protected JDFQueue sortOutput(final String sortBy, final String filter)
		{
			JDFQueue root = filterList(filter);
			// sort according to the given attribute
			if (sortBy != null)
			{
				boolean invert = sortBy.equals(lastSortBy) && sortBy.equals(nextinvert);
				nextinvert = invert ? null : sortBy;
				lastSortBy = sortBy;
				if (sortBy.endsWith("Time"))
				{
					invert = !invert; // initial sort should be late first
				}
				root.sortChildren(new KElement.SingleAttributeComparator(sortBy, invert));
			}
			else
			{
				nextinvert = null;
				lastSortBy = null;
			}
			return root;
		}

		/**
		 * filter the queue by string
		 * @param root
		 * @param filter
		 * @return
		 */
		protected JDFQueue filterList(String filter)
		{
			final JDFQueue root;
			if (FILTER_DIF.equals(filter))
			{
				final JDFQueue lastQueue = getLastQueue(filter);
				if (lastQueue != null)
				{
					final JDFQueueFilter f = (JDFQueueFilter) new JDFDoc(ElementName.QUEUEFILTER).getRoot();
					f.setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
					root = f.copy(_theQueue, lastQueue, null);
				}
				else
				{
					root = cloneQueue();
				}
			}
			else if (filter != null)
			{
				root = cloneQueue();
				root.setAttribute("filter", filter);
				boolean invert = "!".equals(StringUtil.leftStr(filter, 1));
				if (invert)
				{
					filter = StringUtil.rightStr(filter, -1);
					if (filter == null)
					{
						filter = "";
					}
				}
				if (!"*".equals(StringUtil.leftStr(filter, 1)))
					filter = "*" + filter;
				if (!"*".equals(StringUtil.rightStr(filter, 1)))
					filter += "*";
				final Collection<JDFQueueEntry> v = root.getAllQueueEntry();
				for (KElement e : v)
				{
					if (StringUtil.matchesIgnoreCase(e.toDisplayXML(0), filter) == invert)
					{
						e.deleteNode();
					}
				}
			}
			else
			{
				root = cloneQueue();
			}
			final VElement v = root.getChildElementVector(ElementName.QUEUEENTRY, null);
			final int size = v.size();
			root.setAttribute("TotalQueueSize", size, null);
			if (nPos < 0)
			{
				nPos = nPos + 1 + size / 500;
			}
			if ((nPos + 1) * 500 < size)
			{
				root.setAttribute("hasNext", true, null);
			}
			for (int i = 0; i < size; i++)
			{
				if (filterLength(i))
				{
					v.get(i).deleteNode();
				}
			}
			return root;
		}

		/**
		 * TODO add support for next
		 * @param i
		 * @return
		 */
		protected boolean filterLength(final int i)
		{
			return i < nPos * 500 || i > (nPos + 1) * 500; // performance...
		}

		/**
		 * @param request
		 */
		protected void updateQE(final ContainerRequest request)
		{
			final String qeID = request.getParameter(QE_ID);
			if (qeID == null)
			{
				return;
			}

			JDFQueueEntry qe = _theQueue.getQueueEntry(qeID);
			if (qe == null)
			{
				getLog().warn("invalid queuentryID in get request: qeid= null");
				return;
			}
			if (request.getBooleanParam("submit"))
			{
				if (nextPush == null)
				{
					nextPush = qe;
					notifyListeners(qe.getQueueEntryID());
					ThreadUtil.sleep(500); // give the notified thread a moment to submit
				}
				else
				{
					getLog().warn("attempting to queue more than one job: qeid= " + qe.getQueueEntryID());
				}
				return;
			}
			final EnumQueueEntryStatus status = EnumQueueEntryStatus.getEnum(request.getParameter(QE_STATUS));
			if (status == null)
			{
				return;
			}
			// also stop the device processor
			JDFQueueEntry qe2 = null;
			if (EnumQueueEntryStatus.Completed.equals(status))
			{
				qe2 = _parentDevice.stopProcessing(qeID, EnumNodeStatus.Completed, null);
			}
			else if (EnumQueueEntryStatus.Aborted.equals(status))
			{
				qe2 = _parentDevice.stopProcessing(qeID, EnumNodeStatus.Aborted, null);
			}
			else if (EnumQueueEntryStatus.Suspended.equals(status))
			{
				qe2 = _parentDevice.stopProcessing(qeID, EnumNodeStatus.Suspended, null);
			}
			if (qe2 != null)
			{
				qe = qe2;
			}

			if ((EnumQueueEntryStatus.Aborted.equals(qe.getQueueEntryStatus()) || EnumQueueEntryStatus.Completed.equals(qe.getQueueEntryStatus())))
			{
				returnQueueEntry(qe, null, null, status);
			}
			updateEntry(qe, status, null, null, null);
		}

		/**
		 * @param q
		 * 
		 */
		protected void addOptions(final JDFQueue q)
		{
			final Collection<JDFQueueEntry> v = q.getAllQueueEntry();
			if (v != null)
			{
				for (final JDFQueueEntry qe : v)
				{
					Vector<EnumQueueEntryStatus> nextStatusVector = qe.getNextStatusVector();
					EnumQueueEntryStatus queueEntryStatus = qe.getQueueEntryStatus();
					if (!EnumQueueEntryStatus.Running.equals(queueEntryStatus))
						nextStatusVector.remove(EnumQueueEntryStatus.Running);
					XMLResponse.addOptionList(queueEntryStatus, nextStatusVector, qe, QE_STATUS);
					BambiNSExtension.setMyNSAttribute(qe, "QueueEntryURL", UrlUtil.escape(qe.getQueueEntryID(), true));
				}
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////

	protected class SuspendQueueEntryHandler extends AbstractHandler
	{

		public SuspendQueueEntryHandler()
		{
			super(EnumType.SuspendQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param m
		 * @param resp
		 * @return true if handled
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				log.error("null message or response; bailing out ");
				return false;
			}
			log.debug("Handling " + m.getType());
			final JDFQueueEntry qe = getMessageQueueEntry(m, resp);
			if (qe == null)
			{
				return true;
			}
			final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
			final String statusDetails = StringUtil.getNonEmpty(qe.getStatusDetails());
			final String qeid = qe.getQueueEntryID();

			if (EnumQueueEntryStatus.Running.equals(status))
			{
				_parentDevice.stopProcessing(qeid, EnumNodeStatus.Suspended, statusDetails);
				updateEntry(qe, EnumQueueEntryStatus.Suspended, m, resp, null);
				log.info("suspended QueueEntry with ID=" + qeid);
			}
			else
			{
				updateEntry(qe, status, m, resp, null);
				if (EnumQueueEntryStatus.Suspended.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot suspend QueueEntry with ID=" + qeid + ", it is already suspended", 113, EnumClass.Error);
				}
				else if (EnumQueueEntryStatus.Waiting.equals(status) || EnumQueueEntryStatus.Held.equals(status))
				{
					final String errorMsg = "cannot suspend QueueEntry with ID=" + qeid + ", it is " + status.getName();
					JMFHandler.errorResponse(resp, errorMsg, 115, EnumClass.Error);
				}
				else if (EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
				{
					final String errorMsg = "cannot suspend QueueEntry with ID=" + qeid + ", it is already " + status.getName();
					JMFHandler.errorResponse(resp, errorMsg, 114, EnumClass.Error);
				}
				else
				{
					log.error("Whazzup - starnge status for suspending: " + status);
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * handler for the KnownDevices query
	 */
	protected class SubmissionMethodsHandler extends AbstractHandler
	{

		public SubmissionMethodsHandler()
		{
			super(EnumType.SubmissionMethods, new EnumFamily[] { EnumFamily.Query });
		}

		/**
		 * 
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			// "I am the known device"
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			final EnumType typ = m.getEnumType();
			if (EnumType.SubmissionMethods.equals(typ))
			{
				final JDFSubmissionMethods sm = resp.appendSubmissionMethods();
				final Vector<EnumPackaging> v = new Vector<EnumPackaging>();
				v.add(EnumPackaging.MIME);
				sm.setPackaging(v);
				sm.setURLSchemes(new VString("http,file", ","));
				if (_parentDevice.getQueueSubmitHotFolder() != null)
				{
					sm.setHotFolder(UrlUtil.fileToUrl(_parentDevice.getQueueSubmitHotFolder().getHfDirectory(), false));
				}
			}

			return true;
		}
	}

	private RollingBackupFile _queueFile = null;
	String nextinvert = null;
	String lastSortBy = null;

	/**
	 */
	static final String QE_STATUS = "qeStatus";
	/**
	 * 
	 */
	public static final String QE_ID = "qeID";
	static final String isJDF = "isJDF";
	static final String SHOW_QUEUE = "showQueue";
	static final String SHOW_JDF = "showJDF";
	static final String SHOW_XJDF = "showXJDF";
	static final String MODIFY_QE = "modifyQE";

	protected JDFQueue _theQueue;
	private final Vector<Object> _listeners;
	protected AbstractDevice _parentDevice = null;
	protected long lastSort = 0;
	protected final HashMap<String, QueueDelta> deltaMap;
	protected JDFQueueEntry nextPush = null;
	private CanExecuteCallBack _cbCanExecute = null;
	private final QueueMap queueMap;
	private boolean searchByJobPartID = true;
	protected final MutexMap<String> _mutexMap;

	/**
	 * @param searchByJobPartID the searchByJobPartID to set
	 */
	public void setSearchByJobPartID(final boolean searchByJobPartID)
	{
		this.searchByJobPartID = searchByJobPartID;
	}

	/**
	 * @param theParentDevice
	 */
	public QueueProcessor(final AbstractDevice theParentDevice)
	{
		super();
		log.info("Creating queueProcessor");
		nextPush = null;
		_parentDevice = theParentDevice;
		_listeners = new Vector<Object>();
		deltaMap = new HashMap<String, QueueDelta>();
		init();
		queueMap = new QueueMap();
		_mutexMap = new MutexMap<String>();
	}

	/**
	 * @param jmfHandler the handler to add my handlers to
	 */
	public void addHandlers(final IJMFHandler jmfHandler)
	{
		jmfHandler.addHandler(new AcknowledgeThread(this.new SubmitQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(new QueueStatusHandler());
		jmfHandler.addHandler(new AcknowledgeThread(this.new RemoveQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(new AcknowledgeThread(this.new HoldQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(new AcknowledgeThread(this.new AbortQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(new ResumeQueueEntryHandler());
		jmfHandler.addHandler(new SuspendQueueEntryHandler());
		jmfHandler.addHandler(new AcknowledgeThread(this.new FlushQueueHandler(), _parentDevice));
		jmfHandler.addHandler(new OpenQueueHandler());
		jmfHandler.addHandler(new CloseQueueHandler());
		jmfHandler.addHandler(new HoldQueueHandler());
		jmfHandler.addHandler(new ResumeQueueHandler());
		jmfHandler.addHandler(new NewJDFQueryHandler());
		jmfHandler.addHandler(new ResubmitQueueEntryHandler());
		jmfHandler.addHandler(new SubmissionMethodsHandler());
	}

	protected void init()
	{
		final String deviceID = _parentDevice.getDeviceID();
		log.info("QueueProcessor construct for device '" + deviceID + "'");

		if (_queueFile == null)
		{
			_queueFile = new RollingBackupFile(_parentDevice.getDeviceDir() + File.separator + "theQueue.xml", 8);
		}
		if (_queueFile != null && _queueFile.getParentFile() != null && !_queueFile.getParentFile().exists())
		{ // will be null in unit tests
			if (!_queueFile.getParentFile().mkdirs())
			{
				log.error("failed to create base dir at location " + _queueFile.getParentFile());
			}
		}

		final File jdfDir = _parentDevice.getJDFDir();
		if (jdfDir == null || !jdfDir.exists() && !jdfDir.mkdirs())
		{
			log.fatal("failed to create JDFDir at location " + (jdfDir == null ? null : jdfDir.getAbsolutePath()));
		}

		startupParent(deviceID);
	}

	/**
	 * @param deviceID
	 */
	private void setQueueProperties(final String deviceID)
	{
		_theQueue.setAutomated(true);
		_theQueue.setDeviceID(deviceID);
		_theQueue.setMaxCompletedEntries(100);
		_theQueue.setMaxWaitingEntries(-1);
		_theQueue.setMaxRunningEntries(1);
		_theQueue.setDescriptiveName("Queue for " + _parentDevice.getDeviceType());
		_theQueue.setCleanupCallback(_parentDevice.getQECleanup()); // zapps any attached files when removing qe
		_cbCanExecute = new CanExecuteCallBack(deviceID, BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL));
		_theQueue.setExecuteCallback(_cbCanExecute);
		BambiNSExtension.setMyNSAttribute(_theQueue, "EnsureNS", "Dummy"); // ensure that some bambi ns exists
	}

	/**
	 * @param deviceID
	 */
	private void startupParent(final String deviceID)
	{
		JDFDoc d = readQueueFile();
		if (d != null)
		{
			log.info("refreshing queue");
			_theQueue = (JDFQueue) d.getRoot();
			boolean bHold = false;
			final VElement qev = _theQueue.getQueueEntryVector();
			if (qev != null)
			{
				final int qSize = qev.size();
				for (int i = 0; i < qSize; i++)
				{
					final JDFQueueEntry qe = (JDFQueueEntry) qev.get(i);
					final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
					if (EnumQueueEntryStatus.Running.equals(status) || EnumQueueEntryStatus.Waiting.equals(status))
					{
						bHold = true;
						break;
					}
				}
			}
			if (bHold)
			{
				_theQueue.holdQueue();
			}
			setQueueProperties(deviceID);
		}
		else
		{
			d = new JDFDoc(ElementName.QUEUE);
			log.info("creating new queue");
			_theQueue = (JDFQueue) d.getRoot();
			_theQueue.setQueueStatus(EnumQueueStatus.Waiting);
			setQueueProperties(deviceID);
		}
		removeOrphanJDFs();
	}

	/**
	 * @return
	 */
	private JDFDoc readQueueFile()
	{
		boolean exist = _queueFile.exists();
		JDFDoc d = exist ? JDFDoc.parseFile(_queueFile.getAbsolutePath()) : null;
		if (d == null)
		{
			for (int i = 1; true; i++)
			{
				File f = _queueFile.getOldFile(i);
				if (f == null)
				{
					log.info("Could not read queue file - starting from scratch");
					break;
				}
				d = JDFDoc.parseFile(f.getAbsolutePath());
				if (d != null)
				{
					log.warn("problems reading queue file - using backup# " + i);
					break;
				}
			}
		}
		return d;
	}

	/**
	 * get a qe by nodeidentifier - all Entries are evaluated
	 * @param slaveQueueEntryID the qeid in the context of the slave device
	 * @param nodeID the JDFNode.NodeIdentifier
	 * @return the queue entry
	 */
	public JDFQueueEntry getQueueEntry(final String slaveQueueEntryID, NodeIdentifier nodeID)
	{
		JDFQueueEntry qe = queueMap.getQEFromSlaveQEID(slaveQueueEntryID);
		if (nodeID == null || (slaveQueueEntryID != null && qe == null))
		{
			return qe;
		}
		if (searchByJobPartID)
		{
			if (qe == null)
			{
				qe = queueMap.getQEFromNI(nodeID);
			}

			if (qe == null && nodeID.getPartMapVector() != null)
			{
				nodeID = new NodeIdentifier(nodeID); // copy because we zapp internally
				nodeID.setTo(nodeID.getJobID(), nodeID.getJobPartID(), null);
				qe = queueMap.getQEFromNI(nodeID);
			}
		}
		if (qe == null && (!searchByJobPartID || nodeID.getJobPartID() != null))
		{
			nodeID.setTo(nodeID.getJobID(), null, null);
			qe = queueMap.getQEFromNI(nodeID);
		}
		return qe;
	}

	/**
	 * get a qe by nodeidentifier only waiting or suspended entries that have not been forwarded to a lower level device are taken into account
	 * 
	 * @param nodeID the JDFNode.NodeIdentifier
	 * @return the waiting entry, null if none is waiting
	 */
	public IQueueEntry getWaitingQueueEntry(final NodeIdentifier nodeID)
	{

		final VElement vQE = _theQueue.getQueueEntryVector(nodeID);
		if (vQE != null)
		{
			final int siz = vQE.size();
			for (int i = 0; i < siz; i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) vQE.get(i);
				boolean waiting = EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()) || EnumQueueEntryStatus.Suspended.equals(qe.getQueueEntryStatus());
				if (waiting && KElement.isWildCard(BambiNSExtension.getDeviceURL(qe)))
				{
					return getIQueueEntry(qe);
					// try next
				}
			}
		}
		return null;
	}

	/**
	 * get the next queue entry only waiting entries that have not been forwarded to a lower level device are taken into account
	 * @param deviceID
	 * @param canPush
	 * @return the next queue entry
	 */
	public IQueueEntry getNextEntry(final String deviceID, final QERetrieval canPush)
	{
		CanExecuteCallBack cb = _cbCanExecute;
		if (deviceID != null)
		{
			cb = cb.clone();
			cb.deviceID = deviceID;
		}
		synchronized (_theQueue)
		{
			JDFQueueEntry theEntry;
			if (nextPush != null && (cb == null || cb.canExecute(nextPush))) // we have an explicit selection
			{
				log.info("retrieving push qe: " + nextPush.getQueueEntryID());
				theEntry = nextPush;
				nextPush = null;
			}
			else if (canPush == QERetrieval.PUSH || canPush == QERetrieval.BOTH)
			{
				theEntry = _theQueue.getNextExecutableQueueEntry(cb);
			}
			else
			{
				log.debug("no pull entry waiting");
				theEntry = null;
			}
			if (theEntry != null)
			{
				final String proxyFlag = BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL);
				if (proxyFlag != null)
				{
					theEntry.setAttribute(proxyFlag, "true");
				}
				log.debug("new qe: " + theEntry.getQueueEntryID());
			}
			return getIQueueEntry(theEntry);
		}
	}

	/**
	 * @param qeID
	 * @param waitForDoc if true, return null if no doc exists
	 * @return an IQueueEntry that corresponds to the qe, null if none is there
	 */
	public IQueueEntry getIQueueEntry(final String qeID, boolean waitForDoc)
	{
		JDFQueueEntry qe = getQueue().getQueueEntry(qeID);
		if (qe == null || waitForDoc && BambiNSExtension.getDocURL(qe) == null)
			return null;
		else
		{
			return getIQueueEntry(qe, waitForDoc);
		}
	}

	/**
	 * @param qe
	 * @return an IQueueEntry that corresponds to the qe, null if none is there
	 */
	public IQueueEntry getIQueueEntry(final JDFQueueEntry qe)
	{
		return getIQueueEntry(qe, false);
	}

	/**
	 * @param qe
	 * @return an IQueueEntry that corresponds to the qe, null if none is there
	 */
	public IQueueEntry getIQueueEntry(final JDFQueueEntry qe, boolean waitForDoc)
	{
		if (qe == null)
		{
			return null;
		}

		final String docURL = BambiNSExtension.getDocURL(qe);
		final JDFDoc theDoc;
		synchronized (getMutexForQE(qe))
		{
			theDoc = JDFDoc.parseURL(docURL, null);
		}
		if (theDoc == null)
		{
			if (!waitForDoc)
			{
				log.error("QueueProcessor in thread '" + Thread.currentThread().getName() + "' is unable to load the JDFDoc from '" + docURL + "'");
				final String proxyFlag = BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL);
				qe.setAttribute(proxyFlag, null);
				updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null, null);
			}
			return null;
		}

		final JDFNode n = _parentDevice.getNodeFromDoc(theDoc);
		return new QueueEntry(n, qe);
	}

	/**
	 * 
	 * 
	 * @param qe
	 * @return
	 */
	protected MyMutex getMutexForQE(final JDFQueueEntry qe)
	{
		String queueEntryID = qe == null ? null : qe.getQueueEntryID();
		return getMutexForQeID(queueEntryID);
	}

	/**
	 * 
	 *  
	 * @param queueEntryID
	 * @return
	 */
	protected MyMutex getMutexForQeID(String queueEntryID)
	{
		queueEntryID = queueEntryID == null ? "#null#" : queueEntryID;
		return _mutexMap.getCreate(queueEntryID);
	}

	/**
	 * add a listner object that is notified of queue changes
	 * @param listner
	 */
	public void addListener(final MyMutex listner)
	{
		log.info("adding new queue listener");
		_listeners.add(listner);
	}

	/**
	 * @param o
	 */
	public void removeListener(final Object o)
	{
		log.info("removing listener for " + (_parentDevice != null ? _parentDevice.getDeviceID() : " unknown "));
		_listeners.remove(o);
	}

	/**
	 * returns null if the device cannot process the jdf ticket
	 * @param jdf
	 * @param queueEntryID may be null in case of a new submission
	 * @return list of valid deviceIDS if any, else null if none
	 */
	public VString canAccept(final JDFNode jdf, final String queueEntryID)
	{
		boolean acceptAll = false;
		final IDeviceProperties properties = _parentDevice.getProperties();
		if (properties instanceof DeviceProperties)
		{
			acceptAll = ((DeviceProperties) properties).getAcceptAll();
			if (acceptAll)
			{
				return new VString(_parentDevice.getDeviceID(), null);
			}
		}
		return _parentDevice.canAccept(jdf, queueEntryID);
	}

	/**
	 * @param submitQueueEntry the sqe command
	 * @param newResponse the response to fill
	 * @param theJDF the JDFDoc to submit
	 * @return the new qe, null if failed
	 */
	public JDFQueueEntry addEntry(final JDFCommand submitQueueEntry, JDFResponse newResponse, final JDFDoc theJDF)
	{
		if (submitQueueEntry == null || theJDF == null)
		{
			log.error("error submitting new queueentry");
			return null;
		}

		final VString canAccept = canAccept(theJDF.getJDFRoot(), null);
		if (canAccept == null)
		{
			JMFHandler.errorResponse(newResponse, "unable to queue request: No matching nodes found. Check Types and DeviceID - Error code = 101", 101, EnumClass.Error);
			return null;
		}

		JDFQueueEntry newQE;
		synchronized (_theQueue)
		{

			final JDFQueueSubmissionParams qsp = submitQueueEntry.getQueueSubmissionParams(0);
			if (qsp == null)
			{
				log.error("error submitting new queueentry");
				return null;
			}

			final JDFResponse r2 = qsp.addEntry(_theQueue, null, submitQueueEntry.getQueueFilter(0));
			if (newResponse != null)
			{
				newResponse.copyInto(r2, false);
			}
			else
			{
				newResponse = r2;
			}
			if (r2 == null || r2.getReturnCode() != 0)
			{
				log.warn("invalid response while adding queue entry");
				return null;
			}

			newQE = newResponse.getQueueEntry(0);
			if (newResponse.getReturnCode() != 0 || newQE == null)
			{
				log.warn("error submitting queueentry: " + newResponse.getReturnCode());
				return null;
			}

			BambiNSExtension.appendMyNSAttribute(newQE, BambiNSExtension.GOOD_DEVICES, StringUtil.setvString(canAccept));
			_parentDevice.fixEntry(newQE, theJDF);
			DataExtractor dataExtractor = _parentDevice.getDataExtractor(true);
			if (dataExtractor != null)
			{
				dataExtractor.extractFiles(newQE, theJDF);
			}
			if (!storeDoc(newQE, theJDF, qsp.getReturnURL(), qsp.getReturnJMF()))
			{
				newResponse.setReturnCode(120);
				log.error("error storing queueentry: " + newResponse.getReturnCode());
				return null;
			}
			persist(300000);
			final String qeID = newQE.getQueueEntryID();
			notifyListeners(qeID);
			log.info("Successfully queued new QueueEntry: QueueEntryID=" + qeID);
			newQE = _theQueue.getQueueEntry(qeID);
			prepareSubmit(newQE);
		}
		// wait a very short moment to allow any potential processing of the newly created entry to commence, prior to returning the entry
		ThreadUtil.sleep(42);
		return newQE;
	}

	/**
	 * prepare qe for submission
	 * @param newQE
	 */
	protected void prepareSubmit(JDFQueueEntry newQE)
	{
		_parentDevice.prepareSubmit(newQE);
	}

	/**
	 * @param newQE
	 * @param theJDF
	 * @param returnURL the returnURL to add to the qe
	 * @param returnJMF
	 * @return true if successful
	 */
	public boolean storeDoc(final JDFQueueEntry newQE, final JDFDoc theJDF, final String returnURL, final String returnJMF)
	{
		if (newQE == null || theJDF == null)
		{
			log.info("error storing queueentry");
			return false;
		}
		final String newQEID = newQE.getQueueEntryID();
		final JDFQueueEntry newQEReal = _theQueue.getQueueEntry(newQEID); // the "actual" entry in the queue
		if (newQEReal == null)
		{
			log.error("error fetching queueentry: QueueEntryID=" + newQEID);
			return false;
		}
		newQEReal.copyInto(newQE, false);
		queueMap.addEntry(newQEReal, true);

		boolean ok = storeJDF(theJDF, newQEID);
		if (!KElement.isWildCard(returnJMF))
		{
			BambiNSExtension.setReturnJMF(newQEReal, returnJMF);
		}
		else if (!KElement.isWildCard(returnURL))
		{
			BambiNSExtension.setReturnURL(newQEReal, returnURL);
		}

		return ok;
	}

	/**
	 * 
	 * store the JDF again
	 * @param theJDF
	 * @param newQEID
	 * @return
	 */
	public boolean storeJDF(final JDFDoc theJDF, final String newQEID)
	{
		boolean ok;
		final JDFQueueEntry newQEReal = _theQueue.getQueueEntry(newQEID); // the "actual" entry in the queue
		synchronized (getMutexForQeID(newQEID))
		{
			final String theDocFile = _parentDevice.getJDFStorage(newQEID);
			ok = theJDF.write2File(theDocFile, 0, true);
			if (!ok)
			{
				log.error("error writing to: " + theDocFile);
			}
		}
		final String theDocFile = _parentDevice.getJDFStorage(newQEID);
		BambiNSExtension.setDocURL(newQEReal, theDocFile);
		BambiNSExtension.setDocModified(newQEReal, System.currentTimeMillis());
		return ok;
	}

	/**
	 * 
	 */
	protected void removeOrphanJDFs()
	{
		final File[] crap;
		final HashSet<File> hs;
		synchronized (_theQueue)
		{
			crap = FileUtil.listFilesWithExtension(_parentDevice.getJDFDir(), "jdf");
			hs = new HashSet<File>();
			final VElement v = _theQueue.getQueueEntryVector();
			for (int i = 0; i < v.size(); i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) v.get(i);
				final String docURL = BambiNSExtension.getDocURL(qe);
				if (docURL != null)
				{
					hs.add(new File(StringUtil.token(docURL, -1, File.separator)));
				}
			}
		}
		if (crap != null)
		{
			for (final File kill : crap)
			{
				// some systems list complete paths, others list only names - ensure name only
				if (!hs.contains(new File(kill.getName())))
				{
					File dataDir = new File(UrlUtil.newExtension(kill.getAbsolutePath(), null));
					FileUtil.deleteAll(dataDir);
					kill.delete();
					log.warn("removing orphan JDF:" + kill.getName());
				}
			}
		}
	}

	protected void notifyListeners(final String qeID)
	{
		for (int i = 0; i < _listeners.size(); i++)
		{
			final Object elementAt = _listeners.elementAt(i);
			ThreadUtil.notifyAll(elementAt);
		}
		final SignalDispatcher signalDispatcher = _parentDevice.getSignalDispatcher();
		signalDispatcher.triggerQueueEntry(qeID, null, -1, EnumType.QueueStatus.getName());
	}

	/**
	 * asynchronous make the memory queue persistent
	 * 
	 * @param milliseconds length of time wait until persist, if 0 force persist
	 * 
	 */
	public void persist(final long milliseconds)
	{
		DelayedPersist.getDelayedPersist().queue(this, milliseconds);
	}

	/**
	 * make the memory queue persistent
	 * @return true if ok
	 */
	@Override
	public boolean persist()
	{
		synchronized (_theQueue)
		{
			log.info("persisting queue to " + _queueFile.getPath() + " size: " + _theQueue.numEntries(null));
			long t = System.currentTimeMillis();
			if (t - lastSort > 900000) // every 15 minutes is fine
			{
				_theQueue.sortChildren();
				lastSort = t;
			}
			_queueFile.getNewFile();
			return _theQueue.getOwnerDocument_KElement().write2File(_queueFile, 0, true);
		}
	}

	/**
	 * @return the queue element
	 */
	public JDFQueue getQueue()
	{
		return _theQueue;
	}

	/**
	 * update the QueueEntry qe to be in the new status
	 * 
	 * @param qe the QueueEntry to update
	 * @param status the updated QueueEntry status
	 * @param mess the message that triggers the update - may be null
	 * @param resp the message response to be filled - may be null
	 * 
	 * @return JDFQueue the updated queue in its new status
	 * @deprecated use 5 parameter version
	 */
	@Deprecated
	public JDFQueue updateEntry(JDFQueueEntry qe, final EnumQueueEntryStatus status, final JDFMessage mess, final JDFResponse resp)
	{
		return updateEntry(qe, status, mess, resp, null);
	}

	/**
	 * update the QueueEntry qe to be in the new status
	 * 
	 * @param qe the QueueEntry to update
	 * @param status the updated QueueEntry status
	 * @param mess the message that triggers the update - may be null
	 * @param resp the message response to be filled - may be null
	 * @param statusDetails 
	 * 
	 * @return JDFQueue the updated queue in its new status
	 */
	public JDFQueue updateEntry(JDFQueueEntry qe, final EnumQueueEntryStatus status, final JDFMessage mess, final JDFResponse resp, String statusDetails)
	{
		if (qe == null || StringUtil.getNonEmpty(qe.getQueueEntryID()) == null)
		{
			log.error("cannot update qe: " + qe);
			return _theQueue;
		}
		statusDetails = StringUtil.getNonEmpty(statusDetails);
		synchronized (_theQueue)
		{
			JDFQueueEntry qe2 = _theQueue.getQueueEntry(qe.getQueueEntryID());
			if (qe2 != qe)
			{
				if (qe2 == null)
				{
					log.error("no such queueentry: " + qe.getQueueEntryID());
					return _theQueue;
				}
				else
				{
					log.warn("not updating original QE - using original" + qe2.getQueueEntryID());
					qe = qe2;
				}
			}
			if (qe != null && status != null)
			{
				final EnumQueueEntryStatus oldStatus = qe.getQueueEntryStatus();
				final String queueEntryID = qe.getQueueEntryID();
				if (status.equals(EnumQueueEntryStatus.Removed))
				{
					qe.setQueueEntryStatus(status);
					queueMap.removeEntry(qe);
					final String docURL = BambiNSExtension.getDocURL(qe);
					if (docURL != null)
					{
						new File(docURL).delete();
					}
					_parentDevice.getSignalDispatcher().removeSubScriptions(queueEntryID, null, null);
				}
				else if (status.equals(EnumQueueEntryStatus.Running))
				{
					if (!qe.hasAttribute(AttributeName.STARTTIME) || qe.hasAttribute(AttributeName.ENDTIME))
					{
						_theQueue.setAutomated(false);
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Suspended);
						_theQueue.setAutomated(true);
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
						qe.setStatusDetails(statusDetails);
					}
					else if (!ContainerUtil.equals(oldStatus, status))
					{
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
						qe.setStatusDetails(statusDetails);
					}
				}
				else if (status.equals(EnumQueueEntryStatus.Waiting))
				{
					qe.removeAttribute(AttributeName.STARTTIME);
					qe.removeAttribute(AttributeName.ENDTIME);
					qe.removeAttribute(AttributeName.DEVICEID);
					qe.setQueueEntryStatus(status);
					qe.setStatusDetails(statusDetails);
				}
				else if (status.equals(EnumQueueEntryStatus.Aborted) || status.equals(EnumQueueEntryStatus.Completed) || status.equals(EnumQueueEntryStatus.Suspended))
				{
					qe.removeAttribute(AttributeName.DEVICEID);
					BambiNSExtension.setDeviceURL(qe, null);
					qe.setQueueEntryStatus(status);
					qe.setStatusDetails(statusDetails);
				}
				else if (!ContainerUtil.equals(oldStatus, status))
				{
					qe.setQueueEntryStatus(status);
					qe.setStatusDetails(statusDetails);
				}

				if (!ContainerUtil.equals(oldStatus, status))
				{
					persist(300000);
					notifyListeners(qe.getQueueEntryID());
				}
			}

			final JDFQueue q = resp == null ? null : copyToMessage(mess, resp);
			return q;
		}
	}

	private JDFQueue copyToMessage(final JDFMessage mess, final JDFResponse resp)
	{
		JDFQueueFilter qf = null;
		try
		{
			qf = mess == null ? null : mess.getQueueFilter(0);
		}
		catch (final JDFException e)
		{
			log.warn("problems with queuefilter - ignoring", e);
		}
		final JDFQueue q = _theQueue.copyToResponse(resp, qf, getLastQueue(resp, qf));
		if (qf != null && EnumUpdateGranularity.ChangesOnly.equals(qf.getUpdateGranularity()) && q.getQueueEntry(0) == null)
		{
			//we have an empty queue
			return null;
		}
		removeBambiNSExtensions(q);
		return q;
	}

	/**
	 * get the last queue for a differential channelid <br/>
	 * also update the que to a copy of the current state for the next call
	 * 
	 * @param resp
	 * @param qf
	 * @return
	 */
	private JDFQueue getLastQueue(final JDFResponse resp, final JDFQueueFilter qf)
	{
		if (resp == null || qf == null)
		{
			return null;
		}
		final String refID = StringUtil.getNonEmpty(resp.getrefID());
		if (refID == null || !EnumUpdateGranularity.ChangesOnly.equals(qf.getUpdateGranularity()))
		{
			return null;
		}
		return getLastQueue(refID);
	}

	/**
	 * @param refID
	 * @return the last queue that was shown
	 */
	protected JDFQueue getLastQueue(final String refID)
	{
		final QueueDelta delta = deltaMap.get(refID);
		deltaMap.put(refID, new QueueDelta());
		if (delta == null)
		{
			return null;
		}
		delta.cleanOrphans();
		return delta.lastQueue;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		String s = "[QueueProcessor: ] Status= " + _theQueue.getQueueStatus().getName() + " Num Entries: " + _theQueue.numEntries(null) + "\n Queue:\n";
		s += _theQueue.toString();
		return s;
	}

	/**
	 * @param qe
	 * @param finishedNodes
	 * @param docJDF
	 * @param newStatus 
	 */
	public void returnQueueEntry(final JDFQueueEntry qe, VString finishedNodes, JDFDoc docJDF, EnumQueueEntryStatus newStatus)
	{
		new QueueEntryReturn(qe, newStatus).returnQueueEntry(finishedNodes, docJDF);
	}

	private class QueueEntryReturn
	{
		final JDFQueueEntry qe;
		final IDeviceProperties properties;
		final String queueEntryID;

		/**
		 * @param newStatus 
		 * 
		 */
		QueueEntryReturn(final JDFQueueEntry qe, EnumQueueEntryStatus newStatus)
		{
			super();
			this.qe = (JDFQueueEntry) qe.clone();
			if (newStatus != null)
				this.qe.setQueueEntryStatus(newStatus);
			properties = _parentDevice.getProperties();
			queueEntryID = qe.getQueueEntryID();
		}

		/**
		 * 
		 * @param finishedNodes
		 * @param docJDF
		 */
		void returnQueueEntry(VString finishedNodes, JDFDoc docJDF)
		{
			log.info("returning queue entry");
			JMFBuilder jmfBuilder = JMFBuilderFactory.getJMFBuilder(_parentDevice.getDeviceID());

			final JDFJMF jmf = jmfBuilder.buildReturnQueueEntry(queueEntryID);
			final JDFDoc docJMF = jmf.getOwnerDocument_JDFElement();
			jmf.setICSVersions(_parentDevice.getICSVersions());
			final JDFCommand com = jmf.getCommand(0);
			final JDFReturnQueueEntryParams returnQEParams = com.getReturnQueueEntryParams(0);

			if (docJDF == null)
			{
				IQueueEntry iQueueEntry = getIQueueEntry(qe, true);
				JDFNode node = iQueueEntry == null ? null : iQueueEntry.getJDF();
				if (node != null)
				{
					docJDF = node.getOwnerDocument_JDFElement();
				}
			}
			if (docJDF == null)
			{
				log.error("cannot load the JDFDoc to return");
				return;
			}
			finishedNodes = updateFinishedNodes(finishedNodes, docJDF);

			boolean bAborted = false;
			if (EnumNodeStatus.Completed.equals(qe.getStatus()))
			{
				returnQEParams.setCompleted(finishedNodes);
			}
			else if (EnumNodeStatus.Aborted.equals(qe.getStatus()))
			{
				returnQEParams.setAborted(finishedNodes);
				bAborted = true;
				setNodesAborted(docJDF, finishedNodes);
			}

			callBacks(docJDF, docJMF);
			boolean bOK = false;
			_parentDevice.flush();

			final String returnJMF = BambiNSExtension.getReturnJMF(qe);
			if (returnJMF != null)
			{
				bOK = returnJMF(docJDF, jmf);
			}

			final String returnURL = BambiNSExtension.getReturnURL(qe);
			if (!bOK && returnURL != null)
			{
				bOK = returnJDFUrl(docJDF);
			}
			if (!bOK)
			{
				returnHF(docJDF, bAborted);
			}
			removeSubscriptions();
		}

		private void callBacks(JDFDoc docJDF, final JDFDoc docJMF)
		{
			// fix for returning
			final IConverterCallback callBack = _parentDevice.getCallback(null);
			if (callBack != null)
			{
				callBack.updateJDFForExtern(docJDF);
				callBack.updateJMFForExtern(docJMF);
			}
		}

		private void removeSubscriptions()
		{
			// remove any subscriptions in case they are still around
			if (queueEntryID != null)
			{
				final SignalDispatcher signalDispatcher = _parentDevice.getSignalDispatcher();
				if (signalDispatcher != null) // may be null at shutdown
				{
					signalDispatcher.removeSubScriptions(queueEntryID, null, null);
				}
			}
		}

		private boolean returnHF(JDFDoc docJDF, boolean bAborted)
		{
			boolean bOK = false;
			final File deviceOutputHF = properties.getOutputHF();
			final File deviceErrorHF = properties.getErrorHF();
			if (!bAborted && deviceOutputHF != null)
			{
				deviceOutputHF.mkdirs();
				bOK = docJDF.write2File(FileUtil.getFileInDirectory(deviceOutputHF, new File(new File(docJDF.getOriginalFileName()).getName())), 0, true);
				log.info("JDF for " + queueEntryID + " has " + (bOK ? "" : "not ") + "been written to good output: " + deviceOutputHF);
			}
			else if (bAborted && deviceErrorHF != null)
			{
				deviceErrorHF.mkdirs();
				bOK = docJDF.write2File(FileUtil.getFileInDirectory(deviceErrorHF, new File(docJDF.getOriginalFileName())), 0, true);
				log.info("JDF for " + queueEntryID + " has " + (bOK ? "" : "not ") + "been written to error output: " + deviceErrorHF);
			}
			else
			{
				log.warn("No return URL, No HF, No Nothing  specified, bailing out");
			}
			return bOK;
		}

		private boolean returnJDFUrl(JDFDoc docJDF)
		{
			boolean bOK = false;
			final String returnURL = BambiNSExtension.getReturnURL(qe);

			try
			{
				log.info("JDF Document for " + queueEntryID + " is being been sent to " + returnURL);
				final JDFDoc d = docJDF.write2URL(returnURL);
				// TODO error handling
				bOK = d != null;
			}
			catch (final Throwable e)
			{
				log.error("failed to send ReturnQueueEntry: " + e);
			}
			return bOK;
		}

		private boolean returnJMF(JDFDoc docJDF, final JDFJMF jmf)
		{
			final String returnJMF = BambiNSExtension.getReturnJMF(qe);
			log.info("ReturnQueueEntry for " + queueEntryID + " is being been sent to " + returnJMF);
			final QEReturn qr = properties.getReturnMIME();
			HttpURLConnection response = null;
			final JDFReturnQueueEntryParams returnQEParams = jmf.getCommand(0).getReturnQueueEntryParams(0);
			if (QEReturn.MIME.equals(qr))
			{
				returnQEParams.setURL("cid:dummy"); // will be overwritten by buildMimePackage
				final JDFDoc docJMF = jmf.getOwnerDocument_JDFElement();
				final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, _parentDevice.getProperties().getControllerMIMEExpansion());
				final MIMEDetails mimeDetails = new MIMEDetails();
				final String devID = _parentDevice.getDeviceID();
				mimeDetails.httpDetails.setChunkSize(properties.getControllerHTTPChunk());
				mimeDetails.transferEncoding = properties.getControllerMIMEEncoding();
				mimeDetails.modifyBoundarySemicolon = StringUtil.parseBoolean(properties.getDeviceAttribute("FixMIMEBoundarySemicolon"), false);
				response = _parentDevice.getJMFFactory().send2URLSynch(mp, returnJMF, _parentDevice.getCallback(null), mimeDetails, devID, 10000);
			}
			else
			// http
			{
				returnQEParams.setURL(properties.getContextURL() + "/jmb/JDFDir/" + queueEntryID + ".jdf"); // will be overwritten by buildMimePackage
				final HTTPDetails hDet = new HTTPDetails();
				hDet.setChunkSize(properties.getControllerHTTPChunk());

				response = JMFFactory.getJMFFactory().send2URLSynch(jmf, returnJMF, _parentDevice.getCallback(null), _parentDevice.getDeviceID(), 10000);
			}
			boolean bOK = false;

			if (response != null)
			{
				int responseCode;

				try
				{
					responseCode = response.getResponseCode();
				}
				catch (final IOException x)
				{
					log.error("cannot read returnqe response: " + x);
					responseCode = 0;
				}
				if (responseCode == 200)
				{
					log.info("ReturnQueueEntry for " + queueEntryID + " has been sent to " + returnJMF);
					bOK = true;
				}
				else
				{
					log.error("failed to send ReturnQueueEntry. Response: " + response.toString());
					bOK = false;
				}
			}
			return bOK;
		}

		public VString updateFinishedNodes(VString finishedNodes, JDFDoc docJDF)
		{
			if (finishedNodes == null || finishedNodes.size() == 0)
			{
				final JDFNode n = docJDF.getJDFRoot();
				if (n == null)
				{
					finishedNodes = new VString("rootDev", null);
				}
				else
				{
					finishedNodes = new VString(n.getID(), null);
				}
			}
			return finishedNodes;
		}

		/**
		 * @param docJDF
		 * @param finishedNodes
		 */
		private void setNodesAborted(final JDFDoc docJDF, final VString finishedNodes)
		{
			final JDFNode root = docJDF == null ? null : docJDF.getJDFRoot();
			if (root == null)
			{
				log.error("No JDF document returned to abort");
				return;
			}
			final JDFNotification not = root.getCreateAuditPool().addNotification(EnumClass.Warning, null, qe.getPartMapVector());
			final JDFComment notificationComment = not.appendComment();
			notificationComment.setLanguage("en");
			notificationComment.setText("Node aborted in queue entry: " + qe.getQueueEntryID());
			log.warn("Node aborted in queue entry: " + qe.getQueueEntryID());
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	protected JDFQueueEntry getMessageQueueEntry(final JDFMessage m, final JDFResponse resp)
	{
		final JDFQueueEntryDef def = m.getQueueEntryDef(0);
		if (def == null)
		{
			JMFHandler.errorResponse(resp, "Message contains no QueueEntryDef", 105, EnumClass.Error);
			return null;
		}

		final String qeid = def.getQueueEntryID();
		if (KElement.isWildCard(qeid))
		{
			JMFHandler.errorResponse(resp, "QueueEntryDef does not contain any QueueEntryID", 105, EnumClass.Error);
			return null;
		}
		log.info("processing getMessageQueueEntryID for " + qeid);
		final JDFQueueEntry qe = _theQueue.getQueueEntry(qeid);
		if (qe == null)
		{
			JMFHandler.errorResponse(resp, "found no QueueEntry with QueueEntryID=" + qeid, 105, EnumClass.Error);
		}
		return qe;

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * remove all Bambi namespace extensions from a given queue
	 * @param queue the queue to filter
	 * 
	 */
	public static void removeBambiNSExtensions(final JDFQueue queue)
	{
		if (queue == null)
		{
			return;
		}
		final VElement v = queue.getQueueEntryVector();
		if (v != null)
		{
			final int queueSize = v.size();
			for (int i = 0; i < queueSize; i++)
			{
				BambiNSExtension.removeBambiExtensions(v.elementAt(i));
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param request
	 * @return the xml response
	 */
	public XMLResponse handleGet(final ContainerRequest request)
	{
		XMLResponse r = getQueueGetHandler().handleGet(request);
		if (r == null)
		{
			r = getShowJDFHandler().handleGet(request);
		}
		if (r == null)
		{
			r = getShowXJDFHandler().handleGet(request);
		}
		return r;
	}

	/**
	 * hook to overwrite the ShowXJDFHandler
	 * @return
	 */
	protected ShowXJDFHandler getShowXJDFHandler()
	{
		return new ShowXJDFHandler(_parentDevice);
	}

	/**
	 * hook to overwrite the ShowJDFHandler
	 * @return
	 */
	protected ShowJDFHandler getShowJDFHandler()
	{
		return new ShowJDFHandler(_parentDevice);
	}

	/**
	 * 
	 * @return
	 */
	protected QueueGetHandler getQueueGetHandler()
	{
		return new QueueGetHandler();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param m
	 * @param resp
	 * @return true if successfully aborted
	 */
	protected boolean abortQueueEntry(final JDFMessage m, final JDFResponse resp)
	{
		if (m == null || resp == null)
		{
			return false;
		}
		log.debug("Handling " + m.getType());
		final JDFQueueEntry qe = getMessageQueueEntry(m, resp);
		if (qe == null)
		{
			return true;
		}
		final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
		final String qeid = qe.getQueueEntryID();
		JDFNode theNode = null;
		if (EnumQueueEntryStatus.Completed.equals(status))
		{
			updateEntry(qe, status, m, resp, null);
			JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID=" + qeid + ", it is already completed", 114, EnumClass.Error);
			return true;
		}
		else if (EnumQueueEntryStatus.Aborted.equals(status))
		{
			updateEntry(qe, status, m, resp, null);
			JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID=" + qeid + ", it is already aborted", 113, EnumClass.Error);
			return true;
		}
		else if (EnumQueueEntryStatus.Waiting.equals(status)) // no need to check processors - it is still waiting
		{
			IQueueEntry iQueueEntry = getIQueueEntry(qe, true);
			theNode = iQueueEntry == null ? null : iQueueEntry.getJDF();
			updateEntry(qe, EnumQueueEntryStatus.Aborted, m, resp, null);
		}
		final String queueEntryID = qe.getQueueEntryID();
		final JDFQueueEntry returnQE = _parentDevice.stopProcessing(queueEntryID, EnumNodeStatus.Aborted, null);

		// has to be waiting, held, running or suspended: abort it!
		EnumQueueEntryStatus newStatus = (returnQE == null ? null : returnQE.getQueueEntryStatus());
		if (newStatus == null)
		{
			newStatus = EnumQueueEntryStatus.Aborted;
		}
		if (EnumQueueEntryStatus.Aborted.equals(newStatus))
		{
			if (theNode == null)
			{
				IQueueEntry iQueueEntry = getIQueueEntry(qe);
				theNode = iQueueEntry == null ? null : iQueueEntry.getJDF();
			}
			JDFDoc theDoc = theNode == null ? null : theNode.getOwnerDocument_JDFElement();
			returnQueueEntry(qe, null, theDoc, newStatus);
		}
		updateEntry(qe, newStatus, m, resp, null);
		log.info("aborted QueueEntry with ID=" + qeid);
		return true;
	}

	/**
	 * default shutdown method
	 */
	public void shutdown()
	{
		log.info("shutting down queue");
		DelayedPersist.shutDown();
	}

	/**
	 * clean up the entire damn thing - non-reversible...
	 */
	public void reset()
	{
		_theQueue.flushQueue(null);
		_theQueue.setQueueStatus(EnumQueueStatus.Waiting);
		removeOrphanJDFs();
		_queueFile.clearAll();
		queueMap.reset();
		persist(0);
	}

	/**
	 * updates the cache of slave queuentryids for quickly finding a queueEntry based on slave qeid jmfs
	 * 
	 * @param qe the local queueEntry
	 * @param slaveQEID the slave qeid
	 */
	public void updateCache(final JDFQueueEntry qe, final String slaveQEID)
	{
		BambiNSExtension.setSlaveQueueEntryID(qe, slaveQEID);
		if (slaveQEID == null)
			queueMap.removeEntry(qe);
		else
			queueMap.addEntry(qe, false);
	}

	/**
	 * create a comple clone of theQueue
	 *  
	 * @return the clone
	 */
	protected JDFQueue cloneQueue()
	{
		XMLDoc doc = _theQueue.getOwnerDocument_KElement();
		XMLDoc clone = doc.clone();
		return (JDFQueue) clone.getRoot();
	}

	/**
	 * TODO Please insert comment!
	 * @param qeNew
	 * @return
	 */
	public boolean wasSubmitted(JDFQueueEntry qeNew)
	{
		return _parentDevice.wasSubmitted(qeNew);
	}
}
