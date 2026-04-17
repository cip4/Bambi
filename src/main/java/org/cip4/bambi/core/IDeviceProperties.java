/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of
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

import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.util.EnumUtil;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *         09.02.2009
 */
public interface IDeviceProperties
{

	/**
	 * generic catchall
	 *
	 * @param key
	 * @return
	 */
	String getDeviceAttribute(String key);

	/**
	 * queueentry return type
	 */
	public enum QEReturn
	{
		/**
		 * hot folder
		 */
		HF,
		/**
		 * pure http
		 */
		HTTP,
		/**
		 * MIME
		 */
		MIME
	}

	public enum EWatchFormat
	{
		JSON, JMF, XJMF, NONE;

		/**
		 * default is jmf
		 *
		 * @param val
		 * @return
		 */
		public static EWatchFormat getEnum(final String val)
		{
			final EWatchFormat ret = EnumUtil.getJavaEnumIgnoreCase(EWatchFormat.class, val);
			return ret == null ? JMF : ret;
		}
	}

	/**
	 * queueentry retrieval type - push or pull
	 */
	public enum QERetrieval
	{
		/**
		 * push - the queue pushes queue entries to the device automatically
		 */
		PUSH,
		/**
		 * pull - the qe must be pulled from the queue explicitely
		 */
		PULL,
		/**
		 * push and pull - a pulled qe is selected if available, else push
		 */
		BOTH
	}

	/**
	 * get the URL to communicate with this device
	 *
	 * @return the device URL. Send JMFs to this URL, if you want to communicate with this device.
	 */
	String getDeviceURL();

	default String getDeviceSSLURL()
	{
		return null;
	}

	/**
	 * get the URL to communicate with the root of this device
	 *
	 * @return the device URL. Send JMFs to this URL, if you want to communicate with this device.
	 */
	String getContextURL();

	default String getContextSSLURL()
	{
		return null;
	}

	/**
	 * get the URL of the device hotfolder, if null the device does not support a JDF input hot folder
	 *
	 * @return the device hotfolder URL. Drop JDFs to this URL, if you want to submit to the device without JMF.
	 */
	File getInputHF();

	/**
	 * @param hf the hot folder
	 */
	void setInputHF(File hf);

	/**
	 * get the DeviceID of this device
	 *
	 * @return the deviceID
	 */
	String getDeviceID();

	/**
	 * get the Name, ProcessUsage or Usage of the major resource to track
	 *
	 * @return the deviceID
	 */
	String getTrackResource();

	/**
	 * get the URL of the proxy this device is requesting JDFs from.
	 *
	 * @return the proxy URL
	 */
	String getProxyControllerURL();

	/**
	 * get the DeviceType of this device
	 *
	 * @return the DeviceType of this device
	 */
	String getDeviceType();

	/**
	 * get the human readable description of this device
	 *
	 * @return the description of this device
	 */
	String getDescription();

	/**
	 * set the DeviceType of this device
	 *
	 * @param deviceType the DeviceType of this device
	 */
	void setDeviceType(String deviceType);

	/**
	 * set the description of this device
	 *
	 * @param description the description of this device
	 */
	void setDescription(String description);

	/**
	 * get the application context dir of the web application
	 *
	 * @return the base dir of the web application
	 */
	File getAppDir();

	/**
	 * @return true if all jdfs should be accepted (ignore canAccept)
	 */
	boolean getAcceptAll();

	/**
	 * @return true if this device should automatically start
	 */
	boolean getAutoStart();

	/**
	 * get the application configuration dir of the web application
	 *
	 * @return the configuration dir of the Device
	 */
	File getConfigDir();

	/**
	 * get the base dir of the web application
	 *
	 * @return the base dir of the web application
	 */
	File getBaseDir();

	/**
	 * returns the name of the IDevice that specifies the converter name
	 *
	 * @return {@link IConverterCallback} the callback to use, null if none is specified
	 */
	AbstractDevice getDeviceInstance();

	/**
	 * returns the name of the IConverterCallback that specifies the converter name
	 *
	 * @return {@link IConverterCallback} the callback to use, null if none is specified
	 */
	IConverterCallback getCallBackClass();

	/**
	 * get a String representation of this DeviceProperty
	 *
	 * @return this representation of this DeviceProperty
	 */
	@Override
	String toString();

	/**
	 * @return
	 */
	File getOutputHF();

	/**
	 * @param hf the hot folder
	 */
	void setOutputHF(File hf);

	/**
	 * @return the queueentry retrieval method - push or pull
	 */
	QERetrieval getQERetrieval();

	/**
	 * @param qer the queueentry retrieval method - push or pull
	 */
	void setQERetrieval(QERetrieval qer);

	/**
	 * @return
	 */
	QEReturn getReturnMIME();

	/**
	 * @return the error hot folder
	 */
	File getErrorHF();

	/**
	 * @param hf the hot folder
	 */
	void setErrorHF(File hf);

	/**
	 * get the URL to send generic subscriptions to
	 *
	 * @return the device URL. Status, Resource signals will be sent here regardless of any other subscriptions
	 */
	String getWatchURL();

	default EWatchFormat getWatchFormat()
	{
		return EWatchFormat.JMF;
	}

	default void setWatchFormat(final EWatchFormat f)
	{
		// nop
	}

	/**
	 * set the URL to send generic subscriptions to
	 *
	 * @param watchURL
	 */
	void setWatchURL(String watchURL);

	/**
	 * @return the type regular expression that the device accepts
	 */
	String getTypeExpression();

	/**
	 * @param exp the type regular expression that the device accepts
	 */
	void setTypeExpression(String exp);

	/**
	 * @return the vector of amount counting resource names
	 */
	VString getAmountResources();

	/**
	 * get the HTTP chunking to communicate with this device
	 *
	 * @return the device URL. Send JMFs to this URL, if you want to communicate with this device.
	 */
	int getControllerHTTPChunk();

	/**
	 * @return the default body part encoding
	 */
	String getControllerMIMEEncoding();

	/**
	 * @return true if referenced files should be included in the mime package
	 */
	boolean getControllerMIMEExpansion();

	/**
	 * serialize this (write)
	 *
	 * @return true if successful
	 */
	boolean serialize();

	/**
	 * is the processor synchronous or does it run in its own thread?
	 *
	 * @return
	 */
	default boolean isSynch()
	{
		return false;
	}

}