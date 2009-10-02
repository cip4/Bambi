/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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
package org.cip4.bambi.workers;

import java.io.Serializable;
import java.util.Vector;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.BambiServlet;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.cip4.jdflib.util.StringUtil;

/**
 * a single job phase
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * September 29, 2009
 */
public class JobPhase extends BambiLogFactory implements Cloneable
{
	/**
		 * 
		 */
	public class PhaseEmployees
	{
		/**
			 * 
			 */
		Vector<JDFEmployee> theEmployee = new Vector<JDFEmployee>();

		/**
		 * @param e
		 */
		public PhaseEmployees(final KElement e)
		{
			if (e != null)
			{
				final VElement v = e.getChildElementVector(ElementName.EMPLOYEE, null);
				for (int i = 0; i < v.size(); i++)
				{
					final JDFEmployee emp = (JDFEmployee) new JDFDoc(ElementName.EMPLOYEE).getRoot();
					emp.mergeElement(v.get(i), false);
					theEmployee.add(emp);
				}
			}
		}

		/**
		 * @return
		 */
		public Vector<JDFEmployee> getEmployees()
		{
			return theEmployee;
		}
	}

	/**
		 * 
		 */
	public class PhaseAmount implements Serializable
	{
		private static final long serialVersionUID = -8504631585951268571L;

		/**
		 * waste to be produced in this job phase
		 */
		protected boolean bGood = true;
		/**
		 * current speed/hour in this phase
		 */
		protected double speed = 0;

		private String resource = "Output";
		protected String resourceName = "Output";

		/**
		 * @param resName the name or named process usage of the resource
		 * @param _speed speed / hour
		 * @param condition good =true
		 */
		PhaseAmount(final String resName, final double _speed, final boolean condition)
		{
			resource = resourceName = resName;
			bGood = condition;
			speed = _speed;
		}

		/**
		 * @see java.lang.Object#toString()
		 * @return String the string
		 */
		@Override
		public String toString()
		{
			return "[ " + resourceName + " " + resource + (bGood ? " G: " : " W: ") + "Speed: " + speed + "]";
		}

		/**
		 * @param res
		 * @return true if this pjhaseAmount matches res
		 */
		public boolean matchesRes(final String res)
		{
			return resource.equals(res) || resourceName.equals(res);
		}

		@Override
		protected Object clone()
		{
			final PhaseAmount pa = new PhaseAmount(null, speed, bGood);
			pa.resource = resource;
			pa.resourceName = resourceName;
			return pa;
		}

		/**
		 * @return the resource
		 */
		public String getResource()
		{
			return resource;
		}

		/**
		 * @param _resource
		 */
		public void setResource(final String _resource)
		{
			resource = _resource;

		}

	}

	// end of inner class PhaseAmount
	protected Vector<PhaseAmount> amounts = new Vector<PhaseAmount>();

	/**
	 * construction of a JobPhase
	 */
	public JobPhase()
	{
		super();
	}

	/**
	 * 
	 * @param phaseElement the xml element to parse
	 */
	public JobPhase(final KElement phaseElement)
	{
		super();
		deviceStatus = EnumDeviceStatus.getEnum(phaseElement.getXPathAttribute("@DeviceStatus", "Idle"));
		deviceStatusDetails = phaseElement.getXPathAttribute("@DeviceStatusDetails", "");
		nodeStatus = EnumNodeStatus.getEnum(phaseElement.getXPathAttribute("@NodeStatus", "Waiting"));
		nodeStatusDetails = phaseElement.getXPathAttribute("@NodeStatusDetails", "");
		timeToGo = 1000 * StringUtil.parseInt(phaseElement.getXPathAttribute("@Duration", "0"), 0);
		employee = new PhaseEmployees(phaseElement);

		if (phaseElement.hasAttribute("Error"))
		{
			setErrorChance(StringUtil.parseDouble(phaseElement.getXPathAttribute("@Error", "0"), 0) * 0.001);
		}
		else
		{
			setErrorChance(StringUtil.parseDouble(phaseElement.getXPathAttribute("../@Error", "0"), 0) * 0.001);
		}
		final VElement vA = phaseElement.getChildElementVector("Amount", null);
		for (int j = 0; j < vA.size(); j++)
		{
			final KElement am = vA.elementAt(j);
			double speed = am.getRealAttribute("Speed", null, 0);
			if (speed < 0)
			{
				speed = 0;
			}
			final boolean bGood = !am.getBoolAttribute("Waste", null, false);
			// timeToGo is seconds, speed is / hour
			setAmount(am.getAttribute("Resource"), speed, bGood);
		}
	}

