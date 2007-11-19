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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractBambiServlet;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.IDevice;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IMultiDeviceProperties;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;


/**
 * This is main entrance point of a Bambi worker servlet. A worker servlet contains 
 * worker devices (aka workers).<br>
 * Its workers are defined in <code>/WebContent/config/devices.xml</code>.
 * It does not handle QueueEntries. Processing is done by
 * the workers defined in <code>/WebContent/config/devices.xml</code>.
 * @author niels
 */
public abstract class AbstractWorkerServlet extends AbstractBambiServlet 
{
	protected static final long serialVersionUID = -8902151736245089036L;
	protected static final Log log = LogFactory.getLog(AbstractWorkerServlet.class.getName());

	/** Initializes the servlet.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		String configFile=_devProperties.getConfigDir()+"devices.xml";
		createDevicesFromFile(configFile);
	}

	/** Destroys the servlet.
	 */
	@Override
	public void destroy() {
		Set<String> keys=_devices.keySet();
		Iterator<String> it=keys.iterator();
		while (it.hasNext()) {
			String devID=it.next();
			AbstractDevice dev=(AbstractDevice) _devices.get(devID);
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
		
		if (command == null || command.length() == 0) {
			request.setAttribute("devices", getDevices());
			try {
				request.getRequestDispatcher("/overview.jsp").forward(request, response);
			} catch (Exception e) {
				log.error(e);
			} 
		} else if ( command.equals("showDevice") || 
				command.equals("processNextPhase") || command.equals("finalizeCurrentQE") )
		{
			IDevice dev=getDeviceFromRequest(request);
			if (dev!=null)
			{
				request.setAttribute("device", dev);
				showDevice(request,response);
			} else {
				showErrorPage("can't get device", "device ID missing or unknown", request, response);
				return;
			}
		} else if ( command.endsWith("QueueEntry") )  {
			IDevice dev=getDeviceFromRequest(request);
			if (dev!=null)
			{
				request.setAttribute("device", dev);
				try {
					request.getRequestDispatcher("QEController").forward(request, response);
				} catch (Exception e) {
					log.error(e);
				}
			} else {
				log.error("can't get device, device ID is missing or unknown");
			}
		} else if ( command.equals("showJDFDoc") ) {
			String qeid=request.getParameter("qeid");
			if ( (qeid!=null&&qeid.length()>0) ) {
				String filePath=_devProperties.getJDFDir()+qeid+".jdf";
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

	/**
	 * @param request
	 */
	protected IDevice getDeviceFromRequest(HttpServletRequest request) {
		String deviceID = request.getParameter("id");
		if (deviceID == null) {
			log.error("invalid request: device ID is missing");
			return null;
		}
		IDevice dev = getDevice(deviceID);
		if (dev == null) {
			log.error("invalid request: device with id="+deviceID+" not found");
			return null;
		}
		
		return dev;
	}

	

	/** 
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() 
	{
		return "Bambi Device  Servlet";
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
		IDevice dev = buildDevice(prop);
		_devices.put(devID,dev);
		log.info("created device "+devID);
		return dev;
	}
	
	/**
	 * remove device
	 * @param deviceID ID of the device to be removed
	 * @return
	 */
	public boolean removeDevice(String deviceID) {
		if (_devices == null) {
			log.error("list of devices is null");
			return false;
		}
		if (_devices.get(deviceID) == null) {
			log.warn("tried to removing non-existing device");
			return false;
		}
		_devices.remove(deviceID);
		return true;
	}

	/**
	 * get the number of devices
	 * @return
	 */
	public int getDeviceQuantity() {
		return _devices==null ? 0 : _devices.size();
	}

	/**
	 * get a device
	 * @param deviceID ID of the device to get
	 * @return
	 */
	public IDevice getDevice(String deviceID)
	{
		if (_devices == null) {
			log.warn("list of devices is null");
			return null;
		}

		return _devices.get(deviceID);
	}
	
	/**
     * add or replace a device in the devicemap
     * 
     * @param deviceID
     * @param device
     */
    public void addDevice(String deviceID, IDevice device)
    {
        if (_devices == null) {
            log.debug("list of devices is null");
            _devices=new HashMap<String, IDevice>();
        }

        _devices.put(deviceID, device);
    }

    /**
     * create devices based on the list of devices given in a file
     * @param configFile the file containing the list of devices 
     * @return true if successfull, otherwise false
     */
	public boolean createDevicesFromFile(String configFile)
	{
		IMultiDeviceProperties dv = new MultiDeviceProperties(_devProperties.getAppDir(), configFile);
		if (dv.count()==0) {
			log.error("failed to load device properties from "+configFile);
			return false;
		}
		
		Set<String> keys=dv.getDeviceIDs();
		Iterator<String> iter=keys.iterator();
		while (iter.hasNext()) {
			String devID=iter.next();
			IDeviceProperties prop=dv.getDevice(devID);
			prop.setAppDir( _devProperties.getAppDir() );
			prop.setBaseDir( _devProperties.getBaseDir() );
			prop.setConfigDir( _devProperties.getConfigDir() );
			prop.setJDFDir( _devProperties.getJDFDir() );
			createDevice(prop);
		}

		return true;
	}
		
	public HashMap<String, IDevice> getDevices() {
		return _devices;
	}
	
	/**
	 * build a new device instance
	 * @param prop
	 * @return
	 */
	protected abstract IDevice buildDevice(IDeviceProperties prop);
	
	/**
	 * display the device on a web page
	 * @param request
	 * @param response
	 */
	protected abstract void showDevice(HttpServletRequest request, HttpServletResponse response);

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
	 * get a device instance from a given Object
	 * @param dev
	 * @return
	 */
	protected abstract AbstractDevice getDeviceFromObject(Object dev);
	
	private IJMFHandler getTargetHandler(HttpServletRequest request) {
		String deviceID = request.getPathInfo();
		if (deviceID == null)
			return _jmfHandler; // root folder
		deviceID = StringUtil.token(deviceID, 0, "/");
		if (deviceID == null)
			return _jmfHandler; // device not found
		AbstractDevice device = getDeviceFromObject( _devices.get(deviceID) );
		if (device == null)
			return _jmfHandler; // device not found
		return( device.getHandler() );
	}
	
	@Override
	protected boolean handleKnownDevices(JDFMessage m, JDFResponse resp) {
		if(m==null || resp==null) {
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
				AbstractDevice dev = getDeviceFromObject(_devices.get(key));
				if (dev == null)
					log.error("device with key '"+key+"'not found");
				else
					dev.appendDeviceInfo(dl);
			}
			return true;
		}

		return false;
	}
}
