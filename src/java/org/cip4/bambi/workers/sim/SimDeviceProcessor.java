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
package org.cip4.bambi.workers.sim;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.workers.sim.SimDeviceProcessor.JobPhase.PhaseAmount;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFResourceLink.EnumUsage;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * abstract parent class for device processors, with aditional functionality for JobPhases <br>
 * The device processor is the actual working part of a device. The individual job phases of the job are executed here.
 * 
 * @author boegerni
 * 
 */
public class SimDeviceProcessor extends AbstractDeviceProcessor
{

	private static Log log = LogFactory.getLog(SimDeviceProcessor.class.getName());
	protected List<JobPhase> _jobPhases = null;

	/**
	 * a single job phase
	 * 
	 * @author boegerni
	 * 
	 */
	public static class JobPhase implements Serializable, Cloneable
	{
		/**
		 * 
		 */
		public class PhaseAmount implements Serializable
		{
			private static final long serialVersionUID = -8504631585951268571L;

			/**
			 * waste to be produced in this job phase
			 */
			protected boolean bGood = true;
			/**
			 * current speed/hour in this phase
			 */
			protected double speed = 0;

			protected String resource = "Output";
			protected String resourceName = "Output";

			/**
			 * @param resName the name or named process usage of the resource
			 * @param _speed speed / hour
			 * @param condition good =true
			 */
			PhaseAmount(final String resName, final double _speed, final boolean condition)
			{
				resource = resourceName = resName;
				bGood = condition;
				speed = _speed;
			}

			/**
			 * @see java.lang.Object#toString()
			 * @return String the string
			 */
			@Override
			public String toString()
			{
				return "[ " + resourceName + " " + resource + (bGood ? " G: " : " W: ") + "Speed: " + speed + "]";
			}

			/**
			 * @param res
			 * @return true if this pjhaseAmount matches res
			 */
			public boolean matchesRes(final String res)
			{
				return resource.equals(res) || resourceName.equals(res);
			}

			@Override
			protected Object clone()
			{
				final PhaseAmount pa = new PhaseAmount(null, speed, bGood);
				pa.resource = resource;
				pa.resourceName = resourceName;
				return pa;
			}

		}

		// end of inner class PhaseAmount

		private static final long serialVersionUID = 2262422293566643131L;
		protected Vector<PhaseAmount> amounts = new Vector<PhaseAmount>();

		/**
		 * construction of a JobPhase
		 */
		public JobPhase()
		{
			super();
		}

		/**
		 * 
		 * @param phaseElement the xml element to parse
		 */
		public JobPhase(final KElement phaseElement)
		{
			super();
			deviceStatus = EnumDeviceStatus.getEnum(phaseElement.getXPathAttribute("@DeviceStatus", "Idle"));
			deviceStatusDetails = phaseElement.getXPathAttribute("@DeviceStatusDetails", "");
			nodeStatus = EnumNodeStatus.getEnum(phaseElement.getXPathAttribute("@NodeStatus", "Waiting"));
			nodeStatusDetails = phaseElement.getXPathAttribute("@NodeStatusDetails", "");
			timeToGo = 1000 * StringUtil.parseInt(phaseElement.getXPathAttribute("@Duration", "0"), 0);
			if (phaseElement.hasAttribute("Error"))
			{
				errorChance = StringUtil.parseDouble(phaseElement.getXPathAttribute("@Error", "0"), 0) * 0.001;
			}
			else
			{
				errorChance = StringUtil.parseDouble(phaseElement.getXPathAttribute("../@Error", "0"), 0) * 0.001;
			}
			final VElement vA = phaseElement.getChildElementVector("Amount", null);
			for (int j = 0; j < vA.size(); j++)
			{
				final KElement am = vA.elementAt(j);
				double speed = am.getRealAttribute("Speed", null, 0);
				if (speed < 0)
				{
					speed = 0;
				}
				final boolean bGood = !am.getBoolAttribute("Waste", null, false);
				// timeToGo is seconds, speed is / hour
				setAmount(am.getAttribute("Resource"), speed, bGood);
			}
		}

