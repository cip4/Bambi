/**
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
 * originally based on software copyright (c) 1999-2006, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.SignalDispatcher;
import org.cip4.bambi.core.messaging.Trigger;
import org.cip4.bambi.core.queues.QueueEntry;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.MultiModuleStatusCounter;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.thread.DelayedPersist;
import org.cip4.jdflib.util.thread.IPersistable;

/**
 * @author Rainer Prosi
 *
 */
public class StatusListener implements IPersistable
{

	private SignalDispatcher dispatcher;

	private SignalDispatcher rootDispatcher;
	protected StatusCounter theCounter;
	final MultiModuleStatusCounter multiCounter;
	private JDFNode currentNode;
	private boolean wantPersist;
	private static Log log = LogFactory.getLog(StatusListener.class);

	public boolean isWantPersist()
	{
		return wantPersist;
	}

	public void setWantPersist(final boolean wantPersist)
	{
		this.wantPersist = wantPersist;
	}

	/**
	 *
	 * @param dispatch
	 * @param deviceID
	 * @param icsVersions the default ics versions
	 */
	public StatusListener(final SignalDispatcher dispatch, final String deviceID, final VString icsVersions)
	{
		dispatcher = dispatch;
		theCounter = new StatusCounter(null, null, null);
		theCounter.setDeviceID(deviceID);
		theCounter.setIcsVersions(icsVersions);
		multiCounter = new MultiModuleStatusCounter();
		multiCounter.addModule(theCounter);
		rootDispatcher = null;
		currentNode = null;
		wantPersist = true;
	}

