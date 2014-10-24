package org.cip4.bambi.proxy;

import java.io.File;

import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLDevice;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.StringUtil;

/**
 * @author rainer prosi
 */
public class XMLProxyDevice extends XMLDevice
{

	/**
	 * XML representation of this simDevice fore use as html display using an XSLT
	 * @param addProcs - always ignored
	 * @param request BambiServletRequest http context in which this is called
	 * @param abstractProxyDevice TODO
	 */
	public XMLProxyDevice(AbstractProxyDevice parentDevice, final boolean addProcs, final ContainerRequest request)
	{
		// proxies never show processors
		super(parentDevice, false, request);
		updateSlaveProperties();
	}

	/**
	 * 
	 */
	private void updateSlaveProperties()
	{
		final KElement deviceRoot = getRoot();
		final IProxyProperties proxyProperties = getParentDevice().getProperties();
		if (proxyProperties != null)
		{
			String slaveURL = getParentDevice().getSlaveURL();
			if (StringUtil.getNonEmpty(slaveURL) == null)
				slaveURL = "Please Add";
			deviceRoot.setAttribute("SlaveURL", slaveURL);
			deviceRoot.setAttribute("MaxPush", proxyProperties.getMaxPush(), null);
			deviceRoot.setAttribute("DeviceURLForSlave", proxyProperties.getDeviceURLForSlave());

			File hf = proxyProperties.getSlaveInputHF();
			deviceRoot.setAttribute("SlaveInputHF", hf == null ? null : hf.getPath());
			hf = proxyProperties.getSlaveOutputHF();
			deviceRoot.setAttribute("SlaveOutputHF", hf == null ? null : hf.getPath());
			hf = proxyProperties.getSlaveErrorHF();
			deviceRoot.setAttribute("SlaveErrorHF", hf == null ? null : hf.getPath());
			final String id = proxyProperties.getSlaveDeviceID();
			deviceRoot.setAttribute("SlaveDeviceID", id == null ? null : id);
			deviceRoot.setAttribute("SlaveMIMETransferExpansion", proxyProperties.getSlaveMIMEExpansion(), null);
			deviceRoot.setAttribute("SlaveMIMETransferEncoding", proxyProperties.getSlaveMIMEEncoding());
			deviceRoot.setAttribute("SlaveMIMESemicolon", proxyProperties.getSlaveMIMESemicolon(), null);
		}
		deviceRoot.setAttribute("DataURL", getParentDevice().getDataURL(null, true));
	}

	/**
	 * 
	 * @see org.cip4.bambi.core.XMLDevice#getParentDevice()
	 */
	@Override
	protected AbstractProxyDevice getParentDevice()
	{
		return (AbstractProxyDevice) super.getParentDevice();
	}
}