/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of 
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

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.messaging.MessageResponseHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoQueueFilter.EnumQueueEntryDetails;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumActivation;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * 
 * @author rainer prosi
 */
public class ProxyDispatcherProcessor extends AbstractProxyProcessor
{
	private final static Log log = LogFactory.getLog(ProxyDispatcherProcessor.class);

	private final OrphanCleaner cleaner;

	/**
	 * @param parent the owner device
	 */
	public ProxyDispatcherProcessor(final AbstractProxyDevice parent)
	{
		super(parent);
		cleaner = new OrphanCleaner();
		lastbad = -1;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#processDoc(org.cip4.jdflib.node.JDFNode, org.cip4.jdflib.jmf.JDFQueueEntry)
	 * @param nod
	 * @param qe
	 * @return always Waiting
	 */
	@Override
	public EnumQueueEntryStatus processDoc(final JDFNode nod, final JDFQueueEntry qe)
	{
		// nop - the submission processor does the real work
		return EnumQueueEntryStatus.Waiting;

	}

	int lastbad;

	/**
	 *
	 * @return
	 */
	@Override
	protected IQueueEntry fillCurrentQE()
	{
		final boolean canProcess = canProcess();
		if (canProcess)
		{
			lastbad = -1;
			return super.fillCurrentQE();
		}
		else
		{
			if ((++lastbad % 1000) == 0)
			{
				log.warn("Not filling QueueEntry for unavailable Slave #" + lastbad);
			}

		}
		return null;
	}

	protected boolean canProcess()
	{
		final IProxyProperties properties = getParent().getProperties();
		final String slaveURL = getParent().getSlaveURL();
		return (_parent.activeProcessors() < (1 + properties.getMaxPush())) && isQueueAvailable(slaveURL);
	}

	/**
	 * do whatever needs to be done on idle by default, just tell the StatusListner that we are bored
	 */
	@Override
	protected void idleProcess()
	{
		// nop
	}

	/**
	 * 
	 * 
	 * @author rainer prosi
	 * @date Jul 13, 2012
	 */
	public class QueueStatusResponseHandler extends MessageResponseHandler
	{
		/**
		 * @param jmf
		 */
		public QueueStatusResponseHandler(final JDFJMF jmf)
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
			getLog().debug("Finalized handling of QueueStatus Query");
		}

		/**
		 * @return vector of all jmfs that still need to be sent
		 * 
		 */
		public boolean isOpen()
		{
			final JDFMessage m = getFinalMessage();
			if (m == null)
			{
				log.warn("No QueueStatus response to handle at " + getParent().getSlaveURL());
				return false;
			}
			final JDFQueue q = m.getQueue(0);
			if (q == null)
			{
				log.warn("No Queue in QueueStatus response to handle at " + getParent().getSlaveURL());
				return false;
			}
			final boolean canAccept = q.canAccept();
			if (!canAccept)
			{
				log.warn("queue Status=" + q.getQueueStatus() + " at " + getParent().getSlaveURL());
			}
			return canAccept;
		}
	}

	private class OrphanCleaner
	{
		private AbstractDeviceProcessor waitProc;
		private long lastClean;

		/**
		 * 
		 */
		public OrphanCleaner()
		{
			super();
			waitProc = null;
			lastClean = 0;
		}

