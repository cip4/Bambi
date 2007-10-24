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
package org.cip4.bambi.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResourceInfo;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
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
    protected StatusCounter theCounter;
    
    /**
     * 
     * handler for the StopPersistentChannel command
     */
    public class StatusHandler implements IMessageHandler
    {

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
        {
            if(!EnumFamily.Query.equals(inputMessage.getFamily()))
                return false;
            
            JDFDoc docJMF=theCounter.getDocJMFPhaseTime();
            if(docJMF==null) {
                log.warn("StatusHandler.handleMessage: StatusCounter-phasetime = null");
                return false;
            }
            // TODO change interface to public int handleMessage(JDFMessage inputMessage, JDFJMF responses)
            JDFResponse r=docJMF.getJMFRoot().getResponse(-1);
            if(r==null) {
                log.error("StatusHandler.handleMessage: StatusCounter response = null");
                return false;
            }
            try {
            	// JDFDevice d = r.getDeviceInfo(0).getDevice();
            	// TODO insert more Bambi info from properties file
            } catch (NullPointerException e) {
            	log.error("failed to insert further info in Status response");
            }
            response.mergeElement(r, false);
            return true;
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
        public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
        {
        	if(inputMessage==null || response==null) {
	            return false;
	        }
        	
            if(!EnumFamily.Query.equals(inputMessage.getFamily())) {
                return false;
            }

            
            JDFResourceInfo ri = response.appendResourceInfo();            
            StatusCounter sc=theCounter;
            //TODO richtiges element kopieren (nicht ein jmf in ein ri hinein...
            try {
            	ri.copyElement( sc.getDocJMFResource().getJMFRoot(),null );
            } catch (NullPointerException ex) {
            	log.error("hit an npe while trying to add resources: "+ex);
            }
            return true;
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
    
    public StatusListener(ISignalDispatcher dispatch, String deviceID)
    {
        dispatcher=dispatch;
        theCounter=new StatusCounter(null,null,null);
        theCounter.setDeviceID(deviceID);
    }
    
    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#signalStatus(java.lang.String, java.lang.String, org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus, java.lang.String, org.cip4.jdflib.core.JDFElement.EnumNodeStatus, java.lang.String)
     */
    public void signalStatus(EnumDeviceStatus deviceStatus, String deviceStatusDetails, EnumNodeStatus nodeStatus,
            String nodeStatusDetails)
    {
       StatusCounter su=theCounter;
       if(su==null) {
           log.error("updating null status tracker");
           return;
       }
       boolean bMod=su.setPhase(nodeStatus, nodeStatusDetails, deviceStatus, deviceStatusDetails);
       if(bMod) {
           dispatcher.triggerQueueEntry(su.getQueueEntryID(), su.getWorkStepID(), -1);
       }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#updateAmount(java.lang.String, java.lang.String, java.lang.String, double, double)
     */
    public void updateAmount(String resID, double good, double waste)
    {
        StatusCounter su=theCounter;
        if(su==null)
            return;
        su.addPhase(resID, good, waste);
        if(good>0) {
            dispatcher.triggerQueueEntry(su.getQueueEntryID(), su.getWorkStepID(), (int)good);
        }

    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IStatusListener#setNode(java.lang.String, org.cip4.jdflib.node.JDFNode)
     */
    public void setNode(String queueEntryID, String workStepID, JDFNode node, VJDFAttributeMap vPartMap, String trackResourceID)
    {       
        String oldQEID=theCounter.getQueueEntryID();
        if(!KElement.isWildCard(oldQEID))
        {
            log.info("removing subscription for: "+oldQEID);
            dispatcher.removeSubScriptions(oldQEID);
        }
        theCounter.setActiveNode(node, vPartMap, null);
        theCounter.setFirstRefID(trackResourceID);
        theCounter.setQueueEntryID(queueEntryID);
        theCounter.setWorkStepID(workStepID);
        if(node!=null) {
            log.info("adding subscription for: "+queueEntryID);
            dispatcher.addSubscriptions(node,queueEntryID);
        }
    }

    /**
     * @param jmfHandler
     */
    public void addHandlers(IJMFHandler jmfHandler)
    {
        jmfHandler.addHandler(this.new ResourceHandler());        
        jmfHandler.addHandler(this.new StatusHandler());        
    }
    
    
	public EnumDeviceStatus getDeviceStatus() {
		JDFDoc docJMF=null;
		try {
			docJMF=theCounter.getDocJMFPhaseTime();
			JDFResponse r=docJMF.getJMFRoot().getResponse(0);
			return r.getDeviceInfo(-1).getDeviceStatus();
		} catch (NullPointerException e) {
			log.fatal("StatusCounter returned an illegal doc: \r\n"+docJMF);
			return EnumDeviceStatus.Unknown;
		}
	}

}
