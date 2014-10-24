package org.cip4.bambi.workers.console;

import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.workers.WorkerDevice;
import org.cip4.bambi.workers.XMLWorkerDevice;

/**
 * @author rainer prosi
 */
class XMLConsoleDevice extends XMLWorkerDevice
{

	/**
	 * XML representation of this simDevice for use as html display using an XSLT
	 * @param bProc
	 * @param request
	 */
	public XMLConsoleDevice(WorkerDevice workerDevice, final boolean bProc, final ContainerRequest request)
	{
		super(workerDevice, bProc, request);
		final boolean bSetup = request.getBooleanParam("setup");
		if (!bSetup)
		{
			// TODO
		}
	}
}