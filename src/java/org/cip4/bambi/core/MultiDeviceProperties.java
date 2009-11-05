/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * container for the properties of several Bambi devices
 * @author boegerni
 */
public class MultiDeviceProperties extends BambiLogFactory
{
	/**
	 * properties for a single device
	 * @author boegerni
	 */
	protected KElement root;
	protected File baseDir;
	protected String context;

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 13.02.2009
	 */
	public class DeviceProperties implements IDeviceProperties
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
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
		 */
		public String getDeviceURL()
		{
			return getContextURL() + "/jmf/" + getDeviceID();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
		 */
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
				catch (final Exception x)
				{
					log.error("Cannot instantiate callback class: " + _callBackName);
				}
			}
			return null;
		}

		/**
		 * @return
		 */
		public String getCallBackClassName()
		{
			String name = devRoot.getAttribute("CallBackName", null, null);
			if (name == null)
			{
				name = root.getAttribute("CallBackName", null, null);
			}
			return name;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceInstance()
		 * @return the device instance
		 */
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
				catch (final Exception x)
				{
					log.fatal("Cannot instantiate Device class: " + _deviceName, x);
				}
			}
			log.fatal("Cannot instantiate null Device class - set correct device class name ");
			return null;
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
		public String getDeviceID()
		{
			return getDeviceAttribute("DeviceID", null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getProxyControllerURL()
		 */
		public String getProxyControllerURL()
		{
			return getDeviceAttribute("ProxyURL", null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
		 */
		public String getDeviceType()
		{
			return getDeviceAttribute("DeviceType", null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setDeviceType(java.lang.String)
		 */
		public void setDeviceType(final String deviceType)
		{
			devRoot.setAttribute("DeviceType", deviceType);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#toString()
		 */
		@Override
		public String toString()
		{
			return "[ DeviceProperties: " + devRoot == null ? "null" : devRoot.toString() + "]";
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
		 * @param attribute the attribute to set the fial as
		 */
		protected void setFile(final String attribute, final File file)
		{
			final String fil = file == null ? null : file.getPath();
			devRoot.setAttribute(attribute, fil);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getErrorHF()
		 */
		public File getErrorHF()
		{
			return getFile("ErrorHF");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setErrorHF(java.io.File)
		 */
		public void setErrorHF(final File hf)
		{
			setFile("ErrorHF", hf);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getOutputHF()
		 */
		public File getOutputHF()
		{
			return getFile("OutputHF");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setOutputHF(java.io.File)
		 */
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
		public File getInputHF()
		{
			return getFile("InputHF");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setInputHF(java.io.File)
		 */
		public void setInputHF(final File hf)
		{
			setFile("InputHF", hf);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
		 */
		public File getBaseDir()
		{
			return MultiDeviceProperties.this.getBaseDir();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getAppDir()
		 */
		public File getAppDir()
		{
			return MultiDeviceProperties.this.getAppDir();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getJDFDir()
		 */
		public File getJDFDir()
		{
			final File f = MultiDeviceProperties.this.getJDFDir();
			final String deviceID = getDeviceID();
			if (deviceID == null)
			{
				log.fatal("missing deviceID in Config - bailung out " + toString());
				return null;
			}
			return FileUtil.getFileInDirectory(f, new File(deviceID));
		}

		/**
		 * get the tracked resource - defaults to "Output"
		 * @see org.cip4.bambi.core.IDeviceProperties#getTrackResource()
		 */
		public String getTrackResource()
		{
			return getDeviceAttribute("TrackResource", null, "Output");
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceAttribute(java.lang.String)
		 * @param key
		 * @return the device attribute, null if none exists
		 */
		public String getDeviceAttribute(final String key)
		{
			return getDeviceAttribute(key, null, null);

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
		public String getTypeExpression()
		{
			return getDeviceAttribute(AttributeName.TYPEEXPRESSION);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setTypeExpression(java.lang.String)
		 */
		public void setTypeExpression(final String exp)
		{
			devRoot.setAttribute(AttributeName.TYPEEXPRESSION, exp);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getAmountResources()
		 */
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
		public int getControllerHTTPChunk()
		{
			return StringUtil.parseInt(getDeviceAttribute("HTTPChunk"), -1);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEEncoding()
		 */
		public String getControllerMIMEEncoding()
		{
			return getDeviceAttribute("MIMETransferEncoding", null, UrlUtil.BINARY);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEExpansion()
		 */
		public boolean getControllerMIMEExpansion()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("MIMEExpansion"), false);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getReturnMIME()
		 */
		public QEReturn getReturnMIME()
		{
			final String s = getDeviceAttribute("MIMEReturn", null, "MIME");
			try
			{
				return QEReturn.valueOf(s);
			}
			catch (final Exception x)
			{
				return QEReturn.MIME;
			}
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getContextURL()
		 */
		public String getContextURL()
		{
			return MultiDeviceProperties.this.getContextURL();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getWatchURL()
		 */
		public String getWatchURL()
		{
			return getDeviceAttribute("WatchURL", null, null);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setWatchURL(java.lang.String)
		 */
		public void setWatchURL(final String watchURL)
		{
			devRoot.setAttribute("WatchURL", watchURL);
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getConfigDir()
		 * @return the configuration directory
		 */
		public File getConfigDir()
		{
			return MultiDeviceProperties.this.getConfigDir();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#serialize()
		 */
		public boolean serialize()
		{
			return MultiDeviceProperties.this.serialize();
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getQERetrieval()
		 */
		public QERetrieval getQERetrieval()
		{
			final String deviceAttribute = getDeviceAttribute("PushPull", null, "PUSH");
			try
			{
				return QERetrieval.valueOf(deviceAttribute);
			}
			catch (final IllegalArgumentException x)
			{
				return QERetrieval.PULL;
			}
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setQERetrieval(org.cip4.bambi.core.IDeviceProperties.QERetrieval)
		 */
		public void setQERetrieval(final QERetrieval qer)
		{
			final String s = qer == null ? "PULL" : qer.name();
			devRoot.setAttribute("PushPull", s);
		}

	}

	/**
	 * create device properties for the devices defined in the config file
	 * @param baseDir 
	 * @param baseURL 
	 * @param _context the location of the web application in the server
	 * @param configFile the config file
	 */
	public MultiDeviceProperties(File baseDir, String baseURL, final File configFile)
	{
		// to evaluate current name and send it back rather than 127.0.0.1
		final JDFParser p = new JDFParser();
		final XMLDoc doc = p.parseFile(FileUtil.getFileInDirectory(baseDir, configFile));
		root = doc == null ? null : doc.getRoot();
		this.context = baseURL;
		this.baseDir = baseDir;

		if (root == null || doc == null)
		{
			log.fatal("failed to parse " + configFile + ", rootDev is null");
		}
		else
		{
			root.setAttribute("AppDir", baseDir.getAbsolutePath());
			final File deviceDir = getBaseDir();
			final File fileInDirectory = FileUtil.getFileInDirectory(deviceDir, configFile);
			final XMLDoc d2 = p.parseFile(fileInDirectory);
			if (d2 != null) // using config default
			{
				root = d2.getRoot();
				log.info("using updated device config");
			}
			else
			// using updated devices
			{
				deviceDir.mkdirs();
				doc.setOriginalFileName(fileInDirectory.getAbsolutePath());
				serialize();
			}
		}
	}

	/**
	 * serialize this to it's default location
	 * @return true if success
	 */
	public boolean serialize()
	{
		return root.getOwnerDocument_KElement().write2File((String) null, 2, false);
	}

	/**
	 * @return
	 */
	int getPort()
	{
		// TODO extract from servlet
		return root.getIntAttribute("Port", null, BambiServlet.port);

	}

	/**
	 * @return
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
		String contextURL = null;
		try
		{
			final InetAddress localHost = InetAddress.getLocalHost();
			contextURL = "http://" + localHost.getHostAddress() + ":" + getPort() + "/" + context;
		}
		catch (final UnknownHostException x1)
		{
			//
		}
		return contextURL;
	}

	/**
	 * @return the application directory
	 */
	public File getAppDir()
	{
		return getRootFile("AppDir");
	}

	/**
	 * @return the configuration directory
	 */
	public File getConfigDir()
	{
		final File f = getRootFile("AppDir");
		return FileUtil.getFileInDirectory(f, new File("config"));
	}

	/**
	 * get the base directory for data
	 * @return the base directory for data
	 */
	public File getBaseDir()
	{
		File f = getRootFile("BaseDir");
		if (!FileUtil.isAbsoluteFile(f))
		{
			final File fBase = getAppDir();
			f = FileUtil.getFileInDirectory(fBase, f);
		}
		return f;
	}

	/**
	 * @return the jdf directory
	 */
	public File getJDFDir()
	{
		final File fBase = getBaseDir();
		final File f = getRootFile("JDFDir");
		return FileUtil.getFileInDirectory(fBase, f);
	}

	/**
	 * the jmf persistance directory <br/>
	 * defaults to a sibling of JDFDir called JMFDir
	 * 
	 * @return the jmf persistance directory
	 */
	public File getJMFDir()
	{
		File f = getRootFile("JMFDir");
		if (f == null)
		{
			f = getJDFDir();
			if (f != null)
			{
				f = FileUtil.getFileInDirectory(f.getParentFile(), new File("JMFDir"));
			}
		}
		return f;
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
	 * @param element the xml element to parse
	 * @return a IDeviceProperties parsed from the element
	 */
	public IDeviceProperties createDeviceProps(final KElement element)
	{
		return this.new DeviceProperties(element);
	}

}