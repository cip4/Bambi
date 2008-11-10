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

package org.cip4.bambi.workers.sim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.bambi.core.BambiServletRequest;
import org.cip4.bambi.core.BambiServletResponse;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.workers.sim.SimDeviceProcessor.JobPhase;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * a simple JDF device with a fixed list of job phases. <br>
 * Job phases are defined in <code>/WebContend/config/devices.xml</code> and loaded in the constructor. They can be randomized, and random error phases can be
 * added. An example job phase is provided in <code>example_job.xml</code>.<br>
 * This class should remain final: if it is ever subclassed, the DeviceProcessor thread would be started before the constructor from the subclass has a chance
 * to fire.
 * @author boegerni
 */
public class SimDevice extends AbstractDevice implements IGetHandler
{
	/**
	 * 
	 */

	private static final long serialVersionUID = -8412710163767830461L;
	private static Log log = LogFactory.getLog(SimDevice.class.getName());
	protected String _trackResource = null; // the "major" resource to track
	protected VString amountResources = null;
	protected String _typeExpression = null; // the regexp that defines the valid types

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#canAccept(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public boolean canAccept(final JDFDoc doc)
	{
		if (doc != null && _typeExpression == null)
		{
			return true;
		}
		return getAcceptableNodes(doc) != null;
	}

	@Override
	public JDFNode getNodeFromDoc(final JDFDoc doc)
	{
		final VElement v = getAcceptableNodes(doc);
		return (JDFNode) (v == null ? null : v.get(0));
	}

	/**
	 * @param doc
	 * @return
	 */
	public VElement getAcceptableNodes(final JDFDoc doc)
	{
		// TODO plug in devcaps
		if (doc == null)
		{
			return null;
		}

		final JDFNode n = doc.getJDFRoot();
		if (n == null)
		{
			return null;
		}
		final VElement v = n.getvJDFNode(null, null, false);
		for (int i = v.size() - 1; i >= 0; i--)
		{
			final JDFNode n2 = (JDFNode) v.elementAt(i);
			if (!canAccept(n2))
			{
				v.remove(n2);
			}
		}
		return v.size() == 0 ? null : v;
	}

	/**
	 * @param n2
	 */
	private boolean canAccept(final JDFNode n2)
	{
		final String types = n2.getTypesString();
		return StringUtil.matches(types, _typeExpression);
	}

	/**
	 * @author prosirai
	 */
	protected class XMLSimDevice extends XMLDevice
	{

		private final JobPhase currentJobPhase;

		/**
		 * XML representation of this simDevice fore use as html display using an XSLT
		 * @param dev
		 */
		public XMLSimDevice(final BambiServletRequest request)
		{
			super(true, request);

			currentJobPhase = getCurrentJobPhase();
			if (currentJobPhase != null)
			{
				addPhase();
			}
		}

		/**
		 * @param currentJobPhase
		 * @return
		 */
		private KElement addPhase()
		{
			final KElement root = getRoot();
			final KElement phase = root.appendElement("Phase");

			final EnumDeviceStatus deviceStatus = currentJobPhase.getDeviceStatus();
			final EnumNodeStatus nodeStatus = currentJobPhase.getNodeStatus();
			if (deviceStatus != null && nodeStatus != null)
			{
				phase.setAttribute("DeviceStatus", deviceStatus.getName(), null);
				phase.setAttribute("DeviceStatusDetails", currentJobPhase.getDeviceStatusDetails());
				phase.setAttribute("NodeStatus", nodeStatus.getName(), null);
				phase.setAttribute("NodeStatusDetails", currentJobPhase.getNodeStatusDetails());
				phase.setAttribute(AttributeName.DURATION, currentJobPhase.getTimeToGo() / 1000., null);
				final VString v = currentJobPhase.getAmountResourceNames();
				final int vSiz = v == null ? 0 : v.size();
				for (int i = 0; i < vSiz; i++)
				{
					addAmount(v.stringAt(i), phase);
				}
				BambiServlet.addOptionList(deviceStatus, EnumDeviceStatus.getEnumList(), phase, "DeviceStatus");
				BambiServlet.addOptionList(nodeStatus, EnumNodeStatus.getEnumList(), phase, "NodeStatus");
			}
			else
			{
				log.error("null status - bailing out");
			}
			return null;
		}

