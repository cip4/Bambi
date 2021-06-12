/*
  The CIP4 Software License, Version 1.0

  Copyright (c) 2001-2021 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.

  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
  distribution.

  3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
  the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
  normally appear.

  4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
  without prior written permission. For written permission, please contact info@cip4.org.

  5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization

  Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  OF SUCH DAMAGE. ====================================================================

  This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
  originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.

  For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 */
package org.cip4.bambi.workers;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.cip4.jdflib.util.StringUtil;

/**
 * Implementation of a single JobPhase simulation.
 */
public class JobPhase implements Cloneable
{

	private final Log log = BambiLogFactory.getLog(JobPhase.class);

	protected Vector<PhaseAmount> phaseAmounts = new Vector<>();
	protected EnumDeviceStatus deviceStatus = EnumDeviceStatus.Idle;
	protected EnumNodeStatus nodeStatus = EnumNodeStatus.Waiting;
	protected String deviceStatusDetails = "";
	protected String nodeStatusDetails = "";
	protected long durationMillis = 0;
	private double errorChance = 0.00;
	protected PhaseEmployees phaseEmployees = null;

	/**
	 * Default constructor.
	 */
	public JobPhase()
	{
		super();
	}

	/**
	 * Custom constructor. Accepting a JobPhase XML Node from the job_*.xml config file.
	 *
	 * @param xmlJobPhase A JobPhase XML Node of the job_*.xml file.
	 */
	public JobPhase(final KElement xmlJobPhase)
	{
		super();

		// parse attributes from the JobPhase XML Node
		this.deviceStatus = EnumDeviceStatus.getEnum(xmlJobPhase.getXPathAttribute("@DeviceStatus", "Idle"));
		this.deviceStatusDetails = xmlJobPhase.getXPathAttribute("@DeviceStatusDetails", "");
		this.nodeStatus = EnumNodeStatus.getEnum(xmlJobPhase.getXPathAttribute("@NodeStatus", "Waiting"));
		this.nodeStatusDetails = xmlJobPhase.getXPathAttribute("@NodeStatusDetails", "");
		this.durationMillis = 1000L * StringUtil.parseInt(xmlJobPhase.getXPathAttribute("@Duration", "0"), 0);
		this.phaseEmployees = new PhaseEmployees(xmlJobPhase);

		if (xmlJobPhase.hasAttribute("Error"))
		{
			this.errorChance = StringUtil.parseDouble(xmlJobPhase.getXPathAttribute("@Error", "0"), 0) * 0.001;
		}
		else
		{
			this.errorChance = StringUtil.parseDouble(xmlJobPhase.getXPathAttribute("../@Error", "0"), 0) * 0.001;
		}

		// process amount configurations
		final VElement xmlAmounts = xmlJobPhase.getChildElementVector("Amount", null);
		for (final KElement xmlAmount : xmlAmounts)
		{
			double speed = xmlAmount.getRealAttribute("Speed", null, 0);
			if (speed < 0)
			{
				speed = 0;
			}

			final boolean amountIsGood = !xmlAmount.getBoolAttribute("Waste", null, false);
			final String resourceName = xmlAmount.getAttribute("Resource");

			final PhaseAmount pa = addPhaseAmount(resourceName, speed, amountIsGood);
			pa.masterAmount = xmlAmount.getBoolAttribute("Master", null, false);
		}
	}

	/**
	 * Returns the device status as enumeration.
	 *
	 * @return The device status value.
	 */
	public EnumDeviceStatus getDeviceStatus()
	{
		return deviceStatus;
	}

	/**
	 * Set the device status attribute.
	 *
	 * @param deviceStatus The new device status value.
	 */
	public void setDeviceStatus(final EnumDeviceStatus deviceStatus)
	{
		this.deviceStatus = deviceStatus;
	}

	/**
	 * Returns the device status details as String.
	 *
	 * @return The device status details value.
	 */
	public String getDeviceStatusDetails()
	{
		return deviceStatusDetails;
	}

