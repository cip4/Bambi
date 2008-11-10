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
package org.cip4.bambi.proxy;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.StringUtil;

/**
 * container for the properties of several Bambi devices
 * @author boegerni
 */
public class ProxyProperties extends MultiDeviceProperties
{
	/**
	 * properties for a single device
	 * @author boegerni
	 */

	private static final Log log = LogFactory.getLog(ProxyProperties.class.getName());

	public class ProxyDeviceProperties extends DeviceProperties implements IProxyProperties
	{
		/**
		 * constructor
		 */
		protected ProxyDeviceProperties(final KElement elem)
		{
			super(elem);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
		 */
		public IConverterCallback getSlaveCallBackClass()
		{
			final String _callBackName = getSlaveCallBackClassName();

			if (_callBackName != null)
			{
				try
				{
					final Class c = Class.forName(_callBackName);
					return (IConverterCallback) c.newInstance();
				}
				catch (final Exception x)
				{
					log.error("Cannot instantiate callback class: " + _callBackName);
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClassName()
		 */
		public String getSlaveCallBackClassName()
		{
			String name = devRoot.getAttribute("SlaveCallBackName", null, null);
			if (name == null)
			{
				name = root.getAttribute("SlaveCallBackName", null, null);
			}
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveDeviceID()
		 */
		public String getSlaveDeviceID()
		{
			String s = devRoot.getAttribute("SlaveDeviceID", null, null);
			if (s != null)
			{
				return s;
			}
			s = getSlaveURL();
			if (s != null)
			{
				s = StringUtil.token(s, -1, "/");
			}
			return s;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveErrorHF()
		 */
		public File getSlaveErrorHF()
		{
			return getFile("SlaveErrorHF");
		}

		/**
		 * get the URL of this proxy for the slave - includes last '/'
		 * @return the url
		 */
		public String getDeviceURLForSlave()
		{
			final String s = getDeviceURL();
			return StringUtil.replaceString(s, "/jmf/", "/" + AbstractProxyDevice.SLAVEJMF + "/");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveInputHF()
		 */
		public File getSlaveInputHF()
		{
			return getFile("SlaveInputHF");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveOutputHF()
		 */
		public File getSlaveOutputHF()
		{
			return getFile("SlaveOutputHF");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveURL()
		 */
		public String getSlaveURL()
		{
			return devRoot.getAttribute("SlaveURL", null, null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveURL()
		 */
		public void setSlaveURL(final String slaveURL)
		{
			devRoot.setAttribute("SlaveURL", slaveURL, null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getMaxPush()
		 */
		public int getMaxPush()
		{
			return StringUtil.parseInt(getDeviceAttribute("MaxPush"), 0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceHTTPChunk()
		 */
		public int getSlaveHTTPChunk()
		{
			return StringUtil.parseInt(getDeviceAttribute("SlaveHTTPChunk"), getControllerHTTPChunk());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceMIMEEncoding()
		 */
		public String getSlaveMIMEEncoding()
		{
			return getDeviceAttribute("SlaveMIMETransferEncoding", null, getControllerMIMEEncoding());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveMIMEExpansion()
		 */
		public boolean getSlaveMIMEExpansion()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("SlaveMIMETransferExpansion"), getControllerMIMEExpansion());
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#isSlaveMimePackaging()
		 * @return true if slave accepts mime,
		 * @default is true
		 */
		public boolean isSlaveMimePackaging()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("SlaveMimePackaging"), true);
		}

	}

	/**
	 * create device properties for the devices defined in the config file
	 * @param appDir the location of the web application in the server
	 * @param configFile the config file
	 */
	public ProxyProperties(final ServletContext _context, final File configFile)
	{
		super(_context, configFile);

	}

	/**
	 * @param element
	 * @return
	 */
	@Override
	public IDeviceProperties createDevice(final KElement element)
	{
		return this.new ProxyDeviceProperties(element);
	}

}