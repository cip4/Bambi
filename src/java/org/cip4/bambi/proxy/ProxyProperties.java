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
package org.cip4.bambi.proxy;

import java.io.File;

import javax.servlet.ServletContext;

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

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * 11.02.2009
	 */
	public class ProxyDeviceProperties extends DeviceProperties implements IProxyProperties
	{
		/**
		 * constructor
		 */
		protected ProxyDeviceProperties(final KElement elem)
		{
			super(elem);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveCallBackClass()
		 */
		public IConverterCallback getSlaveCallBackClass()
		{
			final String _callBackName = getSlaveCallBackClassName();

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
		public String getSlaveCallBackClassName()
		{
			String name = devRoot.getAttribute("SlaveCallBackName", null, null);
			if (name == null)
			{
				name = root.getAttribute("SlaveCallBackName", null, null);
			}
			return name;
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveDeviceID()
		 */
		public String getSlaveDeviceID()
		{
			final String s = devRoot.getAttribute("SlaveDeviceID", null, null);
			if (s != null)
			{
				return s;
			}
			// if not set, assume proxy and slave are identical
			return getDeviceID();
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveErrorHF()
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

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveInputHF()
		 */
		public File getSlaveInputHF()
		{
			return getFile("SlaveInputHF");
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveOutputHF()
		 */
		public File getSlaveOutputHF()
		{
			return getFile("SlaveOutputHF");
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveInputHF(java.io.File)
		 */
		public void setSlaveInputHF(final File hf)
		{
			setFile("SlaveInputHF", hf);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveOutputHF(java.io.File)
		 */
		public void setSlaveOutputHF(final File hf)
		{
			setFile("SlaveOutputHF", hf);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveErrorHF(java.io.File)
		 */
		public void setSlaveErrorHF(final File hf)
		{
			setFile("SlaveErrorHF", hf);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveURL()
		 */
		public String getSlaveURL()
		{
			return devRoot.getAttribute("SlaveURL", null, null);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveURL(java.lang.String)
		 */
		public void setSlaveURL(final String slaveURL)
		{
			devRoot.setAttribute("SlaveURL", slaveURL, null);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getMaxPush()
		 */
		public int getMaxPush()
		{
			return StringUtil.parseInt(getDeviceAttribute("MaxPush"), 0);
		}

		/**
		 * @param push the max number of jobs to push to the slave device
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveURL(java.lang.String)
		 */
		public void setMaxPush(final int push)
		{
			devRoot.setAttribute("MaxPush", push, null);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveHTTPChunk()
		 */
		public int getSlaveHTTPChunk()
		{
			return StringUtil.parseInt(getDeviceAttribute("SlaveHTTPChunk"), getControllerHTTPChunk());
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveMIMEEncoding()
		 */
		public String getSlaveMIMEEncoding()
		{
			return getDeviceAttribute("SlaveMIMETransferEncoding", null, getControllerMIMEEncoding());
		}

		/**
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

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveDeviceID(java.lang.String)
		 */
		public void setSlaveDeviceID(final String newSlaveID)
		{
			devRoot.setAttribute("SlaveDeviceID", newSlaveID, null);
		}

	}

	/**
	 * create device properties for the devices defined in the config file
	 * @param _context the servlet context
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