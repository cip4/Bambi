/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core.queues;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.BambiNotifyDef;
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
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.messaging.SignalDispatcher;
import org.cip4.jdflib.auto.JDFAutoNotification.EnumClass;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumUpdateGranularity;
import org.cip4.jdflib.auto.JDFAutoSubmissionMethods.EnumPackaging;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFComment;
import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.extensions.XJDFZipReader;
import org.cip4.jdflib.jmf.JDFAbortQueueEntryParams;
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
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.RollingBackupFile;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.URLReader;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.cip4.jdflib.util.thread.DelayedPersist;
import org.cip4.jdflib.util.thread.IPersistable;
import org.cip4.jdflib.util.thread.MutexMap;
import org.cip4.jdflib.util.thread.MyMutex;
import org.cip4.jdflib.util.thread.RegularJanitor;
import org.cip4.jdflib.util.thread.TimeSweeper;
import org.cip4.jdflib.util.zip.ZipReader;
import org.cip4.lib.jdf.jsonutil.JSONReader;

/**
 * @author rainer prosi
 */
public class QueueProcessor extends BambiLogFactory implements IPersistable
{

	private static final int PERSIST_MS = 420000;

	/**
	 * class that handles queue differences
	 *
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG July 6, 2009
	 */
	protected class QueueDelta implements Runnable
	{
		protected final JDFQueue lastQueue;

		/**
		 * @return
		 */
		protected JDFQueue getLastQueue()
		{
			return lastQueue;
		}

		private final long creationTime;

		/**
		 *
		 */
		protected QueueDelta()
		{
			lastQueue = createLastQueue();
			creationTime = System.currentTimeMillis();
			if (!RegularJanitor.getJanitor().hasSweeper(this))
			{
				RegularJanitor.getJanitor().addSweeper(new TimeSweeper(42 * 60, this), true);
			}
		}

		/**
		 * @return
		 */
		protected JDFQueue createLastQueue()
		{
			return cloneQueue();
		}

