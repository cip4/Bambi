/**
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
 * copyright (c) 1999-2006, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.core;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.StatusCounter;

/**
 * @author prosirai
 *
 */
public class StatusListener implements IStatusListener
{

    private static Log log = LogFactory.getLog(StatusListener.class.getName());
    private ISignalDispatcher dispatcher;
    private ISignalDispatcher rootDispatcher=null;
    protected StatusCounter theCounter;
    private JDFNode currentNode=null;
    private long lastSave = 0;
    private Vector<JDFDoc> queuedStatus=new Vector<JDFDoc>();
    private Vector<JDFDoc> queuedResource=new Vector<JDFDoc>();

    /**
     * 
     * @param dispatch
     * @param deviceID
     */
    public StatusListener(ISignalDispatcher dispatch, String deviceID)
    {
        dispatcher=dispatch;
        theCounter=new StatusCounter(null,null,null);
        theCounter.setDeviceID(deviceID);
    }

    public void flush()
    {
        int qsize = queuedStatus.size();
        if(qsize>0)
        {
            dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getWorkStepID(), -1);
            dispatcher.flush();
            if(rootDispatcher!=null)
            {
                rootDispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getWorkStepID(), -1);
                rootDispatcher.flush();
            }
            
            StatusCounter.sleep(100);
            qsize = queuedStatus.size();           
        }

    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#signalStatus(java.lang.String, java.lang.String, org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus, java.lang.String, org.cip4.jdflib.core.JDFElement.EnumNodeStatus, java.lang.String)
     */
    public void signalStatus(EnumDeviceStatus deviceStatus, String deviceStatusDetails, EnumNodeStatus nodeStatus,String nodeStatusDetails, boolean forceOut)
    {
        if(theCounter==null) {
            log.error("updating null status tracker");
            return;
        }
        boolean bMod=theCounter.setPhase(nodeStatus, nodeStatusDetails, deviceStatus, deviceStatusDetails);
        if(bMod || forceOut) {
            final JDFDoc docJMFPhaseTime = theCounter.getDocJMFPhaseTime();
            JDFJMF root=docJMFPhaseTime.getJMFRoot();
            if(root.numChildElements(ElementName.RESPONSE, null)>1)
            {
                JDFDoc doc2=new JDFDoc("JMF");
                final KElement root2 = doc2.getRoot();
                root2.mergeElement(root, false);
                root.removeChild(ElementName.RESPONSE, null, 1);
                root2.removeChild(ElementName.RESPONSE, null, 0);

                queuedStatus.add(docJMFPhaseTime);
                queuedStatus.add(doc2);
                StatusCounter.sleep(10);
            }
            flush();
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#updateAmount(java.lang.String, java.lang.String, java.lang.String, double, double)
     */
    public void updateAmount(String resID, double good, double waste)
    {
        if(theCounter==null)
            return;
        theCounter.addPhase(resID, good, waste);
        if(good>0) {
            dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getWorkStepID(), (int)good);
        }
        if(System.currentTimeMillis()-lastSave>3000)
            saveJDF();
    }

    /**
     * replace the currently tracked node with node
     * used to overwrite the current node with a returned node, e.g from a proxy device
     * @param node the JDFNode used to overwrotie the local JDF node
     */
    public void replaceNode(JDFNode node)
    {
        if(node!=null)
        {
            String location=currentNode==null ? null : currentNode.getOwnerDocument_JDFElement().getOriginalFileName();
            currentNode=node;
            if(location!=null)
                currentNode.getOwnerDocument_JDFElement().setOriginalFileName(location);
            saveJDF();
        }
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#setNode(java.lang.String, org.cip4.jdflib.node.JDFNode)
     */
    public void setNode(String queueEntryID, String workStepID, JDFNode node, VJDFAttributeMap vPartMap, String trackResourceID)
    {       
        String oldQEID=theCounter.getQueueEntryID();
        theCounter.writeAll(); // write all stuff in the counter to the node
        saveJDF();
        boolean bSame=currentNode==node;
        currentNode=node;
        if(!bSame )
        {
            saveJDF();
        }

        if(!KElement.isWildCard(oldQEID))
        {
            log.info("removing subscription for: "+oldQEID);
            dispatcher.removeSubScriptions(oldQEID,"*");
        }
        theCounter.setActiveNode(node, vPartMap, null);
        theCounter.setFirstRefID(trackResourceID);
        theCounter.setTrackWaste(trackResourceID, true); // always track waste
        theCounter.setQueueEntryID(queueEntryID);
        theCounter.setWorkStepID(workStepID);
        while(node!=null) {
            log.info("adding subscription for: "+queueEntryID);
            dispatcher.addSubscriptions(node,queueEntryID);
            node=node.getParentJDF();
        }
    }

    /**
     *  save the currently active jdf
     */
    private void saveJDF()
    {
        if(currentNode==null)
            return;
        final JDFDoc ownerDoc = currentNode.getOwnerDocument_JDFElement();
        if(ownerDoc!=null && ownerDoc.getOriginalFileName()!=null)
        {
            ownerDoc.write2File((String)null, 0, true);
            lastSave=System.currentTimeMillis();
        }
    }


    /**
     * get the device status
     * @return the device status. <br/>
     * Returns EnumDeviceStatus.Idle if the StatusCounter is null. <br/>
     * Returns EnumDeviceStatus.Unknown, if the StatusListener was unable to retrieve the status from the StatusCounter.
     */
    public EnumDeviceStatus getDeviceStatus() {
        if (theCounter==null) {
            return EnumDeviceStatus.Idle;
        }

        JDFDoc docJMF=theCounter.getDocJMFPhaseTime();
        JDFResponse r=docJMF==null ? null : docJMF.getJMFRoot().getResponse(-1);
        JDFDeviceInfo di=r==null ? null : r.getDeviceInfo(0);
        return di==null ? EnumDeviceStatus.Idle : di.getDeviceStatus();

    }

    public void shutdown() {
        // not needed right now, retaining method for future compatability		
    }

    /**
     * get the StatusCounter
     * @return the StatusCounter
     */
    public StatusCounter getStatusCounter() {
        return theCounter;
    }
    public JDFDoc getJMFPhaseTime()
    {
        return (queuedStatus.size()==0) ? theCounter.getDocJMFPhaseTime(): queuedStatus.remove(0);
    }

    @Override
    public String toString()
    {
        return "[StatusListner - counter: "+theCounter+"\n Current Node: "+currentNode;
    }

    /**
     * @param inputMessage
     * @return
     */
    public boolean matchesQuery(JDFMessage inputMessage)
    {
        if(inputMessage==null)
            return false;
        if(!(inputMessage instanceof JDFQuery))
            return false;
        JDFQuery q=(JDFQuery)inputMessage;
        if(EnumType.Status.equals(q.getEnumType()))
        {
            JDFStatusQuParams sqp=q.getStatusQuParams();
            if(sqp==null)
                return true; 
            return matchesIDs(sqp.getJobID(),sqp.getJobPartID(),sqp.getQueueEntryID());            
        }
        else if(EnumType.Resource.equals(q.getEnumType()))
        {
            JDFResourceQuParams rqp=q.getResourceQuParams();
            if(rqp==null)
                return true;
            return matchesIDs(rqp.getJobID(),rqp.getJobPartID(),rqp.getQueueEntryID());            
        }
        return true;
    }
    /**
     * @param q
     * @return
     */
    private boolean matchesIDs(String jobID, String jobPartID, String queueEntryID)
    {
        String id2 = currentNode==null ? null : currentNode.getJobID(true);
//        if(!KElement.isWildCard(jobID)&&!jobID.equals(id2))
//            return false;
//        id2 = currentNode==null ? null : currentNode.getJobPartID(false);
//        if(!KElement.isWildCard(jobPartID)&&!id2.startsWith(jobPartID)) // assume dot notation
//            return false;
        id2 = currentNode==null ? null : theCounter.getQueueEntryID();
        if(!KElement.isWildCard(jobPartID)&&!jobPartID.equals(jobPartID)) 
            return false;

        return true;
    }

    public void setRootDispatcher(ISignalDispatcher _rootDispatcher)
    {
        this.rootDispatcher = _rootDispatcher;
    }

}
