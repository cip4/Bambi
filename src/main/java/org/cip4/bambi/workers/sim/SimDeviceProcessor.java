/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2021 The International Cooperation for the Integration of
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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
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
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * abstract parent class for device processors, with additional functionality for JobPhases <br>
 * The device processor is the actual working part of a device. The individual job phases of the job are executed here.
 *
 * @author boegerni
 */
public class SimDeviceProcessor extends UIModifiableDeviceProcessor
{
	private final static Log log = LogFactory.getLog(SimDeviceProcessor.class);

	final protected Vector<JobPhase> _jobPhases;
	protected JobPhase idlePhase;
	boolean bActive;

	/**
	 * constructor
	 *
	 * @param queueProcessor points to the QueueProcessor
	 * @param statusListener points to the StatusListener
	 * @param devProperties  device properties
	 */
	public SimDeviceProcessor(final QueueProcessor queueProcessor, final StatusListener statusListener, final IDeviceProperties devProperties)
	{
		this();
		bActive = true;
		init(queueProcessor, statusListener, devProperties);
	}

	/**
	 * constructor
	 */
	public SimDeviceProcessor()
	{
		super();
		_jobPhases = new Vector<>();
		idlePhase = null;
	}

	/**
	 * remember where we stopped, so we can resume later
	 */
	protected void persistRemainingPhases()
	{
		if (getCurrentQE() == null)
		{
			return;
		}
		log.error("persistRemainingPhases not yet implemented");
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
		return ContainerUtil.get(_jobPhases, 0);
	}

	/**
	 * process a queue entry
	 *
	 * @param n  the JDF node to process
	 * @param qe the JDF queueentry that corresponds to this
	 * @return EnumQueueEntryStatus the final status of the queuentry
	 */
	@Override
	public EnumQueueEntryStatus processDoc(final JDFNode n, final JDFQueueEntry qe)
	{
		final String qeid = getQueueEntryID();
		if (!bActive)
		{
			log.info("removing inactive JDF: " + qeid);
			return EnumQueueEntryStatus.Removed;
		}
		log.info("processing JDF: " + getJobID() + " qeID=" + qeid);
		JobPhase lastPhase = null;
		while (!_jobPhases.isEmpty())
		{
			processPhase(n);
			lastPhase = _jobPhases.remove(0); // phase(0) is always the active phase
		}
		EnumQueueEntryStatus qes = lastPhase == null ? null : EnumNodeStatus.getQueueEntryStatus(lastPhase.getNodeStatus());
		if (qes == null)
		{
			log.warn("Aborted Job " + getJobID() + " geID= " + qeid);
			return EnumQueueEntryStatus.Aborted;
		}
		if (lastPhase.getDurationMillis() <= 0 && EnumQueueEntryStatus.Running.equals(qes)) // final phase was active
		{
			qes = EnumQueueEntryStatus.Completed;
			log.info("Completed Job " + getJobID() + " geID= " + qeid);
		}

		return qes;
	}

