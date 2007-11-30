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
package org.cip4.bambi.workers.core;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFQueueEntry;

/**
 * abstract parent class for device processors, with aditional functionality for JobPhases <br>
 * The device processor is the actual working part of a device. The individual job phases of
 * the job are executed here. 
 * @author boegerni
 *
 */
public abstract class AbstractWorkerDeviceProcessor extends AbstractDeviceProcessor {
	private static Log log = LogFactory.getLog(AbstractWorkerDeviceProcessor.class.getName());
	protected List<JobPhase> _jobPhases=null;
	
	/**
	 * a single job phase
	 * 
	 * @author boegerni
	 *
	 */
	public static class JobPhase implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2262422293566643131L;
		
		public JobPhase() {
			super();
		}
		
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
		
		@Override
		public String toString()
		{
			return ("[JobPhase: Duration="+duration+", DeviceStatus="+deviceStatus.getName()
					+", DeviceStatusDetails="+deviceStatusDetails
					+", NodeStatus="+nodeStatus.getName()
					+", NodeStatusDetails="+nodeStatusDetails
					+", Good="+Output_Good+", Waste="+Output_Waste+"]");
		}

		public EnumDeviceStatus getDeviceStatus() {
			return deviceStatus;
		}

		public void setDeviceStatus(EnumDeviceStatus deviceStatus) {
			this.deviceStatus = deviceStatus;
		}

		public String getDeviceStatusDetails() {
			return deviceStatusDetails;
		}

		public void setDeviceStatusDetails(String deviceStatusDetails) {
			this.deviceStatusDetails = deviceStatusDetails;
		}

		public EnumNodeStatus getNodeStatus() {
			return nodeStatus;
		}

		public void setNodeStatus(EnumNodeStatus nodeStatus) {
			this.nodeStatus = nodeStatus;
		}

		public String getNodeStatusDetails() {
			return nodeStatusDetails;
		}

		public void setNodeStatusDetails(String nodeStatusDetails) {
			this.nodeStatusDetails = nodeStatusDetails;
		}

		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public double getOutput_Good() {
			return Output_Good;
		}

		public void setOutput_Good(double output_Good) {
			Output_Good = output_Good;
		}

		public double getOutput_Waste() {
			return Output_Waste;
		}

		public void setOutput_Waste(double output_Waste) {
			Output_Waste = output_Waste;
		}

	}
		
	/**
	 * constructor
	 * @param queueProcessor points to the QueueProcessor
	 * @param statusListener points to the StatusListener
	 * @param devProperties  device properties
	 */
	public AbstractWorkerDeviceProcessor(IQueueProcessor queueProcessor, 
			IStatusListener statusListener, IDeviceProperties devProperties)
	{
		super();
        init(queueProcessor, statusListener, devProperties);
	}
	
	/**
     * constructor
     */
	public AbstractWorkerDeviceProcessor()
	{
		super();
	}
	
   
    /**
     * initialize the IDeviceProcessor
     * @param _queueProcessor
     * @param _statusListener
     */
    @Override
	public void init(IQueueProcessor queueProcessor, IStatusListener statusListener, IDeviceProperties devProperties)
    {
    	super.init(queueProcessor, statusListener, devProperties);
        _jobPhases = new ArrayList<JobPhase>();
    }
    
    @Override
	protected EnumQueueEntryStatus suspendQueueEntry(JDFQueueEntry qe, int currentPhase, int remainingPhaseTime)
	{
    	if (qe!=null) {
    		persistRemainingPhases(qe.getQueueEntryID(), currentPhase, 0);
    	}
    	return super.suspendQueueEntry(qe, currentPhase, remainingPhaseTime);
	}
    
    /**
     * check whether qe has been suspended before, and get its remaining job phases if there are any.
     * @param qe the QueueEntry to look for
     * @return a {@link List} of {@link JobPhase}. Returns null if no remaining phases have been found.
     */
    @SuppressWarnings({ "unchecked", "unchecked" })
	protected List<JobPhase> resumeQueueEntry(JDFQueueEntry qe)
    {
    	List<JobPhase> phases=null;
    	String queueEntryID=qe.getQueueEntryID();
    	String fileName = _devProperties.getBaseDir()+queueEntryID+"_phases.xml";
    	if ( !new File(fileName).canRead() ) {
			return null;
		}
		XMLDecoder dec = null; 
	    try { 
	      dec = new XMLDecoder( new FileInputStream(fileName) ); 
	      phases = (List<JobPhase>) dec.readObject();   
	    } catch ( IOException e ) { 
	    	log.error( "error while deserializing: "+e.getMessage() );
	    } finally { 
	      if ( dec!=null ) 
	        dec.close(); 
	    }
    	
		// delete file with remaining phases after loading
		boolean deleted=(new File(fileName)).delete();
		if (!deleted) {
			log.warn( "failed to delete file with remaining job phases after "
					+ "resuming ->'"+fileName+"'" );
		}
		log.info( "successfully loaded remaining phases from "+fileName );
		return phases;
    }
	
	public abstract JobPhase getCurrentJobPhase();
	
	/**
	 * get an ArrayList with all JobPhases
	 * @return
	 */
	public List<JobPhase> getJobPhases() {
		return _jobPhases;
	}
	
	/**
	 * remember where we stopped, so we can resume later
	 * @param queueEntryID the ID of the queue we are talking about
	 * @param currentPhase the last phase that has been processed
	 * @param remainingPhaseTime how long is the first phase to run after resuming
	 */
	protected void persistRemainingPhases(String queueEntryID, int currentPhase, int remainingPhaseTime)
	{
		if ( queueEntryID==null || queueEntryID.equals("") ) {
			log.error("missing QueueEntry ID, aborting persist");
			return;
		}
		
		// make sure there are remaining phases left
		if ( currentPhase >= _jobPhases.size() ) {
			log.info("no more phases remaning, stopping persist");
			return;
		}
		
		// add all remaining phases to a new list
		List<JobPhase> phases = new ArrayList<JobPhase>();
		for (int i=currentPhase;i<_jobPhases.size();i++) {
			phases.add( _jobPhases.get(i) );
		}
		
		// adjust the time of the first job phase
		JobPhase firstPhase = phases.get(0);
		firstPhase.duration = remainingPhaseTime;
		phases.set(0, firstPhase);
		
		// serialize the remaining job phases
		String fileName = _devProperties.getBaseDir()+queueEntryID+"_phases.xml";
		XMLEncoder enc = null; 	 
	    try { 
	      enc = new XMLEncoder( new FileOutputStream(fileName) ); 
	      enc.writeObject( phases );
	    } catch ( IOException e ) { 
	      log.error( "error while persisting: "+e.getMessage() );
	    } finally { 
	      if ( enc != null ) 
	        enc.close(); 
	    }
		log.info("remaining phases have been saved to "+fileName);
	}
}
