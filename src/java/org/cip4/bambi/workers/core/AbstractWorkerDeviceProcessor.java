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
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFResourceLink.EnumUsage;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource;
import org.cip4.jdflib.resource.JDFResource.EnumResourceClass;
import org.cip4.jdflib.util.StatusCounter;

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
    public static class JobPhase implements Serializable, Cloneable{
        /**
         * 
         */
        public class PhaseAmount implements Serializable{

            /**
             * 
             */
            private static final long serialVersionUID = -8504631585951268571L;

            /**
             * waste to be produced in this job phase
             */
            protected boolean bGood=true;
            /**
             * current speed/hour in this phase
             */
            protected double speed=0;

            protected String resource="Output";
            /**
             * @param resName
             * @param good
             * @param waste
             * @param speed speed / hour
             */
            public PhaseAmount(String resName,  double _speed, boolean condition)
            {
                resource=resName;
                bGood=condition;
                speed=_speed;
            }
            public String toString()
            {
                return "[ "+resource+(bGood ? " G: ":" W: ")+"Speed: "+speed+"]";
            }
            /**
             * @param res 
             * @return
             */
            public boolean matchesRes(String res)
            {
                return resource.equals(res);
            }



        }
        private static final long serialVersionUID = 2262422293566643131L;
        protected Vector<PhaseAmount> amounts=new Vector<PhaseAmount>();

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
         * timeToGo of job phase in milliseconds
         */
        public int  timeToGo=0;
        public long  timeStarted=System.currentTimeMillis();
        public double errorChance=0.01;


        @Override
        public String toString()
        {
            String s="[JobPhase: Duration="+timeToGo+", DeviceStatus="+deviceStatus.getName()
            +", DeviceStatusDetails="+deviceStatusDetails
            +", NodeStatus="+nodeStatus.getName()
            +", NodeStatusDetails="+nodeStatusDetails; 
            for(int i=0;i<amounts.size();i++) 
                s+="\n"+amounts.elementAt(i);
            return s;
        }

        public EnumDeviceStatus getDeviceStatus() {
            return deviceStatus;
        }

        public void setDeviceStatus(EnumDeviceStatus _deviceStatus) {
            this.deviceStatus = _deviceStatus;
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

        public int getTimeToGo() {
            return timeToGo;
        }

        public void setTimeToGo(int duration) {
            this.timeToGo = duration;
        }

        public void setAmount(String resName, double speed, boolean bGood){
            PhaseAmount pa=getPhaseAmount(resName);
            if(pa==null)
                amounts.add(this.new PhaseAmount(resName,speed,bGood));
        }

        public double getOutput_Speed(String res) {
            PhaseAmount pa=getPhaseAmount(res);
            return pa==null ? 0 : pa.speed;
        }

        public boolean getOutput_Condition(String res) {
            PhaseAmount pa=getPhaseAmount(res);
            return pa==null ? true : pa.bGood;
        }

        /**
         * @param string
         * @return
         */
        private PhaseAmount getPhaseAmount(String res)
        {
            for(int i=0;i<amounts.size();i++)
            {
                if(amounts.elementAt(i).matchesRes(res))
                    return amounts.elementAt(i);
            }
            return null;
        }

        /**
         * @return the list of amount counting resources in this phase
         */
        public VString getAmountResourceNames()
        {
            VString v=new VString();
            for(int i=0;i<amounts.size();i++)
            {
                v.add(amounts.elementAt(i).resource);
            }
            return v;
        }

        @Override
        public Object clone()
        {
            JobPhase jp=new JobPhase();
            jp.deviceStatus=deviceStatus;
            jp.deviceStatusDetails=deviceStatusDetails;
            jp.timeToGo=timeToGo;
            jp.nodeStatus=nodeStatus;
            jp.nodeStatusDetails=nodeStatusDetails;
            jp.errorChance=errorChance;
            return jp;
        }

        /**
         * @param resource
         * @param i
         * @return
         */
        public double getOutput_Waste(String resource, int i)
        {
            if(getOutput_Condition(resource))
                return 0;
            return getOutput(resource, i);
        }

        private double getOutput(String resource, int i)
        {
            if(i<=0)
                return 0; // negative time??? duh
            double spd=getOutput_Speed(resource);
            if(spd<=0)
                return 0;
            return (spd * i) / (3600 * 1000);
        }
        /**
         * @param resource
         * @param i
         * @return
         */
        public double getOutput_Good(String resource, int i)
        {
            if(!getOutput_Condition(resource))
                return 0;
            return getOutput(resource, i);
        }
    }

    /**
     * constructor
     * @param queueProcessor points to the QueueProcessor
     * @param statusListener points to the StatusListener
     * @param devProperties  device properties
     */
    public AbstractWorkerDeviceProcessor(IQueueProcessor queueProcessor, 
            StatusListener statusListener, IDeviceProperties devProperties)
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
    public void init(IQueueProcessor queueProcessor, StatusListener statusListener, IDeviceProperties devProperties)
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
     * @param qe the QueueEntry to look for
     * @return a {@link List} of {@link JobPhase}. Returns null if no remaining phases have been found.
     */
    @SuppressWarnings("unchecked")
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

    /**
     * get an ArrayList with all JobPhases
     * @return
     */
    public List<JobPhase> getJobPhases() {
        return _jobPhases;
    }

    protected static boolean isMatchingLink(JDFResourceLink li,  String resName)
    {
        if(resName==null)
            return false;

        boolean bIsLink = resName.equals(li.getNamedProcessUsage());
        // 200602 RP added fix
        if(!bIsLink)
        {
            bIsLink = resName.equals(li.getLinkedResourceName());
        }

        // 230802 RP added check for ID in vRWResources
        if(!bIsLink)
        {
            bIsLink = resName.equals(li.getrRef());
        }

        // 040902 RP added check for Usage in vRWResources
        if(!bIsLink)
        {
            bIsLink = resName.equals(li.getAttribute(AttributeName.USAGE));
        }
        return bIsLink;
    }

    /**
     * remember where we stopped, so we can resume later
     * @param queueEntryID the ID of the queue we are talking about
     * @param currentPhase the last phase that has been processed
     * @param remainingPhaseTime how long is the first phase to run after resuming
     */
    protected void persistRemainingPhases()
    {
        if(currentQE==null)
            return;
        
        // add all remaining phases to a new list
        List<JobPhase> phases = new ArrayList<JobPhase>();
        for (int i=0;i<_jobPhases.size();i++) {
            phases.add( _jobPhases.get(i) );
        }
        final String queueEntryID=currentQE.getQueueEntryID();

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

    /**
     * get the current job phase, null if none is ther
     * @return
     */
    public JobPhase getCurrentJobPhase() {
        if ( _jobPhases != null && _jobPhases.size() > 0)
            return _jobPhases.get(0);
        return null;
    }


    public EnumQueueEntryStatus processDoc(JDFDoc doc, JDFQueueEntry qe) {
        JobPhase lastPhase=null;
        while ( _jobPhases.size()>0 ) {
            processPhase();
            lastPhase=_jobPhases.remove(0); // phase(0) is always the active phase
         }
        EnumQueueEntryStatus qes=lastPhase==null ? null : EnumNodeStatus.getQueueEntryStatus(lastPhase.nodeStatus);
        if(qes==null)
            qes=EnumQueueEntryStatus.Aborted;
        return qes;
    }

    private void processPhase()
    {
        JobPhase phase = getCurrentJobPhase();
        log.info("processing new job phase: "+phase.toString());
        _statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails,phase.nodeStatus,phase.nodeStatusDetails);          
        long deltaT=1000;
        while ( phase.timeToGo>0 ) {
            long t0=System.currentTimeMillis();
            _statusListener.updateAmount(null, phase.getOutput_Good(_trackResource,(int)deltaT), phase.getOutput_Waste(_trackResource,(int)deltaT));
            _statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails,phase.nodeStatus,phase.nodeStatusDetails);

            randomErrors(phase);
            StatusCounter.sleep(1000);
            long t1=System.currentTimeMillis();
            deltaT=t1-t0;
            phase.timeToGo-=deltaT;
        }
    }

    /**
     * generate random errors
     */
    protected void randomErrors(JobPhase phase)
    {
        // nop - only overwritten in sim

    }
 
    @Override
    protected boolean finalizeProcessDoc(EnumQueueEntryStatus qes)
    {
        boolean b=super.finalizeProcessDoc(qes);
        _jobPhases.clear();
        return b;
    }

    @Override
    protected void initializeProcessDoc(JDFDoc doc, JDFQueueEntry qe)
    {
        // TODO Auto-generated method stub
        super.initializeProcessDoc(doc,qe);
        if(qe==null || doc==null) {
            log.error("proccessing null job");
            return;
        }
        currentQE=new QueueEntry(doc,qe);
        if ( _jobPhases==null ) {
            _jobPhases = new ArrayList<JobPhase>();
        }
        qe.setDeviceID( _devProperties.getDeviceID() );
        final String queueEntryID = qe.getQueueEntryID();
        log.info("Processing queueentry "+queueEntryID);

        JDFNode node=doc.getJDFRoot();
        VJDFAttributeMap vPartMap=qe.getPartMapVector();
        if(vPartMap==null)
            vPartMap=node.getPartMapVector();
        
        JDFAttributeMap partMap=vPartMap==null ? null : vPartMap.elementAt(0);
        final String workStepID = node.getWorkStepID(partMap);

        VElement vResLinks=node.getResourceLinks(null);
        String inConsume=null;
        String outQuantity=null;
        String trackResourceID=null;
        if (vResLinks!=null) {
            int vSiz=vResLinks.size();
            for (int i = 0; i < vSiz; i++) {
                JDFResourceLink rl = (JDFResourceLink) vResLinks.elementAt(i);
                if(isMatchingLink(rl, _trackResource))
                {
                    trackResourceID=rl.getrRef();
                    break; // gotcha
                }
            }

            //heuristics in case we didn't find anything
            if(trackResourceID==null)
            {
                for (int i = 0; i < vSiz; i++) {
                    JDFResourceLink rl = (JDFResourceLink) vResLinks.elementAt(i);
                    JDFResource r = rl.getLinkRoot();
                    EnumResourceClass c = r.getResourceClass();
                    if (EnumResourceClass.Consumable.equals(c)
                            || EnumResourceClass.Handling.equals(c)
                            || EnumResourceClass.Quantity.equals(c)) {
                        EnumUsage inOut = rl.getUsage();
                        if (EnumUsage.Input.equals(inOut)) {
                            if (EnumResourceClass.Consumable.equals(c))
                                inConsume = rl.getrRef();
                        } else {
                            outQuantity = rl.getrRef();
                        }
                    }
                }
                trackResourceID= inConsume !=null ? inConsume : outQuantity;
            }
        }
        _statusListener.setNode(queueEntryID, workStepID, node, vPartMap, trackResourceID);
    }

    @Override
    public void stopProcessing(EnumNodeStatus newStatus)
    {
        synchronized (_jobPhases)
        {
            JobPhase p=getCurrentJobPhase();
            if(p!=null)
                p.timeToGo=0;
            _jobPhases.clear();
            p=new JobPhase();            
            p.setNodeStatus(newStatus);
            p.setDeviceStatus(EnumDeviceStatus.Idle);
            doNextPhase(p);
        }

    }

    /**
     * proceed to the next job phase
     * @param newPhase the next job phase to process.<br>
     * Phase timeToGo is ignored in this class, it is advancing to the next phase 
     * solely by doNextPhase().
     */
    public void doNextPhase(JobPhase nextPhase)
    {
        JobPhase lastPhase=getCurrentJobPhase();
        if(lastPhase!=null)
            lastPhase.timeToGo=0;
        _jobPhases.add(nextPhase);
    }
}
