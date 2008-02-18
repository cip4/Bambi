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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFRequestQueueEntryParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.omg.CORBA._PolicyStub;

public class ProxyDevice extends AbstractDevice {
	
    private static final Log log = LogFactory.getLog(ProxyDevice.class.getName());

	protected class RequestQueueEntryHandler implements IMessageHandler
    {
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            if(m==null) {
                return false;
            }
            // check for valid RequestQueueEntryParams
            JDFRequestQueueEntryParams qep = m.getRequestQueueEntryParams(0);
            if (qep==null) {
                JMFHandler.errorResponse(resp, "QueueEntryParams missing in RequestQueueEntry message", 7);
                return true;
            }
            final String queueURL=qep.getQueueURL();
            if (queueURL==null || queueURL.length()<1) {
                JMFHandler.errorResponse(resp, "QueueURL is missing", 7);
                return true;
            }

            final String deviceID=m.getSenderID();
            
            final NodeIdentifier nid = qep.getNodeIdentifier();    	
            // submit a specific QueueEntry
            IQueueEntry iqe = _theQueueProcessor.getQueueEntry(nid);
            JDFQueueEntry qe =iqe==null ? null : iqe.getQueueEntry();
            if (qe!=null && EnumQueueEntryStatus.Waiting.equals( qe.getQueueEntryStatus() )
                    && qe.getDeviceID()==null) {
                submitQueueEntry(iqe, queueURL, deviceID);
            } else {
                String qeStatus = qe==null ? "null" : qe.getQueueEntryStatus().getName();
                JMFHandler.errorResponse(resp, "requested QueueEntry is "+qeStatus, 2);
                return true;
            }
            return true;

        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies() {
            return new EnumFamily[]{EnumFamily.Command};
        }
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType() {
            return EnumType.RequestQueueEntry;
        }
    }
    protected class SubmitQueueEntryResponseHandler implements IMessageHandler
    {
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            if(m==null) {
                return false;
            }
            // check for valid RequestQueueEntryParams
            JDFRequestQueueEntryParams qep = m.getRequestQueueEntryParams(0);
            if (qep==null) {
                JMFHandler.errorResponse(resp, "QueueEntryParams missing in RequestQueueEntry message", 7);
                return true;
            }
            final String queueURL=qep.getQueueURL();
            if (queueURL==null || queueURL.length()<1) {
                JMFHandler.errorResponse(resp, "QueueURL is missing", 7);
                return true;
            }

            final String deviceID=m.getSenderID();
            
            final NodeIdentifier nid = qep.getNodeIdentifier();     
            // submit a specific QueueEntry
            IQueueEntry iqe = _theQueueProcessor.getQueueEntry(nid);
            JDFQueueEntry qe =iqe==null ? null : iqe.getQueueEntry();
            if (qe!=null && EnumQueueEntryStatus.Waiting.equals( qe.getQueueEntryStatus() )
                    && qe.getDeviceID()==null) {
                submitQueueEntry(iqe, queueURL, deviceID);
            } else {
                String qeStatus = qe==null ? "null" : qe.getQueueEntryStatus().getName();
                JMFHandler.errorResponse(resp, "requested QueueEntry is "+qeStatus, 2);
                return true;
            }
            return true;

        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies() {
            return new EnumFamily[]{EnumFamily.Response, EnumFamily.Acknowledge };
        }
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType() {
            return EnumType.SubmitQueueEntry;
        }
    }
    protected class ReturnQueueEntryHandler implements IMessageHandler
    {
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            if(m==null) {
                return false;
            }
            log.info("Handling "+m.getType());
            EnumType typ=m.getEnumType();
            JDFReturnQueueEntryParams qep = m.getReturnQueueEntryParams(0);
            if (qep==null) {
                JMFHandler.errorResponse(resp, "ReturnQueueEntryParams missing in ReturnQueueEntry message", 7);
                return true;
            }

            String outQEID=qep.getQueueEntryID();
            if (outQEID==null || outQEID.length()<1) {
                JMFHandler.errorResponse(resp, "ReturnQueueEntryParams missing QueueEntry ID", 7);
                return true;
            }

            String inQEID = _tracker.getIncomingQEID(outQEID);
            if (inQEID==null || inQEID.equals("")) {
                JMFHandler.errorResponse(resp, "QueueEntry with ID="+outQEID+" is not tracked", 2);
                return true;
            }

            JDFQueueEntry qe= _theQueue.getQueueEntry(inQEID);
            if (qe==null) {
                String errorMsg="QueueEntry with ID="+outQEID+" is missing in local"
                +" queue, but known by the QueueTracker: "+_tracker.getQueueEntryString(inQEID);
                JMFHandler.errorResponse(resp, errorMsg, 2);
                return true;
            }

            // get the returned JDFDoc from the incoming ReturnQE command and pack it in the outgoing
            JDFDoc doc = qep.getURLDoc();
            if (doc==null) {
                String errorMsg="failed to parse the JDFDoc from the incoming "
                    + "ReturnQueueEntry with QueueEntryID="+inQEID;
                JMFHandler.errorResponse(resp, errorMsg, 2);

                return true;
            }

