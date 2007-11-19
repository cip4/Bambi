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

package org.cip4.bambi.workers.manual;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.workers.core.AbstractDeviceInfoServlet;
import org.cip4.bambi.workers.core.AbstractBambiDeviceProcessor.JobPhase;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;

public class ManualDeviceInfoServlet extends AbstractDeviceInfoServlet {
	private static final long serialVersionUID = -2807402684627573611L;
	private static Log log = LogFactory.getLog(ManualDeviceInfoServlet.class.getName());

	@Override
	protected void handleCommand(String command, HttpServletRequest request,
			HttpServletResponse response) {
		ManualDevice dev = getDeviceFromRequest(request, response);
		if (dev==null) {
			String errorDetails="Mandatory parameter \"device\"is missing." 
				+ " The Servlet needs a device to continue";
			showErrorPage("Mandatory parameter \"device\" is missing.", errorDetails, request, response);
			return;
		}
		
		if ( command.equals("finalizeCurrentQE") ) {
			dev.finalizeCurrentQueueEntry();
			JobPhase currentPhase = dev.getCurrentJobPhase();
			request.setAttribute("currentPhase", currentPhase);
			showDevice(request, response);
		}  else if ( command.equals("processNextPhase") ) {
			JobPhase nextPhase = buildJobPhaseFromRequest(request);
			dev.doNextJobPhase(nextPhase);
			try {
				Thread.sleep(1000); // allow device to switch phases before displaying page
			} catch (InterruptedException e) {
				if (_displayPage)
				{
					String errDetails = "DeviceInfoServlet was intterupted while waiting "+
						"for the next job phase to be initialized: \r\n"+e.getMessage();
					showErrorPage("interrupted while sleeping", errDetails, request, response);
					log.error(errDetails);
					return;
				}
			} 
			showDevice(request, response);
		}else {
			log.info("unknown command: "+command);
			showErrorPage("unknown command", "unknown command+ "+command, request, response);
		}
		
	}

	private ManualDevice getDeviceFromRequest(HttpServletRequest request,
			HttpServletResponse response) {
		Object oDev = request.getAttribute("device");
		if ( oDev == null ) {
			return null;
		}
		
		return (ManualDevice)oDev;
	}

	@Override
	protected void showDevice(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			ManualDevice dev=getDeviceFromRequest(request, response);
			if (dev==null) {
				String errorDetails="Mandatory parameter \"device\"is missing." 
					+ " The Servlet needs a device to continue";
				showErrorPage("Mandatory parameter \"device\" is missing.", errorDetails, request, response);
				return;
			}
			JobPhase currentPhase = dev.getCurrentJobPhase();
			request.setAttribute("currentPhase", currentPhase);
			forwardVisiblePage("showManualDevice.jsp", request, response);
		} catch (ServletException e) {
			log.error( "failed to display page: "+e.getMessage() );
		} catch (IOException e) {
			log.error( "failed to display page: "+e.getMessage() );
		}
	}
	
	/**
	 * build a new job phase with info from a given request. 
	 * JobPhase parameter 'duration' will remain with its default value 0, since it
	 * is not used in the context of ManualDevice.doNextJobPhase()
	 * @param request request to get the job phase info from
	 * @return the new JobPhase
	 */
	private JobPhase buildJobPhaseFromRequest(HttpServletRequest request) {
		JobPhase newPhase = new JobPhase();
		
		Object devStatus = request.getParameter("DeviceStatus");
		if (devStatus != null) {
			newPhase.deviceStatus = EnumDeviceStatus.getEnum( devStatus.toString() );
		}
		Object devStatusDetails = request.getParameter("DeviceStatusDetails");
		if (devStatusDetails != null) {
			newPhase.deviceStatusDetails = devStatusDetails.toString();
		}
		
		Object nodeStatus = request.getParameter("NodeStatus");
		if (devStatus != null) {
			newPhase.nodeStatus = EnumNodeStatus.getEnum( nodeStatus.toString() );
		}
		Object nodeStatusDetails = request.getParameter("NodeStatusDetails");
		if (nodeStatusDetails != null) {
			newPhase.nodeStatusDetails = nodeStatusDetails.toString();
		}
		
		newPhase.Output_Good = getDoubleFromRequest(request, "Good");
		newPhase.Output_Waste = getDoubleFromRequest(request, "Waste");
		
		return newPhase;
	}

	/**
	 * extract a double attribute from a given request
	 * @param request
	 * @param param
	 * @return
	 */
	private double getDoubleFromRequest(HttpServletRequest request, String param)
	{
		double d = 0.0;
		Object dObj = request.getParameter(param);
		if (dObj != null) {
			try {
				d = Double.valueOf( dObj.toString() ).doubleValue();
			} catch (NumberFormatException ex) {
				log.error("value of attribute '"+param+"' is not a valid double: "+dObj.toString());
			}		
		}	
		return d;
	}

}
