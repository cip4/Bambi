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

package org.cip4.bambi.proxy;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IStatusListener;
import org.cip4.bambi.core.StatusListener;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.IQueueProcessor;
import org.cip4.bambi.proxy.ProxyDevice.EnumSlaveStatus;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author prosirai
 */
public class ProxyDeviceProcessor extends AbstractDeviceProcessor
{
    private static Log log = LogFactory.getLog(ProxyDeviceProcessor.class);
    private static final long serialVersionUID = -384123582645081254L;
    private IConverterCallback callBack;
    private QueueEntryStatusContainer qsc;
    private String _slaveURL;


    /**
     * this baby handles all status updates that are stored in the queuentry
     * 
     * @author prosirai
     *
     */
    protected class QueueEntryStatusContainer
    {
        protected JDFQueueEntry qe;
        protected KElement theContainer;
        protected JDFNode theNode;
        protected JDFJobPhase jp;

        public QueueEntryStatusContainer()
        {
            qe=currentQE.getQueueEntry();
            theNode=currentQE.getJDF();
            theContainer=qe.getCreateElement(BambiNSExtension.MY_NS_PREFIX+"StatusContainer", BambiNSExtension.MY_NS,0);
            jp=(JDFJobPhase) theContainer.appendElement(ElementName.JOBPHASE);
            jp.setStatus(EnumNodeStatus.Waiting); //TODO evaluate qe
        }

        /**
         * return true if this processor is responsible for processing a given queuentry as specified by qe
         * 
         * @param qe the queuentry
         * @return true if we are processing qe
         */
        public boolean matchesQueueEntry(JDFQueueEntry _qe)
        {
            return _qe!=null &&  qe!=null && qe.getQueueEntryID().equals(_qe.getQueueEntryID());
        }

        /**
         * return true if this processor is responsible for processing a given node as specified by ni
         * 
         * @param ni the node identifier (jobid, jobpartid, part*)
         * @return true if we are processing ni
         */
        public boolean matchesQueueEntry(NodeIdentifier ni)
        {
            return qe.matchesNodeIdentifier(ni);
        }

        /**
         * @param jobPhase
         */
        private void applyPhase(JDFJobPhase jobPhase)
        {           
            double deltaWaste=jobPhase.getWasteDifference(jp);
            double deltaAmount=jobPhase.getAmountDifference(jp);
            final IStatusListener statusListener = getStatusListener();
            final JDFDeviceInfo devInfo=(JDFDeviceInfo) jobPhase.getParentNode();
            statusListener.updateAmount(_trackResource, deltaAmount, deltaWaste);
            statusListener.signalStatus(devInfo.getDeviceStatus(), devInfo.getStatusDetails(), jobPhase.getStatus(), jobPhase.getStatusDetails(),false);

            jp=(JDFJobPhase) jp.replaceElement(jobPhase);
        }

        /**
         * @return
         */
        public String getSlaveQEID()
        {
            return theContainer.getAttribute("SlaveQueueEntryID", null, null);
        }

        /**
         * @param slaveQEID
         * 
         */
        public void setSlaveQEID(String slaveQEID)
        {
            theContainer.setAttribute("SlaveQueueEntryID", slaveQEID);
        }

        /**
         * 
         */
        public void delete()
        {
            if(theContainer!=null)
                theContainer.deleteNode();           
        }
    }

    // TODO - make resphandler
    protected class SubmitQueueEntryResponseHandler extends AbstractHandler
    {

        /**
         * @param _type
         * @param _families
         */
        public SubmitQueueEntryResponseHandler()
        {
            super(EnumType.SubmitQueueEntry, new EnumFamily[]{EnumFamily.Acknowledge, EnumFamily.Response});
            // TODO Auto-generated constructor stub
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            if(m==null) {
                return false;
            }
            int rc=m.getReturnCode();
            if(rc!=0)
            {
                //
            }

            return true;

        }

    }

