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
package org.cip4.bambi.workers.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.queues.AbstractQueueProcessor;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;

/**
 * QueueProcessor for devices attached to the Bambi root device
 * @author  niels
 *
 *
 */
public class WorkerQueueProcessor extends AbstractQueueProcessor
{
	protected class AbortQueueEntryHandler implements IMessageHandler
	{
	
	    /* (non-Javadoc)
	     * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
	     */
	    public boolean handleMessage(JDFMessage m, JDFResponse resp)
	    {
	        if(m==null || resp==null)
	        {
	            return false;
	        }
	        log.info("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.AbortQueueEntry.equals(typ))
	        {
	            String qeid = getMessageQueueEntryID(m);
	            JDFQueueEntry qe =_theQueue.getQueueEntry(qeid);
	            if (qe==null) {
	            	JMFHandler.errorResponse(resp, "found no QueueEntry with QueueEntryID="+qeid, 105);
	            	return true;
	            }
				EnumQueueEntryStatus status = qe.getQueueEntryStatus();
				if ( EnumQueueEntryStatus.Completed.equals(status) ) {
					JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID="+qeid+", it is already completed", 114);
				    return true;
				} else if ( EnumQueueEntryStatus.Aborted.equals(status) ) {
					JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID="+qeid+", it is already aborted", 113);
				    return true;
				}
	
				// has to be waiting, held, running or suspended: abort it!
				EnumQueueEntryStatus newStatus=stopOnDevice(qe, EnumQueueEntryStatus.Aborted);
				if (newStatus==null) {
					// got no response
					updateEntry(qe,EnumQueueEntryStatus.Aborted);
					log.error( "failed to abort QueueEntry with ID="+qe.getQueueEntryID() ); 
				} else {
					updateEntry(qe,newStatus);
				}
				JDFQueue q = resp.appendQueue();
				q.copyElement(qe, null);
				q.setDeviceID( _theQueue.getDeviceID() );
				q.setQueueStatus( _theQueue.getQueueStatus() );
				removeBambiNSExtensions(q);
				log.info("aborted QueueEntry with ID="+qeid);		
				return true;
	        }
	
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
	        return EnumType.AbortQueueEntry;
	    }
	}

	protected class ResumeQueueEntryHandler implements IMessageHandler
	{
	
	    /* (non-Javadoc)
	     * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
	     */
	    public boolean handleMessage(JDFMessage m, JDFResponse resp)
	    {
	    	if(m==null || resp==null)
	        {
	            return false;
	        }
	        log.info("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.ResumeQueueEntry.equals(typ))
	        {
	            String qeid = getMessageQueueEntryID(m);
	
	            JDFQueueEntry qe =_theQueue.getQueueEntry(qeid);
	            if (qe==null) {
	            	JMFHandler.errorResponse(resp, "found no QueueEntry with QueueEntryID="+qeid, 105);
	            	return true;
	            }
				EnumQueueEntryStatus status = qe.getQueueEntryStatus();
	
				if ( EnumQueueEntryStatus.Suspended.equals(status) || EnumQueueEntryStatus.Held.equals(status) )
				{
					updateEntry(qe,EnumQueueEntryStatus.Waiting);
					JDFQueue q = resp.appendQueue();
					q.copyElement(qe, null);
					q.setDeviceID( _theQueue.getDeviceID() );
					q.setQueueStatus( _theQueue.getQueueStatus() );
					removeBambiNSExtensions(q);
					log.info("resumed QueueEntry with ID="+qeid); 				
				    return true;
				}
	
				if ( EnumQueueEntryStatus.Running.equals(status) ) {
					JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID="+qeid+", it is "+status.getName(), 113);
				    return true;
				}
	
				if ( EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status) )
				{
					JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID="+qeid+", it is already "+status.getName(), 115);
				    return true;
				}
	        }
	
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
	        return EnumType.ResumeQueueEntry;
	    }
	}

	protected class SuspendQueueEntryHandler implements IMessageHandler
	{
	
