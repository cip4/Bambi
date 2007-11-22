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
package org.cip4.bambi.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.IQueueProcessor;
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

/**
 * abstract parent class for device processors <br>
 * The device processor is the actual working part of a device. 
 * @author boegerni
 *
 */
public abstract class AbstractDeviceProcessor implements IDeviceProcessor
{
    private static Log log = LogFactory.getLog(AbstractDeviceProcessor.class.getName());
    /**
     * note: the queue processor points to the queue processor of the device, 
     * it !does not! copy it
     */
    protected IQueueProcessor _queueProcessor;
    protected IStatusListener _statusListener;
    protected Object _myListener; // the mutex for waiting and reawakening
    protected String _trackResourceID=null;
    protected IDeviceProperties _devProperties=null;
    protected List<ChangeQueueEntryStatusRequest> _updateStatusReqs=null;
    protected boolean _doShutdown=false;

    protected static class ChangeQueueEntryStatusRequest {
        public String queueEntryID=null;
        public EnumQueueEntryStatus newStatus=null;
        public ChangeQueueEntryStatusRequest(String qeid, EnumQueueEntryStatus status) {
            queueEntryID=qeid;
            newStatus=status;
        }
    }

    /**
     * constructor
     * @param queueProcessor points to the QueueProcessor
     * @param statusListener points to the StatusListener
     * @param devProperties TODO
     */
    public AbstractDeviceProcessor(IQueueProcessor queueProcessor, 
            IStatusListener statusListener, IDeviceProperties devProperties)
    {
        super();
        init(queueProcessor, statusListener, devProperties);
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
        while (!_doShutdown)
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
     */
    public void init(IQueueProcessor queueProcessor, IStatusListener statusListener, IDeviceProperties devProperties)
    {
        log.info(this.getClass().getName()+" construct");
        _queueProcessor=queueProcessor;
        _myListener=new Object();
        _queueProcessor.addListener(_myListener);
        _statusListener=statusListener;
        _devProperties=devProperties;
        _updateStatusReqs=new ArrayList<ChangeQueueEntryStatusRequest>();
    }

    protected boolean processQueueEntry()
    {
        IQueueEntry iqe=_queueProcessor.getNextEntry();
        if(iqe==null)
            return false;
        JDFQueueEntry qe=iqe.getQueueEntry();         
        if(qe==null)
            return false;
        final String queueEntryID = qe.getQueueEntryID();
        if (queueEntryID != null)
            log.debug("processing: "+queueEntryID);

        JDFDoc doc=iqe.getJDF();
        if(doc==null)
            return false;

        initializeProcessDoc(queueEntryID);

        try {
            log.info("processing JDF: ");
            EnumQueueEntryStatus qes=processDoc(doc,qe);
            finalizeProcessDoc();
            if (qes==null) {
                if (log!=null)
                    log.error( "QueueEntryStatus is null" );
                return false;
            }
            qe.setQueueEntryStatus(qes);
            _queueProcessor.updateEntry(queueEntryID, qes);
            log.info("finalized processing JDF: ");
        } catch(Exception x) {
            log.error("error processing JDF: "+x);
            qe.setQueueEntryStatus(EnumQueueEntryStatus.Aborted);
            _queueProcessor.updateEntry(queueEntryID, EnumQueueEntryStatus.Aborted);
            return false;
        }

        return true;
    }

    /**
     * genric setup of processing 
     * @param queueEntryID the queueEntryID of the job to process
     */
    protected void initializeProcessDoc(final String queueEntryID)
    {
        _queueProcessor.updateEntry(queueEntryID, EnumQueueEntryStatus.Running);
    }

    protected EnumQueueEntryStatus suspendQueueEntry(JDFQueueEntry qe, int currentPhase, int remainingPhaseTime)
    {
        _statusListener.signalStatus(EnumDeviceStatus.Idle, "Idle", EnumNodeStatus.Suspended, "job suspended");
        _statusListener.setNode(null, null, null, null, null);
        return EnumQueueEntryStatus.Suspended;
    }

    protected EnumQueueEntryStatus abortQueueEntry() {
        _statusListener.signalStatus(EnumDeviceStatus.Idle, "JobCanceledByUser", EnumNodeStatus.Aborted, "job canceled by user");
        _statusListener.setNode(null, null, null, null, null);
        return EnumQueueEntryStatus.Aborted;
    }

    /**
     * @param doc
     * @return EnumQueueEntryStatus the final status of the queuentry 
     */
    public EnumQueueEntryStatus processDoc(JDFDoc doc, JDFQueueEntry qe) {
        if(qe==null || doc==null) {
            log.error("proccessing null job");
            return EnumQueueEntryStatus.Aborted;
        }
        BambiNSExtension.setDeviceID(qe, _devProperties.getDeviceID());
        final String queueEntryID = qe.getQueueEntryID();
        log.info("Processing queueentry "+queueEntryID);

        JDFNode node=doc.getJDFRoot();
        VJDFAttributeMap vPartMap=qe.getPartMapVector();
        JDFAttributeMap partMap=vPartMap==null ? null : vPartMap.elementAt(0);
        final String workStepID = node.getWorkStepID(partMap);
        _statusListener.setNode(queueEntryID, workStepID, node, vPartMap, null);

        VElement v=node.getResourceLinks(null);
        String inConsume=null;
        String outQuantity=null;
        if (v!=null) {
            int vSiz=v.size();
            for (int i = 0; i < vSiz; i++) {
                JDFResourceLink rl = (JDFResourceLink) v.elementAt(i);
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


    public EnumQueueEntryStatus stopProcessing(JDFQueueEntry qe,EnumQueueEntryStatus newStatus) {
        EnumQueueEntryStatus status = qe.getQueueEntryStatus();

        if (EnumQueueEntryStatus.Running.equals(status)) {
            ChangeQueueEntryStatusRequest newReq = new ChangeQueueEntryStatusRequest(qe.getQueueEntryID(),newStatus);
            _updateStatusReqs.add(newReq);
            return status;
        } else if ( EnumQueueEntryStatus.Waiting.equals(status) 
        		|| EnumQueueEntryStatus.Suspended.equals(status) ) {
            qe.setQueueEntryStatus(newStatus);
            return newStatus;
        } else {
            // cannot change status
            return status;
        }
    }

    public void shutdown() {
        _doShutdown=true;
    }

}
