/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.mail.Multipart;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiLog;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.BambiServletRequest;
import org.cip4.bambi.core.BambiServletResponse;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.SignalDispatcher;
import org.cip4.bambi.core.IDeviceProperties.QERetrieval;
import org.cip4.bambi.core.IDeviceProperties.QEReturn;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.core.messaging.AcknowledgeThread;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumUpdateGranularity;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFComment;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.elementwalker.URLExtractor;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFFlushQueueInfo;
import org.cip4.jdflib.jmf.JDFFlushQueueParams;
import org.cip4.jdflib.jmf.JDFIDInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFNewJDFQuParams;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueEntryDef;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFResubmissionParams;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue.CleanupCallback;
import org.cip4.jdflib.jmf.JDFQueue.ExecuteCallback;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MyLong;
import org.cip4.jdflib.util.RollingBackupFile;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.ThreadUtil.MyMutex;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;

/**
 * 
 * @author rainer
 * 
 * 
 */
public class QueueProcessor extends BambiLogFactory
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
			lastQueue = (JDFQueue) _theQueue.getOwnerDocument_KElement().clone().getRoot();
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
				for (int i = 0; i < v.size(); i++)
				{
					final JDFQueueEntry qe = (JDFQueueEntry) v.get(i);
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
		 * @param slaveqeID
		 * @return
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
	}

	/**
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * Jul 6, 2009
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

		/*
		 * (non-Javadoc)
		 * 
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
		 * @return
		 */
		@Override
		protected CanExecuteCallBack clone()
		{
			final CanExecuteCallBack cb = new CanExecuteCallBack(deviceID, proxy);
			return cb;
		}
	}

	/**
	 * cleans up the garbage that belongs to a queueentry when the qe is removed
	 * @author prosirai
	 * 
	 */
	protected class QueueEntryCleanup extends CleanupCallback
	{

		/**
		 * 
		 * 
		 * @see org.cip4.jdflib.jmf.JDFQueue.CleanupCallback#cleanEntry(org.cip4.jdflib.jmf.JDFQueueEntry)
		 */
		@Override
		public void cleanEntry(final JDFQueueEntry qe)
		{
			// the jdf
			final String theDocFile = _parentDevice.getJDFStorage(qe.getQueueEntryID());
			if (theDocFile != null)
			{
				final File f = new File(theDocFile);
				f.delete();
			}
			// now the other stuff
			final File theJobDir = _parentDevice.getJobDirectory(qe.getQueueEntryID());
			if (theJobDir != null)
			{
				FileUtil.deleteAll(theJobDir);
			}
			// now the other stuff
			final File theRootJobDir = _parentDevice.getRootDevice().getJobDirectory(qe.getQueueEntryID());
			if (theRootJobDir != null)
			{
				FileUtil.deleteAll(theJobDir);
			}

			_parentDevice.stopProcessing(qe.getQueueEntryID(), null);
		}

	}

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
					updateEntry(null, null, m, resp);
					String errorMsg = "failed to get JDFDoc from '" + qsp.getURL() + "' on SubmitQueueEntry";
					errorMsg += "\r\nin thread: " + Thread.currentThread().getName();
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
				System.gc();
				if (qe == null)
				{
					return true;
				}
				else
				{
					resp.removeChild(ElementName.QUEUEENTRY, null, 0);
					final JDFQueueEntry qeNew = (JDFQueueEntry) resp.copyElement(qe, null);
					BambiNSExtension.removeBambiExtensions(qeNew);
					updateEntry(qe, null, m, resp);
				}
				return true;
			}
			JMFHandler.errorResponse(resp, "QueueSubmissionParams are missing or invalid", 9, EnumClass.Error);
			log.error("QueueSubmissionParams are missing or invalid");
			return true;
		}
	}

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
				updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp);
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
			updateEntry(null, null, m, resp);
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
			updateEntry(null, null, m, resp);
			// if the filter removed the queue, this is a nop and can be zapped
			return (resp.getQueue(0) != null);
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
					|| EnumQueueEntryStatus.Aborted.equals(status))
			{
				final String queueEntryID = qe.getQueueEntryID();
				final JDFQueueEntry returnQE = _parentDevice.stopProcessing(queueEntryID, null); // use null to flag a removal
				updateEntry(returnQE, EnumQueueEntryStatus.Removed, m, resp);
				log.info("removed QueueEntry with ID=" + qeid);
			}
			else
			{
				final String statName = status.getName();
				updateEntry(qe, status, m, resp);
				JMFHandler.errorResponse(resp, "cannot remove QueueEntry with ID=" + qeid + ", it is " + statName, 106, EnumClass.Error);
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
				updateEntry(qe, EnumQueueEntryStatus.Held, m, resp);
				log.info("held QueueEntry with ID=" + qeid);
			}
			else
			{
				updateEntry(qe, status, m, resp);
				if (EnumQueueEntryStatus.Held.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot suspend QueueEntry with ID=" + qeid + ", it is already held", 113, EnumClass.Error);
				}
				else if (EnumQueueEntryStatus.Running.equals(status) || EnumQueueEntryStatus.Suspended.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot hold QueueEntry with ID=" + qeid + ", it is " + status.getName(), 106, EnumClass.Error);
				}

				else if (EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
				{
					JMFHandler.errorResponse(resp, "cannot hold QueueEntry with ID=" + qeid + ", it is already " + status.getName(), 114, EnumClass.Error);
				}
				else
				{
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
				updateEntry(null, null, m, resp);
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

	// //////////////////////////////////////////////////////////////////////////////////////

	static class DelayedPersist extends Thread
	{
		HashMap<QueueProcessor, MyLong> persistQueue;
		private boolean stop;
		private static DelayedPersist theDelayed = null;
		private final MyMutex waitMutex;
		private final BambiLog log;

		private DelayedPersist()
		{
			super("DelayedPersist");
			persistQueue = new HashMap<QueueProcessor, MyLong>();
			stop = false;
			waitMutex = new MyMutex();
			log = new BambiLogFactory(this.getClass()).getLog();
			start();
		}

		protected static DelayedPersist getDelayedPersist()
		{
			if (theDelayed == null)
				theDelayed = new DelayedPersist();
			return theDelayed;
		}

		static protected void shutDown()
		{
			if (theDelayed == null)
				return;
			theDelayed.stop = true;
			ThreadUtil.notify(theDelayed.waitMutex);
			theDelayed = null;
		}

		/**
		 * 
		 * @param qp
		 * @param deltaTime
		 */
		public void queue(QueueProcessor qp, long deltaTime)
		{
			synchronized (persistQueue)
			{
				MyLong l = persistQueue.get(qp);
				long t = System.currentTimeMillis();
				if (l == null)
				{
					persistQueue.put(qp, new MyLong(t + deltaTime));
				}
				else if (t + deltaTime < l.i)
				{
					l.i = t + deltaTime;
				}
			}
			if (deltaTime <= 0)
				ThreadUtil.notify(waitMutex);
		}

		/**
		 * @see java.lang.Thread#run()
		*/
		@Override
		public void run()
		{
			log.info("starting queue persist loop");
			while (true)
			{
				try
				{
					persistQueues();
				}
				catch (Exception e)
				{
					log.error("whazzup? ", e);
				}
				if (stop)
				{
					log.info("end of queue persist loop");
					break;
				}

				ThreadUtil.wait(waitMutex, 10000);
			}
		}

		/**
		 * 
		 */
		private void persistQueues()
		{
			long t = System.currentTimeMillis();
			Vector<QueueProcessor> theList = new Vector<QueueProcessor>();

			synchronized (persistQueue)
			{
				Vector<QueueProcessor> v = ContainerUtil.getKeyVector(persistQueue);
				if (v == null)
					return;
				Iterator<QueueProcessor> it = v.iterator();

				while (it.hasNext())
				{
					QueueProcessor qp = it.next();
					MyLong l = persistQueue.get(qp);
					if (l.i < t)
					{
						theList.add(qp);
						persistQueue.remove(qp);
					}
				}
			}

			// now the unsynchronized stuff
			Iterator<QueueProcessor> it = theList.iterator();
			while (it.hasNext())
			{
				QueueProcessor qp = it.next();
				qp.persist();
			}
		}

		/**
		 * @see java.lang.Thread#toString()
		 * @return
		*/
		@Override
		public String toString()
		{
			return "DelayedPersist Thread " + stop + " queue: " + persistQueue;
		}
	}

	protected class ResumeQueueEntryHandler extends AbstractHandler
	{

		public ResumeQueueEntryHandler()
		{
			super(EnumType.ResumeQueueEntry, new EnumFamily[] { EnumFamily.Command });
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

				if (EnumQueueEntryStatus.Suspended.equals(status) || EnumQueueEntryStatus.Held.equals(status))
				{
					updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp);
					log.info("resumed QueueEntry with ID=" + qeid);
					return true;
				}

				if (EnumQueueEntryStatus.Running.equals(status))
				{
					updateEntry(qe, status, m, resp);
					JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID=" + qeid + ", it is " + status.getName(), 113, EnumClass.Error);
					return true;
				}

				if (EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
				{
					updateEntry(qe, status, m, resp);
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
		 * @param response
		 * @return
		 * 
		 */
		public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
		{
			boolean modified = false;
			String sortBy = StringUtil.getNonEmpty(request.getParameter("SortBy"));
			final String filter = StringUtil.getNonEmpty(request.getParameter("filter"));
			nPos = request.getIntegerParam("pos");
			if (BambiServlet.isMyContext(request, "showQueue"))
			{
				modified = applyModification(request, modified);
			}
			else if (BambiServlet.isMyContext(request, "modifyQE"))
			{
				updateQE(request);
				modified = true;
				// ensure identical sorting as last time by undoing the sort inversion
				sortBy = lastSortBy;
				nextinvert = nextinvert == null ? lastSortBy : null;
			}
			else
			{
				return false;
			}
			final XMLDoc doc = new JDFDoc(ElementName.QUEUE);
			if (request.getBooleanParam("quiet") == false)
			{
				JDFQueue root = (JDFQueue) doc.getRoot();
				synchronized (_theQueue)
				{
					root.copyInto(_theQueue, false);
				}

				root = sortOutput(sortBy, root, filter);
				root.setAttribute(AttributeName.CONTEXT, request.getContextRoot());
				final QERetrieval qer = _parentDevice.getProperties().getQERetrieval();
				root.setAttribute("Pull", qer == QERetrieval.PULL || qer == QERetrieval.BOTH, null);
				if (_theQueue.numChildElements(ElementName.QUEUEENTRY, null) < 500)
				{
					root.setAttribute("Refresh", true, null);
				}
				root.setAttribute("pos", nPos, null);
				doc.setXSLTURL(_parentDevice.getXSLT(request));
				addOptions(root);

				try
				{
					doc.write2Stream(response.getBufferedOutputStream(), 2, true);
				}
				catch (final IOException x)
				{
					return false;
				}
				response.setContentType(UrlUtil.TEXT_XML);
			}
			if (modified)
			{
				persist(300000);
			}
			return true;
		}

		/**
		 * @param request
		 * @param modified
		 * @return
		 */
		private boolean applyModification(final BambiServletRequest request, boolean modified)
		{
			final EnumQueueStatus qStatus = _theQueue.getQueueStatus();
			EnumQueueStatus qStatusNew = null;
			final boolean bHold = request.getBooleanParam("hold");
			if (bHold)
			{
				qStatusNew = _theQueue.holdQueue();
			}
			final boolean bClose = request.getBooleanParam("close");
			if (bClose)
			{
				qStatusNew = _theQueue.closeQueue();
			}
			final boolean bResume = request.getBooleanParam("resume");
			if (bResume)
			{
				qStatusNew = _theQueue.resumeQueue();
			}
			final boolean bOpen = request.getBooleanParam("open");
			if (bOpen)
			{
				qStatusNew = _theQueue.openQueue();
			}
			final boolean bFlush = request.getBooleanParam("flush");
			if (bFlush)
			{
				final VElement v = _theQueue.flushQueue(null);
				modified = v != null;
			}
			if (qStatusNew != null)
			{
				modified = modified || !ContainerUtil.equals(qStatusNew, qStatus);
			}

			return modified;
		}

		/**
		 * the filter is case insensitive
		 * @param sortBy
		 * @param root
		 * @param filter the regexp to filter by (.)* is added before and after the filter
		 * @return
		 */
		private JDFQueue sortOutput(final String sortBy, JDFQueue root, final String filter)
		{
			root = filterList(root, filter);
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
		private JDFQueue filterList(final JDFQueue root, String filter)
		{
			if (FILTER_DIF.equals(filter))
			{
				final JDFQueue lastQueue = getLastQueue(filter);
				if (lastQueue != null)
				{
					final JDFQueueFilter f = (JDFQueueFilter) new JDFDoc(ElementName.QUEUEFILTER).getRoot();
					f.setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
					f.apply(root, lastQueue);
				}
			}
			else if (filter != null)
			{
				root.setAttribute("filter", filter);
				filter = "(.)*" + filter + "(.)*";
				final VElement v = root.getChildElementVector(ElementName.QUEUEENTRY, null);
				final String lowFilter = filter.toLowerCase();
				for (int i = 0; i < v.size(); i++)
				{
					final KElement e = v.get(i);
					if (!e.toDisplayXML(0).toLowerCase().matches(lowFilter))
					{
						e.deleteNode();
					}
				}
			}
			final VElement v = root.getChildElementVector(ElementName.QUEUEENTRY, null);
			final int size = v.size();
			root.setAttribute("TotalQueueSize", size, null);
			if (nPos < 0)
			{
				nPos = nPos + 1 + size / 500;
			}
			if ((nPos + 1) * 500 < size)
				root.setAttribute("hasNext", true, null);
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
		private boolean filterLength(final int i)
		{
			return i < nPos * 500 || i > (nPos + 1) * 500; // performance...
		}

		/**
		 * @param request
		 */
		private void updateQE(final BambiServletRequest request)
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
				qe2 = _parentDevice.stopProcessing(qeID, EnumNodeStatus.Completed);
			}
			else if (EnumQueueEntryStatus.Aborted.equals(status))
			{
				qe2 = _parentDevice.stopProcessing(qeID, EnumNodeStatus.Aborted);
			}
			else if (EnumQueueEntryStatus.Suspended.equals(status))
			{
				qe2 = _parentDevice.stopProcessing(qeID, EnumNodeStatus.Suspended);
			}
			if (qe2 != null)
			{
				qe = qe2;
			}

			updateEntry(qe, status, null, null);
			if ((EnumQueueEntryStatus.Aborted.equals(qe.getQueueEntryStatus()) || EnumQueueEntryStatus.Completed.equals(qe.getQueueEntryStatus())))
			{
				returnQueueEntry(qe, null, null);
			}
		}

		/**
		 * @param q
		 * 
		 */
		private void addOptions(final JDFQueue q)
		{
			final VElement v = q.getQueueEntryVector();
			for (int i = 0; i < v.size(); i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) v.get(i);
				// TODO select iterator based on current value
				BambiServlet.addOptionList(qe.getQueueEntryStatus(), qe.getNextStatusVector(), qe, QE_STATUS);
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
		 * @return
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
			final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
			final String qeid = qe.getQueueEntryID();

			if (EnumQueueEntryStatus.Running.equals(status))
			{
				final JDFQueueEntry returnQE = _parentDevice.stopProcessing(qeid, EnumNodeStatus.Suspended);
				final EnumQueueEntryStatus newStatus = (returnQE == null ? null : returnQE.getQueueEntryStatus());
				if (newStatus == null)
				{
					// got no response
					updateEntry(qe, EnumQueueEntryStatus.Aborted, m, resp);
					log.error("failed to suspend QueueEntry with ID=" + qeid);
				}
				else
				{
					updateEntry(qe, newStatus, m, resp);
					log.info("suspended QueueEntry with ID=" + qeid);
				}
			}
			else
			{
				updateEntry(qe, status, m, resp);
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
					return false;
				}
			}
			return true;
		}
	}

	private RollingBackupFile _queueFile = null;
	private static final long serialVersionUID = -876551736245089033L;
	String nextinvert = null;
	String lastSortBy = null;
	// protected KElement lastQueue = null;

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
		nextPush = null;
		_parentDevice = theParentDevice;
		_listeners = new Vector<Object>();
		deltaMap = new HashMap<String, QueueDelta>();
		init();
		queueMap = new QueueMap();
	}

	/**
	 * @param jmfHandler the handler to add my handlers to
	 */
	public void addHandlers(final IJMFHandler jmfHandler)
	{
		jmfHandler.addHandler(new AcknowledgeThread(this.new SubmitQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(this.new QueueStatusHandler());
		jmfHandler.addHandler(new AcknowledgeThread(this.new RemoveQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(new AcknowledgeThread(this.new HoldQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(new AcknowledgeThread(this.new AbortQueueEntryHandler(), _parentDevice));
		jmfHandler.addHandler(this.new ResumeQueueEntryHandler());
		jmfHandler.addHandler(this.new SuspendQueueEntryHandler());
		jmfHandler.addHandler(new AcknowledgeThread(this.new FlushQueueHandler(), _parentDevice));
		jmfHandler.addHandler(this.new OpenQueueHandler());
		jmfHandler.addHandler(this.new CloseQueueHandler());
		jmfHandler.addHandler(this.new HoldQueueHandler());
		jmfHandler.addHandler(this.new ResumeQueueHandler());
		jmfHandler.addHandler(this.new NewJDFQueryHandler());
		jmfHandler.addHandler(this.new ResubmitQueueEntryHandler());
	}

	protected void init()
	{
		final String deviceID = _parentDevice.getDeviceID();
		log.info("QueueProcessor construct for device '" + deviceID + "'");

		if (_queueFile == null)
		{
			_queueFile = new RollingBackupFile(_parentDevice.getBaseDir() + File.separator + "theQueue_" + deviceID + ".xml", 8);
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
		_theQueue.setMaxWaitingEntries(100000);
		_theQueue.setMaxRunningEntries(1);
		_theQueue.setDescriptiveName("Queue for " + _parentDevice.getDeviceType());
		_theQueue.setCleanupCallback(new QueueEntryCleanup()); // zapps any attached files when removing qe
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
		JDFDoc d = JDFDoc.parseFile(_queueFile.getAbsolutePath());
		if (d == null)
		{
			for (int i = 1; true; i++)
			{
				File f = _queueFile.getOldFile(i);
				if (f == null)
				{
					log.warn("Could not read queue file - starting from scratch");
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
	 * @param slaveQueueEntryID
	 * @param nodeID the JDFNode.NodeIdentifier
	 * @return
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
		if (qe == null && nodeID.getJobPartID() != null)
		{
			nodeID.setTo(nodeID.getJobID(), null, null);
			qe = queueMap.getQEFromNI(nodeID);
		}
		return qe;
	}

	/**
	 * get a qe by nodeidentifier only waiting entries that have not been forwarded to a lower level device are taken into account
	 * 
	 * @param nodeID the JDFNode.NodeIdentifier
	 * @return
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
				if (EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()) && KElement.isWildCard(BambiNSExtension.getDeviceURL(qe)))
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
	 * @return
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
				theEntry = nextPush;
				nextPush = null;
			}
			else if (canPush == QERetrieval.PUSH || canPush == QERetrieval.BOTH)
			{
				theEntry = _theQueue.getNextExecutableQueueEntry(cb);
			}
			else
			{
				theEntry = null;
			}
			if (theEntry != null)
			{
				final String proxyFlag = BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL);
				if (proxyFlag != null)
				{
					theEntry.setAttribute(proxyFlag, "true");
				}
			}
			return getIQueueEntry(theEntry);
		}
	}

	/**
	 * @param qe
	 * @return an IQueueEntry that corresponds to the qe, null if none is there
	 */
	public IQueueEntry getIQueueEntry(final JDFQueueEntry qe)
	{
		if (qe == null)
		{
			return null;
		}
		final String docURL = BambiNSExtension.getDocURL(qe);
		final JDFDoc theDoc = JDFDoc.parseURL(docURL, null);
		if (theDoc == null)
		{
			log.error("QueueProcessor in thread '" + Thread.currentThread().getName() + "' is unable to load the JDFDoc from '" + docURL + "'");
			final String proxyFlag = BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL);
			qe.setAttribute(proxyFlag, null);
			updateEntry(qe, EnumQueueEntryStatus.Aborted, null, null);
			return null;
		}

		final JDFNode n = _parentDevice.getNodeFromDoc(theDoc);
		return new QueueEntry(n, qe);
	}

	/**
	 * add a listner object that is notified of queue changes
	 * @param listner
	 */
	public void addListener(final Object listner)
	{
		log.info("adding new listener");
		_listeners.add(listner);
	}

	/**
	 * @param o
	 */
	public void removeListener(final Object o)
	{
		log.info("removing listener for " + _parentDevice != null ? _parentDevice.getDeviceID() : " unknown ");
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
	 * stub that allows moving data to and from the jdfdoc to the queueentry 
	 * 
	 * @param qe
	 * @param doc
	 * @return the updated queueEntryID
	 */
	protected String fixEntry(final JDFQueueEntry qe, final JDFDoc doc)
	{
		final JDFNode n = doc == null ? null : doc.getJDFRoot();
		if (qe == null || n == null)
		{
			return null;
		}
		final int prio = qe.getPriority();
		if (prio > 0)
		{
			final JDFNodeInfo ni = n.getCreateNodeInfo();
			if (!ni.hasAttribute(AttributeName.JOBPRIORITY))
			{
				ni.setJobPriority(prio);
			}
		}
		final String qeID = qe.getQueueEntryID();
		return qeID;
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
			if (newResponse != null)
			{
				JMFHandler.errorResponse(newResponse, "unable to queue request: No matching nodes found. Check Types and DeviceID - Error code = 101", 101, EnumClass.Error);
			}
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

			final String qeID = newQE.getQueueEntryID();
			BambiNSExtension.appendMyNSAttribute(newQE, BambiNSExtension.GOOD_DEVICES, StringUtil.setvString(canAccept));
			fixEntry(newQE, theJDF);
			extractFiles(newQE, theJDF);
			if (!storeDoc(newQE, theJDF, qsp.getReturnURL(), qsp.getReturnJMF()))
			{
				newResponse.setReturnCode(120);
				log.error("error storing queueentry: " + newResponse.getReturnCode());
				return null;
			}
			persist(300000);
			notifyListeners(qeID);
			log.info("Successfully queued new QueueEntry: QueueEntryID=" + qeID);
			newQE = _theQueue.getQueueEntry(qeID);
		}
		return newQE;
	}

	/**
	 * stub that copies url links to local storage if required
	 * 
	 * @param newQE
	 * @param doc
	 */
	public void extractFiles(JDFQueueEntry newQE, JDFDoc doc)
	{
		if (doc == null || newQE == null)
			return;
		final File jobDirectory = _parentDevice.getJobDirectory(newQE.getQueueEntryID());
		if (jobDirectory == null)
			return;
		log.info("extracting attached files to: " + jobDirectory);
		URLExtractor ex = new URLExtractor(jobDirectory, _parentDevice.getDataURL(newQE.getQueueEntryID()));
		ex.walkTree(doc.getRoot(), null);
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
		final JDFNode root = _parentDevice.getNodeFromDoc(theJDF);
		newQE.setFromJDF(root); // set jobid, jobpartid, partmaps
		final JDFQueueEntry newQEReal = _theQueue.getQueueEntry(newQEID); // the "actual" entry in the queue
		if (newQEReal == null)
		{
			log.error("error fetching queueentry: QueueEntryID=" + newQEID);
			return false;
		}
		newQEReal.copyInto(newQE, false);
		newQEReal.setFromJDF(root); // repeat for the actual entry
		queueMap.addEntry(newQEReal, true);

		final String theDocFile = _parentDevice.getJDFStorage(newQEID);
		final boolean ok = theJDF.write2File(theDocFile, 0, true);
		BambiNSExtension.setDocURL(newQEReal, theDocFile);
		BambiNSExtension.setDocModified(newQEReal, System.currentTimeMillis());
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
					hs.add(new File(StringUtil.token(docURL, -1, "/")));
				}
			}
		}
		if (crap != null)
		{
			for (final File kill : crap)
			{
				if (!hs.contains(kill))
				{
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
	 * 
	 */
	protected void persist()
	{
		synchronized (_theQueue)
		{
			log.info("persisting queue to " + _queueFile.getPath());
			long t = System.currentTimeMillis();
			if (t - lastSort > 900000) // every 15 minutes is fine
			{
				_theQueue.sortChildren();
				lastSort = t;
			}
			_queueFile.getNewFile();
			_theQueue.getOwnerDocument_KElement().write2File(_queueFile, 0, true);
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
	 */
	public JDFQueue updateEntry(final JDFQueueEntry qe, final EnumQueueEntryStatus status, final JDFMessage mess, final JDFResponse resp)
	{
		synchronized (_theQueue)
		{
			if (qe != null && status != null)
			{
				final EnumQueueEntryStatus oldStatus = qe.getQueueEntryStatus();
				final String queueEntryID = qe.getQueueEntryID();
				if (status.equals(EnumQueueEntryStatus.Removed))
				{
					qe.setQueueEntryStatus(status);
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
					}
					else if (!ContainerUtil.equals(oldStatus, status))
					{
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
					}
				}
				else if (status.equals(EnumQueueEntryStatus.Waiting))
				{
					qe.removeAttribute(AttributeName.STARTTIME);
					qe.removeAttribute(AttributeName.ENDTIME);
					// qe.removeAttribute(AttributeName.DEVICEID);
					// BambiNSExtension.setDeviceURL(qe, null);
					qe.setQueueEntryStatus(status);
				}
				else if (!ContainerUtil.equals(oldStatus, status))
				{
					qe.setQueueEntryStatus(status);
				}

				if (!ContainerUtil.equals(oldStatus, status))
				{
					persist(300000);
					notifyListeners(qe.getQueueEntryID());
				}
			}
			if (resp == null)
			{
				return null;
			}

			JDFQueueFilter qf = null;
			try
			{
				qf = mess == null ? null : mess.getQueueFilter(0);
			}
			catch (final JDFException e)
			{
				// nop
			}
			synchronized (_theQueue)
			{
				final JDFQueue q = _theQueue.copyToResponse(resp, qf, getLastQueue(resp, qf));
				removeBambiNSExtensions(q);
				return q;
			}
		}
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
	 * @return
	 */
	JDFQueue getLastQueue(final String refID)
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
	 */
	public void returnQueueEntry(final JDFQueueEntry qe, VString finishedNodes, JDFDoc docJDF)
	{
		final JDFDoc docJMF = new JDFDoc("JMF");
		final JDFJMF jmf = docJMF.getJMFRoot();
		jmf.setICSVersions(_parentDevice.getICSVersions());
		jmf.setSenderID(_parentDevice.getDeviceID());
		final JDFCommand com = (JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.ReturnQueueEntry);
		final JDFReturnQueueEntryParams returnQEParams = com.appendReturnQueueEntryParams();

		final String queueEntryID = qe.getQueueEntryID();
		returnQEParams.setQueueEntryID(queueEntryID);
		if (docJDF == null)
		{
			final String docFile = _parentDevice.getJDFStorage(qe.getQueueEntryID());
			docJDF = JDFDoc.parseFile(docFile);
		}
		if (docJDF == null)
		{
			log.error("cannot load the JDFDoc to return");
			return;
		}
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

		boolean bAborted = false;
		if (EnumNodeStatus.Completed.equals(qe.getStatus()))
		{
			returnQEParams.setCompleted(finishedNodes);
		}
		else if (EnumNodeStatus.Aborted.equals(qe.getStatus()))
		{
			returnQEParams.setAborted(finishedNodes);
			bAborted = true;
			setNodesAborted(qe, docJDF, finishedNodes);
		}

		// fix for returning
		final IConverterCallback callBack = _parentDevice.getCallback(null);
		if (callBack != null)
		{
			callBack.updateJDFForExtern(docJDF);
			callBack.updateJMFForExtern(docJMF);
		}
		// do not store the updated final returned version
		// storeDoc(qe, docJDF, null, null);
		final String returnURL = BambiNSExtension.getReturnURL(qe);
		final String returnJMF = BambiNSExtension.getReturnJMF(qe);
		final IDeviceProperties properties = _parentDevice.getProperties();
		final File deviceOutputHF = properties.getOutputHF();
		final File deviceErrorHF = properties.getErrorHF();

		boolean bOK = false;
		_parentDevice.flush();

		if (returnJMF != null)
		{
			log.info("ReturnQueueEntry for " + queueEntryID + " is being been sent to " + returnJMF);
			final QEReturn qr = properties.getReturnMIME();
			HttpURLConnection response = null;
			if (QEReturn.MIME.equals(qr))
			{
				returnQEParams.setURL("cid:dummy"); // will be overwritten by buildMimePackage
				final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, _parentDevice.getProperties().getControllerMIMEExpansion());
				final MIMEDetails mimeDetails = new MIMEDetails();
				final String devID = _parentDevice.getDeviceID();
				mimeDetails.httpDetails.chunkSize = properties.getControllerHTTPChunk();
				mimeDetails.transferEncoding = properties.getControllerMIMEEncoding();
				mimeDetails.modifyBoundarySemicolon = StringUtil.parseBoolean(properties.getDeviceAttribute("FixMIMEBoundarySemicolon"), false);
				response = _parentDevice.getJMFFactory().send2URLSynch(mp, returnJMF, _parentDevice.getCallback(null), mimeDetails, devID, 10000);
			}
			else
			// http
			{
				returnQEParams.setURL(properties.getContextURL() + "/jmb/JDFDir/" + queueEntryID + ".jdf"); // will be overwritten by buildMimePackage
				final HTTPDetails hDet = new HTTPDetails();
				hDet.chunkSize = properties.getControllerHTTPChunk();

				response = JMFFactory.getJMFFactory().send2URLSynch(jmf, returnJMF, _parentDevice.getCallback(null), _parentDevice.getDeviceID(), 10000);
			}
			int responseCode;
			if (response != null)
			{
				try
				{
					responseCode = response.getResponseCode();
				}
				catch (final IOException x)
				{
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
		}

		if (!bOK && returnURL != null)
		{
			try
			{
				log.info("JDF Document for " + queueEntryID + " is being been sent to " + returnURL);
				final JDFDoc d = docJDF.write2URL(returnURL);
				// TODO error handling
				bOK = d != null;
			}
			catch (final Exception e)
			{
				log.error("failed to send ReturnQueueEntry: " + e);
			}
		}
		if (!bOK)
		{
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
		}
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

	/**
	 * @param qe
	 * @param docJDF
	 * @param finishedNodes
	 */
	private void setNodesAborted(final JDFQueueEntry qe, final JDFDoc docJDF, final VString finishedNodes)
	{
		if (docJDF == null)
		{
			return;
		}
		final JDFNode root = docJDF.getJDFRoot();
		if (root == null)
		{
			return;
		}
		final JDFNotification not = root.getCreateAuditPool().addNotification(EnumClass.Warning, null, qe.getPartMapVector());
		final JDFComment notificationComment = not.appendComment();
		notificationComment.setLanguage("en");
		notificationComment.setText("Node aborted in queue entry: " + qe.getQueueEntryID());
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	protected JDFQueueEntry getMessageQueueEntry(final JDFMessage m, final JDFResponse resp)
	{
		final JDFQueueEntryDef def = m.getQueueEntryDef(0);
		if (def == null)
		{
			log.error("Message contains no QueueEntryDef");
			return null;
		}

		final String qeid = def.getQueueEntryID();
		if (KElement.isWildCard(qeid))
		{
			log.error("QueueEntryID does not contain any QueueEntryID");
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
	 * @param response
	 * @return
	 */
	public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
	{
		boolean b = this.new QueueGetHandler().handleGet(request, response);
		if (!b)
		{
			b = new ShowJDFHandler(_parentDevice).handleGet(request, response);
		}
		if (!b)
		{
			b = new ShowXJDFHandler(_parentDevice).handleGet(request, response);
		}
		return b;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param m
	 * @param resp
	 * @return
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

		if (EnumQueueEntryStatus.Completed.equals(status))
		{
			updateEntry(qe, status, m, resp);
			JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID=" + qeid + ", it is already completed", 114, EnumClass.Error);
			return true;
		}
		else if (EnumQueueEntryStatus.Aborted.equals(status))
		{
			updateEntry(qe, status, m, resp);
			JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID=" + qeid + ", it is already aborted", 113, EnumClass.Error);
			return true;
		}
		else if (EnumQueueEntryStatus.Waiting.equals(status)) // no need to check processors - it is still waiting
		{
			updateEntry(qe, EnumQueueEntryStatus.Aborted, m, resp);
		}
		final String queueEntryID = qe.getQueueEntryID();
		final JDFQueueEntry returnQE = _parentDevice.stopProcessing(queueEntryID, EnumNodeStatus.Aborted);

		// has to be waiting, held, running or suspended: abort it!
		EnumQueueEntryStatus newStatus = (returnQE == null ? null : returnQE.getQueueEntryStatus());
		if (newStatus == null)
		{
			newStatus = EnumQueueEntryStatus.Aborted;
		}
		updateEntry(qe, newStatus, m, resp);
		if (EnumQueueEntryStatus.Aborted.equals(newStatus))
		{
			returnQueueEntry(qe, null, null);
		}
		log.info("aborted QueueEntry with ID=" + qeid);
		return true;
	}

	/**
	 * default shutdown method
	 */
	public void shutdown()
	{
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
		if (slaveQEID != null)
		{
			BambiNSExtension.setSlaveQueueEntryID(qe, slaveQEID);
			queueMap.addEntry(qe, false);
		}
	}
}
