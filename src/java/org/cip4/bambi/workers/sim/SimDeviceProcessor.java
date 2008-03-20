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
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.workers.core.AbstractWorkerDeviceProcessor;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.StringUtil;

/**
 * A simulated device processor for Bambi. It processes a fixed List of JobPhases.
 * @author boegerni
 *
 */
public class SimDeviceProcessor extends AbstractWorkerDeviceProcessor
{
	private static Log log = LogFactory.getLog(SimDeviceProcessor.class.getName());	
	private static final long serialVersionUID = -256551569245084031L;
	
	/**
	 * load Bambi job definition from file. <br>
	 * The list of job phases is emptied when an error occurs during parsing fileName
	 * @param fileName file to load
	 * @return true, if successful
	 */
	private List<JobPhase> loadJobFromFile(String fileName)
	{
	    File f=new File(fileName);
	    if(!f.canRead())
	    {
	        log.info("No preset file :"+fileName);
	        return null;
	    }
	    JDFParser p = new JDFParser();
	    JDFDoc doc = p.parseFile(fileName);
	    if (doc == null) {
	        log.error( "Job File "+fileName+" not found, using default" );
			return null;
		}

		List<JobPhase> phaseList = new Vector<JobPhase>();
		KElement simJob = doc.getRoot();
		VElement v = simJob.getXPathElementVector("JobPhase", -1);
		for (int i = 0; i < v.size(); i++) {
		    KElement phaseElement = v.elementAt(i);
		    JobPhase phase = new JobPhase();
		    phase.deviceStatus = EnumDeviceStatus.getEnum(phaseElement.getXPathAttribute("@DeviceStatus", "Idle"));
		    phase.deviceStatusDetails = phaseElement.getXPathAttribute("@DeviceStatusDetails", "");
		    phase.nodeStatus = EnumNodeStatus.getEnum(phaseElement.getXPathAttribute("@NodeStatus", "Waiting"));
		    phase.nodeStatusDetails = phaseElement.getXPathAttribute("@NodeStatusDetails", "");
		    phase.timeToGo = 1000 * StringUtil.parseInt( phaseElement.getXPathAttribute("@Duration", "0"), 0 );
		    phase.errorChance = StringUtil.parseInt( phaseElement.getXPathAttribute("@Error", "0"), 0 );
		    VElement vA=phaseElement.getChildElementVector("Amount", null);
		    for(int j=0;j<vA.size();j++)
		    {
		        KElement am=vA.elementAt(j);
		        double speed = am.getRealAttribute("Speed", null, 0);
                if(speed<0)
                    speed=0;
		        final boolean bGood = !am.getBoolAttribute("Waste", null, false);
		        //timeToGo is seconds, speed is / hour
		        phase.setAmount(am.getAttribute("Resource"), speed, bGood);  
		    }
		    phaseList.add(phase);
		}			


		if (phaseList.size() == 0) {
		    log.warn("no job phases were added from "+fileName);
		    return null;
		}
		log.debug("created new job from "+fileName+" with "+phaseList.size()+" job phases.");
        randomizeJobPhases(phaseList,simJob.getRealAttribute("RandomFactor", null, 0.0));

		return phaseList;
	}
	
	/**
	 * randomize phase durations and add random error phases 
	 * @param randomTime the given time of each job phase is to vary by ... percent
	 * @param errorPos random errors to create (as percentage of the total numbers of original job phases)
	 */
	private void randomizeJobPhases(List<JobPhase> phases, double randomTime)
	{
		if (randomTime > 0.0 && phases!=null)
		{
			for (int i=0;i<phases.size();i++)
			{
				double varyBy = Math.random()*randomTime/100.0;
				if (Math.random() < 0.5)
					varyBy *= -1.0;
				JobPhase phase=phases.get(i);
				phase.timeToGo=phase.timeToGo+(int)(phase.timeToGo*varyBy);
			}
		}		
	}
    /**
     * @param node the jdfdoc to process
     * @param qe the queueentry to process
     * @return EnumQueueEntryStatus the final status of the queuentry 
     */
    protected void initializeProcessDoc(JDFNode node, JDFQueueEntry qe) {
        _jobPhases = resumeQueueEntry(qe);
        if (_jobPhases == null)  {
            _jobPhases = loadJobFromFile(_devProperties.getAppDir()+"/config/job_"+_devProperties.getDeviceID()+".xml");
        }
        // we want at least one setup dummy
        if (_jobPhases == null)  {
            _jobPhases=new ArrayList<JobPhase>();
            _jobPhases.add(initFirstPhase(node));
        }
        super.initializeProcessDoc(node, qe);        
    }

		
	public SimDeviceProcessor() {
		super();
	}
    
    @Override
    protected void randomErrors(JobPhase phase)
    {
        if(phase==null)
            return;
        
        if(phase.errorChance>0 && Math.random()<phase.errorChance){
            JobPhase errorPhase=new JobPhase();
            errorPhase.deviceStatus=EnumDeviceStatus.Down;
            errorPhase.deviceStatusDetails=StringUtil.getRandomString();
            errorPhase.nodeStatus=EnumNodeStatus.Stopped;
            errorPhase.nodeStatusDetails=StringUtil.getRandomString();
            errorPhase.setTimeToGo((int)(10000*Math.random()+phase.getTimeToGo()*Math.random()));
            _jobPhases.add(1, errorPhase);
            _jobPhases.add(2,(JobPhase)phase.clone());
            phase.timeToGo=0;               
        }        
    }

  }

 