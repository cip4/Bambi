/*
 *
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
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
package org.cip4.bambi.core.queues;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.IDeviceProperties.QEReturn;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueEntryDef;
import org.cip4.jdflib.jmf.JDFQueueFilter;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;

/**
 *
 * @author rainer
 *
 *
 */
public class QueueProcessor implements IQueueProcessor
{

    protected class SubmitQueueEntryHandler  extends AbstractHandler
    {

        public SubmitQueueEntryHandler()
        {
            super(EnumType.SubmitQueueEntry,new EnumFamily[]{EnumFamily.Command});
        }
        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            if(m==null || resp==null) {
                return false;
            }
            log.info( "Handling  SubmitQueueEntry");
            JDFQueueSubmissionParams qsp=m.getQueueSubmissionParams(0);
            if(qsp!=null) {
                JDFDoc doc=qsp.getURLDoc();
                if (doc==null) {
                    updateEntry(null, null, m, resp);
                    String errorMsg="failed to get JDFDoc from '"+qsp.getURL()+"' on SubmitQueueEntry";
                    errorMsg+="\r\nin thread: "+ Thread.currentThread().getName();
                    JMFHandler.errorResponse(resp, errorMsg, 9);
                    return true;
                }
                final IConverterCallback callback = _parentDevice.getCallback();
                if(callback!=null)
                    callback.prepareJDFForBambi(doc);
                JDFQueueEntry qe=addEntry( (JDFCommand)m, doc);
                if(qe==null) {
                    JMFHandler.errorResponse(resp, "failed to add entry: invalid or missing message parameters", 9);
                    return true;
                }
                else
                {
                    JDFQueueEntry qeNew=(JDFQueueEntry) resp.copyElement(qe, null);
                    BambiNSExtension.removeBambiExtensions(qeNew);
                    updateEntry(qe, null, m, resp);
                }
                return true;
            }
            JMFHandler.errorResponse(resp, "QueueSubmissionParams are missing or invalid", 9);
            log.error("QueueSubmissionParams are missing or invalid");
            return true;
        }
    }

    protected class QueueStatusHandler extends AbstractHandler
    {
        public QueueStatusHandler()
        {
            super(EnumType.QueueStatus,new EnumFamily[]{EnumFamily.Command});
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
         */
        public boolean handleMessage(JDFMessage m, JDFResponse resp)
        {
            if(m==null || resp==null) {
                return false;
            }
            updateEntry(null, null, m, resp);
            return true;
        }
    }

    protected class RemoveQueueEntryHandler extends AbstractHandler
    {
        public RemoveQueueEntryHandler()
        {
            super(EnumType.RemoveQueueEntry,new EnumFamily[]{EnumFamily.Command});
        }

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
            JDFQueueEntry qe =getMessageQueueEntry(m,resp);
            if (qe==null) {
                return true;
            }
            String qeid=qe.getQueueEntryID();
            EnumQueueEntryStatus status = qe.getQueueEntryStatus();

            if ( EnumQueueEntryStatus.Held.equals(status) || EnumQueueEntryStatus.Waiting.equals(status) ||
                    EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status))
            {
                updateEntry(qe,EnumQueueEntryStatus.Removed,m,resp);                   
                log.info("removed QueueEntry with ID="+qeid);
            }
            else
            {
                String statName = status.getName();
                updateEntry(qe,status,m,resp);                   
                JMFHandler.errorResponse(resp, "cannot remove QueueEntry with ID="+qeid+", it is "+statName, 106);
            }
            return true;
        }
    }

    protected class HoldQueueEntryHandler  extends AbstractHandler
    {

        public HoldQueueEntryHandler()
        {
            super(EnumType.HoldQueueEntry,new EnumFamily[]{EnumFamily.Command});
        }

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
            JDFQueueEntry qe =getMessageQueueEntry(m,resp);
            if (qe==null) {
                return true;
            }
            final String qeid=qe.getQueueEntryID();
            EnumQueueEntryStatus status = qe.getQueueEntryStatus();

            if ( EnumQueueEntryStatus.Waiting.equals(status) ) {
                updateEntry(qe, EnumQueueEntryStatus.Held, m, resp);
                log.info("held QueueEntry with ID="+qeid);
            }
            else
            {
                updateEntry(qe, status, m, resp);
                if ( EnumQueueEntryStatus.Held.equals(status) ) {
                    JMFHandler.errorResponse(resp, "cannot suspend QueueEntry with ID="+qeid+", it is already held", 113);
                }
                else if ( EnumQueueEntryStatus.Running.equals(status)  || EnumQueueEntryStatus.Suspended.equals(status) ) {
                    JMFHandler.errorResponse(resp, "cannot hold QueueEntry with ID="+qeid+", it is "+status.getName(), 106);
                }

                else if ( EnumQueueEntryStatus.Completed.equals(status)  || EnumQueueEntryStatus.Aborted.equals(status) ) {
                    JMFHandler.errorResponse(resp, "cannot hold QueueEntry with ID="+qeid+", it is already "+status.getName(), 114);
                }
                else
                    return false; //???
            }
            return true;
        }
    }

    protected class AbortQueueEntryHandler extends AbstractHandler
    {

        public AbortQueueEntryHandler()
        {
            super(EnumType.AbortQueueEntry,new EnumFamily[]{EnumFamily.Command});
        }

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
            JDFQueueEntry qe =getMessageQueueEntry(m,resp);
            if (qe==null) {
                return true;
            }
            EnumQueueEntryStatus status = qe.getQueueEntryStatus();
            final String qeid=qe.getQueueEntryID();

            if ( EnumQueueEntryStatus.Completed.equals(status) ) {
                updateEntry(qe,status,m,resp);                   
                JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID="+qeid+", it is already completed", 114);
                return true;
            } else if ( EnumQueueEntryStatus.Aborted.equals(status) ) {
                updateEntry(qe,status,m,resp);                   
                JMFHandler.errorResponse(resp, "cannot abort QueueEntry with ID="+qeid+", it is already aborted", 113);
                return true;
            } 
            else if ( EnumQueueEntryStatus.Waiting.equals(status) ) // no need to check processors - it is still waiting
            {
                updateEntry(qe,EnumQueueEntryStatus.Aborted,m,resp);                   
            }
            String queueEntryID=qe.getQueueEntryID();
            JDFQueueEntry returnQE=_parentDevice.stopProcessing(queueEntryID, EnumNodeStatus.Aborted);

            // has to be waiting, held, running or suspended: abort it!
            EnumQueueEntryStatus newStatus=(returnQE==null ? null : returnQE.getQueueEntryStatus());
            updateEntry(qe,newStatus==null ? EnumQueueEntryStatus.Aborted : newStatus,m,resp);                   
            log.info("aborted QueueEntry with ID="+qeid);		
            return true;
        }
    }

    protected class ResumeQueueEntryHandler  extends AbstractHandler
    {

        public ResumeQueueEntryHandler()
        {
            super(EnumType.ResumeQueueEntry,new EnumFamily[]{EnumFamily.Command});
        }
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
                JDFQueueEntry qe =getMessageQueueEntry(m,resp);
                if (qe==null) {
                    return true;
                }
                final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
                final String qeid=qe.getQueueEntryID();

                if ( EnumQueueEntryStatus.Suspended.equals(status) || EnumQueueEntryStatus.Held.equals(status) )
                {
                    updateEntry(qe,EnumQueueEntryStatus.Waiting,m,resp);                   
                    log.info("resumed QueueEntry with ID="+qeid); 				
                    return true;
                }

                if ( EnumQueueEntryStatus.Running.equals(status) ) {
                    updateEntry(qe,status,m,resp);                   
                    JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID="+qeid+", it is "+status.getName(), 113);
                    return true;
                }

                if ( EnumQueueEntryStatus.Completed.equals(status) || EnumQueueEntryStatus.Aborted.equals(status) )
                {
                    updateEntry(qe,status,m,resp);                   
                    JMFHandler.errorResponse(resp, "cannot resume QueueEntry with ID="+qeid+", it is already "+status.getName(), 115);
                    return true;
                }
            }

            return false;       
        }
    }

    /**
     * @author prosirai
     *
     */
    private class ShowJDFHandler implements IGetHandler
    {
        public boolean handleGet(HttpServletRequest request, HttpServletResponse response)
        {
            if(!AbstractBambiServlet.isMyContext(request, "showJDF"))
            {
                return false;
            }
            final String qeID=request.getParameter(QE_ID);
            String fil=getJDFStorage(qeID);
            if(fil==null)
                return false;
            File f=new File(fil);
            if(!f.canRead())
                return false;
            try
            {
                InputStream is=new FileInputStream(f);
                IOUtils.copy(is, response.getOutputStream());
                boolean bJDF=AbstractBambiServlet.getBooleanFromRequest(request, isJDF);
                response.setContentType(bJDF ? MimeUtil.VND_JDF: MimeUtil.TEXT_XML);
            }
            catch (FileNotFoundException x)
            {
                return false;
            }
            catch (IOException x)
            {
                return false;
            }
            return true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////

    protected class QueueGetHandler extends XMLDoc implements IGetHandler
    {
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
         */
        public boolean handleGet(HttpServletRequest request, HttpServletResponse response)
        {
            if( AbstractBambiServlet.isMyContext(request, "showQueue"))
            {
                // nop
            }
            else if(AbstractBambiServlet.isMyContext(request, "modifyQE"))       
            {
                updateQE(request);
            }
            else
            {
                return false;
            }
            setRoot(ElementName.QUEUE,null);
            getRoot().mergeElement(_theQueue, false);
            setXSLTURL(_parentDevice.getXSLT(SHOW_QUEUE));
            addOptions();

            try
            {
                write2Stream(response.getOutputStream(), 0,true);
            }
            catch (IOException x)
            {
                return false;
            }
            response.setContentType(MimeUtil.TEXT_XML);
            return true;
        }

        /**
         * @param request
         */
        private void updateQE(HttpServletRequest request)
        {
            final String qeID=request.getParameter(QE_ID);
            if(qeID==null)
                return;
            JDFQueueEntry qe=_theQueue.getQueueEntry(qeID);
            EnumQueueEntryStatus status=EnumQueueEntryStatus.getEnum(request.getParameter(QE_STATUS));
            if(status==null)
                return;
            // also stop the device processor
            if(EnumQueueEntryStatus.Completed.equals(status))
                _parentDevice.stopProcessing(qeID, EnumNodeStatus.Completed);
            else if(EnumQueueEntryStatus.Aborted.equals(status))
                _parentDevice.stopProcessing(qeID, EnumNodeStatus.Aborted);
            else if(EnumQueueEntryStatus.Suspended.equals(status))
                _parentDevice.stopProcessing(qeID, EnumNodeStatus.Suspended);
            updateEntry(qe, status,null,null);
        }

        /**
         * 
         */
        private void addOptions()
        {
            JDFQueue q=(JDFQueue)getRoot();
            VElement v=q.getQueueEntryVector();
            for(int i=0;i<v.size();i++)
            {
                JDFQueueEntry qe=(JDFQueueEntry)v.get(i);
                // TODO select iterator based on current value
                AbstractBambiServlet.addOptionList(qe.getQueueEntryStatus(), qe.getNextStatusVector(), qe, QE_STATUS);
            }       
        }
    }

    //////////////////////////////////////////////////////////////////////

    protected class SuspendQueueEntryHandler extends AbstractHandler
    {

        public SuspendQueueEntryHandler()
        {
            super(EnumType.SuspendQueueEntry,new EnumFamily[]{EnumFamily.Command});
        }
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
            JDFQueueEntry qe =getMessageQueueEntry(m,resp);
            if (qe==null) {
                return true;
            }
            final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
            final String qeid=qe.getQueueEntryID();

            if ( EnumQueueEntryStatus.Running.equals(status) )
            {
                JDFQueueEntry returnQE=_parentDevice.stopProcessing(qeid, EnumNodeStatus.Suspended);
                EnumQueueEntryStatus newStatus=(returnQE==null ? null : returnQE.getQueueEntryStatus());
                if (newStatus==null) {
                    // got no response
                    updateEntry(qe,EnumQueueEntryStatus.Aborted,m,resp);
                    log.error("failed to suspend QueueEntry with ID="+qeid); 
                } else {
                    updateEntry(qe,newStatus,m,resp);
                    log.info("suspended QueueEntry with ID="+qeid); 				
                }
            }
            else
            {
                updateEntry(qe,status,m,resp);
                if ( EnumQueueEntryStatus.Suspended.equals(status) )
                {
                    JMFHandler.errorResponse(resp, "cannot suspend QueueEntry with ID="+qeid+", it is already suspended", 113);
                }
                else if ( EnumQueueEntryStatus.Waiting.equals(status)  || EnumQueueEntryStatus.Held.equals(status) )
                {
                    String errorMsg="cannot suspend QueueEntry with ID="+qeid+", it is "+status.getName();
                    JMFHandler.errorResponse(resp, errorMsg, 115);
                }
                else if ( EnumQueueEntryStatus.Completed.equals(status)  || EnumQueueEntryStatus.Aborted.equals(status) )
                {
                    String errorMsg="cannot suspend QueueEntry with ID="+qeid+", it is already "+status.getName();
                    JMFHandler.errorResponse(resp, errorMsg, 114);
                }
                else
                    return false;       
            }
            return true;
        }
    }

    protected static final Log log = LogFactory.getLog(QueueProcessor.class.getName());
    protected File _queueFile=null;
    private static final long serialVersionUID = -876551736245089033L;
    /**
     */
    private static final String QE_STATUS = "qeStatus";
    private static final String QE_ID = "qeID";
    private static final String isJDF = "isJDF";
    private static final String SHOW_QUEUE = "showQueue";
    private static final String SHOW_JDF = "showJDF";
    private static final String MODIFY_QE = "modifyQE";

    protected JDFQueue _theQueue;
    private Vector<Object> _listeners;
    protected AbstractDevice _parentDevice=null;

    public QueueProcessor(AbstractDevice theParentDevice) {
        super();
        _parentDevice=theParentDevice;
        _listeners=new Vector<Object>();

        init();
    }

    /**
     * @param jmfHandler
     */
    public void addHandlers(IJMFHandler jmfHandler) {
        jmfHandler.addHandler(this.new SubmitQueueEntryHandler());
        final QueueStatusHandler qsh=this.new QueueStatusHandler();
        jmfHandler.addHandler(qsh);
        jmfHandler.addSubscriptionHandler(EnumType.QueueStatus, qsh);
        jmfHandler.addHandler(this.new RemoveQueueEntryHandler());
        jmfHandler.addHandler(this.new HoldQueueEntryHandler());
        jmfHandler.addHandler(this.new AbortQueueEntryHandler());
        jmfHandler.addHandler(this.new ResumeQueueEntryHandler());
        jmfHandler.addHandler(this.new SuspendQueueEntryHandler());

    }

    protected void init() {
        String deviceID=_parentDevice.getDeviceID();
        log.info("QueueProcessor construct for device '"+deviceID+"'");

        if(_queueFile==null)
            _queueFile=new File(_parentDevice.getBaseDir()+File.separator+"theQueue_"+deviceID+".xml");
        if (_queueFile!=null && _queueFile.getParentFile()!=null 
                && !_queueFile.getParentFile().exists() ) { // will be null in unit tests
            if ( !_queueFile.getParentFile().mkdirs() )
                log.error( "failed to create base dir at location "+_queueFile.getParentFile() );
        }

        if (_parentDevice.getJDFDir()!=null) { // will be null in unit tests
            File jdfDir= _parentDevice.getJDFDir();
            if (!jdfDir.exists())
                if ( !jdfDir.mkdirs() )
                    log.error( "failed to create JDFDir at location "+jdfDir.getAbsolutePath() );
        }

        JDFDoc d=JDFDoc.parseFile(_queueFile.getAbsolutePath());
        if(d!=null) {
            log.info("refreshing queue");
            _theQueue=(JDFQueue) d.getRoot();
            // make sure that all QueueEntries are suspended on restart 
            VElement qev=_theQueue.getQueueEntryVector();
            for (int i=0;i<qev.size();i++) {
                JDFQueueEntry qe=(JDFQueueEntry) qev.get(i);
                EnumQueueEntryStatus stat=qe.getQueueEntryStatus();
                if ( EnumQueueEntryStatus.Running.equals(stat) 
                        || EnumQueueEntryStatus.Waiting.equals(stat) ) {
                    qe.setQueueEntryStatus(EnumQueueEntryStatus.Suspended);
                }
            }
        } else {
            d=new JDFDoc(ElementName.QUEUE);
            log.info("creating new queue");
            _theQueue=(JDFQueue) d.getRoot();
            _theQueue.setQueueStatus(EnumQueueStatus.Waiting);
        }
        _theQueue.setAutomated(true);
        _theQueue.setDeviceID(deviceID);
        _theQueue.setMaxCompletedEntries( 100 ); // remove just the selected QE when RemoveQE is called 
    }

    /**
     * get a qe by nodeidentifier
     * only waiting entries that have not been forwarded to a lower level device are tken into account
     * 
     * @param queueEntryID the JDFNode.NodeIdentifier
     * @return
     */
    public IQueueEntry getQueueEntry(NodeIdentifier nodeID) {
        synchronized (_theQueue)
        {

            VElement vQE=_theQueue.getQueueEntryVector(nodeID);
            int siz=vQE==null ? 0 : vQE.size();
            for(int i=0;i<siz;i++)
            {
                JDFQueueEntry qe=(JDFQueueEntry) vQE.get(i);            
                if(EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()) && KElement.isWildCard(BambiNSExtension.getDeviceURL(qe)))
                    return getIQueueEntry(qe);
                // try next    
            }
        }
        return null;
    }

    /**
     * get the next queue entry
     */
    public IQueueEntry getNextEntry(String deviceID) {

        JDFQueueEntry qe=_theQueue.getNextExecutableQueueEntry(deviceID,null);
        return getIQueueEntry(qe);
    }

    /**
     * @param qe
     * @return
     */
    private IQueueEntry getIQueueEntry(JDFQueueEntry qe)
    {
        if(qe==null)
            return null;
        String docURL=BambiNSExtension.getDocURL(qe);
        JDFDoc theDoc = JDFDoc.parseURL(docURL, null);
        if (theDoc==null) {
            log.error( "QueueProcessor in thread '"+Thread.currentThread().getName()
                    +"' is unable to load the JDFDoc from '"+docURL+"'");
            return null;
        }

        JDFNode n=_parentDevice.getNodeFromDoc(theDoc);
        return new QueueEntry(n,qe);
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
     * @see org.cip4.bambi.IQueueProcessor#removeListener(java.lang.Object)
     */
    public void removeListener(Object o)
    {
        log.info("removing listener");
        _listeners.remove(o);        
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#addEntry(org.cip4.jdflib.jmf.JDFCommand, org.cip4.jdflib.core.JDFDoc)
     */
    public JDFQueueEntry addEntry(JDFCommand submitQueueEntry, JDFDoc theJDF)
    {
        if(submitQueueEntry==null || theJDF==null) {
            log.error("error submitting new queueentry");
            return null;
        }
        if(!_theQueue.canAccept())
            return null;
        if(!_parentDevice.canAccept(theJDF))
            return null;

        JDFQueueSubmissionParams qsp=submitQueueEntry.getQueueSubmissionParams(0);
        if(qsp==null) {
            log.error("error submitting new queueentry");
            return null;
        }

        JDFResponse r=qsp.addEntry(_theQueue, null);
        JDFQueueEntry newQE=r.getQueueEntry(0);

        if(r.getReturnCode()!=0 || newQE==null) {
            log.error("error submitting queueentry: "+r.getReturnCode());
            return null;
        }
        final String qeID=newQE.getQueueEntryID();

        if(!storeDoc(newQE,theJDF,qsp.getReturnURL(),qsp.getReturnJMF())) {
            log.error("error storing queueentry: "+r.getReturnCode());
            return null;
        }
        persist();
        notifyListeners();
        return _theQueue.getQueueEntry(qeID);
    }

    /**
     * @param newQE
     * @param theJDF
     */
    protected boolean storeDoc(JDFQueueEntry newQE, JDFDoc theJDF, String returnURL, String returnJMF)
    {
        if(newQE==null || theJDF==null) {
            log.error("error storing queueentry");
            return false;
        }
        String newQEID=newQE.getQueueEntryID();
        final JDFNode root = theJDF.getJDFRoot();
        newQE.setFromJDF(root); // set jobid, jobpartid, partmaps
        newQE=_theQueue.getQueueEntry(newQEID); // the "actual" entry in the queue
        if(newQE==null) {
            log.error("error fetching queueentry: QueueEntryID="+newQEID);
            return false;
        }
        newQE.setFromJDF(root); // repeat for the actual entry

        final String theDocFile=getJDFStorage(newQEID);
        boolean ok=theJDF.write2File(theDocFile, 0, true);
        BambiNSExtension.setDocURL( newQE,theDocFile );
        if(!KElement.isWildCard(returnJMF)) {
            BambiNSExtension.setReturnJMF(newQE, returnJMF);
        } else if(!KElement.isWildCard(returnURL)) {
            BambiNSExtension.setReturnURL(newQE, returnURL);
        }

        return ok;
    }

    /**
     * return the name of the file storage for a given queueentryid
     * @param newQEID
     * @return {@link String} the file name of the storage
     */
    public String getJDFStorage(String newQEID)
    {
        if(newQEID==null)
            return null;
        return _parentDevice.getJDFDir()+File.separator+newQEID+".jdf";
    }

    protected void notifyListeners()
    {
        for(int i=0;i<_listeners.size();i++) {
            final Object elementAt = _listeners.elementAt(i);
            synchronized (elementAt) {
                elementAt.notifyAll();               
            }
        }
    }

    /**
     * make the memory queue persistent
     *
     */
    protected void persist()
    {
        synchronized (_theQueue)
        {
            log.info("persisting queue to "+_queueFile.getAbsolutePath());
            _theQueue.getOwnerDocument_KElement().write2File(_queueFile.getAbsolutePath(), 0, true);
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.IQueueProcessor#getQueue()
     */
    public JDFQueue getQueue()
    {
        return _theQueue;
    }


    /**
     * update the entry qe to be in the new status
     * @return JDFQueue the queue in its new status
     */
    public JDFQueue updateEntry(JDFQueueEntry qe, EnumQueueEntryStatus status, JDFMessage mess, JDFResponse resp)
    {
        if (qe != null && status!=null && !status.equals(qe.getQueueEntryStatus()))
        {
            qe.setQueueEntryStatus(status);
            if (status.equals(EnumQueueEntryStatus.Completed) || status.equals(EnumQueueEntryStatus.Aborted)) {
                if(!qe.hasAttribute(AttributeName.ENDTIME))
                    qe.setEndTime(new JDFDate());
                returnQueueEntry( qe,null ,null);
            }
            else if (status.equals(EnumQueueEntryStatus.Running) ) {
                if(!qe.hasAttribute(AttributeName.STARTTIME))
                    qe.setStartTime(new JDFDate());
            }
            else if (status.equals(EnumQueueEntryStatus.Removed)) {
                String docURL=BambiNSExtension.getDocURL(qe);
                if(docURL!=null)
                    new File(docURL).delete();
            }
            persist();
            notifyListeners();
        }
        if(resp==null)
            return null;

        JDFQueueFilter qf=null;
        try
        {
            qf=mess==null ? null : mess.getQueueFilter(0);
        }
        catch (JDFException e) {
            // nop
        }
        JDFQueue q= _theQueue.copyToResponse(resp, qf);
        removeBambiNSExtensions(q);
        return q;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String s="[QueueProcessor: ] Status= "+_theQueue.getQueueStatus().getName()+" Num Entries: "+_theQueue.numEntries(null)+"\n Queue:\n";
        s+=_theQueue.toString();
        return s;
    }

    protected void returnQueueEntry(JDFQueueEntry qe, VString finishedNodes, JDFDoc docJDF)
    {
        JDFDoc docJMF=new JDFDoc("JMF");
        JDFJMF jmf=docJMF.getJMFRoot();
        JDFCommand com=(JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.ReturnQueueEntry);
        JDFReturnQueueEntryParams returnQEParams = com.appendReturnQueueEntryParams();


        final String queueEntryID = qe.getQueueEntryID();
        returnQEParams.setQueueEntryID(queueEntryID);
        if(docJDF==null)
        {
            String docFile=getJDFStorage(qe.getQueueEntryID());
            docJDF = JDFDoc.parseFile(docFile);
        }
        if ( docJDF==null ) {
            log.error("cannot load the JDFDoc to return");
            return;
        }
        if (finishedNodes==null) {
            JDFNode n=docJDF.getJDFRoot();
            if(n==null)
            {
                finishedNodes=new VString("rootDev",null);
            }
            else
            {
                finishedNodes=new VString(n.getID(),null);
            }
        }

        boolean bAborted=false;
        if ( EnumNodeStatus.Completed.equals( qe.getStatus() )) {
            returnQEParams.setCompleted( finishedNodes );
        } else if ( EnumNodeStatus.Aborted.equals( qe.getStatus() )) {
            returnQEParams.setAborted( finishedNodes );
            bAborted=true;
        }

        // fix for returning
        final IConverterCallback callBack = _parentDevice.getCallback();
        if(callBack!=null)
        {
            callBack.updateJDFForExtern(docJDF);
            callBack.updateJMFForExtern(docJMF);
        }
        String returnURL=BambiNSExtension.getReturnURL(qe);
        String returnJMF=BambiNSExtension.getReturnJMF(qe);
        final IDeviceProperties properties = _parentDevice.getProperties();
        final File deviceOutputHF = properties.getOutputHF();
        final File deviceErrorHF = properties.getErrorHF();

        boolean bOK=false;
        //  need a better synch to message thread
        StatusCounter.sleep(1000); // wait to flush queues

        if(returnJMF!=null) {
            QEReturn qr=properties.getReturnMIME();
            HttpURLConnection response=null;
            if(QEReturn.MIME.equals(qr))
            {
                returnQEParams.setURL("cid:dummy"); // will be overwritten by buildMimePackage
                Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, false);
                MIMEDetails ud=new MIMEDetails();
                ud.httpDetails.chunkSize=properties.getDeviceHTTPChunk();
                ud.transferEncoding=properties.getDeviceMIMEEncoding();
                try {
                    response = MimeUtil.writeToURL(mp, returnJMF,ud);
                } catch (Exception e) {
                    log.error("failed to send ReturnQueueEntry: "+e);
                }
                if(MessageSender.outDump!=null)
                {
                    MimeUtil.writeToFile(mp, MessageSender.outDump.newFile().getAbsolutePath(), null);
                }
            }
            else // http
            {
                returnQEParams.setURL(properties.getContextURL()+"/jmb/JDFDir/"+queueEntryID+".jdf"); // will be overwritten by buildMimePackage
                HTTPDetails hDet=new  HTTPDetails();
                hDet.chunkSize=properties.getDeviceHTTPChunk();

                try
                {
                    URL url = new URL(returnJMF);
                    response = docJMF.write2HTTPURL(url,hDet);
                }
                catch (MalformedURLException x)
                {
                    bOK=false;
                }
            }
            int responseCode;
            if(response!=null)
            {
                try
                {
                    responseCode = response.getResponseCode();
                }
                catch (IOException x)
                {
                    responseCode=0;
                }
                if (responseCode == 200)
                {
                    log.info("ReturnQueueEntry for "+queueEntryID+" has been sent.");
                    bOK=true;
                }
                else
                {
                    log.error("failed to send ReturnQueueEntry. Response: "+response.toString());
                    bOK=false;
                }
            }
        } 

        if (! bOK && returnURL!=null) {
            try {
                JDFDoc d = docJDF.write2URL(returnURL);
                bOK=true;
            } catch (Exception e) {
                log.error("failed to send ReturnQueueEntry: "+e);
            }
        } 
        if(!bOK)
        {
            if (!bAborted && deviceOutputHF!=null){
                deviceOutputHF.mkdirs();
                bOK = docJDF.write2File(deviceOutputHF, 0, true);
                log.info("JDF for "+queueEntryID+" has been written to good output: "+deviceOutputHF);
            } else if (bAborted && deviceErrorHF!=null){
                deviceErrorHF.mkdirs();
                bOK = docJDF.write2File(deviceErrorHF, 0, true);
                log.info("JDF for "+queueEntryID+" has been written to error output: "+deviceErrorHF);
            } else{
                log.warn("No return URL, No HF, No Nothing  specified");
            }
        }
    }

    protected JDFQueueEntry getMessageQueueEntry(JDFMessage m, JDFResponse resp)
    {
        final JDFQueueEntryDef def = m.getQueueEntryDef(0);
        if (def == null) {
            log.error("Message contains no QueueEntryDef");
            return null;
        }

        final String qeid = def.getQueueEntryID();
        if ( KElement.isWildCard(qeid) )  {
            log.error("QueueEntryID does not contain any QueueEntryID");	
            return null;
        }
        log.info("processing getMessageQueueEntryID for "+qeid);
        JDFQueueEntry qe =_theQueue.getQueueEntry(qeid);
        if (qe==null) {
            JMFHandler.errorResponse(resp, "found no QueueEntry with QueueEntryID="+qeid, 105);
        }
        return qe;

    }

    /**
     * remove all Bambi namespace extensions from a given queue
     * @param queue the queue to filter
     * @return a queue without Bambi namespaces 
     */
    public static void removeBambiNSExtensions(JDFQueue queue) {  
        if(queue==null)
            return;
        for (int i=0;i<queue.getQueueSize();i++) {
            BambiNSExtension.removeBambiExtensions( queue.getQueueEntry(i) );
        }
    }

    /* (non-Javadoc)
     * @see org.cip4.bambi.core.IGetHandler#handleGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public boolean handleGet(HttpServletRequest request, HttpServletResponse response)
    {

        boolean b= this.new QueueGetHandler().handleGet(request, response);
        if(!b)
            b=this.new ShowJDFHandler().handleGet(request, response);
        return b;

    }


}