	/**
	 * status to be displayed for this job phase
	 */
	protected EnumDeviceStatus deviceStatus = EnumDeviceStatus.Idle;

	/**
	 * device status details
	 */
	protected String deviceStatusDetails = "";

	protected EnumNodeStatus nodeStatus = EnumNodeStatus.Waiting;
	protected String nodeStatusDetails = "";

	/**
	 * timeToGo of job phase in milliseconds
	 */
	protected long timeToGo = 0;
	protected long timeStarted = System.currentTimeMillis();
	private double errorChance = 0.00;
	protected PhaseEmployees employee = null;

	/**
	 * @see java.lang.Object#toString()
	 * @return String the string
	 */
	@Override
	public String toString()
	{
		String s = "[JobPhase: Duration=" + timeToGo + ", DeviceStatus=" + deviceStatus.getName() + ", DeviceStatusDetails=" + deviceStatusDetails + ", NodeStatus=" + nodeStatus.getName()
				+ ", NodeStatusDetails=" + nodeStatusDetails;
		for (int i = 0; i < amounts.size(); i++)
		{
			s += "\n" + amounts.elementAt(i);
		}
		return s;
	}

	/**
	 * @return EnumDeviceStatus the deviceStatus
	 */
	public EnumDeviceStatus getDeviceStatus()
	{
		return deviceStatus;
	}

	/**
	 * @param _deviceStatus the device statis to set
	 */
	public void setDeviceStatus(final EnumDeviceStatus _deviceStatus)
	{
		this.deviceStatus = _deviceStatus;
	}

	/**
	 * @return
	 */
	public String getDeviceStatusDetails()
	{
		return deviceStatusDetails;
	}

	/**
	 * @param _deviceStatusDetails
	 */
	public void setDeviceStatusDetails(final String _deviceStatusDetails)
	{
		this.deviceStatusDetails = _deviceStatusDetails;
	}

	/**
	 * @return
	 */
	public EnumNodeStatus getNodeStatus()
	{
		return nodeStatus;
	}

	/**
	 * @param _nodeStatus
	 */
	public void setNodeStatus(final EnumNodeStatus _nodeStatus)
	{
		this.nodeStatus = _nodeStatus;
	}

	/**
	 * @return
	 */
	public String getNodeStatusDetails()
	{
		return nodeStatusDetails;
	}

	/**
	 * @param _nodeStatusDetails
	 */
	public void setNodeStatusDetails(final String _nodeStatusDetails)
	{
		this.nodeStatusDetails = _nodeStatusDetails;
	}

	/**
	 * @return
	 */
	public long getTimeToGo()
	{
		return timeToGo;
	}

	/**
	 * @param duration
	 */
	public void setTimeToGo(final long duration)
	{
		this.timeToGo = duration;
	}

	/**
	 * @param resName
	 * @param speed
	 * @param bGood
	 * @return
	 */
	public PhaseAmount setAmount(final String resName, final double speed, final boolean bGood)
	{
		PhaseAmount pa = getPhaseAmount(resName);
		if (pa == null)
		{
			pa = this.new PhaseAmount(resName, speed, bGood);
			amounts.add(pa);
		}
		else
		{
			pa.bGood = bGood;
			pa.speed = speed;
		}
		return pa;
	}

	/**
	 * @param res
	 * @return
	 */
	public double getOutput_Speed(final String res)
	{
		final PhaseAmount pa = getPhaseAmount(res);
		return pa == null ? 0 : pa.speed;
	}

	/**
	 * @param res
	 * @return
	 */
	public boolean getOutput_Condition(final String res)
	{
		final PhaseAmount pa = getPhaseAmount(res);
		return pa == null ? true : pa.bGood;
	}

