/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2019 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

package org.cip4.bambi.workers.sim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.XMLDevice;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.workers.JobPhase;
import org.cip4.bambi.workers.UIModifiableDevice;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResourceInfo;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * a simple JDF device with a fixed list of job phases. <br>
 * Job phases are defined in <code>/WebContend/config/devices.xml</code> and loaded in the constructor. They can be randomized, and random error phases can be added. An example job phase is provided
 * in <code>example_job.xml</code>.<br>
 * This class should remain final: if it is ever subclassed, the DeviceProcessor thread would be started before the constructor from the subclass has a chance to fire.
 *
 * @author boegerni
 */
public class SimDevice extends UIModifiableDevice implements IGetHandler
{
	/**
	 *
	 * resource query catalog
	 *
	 * @author rainer prosi
	 * @date Mar 18, 2012
	 */
	public class ResourceQueryHandler extends ResourceHandler
	{
		protected List<JDFResourceInfo> vResInfo;

		/**
		 *
		 * @param respCopy
		 */
		public ResourceQueryHandler(final JDFJMF respCopy)
		{
			super();
			final JDFResponse resp = respCopy == null ? null : respCopy.getResponse(0);
			if (resp == null)
			{
				log.error("No resource List available");
				vResInfo = new ArrayList<>();
			}
			else
			{
				final List<JDFResourceInfo> vTmp = resp.getChildArrayByClass(JDFResourceInfo.class, false, 0);
				if (vTmp == null)
				{
					log.error("No resourceInfo elements available");
					vResInfo = new ArrayList<>();
				}
				else
				{
					vResInfo = vTmp;
					log.info("parsed resource info with " + vResInfo.size() + " elements");
				}
			}
		}

		/**
		 *
		 * @see org.cip4.bambi.core.AbstractDevice.ResourceHandler#getResourceList(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean getResourceList(final JDFMessage inMessage, final JDFResponse response)
		{
			final JDFResourceQuParams rqp = inMessage.getResourceQuParams();
			if (rqp == null)
			{
				log.info("no resourceQuParams in message; assume general query ID=" + inMessage.getID());
			}
			for (final JDFResourceInfo ri : vResInfo)
			{
				if (ri.matches(rqp))
				{
					response.copyElement(ri, null);
				}
			}
			return true;
		}
	}

	/**
	 * @Override
	 * @see org.cip4.bambi.core.AbstractDevice#addHandlers()
	 */
	@Override
	protected void addHandlers()
	{
		super.addHandlers();
		addResourceQueryHandler();
	}

	/**
	 *
	 */
	protected void addResourceQueryHandler()
	{
		final File cacheDir = getCachedConfigDir();
		File deviceFile = FileUtil.getFileInDirectory(cacheDir, new File("resinfo_" + getDeviceID() + ".xml"));
		if (!deviceFile.canRead())
		{
			deviceFile = FileUtil.getFileInDirectory(cacheDir, new File("resinfo.xml"));
			log.info("defaulting to generic file " + deviceFile.getAbsolutePath());
		}
		final JDFDoc doc = JDFDoc.parseFile(deviceFile);
		final JDFJMF jmf = doc == null ? null : doc.getJMFRoot();
		if (jmf == null)
		{
			log.warn("no resource info file at: " + deviceFile.getAbsolutePath());
		}
		else
		{
			log.info("parsing resource info file at: " + deviceFile.getAbsolutePath());
			final ResourceQueryHandler resourceQueryHandler = new ResourceQueryHandler(jmf);
			getJMFHandler(null).addHandler(resourceQueryHandler);
		}
	}

	/**
	 * @param bProc if true add processors
	 * @param request
	 * @return
	 */
	@Override
	public XMLDevice getXMLDevice(final boolean bProc, final ContainerRequest request)
	{
		final XMLDevice simDevice = new XMLSimDevice(this, bProc, request);
		return simDevice;
	}

	/**
	 *
	 * @see org.cip4.bambi.workers.WorkerDevice#processNextPhase(org.cip4.bambi.core.ContainerRequest)
	 */
	@Override
	protected XMLResponse processNextPhase(final ContainerRequest request)
	{
		final JobPhase nextPhase = buildJobPhaseFromRequest(request);
		((SimDeviceProcessor) _deviceProcessors.get(0)).doNextPhase(nextPhase);
		ThreadUtil.sleep(500); // allow device to switch phases before displaying page
		return showDevice(request, false);
	}

	/**
	 * @param prop the properties of the device
	 */
	public SimDevice(final IDeviceProperties prop)
	{
		super(prop);
	}

	/**
	 *
	 * @see org.cip4.bambi.core.AbstractDevice#buildDeviceProcessor()
	 */
	@Override
	protected SimDeviceProcessor buildDeviceProcessor()
	{
		return new SimDeviceProcessor();
	}

	/**
	 *
	 */
	private void updateTypeExpression(final String newTypeX)
	{
		final IDeviceProperties properties = getProperties();
		final String old = properties.getTypeExpression();
		if (!ContainerUtil.equals(old, newTypeX))
		{
			properties.setTypeExpression(newTypeX);
			properties.serialize();
		}
	}

	/**
	 * @param request
	 */
	@Override
	protected void updateDevice(final ContainerRequest request)
	{
		super.updateDevice(request);

		final JDFAttributeMap map = request.getParameterMap();
		final Set<String> s = map == null ? null : map.keySet();
		if (s == null)
			return;

		final String exp = request.getParameter(AttributeName.TYPEEXPRESSION);
		if (exp != null && s.contains(AttributeName.TYPEEXPRESSION))
		{
			updateTypeExpression(exp);
		}
	}
}