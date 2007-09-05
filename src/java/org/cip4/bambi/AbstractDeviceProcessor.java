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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFResourceLink.EnumUsage;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource;
import org.cip4.jdflib.resource.JDFResource.EnumResourceClass;

//TODO: pull device implementieren
/*
 * handleMessage für requestQueueEntry
 * kein automatisches auspicken aus der queue
 * weitere messages an das geforwarded device leiten
 */

/**
 * abstract parent class for simu job processors
 * @author boegerni
 *
 */
public abstract class AbstractDeviceProcessor implements IDeviceProcessor
{
	private static Log log = LogFactory.getLog(AbstractDeviceProcessor.class.getName());
	protected List _jobPhases = null;
	protected IQueueProcessor _queueProcessor;
	protected IStatusListener _statusListener;
	protected Object _myListener; // the mutex for waiting and reawakening
	protected String _trackResourceID="";
	
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
	
	/**
     * constructor
     */
	public AbstractDeviceProcessor(IQueueProcessor queueProcessor, IStatusListener statusListener, String deviceID)
	{
		super();
        init(queueProcessor, statusListener, deviceID);
	}
	
	/**
     * constructor
     */
	public AbstractDeviceProcessor()
	{
		super();
	}
	
    /**
     * this is the device processor loop
     */
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
   
    /**
     * initialize the IDeviceProcessor
     * @param _queueProcessor
     * @param _statusListener
     * @param deviceID 
     */
    public void init(IQueueProcessor queueProcessor, IStatusListener statusListener, String deviceID)
    {
    	log.info(this.getClass().getName()+" construct");
        _queueProcessor=queueProcessor;
        _myListener=new Object();
        _queueProcessor.addListener(_myListener);
        _statusListener=statusListener;
        _jobPhases = new ArrayList();
    }
    
    protected boolean processQueueEntry()
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

    protected EnumQueueEntryStatus suspendQueueEntry(JDFQueueEntry qe, int currentPhase, int remainingPhaseTime)
	{
    	if (qe!=null) {
    		persistRemainingPhases(qe.getQueueEntryID(), currentPhase, 0);
    	}
    	finalizeProcessDoc();
    	return EnumQueueEntryStatus.Suspended;
	}
    
    /**
     * check whether qe has been suspended before, and load its remaining job phases if required.
     * @param qe the QueueEntry to look for
     * @return the list of loaded phases, null if none has been found.
     */
    protected List resumeQueueEntry(JDFQueueEntry qe)
    {
    	return loadRemainingPhases( qe.getQueueEntryID(), true );
    }
    
	protected EnumQueueEntryStatus abortQueueEntry() {
		_statusListener.signalStatus(EnumDeviceStatus.Cleanup, "WashUp", EnumNodeStatus.Cleanup, "cleaning up the aborted job");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			log.error("interrupted while cleaning up the aborted job");
		}
		_statusListener.signalStatus(EnumDeviceStatus.Idle, "JobCanceledByUser", EnumNodeStatus.Aborted, "job canceled by user");
		_statusListener.setNode(null, null, null, null, null);
		return EnumQueueEntryStatus.Aborted;
	}
	
    /**
     * @param doc
     * @return EnumQueueEntryStatus the final status of the queuentry 
     */
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
        
        _trackResourceID= inConsume !=null ? inConsume : outQuantity;
        _statusListener.setNode(queueEntryID, workStepID, node, vPartMap, _trackResourceID);
        
        
        
        // remember to call finalizeProcessDoc() at the end of derived processDoc implementations
		return null;
	}

	/**
	 * signal that processing has finished and ready the StatusCounter for the next process
	 * @return EnumQueueEntryStatus.Completed
	 */
	protected EnumQueueEntryStatus finalizeProcessDoc()
	{
		_statusListener.signalStatus(EnumDeviceStatus.Idle, "Idle", EnumNodeStatus.Completed, "job completed");
		_statusListener.setNode(null, null, null, null, null);
		return EnumQueueEntryStatus.Completed;
	}
	
	public abstract JobPhase getCurrentJobPhase();
	
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
		List phases = new ArrayList();
		for (int i=currentPhase;i<_jobPhases.size();i++) {
			phases.add( _jobPhases.get(i) );
		}
		
		// adjust the time of the first job phase
		JobPhase firstPhase = (JobPhase) phases.get(0);
		firstPhase.duration = remainingPhaseTime;
		phases.set(0, firstPhase);
		
		// serialize the remaining job phases
		String fileName = org.cip4.bambi.servlets.DeviceServlet.baseDir+queueEntryID+".phases";
		FileOutputStream f_out=null;
		try {
			f_out = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			log.error( "serialization of the remaining job phases failed: \r\n"+e.getMessage() );
		}
		if (f_out == null) {
			log.error( "serialization of the remaining job phases failed: FileOutputStream is null");
			return;
		}
		
		ObjectOutputStream obj_out=null;
		try {
			obj_out = new ObjectOutputStream (f_out);
			obj_out.writeObject ( phases );
		} catch (IOException e) {
			log.error( "serialization of the remaining job phases failed: \r\n"+e.getMessage() );
		}
		log.info("remaining phases have been saved to "+fileName);
	}
	
	/**
	 * get are remaining job phases for the given QueueEntry.
	 * @param queueEntryID the ID of the Queue to load the phases for
	 * @param doDeleteFile delete the file with the list of remaining phases, after they have been loaded successfully
	 * @return a {@link List} of {@link JobPhase}, null of no remaining phases have been found
	 */
	protected List loadRemainingPhases(String queueEntryID, boolean doDeleteFile)
	{
		String fileName = org.cip4.bambi.servlets.DeviceServlet.baseDir+queueEntryID+".phases";
		// Read from disk using FileInputStream
		FileInputStream f_in;
		try {
			f_in = new FileInputStream(fileName);
		} catch (FileNotFoundException e2) {
			log.info( "no remaining job phases found for QueueEntry with ID="+queueEntryID );
			return null;
		}

		Object obj=null;
		try {
			ObjectInputStream obj_in = new ObjectInputStream (f_in);
			obj = obj_in.readObject();
		} catch (Exception e) {
			log.error( "failed to load remaining phases for QueueEntry with ID="
					+queueEntryID+": "+e.getMessage() );
		}

		List phases = null;
		if (obj instanceof List)
		{
			phases = (List) obj;
			if (doDeleteFile) {
				(new File(fileName)).delete();
			}
			return phases;
		} else {
			return null;
		}
	}
}
