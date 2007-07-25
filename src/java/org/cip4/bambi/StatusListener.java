/**
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
 * copyright (c) 1999-2006, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StatusCounter;

/**
 * @author prosirai
 *
 */
public class StatusListener implements IStatusListener
{

    private static Log log = LogFactory.getLog(StatusListener.class.getName());
    private ISignalDispatcher dispatcher;
    private StatusCounter theCounter;
    protected String activeQueueEntryID;
    protected String activeWorkStepID;
    
    /**
     * 
     * handler for the StopPersistentChannel command
     */
    public class StatusHandler implements IMessageHandler
    {

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response, String queueEntryID, String workstepID)
        {
            if(!EnumFamily.Query.equals(inputMessage.getFamily()))
                return false;
            
            StatusCounter sc=getStatusCounter(queueEntryID, workstepID);
            if(sc==null)
            {
                if( queueEntryID!=null)
                {
                    response.setErrorText("No matching queuentry found");
                    log.error("No matching queuentry found");
                    return true;
                }
                response.appendDeviceInfo().setDeviceStatus(EnumDeviceStatus.Idle);
                return true;
            }
            JDFDoc doc=sc.getDocJMFPhaseTime();
//TODO continue here
            return false;
        }



        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies()
        {
            return new EnumFamily[]{EnumFamily.Query};
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType()
        {
            return EnumType.Status;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * 
     * handler for the StopPersistentChannel command
     */
    public class ResourceHandler implements IMessageHandler
    {

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response, String queueEntryID, String workstepID)
        {
            if(!EnumFamily.Query.equals(inputMessage.getFamily()))
                return false;

            StatusCounter sc=getStatusCounter(queueEntryID, workstepID);
            //TODO handle resource query

            return false;
        }



        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies()
        {
            return new EnumFamily[]{EnumFamily.Query};
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType()
        {
            return EnumType.Resource;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////
    
    public StatusListener(ISignalDispatcher dispatch)
    {
        dispatcher=dispatch;
        theCounter=null;
       
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#signalStatus(java.lang.String, java.lang.String, org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus, java.lang.String, org.cip4.jdflib.core.JDFElement.EnumNodeStatus, java.lang.String)
     */
    public void signalStatus(String queueEntryID, String workstepID, EnumDeviceStatus deviceStatus,
            String deviceStatusDetails, EnumNodeStatus nodeStatus, String nodeStatusDetails)
    {
       StatusCounter su=getStatusCounter(queueEntryID,workstepID);
       if(su==null)
       {
           log.error("updating null status tracker: "+queueEntryID+" "+workstepID);
           return;
       }
       boolean bMod=su.setPhase(nodeStatus, nodeStatusDetails, deviceStatus, deviceStatusDetails);
       if(bMod)
           dispatcher.triggerQueueEntry(queueEntryID, workstepID, -1);
    }

    /**
     * @param queueEntryID
     * @param workstepID
     * @return
     */
    private StatusCounter getStatusCounter(String queueEntryID, String workstepID)
    {
        if(!ContainerUtil.equals(queueEntryID,activeQueueEntryID))
            return null;
        if(!ContainerUtil.equals(workstepID,activeWorkStepID))
            return null;
        return theCounter;
    }
    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#updateAmount(java.lang.String, java.lang.String, java.lang.String, double, double)
     */
    public void updateAmount(String queueEntryID, String workstepID, String resID, double good, double waste)
    {
        StatusCounter su=getStatusCounter(queueEntryID, workstepID);
        if(su==null)
            return;
        su.addPhase(resID, good, waste);
        if(good>0)
            dispatcher.triggerQueueEntry(queueEntryID, workstepID, (int)good);

    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#setNode(java.lang.String, org.cip4.jdflib.node.JDFNode)
     */
    public void setNode(String queueEntryID, String workStepID, JDFNode node, VJDFAttributeMap vPartMap, String trackResourceID)
    {       
        theCounter.setActiveNode(node, vPartMap, null);
        theCounter.setFirstRefID(trackResourceID);
        activeQueueEntryID=queueEntryID;
        activeWorkStepID=workStepID;
        theCounter.setQueueEntryID(queueEntryID);
        theCounter.setWorkStepID(workStepID);
    }

    /**
     * @param jmfHandler
     */
    public void addHandlers(IJMFHandler jmfHandler)
    {
        jmfHandler.addHandler(this.new ResourceHandler());        
        jmfHandler.addHandler(this.new StatusHandler());        
    }


}