		/**
		 * @param string
		 */
		private void addAmount(final String resString, final KElement jp)
		{
			if (jp == null)
			{
				return;
			}
			final KElement amount = jp.appendElement("ResourceAmount");
			amount.setAttribute("ResourceName", resString);
			amount.setAttribute("ResourceIndex", jp.numChildElements("ResourceAmount", null) - 1, null);
			amount.setAttribute("Waste", !currentJobPhase.getOutput_Condition(resString), null);
			amount.setAttribute("Speed", currentJobPhase.getOutput_Speed(resString), null);
		}
	}

	public String getTrackResource()
	{
		return _trackResource;
	}

	/**
	 * check whether this resource should track amounts
	 * @param resLink
	 * @return
	 */
	boolean isAmountResource(final JDFResourceLink resLink)
	{
		if (resLink == null || amountResources == null)
		{
			return false;
		}
		for (int i = 0; i < amountResources.size(); i++)
		{
			if (resLink.matchesString(amountResources.get(i)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param request
	 * @return
	 */
	@Override
	protected XMLDevice getSimDevice(final BambiServletRequest request)
	{
		final XMLDevice simDevice = this.new XMLSimDevice(request);
		return simDevice;
	}

	public JobPhase getCurrentJobPhase()
	{
		if (_deviceProcessors == null || _deviceProcessors.size() == 0)
		{
			return null;
		}
		return ((SimDeviceProcessor) _deviceProcessors.get(0)).getCurrentJobPhase();
	}

	/**
	 * build a new job phase with info from a given request. JobPhase parameter 'timeToGo' will remain with its default value 0, since it is not used in the
	 * context of ManualDevice.doNextJobPhase()
	 * @param request request to get the job phase info from
	 * @return the new JobPhase
	 */
	private JobPhase buildJobPhaseFromRequest(final BambiServletRequest request)
	{
		final JobPhase current = getCurrentJobPhase();

		final JobPhase newPhase = (JobPhase) (current == null ? new JobPhase() : current.clone());
		newPhase.timeToGo = Integer.MAX_VALUE; // until modified...

		String status = request.getParameter("DeviceStatus");
		if (status != null)
		{
			newPhase.deviceStatus = EnumDeviceStatus.getEnum(status);
		}
		newPhase.deviceStatusDetails = request.getParameter("DeviceStatusDetails");

		status = request.getParameter("NodeStatus");
		if (status != null)
		{
			newPhase.nodeStatus = EnumNodeStatus.getEnum(status);
			if (EnumNodeStatus.Aborted.equals(newPhase.nodeStatus) || EnumNodeStatus.Completed.equals(newPhase.nodeStatus) || EnumNodeStatus.Suspended.equals(newPhase.nodeStatus))
			{
				newPhase.timeToGo = 0;
			}

		}
		newPhase.nodeStatusDetails = request.getParameter("NodeStatusDetails");

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
			newPhase.setTimeToGo(1000 * (int) request.getDoubleParam(AttributeName.DURATION));
		}
		else if (current != null)
		{
			newPhase.setTimeToGo(current.getTimeToGo());
		}

		return newPhase;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#handleGet(org.cip4.bambi.core.BambiServletRequest, org.cip4.bambi.core.BambiServletResponse)
	 * @param request
	 * @param response
	 * @return true if handled
	 */
	@Override
	public boolean handleGet(final BambiServletRequest request, final BambiServletResponse response)
	{
		if (isMyRequest(request) && BambiServlet.isMyContext(request, "processNextPhase"))
		{
			return processNextPhase(request, response);
		}
		return super.handleGet(request, response);
	}

	private boolean processNextPhase(final BambiServletRequest request, final BambiServletResponse response)
	{
		final JobPhase nextPhase = buildJobPhaseFromRequest(request);
		((SimDeviceProcessor) _deviceProcessors.get(0)).doNextPhase(nextPhase);
		ThreadUtil.sleep(1000); // allow device to switch phases before displaying page
		showDevice(request, response, false);
		return true;
	}

	/**
	 * @param prop the properties of the device
	 */
	public SimDevice(final IDeviceProperties prop)
	{
		super(prop);
		_trackResource = prop.getTrackResource();
		_typeExpression = prop.getTypeExpression();
		amountResources = prop.getAmountResources();
		log.info("created SimDevice '" + prop.getDeviceID() + "'");
	}

	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		return new SimDeviceProcessor();
	}

	/**
	 * reload the queue
	 */
	@Override
	protected void reloadQueue()
	{
		// nop
	}

}