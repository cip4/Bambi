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
package org.cip4.bambi.core.messaging;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.mail.Multipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.VectorMap;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;

/**
 * allow a JMF message to be send in its own thread
 * @author boegerni
 */
public class MessageSender implements Runnable {
    private String _url=null;
    private boolean doShutDown=false;
    private boolean doShutDownGracefully=false;
    private Vector<MessageDetails> _messages=null;
    private static Log log = LogFactory.getLog(MessageSender.class.getName());
    private static IConverterCallback _callBack=null;
    private static VectorMap<String, DumpDir> vDumps=new VectorMap<String, DumpDir>();
    private Object mutexDispatch=new Object();


    protected class MessageDetails
    {
        protected JDFJMF jmf=null;
        protected Multipart mime=null;
        protected IResponseHandler respHandler;
        protected MIMEDetails mimeDet;
        protected String senderID=null;
        protected MessageDetails(JDFJMF _jmf, IResponseHandler _respHandler, HTTPDetails hdet)
        {
            respHandler=_respHandler;
            jmf=_jmf;
            senderID=jmf==null ? null : jmf.getSenderID();
            if(hdet==null)
                mimeDet=null;
            else
            {
                mimeDet=new MIMEDetails();
                mimeDet.httpDetails=hdet;
            }            
        }
        protected MessageDetails(Multipart _mime, IResponseHandler _respHandler, MIMEDetails mdet,String _senderID)
        {
            respHandler=_respHandler;
            mime=_mime;
            mimeDet=mdet;
            senderID=_senderID;
        }
    }

    /**
     * trivial response handler that simply grabs the response and passes it back through
     * getResponse() / isHandled()
     * @author prosirai
     *
     */
    public static class MessageResponseHandler implements IResponseHandler
    {
        private JDFResponse resp=null;
        private HttpURLConnection connect=null;
        protected BufferedInputStream bufferedInput=null;
        private Object mutex=new Object();