		/**
		 * status to be displayed for this job phase
		 */
		protected EnumDeviceStatus deviceStatus = EnumDeviceStatus.Idle;

		/**
		 * device status details
		 */
		protected String deviceStatusDetails = "";

		protected EnumNodeStatus nodeStatus = EnumNodeStatus.Waiting;
		protected String nodeStatusDetails = "";

		/**
		 * timeToGo of job phase in milliseconds
		 */
		protected int timeToGo = 0;
		protected long timeStarted = System.currentTimeMillis();
		protected double errorChance = 0.00;

		/**
		 * @see java.lang.Object#toString()
		 * @return String the string
		 */
		@Override
		public String toString()
		{
			String s = "[JobPhase: Duration=" + timeToGo + ", DeviceStatus=" + deviceStatus.getName() + ", DeviceStatusDetails=" + deviceStatusDetails + ", NodeStatus=" + nodeStatus.getName()
					+ ", NodeStatusDetails=" + nodeStatusDetails;
			for (int i = 0; i < amounts.size(); i++)
			{
				s += "\n" + amounts.elementAt(i);
			}
			return s;
		}

		/**
		 * @return EnumDeviceStatus the deviceStatus
		 */
		public EnumDeviceStatus getDeviceStatus()
		{
			return deviceStatus;
		}

		/**
		 * @param _deviceStatus the device statis to set
		 */
		public void setDeviceStatus(final EnumDeviceStatus _deviceStatus)
		{
			this.deviceStatus = _deviceStatus;
		}

		public String getDeviceStatusDetails()
		{
			return deviceStatusDetails;
		}

		public void setDeviceStatusDetails(final String _deviceStatusDetails)
		{
			this.deviceStatusDetails = _deviceStatusDetails;
		}

		public EnumNodeStatus getNodeStatus()
		{
			return nodeStatus;
		}

		/**
		 * @param _nodeStatus
		 */
		public void setNodeStatus(final EnumNodeStatus _nodeStatus)
		{
			this.nodeStatus = _nodeStatus;
		}

		public String getNodeStatusDetails()
		{
			return nodeStatusDetails;
		}

		public void setNodeStatusDetails(final String _nodeStatusDetails)
		{
			this.nodeStatusDetails = _nodeStatusDetails;
		}

		public int getTimeToGo()
		{
			return timeToGo;
		}

		public void setTimeToGo(final int duration)
		{
			this.timeToGo = duration;
		}

		public PhaseAmount setAmount(final String resName, final double speed, final boolean bGood)
		{
			PhaseAmount pa = getPhaseAmount(resName);
			if (pa == null)
			{
				pa = this.new PhaseAmount(resName, speed, bGood);
				amounts.add(pa);
			}
			else
			{
				pa.bGood = bGood;
				pa.speed = speed;
			}
			return pa;
		}

		public double getOutput_Speed(final String res)
		{
			final PhaseAmount pa = getPhaseAmount(res);
			return pa == null ? 0 : pa.speed;
		}

		public boolean getOutput_Condition(final String res)
		{
			final PhaseAmount pa = getPhaseAmount(res);
			return pa == null ? true : pa.bGood;
		}

		/**
		 * @param string
		 * @return
		 */
		PhaseAmount getPhaseAmount(final String res)
		{
			for (int i = 0; i < amounts.size(); i++)
			{
				if (amounts.elementAt(i).matchesRes(res))
				{
					return amounts.elementAt(i);
				}
			}
			return null;
		}

		/**
		 * @return the list of amount counting resources in this phase
		 */
		public VString getAmountResourceNames()
		{
			final VString v = new VString();
			for (int i = 0; i < amounts.size(); i++)
			{
				v.add(amounts.elementAt(i).resourceName);
			}
			return v;
		}

