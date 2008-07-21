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
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.bambi.workers.core.AbstractWorkerDeviceProcessor.JobPhase.PhaseAmount;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFException;
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
            protected String resourceName="Output";
            /**
             * @param resName
             * @param good
             * @param waste
             * @param speed speed / hour
             */
            public PhaseAmount(String resName,  double _speed, boolean condition)
            {
                resource=resourceName=resName;
                bGood=condition;
                speed=_speed;
            }
            public String toString()
            {
                return "[ "+resourceName+" "+resource+(bGood ? " G: ":" W: ")+"Speed: "+speed+"]";
            }
            /**
             * @param res 
             * @return
             */
            public boolean matchesRes(String res)
            {
                return resource.equals(res)||resourceName.equals(res);
            }
            @Override
            protected Object clone() 
            {
                PhaseAmount pa=new PhaseAmount(null,speed,bGood);
                pa.resource=resource;
                pa.resourceName=resourceName;
                return pa;
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
        public double errorChance=0.00;


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

        public PhaseAmount setAmount(String resName, double speed, boolean bGood){
            PhaseAmount pa=getPhaseAmount(resName);
            if(pa==null)
            {
                pa = this.new PhaseAmount(resName,speed,bGood);
                amounts.add(pa);
            }
            else
            {
                pa.bGood=bGood;
                pa.speed=speed;
            }   
            return pa;
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
                v.add(amounts.elementAt(i).resourceName);
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
            jp.amounts=(Vector<PhaseAmount>) amounts.clone();
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

        /**
         * update the abstract resourcelink names with real idref values from the link
         * @param rl
         */
        public void updateAmountLinks(JDFResourceLink rl)
        {
            if(rl==null || amounts==null)
                return;
            for(int i=0;i<amounts.size();i++)
            {
                PhaseAmount pa=amounts.get(i);
                if(rl.matchesString(pa.resource))
                    pa.resource=rl.getrRef();
            }           
        }
    }

    /**
     * constructor
     * @param queueProcessor points to the QueueProcessor
     * @param statusListener points to the StatusListener
     * @param devProperties  device properties
     */
    public AbstractWorkerDeviceProcessor(QueueProcessor queueProcessor, StatusListener statusListener, IDeviceProperties devProperties)
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
    public void init(QueueProcessor queueProcessor, StatusListener statusListener, IDeviceProperties devProperties)
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
//        if(currentQE==null)
//            _jobPhases.clear(); // just in case we have some remaining spurious phases
        
        if ( _jobPhases != null && _jobPhases.size() > 0)
            return _jobPhases.get(0);
        return null;
    }


    public EnumQueueEntryStatus processDoc(JDFNode n, JDFQueueEntry qe) {
        JobPhase lastPhase=null;
        while ( _jobPhases.size()>0 ) {
            processPhase(n);
            lastPhase=_jobPhases.remove(0); // phase(0) is always the active phase
        }
        if(lastPhase==null)
            return EnumQueueEntryStatus.Aborted;

        EnumQueueEntryStatus qes=EnumNodeStatus.getQueueEntryStatus(lastPhase.nodeStatus);
        if(qes==null)
            return EnumQueueEntryStatus.Aborted;
        if(lastPhase.timeToGo<=0 && EnumQueueEntryStatus.Running.equals(qes)) // final phase was active
        {
            qes=EnumQueueEntryStatus.Completed;
        }

        return qes;
    }

    private void processPhase(JDFNode n)
    {
        JDFResourceLink rlAmount=getAmountLink(n);
        String namedRes=rlAmount==null ? null : rlAmount.getrRef();
        JobPhase phase = getCurrentJobPhase();
        double all=rlAmount==null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.ACTUALAMOUNT, n==null ? null : n.getNodeInfoPartMapVector());
        if(all<0)
            all=0;
        double todoAmount=rlAmount==null ? 0 : rlAmount.getAmountPoolSumDouble(AttributeName.AMOUNT, n==null ? null : n.getNodeInfoPartMapVector());
        log.info("processing new job phase: "+phase.toString());
        _statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails, phase.nodeStatus, phase.nodeStatusDetails,false);          
        long deltaT=1000;
        while ( phase.timeToGo>0 ) {
            long t0=System.currentTimeMillis();
            VString names=phase.getAmountResourceNames();
            boolean reachedEnd=false;
            for(int i=0;i<names.size();i++)
            {
                PhaseAmount pa=phase.getPhaseAmount(names.get(i));
                if(pa!=null)
                {
                    final double phaseGood = phase.getOutput_Good(pa.resource,(int)deltaT);
                    final double phaseWaste = phase.getOutput_Waste(pa.resource,(int)deltaT);
                    _statusListener.updateAmount(pa.resource, phaseGood, phaseWaste);
                    if(namedRes!=null && pa.matchesRes(namedRes))
                    {
                        all+=phaseGood;
                        if(all>todoAmount && todoAmount>0)
                        {
                            phase.timeToGo=0;
                            log.info("phase end for resource: "+namedRes);
                            reachedEnd=true;
                        }
                    }
                }
            }
            if(_doShutdown)
            {
                phase.timeToGo=0;
                reachedEnd=true;
                log.info("external shutdown: "+phase.toString());
            }
            _statusListener.signalStatus(phase.deviceStatus, phase.deviceStatusDetails,phase.nodeStatus,phase.nodeStatusDetails, reachedEnd);
            if(phase.timeToGo>0 &&!_doShutdown)
            {
                randomErrors(phase);
                StatusCounter.sleep(123);
                long t1=System.currentTimeMillis();
                deltaT=t1-t0;
                phase.timeToGo-=deltaT;
            }
        }
    }

    /**
     * @param n
     * @return
     */
    private JDFResourceLink getAmountLink(JDFNode n)
    {
        VJDFAttributeMap vMap=n.getNodeInfoPartMapVector();

        VElement v=n.getResourceLinks(new JDFAttributeMap(AttributeName.USAGE,EnumUsage.Output));
        int siz= v==null ? 0 : v.size();
        for(int i=0;i<siz;i++)
        {
            JDFResourceLink rl=(JDFResourceLink)v.elementAt(i);
            try{
                double d=rl.getAmountPoolSumDouble(AttributeName.AMOUNT, vMap);
                if(d>=0)
                    return rl;
            }
            catch (JDFException e) {
                // nop
            }
        }
        return null;
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
    protected void initializeProcessDoc(JDFNode node, JDFQueueEntry qe)
    {
        super.initializeProcessDoc(node,qe);
        if(qe==null || node==null) {
            log.error("proccessing null job");
            return;
        }
        qe.setDeviceID( _devProperties.getDeviceID() );
        final String queueEntryID = qe.getQueueEntryID();
        log.info("Processing queueentry "+queueEntryID);
        int jobPhaseSize=_jobPhases==null ? 0 : _jobPhases.size();

        VElement vResLinks=node.getResourceLinks(null);
        int vSiz= (vResLinks==null) ? 0 : vResLinks.size();
        for (int i = 0; i < vSiz; i++) {
            JDFResourceLink rl = (JDFResourceLink) vResLinks.elementAt(i);
            for(int j=0;j<jobPhaseSize;j++)
            {
                JobPhase jp=_jobPhases.get(j);
                jp.updateAmountLinks(rl);
            }
        }
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
            if(newStatus!=null)
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
        int pos=0;
        if(lastPhase!=null)
        {
            lastPhase.timeToGo=0;
            pos=1;
        }
        _jobPhases.add(pos,nextPhase);
    }

    protected JobPhase initFirstPhase(JDFNode node) {
        log.info("initializing first phase");
        JobPhase firstPhase = new JobPhase();
        firstPhase.deviceStatus=EnumDeviceStatus.Setup;
        firstPhase.deviceStatusDetails="Setup";
        firstPhase.nodeStatus=EnumNodeStatus.Setup;
        firstPhase.nodeStatusDetails="Setup";
        firstPhase.timeToGo=Integer.MAX_VALUE/2;
        if(node!=null)
        {
            VElement v=node.getResourceLinks(null);
            int s=v==null ? 0 : v.size();
            for(int i=0;i<s;i++)
            {
                JDFResourceLink rl=(JDFResourceLink) v.get(i);
                final JDFResource linkRoot = rl.getLinkRoot();
                if(linkRoot!=null && ((AbstractWorkerDevice)_parent).isAmountResource(rl))
                {
                    PhaseAmount pa=firstPhase.setAmount(rl.getNamedProcessUsage(), 0, false);  
                    pa.resource=linkRoot.getID();
                }
            }
        }
        else
        {
            firstPhase.setAmount(_trackResource, 0, false);
        }
        return firstPhase;   
    }

    @Override
    public String toString()
    {
        StringBuffer b=new StringBuffer(1000);
        int siz=_jobPhases==null ? 0 : _jobPhases.size();
        if(siz==0)
            b.append("no phases");
        else
        {
            b.append(siz+" phases:\n");
            for(int i=0;i<siz;i++)
            {
                b.append(_jobPhases.get(i).toString());
                b.append("\n");
            }
        }
        return "Abstract Worker Device Processor: "+super.toString()+"\nPhases: "+b.toString() + "]";
    }

}
