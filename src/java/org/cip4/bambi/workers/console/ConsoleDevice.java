/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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

package org.cip4.bambi.workers.console;

import java.io.File;
import java.util.Vector;
import java.util.zip.DataFormatException;

import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.IGetHandler;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.workers.JobPhase;
import org.cip4.bambi.workers.UIModifiableDevice;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.datatypes.JDFIntegerList;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * a simple data input terminal/console JDF device . <br>
 * @author Rainer Prosi
 */
public class ConsoleDevice extends UIModifiableDevice implements IGetHandler
{
	/**
	 * 
	 */

	private static final long serialVersionUID = -8412710163767830461L;
	protected Vector<PhaseAction> actions;

	/**
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * Sep 29, 2009
	 */
	protected class ActionLoader
	{
		private final File actionFile;
		private final File actionFileLocal;
		private File actionPath;

		/**
		 * 
		 */
		public ActionLoader()
		{
			actionFile = new File("actions.xml");
			actionFileLocal = new File("actions_" + getDeviceID() + ".xml");
			actionPath = null;
		}

		protected Vector<PhaseAction> load()
		{
			File deviceDir = getCachedConfigDir();
			Vector<PhaseAction> v = loadFile(deviceDir, true);
			if (v == null)
			{
				v = loadFile(getProperties().getConfigDir(), true);
			}
			if (v == null)
			{
				v = loadFile(deviceDir, false);
			}
			if (v == null)
			{
				v = loadFile(getProperties().getConfigDir(), false);
			}
			if (v != null)
			{
				FileUtil.copyFileToDir(actionPath, deviceDir);
			}
			return v;
		}

		/**
		 * @param deviceDir
		 * @param bLocalDevice 
		 * @return
		 */
		private Vector<PhaseAction> loadFile(final File deviceDir, final boolean bLocalDevice)
		{
			final File actionPathLocal = FileUtil.getFileInDirectory(deviceDir, bLocalDevice ? actionFileLocal : actionFile);
			final XMLDoc d = XMLDoc.parseFile(actionPathLocal.getAbsolutePath());
			final KElement root = d == null ? null : d.getRoot();
			if (root == null)
			{
				return null;
			}
			final VElement v = root.getChildElementVector("PhaseAction", null);
			if (v == null)
			{
				return null;
			}
			actionPath = actionPathLocal;
			final Vector<PhaseAction> vPA = new Vector<PhaseAction>(v.size());
			for (int i = 0; i < v.size(); i++)
			{
				final PhaseAction pa = new PhaseAction(v.get(i));
				pa.pos = i;
				vPA.add(pa);
			}
			setNext(v, vPA);
			return vPA;

		}

		/**
		 * @param v
		 * @param vPA
		 */
		private void setNext(final VElement v, final Vector<PhaseAction> vPA)
		{
			for (int i = 0; i < v.size(); i++)
			{
				final PhaseAction pa = vPA.get(i);
				final String next = v.get(i).getAttribute("Next", null, null);
				if (next != null)
				{
					try
					{
						final JDFIntegerList il = new JDFIntegerList(next);
						for (int ii = 0; ii < il.size(); ii++)
						{
							final int pos = il.getInt(ii);
							if (pos >= 0 && pos < vPA.size())
							{
								pa.next.add(vPA.get(pos));
							}
							else
							{
								getLog().warn("reference out of range in Action #" + i + "ref: " + pos);
							}
						}

					}
					catch (final DataFormatException e)
					{
						getLog().warn("No next steps in Action #" + i, e);
					}
				}
			}
		}

	}

	/**
	 * 
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * Sep 29, 2009
	 */
	protected class PhaseAction
	{
		EnumNodeStatus nodeStatus;
		EnumDeviceStatus deviceStatus;
		String nodeStatusDetails;
		String deviceStatusDetails;
		String name;
		int pos;
		Vector<PhaseAction> next;

		/**
		 * @param action
		 * 
		 */
		public PhaseAction(final KElement action)
		{
			nodeStatus = EnumNodeStatus.getEnum(action.getAttribute("NodeStatus"));
			nodeStatusDetails = action.getAttribute("NodeStatusDetails");
			deviceStatus = EnumDeviceStatus.getEnum(action.getAttribute("DeviceStatus"));
			deviceStatusDetails = action.getAttribute("DeviceStatusDetails");
			name = action.getAttribute(AttributeName.DESCRIPTIVENAME);
			next = new Vector<PhaseAction>();
		}

		public KElement addToRoot(final KElement root)
		{
			final KElement action = root.appendElement("PhaseAction");
			action.setAttribute("NodeStatus", nodeStatus.getName());
			action.setAttribute("NodeStatusDetails", nodeStatusDetails);
			action.setAttribute("DeviceStatus", deviceStatus.getName());
			action.setAttribute("DeviceStatusDetails", deviceStatusDetails);
			action.setAttribute(AttributeName.DESCRIPTIVENAME, name);
			action.setAttribute("pos", pos, null);
			return action;
		}
	}

	/**
	 * @author prosirai
	 */
	protected class XMLConsoleDevice extends XMLWorkerDevice
	{

		/**
		 * XML representation of this simDevice for use as html display using an XSLT
		 * @param bProc
		 * @param request
		 */
		public XMLConsoleDevice(final boolean bProc, final ContainerRequest request)
		{
			super(bProc, request);
			final boolean bSetup = request.getBooleanParam("setup");
			if (!bSetup)
			{
				// TODO
			}
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
		final XMLDevice device = this.new XMLConsoleDevice(bProc, request);
		return device;
	}

	/**
	 * @param prop the properties of the device
	 */
	public ConsoleDevice(final IDeviceProperties prop)
	{
		super(prop);
		log.info("created ConsoleDevice '" + prop.getDeviceID() + "'");
		actions = new ActionLoader().load();
	}

	/**
	 * there is no processor for the terminal
	 * @see org.cip4.bambi.core.AbstractDevice#buildDeviceProcessor()
	 */
	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		return new ConsoleDeviceProcessor();
	}

	/**
	 * 
	 *  
	 * @param request
	 * @return
	 */
	@Override
	public String getXSLT(final ContainerRequest request)
	{
		//		final String contextPath = request.getContextPath();
		//		final String command = request.getCommand();
		//		if (("showDevice".equalsIgnoreCase(command) || "processNextPhase".equalsIgnoreCase(command)) && !request.getBooleanParam("setup"))
		//		{
		//			return getXSLTBaseFromContext(contextPath) + "/showConsole.xsl";
		//		}
		return super.getXSLT(request);
	}

	/**
	 * @param currentAction
	 * @return
	 */
	Vector<PhaseAction> getActions(final int currentAction)
	{
		if (currentAction < 0)
		{
			return actions;
		}
		final PhaseAction a = getAction(currentAction);
		return a == null ? null : a.next;
	}

	@Override
	protected XMLResponse processNextPhase(final ContainerRequest request)
	{
		final JobPhase nextPhase = buildJobPhaseFromRequest(request);
		((ConsoleDeviceProcessor) _deviceProcessors.get(0)).doNextPhase(nextPhase, request);
		ThreadUtil.sleep(223);
		return showDevice(request, false);
	}

	/**
	 * @param currentAction
	 * @return
	 */
	private PhaseAction getAction(final int currentAction)
	{
		if (currentAction < 0 || currentAction >= actions.size())
		{
			return null;
		}
		return actions.get(currentAction);
	}
}