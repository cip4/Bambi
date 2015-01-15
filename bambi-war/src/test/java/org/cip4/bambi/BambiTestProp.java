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
package org.cip4.bambi;

import java.io.File;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.jdflib.core.VString;

class BambiTestProp implements IDeviceProperties
{

	/**
	 * @param bambiTestCase
	 */
	BambiTestProp()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getAppDir()
	 */
	@Override
	public File getAppDir()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
	 */
	@Override
	public File getBaseDir()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
	 */
	@Override
	public IConverterCallback getCallBackClass()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
	 */
	@Override
	public String getDeviceID()
	{
		return "ID_42";
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
	 */
	@Override
	public String getDeviceType()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
	 */
	@Override
	public String getDeviceURL()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getErrorHF()
	 */
	@Override
	public File getErrorHF()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getInputHF()
	 */
	@Override
	public File getInputHF()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getJDFDir()
	 */
	public File getJDFDir()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getMaxPush()
	 */
	public int getMaxPush()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getOutputHF()
	 */
	@Override
	public File getOutputHF()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getProxyControllerURL()
	 */
	@Override
	public String getProxyControllerURL()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveDeviceID()
	 */
	public String getSlaveDeviceID()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveErrorHF()
	 */
	public File getSlaveErrorHF()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveInputHF()
	 */
	public File getSlaveInputHF()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveOutputHF()
	 */
	public File getSlaveOutputHF()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveURL()
	 */
	public String getSlaveURL()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getTrackResource()
	 */
	@Override
	public String getTrackResource()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getTypeExpression()
	 */
	@Override
	public String getTypeExpression()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getAmountResources()
	 */
	@Override
	public VString getAmountResources()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceHTTPChunk()
	 */
	@Override
	public int getControllerHTTPChunk()
	{

		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceMIMEEncoding()
	 */
	@Override
	public String getControllerMIMEEncoding()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getReturnMIME()
	 */
	@Override
	public QEReturn getReturnMIME()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getContextURL()
	 */
	@Override
	public String getContextURL()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceAttribute(java.lang.String)
	 */
	@Override
	public String getDeviceAttribute(final String key)
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceClass()
	 */
	@Override
	public AbstractDevice getDeviceInstance()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getWatchURL()
	 */
	@Override
	public String getWatchURL()
	{

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEExpansion()
	 */
	@Override
	public boolean getControllerMIMEExpansion()
	{

		return false;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#getConfigDir()
	 * @return
	 */
	@Override
	public File getConfigDir()
	{

		return null;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#serialize()
	 */
	@Override
	public boolean serialize()
	{

		return false;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setDeviceType(java.lang.String)
	 */
	@Override
	public void setDeviceType(final String deviceType)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setWatchURL(java.lang.String)
	 */
	@Override
	public void setWatchURL(final String WatchURL)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setErrorHF(java.io.File)
	 */
	@Override
	public void setErrorHF(final File hf)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setInputHF(java.io.File)
	 */
	@Override
	public void setInputHF(final File hf)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setOutputHF(java.io.File)
	 */
	@Override
	public void setOutputHF(final File hf)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setTypeExpression(java.lang.String)
	 */
	@Override
	public void setTypeExpression(final String exp)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#getQERetrieval()
	 */
	@Override
	public QERetrieval getQERetrieval()
	{

		return null;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setQERetrieval(org.cip4.bambi.core.IDeviceProperties.QERetrieval)
	 */
	@Override
	public void setQERetrieval(final QERetrieval qer)
	{

	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#getAcceptAll()
	 */
	@Override
	public boolean getAcceptAll()
	{
		return false;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#getDescription()
	 */
	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getAutoStart()
	{
		return true;
	}

}