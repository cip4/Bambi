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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.AbstractDevice;

/**
 * processes commands and view requests for the worker devices
 * @author boegerni
 */
public abstract class AbstractDeviceInfoServlet extends AbstractBambiServlet {

	private static Log log = LogFactory.getLog(AbstractDeviceInfoServlet.class.getName());
	/**
	 * show web page after processing command
	 */
	protected boolean _displayPage = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6128394458416858325L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		String command = request.getParameter("cmd");
		if (command == null)
		{
			String errDetails = "Parameter 'cmd' is missing or null. Which command is the servlet to execute?";
			showErrorPage("mandatory parameter 'cmd' missing or null", errDetails, request, response);
			return;
		}
		
		String showDevice = request.getParameter("show");
		if ( showDevice != null && showDevice.equals("true") ) 
			_displayPage = true;
		
		Object oDev = request.getAttribute("device");
		if ( oDev == null ) // abort if dev is null
		{
			String errDetails = "Mandatory parameter 'dev' is missing or null. "+
			"The servlet needs a device to continue.";
			showErrorPage("mandatory parameter 'dev' is missing or null", errDetails, request, response);
			return;
		}
		
		AbstractDevice dev = (AbstractDevice) oDev;
		request.setAttribute("device", dev);
		request.setAttribute("bqu", dev.getQueueFacade());

		if ( command.equals("showDevice") ) {
			showDevice(request,response);
		} else {
			handleCommand(command,request,response);
		}
	}

	/**
	 * send request to the target, if <code>_displayPage</code> is true
	 * @param target the target to forward the request to
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void forwardVisiblePage(String target, HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		if (_displayPage)
			request.getRequestDispatcher(target).forward(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		log.info("called doPost");
	}
	
	protected abstract void showDevice(HttpServletRequest request, HttpServletResponse response);
	
	protected abstract void handleCommand(String command,HttpServletRequest request, HttpServletResponse response);

}