    /**
     * constructor
     * @param queueProcessor points to the QueueProcessor
     * @param statusListener points to the StatusListener
     * @param _callBack      the converter call back too and from device
     * @param device         the parent device that this processor does processing for
     * @param qeToProcess   the queueentry that this processor will be working for
     * @param doc 
     */
    public ProxyDeviceProcessor(ProxyDevice device,  IQueueProcessor qProc, IQueueEntry qeToProcess, String slaveURL)
    {
        super();
        _statusListener=new StatusListener(device.getSignalDispatcher(),device.getDeviceID());
        _parent=device;
        currentQE=qeToProcess;
        qsc=this.new QueueEntryStatusContainer();
        callBack=device.getCallback();
        
        init(qProc, _statusListener, _parent.getProperties());

        if(slaveURL==null)
            slaveURL = _parent.getProperties().getSlaveURL();
        _slaveURL=slaveURL;

        URL qURL;
        try
        {
            qURL = slaveURL==null ? null : new URL(slaveURL);
        }
        catch (MalformedURLException x)
        {
            qURL=null;
        }
        EnumQueueEntryStatus qes=null;
        if(qURL!=null)
        {
            qes= submitToQueue(qURL);
        }
        else
        {
            File hf=_parent.getProperties().getSlaveInputHF();
            if(hf!=null)
                qes=submitToHF(hf);
        }

        if(qes==null || EnumQueueEntryStatus.Aborted.equals(qes))
        {
            // TODO handle errors
            log.error("submitting queueentry unsuccessful: "+qeToProcess.getQueueEntryID());
            shutdown();
        }       
    }

    public EnumQueueEntryStatus processDoc(JDFNode nod, JDFQueueEntry qe) 
    {
        throw new NotImplementedException();
    }

    /**
     * @param hfURL
     * @param qe
     * @return
     */
    private EnumQueueEntryStatus submitToHF(File fHF)
    {
        JDFQueueEntry qe=currentQE.getQueueEntry();
        JDFNode nod=currentQE.getJDF();
        if(nod==null)
        {
            // TODO abort!
            log.error("submitToQueue - no JDFDoc at: "+BambiNSExtension.getDocURL(qe));
            _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
        }
        else
        {
            File fLoc=new File(((ProxyDevice)_parent).getNameFromQE(qe));
            final File fileInHF = FileUtil.getFileInDirectory(fHF, fLoc);
            boolean bWritten=nod.getOwnerDocument_JDFElement().write2File(fileInHF,0,true);
            if(bWritten)
            {
                submitted(qe, nod, "qe"+JDFElement.uniqueID(0), EnumQueueEntryStatus.Running, UrlUtil.fileToUrl(fileInHF, true));            
            }
            else
            {
                log.error("Could not write File: "+fLoc+" to "+fHF);
                _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
            }
        }
        return qe.getQueueEntryStatus();     
    }

    /**
     * @param qurl
     * @param qe
     * @return
     * TODO mime or not mime...
     */
    private EnumQueueEntryStatus submitToQueue(URL qurl)
    {
        JDFJMF jmf=JDFJMF.createJMF(JDFMessage.EnumFamily.Command,JDFMessage.EnumType.SubmitQueueEntry);
        JDFCommand com = (JDFCommand)jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command, null, 0);
        JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();

        qsp.setReturnJMF(_parent.getDeviceURL());
        final File deviceOutputHF = _parent.getProperties().getSlaveOutputHF();
        if(deviceOutputHF!=null)
        {
            qsp.setReturnURL(deviceOutputHF.getPath());
        }

