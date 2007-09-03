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
 * handleMessage f�r requestQueueEntry
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
	public static class JobPhase {
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

    protected void suspendQueueEntry(JDFQueueEntry qe)
	{
		// TODO process next qe instead of waiting
		while (qe.getQueueEntryStatus()==EnumQueueEntryStatus.Suspended)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("interrupted QueueEntry while waiting for ");
			}
		}
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
        
        // remember to call finalizeProcessDoc() at the of derived processDoc implementations
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
}
