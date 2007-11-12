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

package org.cip4.bambi.proxy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IMultiDeviceProperties;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.queues.QueueFacade;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 * This servlet is the main entrance point for Bambi proxies. <br>
 * Its purpose is to keep track of and forward messages to ProxyDevices.
 * 
 * @author rainer, niels
 */
public class ProxyServlet extends AbstractBambiServlet implements IDevice 
{
	private static final long serialVersionUID = -8902151736245089036L;
	private static Log log = LogFactory.getLog(ProxyServlet.class.getName());
	
	/** Initializes the servlet.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		ServletContext context = config.getServletContext();
		log.info( "Initializing servlet for "+context.getServletContextName() );
        _devices=new HashMap<String, IDevice>();
        createDevices();
	}

	/** Destroys the servlet.
	 */
	@Override
	public void destroy() {
		Set<String> keys=_devices.keySet();
		Iterator<String> it=keys.iterator();
		while (it.hasNext()) {
			String devID=it.next().toString();
			ProxyDevice dev=(ProxyDevice) _devices.get(devID);
			dev.shutdown();
		}
	}


	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		log.info("Processing get request...");
		String command = request.getParameter("cmd");
		
		if ( command==null||command.length()==0 )  {
			try {
				request.setAttribute("devices", _devices);
				request.getRequestDispatcher("/overview.jsp").forward(request, response);
			} catch (Exception e) {
				log.error(e);
			} 
		} else if ( command.equals("showQueue") )  {
			String devID=request.getParameter("devID");
			ProxyDevice dev=(ProxyDevice) _devices.get(devID);
			if (dev==null) {
				String errorMsg="illegal DeviceID '"+devID+"'";
				log.error( errorMsg );
				showErrorPage(errorMsg, "The DeviceID '"+devID+"' is unknown", 
						request, response);
				return;
			}
			QueueFacade bqu = dev.getQueueFacade();
			String quStr = bqu.toHTML();
			writeRawResponse(request, response, quStr);
		} else if ( command.equals("showJDFDoc") ) {
			String qeid=request.getParameter("qeid");
			if ( (qeid!=null&&qeid.length()>0) ) {
				String filePath=_jdfDir+qeid+".jdf";
				JDFDoc theDoc=JDFDoc.parseFile(filePath);
				if (theDoc!=null) {
					writeRawResponse( request,response,theDoc.toXML() );
				} else {
					log.error( "cannot parse '"+filePath+"'" );
					return;
				}
			}
		}
	}

	@Override
	protected void processJMFDoc(HttpServletRequest request,
			HttpServletResponse response, JDFDoc jmfDoc) {
		if(jmfDoc==null) {
			processError(request, response, null, 3, "Error Parsing JMF");
		} else {
			// switch: sends the jmfDoc to correct device
			JDFDoc responseJMF = null;
			IJMFHandler handler = getTargetHandler(request);
			if (handler != null) {
				responseJMF=handler.processJMF(jmfDoc);
			} 
			
			if (responseJMF!=null) {
				response.setContentType(MimeUtil.VND_JMF);
				try {
					responseJMF.write2Stream(response.getOutputStream(), 0, true);
				} catch (IOException e) {
					log.error("cannot write to stream: ",e);
				}
			} else {
				processError(request, response, null, 3, "Error Parsing JMF");               
			}
		}
	}

	/** 
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() {
		return "Bambi Proxy Servlet";
	}
	
	@Override
	public String toString() {
		String ret ="[ BambiProxy - DeviceID="+_deviceID+", DeviceURL="+_deviceURL+", AppDir="
			+_appDir+" ]";
		return ret;
	}
	
	@Override
	protected boolean handleKnownDevices(JDFMessage m, JDFResponse resp) {
		if(m==null || resp==null)
		{
			return false;
		}
//		log.info("Handling "+m.getType());
		EnumType typ=m.getEnumType();
		if(EnumType.KnownDevices.equals(typ)) {
			JDFDeviceList dl = resp.appendDeviceList();
			Set<String> keys = _devices.keySet();
			Object[] strKeys = keys.toArray();
			for (int i=0; i<keys.size();i++) {
				String key = (String)strKeys[i];
				ProxyDevice dev = (ProxyDevice) _devices.get(key);
				if (dev == null)
					log.error("device with key '"+key+"'not found");
				else
					dev.appendDeviceInfo(dl);
			}
			return true;
		}

		return false;
	}
	
	private void createDevices() {
		File configFile=new File(_configDir+"devices.xml");
		createDevicesFromFile(configFile);
	}
	
	/**
     * create devices based on the list of devices given in a file
     * @param configFile the file containing the list of devices 
     * @return true if successfull, otherwise false
     */
	public boolean createDevicesFromFile(File configFile)
	{
		IMultiDeviceProperties dv = new MultiDeviceProperties(_appDir, configFile);
		if (dv.count()==0) {
			log.error("failed to load device properties from "+configFile);
			return false;
		}
		
		Set<String> keys=dv.getDeviceIDs();
		Iterator<String> iter=keys.iterator();
		while (iter.hasNext()) {
			String devID=iter.next().toString();
			IDeviceProperties prop=dv.getDevice(devID);
//			prop.setDeviceURL(_deviceURL+"/"+devID);
			createDevice(prop);
		}

		return true;
	}
	
	/**
	 * create a new device and add it to the map of devices.
	 * @param deviceID
	 * @param deviceType
	 * @return the Device, if device has been created. 
	 * null, if not (maybe device with deviceID is already present)
	 */
	public IDevice createDevice(IDeviceProperties prop)
	{
		if (_devices == null) {
			log.info("map of devices is null, re-initialising map...");
			_devices = new HashMap<String, IDevice>();
		}
		
		String devID=prop.getDeviceID();
		if (_devices.get(prop.getDeviceID()) != null) {	
			log.warn("device "+devID+" is already existing");
			return null;
		}
		ProxyDevice dev = new ProxyDevice(prop);
		_devices.put(devID,dev);
		log.info("created device "+devID);
		return dev;
	}
	
	private IJMFHandler getTargetHandler(HttpServletRequest request) {
		String deviceID = request.getPathInfo();
		if (deviceID == null)
			return _jmfHandler; // root folder
		deviceID = StringUtil.token(deviceID, 0, "/");
		if (deviceID == null)
			return _jmfHandler; // device not found
		ProxyDevice device = (ProxyDevice) _devices.get(deviceID);
		if (device == null)
			return _jmfHandler; // device not found
		return( device.getHandler() );
	}
}
