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
package org.cip4.bambi.core.queues;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Vector;

import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.BambiServletRequest;
import org.cip4.bambi.core.BambiServletResponse;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.SignalDispatcher;
import org.cip4.bambi.core.IDeviceProperties.QEReturn;
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
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.RollingBackupFile;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;

/**
 * 
 * @author rainer
 * 
 * 
 */
public class QueueProcessor
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
			return true;
		}
	}

	/**
	 * cleans up the garbage that belongs to a queueentry when the qe is removed
	 * @author prosirai
	 * 
	 */
	protected class QueueEntryCleanup extends CleanupCallback
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.jdflib.jmf.JDFQueue.CleanupCallback#cleanEntry(org.cip4.jdflib.jmf.JDFQueueEntry)
		 */
		@Override
		public void cleanEntry(final JDFQueueEntry qe)
		{
			final String theDocFile = getJDFStorage(qe.getQueueEntryID());
			if (theDocFile != null)
			{
				final File f = new File(theDocFile);
				f.delete();
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
					return true;
				}
				else
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
					final int canAccept = _parentDevice.canAccept(doc, qeID);
					if (canAccept != 0)
					{
						JMFHandler.errorResponse(resp, "unable to queue request", canAccept, EnumClass.Error);
						return true;
					}
					else
					{
						updateEntry(qe, EnumQueueEntryStatus.Waiting, m, resp);
						storeDoc(qe, doc, null, null);
						return true;
					}
				}
			}
			JMFHandler.errorResponse(resp, "QueueSubmissionParams are missing or invalid", 9, EnumClass.Error);
			log.error("QueueSubmissionParams are missing or invalid");
			return true;
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
			if (EnumQueueEntryStatus.Held.equals(status) || EnumQueueEntryStatus.Waiting.equals(status) || EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
			{
				final String queueEntryID = qe.getQueueEntryID();
				final JDFQueueEntry returnQE = _parentDevice.stopProcessing(queueEntryID, null); // use null to flag a removal
				updateEntry(qe, EnumQueueEntryStatus.Removed, m, resp);
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

		/**
		 * 
		 */
		private void removeOrphanJDFs()
		{
			final File[] crap = FileUtil.listFilesWithExtension(_parentDevice.getJDFDir(), "jdf");
			if (crap != null)
			{
				for (final File kill : crap)
				{
					kill.delete();
				}
			}
		}
	}

	protected class QueueGetHandler extends XMLDoc implements IGetHandler
	{
		/**
		 * 
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
		 */
		public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
		{
			boolean modified = false;
			String sortBy = StringUtil.getNonEmpty(request.getParameter("SortBy"));
			final String filter = StringUtil.getNonEmpty(request.getParameter("filter"));
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
			final KElement root = setRoot(ElementName.QUEUE, null);
			synchronized (_theQueue)
			{
				root.mergeElement(_theQueue, false);
			}

			sortOutput(sortBy, root, filter);
			root.setAttribute(AttributeName.CONTEXT, request.getContextRoot());
			if (_theQueue.numChildElements(ElementName.QUEUEENTRY, null) < 1000)
			{
				root.setAttribute("Refresh", true, null);
			}
			setXSLTURL(_parentDevice.getXSLT(SHOW_QUEUE, request.getContextPath()));
			addOptions();

			try
			{
				write2Stream(response.getBufferedOutputStream(), 2, true);
			}
			catch (final IOException x)
			{
				return false;
			}
			response.setContentType(UrlUtil.TEXT_XML);
			if (modified)
			{
				persist(0);
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
		 */
		private void sortOutput(final String sortBy, final KElement root, final String filter)
		{
			filterList(root, filter);
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
		}

		/**
		 * filter the queue by string
		 * @param root
		 * @param filter
		 */
		private void filterList(final KElement root, String filter)
		{
			if (filter != null)
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
		}

		/**
		 * @param request
		 */
		private void updateQE(final HttpServletRequest request)
		{
			final String qeID = request.getParameter(QE_ID);
			if (qeID == null)
			{
				return;
			}
			JDFQueueEntry qe = _theQueue.getQueueEntry(qeID);
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
			if (qe != null && (EnumQueueEntryStatus.Aborted.equals(qe.getQueueEntryStatus()) || EnumQueueEntryStatus.Completed.equals(qe.getQueueEntryStatus())))
			{
				returnQueueEntry(qe, null, null);
			}
		}

		/**
		 * 
		 */
		private void addOptions()
		{
			final JDFQueue q = (JDFQueue) getRoot();
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

	protected final Log log;
	private RollingBackupFile _queueFile = null;
	private static final long serialVersionUID = -876551736245089033L;
	String nextinvert = null;
	String lastSortBy = null;

	/**
	 */
	static final String QE_STATUS = "qeStatus";
	public static final String QE_ID = "qeID";
	static final String isJDF = "isJDF";
	static final String SHOW_QUEUE = "showQueue";
	static final String SHOW_JDF = "showJDF";
	static final String SHOW_XJDF = "showXJDF";
	static final String MODIFY_QE = "modifyQE";

	protected JDFQueue _theQueue;
	private final Vector<Object> _listeners;
	protected AbstractDevice _parentDevice = null;
	private long lastPersist = 0;
	protected final HashMap<String, QueueDelta> deltaMap;

	/**
	 * @param theParentDevice
	 */
	public QueueProcessor(final AbstractDevice theParentDevice)
	{
		super();
		log = LogFactory.getLog(QueueProcessor.class.getName());
		_parentDevice = theParentDevice;
		_listeners = new Vector<Object>();
		deltaMap = new HashMap<String, QueueDelta>();
		init();
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
		_theQueue.setExecuteCallback(new CanExecuteCallBack(deviceID, BambiNSExtension.getMyNSString(BambiNSExtension.deviceURL)));
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
			final int qSize = qev == null ? 0 : qev.size();
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
	}

	/**
	 * @return
	 */
	private JDFDoc readQueueFile()
	{
		JDFDoc d = JDFDoc.parseFile(_queueFile.getAbsolutePath());
		if (d == null)
		{
			d = JDFDoc.parseFile(_queueFile.getAbsolutePath() + ".bak");
			if (d != null)
			{
				log.warn("problems reading queue file - using backup");
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

		JDFQueueEntry qe = BambiNSExtension.getSlaveQueueEntry(_theQueue, slaveQueueEntryID);
		if (qe == null && nodeID != null)
		{
			qe = _theQueue.getQueueEntry(nodeID, 0);
		}
		if (nodeID == null)
		{
			return qe;
		}

		if (qe == null && nodeID.getPartMapVector() != null)
		{
			nodeID = new NodeIdentifier(nodeID); // copy because we zapp internally
			nodeID.setTo(nodeID.getJobID(), nodeID.getJobPartID(), null);
			qe = _theQueue.getQueueEntry(nodeID, 0);
		}
		if (qe == null && nodeID.getJobPartID() != null)
		{
			nodeID.setTo(nodeID.getJobID(), null, null);
			qe = _theQueue.getQueueEntry(nodeID, 0);
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
		final int siz = vQE == null ? 0 : vQE.size();
		for (int i = 0; i < siz; i++)
		{
			final JDFQueueEntry qe = (JDFQueueEntry) vQE.get(i);
			if (EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()) && KElement.isWildCard(BambiNSExtension.getDeviceURL(qe)))
			{
				return getIQueueEntry(qe);
				// try next
			}
		}
		return null;
	}

	/**
	 * get the next queue entry only waiting entries that have not been forwarded to a lower level device are taken into account
	 * @param deviceID
	 * @return
	 */
	public IQueueEntry getNextEntry(final String deviceID)
	{
		synchronized (_theQueue)
		{

			final JDFQueueEntry theEntry = _theQueue.getNextExecutableQueueEntry();
			if (theEntry != null)
			{
				if (deviceID != null)
				{
					theEntry.setDeviceID(deviceID);
				}
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
	 * @see org.cip4.bambi.IQueueProcessor#removeListener(java.lang.Object)
	 */
	public void removeListener(final Object o)
	{
		log.info("removing listener for " + _parentDevice != null ? _parentDevice.getDeviceID() : " unknown ");
		_listeners.remove(o);
	}

	/**
	 * stub that allows moving data from the jdfdoc to the queueentry
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
		final int canAccept = _parentDevice.canAccept(theJDF, null);
		if (canAccept != 0)
		{
			if (newResponse != null)
			{
				JMFHandler.errorResponse(newResponse, "unable to queue request", canAccept, EnumClass.Error);
			}
			return null;
		}

		synchronized (_theQueue)
		{

			final JDFQueueSubmissionParams qsp = submitQueueEntry.getQueueSubmissionParams(0);
			if (qsp == null)
			{
				log.error("error submitting new queueentry");
				return null;
			}

			final JDFResponse r2 = qsp.addEntry(_theQueue, null);
			if (newResponse != null)
			{
				newResponse.mergeElement(r2, false);
			}
			else
			{
				newResponse = r2;
			}
			if (newResponse == null || newResponse.getReturnCode() != 0)
			{
				log.warn("invalid response while adding queue entry");
				return null;
			}

			final JDFQueueEntry newQE = newResponse.getQueueEntry(0);

			if (newResponse.getReturnCode() != 0 || newQE == null)
			{
				log.warn("error submitting queueentry: " + newResponse.getReturnCode());
				return null;
			}
			final String qeID = newQE.getQueueEntryID();

			fixEntry(newQE, theJDF);
			if (!storeDoc(newQE, theJDF, qsp.getReturnURL(), qsp.getReturnJMF()))
			{
				newResponse.setReturnCode(120);
				log.error("error storing queueentry: " + newResponse.getReturnCode());
				return null;
			}
			persist(0);
			notifyListeners(qeID);
			return _theQueue.getQueueEntry(qeID);
		}
	}

	/**
	 * @param newQE
	 * @param theJDF
	 * @param returnURL the returnURL to add to the qe
	 * @param returnJMF
	 * @return true if successful
	 */
	public boolean storeDoc(JDFQueueEntry newQE, final JDFDoc theJDF, final String returnURL, final String returnJMF)
	{
		if (newQE == null || theJDF == null)
		{
			log.error("error storing queueentry");
			return false;
		}
		final String newQEID = newQE.getQueueEntryID();
		final JDFNode root = _parentDevice.getNodeFromDoc(theJDF);
		newQE.setFromJDF(root); // set jobid, jobpartid, partmaps
		newQE = _theQueue.getQueueEntry(newQEID); // the "actual" entry in the queue
		if (newQE == null)
		{
			log.error("error fetching queueentry: QueueEntryID=" + newQEID);
			return false;
		}
		newQE.setFromJDF(root); // repeat for the actual entry

		final String theDocFile = getJDFStorage(newQEID);
		final boolean ok = theJDF.write2File(theDocFile, 0, true);
		BambiNSExtension.setDocURL(newQE, theDocFile);
		BambiNSExtension.setDocModified(newQE, System.currentTimeMillis());
		if (!KElement.isWildCard(returnJMF))
		{
			BambiNSExtension.setReturnJMF(newQE, returnJMF);
		}
		else if (!KElement.isWildCard(returnURL))
		{
			BambiNSExtension.setReturnURL(newQE, returnURL);
		}

		return ok;
	}

	/**
	 * return the name of the file storage for a given queueentryid
	 * @param newQEID
	 * @return {@link String} the file name of the storage
	 */
	public String getJDFStorage(final String newQEID)
	{
		return _parentDevice.getJDFStorage(newQEID);
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
	 * make the memory queue persistent
	 * @param milliseconds length of time since last persist, if 0 force persist
	 * 
	 */
	public void persist(final long milliseconds)
	{
		final long t = System.currentTimeMillis();
		if (t >= milliseconds + lastPersist)
		{
			synchronized (_theQueue)
			{
				_queueFile.getNewFile();
				log.info("persisting queue to " + _queueFile.getPath());
				_theQueue.getOwnerDocument_KElement().write2File(_queueFile, 0, true);
			}
			lastPersist = t;
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
				if (ContainerUtil.equals(oldStatus, status))
				{
					// nop
				}
				else if (status.equals(EnumQueueEntryStatus.Removed))
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
				}
				else if (status.equals(EnumQueueEntryStatus.Waiting))
				{
					qe.removeAttribute(AttributeName.DEVICEID);
					qe.removeAttribute(AttributeName.STARTTIME);
					qe.removeAttribute(AttributeName.ENDTIME);
					BambiNSExtension.setDeviceURL(qe, null);
					qe.setQueueEntryStatus(status);
				}
				else
				{
					qe.setQueueEntryStatus(status);
				}

				if (!status.equals(oldStatus))
				{
					persist(0);
					notifyListeners(qe.getQueueEntryID());
				}
				else
				{
					persist(60000); // write queue just in case every 10 seconds
				}
			}
			else if (mess != null && !EnumFamily.Query.equals(mess.getFamily()))
			{
				persist(60000); // write queue just in case every 10 seconds
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
			final JDFQueue q = _theQueue.copyToResponse(resp, qf, getLastQueue(resp, qf));
			removeBambiNSExtensions(q);
			return q;
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
			final String docFile = getJDFStorage(qe.getQueueEntryID());
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
		docJDF.write2File((String) null, 0, true);
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
				response = JMFFactory.getJMFFactory().send2URLSynch(mp, returnJMF, _parentDevice.getCallback(null), mimeDetails, devID, 10000);
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
				bOK = true;
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
		final int queueSize = v == null ? 0 : v.size();
		for (int i = 0; i < queueSize; i++)
		{
			BambiNSExtension.removeBambiExtensions(v.elementAt(i));
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
		// nop
	}
}