	/**
	 * Set the device status details attribute.
	 *
	 * @param deviceStatusDetails The new device status details value.
	 */
	public void setDeviceStatusDetails(final String deviceStatusDetails)
	{
		this.deviceStatusDetails = deviceStatusDetails;
	}

	/**
	 * Returns the node status as enumeration.
	 *
	 * @return The node status value.
	 */
	public EnumNodeStatus getNodeStatus()
	{
		return nodeStatus;
	}

	/**
	 * Set the node status attribute.
	 *
	 * @param nodeStatus The new node status value.
	 */
	public void setNodeStatus(final EnumNodeStatus nodeStatus)
	{
		this.nodeStatus = nodeStatus;
	}

	/**
	 * Returns the node status details as String.
	 *
	 * @return The node status details value.
	 */
	public String getNodeStatusDetails()
	{
		return nodeStatusDetails;
	}

	/**
	 * Set the node status details attribute.
	 *
	 * @param nodeStatusDetails The new node status details value.
	 */
	public void setNodeStatusDetails(final String nodeStatusDetails)
	{
		this.nodeStatusDetails = nodeStatusDetails;
	}

	/**
	 * Returns the job phases duration in milliseconds.
	 *
	 * @return The job phases duration in milliseconds.
	 */
	public long getDurationMillis()
	{
		return this.durationMillis;
	}

	/**
	 * Set the job phase's duration time in milliseconds.
	 *
	 * @param durationMillis The job phase's duration time in milliseconds.
	 */
	public void setDurationMillis(final long durationMillis)
	{
		this.durationMillis = durationMillis;
	}

	/**
	 * Returns the error chance in percent.
	 *
	 * @return The error chance in percent.
	 */
	public double getErrorChance()
	{
		return this.errorChance;
	}

	/**
	 * Set the error chance value in percent.
	 *
	 * @param errorChance the new error chance in percent.
	 */
	public void setErrorChance(final double errorChance)
	{
		this.errorChance = errorChance;
	}

	/**
	 * Returns a list of JDFEmployees.
	 *
	 * @return A list of JDFEmployee objects.
	 */
	public Vector<JDFEmployee> getEmployees()
	{
		return this.phaseEmployees == null ? null : this.phaseEmployees.getJdfEmployees();
	}

	/**
	 * Create a new PhaseAmount object for a resource name. In case the resource name is already defined, the attributes
	 * of the existing PhaseAmount object are going to be overwritten.
	 *
	 * @param resourceName The resources name.
	 * @param speed        The devices speed.
	 * @param amountIsGood Flag if the defined amount is good (or waste)
	 * @return The newly created or the modified phaseAmout ojbect.
	 */
	public PhaseAmount addPhaseAmount(final String resourceName, final double speed, final boolean amountIsGood)
	{
		PhaseAmount phaseAmount = findPhaseAmount(resourceName);

		if (phaseAmount == null)
		{
			phaseAmount = new PhaseAmount(resourceName, speed, amountIsGood);
			this.phaseAmounts.add(phaseAmount);
		}
		else
		{
			phaseAmount.amountIsGood = amountIsGood;
			phaseAmount.speed = speed;
		}

		return phaseAmount;
	}

	/**
	 * Returns a phase amounts output speed.
	 *
	 * @param resourceName The identifier of the phase amount.
	 * @return The output speed.
	 */
	public double getOutputSpeed(final String resourceName)
	{
		final PhaseAmount phaseAmount = findPhaseAmount(resourceName);
		return phaseAmount == null ? 0 : phaseAmount.speed;
	}

	/**
	 * Returns whether the output amount for a given resource name is good or waste.
	 *
	 * @param resourceName The given resource name.
	 * @return True in case the output is good, otherwise false for waste.
	 */
	public boolean getOutputAmountIsGood(final String resourceName)
	{
		final PhaseAmount phaseAmount = findPhaseAmount(resourceName);
		return phaseAmount == null || phaseAmount.amountIsGood;
	}

