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
package org.cip4.bambi.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;


/**
 * container for the properties of several Bambi devices
 * 
 * @author boegerni
 */
public class MultiDeviceProperties implements IMultiDeviceProperties
{
	/**
	 * properties for a single device
	 * @author boegerni
	 *
	 */
	public static class DeviceProperties implements IDeviceProperties {
		private String _deviceID=null;
		private String _deviceURL=null;
		private String _proxyURL=null;
		private String _deviceType=null;
        private String _appDir=null;
        private String _hotFolder=null;
        private String _baseDir=null;
        private String _configDir=null;
        private String _jdfDir=null;
		
		/**
		 * create device properties
		 * @param theDeviceID   the ID of the device
		 * @param theDeviceURL  the URL of the device
		 * @param theProxyURL   the URL of its proxy/controller
		 * @param theDeviceType the device type (e.g. "General Foo Device")
		 * @param theAppDir     the location of the web app (e.g. "C:/tomcat/webapps/bambi")
		 * @param theHotFolder  the location of the hotfolder 
		 */
		public DeviceProperties(String theDeviceID, String theDeviceURL, String theProxyURL,
				String theDeviceType, String theAppDir, String theHotFolder) {
			setDeviceID(theDeviceID);
			setDeviceURL(theDeviceURL);
			setProxyURL(theProxyURL);
			setDeviceType(theDeviceType);
			setAppDir(theAppDir);
            _hotFolder=theHotFolder;
		}
		
		/**
		 * constructor
		 */
		public DeviceProperties() {
			
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#setDeviceURL(java.lang.String)
		 */
		public void setDeviceURL(String deviceURL) {
			this._deviceURL = deviceURL;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
		 */
		public String getDeviceURL() {
			return _deviceURL;
		}

		/**
		 * @param deviceID the deviceID to set
		 */
		private void setDeviceID(String deviceID) {
			this._deviceID = deviceID;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
		 */
		public String getDeviceID() {
			return _deviceID;
		}

		/**
		 * @param controllerURL the controllerURL to set
		 */
		private void setProxyURL(String controllerURL) {
			this._proxyURL = controllerURL;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getProxyURL()
		 */
		public String getProxyURL() {
			return _proxyURL;
		}

		/**
		 * @param deviceType the deviceType to set
		 */
		private void setDeviceType(String deviceType) {
			this._deviceType = deviceType;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
		 */
		public String getDeviceType() {
			return _deviceType;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#setAppDir(java.lang.String)
		 */
		public void setAppDir(String appDir) {
			this._appDir = appDir;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getAppDir()
		 */
		public String getAppDir() {
			return _appDir;
		}
		
		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#toString()
		 */
		@Override
		public String toString() {
			String ret="["+this.getClass().getName()+" application directory="+_appDir+", device ID="+
				_deviceID+", device type="+_deviceType+
				", device URL="+_deviceURL+", proxy URL="+_proxyURL+"]";
			return ret;
		}

        /* (non-Javadoc)
         * @see org.cip4.bambi.core.IDeviceProperties#getHotFolderURL()
         */
        public String getHotFolderURL()
        {
           return _hotFolder;
        }

		public String getBaseDir() {
			return _baseDir;
		}

		public String getConfigDir() {
			return _configDir;
		}

		public String getJDFDir() {
			return _jdfDir;
		}

		public void setBaseDir(String baseDir) {
			_baseDir=baseDir;
		}

		public void setConfigDir(String configDir) {
			_configDir=configDir;
		}

		public void setJDFDir(String jdfDir) {
			_jdfDir=jdfDir;
		}
	}
	
	protected static final Log log = LogFactory.getLog(MultiDeviceProperties.class.getName());
	private Map<String, DeviceProperties> _devices=null;

	/**
	 * create device properties for the devices defined in the config file
	 * @param appDir     the location of the web application in the server
	 * @param configFile the config file
	 */
	public MultiDeviceProperties(String appDir, String configFile) {
		_devices = new HashMap<String, DeviceProperties>();
		
		if (configFile==null || configFile.equals("")) {
			log.fatal("path to config file is null");
			return;
		}
		
		JDFParser p = new JDFParser();
	    JDFDoc doc = p.parseFile(configFile);
	    if (doc == null) {
	    	log.fatal( "failed to parse "+configFile );
	    	return;
	    }
	    
	    KElement e = doc.getRoot();
	    if (e==null) {
	    	log.fatal( "failed to parse "+configFile+", root is null" );
	    	return;
	    }
	    VElement v = e.getXPathElementVector("//devices/*", 99);
	    for (int i = 0; i < v.size(); i++)
	    {
	    	KElement device = v.elementAt(i);
	    	String deviceID = device.getXPathAttribute("@DeviceID", null);
	    	if (deviceID==null) {
	    		log.error("cannot create device without device ID");
	    		break;
	    	}
            final String deviceType = device.getXPathAttribute("@DeviceType", null);
            final String proxyURL = device.getXPathAttribute("@ProxyURL", null);
            final String deviceURL= device.getXPathAttribute("@DeviceURL", null);
            final String hotFolderURL= device.getXPathAttribute("@HotFolderURL", null);

	    	DeviceProperties dev = new DeviceProperties(deviceID,deviceURL,proxyURL,
	    			deviceType,appDir,hotFolderURL);
	    	_devices.put(deviceID, dev);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IMultiDeviceProperties#count()
	 */
	public int count() {
		return _devices.size();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IMultiDeviceProperties#getDeviceIDs()
	 */
	public Set<String> getDeviceIDs() {
		return _devices.keySet();
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IMultiDeviceProperties#getDevice(java.lang.String)
	 */
	public IDeviceProperties getDevice(String deviceID) {
		return _devices.get(deviceID);
	}
	
	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IMultiDeviceProperties#toString()
	 */
	@Override
	public String toString() {
		String ret="["+this.getClass().getName()+" Size="+_devices.size()+" DeviceProperties=[";
		
		Set<String> keys=_devices.keySet();
		Iterator<String> it=keys.iterator();
		StringBuffer buf=new StringBuffer(ret);
		while (it.hasNext()) {
			String key=it.next();
			IDeviceProperties prop=_devices.get(key);
			buf.append(prop.toString());
			buf.append(" ");
		}
		ret=new String(buf);
		ret+="] ]";
		return ret;
	}

}