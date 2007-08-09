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

package org.cip4.bambi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFResourceLink.EnumUsage;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource;
import org.cip4.jdflib.resource.JDFResource.EnumResourceClass;
import org.cip4.jdflib.util.StringUtil;

/**
 * a simulated job for Bambi
 * @author boegerni
 *
 */
public class SimJobProcessor implements IDeviceProcessor
{
	/**
	 * a single job phase
	 * 
	 * @author boegerni
	 *
	 */
	public class JobPhase {
		/**
		 * status to be displayed for this job phase
		 */
		public EnumDeviceStatus deviceStatus=EnumDeviceStatus.Idle;
		/**
		 * device status details
		 */
		public String deviceStatusDetails = "";

		public EnumNodeStatus nodeStatus=EnumNodeStatus.Waiting;
		public String nodeStatusDetails="";
		
		/**
		 * duration of job phase in milliseconds
		 */
		public int  duration=0;

		/**
		 * output to be produced in this job phase
		 */
		public double Output_Good=0;
		/**
		 * waste to be produced in this job phase
		 */
		public double Output_Waste=0;
		
		public String toString()
		{
			return ("[JobPhase: Duration="+duration+", DeviceStatus="+deviceStatus.getName()
					+", DeviceStatusDetails="+deviceStatusDetails
					+", NodeStatus="+nodeStatus.getName()
					+", NodeStatusDetails="+nodeStatusDetails
					+", Good="+Output_Good+", Waste="+Output_Waste+"]");
		}

	}

	private static Log log = LogFactory.getLog(SimJobProcessor.class.getName());
	private List _jobPhases = null;
	private static final long serialVersionUID = -256551569245084031L;
	private IQueueProcessor _queueProcessor;
	private IStatusListener _statusListener;
	private Object _myListener; // the mutex for waiting and reawakening
	public boolean isPaused=false;
	
    /**
     * constructor
     */
    public SimJobProcessor(IQueueProcessor queueProcessor, IStatusListener statusListener, String deviceID)
    {
        super();
        init(queueProcessor, statusListener, deviceID);
    }
    
    public SimJobProcessor()
    {
    	super();
    }