	/**
	 * Returns the number of waste being produced of a given resource after a given time.
	 *
	 * @param resourceName   The name of the given resource.
	 * @param durationMillis The given time in milliseconds.
	 * @return The number of waste being produced of a given resource after a given time.
	 */
	public double getOutputWasteAfterTime(final String resourceName, final int durationMillis)
	{
		if (getOutputAmountIsGood(resourceName))
		{
			return 0;
		}

		return getOutputAfterTime(resourceName, durationMillis);
	}

	/**
	 * Returns the number of good items being produced of a given resource after a given time.
	 *
	 * @param resourceName   The name of the given resource.
	 * @param durationMillis The given time in milliseconds.
	 * @return The number of waste being produced of a given resource after a given time.
	 */
	public double getOutputGood(final String resourceName, final int durationMillis)
	{
		if (!getOutputAmountIsGood(resourceName))
		{
			return 0;
		}

		return getOutputAfterTime(resourceName, durationMillis);
	}

	/**
	 * Returns the number of items being produced of a given resource after a given time.
	 *
	 * @param resourceName   The name of the given resource.
	 * @param durationMillis The given time in milliseconds.
	 * @return The number of waste being produced of a given resource after a given time.
	 */
	private double getOutputAfterTime(final String resourceName, final int durationMillis)
	{
		if (durationMillis <= 0)
		{
			return 0;
		}

		final double outputSpeed = getOutputSpeed(resourceName);

		if (outputSpeed <= 0)
		{
			return 0;
		}

		return (outputSpeed * durationMillis) / (3600 * 1000);
	}

	/**
	 * Find a PhaseAmount by resource name.
	 *
	 * @param resourceName The resource name to be found.
	 * @return The appropriate PhaseAmount object, or null if no phase has been found.
	 */
	public PhaseAmount findPhaseAmount(final String resourceName)
	{
		// find phase by resource name
		for (final PhaseAmount phaseAmount : phaseAmounts)
		{
			if (phaseAmount.matchesResource(resourceName))
			{
				// return result
				return phaseAmount;
			}
		}

		// no results
		return null;
	}

	/**
	 * Returns the list of phase amount counting resources in this job phase.
	 *
	 * @return The list of phase amount counting resources in this job phase.
	 */
	public VString getPhaseAmountResourceNames()
	{
		final VString resourceNames = new VString();

		for (final PhaseAmount phaseAmount : phaseAmounts)
		{
			resourceNames.add(phaseAmount.getResourceName());
		}

		return resourceNames;
	}

	/**
	 * Return the single master amount - i.e. the amount used for calculating all derived amounts
	 *
	 * @return The single master amount
	 */
	public String getMasterAmountResourceName()
	{
		for (final PhaseAmount phaseAmount : this.phaseAmounts)
		{
			if (phaseAmount.isMasterAmount())
			{
				return phaseAmount.getResourceName();
			}
		}
		// if no specific master - grab first non zero
		for (final PhaseAmount phaseAmount : this.phaseAmounts)
		{
			if (phaseAmount.getSpeed() > 0)
				return phaseAmount.getResourceName();
		}

		return null;
	}

	/**
	 * Update the abstract resource link names with real idref values from the link.
	 *
	 * @param jdfResourceLink The JDFResourceLink object to be updated.
	 */
	public void updateAmountLinks(final JDFResourceLink jdfResourceLink)
	{
		if (jdfResourceLink == null || phaseAmounts == null)
		{
			return;
		}

		for (final PhaseAmount phaseAmount : phaseAmounts)
		{
			if (jdfResourceLink.matchesString(phaseAmount.getResourceName()))
			{
				phaseAmount.setResourceName(jdfResourceLink.getrRef());
			}
		}
	}

