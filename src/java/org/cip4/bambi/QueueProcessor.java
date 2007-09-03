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
package org.cip4.bambi;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.mail.Multipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.servlets.DeviceServlet;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueEntryDef;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;


/**
 *
 * @author  rainer
 *
 *
 */
public class QueueProcessor implements IQueueProcessor
{
	// TODO add HoldQueueEntryHandler
	
    protected class SubmitQueueEntryHandler implements IMessageHandler
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
            EnumType typ=m.getEnumType();
            log.debug( "Handling "+typ.getName() );
            if(EnumType.SubmitQueueEntry.equals(typ))
            {
                JDFQueueSubmissionParams qsp=m.getQueueSubmissionParams(0);
                if(qsp!=null)
                {
                    JDFDoc doc=qsp.getURLDoc();

                    if(doc!=null)
                    {
                        JDFResponse r2=addEntry((JDFCommand)m, doc);
                        if(r2!=null)
                        {
                            resp.mergeElement(r2, false);
                            return true;
                        }
                    }
                }
                log.error("QueueSubmissionParams missing or invalid");
                resp.setErrorText("QueueSubmissionParams missing or invalid");
                resp.setReturnCode(9);
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
            return EnumType.SubmitQueueEntry;
        }
    }

    protected class QueueStatusHandler implements IMessageHandler
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
	        log.debug("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.QueueStatus.equals(typ))
	        {
	        	
	        	
	        	if (_theQueue != null)
	        	{
	        		JDFQueue q = (JDFQueue) resp.copyElement(_theQueue, null);
	        		//TODO filter some stuff?
	        		removeBambiNSExtensions(q);
	        		return true;
	        	}
	        	else 
	        	{
	        		log.error("queue is null");
	        		return false;
	        	}
	        }
	
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
	        return EnumType.QueueStatus;
	    }
	
	}

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
	        log.debug("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.AbortQueueEntry.equals(typ))
	        {
                String qeid = getMessageQueueEntryID(m);
	            JDFQueueEntry qe =_theQueue.getEntry(qeid);
	            if (qe==null)
	            {
	            	resp.setReturnCode(105);
	            	resp.setErrorText("found no QueueEntry with QueueEntryID="+qeid);
	            	return true;
	            } else {
	                EnumQueueEntryStatus status = qe.getQueueEntryStatus();
	                if ( EnumQueueEntryStatus.Completed.equals(status) )
	                {
	                    log.error("cannot abort QueueEntry with ID="+qeid+", it is already completed");
	                    resp.setReturnCode(114);
	                    resp.setErrorText("job is already completed");
	                    return true;
	                } else if ( EnumQueueEntryStatus.Aborted.equals(status) )
	                {
	                    log.error("cannot abort QueueEntry with ID="+qeid+", it is already aborted");
	                    resp.setReturnCode(113);
	                    resp.setErrorText("QueueEntry is already aborted");
	                    return true;
	                }

	                // has to be waiting, held, running or suspended: abort it!
	                qe.setQueueEntryStatus(EnumQueueEntryStatus.Aborted);
	                JDFQueue q = resp.appendQueue();
	                q.copyElement(qe, null);
	                q.setDeviceID( _theQueue.getDeviceID() );
                    q.setStatus( _theQueue.getStatus() );
                    updateEntry(qeid, EnumQueueEntryStatus.Aborted);
                    removeBambiNSExtensions(q);
	                log.debug("aborted QueueEntry with ID="+qeid); 				
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
	        return EnumType.AbortQueueEntry;
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
	        log.debug("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.SuspendQueueEntry.equals(typ))
	        {
	            String qeid = getMessageQueueEntryID(m);
	            JDFQueueEntry qe =_theQueue.getEntry(qeid);
	            if (qe==null)
	            {
	            	resp.setReturnCode(105);
	            	resp.setErrorText("found no QueueEntry with QueueEntryID="+qeid);
	            	return true;
	            } else {
	            	String qeID = qe.getQueueEntryID();
	                EnumQueueEntryStatus status = qe.getQueueEntryStatus();

	                if ( EnumQueueEntryStatus.Running.equals(status) )
	                {
	                    qe.setQueueEntryStatus(EnumQueueEntryStatus.Suspended);
	                    JDFQueue q = resp.appendQueue();
	                    q.setDeviceID( _theQueue.getDeviceID() );
	                    q.setStatus( _theQueue.getStatus() );
	                    q.copyElement(qe, null);
	                    removeBambiNSExtensions(q);
	                    updateEntry(qeID,EnumQueueEntryStatus.Suspended);
	                    log.debug("suspended QueueEntry with ID="+qeid); 				
	                    return true;
	                }

	                if ( EnumQueueEntryStatus.Suspended.equals(status) )
	                {
	                    log.error("cannot suspend QueueEntry with ID="+qeid+", it is already suspended");
	                    resp.setReturnCode(113);
	                    resp.setErrorText("QueueEntry is already suspended");
	                    return true;
	                }

	                if ( EnumQueueEntryStatus.Waiting.equals(status)  || EnumQueueEntryStatus.Held.equals(status) )
	                {
	                    log.error("cannot abort QueueEntry with ID="+qeid+", it is "+status.getName());
	                    resp.setReturnCode(115);
	                    resp.setErrorText("QueueEntry is "+status.getName());
	                    return true;
	                }

	                if ( EnumQueueEntryStatus.Completed.equals(status)  || EnumQueueEntryStatus.Aborted.equals(status) )
	                {
	                    log.error("cannot suspend QueueEntry with ID="+qeid+", it is already "+status.getName());
	                    resp.setReturnCode(115);
	                    resp.setErrorText("QueueEntry is already "+status.getName());
	                    return true;
	                }
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
	        log.debug("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.ResumeQueueEntry.equals(typ))
	        {
	            String qeid = getMessageQueueEntryID(m);

	            JDFQueueEntry qe =_theQueue.getEntry(qeid);
	            if (qe==null)
	            {
	            	resp.setReturnCode(105);
	            	resp.setErrorText("found no QueueEntry with QueueEntryID="+qeid);
	            	return true;
	            } else {
	            	String qeID = qe.getQueueEntryID();
	                EnumQueueEntryStatus status = qe.getQueueEntryStatus();

	                if ( EnumQueueEntryStatus.Suspended.equals(status) || EnumQueueEntryStatus.Held.equals(status) )
	                {
	                    qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
	                    JDFQueue q = resp.appendQueue();
	                    q.copyElement(qe, null);
	                    q.setDeviceID( _theQueue.getDeviceID() );
	                    q.setStatus( _theQueue.getStatus() );
	                    // TODO should be waiting
	                    updateEntry(qeID,EnumQueueEntryStatus.Running);
	                    removeBambiNSExtensions(q);
	                    log.debug("resumed QueueEntry with ID="+qeid); 				
	                    return true;
	                }

	                if ( EnumQueueEntryStatus.Running.equals(status) )
	                {
	                    log.error("cannot resume QueueEntry with ID="+qeid+", it is "+status.getName());
	                    resp.setReturnCode(113);
	                    resp.setErrorText("QueueEntry is "+status.getName());
	                    return true;
	                }

	                if ( EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status) )
	                {
	                    log.error("cannot resume QueueEntry with ID="+qeid+", it is already "+status.getName());
	                    resp.setReturnCode(115);
	                    resp.setErrorText("QueueEntry is already "+status.getName());
	                    return true;
	                }
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

	protected class RemoveQueueEntryHandler implements IMessageHandler
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
	        log.debug("Handling "+m.getType());
	        EnumType typ=m.getEnumType();
	        if(EnumType.RemoveQueueEntry.equals(typ))
	        {
	            String qeid = getMessageQueueEntryID(m);
	            JDFQueueEntry qe =_theQueue.getEntry(qeid);
	            if (qe==null)
	            {
	            	log.error("failed to remove QueueEntry with ID="+qeid+", QueueEntry does not exist.");
		            resp.setReturnCode(105);
		            resp.setErrorText("found no QueueEntry with QueueEntryID="+qeid);
	            	return true;
	            } else {
	                EnumQueueEntryStatus status = qe.getQueueEntryStatus();
	
	                if ( EnumQueueEntryStatus.Held.equals(status) || 
	                		EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
	                {
	                    qe.setQueueEntryStatus(EnumQueueEntryStatus.Removed);
	                    _theQueue.cleanup();
	                    JDFQueue q = resp.appendQueue();
	                    q.copyElement(qe, null);
	                    q.setDeviceID( _theQueue.getDeviceID() );
	                    q.setStatus( _theQueue.getStatus() );
	                    removeBambiNSExtensions(q);
	                    updateEntry(qeid, EnumQueueEntryStatus.Removed);
	                    log.debug("removed QueueEntry with ID="+qeid);
	                    return true;
	                } else {
	                	String statName = status.getName();
	                    log.error("cannot remove QueueEntry with ID="+qeid+", it is "+statName);
	                    resp.setReturnCode(106);
	                    resp.setErrorText("QueueEntry is "+statName);
	                    return true;
	                }
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
	        return EnumType.RemoveQueueEntry;
	    }
	}

	protected static final Log log = LogFactory.getLog(QueueProcessor.class.getName());
    private File _queueFile;
    private static final long serialVersionUID = -876551736245089033L;
    protected JDFQueue _theQueue;
    private IQueueProcessor fallBackQProcessor=null;
    private Vector _listeners;
    private String currentQueueEntryID=null;
     
    public QueueProcessor(String deviceID)
    {
		super();
		this.init(deviceID);
    }
    
    /**
     * @param jmfHandler
     */
    public void addHandlers(IJMFHandler jmfHandler)
    {
        jmfHandler.addHandler(this.new SubmitQueueEntryHandler());
        jmfHandler.addHandler(this.new QueueStatusHandler());
        jmfHandler.addHandler(this.new AbortQueueEntryHandler());
        jmfHandler.addHandler(this.new SuspendQueueEntryHandler());
        jmfHandler.addHandler(this.new ResumeQueueEntryHandler());
        jmfHandler.addHandler(this.new RemoveQueueEntryHandler());
    }

    private void init(String deviceID) 
    {
        log.info("QueueProcessor construct");
      	_queueFile=new File(DeviceServlet.baseDir+File.separator+"theQueue_"+deviceID+".xml");       
        _queueFile.getParentFile().mkdirs();
        new File(DeviceServlet.jdfDir).mkdirs();
        JDFDoc d=JDFDoc.parseFile(_queueFile.getAbsolutePath());
        if(d!=null)
        {
            log.info("refreshing queue");
            _theQueue=(JDFQueue) d.getRoot();
            
        }
        else
        {
            d=new JDFDoc(ElementName.QUEUE);
            log.info("creating new queue");
            _theQueue=(JDFQueue) d.getRoot();
            _theQueue.setQueueStatus(EnumQueueStatus.Waiting);
        }
        _theQueue.setAutomated(true);
        _theQueue.setDeviceID(deviceID);
        _theQueue.maxCompletedEntries = 100; // remove just the selected QE when RemoveQE is called 
        _listeners=new Vector();
	}

    public IQueueEntry getNextEntry()
    {
    	if (log != null) // dirty hack, static log gets trashed too soon on Tomcat undeploy
    		log.debug("getNextEntry");
        JDFQueueEntry qe=_theQueue.getNextExecutableQueueEntry();
        if(qe==null && fallBackQProcessor!=null)
            return fallBackQProcessor.getNextEntry();
        
        if(qe==null)
        {
        	currentQueueEntryID = null;
        	// TODO query parent device for next qe
            return null;
        }
        currentQueueEntryID=qe.getQueueEntryID();
        String docURL=BambiNSExtension.getDocURL(qe);
        docURL=UrlUtil.urlToFile(docURL).getAbsolutePath();
        JDFDoc doc=JDFDoc.parseFile(docURL);
        return new QueueEntry(doc,qe);        
    }


    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#addListener(java.lang.Object)
     */
    public void addListener(Object o)
    {
        log.info("adding new listener");
        _listeners.add(o);        
    }

     /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#addEntry(org.cip4.jdflib.jmf.JDFCommand, org.cip4.jdflib.core.JDFDoc)
     */
    public JDFResponse addEntry(JDFCommand submitQueueEntry, JDFDoc theJDF)
    {
        if(submitQueueEntry==null || theJDF==null)
        {
            log.error("error submitting new queueentry");
            return null;
        }
        if(!_theQueue.canAccept())
            return null;
        
        JDFQueueSubmissionParams qsp=submitQueueEntry.getQueueSubmissionParams(0);
        if(qsp==null)
        {
            log.error("error submitting new queueentry");
            return null;
        }
        
        JDFResponse r=qsp.addEntry(_theQueue, null);
        JDFQueueEntry newQE=r.getQueueEntry(0);
       
        if(r.getReturnCode()!=0 || newQE==null)
        {
            log.error("error submitting queueentry: "+r.getReturnCode());
            return r;
        }
         
        if(!storeDoc(newQE,theJDF,qsp.getReturnURL(),qsp.getReturnJMF()))
        {
            log.error("error storing queueentry: "+r.getReturnCode());
        }
        persist();
        notifyListeners();
        return r;
    }

    /**
     * @param newQE
     * @param theJDF
     */
    private boolean storeDoc(JDFQueueEntry newQE, JDFDoc theJDF, String returnURL, String returnJMF)
    {
        if(newQE==null || theJDF==null)
        {
            log.error("error storing queueentry");
            return false;
        }
        String newQEID=newQE.getQueueEntryID();
        newQE=_theQueue.getEntry(newQEID);
        if(newQE==null)
        {
            log.error("error fetching queueentry: QueueEntryID="+newQEID);
            return false;
        }
        String theDocFile=DeviceServlet.jdfDir+newQEID+".jdf";
        boolean ok=theJDF.write2File(theDocFile, 0, true);
        try
        {
            BambiNSExtension.setDocURL( newQE,UrlUtil.fileToUrl(new File(theDocFile),false) );
            if(!KElement.isWildCard(returnJMF))
                BambiNSExtension.setReturnURL(newQE, returnJMF);
            else if(!KElement.isWildCard(returnURL))
                BambiNSExtension.setReturnURL(newQE, returnURL);
        }
        catch (MalformedURLException x)
        {
            log.error("invalid file name: "+theDocFile);
        }

        return ok;
    }

    private void notifyListeners()
    {
        for(int i=0;i<_listeners.size();i++)
        {
            final Object elementAt = _listeners.elementAt(i);
            synchronized (elementAt)
            {
                elementAt.notifyAll();               
            }
         }
    }

    /**
     * make the memory queue persistent
     *
     */
    private synchronized void persist()
    {
        log.info("persisting queue to "+_queueFile.getAbsolutePath());
        _theQueue.getOwnerDocument_KElement().write2File(_queueFile.getAbsolutePath(), 0, true);
        if(fallBackQProcessor instanceof QueueProcessor)
        {
            ((QueueProcessor)fallBackQProcessor).persist();
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#getQueue()
     */
    public JDFQueue getQueue()
    {
        return _theQueue;
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#updateEntry(java.lang.String, org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus)
     */
    public void updateEntry(String queueEntryID, EnumQueueEntryStatus status)
    {
        if(queueEntryID==null)
            return;
        JDFQueueEntry qe=getEntry(queueEntryID);
        if (qe == null)
        	return;
        qe.setQueueEntryStatus(status);
        if (status == EnumQueueEntryStatus.Completed || status == EnumQueueEntryStatus.Aborted)
        	returnQueueEntry(qe);
        persist();
        notifyListeners();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String s="[QueueProcessor: ] Status= "+_theQueue.getQueueStatus().getName()+" Num Entries: "+_theQueue.numEntries(null)+"\n Queue:\n";
        s+=_theQueue.toString();
        return s;
    }
    
    private void returnQueueEntry(JDFQueueEntry qe)
	{
		JDFDoc docJMF=new JDFDoc("JMF");
        JDFJMF jmf=docJMF.getJMFRoot();
        JDFCommand com=(JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.ReturnQueueEntry);
        JDFReturnQueueEntryParams qerp = com.appendReturnQueueEntryParams();
        // TODO set real node IDs
        if (qe.getStatus() == EnumNodeStatus.Completed)
        	qerp.setCompleted( new VString("root",null) );
        else if (qe.getStatus() == EnumNodeStatus.Aborted)
        	qerp.setAborted( new VString("root",null) );
        String returnURL=BambiNSExtension.getReturnURL(qe);
        qerp.setURL("cid:dummy"); // will be overwritten by buildMimePackage
        final String queueEntryID = qe.getQueueEntryID();
        qerp.setQueueEntryID(queueEntryID);
        JDFParser p = new JDFParser();
        JDFDoc docJDF = p.parseFile( BambiNSExtension.getDocURL(qe) );
        Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF);
        if(returnURL!=null)
        {
            try {
                HttpURLConnection response = MimeUtil.writeToURL(mp, returnURL);
                if (response.getResponseCode() == 200)
                    log.info("returnQueueEntry for "+queueEntryID+" has been sent.");
                else
                    log.error("failed to send RequestQueueEntry. Response: "+response.toString());
            } catch (Exception e) {
                log.error("failed to send ReturnQueueEntry: "+e);
            }
        }
        else
        {
            // TODO write to default output
            log.warn("No return URL specified");
        }
	}

    protected String getMessageQueueEntryID(JDFMessage m)
    {
        JDFQueueEntryDef def = m.getQueueEntryDef(0);
        if (def == null)
        {
        	log.error("Message contains no QueueEntryDef");
            return null;
        }
        	
        String qeid = def.getQueueEntryID();
        if (KElement.isWildCard(qeid))
        {
        	log.error("QueueEntryID does not contain any QueueEntryID");	
            return null;
        }
        log.debug("processing getMessageQueueEntryID for "+qeid);
        return qeid;
    }
    
	public boolean resume() {
		JDFQueueEntry qe = getCurrentQueueEntry();
		if (qe.getQueueEntryStatus() == EnumQueueEntryStatus.Suspended)
		{
			qe.setQueueEntryStatus(EnumQueueEntryStatus.Running);
			return true;
		}
		log.error("cannot resume current QueueEntry, it is "+qe.getQueueEntryStatus().getName());
		return false;
	}

	public boolean suspend() {
		JDFQueueEntry qe = getCurrentQueueEntry();
		if (qe.getQueueEntryStatus() == EnumQueueEntryStatus.Running)
		{
			qe.setQueueEntryStatus(EnumQueueEntryStatus.Suspended);
			return true;
		}
		log.error("cannot suspend current QueueEntry, it is "+qe.getQueueEntryStatus().getName());
		return false;
	}

	/**
	 * 
	 */
	private JDFQueueEntry getCurrentQueueEntry() 
		{
		return getEntry(currentQueueEntryID);
	}

    private JDFQueueEntry getEntry(String queueEntryID)
    {
        JDFQueueEntry qe=_theQueue.getEntry(queueEntryID);
        if(qe==null && fallBackQProcessor!=null)
        {
            qe=fallBackQProcessor.getQueue().getEntry(queueEntryID);
        }
				return qe;
		}
	

    /**
     * @param fallBackQProcessor the fallBackQProcessor to set
     */
    public void setFallBackQProcessor(IQueueProcessor _fallBackQProcessor)
    {
        this.fallBackQProcessor = _fallBackQProcessor;
	}
    
    /**
     * remove all Bambi namespace extensions from a given queue
     * @param queue the queue to filter
     * @return a queue without Bambi namespaces 
     */
    private void removeBambiNSExtensions(JDFQueue queue)
    {   		
    	for (int i=0;i<queue.getQueueSize();i++) {
    		BambiNSExtension.removeBambiExtensions( queue.getQueueEntry(i) );
    	}
    }
}