	/**
	 * initialize the SimJobProcessor and load the default sim phases from "job_$(DeviceID).xml"
	 * @param queueProcessor
	 * @param statusListener
	 */
	public void init(IQueueProcessor queueProcessor, IStatusListener statusListener, String deviceID) 
	{
		log.info("SimJobProcessor construct");
        _queueProcessor=queueProcessor;
        _myListener=new Object();
        _queueProcessor.addListener(_myListener);
        _statusListener=statusListener;
        
        // try to load default a default job
        
        boolean hasLoaded = loadBambiJobFromFile("job_"+deviceID+".xml");
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
	public boolean loadBambiJobFromFile(String fileName)
	{
		// if fileName has no "/" or "\" it is assumed to be on the server and 
		// needs the config dir to be added
		if ( !fileName.contains(File.separator) )  
			fileName = DeviceServlet.configDir+fileName;
		
		_jobPhases = new ArrayList();
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

				_jobPhases.add(phase);
				counter++;
			}
		}
		catch (Exception ex)
		{
			log.warn("error in importing jobs");
			_jobPhases.clear();
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
				JobPhase phase=(JobPhase)_jobPhases.get(i);
				phase.duration=phase.duration+(int)(phase.duration*varyBy);
			}
		}
		
		if (errorPoss > 0.0)
		{
			int numberOfErrors = (int)((errorPoss/100.0)*_jobPhases.size());
			JobPhase errorPhase = new JobPhase();
			errorPhase.deviceStatus = EnumDeviceStatus.Down;
			errorPhase.deviceStatusDetails = "Random Bambi Error Phase";
			errorPhase.nodeStatus = EnumNodeStatus.InProgress;
			errorPhase.nodeStatusDetails = "Random Bambi Error phase";
			
			for (int i=0;i<numberOfErrors;i++)
			{
				double rand = Math.random();
				int insertErrorAtPos = (int)(rand*_jobPhases.size());
				errorPhase.duration=(int)(5000*rand);
				if (Math.random()>0.5)
					errorPhase.Output_Waste=100.0*rand;
				_jobPhases.add(insertErrorAtPos, errorPhase);
			}
		}
	}

	public EnumQueueEntryStatus processDoc(JDFDoc doc, JDFQueueEntry qe) {
		if(qe==null || doc==null)
        {
            log.error("proccessing null job");
            return EnumQueueEntryStatus.Aborted;
        }
        final String queueEntryID = qe.getQueueEntryID();
        log.info("Processing queueentry"+queueEntryID);
        JDFNode node=doc.getJDFRoot();
        VJDFAttributeMap vPartMap=qe.getPartMapVector();
        JDFAttributeMap partMap=vPartMap==null ? null : vPartMap.elementAt(0);
        final String workStepID = node.getWorkStepID(partMap);
        _statusListener.setNode(queueEntryID, workStepID, node, vPartMap, null);

        VElement v=node.getResourceLinks(null);
        int vSiz=v==null ? 0 : v.size();
        String inConsume=null;
        String outQuantity=null;
        for(int i=0;i<vSiz;i++)
        {
            JDFResourceLink rl=(JDFResourceLink) v.elementAt(i);
            JDFResource r=rl.getLinkRoot();
            EnumResourceClass c=r.getResourceClass();
            if(EnumResourceClass.Consumable.equals(c)
                    || EnumResourceClass.Handling.equals(c)
                    || EnumResourceClass.Quantity.equals(c) )
            {
                EnumUsage inOut=rl.getUsage();
                if(EnumUsage.Input.equals(inOut))
                {
                    if(EnumResourceClass.Consumable.equals(c))
                        inConsume=rl.getrRef();
                }
                else
                {
                    outQuantity=rl.getrRef();
                }
            }

        }
        String trackResourceID= inConsume !=null ? inConsume : outQuantity;
        _statusListener.setNode(queueEntryID, workStepID, node, vPartMap, trackResourceID);
        		
		if ( _jobPhases.isEmpty() )
		{
			log.warn("unable to start the job, no job loaded");
			return EnumQueueEntryStatus.Aborted;
		}
		for (int i=0;i<_jobPhases.size();i++)
		{
			JobPhase phase = (JobPhase)_jobPhases.get(i);
			_statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails, 
					phase.nodeStatus,phase.nodeStatusDetails);
			try {
				int repeats = (int)(phase.duration/1000);
				int remainder = phase.duration % 1000;
				for (int j=0;j < repeats; j++)
				{
					if (qe.getStatus()==EnumNodeStatus.Aborted)
						return abortQueueEntry();
					else
						Thread.sleep(1000);
				}
				if (qe.getStatus()==EnumNodeStatus.Aborted)
					return abortQueueEntry();
				else
					Thread.sleep(remainder);
				
			} catch (InterruptedException e) {
				log.warn("interrupted while sleeping");
			}
			// TODO update amount
//			_statusListener.updateAmount(resID, good, waste)
			
		}
		return EnumQueueEntryStatus.Completed;
	}

	/**
	 * @return
	 */
	private EnumQueueEntryStatus abortQueueEntry() {
		_statusListener.signalStatus(EnumDeviceStatus.Cleanup, "cleaning up the aborted job", EnumNodeStatus.Cleanup, "cleaning up the aborted job");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			log.error("interrupted while cleaning up the aborted job");
		}
		return EnumQueueEntryStatus.Aborted;
	}

	public void run() {
		while (true)
		{
            if(!processQueueEntry())
            {
                try
                {
                	if (log!=null)
                		log.debug("waiting");
                    synchronized (_myListener)
                    {
                        _myListener.wait(10000); // just in case                        
                    }
                }
                catch (InterruptedException x)
                {
                    log.error("interrupted while idle");
                }
            }
		}
	}
	
	private boolean processQueueEntry()
    {
        IQueueEntry iqe=_queueProcessor.getNextEntry();
        if (iqe!=null) // is there a new  QueueEntry to process?
        	if (iqe.getQueueEntry() != null)
        		if (iqe.getQueueEntry().getQueueEntryID() != null)
        			log.debug("processing: "+iqe.getQueueEntry().getQueueEntryID());
       
        if(iqe==null)
            return false;
        JDFDoc doc=iqe.getJDF();
        if(doc==null)
            return false;
        JDFQueueEntry qe=iqe.getQueueEntry();
        if(qe==null)
            return false;
        qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
        final String queueEntryID = qe.getQueueEntryID();
         _queueProcessor.updateEntry(queueEntryID, EnumQueueEntryStatus.Running);
        EnumQueueEntryStatus qes=null;
        try
        {
            log.info("processing JDF: ");
            qes=processDoc(doc,qe);
            qe.setQueueEntryStatus(qes);
            _queueProcessor.updateEntry(queueEntryID, qes);
            log.info("finalized processing JDF: ");
        }
        catch(Exception x)
        {
            log.error("error processing JDF: "+x);
            qe.setQueueEntryStatus(EnumQueueEntryStatus.Aborted);
            _queueProcessor.updateEntry(queueEntryID, EnumQueueEntryStatus.Aborted);
        }
        
        return true;
    }

	public void cancel() {
		// TODO Auto-generated method stub
		
	}
}