	/**
	 * process one phase for a given JDF node
	 *
	 * @param node the currently processed node
	 */
	protected void processPhase(final JDFNode node)
	{
		final JDFResourceLink rlAmount = getAmountLink(node);
		final JobPhase phase = getCurrentJobPhase();

		VJDFAttributeMap nodeInfoPartMapVector = node.getNodeInfoPartMapVector();
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
		final double todoAmount = rlAmount == null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.AMOUNT, nodeInfoPartMapVector);
		log.info("processing new job phase: " + getJobID() + " / " + getQueueEntryID() + phase.shortString());
		_statusListener.setAmountResource(phase.getMasterAmountResourceName());
		_statusListener.signalStatus(phase.getDeviceStatus(), phase.getDeviceStatusDetails(), phase.getNodeStatus(), phase.getNodeStatusDetails(), false);
		loopPhase(node, phase, all, todoAmount);
	}

	void loopPhase(JDFNode node, final JobPhase phase, double all, double todoAmount)
	{
		long deltaT = 1000;
		final JDFResourceLink rlAmount = getAmountLink(node);

		while (phase.getDurationMillis() > 0)
		{
			final long t0 = System.currentTimeMillis();
			final VString resNames = phase.getPhaseAmountResourceNames();
			boolean reachedEnd = EnumNodeStatus.isCompleted(phase.getNodeStatus()) || EnumNodeStatus.Suspended.equals(phase.getNodeStatus());
			for (final String resName : resNames)
			{
				final PhaseAmount phaseAmount = phase.findPhaseAmount(resName);
				if (phaseAmount != null)
				{
					final JDFResourceLink otherlink = getAmountLink(node, resName);
					VJDFAttributeMap resourceParts = new VJDFAttributeMap();
					if (otherlink != null && phaseAmount.isSplitPart())
					{
						resourceParts = otherlink.getResourcePartMapVector();
						resourceParts.overlapMap(otherlink.getMatchingParts(resName));
						final VJDFAttributeMap nodeInfoPartMapVector = node.getNodeInfoPartMapVector();
						resourceParts.overlapMap(nodeInfoPartMapVector);
					}
					if (resourceParts.isEmpty())
					{
						resourceParts.add(new JDFAttributeMap());
					}
					for (final JDFAttributeMap localPart : resourceParts)
					{
						final double phaseGood = phase.getOutputGood(phaseAmount.getResourceName(), (int) deltaT, localPart);
						if ("percent".equalsIgnoreCase(phaseAmount.getResourceName()))
						{
							if (phaseAmount.isMasterAmount())
							{
								if (todoAmount <= 0)
								{
									todoAmount = 100; // percent, duh...
								}
								_statusListener.updatePercentComplete(phaseGood);
							}
						}
						else
						{
							final double phaseWaste = phase.getOutputWasteAfterTime(phaseAmount.getResourceName(), (int) deltaT, localPart);
							_statusListener.updateAmount(phaseAmount.getResourceName(), phaseGood, phaseWaste, localPart);
						}
						if (phaseAmount.isMasterAmount())
						{
							all += phaseGood;
							if (all > todoAmount && todoAmount > 0)
							{
								phase.setDurationMillis(0);
								log.info("phase " + getJobID() + " / " + getQueueEntryID() + " end for resource: " + rlAmount.shortString() + " done=" + all
										+ " planned=" + todoAmount);
								reachedEnd = true;
							}
						}
					}
				}
			}

			if (_doShutdown)
			{
				reachedEnd = true;
				log.info("external shutdown: " + phase.toString());
			}
			if (reachedEnd)
			{
				phase.setDurationMillis(0);
			}
			_statusListener.signalStatus(phase.getDeviceStatus(), phase.getDeviceStatusDetails(), phase.getNodeStatus(), phase.getNodeStatusDetails(),
					reachedEnd);
			if (phase.getDurationMillis() > 0 && !_doShutdown)
			{
				randomErrors(phase);
				if (!ThreadUtil.sleep(123))
				{
					shutdown();
					break; // we hit a hard interrupt
				}
				final long t1 = System.currentTimeMillis();
				deltaT = t1 - t0;
				phase.setDurationMillis(phase.getDurationMillis() - deltaT);
			}
		}
	}

	/**
	 * @param n
	 * @return the "main" amount link
	 */
	protected JDFResourceLink getAmountLink(final JDFNode n)
	{
		final VElement v = n.getResourceLinks(new JDFAttributeMap(AttributeName.USAGE, EnumUsage.Output));
		if (v != null)
		{
			final JobPhase currentJobPhase = getCurrentJobPhase();
			final String name = currentJobPhase.getMasterAmountResourceName();
			for (final KElement e : v)
			{
				final JDFResourceLink rl = (JDFResourceLink) e;
				if (rl.matchesString(name))
				{
					return rl;
				}
			}
		}
		return null;
	}

	/**
	 * @param n
	 * @return all amount link s mentioned in the simulation
	 */
	protected List<JDFResourceLink> getAmountLinks(final JDFNode n)
	{
		final List<JDFResourceLink> ret = new ArrayList<>();
		final VElement v = n.getResourceLinks(null);
		if (v != null)
		{
			for (final JobPhase jobphase : _jobPhases)
			{
				for (final String name : jobphase.getPhaseAmountResourceNames())
				{
					for (final KElement e : v)
					{
						final JDFResourceLink rl = (JDFResourceLink) e;
						if (rl.matchesString(name))
						{
							ret.add(rl);
						}
					}
				}
			}
		}
		ContainerUtil.unify(ret);
		return ret;
	}

	/**
	 * @param n
	 * @return all amount link s mentioned in the simulation
	 */
	protected JDFResourceLink getAmountLink(final JDFNode n, String name)
	{
		final VElement v = n.getResourceLinks(null);
		if (v != null)
		{
			for (final KElement e : v)
			{
				final JDFResourceLink rl = (JDFResourceLink) e;
				if (rl.matchesString(name))
				{
					return rl;
				}
			}
		}

		return null;
	}

	/**
	 * generate random errors
	 *
	 * @param phase the phase element with the appropriate error chance
	 */
	protected void randomErrors(final JobPhase phase)
	{
		if (phase == null || phase.getErrorChance() <= 0 || Math.random() > phase.getErrorChance())
		{
			return;
		}
		final int iEvent = (int) (Math.random() * 100.);
		log.info("random event: " + getJobID() + " / " + getQueueEntryID());
		_statusListener.setEvent("" + iEvent, iEvent > 90 ? "Error" : "Event" + iEvent, StringUtil.getRandomString());
	}

	@Override
	protected boolean finalizeProcessDoc(final EnumQueueEntryStatus qes)
	{
		final boolean b = super.finalizeProcessDoc(qes);
		_jobPhases.clear();
		return !bActive || b;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#initializeProcessDoc(org.cip4.jdflib.node.JDFNode, org.cip4.jdflib.jmf.JDFQueueEntry)
	 * @param node
	 * @param qe
	 * @return true if successful
	 */
	@Override
	protected boolean initializeProcessDoc(final JDFNode node, final JDFQueueEntry qe)
	{
		bActive = getParent().isActive(node, qe);
		if (!bActive)
		{
			return super.initializeProcessDoc(node, qe);
		}

		loadJob(node);
		final JDFResourceLink rl = getAmountLink(node);
		if (rl != null)
		{
			_trackResource = rl.getrRef();
		}
		final boolean bOK = super.initializeProcessDoc(node, qe);
		if (qe == null || node == null)
		{
			log.error("proccessing null job");
			return false;
		}
		qe.setDeviceID(_parent.getDeviceID());
		final String queueEntryID = qe.getQueueEntryID();
		log.info("Processing queue entry: " + queueEntryID);
		prepareAmounts(node);
		return bOK;
	}

	/**
	 * @param node
	 */
	protected void loadJob(final JDFNode node)
	{
		final JobLoader jobLoader = new JobLoader(this);
		jobLoader.setNode(node);
		final List<JobPhase> jobPhases = jobLoader.loadJob();
		// we want at least one setup dummy
		if (jobPhases == null)
		{
			_jobPhases.add(initFirstPhase(node));
		}
		else
		{
			_jobPhases.clear();
			_jobPhases.addAll(jobPhases);
		}
		log.info("loaded job " + shortString());
	}

	/**
	 * @param node
	 */
	protected void prepareAmounts(final JDFNode node)
	{
		// nop
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
		log.info("Stop processing " + getJobID() + " / " + getQueueEntryID());
		synchronized (_jobPhases)
		{
			JobPhase p = getCurrentJobPhase();
			if (p != null)
			{
				p.setDurationMillis(0);
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
	 * get the amount factor for this node, e.g. by evaluating a layout or runlist defaults to 1 but may be overwritten for specific processors
	 *
	 * @param res    the resource to calculate the amount for
	 * @param master the master resource that defines the amount=1
	 * @param node   the jdf node with details to evaluate
	 * @return
	 */
	protected double getAmountFactor(final String res, final String master, final JDFNode node)
	{
		return 1.0;
	}

	/**
	 * proceed to the next job phase
	 *
	 * @param nextPhase the next job phase to process.<br>
	 *                  Phase timeToGo is ignored in this class, it is advancing to the next phase solely by doNextPhase().
	 */
	public void doNextPhase(final JobPhase nextPhase)
	{
		final JobPhase lastPhase = getCurrentJobPhase();
		int pos = 0;
		if (lastPhase != null)
		{
			lastPhase.setDurationMillis(0);
			pos = 1;
		}
		_jobPhases.add(pos, nextPhase);
	}

	/**
	 * @param node
	 * @return the initial job phase
	 */
	protected JobPhase initFirstPhase(final JDFNode node)
	{
		log.info("initializing first phase " + getJobID() + " / " + getQueueEntryID());
		final JobPhase firstPhase = new JobPhase();
		firstPhase.setDeviceStatus(EnumDeviceStatus.Setup);
		firstPhase.setDeviceStatusDetails("Setup");
		firstPhase.setNodeStatus(EnumNodeStatus.Setup);
		firstPhase.setNodeStatusDetails("Setup");
		firstPhase.setDurationMillis(Integer.MAX_VALUE / 2);
		if (node != null)
		{
			final VElement v = node.getResourceLinks(null);
			if (v != null)
			{
				for (final KElement e : v)
				{
					final JDFResourceLink rl = (JDFResourceLink) e;
					final JDFResource linkRoot = rl.getLinkRoot();
					if (linkRoot != null && ((WorkerDevice) _parent).isAmountResource(rl))
					{
						final PhaseAmount pa = firstPhase.addPhaseAmount(rl.getNamedProcessUsage(), 0, false);
						pa.setResourceName(linkRoot.getID());
					}
				}
			}
		}
		else
		{
			firstPhase.addPhaseAmount(_trackResource, 0, false);
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
		final StringBuilder b = new StringBuilder(1000);
		final int siz = _jobPhases.size();
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
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#toString()
	 * @return the string
	 */
	@Override
	public String shortString()
	{
		final JobPhase currentJobPhase = getCurrentJobPhase();
		String ret = super.shortString() + " " + _jobPhases.size();
		if (currentJobPhase != null)
		{
			ret += " " + currentJobPhase.shortString();
		}
		return ret;
	}

	/**
	 * same as super but also randomize some errors
	 *
	 * @see org.cip4.bambi.core.AbstractDeviceProcessor#idleProcess()
	 */
	@Override
	protected void idleProcess()
	{
		super.idleProcess();
		randomErrors(idlePhase);
	}
}