	/**
	 * Scale the amount(Speed) by an factor.
	 *
	 * @param resourceName       The resource's name to scale.
	 * @param masterResourceName The master resource that contains the base value to scale.
	 * @param factor             The factor to scale the speed by.
	 */
	public void scaleAmount(final String resourceName, final String masterResourceName, final double factor)
	{
		final PhaseAmount phaseAmount = findPhaseAmount(resourceName);
		final PhaseAmount masterPhaseAmount = findPhaseAmount(masterResourceName);

		if (phaseAmount == null || masterPhaseAmount == null)
		{
			log.error("bad phases for scaling, base=" + resourceName + " master=" + masterResourceName + " missing=" + ((phaseAmount == null) ? resourceName : masterResourceName));
		}
		else if (phaseAmount.speed <= 0)
		{
			phaseAmount.speed = masterPhaseAmount.speed * factor;
		}
	}

	/**
	 * Append a XML representation of this JobPhase object to a given XML element.
	 *
	 * @param xml The XML Element where the current JobPhase as to be appended to.
	 */
	@SuppressWarnings("unchecked")
	public void appendToXml(final KElement xml)
	{
		// root node of the current JobPhase XML representation
		final KElement xmlPhase = xml.appendElement("Phase");

		if (this.deviceStatus != null && this.nodeStatus != null)
		{
			xmlPhase.setAttribute("DeviceStatus", deviceStatus.getName(), null);
			xmlPhase.setAttribute("DeviceStatusDetails", getDeviceStatusDetails());
			xmlPhase.setAttribute("NodeStatus", nodeStatus.getName(), null);
			xmlPhase.setAttribute("NodeStatusDetails", getNodeStatusDetails());
			xmlPhase.setAttribute(AttributeName.DURATION, getDurationMillis() / 1000., null);

			final VString phaseAmountResourceNames = getPhaseAmountResourceNames();
			if (phaseAmountResourceNames != null)
			{
				for (final String resourceNmae : phaseAmountResourceNames)
				{
					final PhaseAmount phaseAmount = findPhaseAmount(resourceNmae);
					phaseAmount.appendToXml(xmlPhase);
				}
			}

			XMLResponse.addOptionList(deviceStatus, EnumDeviceStatus.getEnumList(), xmlPhase, "DeviceStatus");
			XMLResponse.addOptionList(nodeStatus, EnumNodeStatus.getEnumList(), xmlPhase, "NodeStatus");
		}
		else
		{
			log.error("null status - bailing out");
		}
	}

