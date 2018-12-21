/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2018 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
package org.cip4.bambi.workers;

import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.StringUtil;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *
 *         Sep 29, 2009
 */
public abstract class UIModifiableDevice extends WorkerDevice
{
	/**
	 * @param prop the properties of the device
	 */
	public UIModifiableDevice(final IDeviceProperties prop)
	{
		super(prop);
	}

	/**
	 * @return
	 */
	public JobPhase getCurrentJobPhase()
	{
		if (_deviceProcessors == null || _deviceProcessors.size() == 0)
		{
			return null;
		}
		return ((UIModifiableDeviceProcessor) _deviceProcessors.get(0)).getCurrentJobPhase();
	}

	/**
	 * build a new job phase with info from a given request. JobPhase parameter 'timeToGo' will remain with its default value 0, since it is not used in the context of ManualDevice.doNextJobPhase()
	 * 
	 * @param request request to get the job phase info from
	 * @return the new JobPhase
	 */
	protected JobPhase buildJobPhaseFromRequest(final ContainerRequest request)
	{
		final JobPhase current = getCurrentJobPhase();
		final JobPhase newPhase = (current == null ? new JobPhase() : current.clone());
		newPhase.setTimeToGo(Integer.MAX_VALUE); // until modified...

		String status = request.getParameter("DeviceStatus");
		if (status != null)
		{
			newPhase.setDeviceStatus(EnumDeviceStatus.getEnum(status));
		}
		newPhase.setDeviceStatusDetails(request.getParameter("DeviceStatusDetails"));

		status = request.getParameter("NodeStatus");
		if (status != null)
		{
			newPhase.setNodeStatus(EnumNodeStatus.getEnum(status));
			if (EnumNodeStatus.Aborted.equals(newPhase.getNodeStatus()) || EnumNodeStatus.Completed.equals(newPhase.getNodeStatus()) || EnumNodeStatus.Suspended.equals(newPhase.getNodeStatus()))
			{
				newPhase.setTimeToGo(0);
			}
		}
		newPhase.setNodeStatusDetails(request.getParameter("NodeStatusDetails"));

		for (int i = 0; i < 10; i++)
		{
			final String parameter = request.getParameter("Res" + i);
			if (parameter == null)
			{
				break;
			}
			newPhase.setAmount(parameter, request.getDoubleParam("Speed" + i), !request.getBooleanParam("Waste" + i));
		}
		if (!KElement.isWildCard(request.getParameter(AttributeName.DURATION)))
		{
			newPhase.setTimeToGo(1000l * (long) request.getDoubleParam(AttributeName.DURATION));
		}
		else if (current != null)
		{
			newPhase.setTimeToGo(current.getTimeToGo());
		}

		return newPhase;
	}

	/**
	 * @param request
	 * @return true if handled
	 */
	@Override
	public XMLResponse handleGet(final ContainerRequest request)
	{
		if (isMyRequest(request))
		{
			if (request.isMyContext("processNextPhase"))
			{
				return processNextPhase(request);
			}
			else if (request.isMyContext("login"))
			{
				return handleLogin(request);
			}
		}
		return super.handleGet(request);
	}

	/**
	 * handle login/logout of employees
	 * 
	 * @param request
	 * @return
	 */
	protected XMLResponse handleLogin(final ContainerRequest request)
	{
		String personalID = StringUtil.getNonEmpty(request.getParameter(AttributeName.PERSONALID));
		if (personalID != null)
		{
			final boolean bLogout = "logout".equals(request.getParameter("inout"));
			personalID = StringUtil.token(personalID, 0, " ");
			if (bLogout)
			{
				getStatusListener().removeEmployee(employees.getEmployee(personalID));
			}
			else
			{
				getStatusListener().addEmployee(employees.getEmployee(personalID));
			}
		}

		return showDevice(request, false);
	}

	/**
	 * @param request
	 * @return
	 */
	protected abstract XMLResponse processNextPhase(ContainerRequest request);

}
