/*
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.server.RuntimeProperties;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.util.PlatformUtil;

/**
 * @author rainer prosi
 */
public class XMLDevice extends XMLDoc
{

	/**
	 *
	 */
	private final AbstractDevice parentDevice;

	private final static Log log = LogFactory.getLog(XMLDevice.class);

	/**
	 * XML representation of this simDevice fore use as html display using an XSLT
	 *
	 * @param addProcs if true, add processor elements
	 * @param request
	 * @param abstractDevice TODO
	 */
	protected XMLDevice(final AbstractDevice abstractDevice, final boolean addProcs, final ContainerRequest request)
	{
		super("XMLDevice", null);
		parentDevice = abstractDevice;
		prepare();
		final KElement deviceRoot = getRoot();
		setXSLTURL(parentDevice.getXSLT(request));
		final boolean bModify = request.getBooleanParam("modify");
		deviceRoot.setAttribute("modify", bModify, null);
		deviceRoot.setAttribute(AttributeName.CONTEXT, parentDevice.getContext(request));

		setRoot(deviceRoot);
		addHotFolders(deviceRoot);
		addQueueInfo(deviceRoot);
		if (addProcs)
		{
			addProcessors();
		}
	}

	/**
	 *
	 * @param request
	 * @param deviceRoot
	 */
	protected void setRoot(final KElement deviceRoot)
	{
		deviceRoot.setAttribute("mutable", parentDevice.isMutable(), null);
		deviceRoot.setAttribute("NumRequests", parentDevice.numRequests, null);
		deviceRoot.setAttribute("EntriesProcessed", parentDevice.getEntriesProcessed(), null);
		deviceRoot.setAttribute(RootDevice.RELEASE_VERSION_STRING, RuntimeProperties.getProductVersion(), null);
		deviceRoot.setAttribute(RootDevice.RELEASE_TIMESTAMP_STRING, RuntimeProperties.getProductBuildTimestamp(), null);
		deviceRoot.setAttribute(RootDevice.RELEASE_BUILD_NUMBER_STRING, RuntimeProperties.getProductBuildNumber(), null);
		deviceRoot.setAttribute("JdfLibVersion", parentDevice.getAgentVersion(), null);
		deviceRoot.copyElement(parentDevice.getDeviceTimer(true).toXML(), null);
		deviceRoot.setAttribute(AttributeName.DEVICEID, parentDevice.getDeviceID());
		deviceRoot.setAttribute(AttributeName.DEVICETYPE, parentDevice.getDeviceType());
		deviceRoot.setAttribute("Description", parentDevice.getDescription());
		deviceRoot.setAttribute("DeviceURL", parentDevice.getDeviceURL());
		final IDeviceProperties properties = parentDevice.getProperties();
		deviceRoot.setAttribute("WatchURL", properties.getWatchURL());
		deviceRoot.setAttribute("WatchFormat", properties.getWatchFormat().name());
		deviceRoot.setAttribute("JavaVersion", PlatformUtil.getProperty("java.version"));
		deviceRoot.setAttribute(AttributeName.DEVICESTATUS, parentDevice.getDeviceStatus().getName());
		if (parentDevice._rootDevice == null && BambiContainer.getInstance() != null)
		{
			deviceRoot.setAttribute("Dump", BambiContainer.getInstance().bWantDump, null);
		}
	}

	/**
	 * @param deviceRoot
	 */
	protected void addHotFolders(final KElement deviceRoot)
	{
		final File inputHF = parentDevice.getInputHFUrl();
		if (inputHF != null)
		{
			deviceRoot.setAttribute("InputHF", inputHF.getPath());
		}
		final IDeviceProperties properties = parentDevice.getProperties();
		final File outputHF = properties.getOutputHF();
		if (outputHF != null)
		{
			deviceRoot.setAttribute("OutputHF", outputHF.getPath());
		}
		final File errorHF = properties.getErrorHF();
		if (errorHF != null)
		{
			deviceRoot.setAttribute("ErrorHF", errorHF.getPath());
		}
	}

	/**
	 * @param deviceRoot
	 */
	protected void addQueueInfo(final KElement deviceRoot)
	{
		if (parentDevice._theQueueProcessor == null)
		{
			log.error("device with null queueprocessor - bailing out: ID=" + parentDevice.getDeviceID());
		}
		else
		{
			final JDFQueue jdfQueue = parentDevice._theQueueProcessor.getQueue();
			final EnumQueueStatus queueStatus = jdfQueue == null ? null : jdfQueue.getQueueStatus();
			final int running = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Running);
			final int waiting = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Waiting) + jdfQueue.numEntries(EnumQueueEntryStatus.Suspended);
			final int completed = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Completed) + jdfQueue.numEntries(EnumQueueEntryStatus.Aborted);
			final int all = jdfQueue == null ? 0 : BambiNSExtension.getTotal(jdfQueue);

			deviceRoot.setAttribute("QueueStatus", queueStatus == null ? "Unknown" : queueStatus.getName());
			deviceRoot.setAttribute("QueueWaiting", waiting, null);
			deviceRoot.setAttribute("QueueRunning", running, null);
			deviceRoot.setAttribute("QueueCompleted", completed, null);
			deviceRoot.setAttribute("QueueAll", all, null);
		}
	}

	/**
	 * hook to call any preparation setup prior to constructing
	 */
	protected void prepare()
	{
		// nop
	}

	/**
	 *
	 */
	protected void addProcessors()
	{
		for (final AbstractDeviceProcessor proc : parentDevice._deviceProcessors)
		{
			proc.addToDisplayXML(getRoot());
		}
	}

	/**
	 *
	 * @return
	 */
	protected AbstractDevice getParentDevice()
	{
		return parentDevice;
	}
}