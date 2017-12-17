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
package org.cip4.bambi.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.cip4.bambi.actions.beans.DeviceJob;
import org.cip4.bambi.actions.beans.XMLDevice;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.queues.QueueProcessor.QueueStatistic;
import org.cip4.bambi.settings.BambiServerUtils;
import org.cip4.jdflib.jmf.JDFQueueEntry;

import com.opensymphony.xwork2.ActionSupport;

public class HomeAction2 extends ActionSupport implements ServletRequestAware
{
	private static final Logger LOG = Logger.getLogger(HomeAction2.class);

	private static final BambiContainer theContainer = BambiContainer.getInstance();

	private HttpServletRequest request;

	private final List<XMLDevice> deviceList = new ArrayList<XMLDevice>();

	@Override
	public String execute() throws Exception
	{
		LOG.info("theContainer action: " + theContainer);

		String pageName = SUCCESS;

		if (theContainer == null)
		{
			pageName = ERROR;
		}
		else
		{
			LOG.info("... some work occured here");

			//			AbstractDevice rootDev = theContainer.getRootDev();
			//			rootDev.startWork();

			final boolean isPost = request.getMethod().equalsIgnoreCase("POST") ? true : false;

			//			rootDev.endWork();
		}

		final List<String> devices = theContainer.getDevices();

		for (final String deviceId : devices)
		{
			final AbstractDevice device = theContainer.getDeviceFromID(deviceId);

			final XMLDevice deviceModel = new XMLDevice();
			deviceModel.setDeviceId(deviceId);
			deviceModel.setDeviceStatus(device.getDeviceStatus().getName());

			if (null == device.getQueueProcessor().getQueue().getStatus())
			{
				deviceModel.setQueueStatus("Held" /*EnumQueueEntryStatus.Held.getName()*/);
			}
			else
			{
				deviceModel.setQueueStatus(device.getQueueProcessor().getQueue().getStatus().getName());
			}
			final QueueStatistic queueStatistic2 = device.getQueueProcessor().getQueueStatistic2();
			deviceModel.setQueueWaiting(queueStatistic2.waiting);
			deviceModel.setQueueRunning(queueStatistic2.running);
			deviceModel.setQueueCompleted(queueStatistic2.completed);
			deviceModel.setQueueAll(queueStatistic2.all);

			deviceList.add(deviceModel);
		}
		fillDevicesQueue();

		return pageName;
	}

	private void fillDevicesQueue()
	{
		for (final XMLDevice device : deviceList)
		{
			final Collection<JDFQueueEntry> queueCurrent = theContainer.getDeviceFromID(device.getDeviceId()).getQueueProcessor().getQueue().getAllQueueEntry();
			LOG.info("Device: '" + device.getDeviceId() + "' has queue size: " + queueCurrent.size());

			final List<DeviceJob> queue = new ArrayList<DeviceJob>();
			final Iterator<JDFQueueEntry> it = queueCurrent.iterator();
			while (it.hasNext())
			{
				final JDFQueueEntry jdfJob = it.next();

				final DeviceJob job = new DeviceJob();
				job.setJobId(jdfJob.getQueueEntryID());
				job.setPriority("" + jdfJob.getPriority());
				job.setStatus(jdfJob.getQueueEntryStatus().getName());
				job.setSubmitted(BambiServerUtils.convertTime(jdfJob.getSubmissionTime().getTimeInMillis()));
				job.setStarted(BambiServerUtils.convertTime(getStartTime(jdfJob)));
				job.setEnded(BambiServerUtils.convertTime(getEndTime(jdfJob)));

				queue.add(job);
			}

			Collections.reverse(queue);
			device.setJobsQueue(queue);
		}
	}

	public List<XMLDevice> getDevices()
	{
		return deviceList;
	}

	@Override
	public void setServletRequest(final HttpServletRequest httpServletRequest)
	{
		request = httpServletRequest;
	}

	private long getStartTime(final JDFQueueEntry qe)
	{
		if (qe.getStartTime() == null)
		{
			return 0;
		}
		return qe.getStartTime().getTimeInMillis();
	}

	private long getEndTime(final JDFQueueEntry qe)
	{
		if (qe.getEndTime() == null)
		{
			return 0;
		}
		return qe.getEndTime().getTimeInMillis();
	}
}
