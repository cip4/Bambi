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
package org.cip4.bambi.workers.sim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.workers.JobPhase;
import org.cip4.bambi.workers.JobPhase.PhaseAmount;
import org.cip4.bambi.workers.UIModifiableDeviceProcessor;
import org.cip4.bambi.workers.WorkerDevice;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.JDFResourceLink.EnumUsage;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * abstract parent class for device processors, with additional functionality for JobPhases <br>
 * The device processor is the actual working part of a device. The individual job phases of the job are executed here.
 * 
 * @author boegerni
 * 
 */
public class SimDeviceProcessor extends UIModifiableDeviceProcessor
{
	protected List<JobPhase> _jobPhases = null;
	protected JobPhase idlePhase = null;

	/**
	 * constructor
	 * 
	 * @param queueProcessor points to the QueueProcessor
	 * @param statusListener points to the StatusListener
	 * @param devProperties device properties
	 */
	public SimDeviceProcessor(final QueueProcessor queueProcessor, final StatusListener statusListener, final IDeviceProperties devProperties)
	{
		super();
		init(queueProcessor, statusListener, devProperties);
	}

	/**
	 * constructor
	 */
	public SimDeviceProcessor()
	{
		super();
	}

	/**
	 * initialize the IDeviceProcessor
	 * @param queueProcessor
	 * @param statusListener
	 * @param devProperties
	 */
	@Override
	public void init(final QueueProcessor queueProcessor, final StatusListener statusListener, final IDeviceProperties devProperties)
	{
		_jobPhases = new ArrayList<JobPhase>();
		super.init(queueProcessor, statusListener, devProperties);

	}

	/**
	 * suspend this - also persist remaining tasks
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#suspend()
	 */
	@Override
	protected void suspend()
	{
		persistRemainingPhases();
		super.suspend();
	}

	/**
	 * check whether qe has been suspended before, and get its remaining job phases if there are any.
	 * 
	 * @param qe the QueueEntry to look for
	 * @return a {@link List} of {@link JobPhase}. Returns null if no remaining phases have been found.
	 */
	protected List<JobPhase> resumeQueueEntry(final JDFQueueEntry qe)
	{
		final List<JobPhase> phases = null;
		final String queueEntryID = qe.getQueueEntryID();
		return null;
		// TODO persist correctly
	}

	/**
	 * remember where we stopped, so we can resume later
	 */
	protected void persistRemainingPhases()
	{
		if (currentQE == null)
		{
			return;
		}
		return;
		// TODO persist correctly
	}

	/**
	 * get the current job phase, null if none is there
	 * 
	 * @return the current phase
	 */
	@Override
	public JobPhase getCurrentJobPhase()
	{
		if (_jobPhases != null && _jobPhases.size() > 0)
		{
			return _jobPhases.get(0);
		}
		return null;
	}

	/**
	 * process a queue entry
	 * 
	 * @param n the JDF node to process
	 * @param qe the JDF queueentry that corresponds to this
	 * @return EnumQueueEntryStatus the final status of the queuentry
	 */
	@Override
	public EnumQueueEntryStatus processDoc(final JDFNode n, final JDFQueueEntry qe)
	{
		log.debug("processing JDF: ");
		JobPhase lastPhase = null;
		while (_jobPhases.size() > 0)
		{
			processPhase(n);
			lastPhase = _jobPhases.remove(0); // phase(0) is always the active
			// phase
		}
		if (lastPhase == null)
		{
			return EnumQueueEntryStatus.Aborted;
		}

		EnumQueueEntryStatus qes = EnumNodeStatus.getQueueEntryStatus(lastPhase.getNodeStatus());
		if (qes == null)
		{
			return EnumQueueEntryStatus.Aborted;
		}
		if (lastPhase.getTimeToGo() <= 0 && EnumQueueEntryStatus.Running.equals(qes)) // final
		// phase was active
		{
			qes = EnumQueueEntryStatus.Completed;
		}

		return qes;
	}

