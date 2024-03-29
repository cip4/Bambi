/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2015 The International Cooperation for the Integration of 
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

import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.StringUtil;

/**
 * container for the properties of several Bambi devices
 * 
 * @author boegerni
 */
public class ProxyProperties extends MultiDeviceProperties
{
	/**
	 * properties for a single device
	 * 
	 * @author boegerni
	 */

	/**
	 * 
	 * @param baseDir
	 */
	public ProxyProperties(File baseDir)
	{
		super(baseDir);
	}

	/**
	 * 
	 * @param doc
	 */
	protected ProxyProperties(XMLDoc doc)
	{
		super(doc);
	}

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 *         11.02.2009
	 */
	public class ProxyDeviceProperties extends DeviceProperties implements IProxyProperties
	{
		/**
		 * constructor
		 * 
		 * @param elem
		 */
		public ProxyDeviceProperties(final KElement elem)
		{
			super(elem);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveCallBackClass()
		 */
		@Override
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
				catch (final Throwable x)
				{
					getLog().error("Cannot instantiate callback class: " + _callBackName);
				}
			}
			return null;
		}

		/**
		 * @return
		 */
		public String getSlaveCallBackClassName()
		{
			return getCallBackClassName("SlaveCallBackName");
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveDeviceID()
		 */
		@Override
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
		@Override
		public File getSlaveErrorHF()
		{
			return getFile("SlaveErrorHF");
		}

		/**
		 * get the URL of this proxy for the slave - includes last '/'
		 * 
		 * @return the url
		 */
		@Override
		public String getDeviceURLForSlave()
		{
			final String s = getDeviceURL();
			return StringUtil.replaceString(s, "/jmf/", "/" + AbstractProxyDevice.SLAVEJMF + "/");
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveInputHF()
		 */
		@Override
		public File getSlaveInputHF()
		{
			return getFile("SlaveInputHF");
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveOutputHF()
		 */
		@Override
		public File getSlaveOutputHF()
		{
			return getFile("SlaveOutputHF");
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveInputHF(java.io.File)
		 */
		@Override
		public void setSlaveInputHF(final File hf)
		{
			setFile("SlaveInputHF", hf);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveOutputHF(java.io.File)
		 */
		@Override
		public void setSlaveOutputHF(final File hf)
		{
			setFile("SlaveOutputHF", hf);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveErrorHF(java.io.File)
		 */
		@Override
		public void setSlaveErrorHF(final File hf)
		{
			setFile("SlaveErrorHF", hf);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveURL()
		 */
		@Override
		public String getSlaveURL()
		{
			return devRoot.getAttribute("SlaveURL", null, null);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveURL(java.lang.String)
		 */
		@Override
		public void setSlaveURL(final String slaveURL)
		{
			setDeviceAttribute("SlaveURL", slaveURL);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getMaxPush()
		 */
		@Override
		public int getMaxPush()
		{
			return StringUtil.parseInt(getDeviceAttribute("MaxPush"), 0);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getMaxSlaveRunning()
		 */
		@Override
		public int getMaxSlaveRunning()
		{
			return StringUtil.parseInt(getDeviceAttribute("MaxSlaveRunning"), 999);
		}

		/**
		 * @param push the max number of jobs to push to the slave device
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveURL(java.lang.String)
		 */
		@Override
		public void setMaxPush(final int push)
		{
			setDeviceAttribute("MaxPush", StringUtil.formatInteger(push));
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveHTTPChunk()
		 */
		@Override
		public int getSlaveHTTPChunk()
		{
			return StringUtil.parseInt(getDeviceAttribute("SlaveHTTPChunk"), getControllerHTTPChunk());
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveMIMEEncoding()
		 */
		@Override
		public String getSlaveMIMEEncoding()
		{
			return getDeviceAttribute("SlaveMIMETransferEncoding", null, getControllerMIMEEncoding());
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#getSlaveMIMEExpansion()
		 */
		@Override
		public boolean getSlaveMIMEExpansion()
		{
			return isSlaveMimePackaging() && StringUtil.parseBoolean(getDeviceAttribute("SlaveMIMETransferExpansion"), getControllerMIMEExpansion());
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveMIMEExpansion(boolean)
		 * @param extendMime
		 */
		@Override
		public void setSlaveMIMEExpansion(boolean extendMime)
		{
			setDeviceAttribute("SlaveMIMETransferExpansion", "" + extendMime);
		}

		/**
		 * @return true if a semicolon is allowed after the mime type
		 */
		@Override
		public boolean getSlaveMIMESemicolon()
		{
			return isSlaveMimePackaging() && StringUtil.parseBoolean(getDeviceAttribute("SlaveMIMESemicolon"), true);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#isSlaveMimePackaging()
		 * @return true if slave accepts mime,
		 * @default is true
		 */
		@Override
		public boolean isSlaveMimePackaging()
		{
			return StringUtil.parseBoolean(getDeviceAttribute("SlaveMimePackaging"), true);
		}

		/**
		 * @see org.cip4.bambi.proxy.IProxyProperties#setSlaveDeviceID(java.lang.String)
		 */
		@Override
		public void setSlaveDeviceID(final String newSlaveID)
		{
			setDeviceAttribute("SlaveDeviceID", newSlaveID);
		}

	}

	/**
	 * @param element
	 * @return
	 */
	@Override
	public ProxyDeviceProperties createDeviceProps(KElement element)
	{
		if (element == null)
			element = root.appendElement(ElementName.DEVICE);
		return this.new ProxyDeviceProperties(element);
	}

}