            VString aborted = qep.getAborted();
            if (aborted!=null && aborted.size()!=0) {
                qe.setQueueEntryStatus(EnumQueueEntryStatus.Aborted);
                returnQueueEntry(qe, aborted, doc);
            } else {
                VString completed = qep.getCompleted();
                if (completed!=null && completed.size()!=0) {
                    qe.setQueueEntryStatus(EnumQueueEntryStatus.Completed);
                } 
                returnQueueEntry(qe, completed, doc);
            }

            _tracker.removeEntry(inQEID);
            persist();
            return true;

         }
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getFamilies()
         */
        public EnumFamily[] getFamilies()
        {
            return new EnumFamily[]{EnumFamily.Command};
        }
    
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#getMessageType()
         */
        public EnumType getMessageType()
        {
            return EnumType.ReturnQueueEntry;
        }
    }


    public ProxyDevice(IDeviceProperties properties) {
		super(properties);
	}

	/**
	 * returns null, since the ProxyDevice doesn't need a DeviceProcessor
	 */
	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor() {
		return null;
	}

    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * change the QueueEntryStatus of a QueueEntry
     * @param  qeid   the QueueEntryID of the QueueEntry to change status
     * @param  status the desired new status of the QueueEntry
     * @return the return code of the JMF signalling the status (0=success)
     */
    private int changeQEStatus(String qeid, EnumQueueEntryStatus status) {
    	// check if the QueueEntry has already been forwarded
    	if ( _tracker.hasIncomingQE(qeid) ) {
    		String outQeid=_tracker.getOutgoingQEID(qeid);
    		if (outQeid==null) {
    			log.error( "found no matching outgoing QueueEntry for "
    					+"incoming QueueEntry with QueueEntryID="+qeid );
    			return 105;
    		}
    		
    		JDFJMF jmf=null;
    		String cmdName=status.getName()+"QueueEntry";
    		if ( EnumQueueEntryStatus.Suspended.equals( status ) ) {
    			jmf = JMFFactory.buildSuspendQueueEntry( outQeid );
    		} else if ( EnumQueueEntryStatus.Running.equals( status ) ) {
    			jmf = JMFFactory.buildResumeQueueEntry( outQeid );
    		} else if ( EnumQueueEntryStatus.Aborted.equals( status )) {
    			jmf = JMFFactory.buildAbortQueueEntry( outQeid );
    		} else {
    			log.error( cmdName+" is not supported" );
    			return 5;
    		}
    		
    		String devUrl=_tracker.getDeviceURL(qeid);
    		if ( devUrl==null) {
    			log.error( "found no matching DeviceURL for QueueEntryID="+qeid );
    			return 105;
    		}
    		
    		JDFResponse resp=JMFFactory.send2URL(jmf,devUrl);
    		if (resp==null) {
    			log.error( "failed to forward "+cmdName+" to "+devUrl
    					+", response is null" );
    			return 120;
    		}
    		int retCode=resp.getReturnCode();
    		if (retCode!=0) {
    			log.error( "failed to forward "+cmdName+" to "+devUrl
    					+", ReturnCode="+retCode );
    			return retCode;
    		}
    	} 
    	return 0;
    }

    /**
     * submit the QueueEntry to the specified URL<br>
     * the submitted QueueEntry will have references to the root device only, so only the 
     * root will receive status updates. Then the root has to forward the updates to the origin 
     * of the QueueEntry (e. g. the MIS).
     * @param qe the QueueEntry to submit
     * @param targetURL the URL to submit the QueueEntry to
     * @return true, if successful
     */
    private void submitQueueEntry(IQueueEntry iqe, String targetURL, String deviceID)
    {
        if(iqe==null)
            return;
        JDFQueueEntry qe=iqe.getQueueEntry();
        synchronized (qe)
        {
                	        
        qe.setDeviceID( deviceID );
    	BambiNSExtension.setDeviceURL(qe,targetURL);
    	
      	JDFJMF jmf=JDFJMF.createJMF(JDFMessage.EnumFamily.Command,JDFMessage.EnumType.SubmitQueueEntry);
    	JDFCommand com = (JDFCommand)jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command, null, 0);
    	JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();
    	qsp.setURL( "dummy" );
  		qsp.setReturnJMF(_devProperties.getDeviceURL() );
    
    	JDFResponse resp = JMFFactory.send2URL(jmf, targetURL);
    	if (resp!=null && resp.getReturnCode()==0) {
    	    JDFQueueEntry newQE = resp.getQueueEntry(0);
    	    updateEntry(qe, EnumQueueEntryStatus.Running);
    	    _tracker.addEntry(qe.getQueueEntryID(), newQE.getQueueEntryID(), deviceID, targetURL);
    	    return true;
    	}
    	String respError = resp==null ? "response is null" : "ReturnCode is "+resp.getReturnCode();
    	if (resp!=null) {
    		respError += ", JDFResponse is: \r\n"+resp.getText();
    	}
    	log.error("failed to send SubmitQueueEntry, "+respError);
    	qe.setDeviceID(null);
    	return false;
        }

    }
}