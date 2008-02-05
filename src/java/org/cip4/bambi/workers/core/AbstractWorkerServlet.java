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

package org.cip4.bambi.workers.core;

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
	public static final Log log = LogFactory.getLog(AbstractWorkerServlet.class.getName());

	/** Initializes the servlet.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
		String configFile=_devProperties.getConfigDir()+"devices.xml";
		createDevicesFromFile(configFile);
        
	}

	/** 
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo() 
	{
		return "Bambi Device Worker Servlet";
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
    protected void showDevice(HttpServletRequest request,
            HttpServletResponse response) {
        try {
            request.getRequestDispatcher("DeviceInfo").forward(request, response);
        } catch (Exception e) {
            log.error(e);
        }
    }

}
