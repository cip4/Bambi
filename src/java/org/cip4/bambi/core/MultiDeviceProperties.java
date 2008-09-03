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
package org.cip4.bambi.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 * container for the properties of several Bambi devices
 * 
 * @author boegerni
 */
public class MultiDeviceProperties
{
	/**
	 * properties for a single device
	 * @author boegerni
	 *
	 */
	protected KElement root;
	private URL contextURL;
	private final ServletContext context;

	public class DeviceProperties implements IDeviceProperties
	{
		/**
		 * constructor
		 */
		protected KElement devRoot;

		protected DeviceProperties(KElement _devRoot)
		{
			devRoot = _devRoot;
		}

		public KElement getDevRoot()
		{
			return devRoot;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
		 */
		public String getDeviceURL()
		{
			try
			{
				InetAddress localHost = InetAddress.getLocalHost();
				contextURL = new URL("http://" + localHost.getHostName() + ":" + getPort() + "/"
						+ StringUtil.token(context.getResource("/").toExternalForm(), -1, "/"));
			}
			catch (UnknownHostException x1)
			{
				//
			}
			catch (MalformedURLException x2)
			{
				// 
			}

			return contextURL.toExternalForm() + "/jmf/" + getDeviceID();
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
		 */
		public IConverterCallback getCallBackClass()
		{
			String _callBackName = getCallBackClassName();

			if (_callBackName != null)
			{
				try
				{
					Class c = Class.forName(_callBackName);
					return (IConverterCallback) c.newInstance();
				}
				catch (Exception x)
				{
					log.error("Cannot instantiate callback class: " + _callBackName);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClassName()
		 */
		public String getCallBackClassName()
		{
			String name = devRoot.getAttribute("CallBackName", null, null);
			if (name == null)
				name = root.getAttribute("CallBackName", null, null);
			return name;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
		 */
		public IDevice getDeviceInstance()
		{
			String _deviceName = getDeviceClassName();

			if (_deviceName != null)
			{
				try
				{
					Class c = Class.forName(_deviceName);
					Constructor con = c.getConstructor(new Class[] { IDeviceProperties.class });
					return (IDevice) con.newInstance(new Object[] { this });
				}
				catch (Exception x)
				{
					log.error("Cannot instantiate Device class: " + _deviceName, x);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClassName()
		 */
		public String getDeviceClassName()
		{
			return getDeviceAttribute("DeviceClass", null, "org.cip4.bambi.workers.sim.SimDevice");
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
		 */
		public String getDeviceID()
		{
			return getDeviceAttribute("DeviceID", null, null);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getProxyURL()
		 */
		public String getProxyControllerURL()
		{
			return getDeviceAttribute("ProxyURL", null, null);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
		 */
		public String getDeviceType()
		{
			return getDeviceAttribute("DeviceType", null, null);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#toString()
		 */
		@Override
		public String toString()
		{
			return "[ DeviceProperties: " + devRoot.toString() + "]";
		}

		protected File getFile(String file)
		{
			final String fil = devRoot.getAttribute(file, null, null);
			return fil == null ? getRootFile(file) : new File(fil);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceErrorHF()
		 */
		public File getErrorHF()
		{
			return getFile("ErrorHF");
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceOutputHF()
		 */
		public File getOutputHF()
		{
			return getFile("OutputHF");
		}

		/**
		 * @return
		 */
		public File getInputHF()
		{
			return getFile("InputHF");
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
		 */
		public File getBaseDir()
		{
			return MultiDeviceProperties.this.getBaseDir();
		}

		public File getAppDir()
		{
			return MultiDeviceProperties.this.getAppDir();
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getJDFDir()
		 */
		public File getJDFDir()
		{
			File f = MultiDeviceProperties.this.getJDFDir();
			return FileUtil.getFileInDirectory(f, new File(getDeviceID()));
		}

		/**
		 * get the tracked resource - defaults to "Output"
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getTrackResource()
		 */
		public String getTrackResource()
		{
			return getDeviceAttribute("TrackResource", null, "Output");
		}

		public String getDeviceAttribute(String key)
		{
			return getDeviceAttribute(key, null, null);

		}

		public String getDeviceAttribute(String key, String ns, String def)
		{
			String val = devRoot.getAttribute(key, ns, null);
			if (val == null)
				val = root.getAttribute(key, ns, def);
			return val;

		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getTypeExpression()
		 */
		public String getTypeExpression()
		{
			return getDeviceAttribute(AttributeName.TYPEEXPRESSION);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getAmountResources()
		 */
		public VString getAmountResources()
		{
			VString v = StringUtil.tokenize(getDeviceAttribute("AmountResources", null, null), ",", false);
			final String trackResource = getTrackResource();
			if (v == null)
			{
				return StringUtil.tokenize(trackResource, null, false);
			}
			v.appendUnique(trackResource);
			return v;
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceHTTPChunk()
		 */
		public int getControllerHTTPChunk()
		{
			return StringUtil.parseInt(getDeviceAttribute("HTTPChunk"), 10000);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceMIMEEncoding()
		 */
		public String getControllerMIMEEncoding()
		{
			return getDeviceAttribute("MIMETransferEncoding", null, MimeUtil.BASE64);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEExpansion()
		 */
		public boolean getControllerMIMEExpansion()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("MIMEExpansion"), false);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getReturnMIME()
		 */
		public QEReturn getReturnMIME()
		{
			String s = getDeviceAttribute("MIMEReturn", null, "MIME");
			try
			{
				return QEReturn.valueOf(s);
			}
			catch (Exception x)
			{
				return QEReturn.MIME;
			}
		}

		public String getContextURL()
		{
			return contextURL == null ? null : contextURL.toExternalForm();
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.core.IDeviceProperties#getWatchURL()
		 */
		public String getWatchURL()
		{
			return getDeviceAttribute("WatchURL", null, null);
		}

	}

	private static final Log log = LogFactory.getLog(MultiDeviceProperties.class.getName());

	/**
	 * create device properties for the devices defined in the config file
	 * @param appDir     the location of the web application in the server
	 * @param configFile the config file
	 */
	public MultiDeviceProperties(ServletContext _context, File configFile)
	{
		context = _context;
		// to evaluate current name and send it back rather than 127.0.0.1
		File baseDir = new File(context.getRealPath(""));
		JDFParser p = new JDFParser();
		XMLDoc doc = p.parseFile(FileUtil.getFileInDirectory(baseDir, configFile));
		root = doc == null ? null : doc.getRoot();
		if (root == null)
		{
			log.fatal("failed to parse " + configFile + ", rootDev is null");
		}
		else
		{
			root.setAttribute("AppDir", baseDir.getAbsolutePath());
		}
		try
		{
			InetAddress localHost = InetAddress.getLocalHost();
			contextURL = new URL("http://" + localHost.getHostName() + ":" + getPort() + "/"
					+ StringUtil.token(context.getResource("/").toExternalForm(), -1, "/"));
		}
		catch (UnknownHostException x1)
		{
			//
		}
		catch (MalformedURLException x2)
		{
			// 
		}

	}

	/**
	 * @return
	 */
	private int getPort()
	{
		//TODO extract from servlet
		return root.getIntAttribute("Port", null, BambiServlet.port);

	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IMultiDeviceProperties#count()
	 */
	public int count()
	{
		return root.numChildElements(ElementName.DEVICE, null);
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IMultiDeviceProperties#toString()
	 */
	@Override
	public String toString()
	{
		return "[ MultiDeviceProperties: " + contextURL + "\n" + root + "]";
	}

	/**
	 * @return the application directory
	 */
	public File getAppDir()
	{
		return getRootFile("AppDir");
	}

	/**
	 * get the base directory for data
	 * @return  the base directory for data
	 */
	public File getBaseDir()
	{
		File f = getRootFile("BaseDir");
		if (!FileUtil.isAbsoluteFile(f))
		{
			File fBase = getAppDir();
			f = FileUtil.getFileInDirectory(fBase, f);
		}
		return f;
	}

	/**
	 * @return the jdf directory
	 */
	public File getJDFDir()
	{
		File fBase = getBaseDir();
		File f = getRootFile("JDFDir");
		return FileUtil.getFileInDirectory(fBase, f);
	}

	/**
	 * @return the jmf persistance directory
	 */
	public File getJMFDir()
	{
		File f = getRootFile("JMFDir");
		if (f == null)
			f = new File("C:/BambiData/JMFDir");
		return f;
	}

	/**
	 * @param file the file type to search
	 * @return a File representing the directory
	 */
	protected File getRootFile(String file)
	{
		final String fil = root.getAttribute(file, null, null);
		return fil == null ? null : new File(fil);
	}

	/**
	 * 
	 * @return the sender ID...
	 */
	public String getSenderID()
	{
		return root.getAttribute(AttributeName.SENDERID);
	}

	/**
	 * @return the vector of device elements
	 */
	public VElement getDevices()
	{
		return root.getChildElementVector(ElementName.DEVICE, null);
	}

	/**
	 * 
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
	public IDeviceProperties createDevice(KElement element)
	{
		return this.new DeviceProperties(element);
	}

}