	/**
	 * process one phase for a given JDF node
	 * @param n the currently processed node
	 */
	private void processPhase(final JDFNode n)
	{
		final JDFResourceLink rlAmount = getAmountLink(n);
		final String namedRes = rlAmount == null ? null : rlAmount.getrRef();
		final JobPhase phase = getCurrentJobPhase();

		VJDFAttributeMap nodeInfoPartMapVector = n.getNodeInfoPartMapVector();
		if (nodeInfoPartMapVector == null)
		{
			nodeInfoPartMapVector = new VJDFAttributeMap();
		}
		nodeInfoPartMapVector.put(AttributeName.CONDITION, "Good");
		double all = rlAmount == null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, nodeInfoPartMapVector);
		if (all < 0)
		{
			all = 0;
		}

		final Vector<JDFEmployee> employees = phase.getEmployees();
		if (employees != null)
		{
			_statusListener.setEmployees(employees);
		}
		double todoAmount = rlAmount == null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.AMOUNT, nodeInfoPartMapVector);
		log.debug("processing new job phase: " + phase.toString());
		_statusListener.signalStatus(phase.getDeviceStatus(), phase.getDeviceStatusDetails(), phase.getNodeStatus(), phase.getNodeStatusDetails(), false);
		long deltaT = 1000;
		while (phase.getTimeToGo() > 0)
		{
			final long t0 = System.currentTimeMillis();
			final VString names = phase.getAmountResourceNames();
			boolean reachedEnd = false;
			for (int i = 0; i < names.size(); i++)
			{
				final PhaseAmount pa = phase.getPhaseAmount(names.get(i));
				if (pa != null)
				{
					final double phaseGood = phase.getOutput_Good(pa.getResource(), (int) deltaT);
					if ("percent".equalsIgnoreCase(pa.getResource()))
					{
						if (todoAmount <= 0)
						{
							todoAmount = 100; // percent, duh...
						}
						_statusListener.updatePercentComplete(phaseGood);
					}
					else
					{
						final double phaseWaste = phase.getOutput_Waste(pa.getResource(), (int) deltaT);
						_statusListener.updateAmount(pa.getResource(), phaseGood, phaseWaste);
					}
					if (namedRes != null && pa.matchesRes(namedRes))
					{
						all += phaseGood;
						if (all > todoAmount && todoAmount > 0)
						{
							phase.setTimeToGo(0);
							log.debug("phase end for resource: " + namedRes);
							reachedEnd = true;
						}
					}
				}
			}
			if (_doShutdown)
			{
				phase.setTimeToGo(0);
				reachedEnd = true;
				log.info("external shutdown: " + phase.toString());
			}
			_statusListener.signalStatus(phase.getDeviceStatus(), phase.getDeviceStatusDetails(), phase.getNodeStatus(), phase.getNodeStatusDetails(), reachedEnd);
			if (phase.getTimeToGo() > 0 && !_doShutdown)
			{
				randomErrors(phase);
				ThreadUtil.sleep(123);
				final long t1 = System.currentTimeMillis();
				deltaT = t1 - t0;
				phase.setTimeToGo(phase.getTimeToGo() - deltaT);
			}
		}
	}

	/**
	 * @param n
	 * @return the "main" amount link
	 */
	private JDFResourceLink getAmountLink(final JDFNode n)
	{
		final VJDFAttributeMap vMap = n.getNodeInfoPartMapVector();

		final VElement v = n.getResourceLinks(new JDFAttributeMap(AttributeName.USAGE, EnumUsage.Output));
		if (v != null)
		{
			final int siz = v.size();
			for (int i = 0; i < siz; i++)
			{
				final JDFResourceLink rl = (JDFResourceLink) v.elementAt(i);
				try
				{
					final double d = rl.getAmountPoolSumDouble(AttributeName.AMOUNT, vMap);
					if (d >= 0)
					{
						return rl;
					}
				}
				catch (final JDFException e)
				{
					// nop
				}
			}
		}
		return null;
	}

	/**
	 * generate random errors
	 * @param phase the phase element with the appropriate error chance
	 */
	protected void randomErrors(final JobPhase phase)
	{
		if (phase == null || phase.getErrorChance() <= 0)
		{
			return;
		}
		if (Math.random() > phase.getErrorChance())
		{
			return;
		}
		final int iEvent = (int) (Math.random() * 100.);
		_statusListener.setEvent("" + iEvent, iEvent > 90 ? "Error" : "Event" + iEvent, StringUtil.getRandomString());
	}

	@Override
	protected boolean finalizeProcessDoc(final EnumQueueEntryStatus qes)
	{
		final boolean b = super.finalizeProcessDoc(qes);
		_jobPhases.clear();
		return b;
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#initializeProcessDoc(org.cip4.jdflib.node.JDFNode, org.cip4.jdflib.jmf.JDFQueueEntry)
	 * @param node
	 * @param qe
	 * @return true if successful
	 */
	@Override
	protected boolean initializeProcessDoc(final JDFNode node, final JDFQueueEntry qe)
	{
		new JobLoader().loadJob();
		// we want at least one setup dummy
		if (_jobPhases == null)
		{
			_jobPhases = new ArrayList<JobPhase>();
			_jobPhases.add(initFirstPhase(node));
		}
		final boolean bOK = super.initializeProcessDoc(node, qe);
		if (qe == null || node == null)
		{
			log.error("proccessing null job");
			return false;
		}
		qe.setDeviceID(_parent.getDeviceID());
		final String queueEntryID = qe.getQueueEntryID();
		log.info("Processing queueentry " + queueEntryID);
		final int jobPhaseSize = _jobPhases == null ? 0 : _jobPhases.size();

		final VElement vResLinks = node.getResourceLinks(null);
		if (vResLinks != null)
		{
			final int vSiz = vResLinks.size();
			for (int i = 0; i < vSiz; i++)
			{
				final JDFResourceLink rl = (JDFResourceLink) vResLinks.elementAt(i);
				for (int j = 0; j < jobPhaseSize; j++)
				{
					final JobPhase jp = _jobPhases.get(j);
					jp.updateAmountLinks(rl);
				}
			}
		}
		return bOK;
	}

	/**
	 * class to load .job files for simulation
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * Sep 29, 2009
	 */
	private class JobLoader
	{
		protected JobLoader()
		{
			super();
		}

		protected File srcFile = null;

		/**
		 * load Bambi job definition from file. <br>
		 * The list of job phases is emptied when an error occurs during parsing fileName
		 * @param configdir the configuration directory
		 * @param fileName the file to load
		 * @return true, if successful
		 */
		private List<JobPhase> loadJobFromFile(final File configdir, final String fileName)
		{
			final File f = FileUtil.getFileInDirectory(configdir, new File(fileName));
			if (!f.canRead())
			{
				getLog().info("No preset file :" + fileName);
				return null;
			}
			final JDFParser p = new JDFParser();
			final JDFDoc doc = p.parseFile(f);
			if (doc == null)
			{
				getLog().error("Job File " + fileName + " not found, using default");
				return null;
			}
			srcFile = f;
			final List<JobPhase> phaseList = new Vector<JobPhase>();
			final KElement simJob = doc.getRoot();
			final VElement v = simJob.getXPathElementVector("JobPhase", -1);
			for (int i = 0; i < v.size(); i++)
			{
				final KElement phaseElement = v.elementAt(i);
				final JobPhase phase = new JobPhase(phaseElement);
				phaseList.add(phase);
			}

			if (phaseList.size() == 0)
			{
				getLog().warn("no job phases were added from " + fileName);
				return null;
			}
			else
			{
				idlePhase = null;
				final JobPhase tmpPhase = phaseList.get(phaseList.size() - 1);
				if (EnumDeviceStatus.Idle.equals(tmpPhase.getDeviceStatus()))
				{
					idlePhase = tmpPhase;
					phaseList.remove(idlePhase);
					getLog().info("defined an idle phase");
				}
			}
			getLog().debug("created new job from " + fileName + " with " + phaseList.size() + " job phases.");
			randomizeJobPhases(phaseList, simJob.getRealAttribute("RandomFactor", null, 0.0));

			return phaseList;
		}

		/**
		 * load a job and ensure that a local copy of the sim file is created
		 */
		protected void loadJob()
		{
			final AbstractDevice parent = getParent();
			File deviceDir = parent.getCachedConfigDir();
			final String deviceFile = "job_" + parent.getDeviceID() + ".xml";
			_jobPhases = loadJobFromFile(deviceDir, deviceFile);
			final File configDir = parent.getProperties().getConfigDir();
			if (_jobPhases == null)
			{
				_jobPhases = loadJobFromFile(configDir, deviceFile);
			}
			if (_jobPhases == null)
			{
				_jobPhases = loadJobFromFile(deviceDir, "job.xml");
			}
			if (_jobPhases == null)
			{
				_jobPhases = loadJobFromFile(configDir, "job.xml");
			}
			if (srcFile != null)
			{
				final File destFile = FileUtil.getFileInDirectory(deviceDir, new File(deviceFile));
				FileUtil.copyFile(srcFile, destFile);
				getLog().info("copying sim file to local directory: " + destFile);
			}
		}

		/**
		 * randomize phase durations and add random error phases
		 * @param phases
		 * @param randomTime the given time of each job phase is to vary by ... percent
		 */
		private void randomizeJobPhases(final List<JobPhase> phases, final double randomTime)
		{
			if (randomTime > 0.0 && phases != null)
			{
				for (int i = 0; i < phases.size(); i++)
				{
					double varyBy = Math.random() * randomTime / 100.0;
					if (Math.random() < 0.5)
					{
						varyBy *= -1.0;
					}
					final JobPhase phase = phases.get(i);
					phase.setTimeToGo(phase.getTimeToGo() + (long) (phase.getTimeToGo() * varyBy));
				}
			}
		}
	}

	/**
	 * stop the processor with the new status (either completed or aborted)
	 * 
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#stopProcessing(org.cip4.jdflib.core.JDFElement.EnumNodeStatus)
	 * @param newStatus
	 */
	@Override
	public EnumNodeStatus stopProcessing(final EnumNodeStatus newStatus)
	{
		synchronized (_jobPhases)
		{
			JobPhase p = getCurrentJobPhase();
			if (p != null)
			{
				p.setTimeToGo(0);
			}
			_jobPhases.clear();
			p = new JobPhase();
			if (newStatus != null)
			{
				p.setNodeStatus(newStatus);
			}
			p.setDeviceStatus(EnumDeviceStatus.Idle);
			doNextPhase(p);
		}
		return newStatus;
	}

	/**
	 * proceed to the next job phase
	 * 
	 * @param nextPhase the next job phase to process.<br>
	 * Phase timeToGo is ignored in this class, it is advancing to the next phase solely by doNextPhase().
	 */
	public void doNextPhase(final JobPhase nextPhase)
	{
		final JobPhase lastPhase = getCurrentJobPhase();
		int pos = 0;
		if (lastPhase != null)
		{
			lastPhase.setTimeToGo(0);
			pos = 1;
		}
		_jobPhases.add(pos, nextPhase);
	}

	/**
	 * 
	 * @param node
	 * @return the initial job phase
	 */
	protected JobPhase initFirstPhase(final JDFNode node)
	{
		log.debug("initializing first phase");
		final JobPhase firstPhase = new JobPhase();
		firstPhase.setDeviceStatus(EnumDeviceStatus.Setup);
		firstPhase.setDeviceStatusDetails("Setup");
		firstPhase.setNodeStatus(EnumNodeStatus.Setup);
		firstPhase.setNodeStatusDetails("Setup");
		firstPhase.setTimeToGo(Integer.MAX_VALUE / 2);
		if (node != null)
		{
			final VElement v = node.getResourceLinks(null);
			if (v != null)
			{
				final int s = v.size();
				for (int i = 0; i < s; i++)
				{
					final JDFResourceLink rl = (JDFResourceLink) v.get(i);
					final JDFResource linkRoot = rl.getLinkRoot();
					if (linkRoot != null && ((WorkerDevice) _parent).isAmountResource(rl))
					{
						final PhaseAmount pa = firstPhase.setAmount(rl.getNamedProcessUsage(), 0, false);
						pa.setResource(linkRoot.getID());
					}
				}
			}
		}
		else
		{
			firstPhase.setAmount(_trackResource, 0, false);
		}
		return firstPhase;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#toString()
	 * @return the string
	 */
	@Override
	public String toString()
	{
		final StringBuffer b = new StringBuffer(1000);
		final int siz = _jobPhases == null ? 0 : _jobPhases.size();
		if (siz == 0)
		{
			b.append("no phases");
		}
		else
		{
			b.append(siz + " phases:\n");
			for (int i = 0; i < siz; i++)
			{
				b.append(_jobPhases.get(i).toString());
				b.append("\n");
			}
		}
		return "Abstract Worker Device Processor: " + super.toString() + "\nPhases: " + b.toString() + "]";
	}

	/**
	 * same as super but also randomize some errors
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#idleProcess()
	 */
	@Override
	protected void idleProcess()
	{
		super.idleProcess();
		randomErrors(idlePhase);
	}

}
