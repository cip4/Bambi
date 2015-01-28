package org.cip4.bambi.core;

import java.io.File;

import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFQueue;

/**
 * @author rainer prosi
 */
public class XMLDevice extends XMLDoc
{

	/**
	 * 
	 */
	private final AbstractDevice parentDevice;

	/**
	 * XML representation of this simDevice fore use as html display using an XSLT
	 * @param addProcs if true, add processor elements
	 * @param request 
	 * @param abstractDevice TODO
	 */
	protected XMLDevice(AbstractDevice abstractDevice, final boolean addProcs, final ContainerRequest request)
	{
		super("XMLDevice", null);
		parentDevice = abstractDevice;
		prepare();
		final KElement deviceRoot = getRoot();
		setXSLTURL(parentDevice.getXSLT(request));

		deviceRoot.setAttribute(AttributeName.CONTEXT, parentDevice.getContext(request));
		final boolean bModify = request.getBooleanParam("modify");
		deviceRoot.setAttribute("modify", bModify, null);
		deviceRoot.setAttribute("NumRequests", parentDevice.numRequests, null);
		deviceRoot.setAttribute("EntriesProcessed", parentDevice.getEntriesProcessed(), null);
		deviceRoot.setAttribute("VersionString", parentDevice.getVersionString(), null);
		deviceRoot.copyElement(parentDevice.getDeviceTimer(true).toXML(), null);
		deviceRoot.setAttribute(AttributeName.DEVICEID, parentDevice.getDeviceID());
		deviceRoot.setAttribute(AttributeName.DEVICETYPE, parentDevice.getDeviceType());
		deviceRoot.setAttribute("Description", parentDevice.getDescription());
		deviceRoot.setAttribute("DeviceURL", parentDevice.getDeviceURL());
		final IDeviceProperties properties = parentDevice.getProperties();
		deviceRoot.setAttribute("WatchURL", properties.getWatchURL());
		deviceRoot.setAttribute(AttributeName.DEVICESTATUS, parentDevice.getDeviceStatus().getName());
		if (parentDevice._rootDevice == null && BambiContainer.getInstance() != null)
		{
			deviceRoot.setAttribute("Dump", BambiContainer.getInstance().bWantDump, null);
		}
		addHotFolders(deviceRoot);
		addQueueInfo(deviceRoot);
		if (addProcs)
		{
			addProcessors();
		}
	}

	/**
	 * @param deviceRoot
	 */
	private void addHotFolders(final KElement deviceRoot)
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
	private void addQueueInfo(final KElement deviceRoot)
	{
		if (parentDevice._theQueueProcessor == null)
		{
			log.error("device with null queueprocessor - bailing ot: ID=" + parentDevice.getDeviceID());
		}
		final JDFQueue jdfQueue = parentDevice._theQueueProcessor.getQueue();
		final EnumQueueStatus queueStatus = jdfQueue == null ? null : jdfQueue.getQueueStatus();
		final int running = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Running);
		final int waiting = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Waiting) + jdfQueue.numEntries(EnumQueueEntryStatus.Suspended);
		final int completed = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Completed) + jdfQueue.numEntries(EnumQueueEntryStatus.Aborted);
		final int all = jdfQueue == null ? 0 : jdfQueue.numEntries(null);

		deviceRoot.setAttribute("QueueStatus", queueStatus == null ? "Unknown" : queueStatus.getName());
		deviceRoot.setAttribute("QueueWaiting", waiting, null);
		deviceRoot.setAttribute("QueueRunning", running, null);
		deviceRoot.setAttribute("QueueCompleted", completed, null);
		deviceRoot.setAttribute("QueueAll", all, null);
	}

	/**
	 * hook to call any preparation setup prior to constructing
	 */
	protected void prepare()
	{
		// nop
	}

	private void addProcessors()
	{
		for (int i = 0; i < parentDevice._deviceProcessors.size(); i++)
		{
			parentDevice._deviceProcessors.get(i).addToDisplayXML(getRoot());
		}
	}

	protected AbstractDevice getParentDevice()
	{
		return parentDevice;
	}
}