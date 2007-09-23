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
package org.cip4.bambi.devices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.AbstractQueueProcessor;
import org.cip4.bambi.BambiNSExtension;
import org.cip4.bambi.queues.IQueueEntry;
import org.cip4.bambi.queues.QueueEntry;
import org.cip4.bambi.messaging.JMFFactory;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.UrlUtil;




/**
 * QueueProcessor for devices attached to the Bambi root device
 * @author  niels
 *
 *
 */
public class SubdeviceQueueProcessor extends AbstractQueueProcessor
{
	protected static final Log log = LogFactory.getLog(SubdeviceQueueProcessor.class.getName());

	public SubdeviceQueueProcessor(String deviceID, AbstractDevice theParent) {
		super(deviceID, theParent);
	}
	public IQueueEntry getNextEntry()
    {
   		//log.debug("getNextEntry");
        JDFQueueEntry qe=_theQueue.getNextExecutableQueueEntry();
        
        if(qe==null)
        {
        	//log.info("sending RequestQueueEntry to root device");
        	JDFJMF jmf = JMFFactory.buildRequestQueueEntry( _theQueue.getDeviceID() );
        	JMFFactory.send2Bambi(jmf,"");
            return null;
        }
        String docURL=BambiNSExtension.getDocURL(qe);
        if (docURL!=null && !docURL.equals("")) {
        	docURL=UrlUtil.urlToFile(docURL).getAbsolutePath();
            JDFDoc doc=JDFDoc.parseFile(docURL);
            return new QueueEntry(doc,qe);
        } else {
        	log.error("DocURL is missing");
        	return null;
        }
    }
	
	protected void handleAbortQueueEntry(JDFResponse resp, String qeid,
			JDFQueueEntry qe) {
		EnumQueueEntryStatus newStatus=stopOnDevice(qe, EnumQueueEntryStatus.Aborted);
		if (newStatus==null) {
			// got no response
			updateEntry(qeid,EnumQueueEntryStatus.Aborted);
			log.error("failed to suspend QueueEntry with ID="+qeid); 
		} else {
			updateEntry(qeid,newStatus);
		}
		JDFQueue q = resp.appendQueue();
		q.copyElement(qe, null);
		q.setDeviceID( _theQueue.getDeviceID() );
		q.setStatus( _theQueue.getStatus() );
		removeBambiNSExtensions(q);
		log.info("aborted QueueEntry with ID="+qeid);
	}
	
	protected void handleQueueStatus(JDFQueue q) {
		// nothing to do
	}
	
	protected void handleSuspendQueueEntry(JDFResponse resp, String qeid,
			JDFQueueEntry qe) {
		EnumQueueEntryStatus newStatus=stopOnDevice(qe, EnumQueueEntryStatus.Suspended);
		if (newStatus==null) {
			// got no response
			updateEntry(qeid,EnumQueueEntryStatus.Aborted);
			log.error("failed to suspend QueueEntry with ID="+qeid); 
		} else {
			updateEntry(qeid,newStatus);
		}
		JDFQueue q = resp.appendQueue();
		q.setDeviceID( _theQueue.getDeviceID() );
		q.setStatus( _theQueue.getStatus() );
		q.copyElement(qe, null);
		removeBambiNSExtensions(q);
		log.info("suspended QueueEntry with ID="+qeid);
	}
	
	protected void handleResumeQueueEntry(JDFResponse resp, String qeid,
			JDFQueueEntry qe) {
		updateEntry(qeid,EnumQueueEntryStatus.Waiting);
		JDFQueue q = resp.appendQueue();
		q.copyElement(qe, null);
		q.setDeviceID( _theQueue.getDeviceID() );
		q.setStatus( _theQueue.getStatus() );
		removeBambiNSExtensions(q);
		log.info("resumed QueueEntry with ID="+qeid);
	}
	
}
