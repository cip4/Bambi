/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2012 The International Cooperation for the Integration of 
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
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.workers.JobPhase;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.XMLParser;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.FileUtil;

/**
 * class to load .job files for simulation
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * Sep 29, 2009
 */
class JobLoader
{
	/**
	 * 
	 */
	private final SimDeviceProcessor simDeviceProcessor;
	private JDFNode node;
	private final Log log;

	protected JobLoader(SimDeviceProcessor simDeviceProcessor)
	{
		super();
		this.simDeviceProcessor = simDeviceProcessor;
		node = null;
		log = LogFactory.getLog(getClass());
	}

	/**
	 * set the node for amount calculations 
	 * @param node
	 */
	public void setNode(JDFNode node)
	{
		this.node = node;
	}

	/**
	 * update amounts of all resources with no speed
	 * @param phase the jobphase to update the amounts for
	 * @param node the node to extract update data from. this implementation assumes a one to one match of input to output
	 */
	private void updateAmountsFromNode(JobPhase phase, JDFNode node)
	{
		if (node == null)
		{
			log.warn("updating amounts for null node");
		}
		else
		{
			VString resNames = phase.getPhaseAmountResourceNames();
			String master = phase.getMasterAmountResourceName();
			if (master != null)
			{
				resNames.remove(master);
				resNames.add(master); // move master to the very end so that we don't continue with a modified master amount
				for (String res : resNames)
				{
					double amountFactor = simDeviceProcessor.getAmountFactor(res, master, node);
					phase.scaleAmount(res, master, amountFactor);
				}
			}
		}
	}

	/**
	 * load Bambi job definition from file. <br>
	 * The list of job phases is emptied when an error occurs during parsing fileName
	 * 
	 * @param configdir the configuration directory
	 * @param fileName the file to load
	 * @return true, if successful
	 */
	private List<JobPhase> loadJobFromFile(final File configdir, final String fileName)
	{
		final File f = FileUtil.getFileInDirectory(configdir, new File(fileName));
		if (!f.canRead())
		{
			log.info("Could not find preset file :" + fileName + " in " + configdir);
			return null;
		}
		final XMLParser p = new XMLParser();
		final XMLDoc doc = p.parseFile(f);
		if (doc == null)
		{
			log.error("Job File " + fileName + " not found, using default");
			return null;
		}
		final List<JobPhase> phaseList = new Vector<JobPhase>();
		final KElement simJob = doc.getRoot();
		final VElement v = simJob.getXPathElementVector("JobPhase", -1);
		for (KElement phaseElement : v)
		{
			final JobPhase phase = new JobPhase(phaseElement);
			updateAmountsFromNode(phase, node);
			phaseList.add(phase);
		}

		if (phaseList.size() == 0)
		{
			log.warn("no job phases were added from " + fileName);
			return null;
		}
		else
		{
			simDeviceProcessor.idlePhase = null;
			final JobPhase tmpPhase = phaseList.get(phaseList.size() - 1);
			if (EnumDeviceStatus.Idle.equals(tmpPhase.getDeviceStatus()))
			{
				simDeviceProcessor.idlePhase = tmpPhase;
				phaseList.remove(simDeviceProcessor.idlePhase);
				log.info("defined an idle phase");
			}
		}
		log.info("created new job from " + fileName + " with " + phaseList.size() + " job phases.");
		randomizeJobPhases(phaseList, simJob.getRealAttribute("RandomFactor", null, 0.0));

		return phaseList;
	}

	/**
	 * load a job from the cached directory
	 * @return 
	 */
	protected List<JobPhase> loadJob()
	{
		final AbstractDevice parent = simDeviceProcessor.getParent();
		File cacheDir = parent.getCachedConfigDir();
		final String deviceFile = "job_" + parent.getDeviceID() + ".xml";

		log.info("loading job: " + deviceFile);
		List<JobPhase> jobPhases = loadJobFromFile(cacheDir, deviceFile);
		if (jobPhases == null)
		{
			log.info("loading default job.xml");
			jobPhases = loadJobFromFile(cacheDir, "job.xml");
		}
		return jobPhases;
	}

	/**
	 * randomize phase durations and add random error phases
	 * @param phases the phases to randomize
	 * @param randomTime the given time in seconds of each job phase is to vary by +/- 100 percent
	 */
	private void randomizeJobPhases(final List<JobPhase> phases, final double randomTime)
	{
		if (randomTime > 0.0 && phases != null)
		{
			for (JobPhase phase : phases)
			{
				double varyBy = Math.random() * randomTime / 100.0;
				if (Math.random() < 0.5)
				{
					varyBy *= -1.0;
				}
				phase.setDurationMillis(phase.getDurationMillis() + (long) (phase.getDurationMillis() * varyBy));
			}
		}
	}
}