	    /* (non-Javadoc)
	     * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
	     */
	    public boolean handleMessage(JDFMessage m, JDFResponse resp)
	    {
	    	if(m==null || resp==null)
	        {
	            return false;
	        }
	        log.info("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.SuspendQueueEntry.equals(typ))
	        {
	            String qeid = getMessageQueueEntryID(m);
	            JDFQueueEntry qe =_theQueue.getQueueEntry(qeid);
	            if (qe==null) {
	            	JMFHandler.errorResponse(resp, "found no QueueEntry with QueueEntryID="+qeid, 105);
	            	return true;
	            }
				EnumQueueEntryStatus status = qe.getQueueEntryStatus();
	
				if ( EnumQueueEntryStatus.Running.equals(status) )
				{
					EnumQueueEntryStatus newStatus=stopOnDevice(qe, EnumQueueEntryStatus.Suspended);
					if (newStatus==null) {
						// got no response
						updateEntry(qe,EnumQueueEntryStatus.Aborted);
						log.error("failed to suspend QueueEntry with ID="+qeid); 
					} else {
						updateEntry(qe,newStatus);
					}
					JDFQueue q = resp.appendQueue();
					q.setDeviceID( _theQueue.getDeviceID() );
					q.setQueueStatus( _theQueue.getQueueStatus() );
					q.copyElement(qe, null);
					removeBambiNSExtensions(q);
					log.info("suspended QueueEntry with ID="+qeid); 				
				    return true;
				}
	
				if ( EnumQueueEntryStatus.Suspended.equals(status) )
				{
					JMFHandler.errorResponse(resp, "cannot suspend QueueEntry with ID="+qeid+", it is already suspended", 113);
				    return true;
				}
	
				if ( EnumQueueEntryStatus.Waiting.equals(status)  || EnumQueueEntryStatus.Held.equals(status) )
				{
					String errorMsg="cannot suspend QueueEntry with ID="+qeid+", it is "+status.getName();
					JMFHandler.errorResponse(resp, errorMsg, 115);
				    return true;
				}
	
				if ( EnumQueueEntryStatus.Completed.equals(status)  || EnumQueueEntryStatus.Aborted.equals(status) )
				{
					String errorMsg="cannot suspend QueueEntry with ID="+qeid+", it is already "+status.getName();
				    JMFHandler.errorResponse(resp, errorMsg, 114);
				    return true;
				}
	        }
	
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
	        return EnumType.SuspendQueueEntry;
	    }
	}

	protected static final Log log = LogFactory.getLog(WorkerQueueProcessor.class.getName());

	public WorkerQueueProcessor(AbstractDevice theParent) {
		super(theParent);
	}
	
	/**
     * stop processing the given {@link JDFQueueEntry} on the parent device
     * @param qe the {@link JDFQueueEntry} to work on
     * @param status the targeted {@link EnumQueueEntryStatus} for qe (Aborted, Suspended, Held)
     * @return the new status of qe, null if unable to forwared stop request<br>
     *         Note that it might take some time for the status request to be put in action.
     */
    protected EnumQueueEntryStatus stopOnDevice(JDFQueueEntry qe,EnumQueueEntryStatus status)
    {
    	String queueEntryID=qe.getQueueEntryID();
    	String deviceID=qe.getDeviceID();
    	if (deviceID==null) {
    		log.error("no device ID supplied for "+queueEntryID);
    		return null;
    	}
     	
    	if (_parent == null) {
    		log.error("cannot stop on parent device, parent is null");
    		return null;
    	}
    	JDFQueueEntry returnQE=_parent.stopProcessing(queueEntryID, status);
    	if (returnQE==null) {
    		log.fatal("device '"+deviceID+"' returned a null QueueEntry");
    		return null;
    	}
    	
    	EnumQueueEntryStatus newStatus=qe.getQueueEntryStatus();
    	log.info("QueueEntry with ID="+queueEntryID+" is now "+newStatus.getName());
    	return newStatus;
    }

	@Override
	public void addHandlers(IJMFHandler jmfHandler) {
		super.addHandlers(jmfHandler);
	    jmfHandler.addHandler(this.new AbortQueueEntryHandler());
	    jmfHandler.addHandler(this.new ResumeQueueEntryHandler());
	    jmfHandler.addHandler(this.new SuspendQueueEntryHandler());
	}
}
