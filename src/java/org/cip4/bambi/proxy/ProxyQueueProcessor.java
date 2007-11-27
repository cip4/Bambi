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
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.queues.AbstractQueueProcessor;
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
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 *
 * QueueProcessor for the Bambi root device
 * @author niels
 *
 *
 */
public class ProxyQueueProcessor extends AbstractQueueProcessor
{	
	
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
	        EnumType typ=m.getEnumType();
            //log.info("Handling "+m.getType());
	        if(EnumType.RequestQueueEntry.equals(typ)) {
	        	// check for valid RequestQueueEntryParams
	        	JDFRequestQueueEntryParams qep = m.getRequestQueueEntryParams(0);
	        	if (qep==null) {
	        		log.error("QueueEntryParams missing in RequestQueueEntry message");
	        		return false;
	        	}
                final String queueURL=qep.getQueueURL();
	        	if (queueURL==null || queueURL.length()<1) {
	        		log.error("queueURL is missing");
	        		return false;
	        	}
	        	
                final String deviceID=BambiNSExtension.getDeviceID( qep );
	        	final String queueEntryID = qep.getJobID();        	
	        	if (queueEntryID!=null && queueEntryID.length()>0) {
	        		// submit a specific QueueEntry
	        		JDFQueueEntry qe = _theQueue.getQueueEntry(queueEntryID);
	        		if (qe!=null && EnumQueueEntryStatus.Waiting.equals( qe.getQueueEntryStatus() )
	        				&& qe.getDeviceID()==null) {
	        			// mark QueueEntry as "Running" before submitting, so it won't be
	        			// submitted twice. If SubmitQE fails, mark it as "Waiting" so other workers
	        			// can grab it.
	        			qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
	        			boolean submitted=submitQueueEntry(qe, queueURL, deviceID);
	        			if (!submitted) {
	        				qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
	        			}
	        		} else {
	        			String qeStatus = qe==null ? "null" : qe.getQueueEntryStatus().getName();
	        			log.error("requested QueueEntry is "+qeStatus);
	        			return true;
	        		}
	        	} else {
	        		// submit the next QueueEntry available
	        		IQueueEntry qe = getNextEntry();
	        		if (qe!=null) {
	        			submitQueueEntry( qe.getQueueEntry(),qep.getQueueURL(), deviceID );
	        		} 
	        		return true;
	        	} 
	        }
	        
