/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2016 The International Cooperation for the Integration of 
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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.server.BambiServer;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.JDFException;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.file.UserDir;
import org.cip4.jdflib.util.thread.DelayedPersist;
import org.cip4.jdflib.util.thread.IPersistable;

/**
 * container for the properties of several Bambi devices
 * @author boegerni
 */
public class MultiDeviceProperties extends BambiLogFactory implements IPersistable
{
	/**
	 * properties for a single device
	 * @author boegerni
	 */
	protected KElement root;
	protected String context;

	private static final File DEVICES_CONFIG_FILE = new File("config/devices.xml");

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 13.02.2009
	 */
	public class DeviceProperties implements IDeviceProperties, IPersistable
	{
		/**
		 * constructor
		 */
		protected KElement devRoot;

		protected DeviceProperties(final KElement _devRoot)
		{
			devRoot = _devRoot;
		}

		/**
		 * @return
		 */
		public KElement getDevRoot()
		{
			return devRoot;
		}

		/**
		 * @return
		 */
		public KElement getRoot()
		{
			return root;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
		 */
		@Override
		public String getDeviceURL()
		{
			return getContextURL() + "/jmf/" + getDeviceID();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
		 */
		@Override
		public IConverterCallback getCallBackClass()
		{
			final String _callBackName = getCallBackClassName();

			if (_callBackName != null)
			{
				try
				{
					final Class<?> c = Class.forName(_callBackName);
					return (IConverterCallback) c.newInstance();
				}
				catch (final Throwable x)
				{
					log.error("Cannot instantiate callback class: " + _callBackName);
				}
			}
			return null;
		}

		/**
		 * @return the name of the callback class, null if no callback is requested
		 */
		public String getCallBackClassName()
		{
			return getCallBackClassName("CallBackName");
		}

		/**
		 * @param callbackType 
		 * @return the name of the callback class, null if no callback is requested
		 */
		protected String getCallBackClassName(String callbackType)
		{
			String name = devRoot.getAttribute(callbackType, null, null);
			if (name == null && !"null".equalsIgnoreCase(name))
			{
				name = root.getAttribute(callbackType, null, null);
			}
			if ("null".equalsIgnoreCase(name))
			{
				name = null;
			}
			return StringUtil.getNonEmpty(name);
		}

		/**
		 * @param callbackName
		 */
		public void setCallBackClassName(String callbackName)
		{
			setDeviceAttribute("CallBackName", callbackName);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceInstance()
		 * @return the device instance
		 */
		@Override
		public AbstractDevice getDeviceInstance()
		{
			final String _deviceName = getDeviceClassName();
			if (_deviceName != null)
			{
				try
				{
					final Class<?> c = Class.forName(_deviceName);
					final Constructor<?> con = c.getConstructor(new Class[] { IDeviceProperties.class });
					return (AbstractDevice) con.newInstance(new Object[] { this });
				}
				catch (final Throwable x)
				{
					log.fatal("Cannot instantiate Device class: " + _deviceName, x);
				}
			}
			log.fatal("Cannot instantiate null Device class - set correct device class name ");
			return null;
		}

		/**
		 * @param deviceClass
		 */
		public void setDeviceClassName(final String deviceClass)
		{
			setDeviceAttribute("DeviceClass", deviceClass);
		}

		/**
		 * @return
		 */
		public String getDeviceClassName()
		{
			return getDeviceAttribute("DeviceClass", null, "org.cip4.bambi.workers.sim.SimDevice");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
		 */
		@Override
		public String getDeviceID()
		{
			return getDeviceAttribute("DeviceID", null, null);
		}

		/**
		 * @param deviceID the deviceID to set
		 * 
		 */
		public void setDeviceID(String deviceID)
		{
			setDeviceAttribute("DeviceID", deviceID);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getProxyControllerURL()
		 */
		@Override
		public String getProxyControllerURL()
		{
			return getDeviceAttribute("ProxyURL", null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
		 */
		@Override
		public String getDeviceType()
		{
			return getDeviceAttribute("DeviceType", null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setDeviceType(java.lang.String)
		 */
		@Override
		public void setDeviceType(final String deviceType)
		{
			setDeviceAttribute("DeviceType", deviceType);
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDescription()
		 */
		@Override
		public String getDescription()
		{
			String deviceAttribute = getDeviceAttribute("Description", null, null);
			if (deviceAttribute == null)
				deviceAttribute = getDeviceType() + " " + getDeviceID();
			return deviceAttribute;
		}

		/**
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#setDescription(java.lang.String)
		 */
		@Override
		public void setDescription(final String description)
		{
			setDeviceAttribute("Description", description);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#toString()
		 */
		@Override
		public String toString()
		{
			return "[ DeviceProperties: " + (devRoot == null ? "null" : devRoot.toString()) + "]";
		}

		/**
		 * @param attribute
		 * @return
		 */
		protected File getFile(final String attribute)
		{
			final String fil = devRoot.getAttribute(attribute, null, null);
			return fil == null ? getRootFile(attribute) : new File(fil);
		}

		/**
		 * @param attribute the attribute to set the file as
		 * @param file 
		 */
		protected void setFile(final String attribute, final File file)
		{
			final String fil = file == null ? null : file.getPath();
			setDeviceAttribute(attribute, fil);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getErrorHF()
		 */
		@Override
		public File getErrorHF()
		{
			return getFile("ErrorHF");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setErrorHF(java.io.File)
		 */
		@Override
		public void setErrorHF(final File hf)
		{
			setFile("ErrorHF", hf);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getOutputHF()
		 */
		@Override
		public File getOutputHF()
		{
			return getFile("OutputHF");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setOutputHF(java.io.File)
		 */
		@Override
		public void setOutputHF(final File hf)
		{
			setFile("OutputHF", hf);
		}

		/**
		 * @return a vector of ICS versions
		 */
		public VString getICSVersions()
		{
			return StringUtil.tokenize(getDeviceAttribute(AttributeName.ICSVERSIONS), null, false);
		}

		/**
		 * @return the input hot folder of the device
		 */
		@Override
		public File getInputHF()
		{
			return getFile("InputHF");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setInputHF(java.io.File)
		 */
		@Override
		public void setInputHF(final File hf)
		{
			setFile("InputHF", hf);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
		 */
		@Override
		public File getBaseDir()
		{
			return MultiDeviceProperties.this.getBaseDir();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getAppDir()
		 */
		@Override
		public File getAppDir()
		{
			return MultiDeviceProperties.this.getAppDir();
		}

		/**
		 * get the tracked resource - defaults to "Output"
		 * @see org.cip4.bambi.core.IDeviceProperties#getTrackResource()
		 */
		@Override
		public String getTrackResource()
		{
			return getDeviceAttribute("TrackResource", null, "Output");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceAttribute(java.lang.String)
		 * @param key
		 * @return the device attribute, null if none exists
		 */
		@Override
		public String getDeviceAttribute(final String key)
		{
			return getDeviceAttribute(key, null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceAttribute(java.lang.String)
		 * @param key
		 * @param ifNotSet the default value
		 * @return true if the option is set
		 */
		public boolean hasDeviceOption(final String key, boolean ifNotSet)
		{
			return StringUtil.parseBoolean(getDeviceAttribute(key), ifNotSet);
		}

		/**
		 * @param key
		 * @param ns
		 * @param def the default if not found
		 * @return the device attribute
		 */
		public String getDeviceAttribute(final String key, final String ns, final String def)
		{
			String val = devRoot.getAttribute(key, ns, null);
			if (val == null)
			{
				val = root.getAttribute(key, ns, def);
			}
			return val;
		}

		/**
		 * @param key
		 * @param val the value to set
		 *  
		 */
		public void setDeviceAttribute(final String key, final String val)
		{
			devRoot.setAttribute(key, val);
			DelayedPersist.getDelayedPersist().queue(this, 5000);
		}

		/**
		 * @param xpath the element relative xpath
		 * @return the device attribute
		 */
		public KElement getDeviceElement(final String xpath)
		{
			KElement el = devRoot.getXPathElement(xpath);
			if (el == null)
			{
				el = root.getXPathElement(xpath);
			}
			return el;

		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getTypeExpression()
		 */
		@Override
		public String getTypeExpression()
		{
			return getDeviceAttribute(AttributeName.TYPEEXPRESSION);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setTypeExpression(java.lang.String)
		 */
		@Override
		public void setTypeExpression(final String exp)
		{
			setDeviceAttribute(AttributeName.TYPEEXPRESSION, exp);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getAmountResources()
		 */
		@Override
		public VString getAmountResources()
		{
			final VString v = StringUtil.tokenize(getDeviceAttribute("AmountResources", null, null), ",", false);
			final String trackResource = getTrackResource();
			if (v == null)
			{
				return StringUtil.tokenize(trackResource, null, false);
			}
			v.appendUnique(trackResource);
			return v;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerHTTPChunk()
		 */
		@Override
		public int getControllerHTTPChunk()
		{
			return StringUtil.parseInt(getDeviceAttribute("HTTPChunk"), -1);
		}

		/**
		 * @return true if all jdfs should be accepted (ignore canAccept)
		*
		 */
		@Override
		public boolean getAcceptAll()
		{
			return hasDeviceOption("AcceptAll", false);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEEncoding()
		 */
		@Override
		public String getControllerMIMEEncoding()
		{
			return getDeviceAttribute("MIMETransferEncoding", null, UrlUtil.BINARY);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEExpansion()
		 */
		@Override
		public boolean getControllerMIMEExpansion()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("MIMEExpansion"), false);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getReturnMIME()
		 */
		@Override
		public QEReturn getReturnMIME()
		{
			final String s = getDeviceAttribute("MIMEReturn", null, "MIME");
			try
			{
				return QEReturn.valueOf(s);
			}
			catch (final Throwable x)
			{
				return QEReturn.MIME;
			}
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getContextURL()
		 */
		@Override
		public String getContextURL()
		{
			return MultiDeviceProperties.this.getContextURL();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getWatchURL()
		 */
		@Override
		public String getWatchURL()
		{
			return getDeviceAttribute("WatchURL");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setWatchURL(java.lang.String)
		 */
		@Override
		public void setWatchURL(final String watchURL)
		{
			setDeviceAttribute("WatchURL", watchURL);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getConfigDir()
		 * @return the configuration directory
		 */
		@Override
		public File getConfigDir()
		{
			return MultiDeviceProperties.this.getConfigDir();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#serialize()
		 */
		@Override
		public boolean serialize()
		{
			MultiDeviceProperties.this.serialize();
			return true;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getQERetrieval()
		 */
		@Override
		public QERetrieval getQERetrieval()
		{
			final String deviceAttribute = getDeviceAttribute("PushPull", null, "PUSH");
			try
			{
				return QERetrieval.valueOf(deviceAttribute);
			}
			catch (final IllegalArgumentException x)
			{
				return QERetrieval.PUSH;
			}
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setQERetrieval(org.cip4.bambi.core.IDeviceProperties.QERetrieval)
		 */
		@Override
		public void setQERetrieval(final QERetrieval qer)
		{
			final String s = qer == null ? "PUSH" : qer.name();
			setDeviceAttribute("PushPull", s);
		}

		/**
		 * @see org.cip4.jdflib.util.thread.IPersistable#persist()
		 */
		@Override
		public boolean persist()
		{
			return MultiDeviceProperties.this.persist();
		}

		/**
		 * defaults to true for legacy
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getAutoStart()
		 */
		@Override
		public boolean getAutoStart()
		{
			return hasDeviceOption("AutoStart", true);
		}

		/**
		 * if true, the we add ourselves to the template list and can create new devices
		 * 
		 * defaults to true for legacy
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getAutoStart()
		 */
		public boolean isTemplate()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("Template"), true);
		}

		/**
		 * set the autostart property
		 * 
		 *
		 */
		public void setAutoStart(boolean bAutoStart)
		{
			setDeviceAttribute("AutoStart", "" + bAutoStart);
		}

		/**
		 * clones this inactive DeviceProperties and sets clone to active
		 * @param deviceID the deviceID of the new device
		 * @return a new DeviceProperties with AutoStart=true
		 */
		public DeviceProperties activateDeviceProps(String deviceID)
		{
			KElement newDevElem = root.copyElement(devRoot, null);
			DeviceProperties newProps = createDeviceProps(newDevElem);
			newProps.setDeviceID(deviceID);
			newProps.setAutoStart(true);
			devRoot.getOwnerDocument_KElement().write2File((String) null, 2, false);
			return newProps;
		}

		/**
		 * 
		 * @return
		 */
		public MultiDeviceProperties getParent()
		{
			return MultiDeviceProperties.this;
		}
	}

	/**
	 * gets a subclass of this based on the value of application/@PropertiesName
	 * @param config 
	 * @param
	 * 
	 * @return the subclass instance, this if @PropertiesName is not set
	 */
	private MultiDeviceProperties getSubClass()
	{
		String propName = root.getAttribute("PropertiesName", null, null);
		if (propName == null)
		{
			return this;
		}

		try
		{
			final Class<?> c = Class.forName(propName);
			final Constructor<?> con = c.getConstructor(new Class[] { XMLDoc.class });
			MultiDeviceProperties subClass = (MultiDeviceProperties) con.newInstance(new Object[] { root.getOwnerDocument_KElement() });
			subClass.context = context;
			return subClass;
		}
		catch (final Throwable x)
		{
			log.error("Cannot instantiate Device properties: " + propName, x);
			return this;
		}
	}

	/**
	 * create device properties for the devices defined in the config file
	 * this is for tests only
	 * @param baseDir 
	 * @param baseURL 
	 */
	public MultiDeviceProperties(File baseDir)
	{
		this.context = "test";
		root = new XMLDoc("application", null).getRoot();
		root.setAttribute("AppDir", baseDir.getAbsolutePath());
		root.setAttribute("BaseDir", baseDir.getAbsolutePath());
	}

	/**
	 * Create device properties for the devices defined in the config file.
	 */
	public static MultiDeviceProperties getProperties(File appDir, String baseURL)
	{
		// to evaluate current name and send it back rather than 127.0.0.1
		final XMLDoc installDoc = getXMLDoc(appDir);
		Log sLog = LogFactory.getLog(MultiDeviceProperties.class);

		if (installDoc == null)
		{
			sLog.fatal("Failed to parse " + DEVICES_CONFIG_FILE + " at " + getConfigFile(appDir).getAbsolutePath() + ", rootDev is null");
			throw new JDFException("Failed to parse " + DEVICES_CONFIG_FILE + " at " + getConfigFile(appDir).getAbsolutePath() + ", rootDev is null");
		}

		String appPath = appDir.getAbsolutePath();
		MultiDeviceProperties installProps = new MultiDeviceProperties(installDoc).getSubClass();
		final File deviceDir = installProps.getBaseDir();
		final File localConfigFile = getConfigFile(deviceDir);
		final XMLDoc localDoc = XMLDoc.parseFile(localConfigFile);
		boolean mustCopy = deviceDir != null;
		if (localDoc != null) // using config default
		{
			MultiDeviceProperties localProps = new MultiDeviceProperties(localDoc).getSubClass();
			if (installProps.isCompatible(localProps))
			{
				installProps = localProps;
				sLog.info("using updated device config from: " + localConfigFile.getAbsolutePath());
				mustCopy = false;
			}
			else
			{
				sLog.warn("overwriting incompatible device config in: " + localConfigFile.getAbsolutePath());
			}
		}
		if (mustCopy)
		// using webapp devices
		{
			sLog.info("copying to local device config file: " + localConfigFile.getAbsolutePath());
			deviceDir.mkdirs();
			installDoc.setOriginalFileName(localConfigFile.getAbsolutePath());
			installProps.serialize();
		}

		installProps.context = baseURL;
		installProps.root.setAttribute("AppDir", appPath);

		return installProps;
	}

	/**
	 * @param mp2
	 * @return
	 */
	protected boolean isCompatible(MultiDeviceProperties mp2)
	{
		if (mp2 == null)
			return false;
		final String configVersion = getConfigVersion();
		return configVersion.equals(mp2.getConfigVersion());
	}

	/**
	 * @return a version string - should never be null
	 */
	private String getConfigVersion()
	{
		return root.getAttribute("ConfigVersion");
	}

	/**
	 * 
	 * @param appDir
	 * @return
	 */
	public static XMLDoc getXMLDoc(File appDir)
	{
		final XMLDoc doc = XMLDoc.parseFile(getConfigFile(appDir));
		return doc;
	}

	/**
	 * 
	 * @param appDir
	 * @return
	 */
	public static File getConfigFile(File appDir)
	{
		return FileUtil.getFileInDirectory(appDir, DEVICES_CONFIG_FILE);
	}

	/**
	 * create device properties for the devices defined in the config stream
	 * @param inStream
	 */
	private MultiDeviceProperties(InputStream inStream)
	{
		this(XMLDoc.parseStream(inStream));
	}

	/**
	 * 
	 * @param doc
	 */
	protected MultiDeviceProperties(final XMLDoc doc)
	{
		root = doc == null ? null : doc.getRoot();
		this.context = null;

		if (root == null)
		{
			log.fatal("failed to parse internal stream, rootDev is null");
		}
		else if (StringUtil.getNonEmpty(doc.getOriginalFileName()) == null)
		{
			String appDir = System.getProperty("user.dir");
			root.setAttribute("AppDir", appDir);
			root.getOwnerDocument_KElement().setOriginalFileName(getConfigFile(new File(appDir)).getAbsolutePath());
		}
	}

	/**
	 * serialize this to it's default location
	 * @return true if success
	 */
	public void serialize()
	{
		DelayedPersist.getDelayedPersist().queue(this, 333);
	}

	/**
	 * @return the setup port, in case we have a jetty server, return the port from there
	 */
	public int getPort()
	{
		int p = root.getIntAttribute("Port", null, 0);
		if (p == 0)
		{
			p = root.getIntAttribute("JettyPort", null, 0);
		}
		if (p == 0)
		{
			p = 8080; // better guess - default tomcat Port
			log.warn("guessing default port - using " + p);
		}
		return p;
	}

	/**
	 * 
	 * @param port
	 */
	public void setPort(int port)
	{
		if (getPort() != port && port > 0)
		{
			root.setAttribute("Port", port, null);
			serialize();
		}
	}

	/**
	 * @return true if we currently want to dump
	 */
	public boolean wantDump()
	{
		return root.getBoolAttribute("Dump", null, true);
	}

	/**
	 * @return the number of device elements
	 */
	public int count()
	{
		return root.numChildElements(ElementName.DEVICE, null);
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		return "[ MultiDeviceProperties: " + getContextURL() + "\n" + root + "]";
	}

	/**
	 * @return 
	 * @see org.cip4.bambi.core.IDeviceProperties#getContextURL()
	 */
	public String getContextURL()
	{
		return "http://" + getHostName() + ":" + getPort() + "/" + context;
	}

	/**
	 * @return the hostname; if network snafu either the hard coded host ip or "localhost"
	 * @see org.cip4.bambi.core.IDeviceProperties#getContextURL()
	 */
	public String getHostName()
	{
		String hostName = null;
		try
		{
			final InetAddress localHost = InetAddress.getLocalHost();
			hostName = localHost.getHostName();
		}
		catch (final UnknownHostException x1)
		{
			hostName = StringUtil.getNonEmpty(root.getAttribute("host", null, null));
			if (hostName == null)
			{
				hostName = "localhost";
			}
			log.error("network setup looks sub-optimal - using; " + hostName, x1);
		}
		return hostName;
	}

	/**
	 * @return the application directory
	 */
	public File getAppDir()
	{
		return getRootFile("AppDir");
	}

	/**
	 * @return the original configuration directory
	 */
	public File getConfigDir()
	{
		final File f = getAppDir();
		return FileUtil.getFileInDirectory(f, new File("config"));
	}

	/**
	 * 
	 * @return
	 */
	public String getCSS()
	{
		return root.getAttribute("CSS", null, "/legacy");
	}

	/**
	 * 
	 * @return
	 */
	public void setCSS(String css)
	{
		root.setAttribute("CSS", css, null);
	}

	/**
	 * get the base directory for data
	 * @return the base directory for data
	 */
	public File getBaseDir()
	{
		UserDir userDir = new UserDir(BambiServer.BAMBI);

		File f1 = getRootFile("BaseDir");
		File f = new File(userDir.getToolPath() + f1.getAbsolutePath());

		if (!FileUtil.isAbsoluteFile(f))
		{
			final File fBase = getAppDir();
			f = FileUtil.getFileInDirectory(fBase, f);
		}
		return f;
	}

	/**
	 * 
	 * @param newBase
	 */
	public void setBaseDir(File newBase)
	{
		if (newBase != null && !newBase.getAbsolutePath().equals(root.getAttribute("BaseDir")))
		{
			log.info("Setting base directory to: " + newBase.getAbsolutePath());
			root.setAttribute("BaseDir", newBase.getAbsolutePath());
			serialize();
		}
	}

	/**
	 * @param file the file type to search
	 * @return a File representing the directory
	 */
	protected File getRootFile(final String file)
	{
		final String fil = root.getAttribute(file, null, null);
		return fil == null ? null : new File(fil);
	}

	/**
	 * @return the sender ID...
	 */
	public String getSenderID()
	{
		return root.getAttribute(AttributeName.SENDERID);
	}

	/**
	 * @param senderID the sender ID...
	 */
	public void setSenderID(String senderID)
	{
		root.setAttribute(AttributeName.SENDERID, senderID);
		serialize();
	}

	/**
	 * @return the sender ID...
	 */
	public EnumVersion getJDFVersion()
	{
		EnumVersion v = EnumVersion.getEnum(root.getAttribute(AttributeName.VERSION));
		if (v == null)
		{
			v = EnumVersion.Version_1_3;
		}
		return v;
	}

	/**
	 * @return the vector of device elements
	 */
	public VElement getDevices()
	{
		return root.getChildElementVector(ElementName.DEVICE, null);
	}

	/**
	 * @return the root application node
	 */
	public KElement getRoot()
	{
		return root;
	}

	/**
	 * @param element the xml element to parse, if null an empty element is created
	 * @return a IDeviceProperties parsed from the element
	 */
	public DeviceProperties createDeviceProps(KElement element)
	{
		if (element == null)
			element = root.appendElement(ElementName.DEVICE);
		return this.new DeviceProperties(element);
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean persist()
	{
		return root.getOwnerDocument_KElement().write2File((String) null, 2, false);
	}

	/**
	 * 
	 * @param resourceAsStream
	 * @return
	 */
	public static MultiDeviceProperties getProperties(InputStream resourceAsStream)
	{
		MultiDeviceProperties multiDeviceProperties = new MultiDeviceProperties(resourceAsStream);
		return multiDeviceProperties.getSubClass();
	}
}