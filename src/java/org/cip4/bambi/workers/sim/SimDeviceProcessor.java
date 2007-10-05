/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IQueueProcessor;
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.workers.core.AbstractDeviceProcessor;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.util.StringUtil;

/**
 * a simulated device processor for Bambi
 * @author boegerni
 *
 */
public class SimDeviceProcessor extends AbstractDeviceProcessor
{
	private static Log log = LogFactory.getLog(SimDeviceProcessor.class.getName());	
	private List _originalPhases = null;
	private static final long serialVersionUID = -256551569245084031L;
	public boolean isPaused=false;
	JobPhase _currentPhase = null;

	/**
	 * initialize the SimDeviceProcessor and load the default sim phases from "job_$(DeviceID).xml"
	 */
	private void initSimDeviceProcessor(String deviceID) {     
        // try to load default a default job
        boolean hasLoaded = loadJobFromFile(_appDir+"/config/job_"+deviceID+".xml");
        if (hasLoaded)
        	randomizeJobPhases(10.0, 30.0);
        else
        	log.error("no default job defined for SimJobProcessor of "+deviceID);
	}
	
	/**
	 * load Bambi job definition from file. <br>
	 * The list of job phases is emptied when an error occurs during parsing fileName
	 * @param fileName file to load
	 * @return true, if successful
	 */
	private boolean loadJobFromFile(String fileName)
	{
		// if fileName has no file separator it is assumed to be on the server and 
		// needs the config dir to be added
		if ( !fileName.contains(File.separator) )  
			fileName = _appDir+fileName;
		_originalPhases = new ArrayList();
		_originalPhases.clear();
		JDFParser p = new JDFParser();
		JDFDoc doc = p.parseFile(fileName);
		if (doc == null)
		{
			log.error( fileName+" not found, list of job phases remains empty" );
			return false;
		}

		int counter = 0;
		try 
		{
			KElement simJob = doc.getRoot();
			VElement v = simJob.getXPathElementVector("//BambiJob/*", 99);
			for (int i = 0; i < v.size(); i++)
			{
				KElement job = (KElement)v.elementAt(i);
				JobPhase phase = new JobPhase();
				phase.deviceStatus = EnumDeviceStatus.getEnum(job.getXPathAttribute("@DeviceStatus", "Idle"));
				phase.deviceStatusDetails = job.getXPathAttribute("@DeviceStatusDetails", "");
				phase.nodeStatus = EnumNodeStatus.getEnum(job.getXPathAttribute("@NodeStatus", "Waiting"));
				phase.nodeStatusDetails = job.getXPathAttribute("@NodeStatusDetails", "");
				phase.duration = StringUtil.parseInt( job.getXPathAttribute("@Duration", "0"), 0 );
				phase.Output_Good = StringUtil.parseDouble( job.getXPathAttribute("@Good", "0"),0.0 );
				phase.Output_Waste = StringUtil.parseDouble( job.getXPathAttribute("@Waste", "0"),0.0 );

				_originalPhases.add(phase);
				counter++;
			}
			
			_jobPhases = new ArrayList();
			_jobPhases.addAll(_originalPhases);
		}
		catch (Exception ex)
		{
			log.warn("error in importing jobs");
			_originalPhases = new ArrayList();
			return false;
		}

		if (counter > 0)
		{
			log.debug("created new job from "+fileName+" with "+counter+" job phases.");
			return true;
		}
		else
		{
			log.warn("no job phases were added from "+fileName);
			return false;
		}
	}
	