	/**
	 * @param res
	 * @return
	 */
	public PhaseAmount getPhaseAmount(final String res)
	{
		for (int i = 0; i < amounts.size(); i++)
		{
			if (amounts.elementAt(i).matchesRes(res))
			{
				return amounts.elementAt(i);
			}
		}
		return null;
	}

	/**
	 * @return the list of amount counting resources in this phase
	 */
	public VString getAmountResourceNames()
	{
		final VString v = new VString();
		for (int i = 0; i < amounts.size(); i++)
		{
			v.add(amounts.elementAt(i).resourceName);
		}
		return v;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JobPhase clone()
	{
		final JobPhase jp = new JobPhase();
		jp.deviceStatus = deviceStatus;
		jp.deviceStatusDetails = deviceStatusDetails;
		jp.timeToGo = timeToGo;
		jp.nodeStatus = nodeStatus;
		jp.nodeStatusDetails = nodeStatusDetails;
		jp.setErrorChance(errorChance);
		jp.amounts = (Vector<PhaseAmount>) amounts.clone();
		return jp;
	}

	/**
	 * @param resource
	 * @param i
	 * @return
	 */
	public double getOutput_Waste(final String resource, final int i)
	{
		if (getOutput_Condition(resource))
		{
			return 0;
		}
		return getOutput(resource, i);
	}

	private double getOutput(final String resource, final int i)
	{
		if (i <= 0)
		{
			return 0; // negative time??? duh
		}
		final double spd = getOutput_Speed(resource);
		if (spd <= 0)
		{
			return 0;
		}
		return (spd * i) / (3600 * 1000);
	}

	/**
	 * @param resource
	 * @param i
	 * @return
	 */
	public double getOutput_Good(final String resource, final int i)
	{
		if (!getOutput_Condition(resource))
		{
			return 0;
		}
		return getOutput(resource, i);
	}

	/**
	 * update the abstract resourcelink names with real idref values from the link
	 * 
	 * @param rl
	 */
	public void updateAmountLinks(final JDFResourceLink rl)
	{
		if (rl == null || amounts == null)
		{
			return;
		}
		for (int i = 0; i < amounts.size(); i++)
		{
			final PhaseAmount pa = amounts.get(i);
			if (rl.matchesString(pa.getResource()))
			{
				pa.setResource(rl.getrRef());
			}
		}
	}

	/**
	 * @return
	 */
	public Vector<JDFEmployee> getEmployees()
	{
		return employee == null ? null : employee.getEmployees();
	}

	/**
	 * @param errorChance the errorChance to set
	 */
	public void setErrorChance(final double errorChance)
	{
		this.errorChance = errorChance;
	}

	/**
	 * @return the errorChance
	 */
	public double getErrorChance()
	{
		return errorChance;
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
		amount.setAttribute("Waste", getOutput_Condition(resString), null);
		amount.setAttribute("Speed", getOutput_Speed(resString), null);
	}

	/**
	 * write myself to an element
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	public void writeToParent(final KElement root)
	{
		final KElement phase = root.appendElement("Phase");

		if (deviceStatus != null && nodeStatus != null)
		{
			phase.setAttribute("DeviceStatus", deviceStatus.getName(), null);
			phase.setAttribute("DeviceStatusDetails", getDeviceStatusDetails());
			phase.setAttribute("NodeStatus", nodeStatus.getName(), null);
			phase.setAttribute("NodeStatusDetails", getNodeStatusDetails());
			phase.setAttribute(AttributeName.DURATION, getTimeToGo() / 1000., null);
			final VString v = getAmountResourceNames();
			if (v != null)
			{
				final int vSiz = v.size();
				for (int i = 0; i < vSiz; i++)
				{
					addAmount(v.stringAt(i), phase);
				}
			}
			BambiServlet.addOptionList(deviceStatus, EnumDeviceStatus.getEnumList(), phase, "DeviceStatus");
			BambiServlet.addOptionList(nodeStatus, EnumNodeStatus.getEnumList(), phase, "NodeStatus");
		}
		else
		{
			getLog().error("null status - bailing out");
		}
	}

}