		/**
		 * clean up stored queues that have not been touched for a while
		 */
		protected void cleanOrphans()
		{
			final List<String> keys = ContainerUtil.getKeyList(deltaMap);
			if (keys != null)
			{
				for (final String key : keys)
				{
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

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "QueueDelta [lastQueue.size=" + (lastQueue == null ? 0 : lastQueue.numEntries(null)) + ", creationTime=" + creationTime + "]";
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			cleanOrphans();
		}
	}

	/**
	 * class to quickly retrieve queueentries based on slave qeids - required by proxies
	 *
	 * @author Rainer Prosi, Heidelberger Druckmaschinen
	 */
	protected class SlaveQueueMap
	{
		private final HashMap<String, JDFQueueEntry> slaveQeIDMap;
		private final HashMap<NodeIdentifier, JDFQueueEntry> niMap;
		private final HashSet<NodeIdentifier> niNull;

		/**
		 *
		 */
		public SlaveQueueMap()
		{
			slaveQeIDMap = new HashMap<>();
			niMap = new HashMap<>();
			niNull = new HashSet<>();
			fill(_theQueue.get());
		}

		/**
		 * @param queue
		 */
		protected void fill(final JDFQueue queue)
		{
			reset();
			if (queue == null)
			{
				return;
			}
			synchronized (queue)
			{
				final VElement v = queue.getQueueEntryVector();
				for (final KElement e : v)
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
				slaveQeIDMap.put(slaveqeID, qe);
			}
			if (clearNull)
			{
				niNull.clear();
			}
		}

		/**
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
				qe = getQueue().getQueueEntry(ni, 0);
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
		 * @param slaveqeID the qeID in the slave system
		 * @return the local queueentry
		 */
		protected JDFQueueEntry getQEFromSlaveQEID(final String slaveqeID)
		{
			return slaveqeID == null ? null : slaveQeIDMap.get(slaveqeID);
		}

		/**
		 *
		 */
		public void reset()
		{
			slaveQeIDMap.clear();
			niMap.clear();
			niNull.clear();
		}

		/**
		 * remove a slave qe from the map
		 *
		 * @param qe
		 */
		protected void removeEntry(final JDFQueueEntry qe)
		{
			final String slaveqeID = BambiNSExtension.getSlaveQueueEntryID(qe);
			if (slaveqeID != null)
			{
				slaveQeIDMap.remove(slaveqeID);
			}
		}

		/**
		 * @return
		 */
		protected int size()
		{
			return slaveQeIDMap == null ? 0 : slaveQeIDMap.size();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "QueueMap [size= " + slaveQeIDMap.size() + " null size= " + niNull.size() + " ]";
		}
	}

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG July 6, 2009
	 */
	protected class CanExecuteCallBack extends ExecuteCallback
	{

		/**
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
	 * @author rainer prosi
	 * @date before Feb 20, 2013
	 */
	protected class SubmitQueueEntryHandler extends AbstractHandler
	{
		/**
		 *
		 */
		public SubmitQueueEntryHandler()
		{
			super(EnumType.SubmitQueueEntry, new EnumFamily[] { EnumFamily.Command });
		}

		/**
		 * @param m
		 * @return
		 */
		protected JDFDoc getDocFromMessage(final JDFMessage m)
		{
			final JDFQueueSubmissionParams qsp = m == null ? null : m.getQueueSubmissionParams(0);
			JDFDoc doc = qsp == null ? null : qsp.getURLDoc();
			if (doc == null && qsp != null)
			{
				final String url = qsp.getURL();
				if (!StringUtil.isEmpty(url))
				{
					final ZipReader zipReader = qsp.getOwnerDocument_KElement().getZipReader();
					doc = getDocFromXJDFZip(url, zipReader);
					if (doc == null)
					{
						final URLReader r = new URLReader(url);
						try
						{
							r.setBodyPart(m.getOwnerDocument_JDFElement().getMultiPart().getBodyPart(0));
						}
						catch (final MessagingException e1)
						{
							// nop
						}
						final JSONReader jr = new JSONReader();
						jr.setXJDF();
						final KElement e = jr.getElement(r.getURLInputStream());
						if (e != null)
						{
							doc = new JDFDoc(e.getOwnerDocument());
						}
					}
				}
			}
			final IConverterCallback callback = doc == null ? null : _parentDevice.getCallback(null);
			if (callback != null)
			{
				doc = callback.prepareJDFForBambi(doc);
			}
			return doc;
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

			final JDFDoc doc = getDocFromMessage(m);
			processDoc(m, resp, doc);
			// this is not a bug the return of processdoc is success - this true is handled (at all)
			return true;
		}

		/**
		 * @param m
		 * @param resp
		 * @param doc
		 * @return true if success
		 */
		protected boolean processDoc(final JDFMessage m, final JDFResponse resp, final JDFDoc doc)
		{
			final JDFQueueSubmissionParams qsp = m.getQueueSubmissionParams(0);
			final boolean bRet;
			if (qsp == null)
			{
				JMFHandler.errorResponse(resp, "QueueSubmissionParams are missing or invalid", 9, EnumClass.Error);
				bRet = false;
			}
			else if (doc == null)
			{
				updateEntry(null, null, m, resp, null);
				String errorMsg = "failed to get JDFDoc from '" + qsp.getURL() + "' on SubmitQueueEntry";
				errorMsg += "\nin thread: " + Thread.currentThread().getName();
				JMFHandler.errorResponse(resp, errorMsg, 9, EnumClass.Error);
				bRet = false;
			}
			else
			{
				final JDFQueueEntry qe = addEntry((JDFCommand) m, resp, doc);
				int rc = resp.getReturnCode();
				if (qe == null && rc == 0)
				{
					log.warn("whazzup: rc=0 but no queue entry");
					rc = 112;
				}

				if (rc == 0)
				{
					resp.removeChild(ElementName.QUEUEENTRY, null, 0);
					final JDFQueueEntry qeNew = (JDFQueueEntry) resp.copyElement(qe, null);
					BambiNSExtension.removeBambiExtensions(qeNew);
					updateEntry(qe, null, m, resp, null);
				}
				else if (rc == 112)
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
				bRet = rc == 0;
			}
			return bRet;
		}
	}

	/**
	 * handler for the resubmitqueueentry message
	 *
	 * @author rainer prosi
	 * @date Nov 13, 2011
	 */
	protected class ResubmitQueueEntryHandler extends AbstractHandler
	{
		/**
		 *
		 */
		public ResubmitQueueEntryHandler()
		{
			super(EnumType.ResubmitQueueEntry, new EnumFamily[] { EnumFamily.Command });
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
			log.debug("Handling  ResubmitQueueEntry");
			final JDFResubmissionParams qsp = m.getResubmissionParams(0);
			if (qsp != null)
			{
				final String qeID = qsp.getQueueEntryID();
				final JDFQueueEntry qe = getQueueEntry(qeID);
				if (qe == null)
				{
					JMFHandler.errorResponse(resp, "unknown QueueEntryID: " + qeID, 105, EnumClass.Error);
				}
				else
				{
					final JDFDoc doc = getDocFromMessage(m);
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
		 * @return
		 */
		protected JDFDoc getDocFromMessage(final JDFMessage m)
		{
			final JDFResubmissionParams rsp = m.getResubmissionParams(0);
			JDFDoc doc = rsp == null ? null : rsp.getURLDoc();
			if (doc == null && rsp != null)
			{
				final String url = rsp.getURL();
				final ZipReader zipReader = rsp.getOwnerDocument_KElement().getZipReader();
				doc = getDocFromXJDFZip(url, zipReader);
			}
			final IConverterCallback callback = doc == null ? null : _parentDevice.getCallback(null);
			if (callback != null)
			{
				doc = callback.prepareJDFForBambi(doc);
			}
			return doc;
		}

		/**
		 * @param m
		 * @param resp
		 * @param qe
		 * @param doc
		 */
		protected void handleValidURL(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe, final JDFDoc doc)
		{
			final String qeID = qe.getQueueEntryID();
			final boolean canResubmit = _parentDevice.canResubmit(doc.getJDFRoot(), qeID);
			if (!canResubmit)
			{
				JMFHandler.errorResponse(resp, "unable to queue resubmit request", 101, EnumClass.Error);
			}
			else
			{
				updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp, null);
				_parentDevice.fixEntry(qe, doc);
				final DataExtractor dataExtractor = _parentDevice.getDataExtractor(true);
				if (dataExtractor != null)
				{
					dataExtractor.extractFiles(qe, doc);
				}
				storeDoc(qe, doc, null, null);
			}
		}

		/**
		 * @param m
		 * @param resp
		 * @param qsp
		 */
		protected void handleInvalidURL(final JDFMessage m, final JDFResponse resp, final JDFResubmissionParams qsp)
		{
			updateEntry(null, null, m, resp, null);
			String errorMsg = "failed to read JDFDoc from '" + qsp.getURL() + "' on SubmitQueueEntry";
			errorMsg += "\r\nin thread: " + Thread.currentThread().getName();
			JMFHandler.errorResponse(resp, errorMsg, 9, EnumClass.Error);
		}
	}

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG July 10, 2009
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
				JMFHandler.errorResponse(resp, "missing Params or missing JobID in NewJDFQuParams", 7, EnumClass.Error);
				return false;
			}
			final String qeID = StringUtil.getNonEmpty(nqParams.getQueueEntryID());
			JDFQueueEntry qe = null;
			if (qeID != null)
			{
				qe = getQueueEntry(qeID);
			}
			final NodeIdentifier identifier = nqParams.getIdentifier();
			if (qe == null)
			{
				qe = getQueue().getQueueEntry(identifier, 0);
			}
			if (qe == null && nqParams.hasAttribute(AttributeName.JOBPARTID))
			{
				qe = getQueue().getQueueEntry(new NodeIdentifier(nqParams.getJobID(), null, null), 0);
			}
			if (qe == null)
			{
				log.warn("could not find queueentry for " + identifier);
				return false;
			}
			final IQueueEntry iqe = getIQueueEntry(qe, true);
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
	 *
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG 03.12.2008
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
			copyToMessage(m, resp);
			return true;
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

	/**
	 * @author rainerprosi
	 */
	abstract class AbortRemoveHandler extends AbstractHandler
	{

		AbortRemoveHandler(final EnumType _type, final EnumFamily[] _families)
		{
			super(_type, _families);
		}

		protected void abortSingleEntry(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe)
		{
			final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
			final String qeid = qe.getQueueEntryID();
			JDFNode theNode = null;
			if (EnumQueueEntryStatus.Completed.equals(status))
			{
				updateEntry(qe, status, m, resp, null);
				JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID=" + qeid + ", it is already completed", 114, EnumClass.Error);
				return;
			}
			else if (EnumQueueEntryStatus.Aborted.equals(status))
			{
				updateEntry(qe, status, m, resp, null);
				JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID=" + qeid + ", it is already aborted", 113, EnumClass.Error);
				return;
			}
			else if (EnumQueueEntryStatus.Waiting.equals(status)) // no need to check processors - it is still waiting
			{
				final IQueueEntry iQueueEntry = getIQueueEntry(qe, true);
				theNode = iQueueEntry == null ? null : iQueueEntry.getJDF();
				updateEntry(qe, EnumQueueEntryStatus.Aborted, m, resp, null);
			}
			final String queueEntryID = qe.getQueueEntryID();
			EnumNodeStatus nodestatus = EnumNodeStatus.Aborted;
			final JDFAbortQueueEntryParams aqp = (JDFAbortQueueEntryParams) m.getElement(ElementName.ABORTQUEUEENTRYPARAMS);
			if (aqp != null)
			{
				final EnumNodeStatus endStatus = aqp.getEndStatus();
				if (endStatus != null)
				{
					nodestatus = endStatus;
				}
			}
			final JDFQueueEntry returnQE = _parentDevice.stopProcessing(queueEntryID, nodestatus, null);

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
					final IQueueEntry iQueueEntry = getIQueueEntry(qe, true);
					theNode = iQueueEntry == null ? null : iQueueEntry.getJDF();
				}
				final JDFDoc theDoc = theNode == null ? null : theNode.getOwnerDocument_JDFElement();
				returnQueueEntry(qe, null, theDoc, newStatus);
			}
			updateEntry(qe, newStatus, m, resp, null);
			log.info("aborted QueueEntry with ID=" + qeid + " new status=" + newStatus.getName());
		}

	}

	/**
	 *
	 *
	 *
	 */
	protected class RemoveQueueEntryHandler extends AbortRemoveHandler
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
			log.info("received RemoveQueueEntry - message ID: " + m.getID());

			final List<JDFQueueEntry> v = getMessageQueueEntries(m, resp);
			if (v == null)
			{
				return true;
			}
			for (final JDFQueueEntry qe : v)
			{
				removeSingleEntry(m, resp, qe);
			}
			return true;
		}

		protected void removeSingleEntry(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe)
		{
			final String qeid = qe.getQueueEntryID();
			EnumQueueEntryStatus status = qe.getQueueEntryStatus();

			if (EnumQueueEntryStatus.Held.equals(status) || EnumQueueEntryStatus.Waiting.equals(status))
			{
				abortSingleEntry(m, resp, qe); // abort before removing
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
		}
	}

	protected class HoldQueueEntryHandler extends AbstractHandler
	{

