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

import org.cip4.jdflib.core.VString;

public interface IDeviceProperties {

    /**
     * queueentry return type
     *
     */
    public enum QEReturn{HF,HTTP,MIME};
    /**
     * get the URL to communicate with this device
     * @return the device URL. Send JMFs to this URL, if you want to communicate with this device. 
     */
    public String getDeviceURL();
    /**
     * get the URL to communicate with the root of this device
     * @return the device URL. Send JMFs to this URL, if you want to communicate with this device. 
     */
    public String getContextURL();
    /**
     * get the URL to communicate with this device
     * @return the device URL. Send JMFs to this URL, if you want to communicate with this device. 
     */
    public int getDeviceHTTPChunk();
    
    /**
     * get the URL of the device hotfolder, if null the device does not support a JDF input hot folder
     * @return the device hotfolder URL. Drop JDFs to this URL, if you want to submit to the device without JMF. 
     */
    public File getInputHF();

    /**
     * get the DeviceID of this device
     * @return the deviceID
     */
    public String getDeviceID();

    /**
     * get the Name, ProcessUsage or Usage of the major resource to track
     * @return the deviceID
     */
    public String getTrackResource();

	/**
	 * get the URL of the proxy this device is requesting JDFs from.
	 * @return the proxy URL
	 */
	public String getProxyControllerURL();

	/**
	 * get the DeviceType of this device
	 * @return the DeviceType of this device
	 */
	public String getDeviceType();


	
    /**
     * get the application context dir of the web application
     * @return the base dir of the web application
     */
    public File getAppDir();
    
    /**
     * get the base dir of the web application
     * @return the base dir of the web application
     */
    public File getBaseDir();
	
	/**
	 * get the directory containing the JDF documents
	 * @return the directory containing the JDF documents
	 */
	public File getJDFDir();
		
    /**
     * returns the name of the IConverterCallback that specifies the converter name
     * @return {@link IConverterCallback} the callback to use, null if none is specified
     */
	public IConverterCallback getCallBackClass();

	/**
	 * get a String representation of this DeviceProperty
	 */
	public String toString();

    /**
     * @return
     */
    public File getOutputHF();
    
    /**
     * if true, qes are returned as MIME, else 
     * @return
     */
    public QEReturn getReturnMIME();

    /**
     * @return
     */
    public File getErrorHF();

    /**
     * @return
     */
    public File getSlaveInputHF();
    /**
     * @return
     */
    public File getSlaveOutputHF();
    /**
     * @return
     */
    public File getSlaveErrorHF();
    /**
     * get the URL to communicate with this device
     * @return the device URL. Send JMFs to this URL, if you want to communicate with this device. 
     */
    public String getSlaveURL();

    /**
     * @return
     */
    public String getSlaveDeviceID();

    /**
     * @return
     */
    public int getMaxPush();

    /**
     * @return the type regular expression that the device accepts
     */
    public String getTypeExpression();

    /**
     * @return the vector of amount counting resource names
     */
    public VString getAmountResources();
    /**
     * @return the default body part encoding
     */
    public String getDeviceMIMEEncoding();

}