	/**
	 * Generation of a short string representation of this JobPhase object.
	 *
	 * @return A short string representation of this JobPhase object.
	 */
	public String shortString()
	{
		return "[JobPhase: Duration=" + durationMillis + ", DeviceStatus=" + deviceStatus.getName() + ", DeviceStatusDetails=" + deviceStatusDetails + ", NodeStatus="
				+ nodeStatus.getName() + ", NodeStatusDetails=" + nodeStatusDetails;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JobPhase clone()
	{
		final JobPhase jobPhase = new JobPhase();
		jobPhase.deviceStatus = deviceStatus;
		jobPhase.deviceStatusDetails = deviceStatusDetails;
		jobPhase.durationMillis = durationMillis;
		jobPhase.nodeStatus = nodeStatus;
		jobPhase.nodeStatusDetails = nodeStatusDetails;
		jobPhase.setErrorChance(errorChance);
		jobPhase.phaseAmounts = (Vector<PhaseAmount>) phaseAmounts.clone();
		return jobPhase;
	}

	@Override
	public String toString()
	{
		final StringBuilder s = new StringBuilder(shortString());

		for (int i = 0; i < phaseAmounts.size(); i++)
		{
			s.append("\n").append(phaseAmounts.elementAt(i));
		}

		return s.toString();
	}

	/**
	 * Model class for the logged in employees of this phase.
	 */
	public static class PhaseEmployees
	{
		Vector<JDFEmployee> jdfEmployees = new Vector<>();

		/**
		 * Custom Constructor. Accepting a JobPhase XML Node from the job_*.xml config file.
		 *
		 * @param xmlJobPhase A JobPhase XML Node of the job_*.xml file.
		 */
		public PhaseEmployees(final KElement xmlJobPhase)
		{
			if (xmlJobPhase != null)
			{
				final VElement xmlEmployees = xmlJobPhase.getChildElementVector(ElementName.EMPLOYEE, null);

				for (final KElement xmlEmployee : xmlEmployees)
				{
					final JDFEmployee jdfEmployee = (JDFEmployee) new JDFDoc(ElementName.EMPLOYEE).getRoot();
					jdfEmployee.copyInto(xmlEmployee, false);
					this.jdfEmployees.add(jdfEmployee);
				}
			}
		}

		/**
		 * Return a list of JDF Employees.
		 *
		 * @return List of JDF Employee objects.
		 */
		public Vector<JDFEmployee> getJdfEmployees()
		{
			return jdfEmployees.size() == 0 ? null : jdfEmployees;
		}
	}

	/**
	 * Model class for a PhaseAmount.
	 */
	public static class PhaseAmount
	{
		protected boolean amountIsGood = true;
		protected double speed = 0;
		protected String resourceName;
		boolean masterAmount;

		/**
		 * Custom Constructor. Accepting multiple attribute for initializting.
		 *
		 * @param resourceName The name of the resource being consumed.
		 * @param speed        The speed per hour
		 * @param amountIsGood Flag if the defined amount is good or waste.
		 */
		PhaseAmount(final String resourceName, final double speed, final boolean amountIsGood)
		{
			this.resourceName = resourceName;
			this.amountIsGood = amountIsGood;
			this.speed = speed;
			this.masterAmount = false;
		}

		public boolean isAmountIsGood()
		{
			return this.amountIsGood;
		}

		public void setAmountIsGood(final boolean amountIsGood)
		{
			this.amountIsGood = amountIsGood;
		}

		public double getSpeed()
		{
			return this.speed;
		}

		public void setSpeed(final double speed)
		{
			this.speed = speed;
		}

		public String getResourceName()
		{
			return this.resourceName;
		}

		public void setResourceName(final String resourceName)
		{
			this.resourceName = resourceName;
		}

		public boolean isMasterAmount()
		{
			return this.masterAmount;
		}

		public void setMasterAmount(final boolean masterAmount)
		{
			this.masterAmount = masterAmount;
		}

		/**
		 * Append a XML representation of this PhaseAmount object to a given XML element.
		 *
		 * @param xml The XML Element where the current PhaseAmount has to be appended to.
		 */
		void appendToXml(final KElement xml)
		{
			final KElement xmlResourceAmount = xml.appendElement("ResourceAmount");

			xmlResourceAmount.setAttribute("ResourceName", resourceName);
			xmlResourceAmount.setAttribute("Waste", amountIsGood, null);
			xmlResourceAmount.setAttribute("Speed", speed, null);

			if (masterAmount)
			{
				xmlResourceAmount.setAttribute("Master", true, null);
			}
		}

		/**
		 * Check if a resource applies to an others resource name.
		 *
		 * @param resourceName The resource nae of the other.
		 * @return true if this PhaseAmount object matches the given resource name
		 */
		public boolean matchesResource(final String resourceName)
		{
			return this.resourceName.equals(resourceName);
		}

		@Override
		protected Object clone()
		{
			final PhaseAmount phaseAmount = new PhaseAmount(null, speed, amountIsGood);
			phaseAmount.resourceName = resourceName;
			phaseAmount.masterAmount = masterAmount;
			return phaseAmount;
		}

		@Override
		public String toString()
		{
			return "[ " + resourceName + " " + this.resourceName + (amountIsGood ? " G: " : " W: ") + "Speed: " + speed + "]";
		}
	}

	/**
	 * we always want some kind of master amount
	 */
	public void ensureMasterAmount()
	{
		boolean master = false;
		for (final PhaseAmount pa : phaseAmounts)
		{
			if (pa.isMasterAmount())
				master = true;
		}
		if (!master)
		{
			for (final PhaseAmount pa : phaseAmounts)
			{
				if (pa.isAmountIsGood())
				{
					master = true;
					break;
				}
			}

		}

	}
}
