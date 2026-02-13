/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of
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
import java.util.concurrent.atomic.AtomicInteger;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.ConverterCallback;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.proxy.IProxyProperties;
import org.cip4.bambi.proxy.ProxyProperties;
import org.cip4.bambi.proxy.ProxyProperties.ProxyDeviceProperties;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;

public class BambiTestProp extends BambiTestCaseBase implements IProxyProperties
{

	private final String devID;
	private static AtomicInteger nn = new AtomicInteger();
	private final ProxyProperties delegate = new ProxyProperties(new File(sm_dirTestDataTemp + "testprops"));
	private final ProxyDeviceProperties devdelegate = delegate.createDeviceProps(null);
	private final int n;

	/**
	 * @param bambiTestCase
	 */
	public BambiTestProp()
	{
		super();
		this.devID = "ID_42";
		n = nn.incrementAndGet();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.cip4.bambi.core.IDeviceProperties#getAppDir()
	 */
	@Override
	public File getAppDir()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
	 */
	@Override
	public File getBaseDir()
	{
		return new File(sm_dirTestDataTemp + "bambiBase" + n);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
	 */
	@Override
	public IConverterCallback getCallBackClass()
	{
		return new ConverterCallback();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
	 */
	@Override
	public String getDeviceID()
	{
		return devID;
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#getConfigDir()
	 * @return
	 */
	@Override
	public File getConfigDir()
	{

		return new File(sm_dirTestDataTemp + "Bambi" + n);
	}

	/**
	 * @see org.cip4.bambi.core.IDeviceProperties#serialize()
	 */
	@Override
	public boolean serialize()
	{

		return false;
	}

	@Override
	public int hashCode()
	{
		return devdelegate.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return devdelegate.equals(obj);
	}

	@Override
	public IConverterCallback getSlaveCallBackClass()
	{
		return devdelegate.getSlaveCallBackClass();
	}

	public String getSlaveCallBackClassName()
	{
		return devdelegate.getSlaveCallBackClassName();
	}

	public KElement getDevRoot()
	{
		return devdelegate.getDevRoot();
	}

	@Override
	public String getSlaveDeviceID()
	{
		return devdelegate.getSlaveDeviceID();
	}

	public KElement getRoot()
	{
		return devdelegate.getRoot();
	}

	@Override
	public String getDeviceURL()
	{
		return devdelegate.getDeviceURL();
	}

	@Override
	public File getSlaveErrorHF()
	{
		return devdelegate.getSlaveErrorHF();
	}

	@Override
	public String getDeviceURLForSlave()
	{
		return devdelegate.getDeviceURLForSlave();
	}

	public String getCallBackClassName()
	{
		return devdelegate.getCallBackClassName();
	}

	@Override
	public File getSlaveInputHF()
	{
		return devdelegate.getSlaveInputHF();
	}

	@Override
	public File getSlaveOutputHF()
	{
		return devdelegate.getSlaveOutputHF();
	}

	@Override
	public void setSlaveInputHF(File hf)
	{
		devdelegate.setSlaveInputHF(hf);
	}

	@Override
	public void setSlaveOutputHF(File hf)
	{
		devdelegate.setSlaveOutputHF(hf);
	}

	public void setCallBackClassName(String callbackName)
	{
		devdelegate.setCallBackClassName(callbackName);
	}

	@Override
	public void setSlaveErrorHF(File hf)
	{
		devdelegate.setSlaveErrorHF(hf);
	}

	@Override
	public AbstractDevice getDeviceInstance()
	{
		return devdelegate.getDeviceInstance();
	}

	@Override
	public String getSlaveURL()
	{
		return devdelegate.getSlaveURL();
	}

	@Override
	public void setSlaveURL(String slaveURL)
	{
		devdelegate.setSlaveURL(slaveURL);
	}

	@Override
	public int getMaxPush()
	{
		return devdelegate.getMaxPush();
	}

	@Override
	public int getMaxSlaveRunning()
	{
		return devdelegate.getMaxSlaveRunning();
	}

	public void setDeviceClassName(String deviceClass)
	{
		devdelegate.setDeviceClassName(deviceClass);
	}

	public String getDeviceClassName()
	{
		return devdelegate.getDeviceClassName();
	}

	@Override
	public void setMaxPush(int push)
	{
		devdelegate.setMaxPush(push);
	}

	@Override
	public int getSlaveHTTPChunk()
	{
		return devdelegate.getSlaveHTTPChunk();
	}

	public void setDeviceID(String deviceID)
	{
		devdelegate.setDeviceID(deviceID);
	}

	@Override
	public String getProxyControllerURL()
	{
		return devdelegate.getProxyControllerURL();
	}

	@Override
	public String getSlaveMIMEEncoding()
	{
		return devdelegate.getSlaveMIMEEncoding();
	}

	@Override
	public String getDeviceType()
	{
		return devdelegate.getDeviceType();
	}

	@Override
	public boolean getSlaveMIMEExpansion()
	{
		return devdelegate.getSlaveMIMEExpansion();
	}

	@Override
	public void setDeviceType(String deviceType)
	{
		devdelegate.setDeviceType(deviceType);
	}

	@Override
	public void setSlaveMIMEExpansion(boolean extendMime)
	{
		devdelegate.setSlaveMIMEExpansion(extendMime);
	}

	@Override
	public String getDescription()
	{
		return devdelegate.getDescription();
	}

	@Override
	public boolean getSlaveMIMESemicolon()
	{
		return devdelegate.getSlaveMIMESemicolon();
	}

	@Override
	public void setDescription(String description)
	{
		devdelegate.setDescription(description);
	}

	@Override
	public boolean isSlaveMimePackaging()
	{
		return devdelegate.isSlaveMimePackaging();
	}

	@Override
	public String toString()
	{
		return devdelegate.toString();
	}

	@Override
	public void setSlaveDeviceID(String newSlaveID)
	{
		devdelegate.setSlaveDeviceID(newSlaveID);
	}

	@Override
	public File getErrorHF()
	{
		return devdelegate.getErrorHF();
	}

	@Override
	public void setErrorHF(File hf)
	{
		devdelegate.setErrorHF(hf);
	}

	@Override
	public File getOutputHF()
	{
		return devdelegate.getOutputHF();
	}

	@Override
	public void setOutputHF(File hf)
	{
		devdelegate.setOutputHF(hf);
	}

	public VString getICSVersions()
	{
		return devdelegate.getICSVersions();
	}

	@Override
	public File getInputHF()
	{
		return devdelegate.getInputHF();
	}

	@Override
	public void setInputHF(File hf)
	{
		devdelegate.setInputHF(hf);
	}

	@Override
	public String getTrackResource()
	{
		return devdelegate.getTrackResource();
	}

	@Override
	public String getDeviceAttribute(String key)
	{
		return devdelegate.getDeviceAttribute(key);
	}

	public boolean hasDeviceOption(String key, boolean ifNotSet)
	{
		return devdelegate.hasDeviceOption(key, ifNotSet);
	}

	public String getDeviceAttribute(String key, String ns, String def)
	{
		return devdelegate.getDeviceAttribute(key, ns, def);
	}

	public void setDeviceAttribute(String key, String val)
	{
		devdelegate.setDeviceAttribute(key, val);
	}

	public KElement getDeviceElement(String xpath)
	{
		return devdelegate.getDeviceElement(xpath);
	}

	@Override
	public String getTypeExpression()
	{
		return devdelegate.getTypeExpression();
	}

	@Override
	public void setTypeExpression(String exp)
	{
		devdelegate.setTypeExpression(exp);
	}

	@Override
	public VString getAmountResources()
	{
		return devdelegate.getAmountResources();
	}

	@Override
	public int getControllerHTTPChunk()
	{
		return devdelegate.getControllerHTTPChunk();
	}

	@Override
	public boolean getAcceptAll()
	{
		return devdelegate.getAcceptAll();
	}

	@Override
	public String getControllerMIMEEncoding()
	{
		return devdelegate.getControllerMIMEEncoding();
	}

	@Override
	public boolean getControllerMIMEExpansion()
	{
		return devdelegate.getControllerMIMEExpansion();
	}

	@Override
	public org.cip4.bambi.core.IDeviceProperties.QEReturn getReturnMIME()
	{
		return devdelegate.getReturnMIME();
	}

	@Override
	public String getContextURL()
	{
		return devdelegate.getContextURL();
	}

	@Override
	public String getWatchURL()
	{
		return devdelegate.getWatchURL();
	}

	@Override
	public void setWatchURL(String watchURL)
	{
		devdelegate.setWatchURL(watchURL);
	}

	@Override
	public QERetrieval getQERetrieval()
	{
		return devdelegate.getQERetrieval();
	}

	@Override
	public void setQERetrieval(QERetrieval qer)
	{
		devdelegate.setQERetrieval(qer);
	}

	public boolean persist()
	{
		return devdelegate.persist();
	}

	@Override
	public boolean getAutoStart()
	{
		return devdelegate.getAutoStart();
	}

	public boolean isTemplate()
	{
		return devdelegate.isTemplate();
	}

	public void setAutoStart(boolean bAutoStart)
	{
		devdelegate.setAutoStart(bAutoStart);
	}

	public DeviceProperties activateDeviceProps(String deviceID)
	{
		return devdelegate.activateDeviceProps(deviceID);
	}

	public MultiDeviceProperties getParent()
	{
		return devdelegate.getParent();
	}

	@Override
	public EWatchFormat getWatchFormat()
	{
		return devdelegate.getWatchFormat();
	}

	@Override
	public void setWatchFormat(EWatchFormat f)
	{
		devdelegate.setWatchFormat(f);
	}

}