		/**
		 * 
		 */
		protected void cleanOrphans()
		{
			final long t = System.currentTimeMillis();
			if (t - lastClean < 30000)
				return;
			lastClean = t;
			/**
			 * clean up orphaned or duplicate processors
			 */
			final HashSet<String> setQE = new HashSet<String>();
			for (int i = 0; true; i++)
			{
				final AbstractDeviceProcessor proc = getParent().getProcessor(null, i);
				if (proc == null)
					break;
				if (!proc.isActive())
				{
					proc.shutdown();
				}
				else if (!proc.isIdle())
				{
					final IQueueEntry iqe = proc.getCurrentQE();
					if (iqe != null)
					{
						final String qe = iqe.getQueueEntryID();
						if (setQE.contains(qe)) // remove duplicates
						{
							getLog().warn("removing duplicate processor ");
							proc.shutdown();
						}
						else
						{
							setQE.add(qe);
						}
						final JDFQueueEntry qentry = iqe.getQueueEntry();
						if (qentry == null)
						{
							proc.shutdown();
						}
						else
						{
							final EnumQueueEntryStatus qes = qentry.getQueueEntryStatus();
							if (EnumQueueEntryStatus.Aborted.equals(qes) || EnumQueueEntryStatus.Completed.equals(qes))
							{
								if (waitProc == null || waitProc != proc)
								{
									waitProc = proc;
									break; // no flip-flop wanted - always zapp first
								}
								else if (waitProc == proc && (proc instanceof ProxyDeviceProcessor))
								{
									((ProxyDeviceProcessor) proc).finalizeProcessDoc(null);
									waitProc = null;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @param root the Kelement root this is not really a processor to display - ignore call
	 */
	@Override
	public void addToDisplayXML(final KElement root)
	{
		return;
	}

	/**
	 * 
	 * 
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#stopProcessing(org.cip4.jdflib.core.JDFElement.EnumNodeStatus)
	 */
	@Override
	public EnumNodeStatus stopProcessing(final EnumNodeStatus newStatus)
	{
		log.info("stopProcessing not implemented for dispatcher processor");
		return null;
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#finalizeProcessDoc(org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus)
	 */
	@Override
	protected boolean finalizeProcessDoc(final EnumQueueEntryStatus qes)
	{
		final int maxPush = getParent().getProperties().getMaxPush();
		// if we can't push, there is no need to constantly check the queue
		if (_parent.activeProcessors() > 1 + maxPush)
		{
			cleaner.cleanOrphans();
		}
		return _parent.activeProcessors() < 1 + maxPush;
	}

	@Override
	protected boolean initializeProcessDoc(final JDFNode node, final JDFQueueEntry qe)
	{
		setCurrentQE(null);
		final IProxyProperties properties = getParent().getProperties();
		final String slaveURL = getParent().getSlaveURL();
		if (!canProcess())
		{
			BambiNSExtension.setDeviceURL(qe, null);
			cleaner.cleanOrphans();
			log.warn("Not processing QueueEntry for unavailable Slave " + (qe == null ? null : qe.getQueueEntryID()));
			return false; // no more push
		}
		qe.setDeviceID(properties.getSlaveDeviceID());
		final IQueueEntry iqe = new QueueEntry(node, qe);
		final ProxyDeviceProcessor pdb = ((ProxyDevice) _parent).submitQueueEntry(iqe, slaveURL, EnumActivation.Active);
		if (pdb == null)
		{
			BambiNSExtension.setDeviceURL(qe, null); // see above clean up any multiple markers
		}
		return pdb != null;
	}

	/**
	 * ensure that the queue is alive and accepting entries prior to submitting
	 * 
	 * @param slaveURL
	 * @return
	 */
	protected boolean isQueueAvailable(final String slaveURL)
	{
		if (StringUtil.isEmpty(slaveURL))
		{
			return false;
		}

		final MessageChecker knownSlaveMessages = getParent().knownSlaveMessages;
		knownSlaveMessages.updateKnownMessages();
		boolean ret = false;
		if (!knownSlaveMessages.knows(EnumType.KnownMessages))
		{
			log.warn("no access to slave url at " + getParent().getSlaveURL());
		}
		else if (knownSlaveMessages.knows(EnumType.QueueStatus))
		{
			ret = checkSlaveQueueStatus();
		}
		else
		{
			log.warn("bypassing queue status check for " + getParent().getSlaveURL());
			ret = true;
		}
		return ret;
	}

	protected boolean checkSlaveQueueStatus()
	{
		final JDFJMF queueStatusQuery = getParent().getBuilderForSlave().buildQueueStatus();
		final JDFQueueFilter qf = queueStatusQuery.getQuery(0).getCreateQueueFilter(0);
		qf.setMaxEntries(0);
		qf.setQueueEntryDetails(EnumQueueEntryDetails.None);
		final QueueStatusResponseHandler qrh = new QueueStatusResponseHandler(queueStatusQuery);
		final boolean sent = getParent().sendJMFToSlave(queueStatusQuery, qrh);
		if (sent)
		{
			qrh.waitHandled(5000, 20000, true);
			return qrh.isOpen();
		}
		else
		{
			return true;
		}
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#isActive()
	 * @return
	 */
	@Override
	public boolean isActive()
	{
		// dispatchers are always active
		return true;
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#isIdle()
	 */
	@Override
	public boolean isIdle()
	{
		// dispatchers are always idle
		return true;
	}

	/**
	 * @see org.cip4.bambi.proxy.AbstractProxyProcessor#returnFromSlave(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse, org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	protected boolean returnFromSlave(final JDFMessage m, final JDFResponse resp, final JDFDoc doc)
	{
		final boolean b = super.returnFromSlave(m, resp, doc);

		if (b)
		{
			final JDFReturnQueueEntryParams retQEParams = m == null ? null : m.getReturnQueueEntryParams(0);
			final String queueEntryID = retQEParams == null ? null : retQEParams.getQueueEntryID();
			JDFQueueEntry qeBambi = queueEntryID == null ? null : getParent().getQueueProcessor().getQueueEntry(queueEntryID, null);
			if (queueEntryID == null)
			{
				log.error("Skipping return of null queue entry ID=");
			}
			else
			{
				int nLoop = 0;
				while (qeBambi == null)
				{
					if (nLoop == 0)
					{
						log.info("waiting for return of unknown queue entry ID=" + queueEntryID);
					}
					else if (nLoop == 5)
					{
						log.error("Skipping return of unknown queue entry ID=" + queueEntryID + " after waiting about 1/2 a minute");
						break;
					}
					ThreadUtil.sleep(100 + nLoop * 1000);
					qeBambi = getParent().getQueueProcessor().getQueueEntry(queueEntryID, null);
					nLoop++;
				}
			}
			if (qeBambi != null)
			{
				BambiNSExtension.setDeviceURL(qeBambi, null);
				// remove slave qeid from map
				_queueProcessor.updateCache(qeBambi, null);
				EnumQueueEntryStatus finalStatus = calculateFinalStatus(doc, retQEParams);
				finalStatus = fixFinalStatus(queueEntryID, finalStatus);

				log.info("received returned entry " + queueEntryID + " final status=" + finalStatus.getName());
				_queueProcessor.returnQueueEntry(qeBambi, null, null, finalStatus);
				qeBambi.removeAttribute(AttributeName.DEVICEID);
				_queueProcessor.updateEntry(qeBambi, finalStatus, null, null, null);
			}
		}
		return b;
	}

	/**
	 * 
	 * 
	 * @param doc
	 * @param retQEParams
	 * @return
	 */
	protected EnumQueueEntryStatus calculateFinalStatus(final JDFDoc doc, final JDFReturnQueueEntryParams retQEParams)
	{
		final VString aborted = retQEParams.getAborted();
		final VString completed = retQEParams.getCompleted();
		EnumQueueEntryStatus finalStatus;
		if (aborted != null && aborted.size() != 0)
		{
			finalStatus = EnumQueueEntryStatus.Aborted;
		}
		else if (completed != null && completed.size() != 0)
		{
			finalStatus = EnumQueueEntryStatus.Completed;
		}
		else
		{
			final JDFNode root = doc == null ? null : doc.getJDFRoot();
			finalStatus = root == null ? EnumQueueEntryStatus.Aborted : EnumNodeStatus.getQueueEntryStatus(root.getPartStatus(null, -1));
			if (finalStatus == null)
			{
				finalStatus = EnumQueueEntryStatus.Aborted;
			}
		}
		return finalStatus;
	}

	/**
	 * 
	 * repair calculated status
	 * 
	 * @param queueEntryID
	 * @param finalStatus
	 * @return
	 */
	protected EnumQueueEntryStatus fixFinalStatus(final String queueEntryID, EnumQueueEntryStatus finalStatus)
	{
		if (EnumQueueEntryStatus.Suspended.equals(finalStatus))
		{
			if (getParent().getProperties().getMaxPush() > 0)
			{
				finalStatus = EnumQueueEntryStatus.Completed;
				log.warn("Moving suspended to completed " + queueEntryID);
			}
		}
		else if (!EnumQueueEntryStatus.Completed.equals(finalStatus) && !EnumQueueEntryStatus.Aborted.equals(finalStatus))
		{
			log.warn("Moving " + finalStatus.getName() + " to Aborted; qe=" + queueEntryID);
			finalStatus = EnumQueueEntryStatus.Aborted;
		}
		return finalStatus;
	}
}