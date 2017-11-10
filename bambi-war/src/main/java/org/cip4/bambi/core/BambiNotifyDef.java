/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class implements default notification mechanism, which is simply empty, thus
 * "no-notifications" strategy. It is implemented as a Singleton so that you
 * always have static access.
 *
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public class BambiNotifyDef implements BambiNotify
{
	protected Log log;

	private static BambiNotifyDef theInstance;
	private static BambiNotify notifier;

	static
	{
		theInstance = new BambiNotifyDef();
	}

	/**
	 * use getInstance from outside
	 */
	protected BambiNotifyDef()
	{
		log = LogFactory.getLog(getClass());
		log.info("Creating default Bambi Notifier");
	}

	public static BambiNotifyDef getInstance()
	{
		return theInstance;
	}

	public void setImpl(final BambiNotify impl)
	{
		log.info("Switch from default to other implementation, impl: " + impl);
		notifier = impl;
	}

	@Override
	public void addListener(final Observer obs)
	{
		if (null != notifier)
		{
			notifier.addListener(obs);
		}
	}

	@Override
	public void removeListener(final Observer obs)
	{
		if (null != notifier)
		{
			notifier.removeListener(obs);
		}
	}

	@Override
	public void notifyDeviceJobAdded(final String deviceId, final String jobId, final String status, final long submission)
	{
		if (null != notifier)
		{
			notifier.notifyDeviceJobAdded(deviceId, jobId, status, submission);
		}
	}

	@Override
	public void notifyDeviceJobRemoved(final String deviceId, final String jobId)
	{
		if (null != notifier)
		{
			notifier.notifyDeviceJobRemoved(deviceId, jobId);
		}
	}

	@Override
	public void notifyDeviceQueueStatus(final String deviceId, final String queueStatus, final String queueStatistic)
	{
		if (null != notifier)
		{
			notifier.notifyDeviceQueueStatus(deviceId, queueStatus, queueStatistic);
		}
	}

	@Override
	public void notifyDeviceJobPropertiesChanged(final String deviceId, final String jobId, final String status, final long start, final long end)
	{
		if (null != notifier)
		{
			notifier.notifyDeviceJobPropertiesChanged(deviceId, jobId, status, start, end);
		}
	}

}