        JDFNode nod =currentQE.getJDF(); 
        JDFQueueEntry qe=currentQE.getQueueEntry();
        qsp.setURL("dummy"); // replaced by mimeutil
        if(nod!=null)
        {
            try
            {
                final String urlString = qurl.toExternalForm();
                MIMEDetails ud=new MIMEDetails();
                ud.httpDetails.chunkSize=_devProperties.getDeviceHTTPChunk();
                JDFDoc d=MimeUtil.writeToQueue(jmf.getOwnerDocument_JDFElement(), nod.getOwnerDocument_JDFElement(), urlString,ud);
                if(d!=null)
                {
                    JDFJMF jmfResp=d.getJMFRoot();
                    if(jmfResp==null)
                    {
                        d=null;
                    }
                    else
                    {
                        JDFResponse r=jmfResp.getResponse(0);
                        if(r==null)
                        {
                            d=null;
                        }
                        else
                        {
                            if(r.getReturnCode()!=0 || !EnumType.SubmitQueueEntry.equals(r.getEnumType()))
                            {
                                log.error("Device returned rc="+r.getReturnCode());
                                _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
                            }
                            else
                            {
                                JDFQueueEntry qeR=r.getQueueEntry(0);

                                if(qeR!=null)
                                {
                                    submitted(qe, nod, qeR.getQueueEntryID(),qeR.getQueueEntryStatus(),urlString);
                                }
                                else
                                {
                                    log.error("No QueueEntry in the submitqueuentry response");
                                }
                            }
                        }
                    }
                }
                if(d==null)
                {
                    log.error("submitToQueue - no response at: "+BambiNSExtension.getDocURL(qe));
                    _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
                }
            }
            catch (IOException x)
            {
                nod=null;
            }
            catch (MessagingException x)
            {
                nod=null;                
            }
        }
        if(nod==null)
        {
            log.error("submitToQueue - no JDFDoc at: "+BambiNSExtension.getDocURL(qe));
            _queueProcessor.updateEntry(qe, EnumQueueEntryStatus.Aborted,null,null);
        }
        return qe.getQueueEntryStatus();
    }

    /**
     * @param qe
     * @param node
     * @param qeR
     */
    private void submitted(JDFQueueEntry qe, JDFNode node,  String devQEID, EnumQueueEntryStatus newStatus, String slaveURL)
    {
        qsc.setSlaveQEID(devQEID);
        BambiNSExtension.setDeviceURL(qe, slaveURL);
        _queueProcessor.updateEntry(qe, newStatus ,null,null);
        _statusListener.setNode(qe.getQueueEntryID(), node.getJobPartID(false), node, node.getPartMapVector(), null);
        _statusListener.signalStatus(EnumDeviceStatus.Running, "Submitted", EnumNodeStatus.InProgress, "Submitted",false);
        createSubscriptionsForQE(slaveURL,devQEID);
    }

    /**
     * 
     */
    private void createSubscriptionsForQE(String slaveURL, String devQEID)
    {
        if(!UrlUtil.isHttp(slaveURL))
            return;
        if(!EnumSlaveStatus.JMF.equals(getParent().getSlaveStatus()))
            return;

        JDFJMF jmf=JMFFactory.buildStatusSubscription(_parent.getDeviceURL(), 10.,0);
        new JMFFactory(callBack).send2URL(jmf, slaveURL, null, _devProperties.getDeviceID()); // TODO handle response        
    }

    /**
     * @return
     */
    private ProxyDevice getParent()
    {
        return (ProxyDevice)_parent;
    }

    /**
     * @return
     */
    private JDFQueueEntry getQueueEntry()
    {
        return qsc.qe;
    }

    /**
     * @return the internal jobphase for this processor
     */
    public JDFJobPhase getJobPhase()
    {
        return qsc.jp;
    }

    /**
     * @param qe
     * @return
     */
    public boolean matchesQueueEntry(JDFQueueEntry qe)
    {
        return qsc.matchesQueueEntry(qe);
    }  
    /**
     * @param qe
     * @return
     */
    public boolean matchesNode(NodeIdentifier ni)
    {
        return qsc.matchesQueueEntry(ni);
    }


    @Override
    public void init(IQueueProcessor queueProcessor, StatusListener statusListener, IDeviceProperties devProperties)
    {

        super.init(queueProcessor, statusListener,devProperties);
        JDFQueueEntry qe=getQueueEntry();
        log.info("processQueueEntry queuentryID="+qe.getQueueEntryID());
        JDFNode nod=getJDFNode();

        createNISubscriptions(nod);
        if(callBack!=null)
            callBack.prepareJDFForBambi(nod.getOwnerDocument_JDFElement());

    }

    /**
     * @param doc the doc to add a subscription to
     * @return String the channelID of the newly created subscription, null if none was created
     */
    private String createNISubscriptions(JDFNode root)
    {
        if(root==null)
            return null;
        if(!EnumSlaveStatus.NODEINFO.equals(getParent().getSlaveStatus()))
            return null;
        
        JDFNodeInfo ni=root.getCreateNodeInfo();
        final String deviceURL = _parent.getDeviceURL();
        final JDFJMF jmf=JMFFactory.buildStatusSubscription(deviceURL, 10, 100);
        ni.copyElement(jmf,null);
        log.info("creating subscription for doc:"+root.getJobID(true)+" - "+root.getJobPartID(false)+ " to "+deviceURL);

        return jmf.getQuery(0).getSubscription().getID();
    }

    /**
     * @return
     */
    JDFNode getJDFNode()
    {
        return qsc.theNode;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        ((ProxyDevice)_parent).removeProcessor(this);
        log.info("shutting down devcondeviceprocessor");
    }


    /**
     * updates the current status based on the data in the input status signal or response
     * @param jobPhase the jobphase containing the status information
     */
    private boolean handleStatusUpdate(JDFJobPhase jobPhase)
    {
        NodeIdentifier ni=jobPhase.getIdentifier();
        JDFNode n=currentQE.getJDF().getJDFRoot();
        VElement v=n.getMatchingNodes(ni);
      
        if(v==null)
            return false;
    
        qsc.applyPhase(jobPhase);
        _queueProcessor.updateEntry(qsc.qe, jobPhase.getQueueEntryStatus(),null,null);
        return true;
    
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.AbstractDeviceProcessor#stopProcessing(org.cip4.jdflib.core.JDFElement.EnumNodeStatus)
     */
    @Override
    public void stopProcessing(EnumNodeStatus newStatus)
    {
        JDFJMF jmf=null;
        String slaveQE=getSlaveQEID();
        if(EnumNodeStatus.Aborted.equals(newStatus))
            jmf=JMFFactory.buildAbortQueueEntry(slaveQE);
        else if(EnumNodeStatus.Suspended.equals(newStatus))
            jmf=JMFFactory.buildSuspendQueueEntry(slaveQE);
        if(jmf!=null)
        {
            new JMFFactory(callBack).send2URL(jmf, slaveQE, null,getParent().getDeviceID());
        }
    }

    /**
     * @param m
     * @param resp
     * @return
     */
    public boolean returnFromSlave(JDFMessage m, JDFResponse resp)
    {
        JDFReturnQueueEntryParams retQEParams = m.getReturnQueueEntryParams(0);

        // get the returned JDFDoc from the incoming ReturnQE command and pack it in the outgoing
        JDFDoc doc = retQEParams.getURLDoc();
        if (doc==null) {
            String errorMsg="failed to parse the JDFDoc from the incoming "
                + "ReturnQueueEntry with QueueEntryID="+currentQE.getQueueEntryID();
            JMFHandler.errorResponse(resp, errorMsg, 2);

            return true;
        }
        final JDFQueueEntry qe=currentQE.getQueueEntry();
        // brutally overwrite the current node with this
        _statusListener.replaceNode(doc.getJDFRoot());            

        BambiNSExtension.setDeviceURL(qe, null);

        VString aborted = retQEParams.getAborted();
        if (aborted!=null && aborted.size()!=0) {
            finalizeProcessDoc(EnumQueueEntryStatus.Aborted);
        } else {
            finalizeProcessDoc(EnumQueueEntryStatus.Completed);
        }
 
        return true;
    }

    /**
     * @return the QueuentryID as submitted to the slave device
     */
    public String getSlaveQEID()
    {
        // TODO Auto-generated method stub
        return qsc.getSlaveQEID();
    }

    @Override
    protected boolean finalizeProcessDoc(EnumQueueEntryStatus qes)
    {
        boolean b= super.finalizeProcessDoc(qes);
        qsc.delete();
        shutdown(); // remove ourselves ot of the processors list
        return b;
    }

    /**
     * @param m
     * @param resp
     * @return
     */
    public boolean handleStatusSignal(JDFMessage m, JDFResponse resp)
    {
        if(m==null || currentQE==null)
            return false;
        
        JDFStatusQuParams sqp = m.getStatusQuParams();
        NodeIdentifier ni=null;
        if(sqp!=null)
        {
            String qeid=sqp.getQueueEntryID();
            boolean matches=KElement.isWildCard(qeid) || getSlaveQEID().equals(qeid);
            if(!matches)
                return false;
            ni=sqp.getIdentifier();
        }
        VElement vMatch=currentQE.getJDF().getJDFRoot().getMatchingNodes(ni);
        if(vMatch==null)
            return false;
        VElement devicInfos=m.getChildElementVector(ElementName.DEVICEINFO, null);
        if(devicInfos==null)
            return false;
        boolean b=false;
        for(int i=0;i<devicInfos.size();i++)
        {
            b= handleDeviceInfo((JDFDeviceInfo)devicInfos.get(i)) || b;
        }
        
        return b;    
    }

    /**
     * @param info
     * @param match
     */
    private boolean handleDeviceInfo(JDFDeviceInfo info)
    {
        String slaveDevice=getSlaveDeviceID();
        if(!info.getDeviceID().equals(slaveDevice))
            return false;
        VElement jobPhases=info.getChildElementVector(ElementName.JOBPHASE, null);
        if(jobPhases==null)
            return false;
        boolean b=false;
        for(int i=0;i<jobPhases.size();i++)
            b=handleStatusUpdate((JDFJobPhase) jobPhases.get(i)) || b;
        return b;
    }

    /**
     * @return
     */
    private String getSlaveDeviceID()
    {
       return currentQE.getQueueEntry().getDeviceID();
    }

 

}