		public HoldQueueEntryHandler()
		{
			super(EnumType.HoldQueueEntry, new EnumFamily[] { EnumFamily.Command });
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
			log.debug("Handling " + m.getType());
			final List<JDFQueueEntry> v = getMessageQueueEntries(m, resp);
			if (v == null)
			{
				return true;
			}
			for (final JDFQueueEntry qe : v)
			{
				holdSingleEntry(m, resp, qe);
			}
			return true;
		}

		protected void holdSingleEntry(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe)
		{
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
				}
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	protected class HoldQueueHandler extends ModifyQueueStatusHandler
	{
		@Override
		protected EnumQueueStatus getNewStatus()
		{
			return getQueue().holdQueue();
		}

		public HoldQueueHandler()
		{
			super(EnumType.HoldQueue);
		}
	}

	protected class CloseQueueHandler extends ModifyQueueStatusHandler
	{
		@Override
		protected EnumQueueStatus getNewStatus()
		{
			return getQueue().closeQueue();
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
			return getQueue().openQueue();
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
			return getQueue().resumeQueue();
		}

		public ResumeQueueHandler()
		{
			super(EnumType.ResumeQueue);
		}
	}

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG 03.12.2008
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
				if (!ContainerUtil.equals(newStatus, getQueue().getQueueStatus()))
				{
					getQueue().setQueueStatus(newStatus);
				}
				updateEntry(null, null, m, resp, null);
			}
			return true;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////