	        // unable to handle request
	        return false;
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
	        if(EnumType.ReturnQueueEntry.equals(typ))
	        {
	        	JDFReturnQueueEntryParams qep = m.getReturnQueueEntryParams(0);
	        	if (qep==null) {
	        		log.error("ReturnQueueEntryParams missing in ReturnQueueEntry message");
	        		return false;
	        	}
	        	
	        	String outQEID=qep.getQueueEntryID();
	        	if (outQEID==null || outQEID.length()<1) {
	        		log.error("ReturnQueueEntryParams is missing QueueEntry ID ");
	        		return false;
	        	}
	        	
	        	String inQEID = _tracker.getIncomingQEID(outQEID);
	        	if (inQEID==null || inQEID.equals("")) {
	        		log.error("QueueEntry with ID="+outQEID+" is not tracked");
	        		return false;
	        	}
	        	
	        	JDFQueueEntry qe= _theQueue.getQueueEntry(inQEID);
	        	if (qe==null) {
	        		log.error("QueueEntry with ID="+outQEID+" is missing in local"
	        				+" queue, but known by the QueueTracker: "+_tracker.getQueueEntryString(inQEID));
	        		_tracker.removeEntry(inQEID);
	        		return false;
	        	}
	        	
	        	// get the returned JDFDoc from the incoming ReturnQE command and pack it in the outgoing
	        	JDFDoc doc = qep.getURLDoc();
	        	if (doc==null) {
	        		log.error("failed to parse the JDFDoc from the incoming "
	        				+ "ReturnQueueEntry with QueueEntryID="+inQEID);
	        		return false;
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
	        
	        // unable to handle request
	        return false;
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

	protected static final Log log = LogFactory.getLog(ProxyQueueProcessor.class.getName());
	protected IQueueEntryTracker _tracker=null;
	
	public ProxyQueueProcessor(AbstractDevice theParent) {
		super(theParent);
		_configDir=theParent.getConfigDir();		
		_tracker=new QueueEntryTracker(_configDir, theParent.getDeviceID());
		_theQueue.setMaxRunningEntries(99);
	}
	
	@Override
	public void addHandlers(IJMFHandler jmfHandler)
    {
		jmfHandler.addHandler(this.new RequestQueueEntryHandler());
		jmfHandler.addHandler(this.new ReturnQueueEntryHandler());
		super.addHandlers(jmfHandler);
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
	protected boolean submitQueueEntry(JDFQueueEntry qe, String targetURL, String deviceID)
	{
		if ( qe.getDeviceID()!=null && !qe.getDeviceID().equals("") ) {
			log.error( "QueueEntry '"+qe.getQueueEntryID()+"' has already been forwarded" );
			return false;
		}
		
		// get DeviceID from targetURL
		if (targetURL.endsWith("/")) {
			targetURL = targetURL.substring(0, targetURL.length()-2);
		}
		// recalculate device ID
        if(KElement.isWildCard(deviceID))
            deviceID = targetURL.substring(targetURL.lastIndexOf("/"));
        
        qe.setDeviceID( deviceID );
		BambiNSExtension.setDeviceURL(qe,targetURL);
		
		// build SubmitQueueEntry
		File fOut=UrlUtil.urlToFile(targetURL);
        if(fOut!=null)
        {
            File fIn=UrlUtil.urlToFile(BambiNSExtension.getDocURL(qe));
            if(fIn!=null && fIn.canRead())
            {
                return FileUtil.copyFile(fIn, fOut);
            }  
            return false; // snafu ...
        }
		JDFJMF jmf=JDFJMF.createJMF(JDFMessage.EnumFamily.Command,JDFMessage.EnumType.SubmitQueueEntry);
		JDFCommand com = (JDFCommand)jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command,0);
		JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();
		qsp.setURL( _parent.getDeviceURL()+"?cmd=showJDFDoc&qeid="+qe.getQueueEntryID() );
		String returnURL = BambiNSExtension.getReturnURL(qe);
		// QueueSubmitParams need either ReturnJMF or ReturnURL
		if (returnURL!=null && returnURL.length()>0) {
		    qsp.setReturnURL( _parent.getDeviceURL() );
		} else {
			qsp.setReturnJMF( _parent.getDeviceURL() );
		}

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
//////////////////////////////////////////////////////////////////////////////
    
	@Override
	protected void handleAbortQueueEntry(JDFResponse resp, String qeid,
			JDFQueueEntry qe) {
		int retCode=changeQEStatus(qeid, EnumQueueEntryStatus.Aborted);
		if (retCode==0) {
			updateEntry(qe,EnumQueueEntryStatus.Aborted);
		}
		
	}

	@Override
	protected void handleResumeQueueEntry(JDFResponse resp, String qeid,
			JDFQueueEntry qe) {
		int retCode=changeQEStatus(qeid, EnumQueueEntryStatus.Running);
		if (retCode==0) {
			updateEntry(qe,EnumQueueEntryStatus.Running);
		}
	}

	@Override
	protected void handleSuspendQueueEntry(JDFResponse resp, String qeid,
			JDFQueueEntry qe) {
		int retCode=changeQEStatus(qeid, EnumQueueEntryStatus.Suspended);
		if (retCode==0)
			updateEntry(qe,EnumQueueEntryStatus.Suspended);
	}
	
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
	
	@Override
	protected boolean storeDoc(JDFQueueEntry newQE, JDFDoc theJDF, String returnURL, String returnJMF)
    {
        if(newQE==null || theJDF==null) {
            log.error("error storing queueentry");
            return false;
        }
        String newQEID=newQE.getQueueEntryID();
        newQE=_theQueue.getQueueEntry(newQEID);
        if(newQE==null) {
            log.error("error fetching queueentry: QueueEntryID="+newQEID);
            return false;
        }
        
        String theDocFile=_jdfDir+newQEID+".jdf";
        boolean ok=theJDF.write2File(theDocFile, 0, true);
        String docURL=_parent.getDeviceURL()+"?cmd=showJDFDoc&qeid="+newQEID;
        BambiNSExtension.setDocURL( newQE,docURL );
        if(!KElement.isWildCard(returnJMF)) {
        	BambiNSExtension.setReturnURL(newQE, returnJMF);
        } else if(!KElement.isWildCard(returnURL)) {
        	BambiNSExtension.setReturnURL(newQE, returnURL);
        }

        return ok;
    }
	
}