		@Override
		public Object clone()
		{
			final JobPhase jp = new JobPhase();
			jp.deviceStatus = deviceStatus;
			jp.deviceStatusDetails = deviceStatusDetails;
			jp.timeToGo = timeToGo;
			jp.nodeStatus = nodeStatus;
			jp.nodeStatusDetails = nodeStatusDetails;
			jp.errorChance = errorChance;
			jp.amounts = (Vector<PhaseAmount>) amounts.clone();
			return jp;
		}

		/**
		 * @param resource
		 * @param i
		 * @return
		 */
		public double getOutput_Waste(final String resource, final int i)
		{
			if (getOutput_Condition(resource))
			{
				return 0;
			}
			return getOutput(resource, i);
		}

		private double getOutput(final String resource, final int i)
		{
			if (i <= 0)
			{
				return 0; // negative time??? duh
			}
			final double spd = getOutput_Speed(resource);
			if (spd <= 0)
			{
				return 0;
			}
			return (spd * i) / (3600 * 1000);
		}

		/**
		 * @param resource
		 * @param i
		 * @return
		 */
		public double getOutput_Good(final String resource, final int i)
		{
			if (!getOutput_Condition(resource))
			{
				return 0;
			}
			return getOutput(resource, i);
		}

		/**
		 * update the abstract resourcelink names with real idref values from the link
		 * 
		 * @param rl
		 */
		public void updateAmountLinks(final JDFResourceLink rl)
		{
			if (rl == null || amounts == null)
			{
				return;
			}
			for (int i = 0; i < amounts.size(); i++)
			{
				final PhaseAmount pa = amounts.get(i);
				if (rl.matchesString(pa.resource))
				{
					pa.resource = rl.getrRef();
				}
			}
		}
	}

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
	 * 
	 * @param _queueProcessor
	 * @param _statusListener
	 */
	@Override
	public void init(final QueueProcessor queueProcessor, final StatusListener statusListener, final IDeviceProperties devProperties)
	{
		_jobPhases = new ArrayList<JobPhase>();
		super.init(queueProcessor, statusListener, devProperties);

	}

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
	public JobPhase getCurrentJobPhase()
	{
		// if(currentQE==null)
		// _jobPhases.clear(); // just in case we have some remaining spurious
		// phases

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

		EnumQueueEntryStatus qes = EnumNodeStatus.getQueueEntryStatus(lastPhase.nodeStatus);
		if (qes == null)
		{
			return EnumQueueEntryStatus.Aborted;
		}
		if (lastPhase.timeToGo <= 0 && EnumQueueEntryStatus.Running.equals(qes)) // final
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
		double all = rlAmount == null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, n == null ? null : nodeInfoPartMapVector);
		if (all < 0)
		{
			all = 0;
		}

