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
package org.cip4.bambi.server.mockImpl;

import java.util.ArrayList;
import java.util.List;

import org.cip4.bambi.core.BambiNotify;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.Observer;
import org.json.JSONObject;
import org.json.XML;

/**
 * class that handles all bambi JDF/JMF requests - regardless of the servlet context
 * previously part of {@link BambiServlet}
 * it is implemented as a Singleton so that you always have static access
 * 
 * note that the get handling routines still assume a servlet context - only the actual JDF / JMF post does not
 * @author Rainer Prosi, Heidelberger Druckmaschinen 
 */
public final class BambiRealNotify extends BambiNotify
{
	private final List<Observer> observersList;

	/**
	 * use getCreateInstance from outside
	 */
	private BambiRealNotify()
	{
		super();
		log.info("Creating Bambi Notifier");
		observersList = new ArrayList<Observer>();
	}

	/**
	 * 
	 *  
	 * @return the singleton bambi container instance
	 */
	public synchronized static BambiNotify getCreateInstance()
	{
		if (theInstance == null || !(theInstance instanceof BambiRealNotify))
		{
			theInstance = new BambiRealNotify();
			((BambiRealNotify) theInstance).log.info("created new real Singleton bambi notifier");
		}

		return theInstance;
	}

	/**
	 * 
	 * @param obs
	 */
	@Override
	public void addListener(final Observer obs)
	{
		log.info("addListener obs:" + obs);
		observersList.add(obs);
	}

	/**
	 * 
	 * @param obs
	 */
	@Override
	public void removeListener(final Observer obs)
	{
		log.info("removeListener obs:" + obs);
		observersList.remove(obs);
	}

	/**
	 * 
	 * @param deviceId
	 * @param jobId
	 * @param status
	 * @param submission
	 */
	@Override
	public void notifyDeviceJobAdded(String deviceId, String jobId, String status, String submission)
	{
		String notifyXml = "<AddDeviceJob " + "deviceId='" + deviceId + "' " + "jobid='" + jobId + "'" + "status='" + status + "'" + "submission='" + submission + "'" + ">"
				+ "</AddDeviceJob>";
		prepareAndPushNotificationMessage(notifyXml);
	}

	/**
	 * 
	 * @param deviceId
	 * @param jobId
	 */
	@Override
	public void notifyDeviceJobRemoved(String deviceId, String jobId)
	{
		String notifyXml = "<DeleteDeviceJob " + "deviceId='" + deviceId + "' " + "jobid='" + jobId + "'" + ">" + "</DeleteDeviceJob>";
		prepareAndPushNotificationMessage(notifyXml);
	}

	@Override
	public void notifyDeviceQueueStatus(String deviceId, String queueStatus, String queueStatistic)
	{
		String updateQueueXml = "<UpdateDeviceQueue deviceId='" + deviceId + "' " + "queueStatus='" + queueStatus + "' " + "queueStatistic='" + queueStatistic + "'" + ">"
				+ "</UpdateDeviceQueue>";

		prepareAndPushNotificationMessage(updateQueueXml);
	}

	/**
	 * 
	 * @param deviceId
	 * @param jobId
	 * @param status
	 * @param start
	 * @param end
	 */
	@Override
	public void notifyDeviceJobPropertiesChanged(String deviceId, String jobId, String status, String start, String end)
	{
		String notifyXml = "<JobPropertiesChanged " + "deviceId='" + deviceId + "' " + "jobId='" + jobId + "' " + "status='" + status + "'" + "start='" + start + "'" + "end='"
				+ end + "'" + ">" + "</JobPropertiesChanged>";

		prepareAndPushNotificationMessage(notifyXml);
	}

	/**
	 * 
	 * @param message
	 */
	private void prepareAndPushNotificationMessage(String message)
	{
		JSONObject jsonObj = XML.toJSONObject(message);
		log.debug("jsonObj.toString: " + jsonObj.toString());

		for (Observer obs : observersList)
		{
			obs.pushData(jsonObj.toString());
		}
	}

}