	/**
	 * @param msgType the type of messages to flush out, null if any/all types
	 * @return
	 */
	public boolean flush(final String msgType)
	{
		if (dispatcher != null)
		{
			final Trigger[] t = dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), -1, msgType);
			dispatcher.flush();
			if (rootDispatcher != null)
			{
				final Trigger[] t2 = rootDispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), -1, msgType);
				rootDispatcher.flush();
				if (!Trigger.waitQueued(t2, 420))
					return false;
			}
			return Trigger.waitQueued(t, 420);
		}
		return false;
	}

	/**
	 * update the status information by starting a new phase all amounts that have been accumulated are linked to the prior phase should be called after all amounts have been
	 * appropriately set
	 *
	 * @param deviceStatus
	 * @param deviceStatusDetails
	 * @param nodeStatus
	 * @param nodeStatusDetails
	 * @param forceOut forces writing by any generator, even if the status remains the same and the trigger would not call for a write
	 */
	public void signalStatus(final EnumDeviceStatus deviceStatus, final String deviceStatusDetails, final EnumNodeStatus nodeStatus, final String nodeStatusDetails, final boolean forceOut)
	{
		if (theCounter == null)
		{
			log.error("updating null status tracker");
			return;
		}
		final boolean bMod = theCounter.setPhase(nodeStatus, nodeStatusDetails, deviceStatus, deviceStatusDetails);
		if (bMod || forceOut)
		{
			flush("Status");
			DelayedPersist.getDelayedPersist().queue(this, -1);
		}
		else
		{
			saveJDF(123456);
		}
	}

	/**
	 *
	 * @param deltaTime time to wait / collct before really saving
	 */
	public void saveJDF(final int deltaTime)
	{
		DelayedPersist.getDelayedPersist().queue(this, deltaTime);
	}

	/**
	 * set event, append the Event element and optionally the comment<br/>
	 * overwrites existing values
	 *
	 * @param eventID Event/@EventID to set
	 * @param eventValue Event/@EventValue to set
	 * @param comment the comment text, if null no comment is set
	 */
	public void setEvent(final String eventID, final String eventValue, final String comment)
	{
		if (theCounter == null)
		{
			log.error("updating null status tracker");
			return;
		}
		theCounter.setEvent(eventID, eventValue, comment);
		flush("Notification");
	}

	/**
	 * updates the amount for a given resource the amounts are collected but not signaled until @see signalStatus() is called
	 *
	 * @param resID the resource id of the tracked resource
	 * @param good the number of good copies
	 * @param waste the number of waste copies, 0 specifies that waste should be ignored
	 */
	public void updateAmount(final String resID, final double good, final double waste)
	{
		if (theCounter == null)
		{
			return;
		}
		theCounter.addPhase(resID, good, waste, true);
		if (good + waste > 0)
		{
			theCounter.setPhase(null, null, null, null);
			dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), (int) (good + waste), null);
		}
		DelayedPersist.getDelayedPersist().queue(this, 123456);
	}

	/**
	 * set the total amount of a given resource by the value specified
	 *
	 * @param percent the percent completed
	 *
	 *
	 */
	public void setPercentComplete(final double percent)
	{
		if (theCounter == null)
		{
			return;
		}
		theCounter.setPercentComplete(percent);
		theCounter.setPhase(null, null, null, null);
		DelayedPersist.getDelayedPersist().queue(this, 123456);
	}

	/**
	 * incrementally update the total amount of a given resource by the value specified
	 *
	 * @param percent the percent completed
	 *
	 *
	 */
	public void updatePercentComplete(final double percent)
	{
		if (theCounter == null)
		{
			return;
		}
		theCounter.updatePercentComplete(percent);
		DelayedPersist.getDelayedPersist().queue(this, 123456);
	}

	/**
	 * update the total amount of a given resource to the value specified
	 *
	 * @param resID the resource id
	 * @param amount the total amount top set
	 * @param waste if true, this is waste, else it is good
	 *
	 */
	public void updateTotal(final String resID, final double amount, final boolean waste)
	{
		if (theCounter == null)
		{
			return;
		}
		theCounter.setTotal(resID, amount, waste);
		if (amount > 0)
		{
			theCounter.setPhase(null, null, null, null);
			dispatcher.triggerQueueEntry(theCounter.getQueueEntryID(), theCounter.getNodeIDentifier(), (int) amount, null);
		}
		DelayedPersist.getDelayedPersist().queue(this, 123456);
	}

	/**
	 * replace the currently tracked node with node used to overwrite the current node with a returned node, e.g from a proxy device
	 *
	 * @param node the JDFNode used to overwrite the local JDF node
	 */
	public void replaceNode(final JDFNode node)
	{
		if (node != null)
		{
			final String location = currentNode == null ? null : currentNode.getOwnerDocument_JDFElement().getOriginalFileName();
			currentNode = node;
			if (location != null)
			{
				currentNode.getOwnerDocument_JDFElement().setOriginalFileName(location);
			}
			DelayedPersist.getDelayedPersist().queue(this, -1);
		}
	}

	/**
	 * setup the map of queueentryid and node
	 *
	 * @param queueEntryID the queueentryid is associated to the node if {@link QueueEntry}==null, the entire list is cleared
	 * @param vPartMap the vector of partitions that are being tracked
	 * @param trackResourceID the id of the "major" resource to be counted for phasetimes
	 * @param node the jdf node that will be processed. this may be a group node with additional sub nodes if node==null the queueentryid is removed from the map
	 */
	public synchronized void setNode(final String queueEntryID, JDFNode node, final VJDFAttributeMap vPartMap, final String trackResourceID)
	{
		final String oldQEID = theCounter.getQueueEntryID();
		theCounter.writeAll(); // write all stuff in the counter to the node
		DelayedPersist.getDelayedPersist().queue(this, -1);
		final boolean bSame = currentNode == node;
		currentNode = node;
		if (!bSame)
		{
			DelayedPersist.getDelayedPersist().queue(this, -1);
		}

		if (!KElement.isWildCard(oldQEID))
		{
			dispatcher.removeSubScriptions(oldQEID, "*", null);
		}

		theCounter.setTrackWaste("*", true); // always track waste
		theCounter.setActiveNode(node, vPartMap, null);
		theCounter.setTrackWaste("*", true); // always track waste
		theCounter.setFirstRefID(trackResourceID);
		theCounter.setQueueEntryID(queueEntryID);
		if (node != null)
		{
			final VJDFAttributeMap partMapVector = node.getNodeInfoPartMapVector();
			final JDFAttributeMap partMap = partMapVector == null ? null : partMapVector.getCommonMap();
			theCounter.setPhase(node.getPartStatus(partMap, 1), node.getPartStatusDetails(partMap), EnumDeviceStatus.Running, node.getPartStatusDetails(partMap));
		}
		while (node != null)
		{
			dispatcher.addSubscriptions(node, queueEntryID);
			node = node.getParentJDF();
		}
	}

	/**
	 * save the currently active jdf
	 *
	 */
	@Override
	public boolean persist()
	{
		if (currentNode == null || !wantPersist)
		{
			return false;
		}
		final JDFDoc ownerDoc = currentNode.getOwnerDocument_JDFElement();
		if (ownerDoc != null && ownerDoc.getOriginalFileName() != null)
		{
			ownerDoc.write2File((String) null, 0, true);
		}
		return true;
	}

	/**
	 * get the device status
	 *
	 * @return the device status. <br/>
	 *         Returns EnumDeviceStatus.Idle if the StatusCounter is null. <br/>
	 *         Returns EnumDeviceStatus.Unknown, if the StatusListener was unable to retrieve the status from the StatusCounter.
	 */
	public EnumDeviceStatus getDeviceStatus()
	{
		return multiCounter.getDeviceStatus();
	}

	/**
	 * shut down this StatusListener
	 */
	public void shutdown()
	{
		// not needed right now, retaining method for future compatability
	}

	/**
	 * get the StatusCounter
	 *
	 * @return the StatusCounter
	 */
	public StatusCounter getStatusCounter()
	{
		return theCounter;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String - the string representation
	 */
	@Override
	public String toString()
	{
		return "[StatusListner - counter: " + theCounter + "\n Current Node: " + currentNode;
	}

	/**
	 * @param inputMessage
	 * @return return true if inputMessage applies to this Listener
	 */
	public boolean matchesQuery(final JDFMessage inputMessage)
	{
		if (inputMessage == null)
		{
			return false;
		}
		if (!(inputMessage instanceof JDFQuery))
		{
			return false;
		}
		final JDFQuery q = (JDFQuery) inputMessage;
		final EnumType type = q.getEnumType();
		if (EnumType.Status.equals(type))
		{
			final JDFStatusQuParams sqp = q.getStatusQuParams();
			if (sqp == null)
			{
				return true;
			}
			return matchesIDs(sqp.getJobID(), sqp.getJobPartID(), sqp.getQueueEntryID());
		}
		else if (EnumType.Resource.equals(type))
		{
			final JDFResourceQuParams rqp = q.getResourceQuParams();
			if (rqp == null)
			{
				return true;
			}
			return matchesIDs(rqp.getJobID(), rqp.getJobPartID(), rqp.getQueueEntryID());
		}
		return true;
	}

	/**
	 * @param jobID
	 * @param jobPartID
	 * @param queueEntryID
	 * @return true if jobID jobPartID and qeID match or are wildcards
	 */
	private boolean matchesIDs(final String jobID, final String jobPartID, String queueEntryID)
	{
		final NodeIdentifier niIn = new NodeIdentifier(jobID, jobPartID, null);
		final NodeIdentifier niCurrent = currentNode == null ? new NodeIdentifier() : currentNode.getIdentifier();
		if (!niIn.matches(niCurrent) && !niCurrent.matches(niIn))
		{
			return false;
		}

		queueEntryID = StringUtil.getNonEmpty(queueEntryID);
		return queueEntryID == null || ContainerUtil.equals(queueEntryID, theCounter.getQueueEntryID());
	}

	/**
	 * @param dispatcher
	 */
	public void setDispatcher(final SignalDispatcher dispatcher)
	{
		this.dispatcher = dispatcher;
	}

	/**
	 * @param _rootDispatcher
	 */
	public void setRootDispatcher(final SignalDispatcher _rootDispatcher)
	{
		this.rootDispatcher = _rootDispatcher;
	}

	/**
	 * @param resID the resource id of the tracked resource
	 * @param deltaAmount the number of good copies
	 * @param deltaWaste the number of waste copies, 0 specifies that waste should be ignored
	 * @param amount
	 * @param waste
	 */
	public void setAmount(final String resID, final double deltaAmount, final double deltaWaste, final double amount, final double waste)
	{
		if (theCounter == null)
		{
			return;
		}
		theCounter.setPhase(resID, deltaAmount, deltaWaste);
		if (amount >= deltaAmount)
		{
			theCounter.setTotal(resID, amount, false);
		}
		if (waste >= deltaWaste)
		{
			theCounter.setTotal(resID, waste, true);
		}
	}

	/**
	 * @param employee
	 * @return
	 */
	public boolean removeEmployee(final JDFEmployee employee)
	{
		if (theCounter == null)
		{
			return false;
		}
		final boolean b = theCounter.removeEmployee(employee);
		if (b)
		{
			theCounter.setPhase(null, null, null, null);
			DelayedPersist.getDelayedPersist().queue(this, -1);
		}
		return b;
	}

	/**
	 * @param employee
	 * @return
	 */
	public int addEmployee(final JDFEmployee employee)
	{
		if (theCounter == null)
		{
			return 0;
		}
		final int n0 = theCounter.addEmployee(null);
		final int n1 = theCounter.addEmployee(employee);
		if (n1 != n0)
		{
			theCounter.setPhase(null, null, null, null);
			DelayedPersist.getDelayedPersist().queue(this, -1);
		}
		return n1;
	}

	/**
	 * @param employees
	 */
	public void setEmployees(final Vector<JDFEmployee> employees)
	{
		if (theCounter == null)
		{
			return;
		}
		theCounter.replaceEmployees(employees);
	}

	public void setDeviceID(final String deviceid)
	{
		theCounter.setDeviceID(deviceid);
	}

	public void setIcsVersions(final VString icsVersions)
	{
		theCounter.setIcsVersions(icsVersions);
	}

	public JDFDoc getDocJMFPhaseTime()
	{
		return multiCounter.getStatusResponse();
	}

	public void removeListener(final StatusListener statusListener)
	{
		multiCounter.removeModule(statusListener.getStatusCounter());
		if (EnumDeviceStatus.Idle.equals(multiCounter.getDeviceStatus()))
		{
			theCounter.setActiveNode(null, null, null);
		}
	}

	public void addListener(final StatusListener statusListener)
	{
		multiCounter.addModule(statusListener.getStatusCounter());
	}

}