	protected class AbortQueueEntryHandler extends AbortRemoveHandler
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
			final List<JDFQueueEntry> v = getMessageQueueEntries(m, resp);
			if (v == null)
			{
				return true;
			}
			log.info("received AbortQueueEntry - message ID: " + m.getID());
			for (final JDFQueueEntry qe : v)
			{
				abortSingleEntry(m, resp, qe);
			}
			return true;
		}

	}

	protected class ResumeQueueEntryHandler extends AbstractHandler
	{

		public ResumeQueueEntryHandler()
		{
			super(EnumType.ResumeQueueEntry, new EnumFamily[] { EnumFamily.Command });
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
			final List<JDFQueueEntry> v = getMessageQueueEntries(m, resp);
			if (v == null)
			{
				return true;
			}
			for (final JDFQueueEntry qe : v)
			{
				resumeSingleEntry(m, resp, qe);
			}
			return true;
		}

		protected void resumeSingleEntry(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe)
		{
			final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
			final String qeid = qe.getQueueEntryID();

			if (EnumQueueEntryStatus.Suspended.equals(status))
			{
				updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp, null);
				log.info("resumed QueueEntry to waiting with ID=" + qeid);
			}
			else if (EnumQueueEntryStatus.Held.equals(status))
			{
				updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp, null);
				log.info("resumed QueueEntry with ID=" + qeid);
			}
			else if (EnumQueueEntryStatus.Running.equals(status))
			{
				updateEntry(qe, status, m, resp, null);
				JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID=" + qeid + ", it is " + status.getName(), 113, EnumClass.Error);
			}
			else if (EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
			{
				updateEntry(qe, status, m, resp, null);
				JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID=" + qeid + ", it is already " + status.getName(), 115, EnumClass.Error);
			}
		}
	}

	protected class FlushQueueHandler extends AbstractHandler
	{

		public FlushQueueHandler()
		{
			super(EnumType.FlushQueue, new EnumFamily[] { EnumFamily.Command });
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
			log.debug("Handling " + m.getType());
			final JDFFlushQueueParams fqp = m.getFlushQueueParams(0);
			final JDFQueueFilter qf = fqp == null ? null : fqp.getQueueFilter();
			final JDFQueueFilter qfo = m.getQueueFilter(0);
			final VElement zapped = getQueue().flushQueue(qf);
			getQueue().copyToResponse(resp, qfo, null);
			final JDFFlushQueueInfo flushQueueInfo = resp.appendFlushQueueInfo();
			flushQueueInfo.setQueueEntryDefsFromQE(zapped);
			persist(0);

			removeOrphanJDFs();

			return true;
		}

	}

	public class QueueGetHandler implements IGetHandler
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

		protected static final String FILTER_DIF = "_FILTER_DIF_";

		/**
		 * @param request
		 * @return the xmlresponse to the get request
		 */
		@Override
		public XMLResponse handleGet(final ContainerRequest request)
		{
			boolean modified = false;
			String sortBy = StringUtil.getNonEmpty(request.getParameter("SortBy"));
			final String filter = StringUtil.getNonEmpty(request.getParameter("filter"));
			nPos = request.getParameter("pos") == null ? lastPos : request.getIntegerParam("pos");
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
			}
			else
			{
				return null;
			}
			final JDFQueue root;

			if (request.getBooleanParam("quiet") == false)
			{
				root = sortOutput(sortBy, filter);
				root.setAttribute(AttributeName.CONTEXT, _parentDevice.getContext(request));
				final QERetrieval qer = _parentDevice.getProperties().getQERetrieval();
				root.setAttribute("Pull", qer == QERetrieval.PULL || qer == QERetrieval.BOTH, null);
				if (getQueue().numChildElements(ElementName.QUEUEENTRY, null) < 500)
				{
					root.setAttribute("Refresh", true, null);
				}
				root.setAttribute("sortby", sortBy, null);
				root.getOwnerDocument_JDFElement().setXSLTURL(_parentDevice.getXSLT(request));
				addOptions(root);
			}
			else
			{
				final XMLDoc doc = new JDFDoc(ElementName.QUEUE);
				root = (JDFQueue) doc.getRoot();
			}
			final XMLResponse response = new XMLResponse(root);
			if (modified)
			{
				persist(PERSIST_MS);
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
			final EnumQueueStatus queueStatusCurrent = getQueue().getQueueStatus();
			EnumQueueStatus queueStatusNew = null;
			final boolean bHold = request.getBooleanParam("hold");
			if (bHold)
			{
				queueStatusNew = applyHold();
			}
			final boolean bClose = request.getBooleanParam("close");
			if (bClose)
			{
				queueStatusNew = applyClose();
			}
			final boolean bResume = request.getBooleanParam("resume");
			if (bResume)
			{
				queueStatusNew = applyResume();
			}
			final boolean bOpen = request.getBooleanParam("open");
			if (bOpen)
			{
				queueStatusNew = applyOpen();
			}
			final boolean bFlush = request.getBooleanParam("flush");
			if (bFlush)
			{
				final VElement v = applyFlush();
				modified = v != null;
			}
			if (queueStatusNew != null)
			{
				modified = modified || !ContainerUtil.equals(queueStatusNew, queueStatusCurrent);
			}

			if (modified && queueStatusNew != null)
			{
				BambiNotifyDef.getInstance().notifyDeviceQueueStatus(getQueue().getDeviceID(), queueStatusNew.getName(), getQueueStatistic());
			}

			return modified;
		}

		protected VElement applyFlush()
		{
			final VElement v = getQueue().flushQueue(null);
			return v;
		}

		public EnumQueueStatus applyOpen()
		{
			final EnumQueueStatus qStatusNew = getQueue().openQueue();
			BambiNotifyDef.getInstance().notifyDeviceQueueStatus(getQueue().getDeviceID(), qStatusNew.getName(), getQueueStatistic());
			return qStatusNew;
		}

		public EnumQueueStatus applyResume()
		{
			final EnumQueueStatus qStatusNew = getQueue().resumeQueue();
			BambiNotifyDef.getInstance().notifyDeviceQueueStatus(getQueue().getDeviceID(), qStatusNew.getName(), getQueueStatistic());
			return qStatusNew;
		}

		public EnumQueueStatus applyClose()
		{
			final EnumQueueStatus qStatusNew = getQueue().closeQueue();
			BambiNotifyDef.getInstance().notifyDeviceQueueStatus(getQueue().getDeviceID(), qStatusNew.getName(), getQueueStatistic());
			return qStatusNew;
		}

		public EnumQueueStatus applyHold()
		{
			final EnumQueueStatus qStatusNew = getQueue().holdQueue();
			BambiNotifyDef.getInstance().notifyDeviceQueueStatus(getQueue().getDeviceID(), qStatusNew.getName(), getQueueStatistic());
			return qStatusNew;
		}

		/**
		 * the filter is case insensitive
		 *
		 * @param sortBy
		 * @param filter the regexp to filter by (.)* is added before and after the filter
		 * @return
		 */
		private JDFQueue sortOutput(final String sortBy, final String filter)
		{
			final JDFQueue root = filterList(filter);
			// sort according to the given attribute
			if (sortBy != null)
			{
				boolean invert = sortBy.equals(lastSortBy) && lastPos == nPos;
				if (invert && lastinvert)
				{
					invert = false;
				}
				lastinvert = invert;
				lastSortBy = sortBy;
				if (sortBy.endsWith("Time"))
				{
					invert = !invert; // initial sort should be late first
				}
				root.sortChildren(new KElement.SingleAttributeComparator(sortBy, invert));
			}
			else
			{
				lastSortBy = null;
			}
			final VElement v = root.getChildElementVector(ElementName.QUEUEENTRY, null);
			final int size = v.size();
			root.setAttribute("TotalQueueSize", size, null);
			if (nPos < 0)
			{
				nPos = nPos + 1 + size / 500;
			}
			if (nPos * 500 > size)
			{
				nPos = 0;
			}
			root.setAttribute("pos", nPos, null);
			lastPos = nPos;
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
		 * filter the queue by string
		 *
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
				nPos = 0;
				if (lastQueue != null)
				{
					final JDFQueueFilter f = (JDFQueueFilter) new JDFDoc(ElementName.QUEUEFILTER).getRoot();
					f.setUpdateGranularity(EnumUpdateGranularity.ChangesOnly);
					root = f.copy(getQueue(), lastQueue, null);
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
				final boolean invert = "!".equals(StringUtil.leftStr(filter, 1));
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
				for (final KElement e : v)
				{
					if (StringUtil.matchesIgnoreCase(e.toValueString((char) 1), filter) == invert)
					{
						e.deleteNode();
					}
				}
			}
			else
			{
				root = cloneQueue();
			}

			return root;
		}

		/**
		 * TODO add support for next
		 *
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

			JDFQueueEntry qe = getQueue().getQueueEntry(qeID);
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
		 */
		protected void addOptions(final JDFQueue q)
		{
			final Collection<JDFQueueEntry> v = q.getAllQueueEntry();
			if (v != null)
			{
				for (final JDFQueueEntry qe : v)
				{
					final List<EnumQueueEntryStatus> nextStatusVector = qe.getNextStatusVector();
					final EnumQueueEntryStatus queueEntryStatus = qe.getQueueEntryStatus();
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
			final List<JDFQueueEntry> v = getMessageQueueEntries(m, resp);
			if (v == null)
			{
				return true;
			}
			for (final JDFQueueEntry qe : v)
			{
				suspendSingleEntry(m, resp, qe);
			}
			return true;
		}

		protected void suspendSingleEntry(final JDFMessage m, final JDFResponse resp, final JDFQueueEntry qe)
		{
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
				}
			}
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
				final Vector<EnumPackaging> v = new Vector<>();
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
	boolean lastinvert = false;
	String lastSortBy = null;
	int lastPos = 0;

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

	private final AtomicReference<JDFQueue> _theQueue;
	private final List<MyMutex> _listeners;
	protected AbstractDevice _parentDevice = null;
	protected long lastSort = 0;
	protected final HashMap<String, QueueDelta> deltaMap;

	protected JDFQueueEntry nextPush = null;
	private CanExecuteCallBack _cbCanExecute = null;
	private final SlaveQueueMap slaveQueueMap;
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
		_theQueue = new AtomicReference<>();
		nextPush = null;
		_parentDevice = theParentDevice;
		_listeners = new ArrayList<>();
		deltaMap = new HashMap<>();
		slaveQueueMap = new SlaveQueueMap();
		_mutexMap = new MutexMap<>();
		init();
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

	/**
	 *
	 */
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
	protected void setQueueProperties(final String deviceID)
	{
		final JDFQueue q = getQueue();
		q.setAutomated(true);
		q.setDeviceID(deviceID);
		q.setMaxCompletedEntries(getMaxCompleted());
		q.setMaxWaitingEntries(getMaxWaiting());
		q.setMaxRunningEntries(getMaxRunning());
		q.setDescriptiveName("Queue for " + _parentDevice.getDeviceType());
		q.setCleanupCallback(_parentDevice.getQECleanup()); // zapps any attached files when removing qe
		_cbCanExecute = getCanExecuteCallback(deviceID);
		q.setExecuteCallback(_cbCanExecute);
		BambiNSExtension.setMyNSAttribute(q, "EnsureNS", "Dummy"); // ensure that some bambi ns exists
	}

	/**
	 * @param deviceID
	 * @return
	 */
	public CanExecuteCallBack getCanExecuteCallback(final String deviceID)
	{
		return new CanExecuteCallBack(deviceID, BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL));
	}

	/**
	 * @return
	 */
	public int getMaxRunning()
	{
		return 1;
	}

	/**
	 * @return
	 */
	public int getMaxWaiting()
	{
		return -1;
	}

	/**
	 * @return
	 */
	public int getMaxCompleted()
	{
		return 100;
	}

	/**
	 * @param deviceID
	 */
	private void startupParent(final String deviceID)
	{
		JDFDoc d = readQueueFile();
		if (d != null)
		{
			final JDFQueue q = (JDFQueue) d.getRoot();
			log.info("refreshing queue");
			_theQueue.set(q);
			boolean bHold = false;
			final VElement qev = q.getQueueEntryVector();
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
				q.holdQueue();
			}
			setQueueProperties(deviceID);
		}
		else
		{
			d = new JDFDoc(ElementName.QUEUE);
			final JDFQueue q = (JDFQueue) d.getRoot();
			log.info("creating new queue");
			_theQueue.set(q);
			q.setQueueStatus(EnumQueueStatus.Waiting);
			setQueueProperties(deviceID);
		}
		removeOrphanJDFs();
	}

	/**
	 * @return
	 */
	protected JDFDoc readQueueFile()
	{
		final boolean exist = _queueFile.exists();
		JDFDoc d = exist ? JDFDoc.parseFile(_queueFile.getAbsolutePath()) : null;
		if (d == null)
		{
			for (int i = 1; true; i++)
			{
				final File f = _queueFile.getOldFile(i);
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
	 *
	 * @param slaveQueueEntryID the qeid in the context of the slave device
	 * @param nodeID the JDFNode.NodeIdentifier
	 * @return the queue entry
	 */
	public JDFQueueEntry getQueueEntry(final String slaveQueueEntryID, NodeIdentifier nodeID)
	{
		JDFQueueEntry qe = slaveQueueMap.getQEFromSlaveQEID(slaveQueueEntryID);
		if (nodeID == null || (slaveQueueEntryID != null && qe == null))
		{
			return qe;
		}
		if (searchByJobPartID)
		{
			if (qe == null)
			{
				qe = slaveQueueMap.getQEFromNI(nodeID);
			}

			if (qe == null && nodeID.getPartMapVector() != null)
			{
				nodeID = new NodeIdentifier(nodeID); // copy because we zapp internally
				nodeID.setTo(nodeID.getJobID(), nodeID.getJobPartID(), null);
				qe = slaveQueueMap.getQEFromNI(nodeID);
			}
		}
		if (qe == null && (!searchByJobPartID || nodeID.getJobPartID() != null))
		{
			nodeID.setTo(nodeID.getJobID(), null, null);
			qe = slaveQueueMap.getQEFromNI(nodeID);
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

		final VElement vQE = getQueue().getQueueEntryVector(nodeID);
		if (vQE != null)
		{
			final int siz = vQE.size();
			for (int i = 0; i < siz; i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) vQE.get(i);
				final boolean waiting = EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()) || EnumQueueEntryStatus.Suspended.equals(qe.getQueueEntryStatus());
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
	 *
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
		JDFQueueEntry theEntry;
		synchronized (_theQueue)
		{
			if (nextPush != null && (cb == null || cb.canExecute(nextPush))) // we have an explicit selection
			{
				log.info("retrieving push qe: " + nextPush.getQueueEntryID());
				theEntry = nextPush;
				nextPush = null;
			}
			else if (canPush == QERetrieval.PUSH || canPush == QERetrieval.BOTH)
			{
				theEntry = getQueue().getNextExecutableQueueEntry(cb);
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
				log.info("new qe: " + theEntry.getQueueEntryID());
			}
		}
		return getIQueueEntry(theEntry);
	}

	/**
	 * @param qeID
	 * @param waitForDoc if true, return null if no doc exists
	 * @return an IQueueEntry that corresponds to qeID, null if none is there
	 */
	public IQueueEntry getIQueueEntry(final String qeID, final boolean waitForDoc)
	{
		final JDFQueueEntry qe = getQueueEntry(qeID);
		if (qe == null || waitForDoc && BambiNSExtension.getDocURL(qe) == null)
		{
			return null;
		}
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
	 * @param waitForDoc if true we wait for an existing doc and also we do not check whether it is executable
	 * @return an IQueueEntry that corresponds to the qe, null if none is there
	 */
	public IQueueEntry getIQueueEntry(final JDFQueueEntry qe, final boolean waitForDoc)
	{
		if (qe == null)
		{
			return null;
		}

		final String docURL = BambiNSExtension.getDocURL(qe);
		JDFDoc theDoc = null;
		for (int i = 1; i < 42; i++)
		{
			synchronized (getMutexForQE(qe))
			{
				theDoc = JDFDoc.parseURL(docURL, null);
			}
			if (theDoc == null)
			{
				log.warn("waiting for get: " + qe.getQueueEntryID());

				if (!ThreadUtil.sleep(10 * i))
				{
					break;
				}
			}
			else
			{
				break;
			}
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

		final JDFNode n = waitForDoc && theDoc != null ? theDoc.getJDFRoot() : _parentDevice.getNodeFromDoc(theDoc);
		return new QueueEntry(n, qe);
	}

	/**
	 * @param qe
	 * @return
	 */
	protected MyMutex getMutexForQE(final JDFQueueEntry qe)
	{
		final String queueEntryID = qe == null ? null : qe.getQueueEntryID();
		return getMutexForQeID(queueEntryID);
	}

	/**
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
	 *
	 * @param listner
	 */
	public void addListener(final MyMutex listner)
	{
		log.info("adding new queue listener");
		_listeners.add(listner);
	}

	/**
	 * @param mutex
	 */
	public void removeListener(final MyMutex mutex)
	{
		log.info("removing listener for " + (_parentDevice != null ? _parentDevice.getDeviceID() : " unknown "));
		_listeners.remove(mutex);
	}

	/**
	 * returns null if the device cannot process the jdf ticket
	 *
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
		final JDFQueueSubmissionParams qsp = submitQueueEntry.getQueueSubmissionParams(0);
		if (qsp == null)
		{
			log.error("error submitting new queueentry");
			return null;
		}

		final JDFResponse r2;
		synchronized (_theQueue)
		{
			r2 = qsp.addEntry(getQueue(), null, submitQueueEntry.getQueueFilter(0));
		}

		if (newResponse != null)
		{
			newResponse.copyInto(r2, false);
		}
		else
		{
			newResponse = r2;
		}
		if (newResponse.getReturnCode() != 0)
		{
			log.warn("invalid response while adding queue entry: rc=" + newResponse.getReturnCode() + " queue status=" + getQueue().getStatus());
			return null;
		}

		newQE = newResponse.getQueueEntry(0);
		if (newQE == null)
		{
			log.warn("error submitting queueentry: " + newResponse.getReturnCode());
			return null;
		}
		final String qeID = newQE.getQueueEntryID();

		BambiNSExtension.appendMyNSAttribute(newQE, BambiNSExtension.GOOD_DEVICES, StringUtil.setvString(canAccept));
		_parentDevice.fixEntry(newQE, theJDF);

		extractToJob(theJDF, newQE);

		if (!storeDoc(newQE, theJDF, qsp.getReturnURL(), qsp.getReturnJMF()))
		{
			newResponse.setReturnCode(120);
			log.error("error storing queueentry: " + qeID + " " + newResponse.getReturnCode());
			return null;
		}
		notifyListeners(qeID);
		JDFQueueEntry ret = waitForEntry(theJDF, qeID, newQE);
		if (ret == null)
		{
			newResponse.setReturnCode(120);
			log.error("error creating queueentry: " + qeID + " " + newResponse.getReturnCode());
			return null;

		}
		prepareSubmit(ret);
		incrmentTotal();
		return ret;
	}

	void incrmentTotal()
	{
		BambiNSExtension.incrmentTotal(_theQueue.get());
	}

	protected JDFQueueEntry waitForEntry(final JDFDoc theJDF, final String qeID, JDFQueueEntry newQE)
	{
		JDFQueueEntry ret = null;
		for (int i = 1; i < 42; i++)
		{
			synchronized (getMutexForQE(newQE))
			{
				ret = getQueueEntry(qeID);
			}
			if (ret == null)
			{
				ThreadUtil.sleep(i * 10);
				log.warn("waiting for " + qeID);
			}
			else
			{
				log.info("Successfully queued new QueueEntry: QueueEntryID=" + qeID + " / " + theJDF.getJDFRoot().getJobID(true));
				persist(PERSIST_MS);
				break;
			}
		}
		return ret;
	}

	protected void extractToJob(final JDFDoc theJDF, final JDFQueueEntry newQE)
	{
		final DataExtractor dataExtractor = _parentDevice.getDataExtractor(true);
		if (dataExtractor != null)
		{
			dataExtractor.extractFiles(newQE, theJDF);
		}
	}

	/**
	 * prepare qe for submission
	 *
	 * @param newQE
	 */
	protected void prepareSubmit(final JDFQueueEntry newQE)
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
		final JDFQueueEntry newQEReal = getQueueEntry(newQEID); // the "actual" entry in the queue
		if (newQEReal == null)
		{
			log.error("error fetching queueentry: QueueEntryID=" + newQEID);
			return false;
		}
		newQEReal.copyInto(newQE, false);
		slaveQueueMap.addEntry(newQEReal, true);
		final JDFQueue q = getQueue();
		BambiNotifyDef.getInstance().notifyDeviceJobAdded(q.getDeviceID(), newQEReal.getQueueEntryID(), newQEReal.getQueueEntryStatus().getName(),
				newQEReal.getSubmissionTime().getTimeInMillis());
		BambiNotifyDef.getInstance().notifyDeviceQueueStatus(q.getDeviceID(), q.getQueueStatus().getName(), getQueueStatistic());

		final boolean ok = storeJDF(theJDF, newQEID);
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

	public JDFQueueEntry getQueueEntry(final String newQEID)
	{
		return getQueue().getQueueEntry(newQEID);
	}

	/**
	 * store the JDF again
	 *
	 * @param theJDF
	 * @param newQEID
	 * @return
	 */
	public boolean storeJDF(final JDFDoc theJDF, final String newQEID)
	{
		boolean ok;
		final JDFQueueEntry newQEReal = getQueueEntry(newQEID); // the "actual" entry in the queue
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
			hs = new HashSet<>();
			final VElement v = getQueue().getQueueEntryVector();
			for (int i = 0; i < v.size(); i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) v.get(i);
				final String docURL = BambiNSExtension.getDocURL(qe);
				if (docURL != null)
				{
					hs.add(new File(StringUtil.token(docURL, getMaxWaiting(), File.separator)));
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
					final File dataDir = new File(UrlUtil.newExtension(kill.getAbsolutePath(), null));
					FileUtil.deleteAll(dataDir);
					kill.delete();
					log.warn("removing orphan JDF:" + kill.getName());
				}
			}
		}
	}

	protected void notifyListeners(final String qeID)
	{
		for (final MyMutex mutex : _listeners)
		{
			ThreadUtil.notifyAll(mutex);
		}
		final SignalDispatcher signalDispatcher = _parentDevice.getSignalDispatcher();
		signalDispatcher.triggerQueueEntry(qeID, null, getMaxWaiting(), EnumType.QueueStatus.getName());
	}

	/**
	 * asynchronous make the memory queue persistent
	 *
	 * @param milliseconds length of time wait until persist, if 0 force persist
	 */
	public void persist(final long milliseconds)
	{
		DelayedPersist.getDelayedPersist().queue(this, milliseconds);
	}

	/**
	 * make the memory queue persistent
	 *
	 * @return true if ok
	 */
	@Override
	public boolean persist()
	{
		synchronized (_theQueue)
		{
			log.info("persisting queue to " + _queueFile.getPath() + " size: " + getQueue().numEntries(null));
			final long t = System.currentTimeMillis();
			if (t - lastSort > 900000) // every 15 minutes is fine
			{
				getQueue().sortChildren();
				lastSort = t;
			}
			_queueFile.getNewFile();
			return getQueue().getOwnerDocument_KElement().write2File(_queueFile, 0, true);
		}
	}

	/**
	 * @return the queue element
	 */
	public JDFQueue getQueue()
	{
		return _theQueue.get();
	}

	/**
	 * @return the queue element
	 */
	public void setQueue(final JDFQueue queue)
	{
		_theQueue.set(queue);
	}

	/**
	 * @return the parent device
	 */
	protected AbstractDevice getParent()
	{
		return _parentDevice;
	}

	/**
	 * update the QueueEntry qe to be in the new status
	 *
	 * @param qe the QueueEntry to update
	 * @param status the updated QueueEntry status
	 * @param mess the message that triggers the update - may be null
	 * @param resp the message response to be filled - may be null
	 * @return JDFQueue the updated queue in its new status
	 * @deprecated use 5 parameter version
	 */
	@Deprecated
	public JDFQueue updateEntry(final JDFQueueEntry qe, final EnumQueueEntryStatus status, final JDFMessage mess, final JDFResponse resp)
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
	 * @return JDFQueue the updated queue in its new status
	 */
	public JDFQueue updateEntry(JDFQueueEntry qe, final EnumQueueEntryStatus status, final JDFMessage mess, final JDFResponse resp, String statusDetails)
	{
		final JDFQueue q = getQueue();
		if (qe == null || StringUtil.isEmpty(qe.getQueueEntryID()))
		{
			log.error("cannot update qe with no qeID");
			return q;
		}
		statusDetails = StringUtil.getNonEmpty(statusDetails);
		synchronized (_theQueue)
		{
			final JDFQueueEntry qe2 = getQueueEntry(qe.getQueueEntryID());
			if (qe2 != qe)
			{
				if (qe2 == null)
				{
					log.error("no such queueentry: " + qe.getQueueEntryID());
					return q;
				}
				else
				{
					log.warn("not updating QE - using original from Queue " + qe2.getQueueEntryID());
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
					slaveQueueMap.removeEntry(qe);

					BambiNotifyDef.getInstance().notifyDeviceJobRemoved(q.getDeviceID(), qe.getQueueEntryID());
					BambiNotifyDef.getInstance().notifyDeviceQueueStatus(q.getDeviceID(), q.getQueueStatus().getName(), getQueueStatistic());

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
						q.setAutomated(false);
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Suspended);
						q.setAutomated(true);
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
						qe.setStatusDetails(statusDetails);
					}
					else if (!ContainerUtil.equals(oldStatus, status))
					{
						qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
						qe.setStatusDetails(statusDetails);
					}

					BambiNotifyDef.getInstance().notifyDeviceJobPropertiesChanged(q.getDeviceID(), qe.getQueueEntryID(), qe.getQueueEntryStatus().getName(), getStartTime(qe),
							getEndTime(qe));
					BambiNotifyDef.getInstance().notifyDeviceQueueStatus(q.getDeviceID(), q.getQueueStatus().getName(), getQueueStatistic());
				}
				else if (status.equals(EnumQueueEntryStatus.Waiting))
				{
					qe.removeAttribute(AttributeName.STARTTIME);
					qe.removeAttribute(AttributeName.ENDTIME);
					qe.removeAttribute(AttributeName.DEVICEID);
					qe.setQueueEntryStatus(status);
					qe.setStatusDetails(statusDetails);

					BambiNotifyDef.getInstance().notifyDeviceJobPropertiesChanged(q.getDeviceID(), qe.getQueueEntryID(), qe.getQueueEntryStatus().getName(), 0, 0);
					BambiNotifyDef.getInstance().notifyDeviceQueueStatus(q.getDeviceID(), q.getQueueStatus().getName(), getQueueStatistic());
				}
				else if (status.equals(EnumQueueEntryStatus.Aborted) || status.equals(EnumQueueEntryStatus.Completed) || status.equals(EnumQueueEntryStatus.Suspended))
				{
					qe.removeAttribute(AttributeName.DEVICEID);
					BambiNSExtension.setDeviceURL(qe, null);
					qe.setQueueEntryStatus(status);
					qe.setStatusDetails(statusDetails);

					BambiNotifyDef.getInstance().notifyDeviceJobPropertiesChanged(q.getDeviceID(), qe.getQueueEntryID(), qe.getQueueEntryStatus().getName(), getStartTime(qe),
							getEndTime(qe));
					BambiNotifyDef.getInstance().notifyDeviceQueueStatus(q.getDeviceID(), q.getQueueStatus().getName(), getQueueStatistic());
				}
				else if (!ContainerUtil.equals(oldStatus, status))
				{
					qe.setQueueEntryStatus(status);
					qe.setStatusDetails(statusDetails);

					BambiNotifyDef.getInstance().notifyDeviceJobPropertiesChanged(q.getDeviceID(), qe.getQueueEntryID(), qe.getQueueEntryStatus().getName(), getStartTime(qe),
							getEndTime(qe));
					BambiNotifyDef.getInstance().notifyDeviceQueueStatus(q.getDeviceID(), q.getQueueStatus().getName(), getQueueStatistic());
				}

				if (!ContainerUtil.equals(oldStatus, status))
				{
					persist(PERSIST_MS);
					notifyListeners(qe.getQueueEntryID());
				}
			}

			final JDFQueue q2 = resp == null ? null : copyToMessage(mess, resp);
			return q2;
		}
	}

	/**
	 * @param mess
	 * @param resp
	 * @return
	 */
	protected JDFQueue copyToMessage(final JDFMessage mess, final JDFResponse resp)
	{
		final JDFQueueFilter qf = mess == null ? null : mess.getQueueFilter(0);
		JDFQueue q = getQueue().copyToResponse(resp, qf, getLastQueue(resp, qf));
		if (q != null)
		{
			q.setQueueSize(slaveQueueMap.size());
			// we have an empty queue
			removeBambiNSExtensions(q);
			if (qf != null && EnumUpdateGranularity.ChangesOnly.equals(qf.getUpdateGranularity()) && q.getQueueEntry(0) == null)
			{
				resp.deleteNode();
				q = null;
			}
		}

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
		final QueueDelta queueDelta = getQueueDelta();
		if (queueDelta == null)
		{
			deltaMap.remove(refID);
		}
		deltaMap.put(refID, queueDelta);
		if (delta == null)
		{
			return null;
		}
		return delta.getLastQueue();
	}

	/**
	 * @return
	 */
	protected QueueDelta getQueueDelta()
	{
		return new QueueDelta();
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		String s = "[QueueProcessor: ] Status= " + getQueue().getQueueStatus().getName() + " Num Entries: " + getQueue().numEntries(null) + "\n Queue:\n";
		s += _theQueue.toString();
		return s;
	}

	/**
	 * @param qe
	 * @param finishedNodes
	 * @param docJDF
	 * @param newStatus
	 * @return
	 */
	public boolean returnQueueEntry(final JDFQueueEntry qe, final VString finishedNodes, final JDFDoc docJDF, final EnumQueueEntryStatus newStatus)
	{
		final QueueEntryReturn queueEntryReturn = new QueueEntryReturn(qe, newStatus);
		return queueEntryReturn.returnQueueEntry(finishedNodes, docJDF);
	}

	class QueueEntryReturn
	{
		final JDFQueueEntry qe;
		final IDeviceProperties properties;
		final String queueEntryID;

		/**
		 * @param newStatus
		 */
		QueueEntryReturn(final JDFQueueEntry qe, final EnumQueueEntryStatus newStatus)
		{
			super();
			this.qe = (JDFQueueEntry) qe.clone();
			if (newStatus != null)
				this.qe.setQueueEntryStatus(newStatus);
			properties = _parentDevice.getProperties();
			queueEntryID = qe.getQueueEntryID();
		}

		/**
		 * @param finishedNodes
		 * @param docJDF
		 */
		boolean returnQueueEntry(VString finishedNodes, JDFDoc docJDF)
		{
			final JMFBuilder jmfBuilder = _parentDevice.getJMFBuilder();

			final JDFJMF jmf = jmfBuilder.buildReturnQueueEntry(queueEntryID);
			jmf.getOwnerDocument_JDFElement();
			jmf.setICSVersions(_parentDevice.getICSVersions());
			final JDFCommand com = jmf.getCommand(0);
			final JDFReturnQueueEntryParams returnQEParams = com.getReturnQueueEntryParams(0);

			if (docJDF == null || docJDF.getJDFRoot() == null)
			{
				final IQueueEntry iQueueEntry = getIQueueEntry(qe, true);
				final JDFNode node = iQueueEntry == null ? null : iQueueEntry.getJDF();
				if (node != null)
				{
					docJDF = node.getOwnerDocument_JDFElement();
				}
			}
			if (docJDF == null || docJDF.getJDFRoot() == null)
			{
				log.error("cannot load the JDFDoc to return");
				return false;
			}
			finishedNodes = updateFinishedNodes(finishedNodes, docJDF);

			boolean bAborted = false;
			if (EnumNodeStatus.Completed.equals(qe.getStatus()))
			{
				returnQEParams.setCompleted(finishedNodes);
				log.info("Received return request for completed entry: " + queueEntryID);
			}
			else if (EnumNodeStatus.Aborted.equals(qe.getStatus()))
			{
				returnQEParams.setAborted(finishedNodes);
				bAborted = true;
				setNodesAborted(docJDF, finishedNodes);
				log.info("Received return request for aborted entry: " + queueEntryID);
			}

			return reallyReturn(docJDF, jmf, bAborted);
		}

		boolean reallyReturn(JDFDoc docJDF, final JDFJMF jmf, final boolean bAborted)
		{
			boolean bOK = false;
			_parentDevice.flush();

			final String returnJMF = BambiNSExtension.getReturnJMF(qe);
			if (UrlUtil.isHttp(returnJMF) || UrlUtil.isHttps(returnJMF))
			{
				bOK = returnJMF(docJDF, jmf);
			}

			if (!bOK && docJDF != null && _parentDevice.getCallback(null) != null)
			{
				docJDF = _parentDevice.getCallback(null).updateJDFForExtern(docJDF);
			}
			final String returnURL = BambiNSExtension.getReturnURL(qe);
			if (!bOK && returnURL != null && docJDF != null)
			{
				bOK = returnJDFUrl(docJDF);
			}
			if (!bOK)
			{
				bOK = returnHF(docJDF, bAborted);
			}
			removeSubscriptions();
			return bOK;
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

		private boolean returnHF(final JDFDoc docJDF, final boolean bAborted)
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
				final KElement jdfRoot = docJDF.getRoot();
				final String jobID = jdfRoot == null ? "null" : jdfRoot.getAttribute(AttributeName.JOBID);
				log.warn("No return URL, No HF, No Nothing specified, bailing out: " + jobID);
			}
			return bOK;
		}

		private boolean returnJDFUrl(final JDFDoc docJDF)
		{
			boolean bOK = false;
			final String returnURL = BambiNSExtension.getReturnURL(qe);

			try
			{
				log.info("JDF Document for " + queueEntryID + " is being been sent to " + returnURL);
				final JDFDoc d = docJDF.write2URL(returnURL);
				if (d == null)
				{
					log.warn("JDF Document for " + queueEntryID + " has not been sent to " + returnURL);
				}
				bOK = d != null;
			}
			catch (final Throwable e)
			{
				log.error("failed to send ReturnQueueEntry: " + e);
			}
			return bOK;
		}

		private boolean returnJMF(final JDFDoc docJDF, final JDFJMF jmf)
		{
			final String returnJMF = BambiNSExtension.getReturnJMF(qe);
			log.info("ReturnQueueEntry for " + queueEntryID + " is being been sent to " + returnJMF);
			final QEReturn qr = properties.getReturnMIME();
			HttpURLConnection response = null;
			final JDFReturnQueueEntryParams returnQEParams = jmf.getCommand(0).getReturnQueueEntryParams(0);
			if (QEReturn.MIME.equals(qr))
			{
				returnQEParams.setURL("cid:dummy"); // will be overwritten by buildMimePackage
				final MIMEDetails mimeDetails = new MIMEDetails();
				final String devID = _parentDevice.getDeviceID();
				mimeDetails.httpDetails.setChunkSize(properties.getControllerHTTPChunk());
				mimeDetails.transferEncoding = properties.getControllerMIMEEncoding();
				mimeDetails.modifyBoundarySemicolon = StringUtil.parseBoolean(properties.getDeviceAttribute("FixMIMEBoundarySemicolon"), false);
				final JDFNode jdfRoot = docJDF == null ? null : docJDF.getJDFRoot();
				if (jdfRoot == null)
				{
					log.error("No JDF root; root=" + ((docJDF == null) ? "null" : docJDF.getRoot().getNodeName()));
				}
				response = _parentDevice.getJMFFactory().send2URLSynch(jmf, jdfRoot, returnJMF, _parentDevice.getCallback(null), mimeDetails, devID, 10000);
			}
			else
			// http
			{
				returnQEParams.setURL(properties.getContextURL() + "/jmb/JDFDir/" + queueEntryID + ".jdf"); // will be overwritten by buildMimePackage
				final HTTPDetails hDet = new HTTPDetails();
				hDet.setChunkSize(properties.getControllerHTTPChunk());

				response = JMFFactory.getInstance().send2URLSynch(jmf, returnJMF, _parentDevice.getCallback(null), _parentDevice.getDeviceID(), 10000);
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
				if (UrlUtil.isReturnCodeOK(responseCode))
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

		/**
		 * ensure valid finished nodes
		 *
		 * @param finishedNodes
		 * @param docJDF
		 * @return
		 */
		public VString updateFinishedNodes(VString finishedNodes, final JDFDoc docJDF)
		{
			if (ContainerUtil.isEmpty(finishedNodes))
			{
				final JDFNode n = docJDF.getJDFRoot();
				if (n == null)
				{
					log.warn("no root node - whazzup? ");
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
		protected void setNodesAborted(final JDFDoc docJDF, final VString finishedNodes)
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
			notificationComment.setText("process aborted; types: " + root.getTypesString());
			log.warn("Node aborted in queue entry: " + qe.getQueueEntryID() + " JobID=" + root.getJobID(true));
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "QueueEntryReturn [status=" + qe.getQueueEntryStatus() + ", queueEntryID=" + queueEntryID + "]";
		}
	}

	/**
	 *
	 */
	protected List<JDFQueueEntry> getMessageQueueEntries(final JDFMessage m, final JDFResponse resp)
	{
		final JDFQueueEntryDef def = m.getQueueEntryDef(0);
		if (def == null)
		{
			final JDFQueueFilter qf = (JDFQueueFilter) m.getXPathElement("*/QueueFilter");
			if (qf == null)
			{
				JMFHandler.errorResponse(resp, "Message contains no QueueFilter", 105, EnumClass.Error);
				return null;
			}
			else if (qf.getAttributeMap().isEmpty() && qf.getQueueEntryDef(0) == null)
			{
				JMFHandler.errorResponse(resp, "Message contains empty QueueFilter", 105, EnumClass.Error);
				return null;
			}
			else
			{
				final int maxEnt = qf.getMaxEntries();
				qf.setMaxEntries(Integer.MAX_VALUE);
				final JDFQueue q = qf.copy(getQueue(), null, resp);
				qf.setMaxEntries(maxEnt);
				if (q == null || q.numEntries(null) == 0)
				{
					JMFHandler.errorResponse(resp, "found no QueueEntry matching filter ", 105, EnumClass.Error);
					return null;
				}
				else
				{
					final List<JDFQueueEntry> childrenByClass = q.getChildArrayByClass(JDFQueueEntry.class, false, -1);
					final List<JDFQueueEntry> ret = new ArrayList<>();
					for (final JDFQueueEntry qe : childrenByClass)
					{
						final JDFQueueEntry mine = getQueueEntry(qe.getQueueEntryID());
						if (mine != null)
						{
							ret.add(mine);
						}
					}
					return ret.isEmpty() ? null : ret;
				}
			}
		}
		else
		{

			final String qeid = def.getQueueEntryID();
			if (KElement.isWildCard(qeid))
			{
				JMFHandler.errorResponse(resp, "QueueEntryDef does not contain any QueueEntryID", 105, EnumClass.Error);
				return null;
			}
			log.info("processing getMessageQueueEntryID for " + qeid);
			final JDFQueueEntry qe = getQueueEntry(qeid);
			if (qe == null)
			{
				JMFHandler.errorResponse(resp, "found no QueueEntry with QueueEntryID=" + qeid, 105, EnumClass.Error);
				return null;
			}
			final List<JDFQueueEntry> v = new ArrayList<>();
			v.add(qe);
			return v;
		}

	}

	/**
	 *
	 */
	protected JDFQueueEntry getMessageQueueEntry(final JDFMessage m, final JDFResponse resp)
	{
		final List<JDFQueueEntry> v = getMessageQueueEntries(m, resp);
		if (ContainerUtil.isEmpty(v))
		{
			return null;
		}
		return v.get(0);

	}

	/**
	 * remove all Bambi namespace extensions from a given queue
	 *
	 * @param queue the queue to filter
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
	 *
	 * @return
	 */
	protected ShowXJDFHandler getShowXJDFHandler()
	{
		return new ShowXJDFHandler(_parentDevice);
	}

	/**
	 * hook to overwrite the ShowJDFHandler
	 *
	 * @return
	 */
	protected ShowJDFHandler getShowJDFHandler()
	{
		return new ShowJDFHandler(_parentDevice);
	}

	/**
	 * @return
	 */
	public QueueGetHandler getQueueGetHandler()
	{
		return new QueueGetHandler();
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
		getQueue().flushQueue(null);
		getQueue().setQueueStatus(EnumQueueStatus.Waiting);
		removeOrphanJDFs();
		_queueFile.clearAll();
		slaveQueueMap.reset();
		getQueueStatistic();
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
			slaveQueueMap.removeEntry(qe);
		else
			slaveQueueMap.addEntry(qe, false);
	}

	/**
	 * create a complete clone of theQueue
	 *
	 * @return the clone
	 */
	protected JDFQueue cloneQueue()
	{
		return (JDFQueue) getQueue().cloneNewDoc();
	}

	public class QueueStatistic
	{
		public final int waiting;
		public final int running;
		public final int completed;
		public final int all;

		/**
		 *
		 */
		QueueStatistic()
		{
			synchronized (_theQueue)
			{
				final JDFQueue queue = getQueue();
				waiting = queue.numEntries(EnumQueueEntryStatus.Waiting);
				running = queue.numEntries(EnumQueueEntryStatus.Running);
				completed = queue.numEntries(EnumQueueEntryStatus.Completed);
				all = queue.numEntries(null);
			}

		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "QueueStatistic [waiting=" + waiting + ", running=" + running + ", completed=" + completed + ", all=" + all;
		}
	}

	/**
	 * @return
	 */
	public QueueStatistic getQueueStatistic2()
	{
		return new QueueStatistic();
	}

	/**
	 * @return
	 */
	protected String getQueueStatistic()
	{
		String result = "${W}/${R}/${C}/${ALL}";

		final QueueStatistic stat = new QueueStatistic();

		result = StringUtils.replaceOnce(result, "${W}", JDFConstants.EMPTYSTRING + stat.waiting);
		result = StringUtils.replaceOnce(result, "${R}", JDFConstants.EMPTYSTRING + stat.running);
		result = StringUtils.replaceOnce(result, "${C}", JDFConstants.EMPTYSTRING + stat.completed);
		result = StringUtils.replaceOnce(result, "${ALL}", JDFConstants.EMPTYSTRING + stat.all);
		return result;
	}

	private long getStartTime(final JDFQueueEntry qe)
	{
		if (qe.getStartTime() == null)
		{
			return 0;
		}
		return qe.getStartTime().getTimeInMillis();
	}

	private long getEndTime(final JDFQueueEntry qe)
	{
		if (qe.getEndTime() == null)
		{
			return 0;
		}
		return qe.getEndTime().getTimeInMillis();
	}

	/**
	 * @param qeNew
	 * @return
	 */
	public boolean wasSubmitted(final JDFQueueEntry qeNew)
	{
		return _parentDevice.wasSubmitted(qeNew);
	}

	/**
	 * @param url
	 * @param zipReader
	 * @return
	 */
	protected JDFDoc getDocFromXJDFZip(final String url, final ZipReader zipReader)
	{
		JDFDoc doc = null;
		if (UrlUtil.isRelativeURL(url) && zipReader != null)
		{
			final XJDFZipReader xzr = new XJDFZipReader(zipReader);
			xzr.setPath(url);
			xzr.convertXJDF();
			final JDFNode node = xzr.getJDFRoot();
			doc = node == null ? null : node.getOwnerDocument_JDFElement();
		}
		return doc;
	}
}
