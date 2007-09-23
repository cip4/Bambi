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

package org.cip4.bambi.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.devices.AbstractDevice;
import org.cip4.bambi.messaging.JMFFactory;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;

/**
 * processes QueueEntry commands. <br>
 * This servlet has no matching Java Server Page.
 * @author boegerni
 *
 */
public class QueueEntryServlet extends AbstractBambiServlet {
	private String _command="";
	private String _queueEntryID = "";
	private String _deviceID = "";
	private boolean _displayPage = false;
	
	private static Log log = LogFactory.getLog(QueueEntryServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -6128394458416858325L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {		
		if (!checkInit(request, response))
		{
			log.error("failed on checkInit()");
			return;
		}
		
		
		String showDevice = request.getParameter("show");
		if ( showDevice != null && showDevice.equals("true") ) 
			_displayPage = true;
		
		// note: presence of _command is checked in checkInit()
		boolean hasSend = false;
		if ( _command.equals("suspendQueueEntry") )
		{
			hasSend = sendJMF(request, response, EnumType.SuspendQueueEntry); 
		} else if ( _command.equals("resumeQueueEntry") ) {
			hasSend = sendJMF(request, response, EnumType.ResumeQueueEntry); 
		} else if ( _command.equals("abortQueueEntry") ) {
			hasSend = sendJMF(request, response, EnumType.AbortQueueEntry); 
		} else if ( _command.equals("removeQueueEntry") ) {
			hasSend = sendJMF(request, response, EnumType.RemoveQueueEntry);	
		}
		else
		{
			showError("command not implemented", "the command "+_command+" is not implemented",
						request, response);
			return;
		}
		
		if (hasSend)
		{
			log.info("successfully sent "+_command);
			if (_displayPage)
				request.getRequestDispatcher("BambiRootDevice?cmd=showDevice&id="+_deviceID).forward(request, response);
		}
		
	}


	/**
	 * build and send the corresponding JMF command message
	 * @param request
	 * @param response
	 * @param type type of message to send
	 * @throws ServletException
	 * @throws IOException
	 */
	private boolean sendJMF(HttpServletRequest request,
			HttpServletResponse response, EnumType type) throws ServletException, IOException {
		JDFJMF jmf = null;
		if (type == EnumType.SuspendQueueEntry)
			jmf = JMFFactory.buildSuspendQueueEntry(_queueEntryID);
		else if (type == EnumType.ResumeQueueEntry)
			jmf = JMFFactory.buildResumeQueueEntry(_queueEntryID);
		else if (type == EnumType.AbortQueueEntry)
			jmf = JMFFactory.buildAbortQueueEntry(_queueEntryID);
		else if (type == EnumType.RemoveQueueEntry)
			jmf = JMFFactory.buildRemoveQueueEntry(_queueEntryID);
		
		if (jmf == null)
		{
			log.error("failed to create JMF");
			return false;
		}
		
		JDFResponse resp = JMFFactory.send2Bambi(jmf, _deviceID);		
		if (resp == null)
		{
			String errorMsg=type.getName()+" with ID="+_queueEntryID+" on device "+_deviceID+" failed, ";
			errorMsg += "\r\nResponse is null";
			showError("failed to send JMF command", errorMsg, request, response);
			return false;
		}
		if (resp.getReturnCode()!=0 && type!=EnumType.AbortQueueEntry)
		{
			String errorMsg=type.getName()+" with ID="+_queueEntryID+" on device "+_deviceID+" failed.";
			errorMsg += "\r\nResponse: "+resp.toString();
			showError("failed to send JMF command", resp.toString(), request, response);
			return false;
		}
		
		log.info("suspended QueueEntry with ID="+_queueEntryID+" on device "+_deviceID);
		return true;
	}


	/**
	 * checks whether all required attributes are present
	 * @param request
	 * @param response
	 * @return
	 */
	private boolean checkInit(HttpServletRequest request,
			HttpServletResponse response) {
		// make sure there is a command to execute
		_command = request.getParameter("cmd");
		if (_command == null || _command.length() == 0)
		{
			showError("missing command", "-", request, response);
			return false;
		}
		
		// make sure that DeviceID and QueueEntryID are given
		_deviceID = request.getParameter("id");
		_queueEntryID = request.getParameter("qeid");
		if (_deviceID == null || _deviceID.length() == 0 || _queueEntryID == null || _queueEntryID.length() == 0)
		{
			String errorMsg = "can't change status of QueueEntry with DeviceID='"+_deviceID+"' and QueueEntryID='"+
				_queueEntryID+"', either DeviceID or QueueEntryID is missing.";
			showError("missing DeviceID/QueueEntryID", errorMsg, request, response);
			return false;
		}
		
		// get the matching device
		AbstractDevice dev = getDeviceFromObject( request.getAttribute("device") );
		// if dev is null, try to get it from the root device
		if (dev == null) {
			try {
				request.getRequestDispatcher("/BambiRootDevice");
			} catch (Exception e) {
				log.error(e);
			}
			return false;
		}
		
		JDFQueue qu = dev.getQueue();
		if (qu == null)
		{
			showError("queue is null", "-", request, response);
			return false;
		}
		int quPos = qu.getQueueEntryPos(_queueEntryID);
		if (quPos == -1)
		{
			showError("QueueEntry with ID="+_queueEntryID+" is unknown", qu.toString(), request, response);
			return false;
		}
		
		JDFQueueEntry qe = qu.getQueueEntry(quPos);
		if (qe == null)
			showError("QueueEntry with ID="+_queueEntryID+" is null", qu.toString(), request, response);
		
		return true;
	}
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		log.info("called doPost");
	}
	
	private void showError(String errorMsg, String errorDetails, HttpServletRequest request, HttpServletResponse response)
	{
		log.error(errorMsg+":\rn"+errorDetails);
		if (_displayPage)
			showErrorPage(errorMsg, errorDetails, request, response);
	}
}