	/**
	 * randomize phase durations and add random error phases 
	 * @param randomTime the given time of each job phase is to vary by ... percent
	 * @param errorPos random errors to create (as percentage of the total numbers of original job phases)
	 */
	public void randomizeJobPhases(double randomTime, double errorPoss)
	{
		if (randomTime > 0.0)
		{
			for (int i=0;i<_jobPhases.size();i++)
			{
				double varyBy = Math.random()*randomTime/100.0;
				if (Math.random() < 0.5)
					varyBy *= -1.0;
				JobPhase phase=(JobPhase)_originalPhases.get(i);
				phase.duration=phase.duration+(int)(phase.duration*varyBy);
			}
		}
		
		if (errorPoss > 0.0)
		{
			int numberOfErrors = (int)((errorPoss/100.0)*_originalPhases.size());
			JobPhase errorPhase = new JobPhase();
			errorPhase.deviceStatus = EnumDeviceStatus.Down;
			errorPhase.deviceStatusDetails = "Random Bambi Error Phase";
			errorPhase.nodeStatus = EnumNodeStatus.InProgress;
			errorPhase.nodeStatusDetails = "Random Bambi Error phase";
			
			for (int i=0;i<numberOfErrors;i++)
			{
				double rand = Math.random();
				int insertErrorAtPos = (int)(rand*_originalPhases.size());
				errorPhase.duration=(int)(5000*rand);
				if (Math.random()>0.5)
					errorPhase.Output_Waste=100.0*rand;
				_originalPhases.add(insertErrorAtPos, errorPhase);
			}
		}
	}

	public EnumQueueEntryStatus processDoc(JDFDoc doc, JDFQueueEntry qe) {
		super.processDoc(doc, qe);
		
		// try to load a list of remaining phases for qe
		_jobPhases = new ArrayList();
		List phases = resumeQueueEntry(qe);
		if (phases != null) {
			_jobPhases.addAll(phases);
		} else {
			_jobPhases.addAll(_originalPhases);
		}
        		
		if ( _jobPhases.isEmpty() )
		{
			log.warn("unable to start the job, no job loaded");
			return EnumQueueEntryStatus.Aborted;
		}
		for (int i=0;i<_jobPhases.size();i++)
		{
			_currentPhase = (JobPhase)_jobPhases.get(i);
			if (_currentPhase!=null && _currentPhase.duration>0) 
			{
				_statusListener.signalStatus(_currentPhase.deviceStatus, _currentPhase.deviceStatusDetails, 
						_currentPhase.nodeStatus,_currentPhase.nodeStatusDetails);
				try {
					int repeats = (int)(_currentPhase.duration/1000);
					int remainder = _currentPhase.duration % 1000;
					for (int j=0;j < repeats; j++)
					{
						int reqSize=_updateStatusReqs.size();
						if (reqSize>0) {
							for (int reqNo=0;reqNo<reqSize;reqNo++) {
								ChangeQueueEntryStatusRequest request=(ChangeQueueEntryStatusRequest) _updateStatusReqs.get(reqNo);
								if ( !request.queueEntryID.equals(qe.getQueueEntryID()) ) {	
									_updateStatusReqs.remove(reqNo);
									log.error("failed to change status of QueueEntry, it is not running");
								} else if (request.newStatus.equals(EnumQueueEntryStatus.Suspended)) {
									_updateStatusReqs.remove(reqNo);
									return suspendQueueEntry(qe,i+1, remainder);
								} else if (request.newStatus.equals(EnumQueueEntryStatus.Aborted)) {
									_updateStatusReqs.remove(reqNo);
									return abortQueueEntry();
								}
							}					
						} else {
							Thread.sleep(1000);
						}
					}
				} catch (InterruptedException e) {
					log.warn("interrupted while sleeping");
				}
			}
		}
		
		return finalizeProcessDoc();
	}


	public JobPhase getCurrentJobPhase() {
		return _currentPhase;
	}
	
	public SimDeviceProcessor(IQueueProcessor queueProcessor, 
			IStatusListener statusListener,	String deviceID, String appDir) {
		super(queueProcessor, statusListener, deviceID, appDir);
		initSimDeviceProcessor(deviceID);
	}
	
	public void init(IQueueProcessor queueProcessor, IStatusListener statusListener, 
			String deviceID, String appDir) {
		super.init(queueProcessor, statusListener, deviceID, appDir);
		initSimDeviceProcessor(deviceID);
	}
	
	public SimDeviceProcessor() {
		super();
	}
}