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

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFResponse;

/**
 * allow a JMF message to be send in its own thread
 * @author boegerni
 */
public class MessageSender implements Runnable {
	private String _url=null;
	private IResponseHandler _responseHandler=null;
	private boolean doShutDown=false;
	private boolean doShutDownGracefully=false;
	private Vector<JDFJMF> _messages=null;
	private static Log log = LogFactory.getLog(MessageSender.class.getName());
    private static IConverterCallback _callBack=null;
		
	/**
	 * constructor
	 * @param theJMF the message to send
	 * @param theUrl the URL to send the message to
	 * @param responseHandler the origin of the JMF. Its notify(JDFResponse) method will be
	 *               triggered when a response has been received. <br/>
	 *               If the response is null, the IResponseHandler will not be triggered.     
     * @param callBack the converter callback to use, null if no modification is required                
	 */
	public MessageSender(JDFJMF theJMF, String theUrl, IResponseHandler responseHandler, IConverterCallback callback) {
		init(theJMF,theUrl, responseHandler,callback);
	}
	
	private void init(JDFJMF theJMF, String theUrl, IResponseHandler responseHandler,IConverterCallback callback) 
    {
		_messages=new Vector<JDFJMF>();
		if (theJMF!=null)
			_messages.add(theJMF);
		_url=theUrl;
		_responseHandler=responseHandler;
        _callBack=callback;
	}

	/**
	 * the sender loop. <br/>
	 * Checks whether its vector of pending messages is empty. If it is not empty, 
	 * the first message is sent and removed from the map.
	 */
	public void run() {
		while (!doShutDown) {
			synchronized(_messages) {
					if(sendFirstMessage())
					    _messages.remove(0);
				
				if ( doShutDownGracefully && _messages.isEmpty() ) {
					doShutDown=true;
				}
			}
			
			try {
				synchronized (_messages) {
                    _messages.wait(100);                       
                }
			} catch (InterruptedException e) {
				log.error( "MessageSender was interrupted while waiting: "+e.getMessage() );
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
        if (_messages==null ||_messages.isEmpty() ) 
            return false;

        boolean b=true;
        JDFJMF jmf=_messages.get(0);
        if ( jmf==null)
            return true; // need no resend - will remove
        if(KElement.isWildCard(_url) ) 
            return false; // snafu anyhow but not sent

        JDFResponse resp=JMFFactory.send2URL(jmf, _url);
        if (_responseHandler!=null) 
        {
            b=_responseHandler.handleResponse(resp);
        }
        if(_callBack!=null && resp!=null)
        {
            _callBack.prepareJMFForBambi(resp.getOwnerDocument_JDFElement());
        }
        return b;
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
	public boolean queueMessage(JDFJMF jmf) {
		if (doShutDown || doShutDownGracefully) {
			return false;
		}
        if(_callBack!=null)
            _callBack.updateJMFForExtern(jmf.getOwnerDocument_JDFElement());
        
		synchronized(_messages) {
			_messages.add(jmf);
		}
		return true;
	}
	
}