        public MessageResponseHandler()
        {
            super();
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
         */
        public boolean handleMessage()
        {
            finalizeHandling();
            return true;
        }

        /**
         * 
         */
        protected void finalizeHandling()
        {
            if(mutex==null)
                return;
            synchronized (mutex)
            {
                mutex.notifyAll();                     
            }
            mutex=null;
        }

        public JDFResponse getResponse()
        {
            return resp;
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
         */
        public HttpURLConnection getConnection()
        {
            return connect;
        }
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.IResponseHandler#getConnection()
         */
        public void setConnection(HttpURLConnection uc)
        {
            connect=uc;
        }

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(java.io.BufferedInputStream)
         */
        public void setBufferedStream(BufferedInputStream bis)
        {
            bufferedInput=bis;            
        }
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.IResponseHandler#setBufferedStream(java.io.BufferedInputStream)
         */
        public InputStream getBufferedStream()
        {
            if(bufferedInput!=null) 
                return bufferedInput;
            if(connect==null)
                return null;
            try
            {
                bufferedInput=new BufferedInputStream(connect.getInputStream());
            }
            catch (IOException x)
            {
                //nop 
            }
            return bufferedInput;
        }

        /**
         * @param i
         */
        public void waitHandled(int i)
        {
            if(mutex==null)
                return;
            synchronized (mutex)
            {
                try
                {
                    mutex.wait(i);
                }
                catch (InterruptedException x)
                {
                    //nop
                }
            }
        }
    }
    /**
     * constructor
     * @param theUrl the URL to send the message to
     * @param responseHandler the origin of the JMF. Its notify(JDFResponse) method will be
     *               triggered when a response has been received. <br/>
     *               If the response is null, the IResponseHandler will not be triggered.     
     * @param callBack the converter callback to use, null if no modification is required                
     */
    public MessageSender(String theUrl,  IConverterCallback callback) {
        _messages=new Vector<MessageDetails>();
        _url=theUrl;
        _callBack=callback;
    }

    /**
     * the sender loop. <br/>
     * Checks whether its vector of pending messages is empty. If it is not empty, 
     * the first message is sent and removed from the map.
     */
    public void run() {
        while (!doShutDown) 
        {
            boolean sentFirstMessage;
            try{
                synchronized(_messages) {
                    sentFirstMessage = sendFirstMessage();
                    if(sentFirstMessage)
                    {
                        _messages.remove(0);
                    }

                    if ( doShutDownGracefully && _messages.isEmpty() ) {
                        doShutDown=true;
                    }
                }
            }
            catch(Exception x)
            {
                sentFirstMessage=false;
            }
            if(!sentFirstMessage)
            {
                try
                {
                    synchronized (mutexDispatch)
                    {
                        mutexDispatch.wait(1000);                        
                    }
                }
                catch (InterruptedException x)
                {
                    //nop
                }
            }
        }
    }


    /**
     * send the first enqueued message and return true if all went well
     * also update any returned responses for Bambi internally
     * @return boolean true if the message is assumed sent
     *                 false if an error was detected and the Message must remain in the queue
     */
    private boolean sendFirstMessage()
    {
        synchronized (_messages)
        {

            if (_messages==null ||_messages.isEmpty() ) 
                return false;

            boolean b=true;
            MessageDetails mh=_messages.get(0);
            if(mh==null)
                return true;
            final DumpDir outDump = getOutDump(mh.senderID);
            final DumpDir inDump = getInDump(mh.senderID);

            JDFJMF jmf=mh.jmf;
            Multipart mp=mh.mime;
            if(KElement.isWildCard(_url) ) 
                return true; // snafu anyhow but not sent but no retry useful
            if ( jmf==null && mp==null)
                return true; // need no resend - will remove

            try{
                HttpURLConnection con;
                if(jmf!=null)
                {
                    final JDFDoc jmfDoc = jmf.getOwnerDocument_JDFElement();
                    HTTPDetails hd=mh.mimeDet==null ? null : mh.mimeDet.httpDetails;
                    con=jmfDoc.write2HTTPURL(new URL(_url),hd);
                    if(outDump!=null)
                    {
                        File dump=outDump.newFile();
                        jmfDoc.write2File(dump, 0, true);                    
                    }
                }
                else if(mp!=null)
                {
                    con=MimeUtil.writeToURL(mp, _url, mh.mimeDet);
                    if(outDump!=null)
                    {
                        File dump=outDump.newFile();
                        MimeUtil.writeToFile(mp, dump.getAbsolutePath(),mh.mimeDet);                    
                    }
                }
                else
                {
                    return true; // nothing to send; remove it
                }

                if(con!=null && con.getResponseCode()==200)
                {
                    BufferedInputStream bis=new BufferedInputStream(con.getInputStream());                    
                    bis.mark(1000000);

                    if(inDump!=null)
                    {
                        inDump.newFileFromStream(bis);
                    }
                    if (mh.respHandler!=null) 
                    {
                        mh.respHandler.setConnection(con);
                        mh.respHandler.setBufferedStream(bis);
                        b=mh.respHandler.handleMessage();
                    }
                }

            }
            catch (Exception e) {
               log.error("Exception in sendfirstmessage",e);
            }
            return b;
        }
    }

    /**
     * stop sending new messages immediately and shut down
     * @param gracefully true  - process remaining messages first, then shut down. <br/>
     *                   false - shut down immediately, skip remaining messages.
     */
    public void shutDown(boolean gracefully) {
        if (gracefully) {
            doShutDownGracefully=true;
        } else {
            doShutDown=true;
        }
    }
    private DumpDir getInDump(String senderID)
    {
        return vDumps.getOne(senderID, 0);
    }
    private DumpDir getOutDump(String senderID)
    {
        return vDumps.getOne(senderID, 1);        
    }

    public static void addDumps(String senderID, DumpDir inDump, DumpDir outDump)
    {
        vDumps.putOne(senderID, inDump);
        vDumps.putOne(senderID, outDump);
    }
    /**
     * queses a message for the URL that this MessageSender belongs to
     * also updates the message for a given recipient if required
     * @param jmf the message to send
     * @return true, if the message is successfully queued. 
     *         false, if this MessageSender is unable to accept further messages (i. e. it is shutting down). 
     */
    public boolean queueMessage(JDFJMF jmf, IResponseHandler handler) {
        if (doShutDown || doShutDownGracefully) {
            return false;
        }
        if(_callBack!=null)
            _callBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());

        synchronized(_messages) {
            _messages.add(new MessageDetails(jmf,handler,null));
        }
        synchronized (mutexDispatch)
        {
            mutexDispatch.notifyAll();            
        }
        return true;
    }
    /**
     * queses a message for the URL that this MessageSender belongs to
     * also updates the message for a given recipient if required
     * @param jmf the message to send
     * @return true, if the message is successfully queued. 
     *         false, if this MessageSender is unable to accept further messages (i. e. it is shutting down). 
     */
    public boolean queueMimeMessage(Multipart multpart, IResponseHandler handler, MIMEDetails md, String senderID) {
        if (doShutDown || doShutDownGracefully) {
            return false;
        }

        synchronized(_messages) {
            _messages.add(new MessageDetails(multpart,handler,md,senderID));
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MessageSender - URL: "+_url+" size: "+_messages.size()+"\n"+_messages;
    }

}