		double todoAmount = rlAmount == null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.AMOUNT, n == null ? null : nodeInfoPartMapVector);
		log.debug("processing new job phase: " + phase.toString());
		_statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails, phase.nodeStatus, phase.nodeStatusDetails, false);
		long deltaT = 1000;
		while (phase.timeToGo > 0)
		{
			final long t0 = System.currentTimeMillis();
			final VString names = phase.getAmountResourceNames();
			boolean reachedEnd = false;
			for (int i = 0; i < names.size(); i++)
			{
				final PhaseAmount pa = phase.getPhaseAmount(names.get(i));
				if (pa != null)
				{
					final double phaseGood = phase.getOutput_Good(pa.resource, (int) deltaT);
					if ("percent".equalsIgnoreCase(pa.resource))
					{
						if (todoAmount <= 0)
						{
							todoAmount = 100; // percent, duh...
						}
						_statusListener.updatePercentComplete(phaseGood);
					}
					else
					{
						final double phaseWaste = phase.getOutput_Waste(pa.resource, (int) deltaT);
						_statusListener.updateAmount(pa.resource, phaseGood, phaseWaste);
					}
					if (namedRes != null && pa.matchesRes(namedRes))
					{
						all += phaseGood;
						if (all > todoAmount && todoAmount > 0)
						{
							phase.timeToGo = 0;
							log.debug("phase end for resource: " + namedRes);
							reachedEnd = true;
						}
					}
				}
			}
			if (_doShutdown)
			{
				phase.timeToGo = 0;
				reachedEnd = true;
				log.info("external shutdown: " + phase.toString());
			}
			_statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails, phase.nodeStatus, phase.nodeStatusDetails, reachedEnd);
			if (phase.timeToGo > 0 && !_doShutdown)
			{
				randomErrors(phase);
				ThreadUtil.sleep(123);
				final long t1 = System.currentTimeMillis();
				deltaT = t1 - t0;
				phase.timeToGo -= deltaT;
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
		final int siz = v == null ? 0 : v.size();
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
		return null;
	}

	/**
	 * generate random errors
	 * @param phase the phase element with the appropriate error chance
	 */
	protected void randomErrors(final JobPhase phase)
	{
		if (phase.errorChance <= 0)
		{
			return;
		}
		if (Math.random() > phase.errorChance)
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
		final File configDir = _parent.getProperties().getConfigDir();
		_jobPhases = loadJobFromFile(configDir, "job_" + _parent.getDeviceID() + ".xml");
		if (_jobPhases == null)
		{
			_jobPhases = loadJobFromFile(configDir, "job.xml");
		}
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
		final int vSiz = (vResLinks == null) ? 0 : vResLinks.size();
		for (int i = 0; i < vSiz; i++)
		{
			final JDFResourceLink rl = (JDFResourceLink) vResLinks.elementAt(i);
			for (int j = 0; j < jobPhaseSize; j++)
			{
				final JobPhase jp = _jobPhases.get(j);
				jp.updateAmountLinks(rl);
			}
		}
		return bOK;
	}

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
			log.info("No preset file :" + fileName);
			return null;
		}
		final JDFParser p = new JDFParser();
		final JDFDoc doc = p.parseFile(f);
		if (doc == null)
		{
			log.error("Job File " + fileName + " not found, using default");
			return null;
		}

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
			log.warn("no job phases were added from " + fileName);
			return null;
		}
		log.debug("created new job from " + fileName + " with " + phaseList.size() + " job phases.");
		randomizeJobPhases(phaseList, simJob.getRealAttribute("RandomFactor", null, 0.0));

		return phaseList;
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
				phase.timeToGo = phase.timeToGo + (int) (phase.timeToGo * varyBy);
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
				p.timeToGo = 0;
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
			lastPhase.timeToGo = 0;
			pos = 1;
		}
		_jobPhases.add(pos, nextPhase);
	}

	/**
	 * 
	 * @param node
	 * @return the initial joob phase
	 */
	protected JobPhase initFirstPhase(final JDFNode node)
	{
		log.debug("initializing first phase");
		final JobPhase firstPhase = new JobPhase();
		firstPhase.deviceStatus = EnumDeviceStatus.Setup;
		firstPhase.deviceStatusDetails = "Setup";
		firstPhase.nodeStatus = EnumNodeStatus.Setup;
		firstPhase.nodeStatusDetails = "Setup";
		firstPhase.timeToGo = Integer.MAX_VALUE / 2;
		if (node != null)
		{
			final VElement v = node.getResourceLinks(null);
			final int s = v == null ? 0 : v.size();
			for (int i = 0; i < s; i++)
			{
				final JDFResourceLink rl = (JDFResourceLink) v.get(i);
				final JDFResource linkRoot = rl.getLinkRoot();
				if (linkRoot != null && ((SimDevice) _parent).isAmountResource(rl))
				{
					final PhaseAmount pa = firstPhase.setAmount(rl.getNamedProcessUsage(), 0, false);
					pa.resource = linkRoot.getID();
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

}
