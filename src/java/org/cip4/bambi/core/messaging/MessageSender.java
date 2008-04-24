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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.mail.Multipart;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StatusCounter;
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
    public static DumpDir inDump=null; // messy bu efficient...
    public static DumpDir outDump=null; // messy bu efficient...

    protected class MessageDetails
    {
        protected JDFJMF jmf=null;
        protected Multipart mime=null;
        protected IResponseHandler respHandler;
        protected MIMEDetails mimeDet;
        protected MessageDetails(JDFJMF _jmf, IResponseHandler _respHandler, HTTPDetails hdet)
        {
            respHandler=_respHandler;
            jmf=_jmf;
            if(hdet==null)
                mimeDet=null;
            else
            {
                mimeDet=new MIMEDetails();
                mimeDet.httpDetails=hdet;
            }            
        }
        protected MessageDetails(Multipart _mime, IResponseHandler _respHandler, MIMEDetails mdet)
        {
            respHandler=_respHandler;
            mime=_mime;
            mimeDet=mdet;
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
        /* (non-Javadoc)
         * @see org.cip4.bambi.core.messaging.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
         */
        public boolean handleMessage()
        {
            /*
            JDFDoc doc;
            try
            {
                doc = new JDFParser().parseStream(connect.getInputStream());
            }
            catch (IOException x)
            {
                return false;
            }
            if(doc!=null)
            {
                JDFJMF jmfRet=doc.getJMFRoot();
                VElement resps=jmfRet==null ? null : jmfRet.getMessageVector(JDFMessage.EnumFamily.Response, null);
                int siz=resps==null ? 0 : resps.size();
                for(int i=0;i<siz;i++)
                {
                    JDFResponse resp2=(JDFResponse) resps.get(i);
                    if(_callBack!=null && resp!=null)
                    {
                        _callBack.prepareJMFForBambi(resp2.getOwnerDocument_JDFElement());
                    }
                 }
            }
            */
            return true;
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
        while (!doShutDown) {
            boolean sentFirstMessage;
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
            if(!sentFirstMessage)
                StatusCounter.sleep(100);
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

                if(mh.respHandler!=null)
                    mh.respHandler.setConnection(con);

                if(con!=null && con.getResponseCode()==200)
                {
                    if(inDump!=null)
                    {
                        File dump=inDump.newFile();
                        final FileOutputStream fs = new FileOutputStream(dump);
                        IOUtils.copy(con.getInputStream(), fs);
                        fs.flush();
                        fs.close();
                    }
                    if (mh.respHandler!=null) 
                    {
                        mh.respHandler.setConnection(con);
                        b=mh.respHandler.handleMessage();
                    }
                }

            }
            catch (Exception e) {
                // TODO: handle exception
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
        return true;
    }
    /**
     * queses a message for the URL that this MessageSender belongs to
     * also updates the message for a given recipient if required
     * @param jmf the message to send
     * @return true, if the message is successfully queued. 
     *         false, if this MessageSender is unable to accept further messages (i. e. it is shutting down). 
     */
    public boolean queueMimeMessage(Multipart multpart, IResponseHandler handler, MIMEDetails md) {
        if (doShutDown || doShutDownGracefully) {
            return false;
        }

        synchronized(_messages) {
            _messages.add(new MessageDetails(multpart,handler,md));
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "MessageSender - URL: "+_url+" size: "+_messages.size()+"\n"+_messages;
    }

}
