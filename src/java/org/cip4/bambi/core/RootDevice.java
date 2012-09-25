/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2012 The International Cooperation for the Integration of 
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

package org.cip4.bambi.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.core.IDeviceProperties.QERetrieval;
import org.cip4.bambi.core.messaging.DispatchHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFDeviceFilter;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.MemorySpy;
import org.cip4.jdflib.util.MemorySpy.MemScope;
import org.cip4.jdflib.util.StringUtil;

/**
 * the dispatcher / rootDev controller device
 */
public class RootDevice extends AbstractDevice
{
	protected HashMap<String, AbstractDevice> _devices = null;
	private JMFFactory jmfFactory;

	/**
	 * 
	 */
	private static final long serialVersionUID = -4412710163767830461L;

	/**
	 * @param prop
	 */
	public RootDevice(final IDeviceProperties prop)
	{
		super(prop);
		_devices = new HashMap<String, AbstractDevice>();
		_jmfHandler.setFilterOnDeviceID(false); // accept all
		log.info("created RootDevice '" + prop.getDeviceID() + "'");
	}

	/**
	 * add handlers to this
	 * @see org.cip4.bambi.core.AbstractDevice#addHandlers()
	 */
	@Override
	protected void addHandlers()
	{
		// not here - these should be grabbed by catchall super.addHandlers();
		_jmfHandler.addHandler(this.new KnownDevicesHandler());
		_jmfHandler.addHandler(this.new StatusHandler());
		// this guy is the catchall
		_jmfHandler.addHandler(this.new RootDispatchHandler("*", new EnumFamily[] { EnumFamily.Query, EnumFamily.Command, EnumFamily.Signal }));
		_jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.AbortQueueEntry, new EnumFamily[] { EnumFamily.Command }));
		_jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.HoldQueueEntry, new EnumFamily[] { EnumFamily.Command }));
		_jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.RemoveQueueEntry, new EnumFamily[] { EnumFamily.Command }));
		_jmfHandler.addHandler(this.new QueueDispatchHandler(EnumType.ResumeQueueEntry, new EnumFamily[] { EnumFamily.Command }));
	}

	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		return null;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#canAccept(org.cip4.jdflib.node.JDFNode, java.lang.String)
	 * @param jdf
	 * @param queueEntryID
	 * @return
	*/
	@Override
	public VString canAccept(final JDFNode jdf, final String queueEntryID)
	{
		VString vs = new VString();
		Set<String> deviceIDs = _devices.keySet();
		for (String id : deviceIDs)
		{
			final AbstractDevice ad = _devices.get(id);
			VString canAccept = ad.canAccept(jdf, queueEntryID);
			vs.appendUnique(canAccept);
		}
		return vs.size() == 0 ? null : vs;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#getNodeFromDoc(org.cip4.jdflib.core.JDFDoc)
	 */
	@Override
	public JDFNode getNodeFromDoc(final JDFDoc doc)
	{
		final Iterator<String> it = _devices.keySet().iterator();
		while (it.hasNext())
		{
			final AbstractDevice ad = _devices.get(it.next());
			final JDFNode n = ad.getNodeFromDoc(doc);
			if (n != null)
			{
				return n;
			}
		}
		return null;
	}

	/**
	 * create a new device and add it to the map of devices. reload if the device is already in
	 * @param prop
	 * @return the Device, if device has been created. null, if not (maybe device with deviceID is already present)
	 */
	AbstractDevice createDevice(final IDeviceProperties prop)
	{
		if (_devices == null)
		{
			log.info("map of devices is null, re-initialising map...");
			_devices = new HashMap<String, AbstractDevice>();
		}

		final String devID = prop.getDeviceID();
		final AbstractDevice abstractDevice = _devices.get(devID);
		if (abstractDevice != null)
		{
			abstractDevice.shutdown(); // just in case
			log.info("device " + devID + " is already existing");
			_devices.remove(devID);
		}
		final AbstractDevice dev = prop.getDeviceInstance();
		if (dev == null)
		{
			log.warn("could not create device devID=" + devID);
			return null;
		}
		dev.setRootDevice(this);
		_devices.put(devID, dev);
		log.info("created device " + devID);
		updateQERetrieval(prop);
		return dev;
	}

	/**
	 * @param prop
	 */
	private void updateQERetrieval(final IDeviceProperties prop)
	{
		QERetrieval myqeRet = getProperties().getQERetrieval();
		if (!QERetrieval.BOTH.equals(myqeRet))
		{
			QERetrieval devqeRet = prop.getQERetrieval();
			if (!devqeRet.equals(myqeRet))
			{
				getProperties().setQERetrieval(QERetrieval.BOTH);
			}
		}
	}

	/**
	 * handler for the knowndevices query
	 */
	protected class KnownDevicesHandler extends AbstractHandler
	{
		public KnownDevicesHandler()
		{
			super(EnumType.KnownDevices, new EnumFamily[] { EnumFamily.Query });
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(final JDFMessage m, final JDFResponse resp)
		{
			return handleKnownDevices(m, resp);
		}

		protected boolean handleKnownDevices(final JDFMessage m, final JDFResponse resp)
		{
			if (m == null || resp == null)
			{
				return false;
			}
			// log.info("Handling "+m.getType());
			final EnumType typ = m.getEnumType();
			if (EnumType.KnownDevices.equals(typ))
			{
				final JDFDeviceList dl = resp.appendDeviceList();
				appendDeviceInfo(dl); // write myself into the list...
				final Set<String> keys = _devices.keySet();
				final Object[] strKeys = keys.toArray();
				for (int i = 0; i < keys.size(); i++)
				{
					final String key = (String) strKeys[i];
					final AbstractDevice dev = _devices.get(key);
					if (dev == null)
					{
						log.error("device with key '" + key + "'not found");
					}
					else
					{
						dev.appendDeviceInfo(dl);
					}
				}
				final JDFDeviceFilter filter = m.getDeviceFilter(0);
				if (filter != null)
				{
					filter.applyTo(dl);
				}

				return true;
			}

			return false;
		}
	}

	// //////////////////////////////////////////////////////////////////////

	/**
	 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
	 * 
	 * Apr 28, 2009
	 */
	public class StatusHandler extends RootDispatchHandler
	{
		/**
		 * 
		 */
		public StatusHandler()
		{
			super(EnumType.Status, new EnumFamily[] { EnumFamily.Query, EnumFamily.Command });
		}

		/**
		 * @see org.cip4.bambi.core.RootDevice.RootDispatchHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			final JDFStatusQuParams statusQuParams = inputMessage.getStatusQuParams();
			final boolean bQueue = statusQuParams == null ? false : statusQuParams.getQueueInfo();
			final boolean bGood = super.handleMessage(inputMessage, response);
			if (bGood && bQueue)
			{
				final VElement vq = response.getChildElementVector(ElementName.QUEUE, null);
				if (vq != null)
				{
					final JDFQueue q = _theQueueProcessor.getQueue().copyToResponse(response, null, null);
					final int nQ = vq.size();
					final JDFQueueEntry qe0 = q.getQueueEntry(0);
					for (int i = 0; i < nQ; i++)
					{
						final JDFQueue qi = (JDFQueue) vq.elementAt(i);
						final VElement vQE = qi.getQueueEntryVector();
						if (vQE != null)
						{
							final int nQE = vQE.size();

							for (int j = 0; j < nQE; j++)
							{
								q.copyElement(vQE.elementAt(j), qe0);
							}
						}
					}
					QueueProcessor.removeBambiNSExtensions(q);
				}
			}
			return bGood;
		}

	}

	/**
	 * handler for the StopPersistentChannel command
	 */
	public class RootDispatchHandler extends DispatchHandler
	{
		/**
		 * @param _type
		 * @param _families
		 */
		public RootDispatchHandler(final EnumType _type, final EnumFamily[] _families)
		{
			super(_type, _families);
		}

		/**
		 * @param _type
		 * @param _families
		 */
		public RootDispatchHandler(final String _type, final EnumFamily[] _families)
		{
			super(_type, _families);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param inputMessage
		 * @param response
		 * @return true if handled
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			final AbstractDevice[] devs = getDeviceArray();
			return super.handleMessage(inputMessage, response, devs, false);
		}
	}

	/**
	 * handler for the StopPersistentChannel command
	 */
	public class RootGetDispatchHandler extends BambiLogFactory implements IGetHandler
	{
		/**
		 * @param request
		 * @return true if handled
		 */
		public XMLResponse handleGet(final ContainerRequest request)
		{
			final AbstractDevice[] devs = getDeviceArray();
			XMLResponse r = null;
			for (int i = 0; i < devs.length; i++)
			{
				r = devs[i].handleGet(request);
				if (r != null)
				{
					return r;
				}
			}
			return RootDevice.this.handleGet(request);
		}
	}

	/**
	 * handler for the StopPersistentChannel command
	 */
	public class QueueDispatchHandler extends DispatchHandler
	{
		private IMessageHandler superHandler = null;

		/**
		 * @param _type
		 * @param _families
		 */
		public QueueDispatchHandler(final EnumType _type, final EnumFamily[] _families)
		{
			super(_type, _families);
			superHandler = _jmfHandler.getHandler(_type.getName(), _families[0]);
		}

		/**
		 * @param _type
		 * @param _families
		 */
		public QueueDispatchHandler(final String _type, final EnumFamily[] _families)
		{
			super(_type, _families);
			superHandler = _jmfHandler.getHandler(_type, _families[0]);
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 * @param inputMessage
		 * @param response
		 * @return true if handled
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			boolean bHandled = false;
			if (superHandler != null)
			{
				bHandled = superHandler.handleMessage(inputMessage, response);
			}
			if (bHandled)
			{
				final int rc = response.getReturnCode();
				bHandled = rc == 0;
			}
			if (!bHandled)
			{
				final AbstractDevice[] devs = getDeviceArray();
				bHandled = super.handleMessage(inputMessage, response, devs, true);
			}
			return bHandled;
		}
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#shutdown()
	 */
	@Override
	public void shutdown()
	{
		log.info("shutting down root device: " + getDeviceID());
		if (_devices != null)
		{
			final Set<String> keys = _devices.keySet();
			final Iterator<String> it = keys.iterator();
			while (it.hasNext())
			{
				final String devID = it.next();
				final AbstractDevice dev = _devices.get(devID);
				if (dev != null)
				{
					dev.shutdown();
				}
			}
			_devices.clear();
		}
		if (jmfFactory != null)
		{
			jmfFactory.shutDown(null, true);
		}
		super.shutdown();
	}

	/**
	 * resets this and all child devices
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#reset()
	 */
	@Override
	public void reset()
	{
		if (_devices != null)
		{
			final Set<String> keys = _devices.keySet();
			final Iterator<String> it = keys.iterator();
			while (it.hasNext())
			{
				final String devID = it.next();
				final AbstractDevice dev = _devices.get(devID);
				if (dev != null)
				{
					dev.reset();
				}
			}
		}
		super.reset();
	}

	/**
	 * get a device
	 * @param deviceID ID of the device to get
	 * @return the {@link AbstractDevice} for a given device ID
	 */
	@Override
	public AbstractDevice getDevice(final String deviceID)
	{
		if (ContainerUtil.equals(deviceID, getDeviceID()))
			return this;
		if (_devices == null)
		{
			log.warn("list of devices is null");
			return this;
		}
		else if (deviceID == null)
		{
			log.info("attempting to retrieve null device - defaulting to root");
			return this;
		}
		return _devices.get(deviceID);
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#getJMFFactory()
	 */
	@Override
	public JMFFactory getJMFFactory()
	{
		if (jmfFactory == null)
		{
			jmfFactory = new JMFFactory(getDeviceID());
		}
		return jmfFactory;
	}

	/**
	 * @return the gethandler for this device
	 */
	@Override
	public IGetHandler getGetDispatchHandler()
	{
		return this.new RootGetDispatchHandler();
	}

	/**
	 * get the IConverterCallback for a given local url - excluding the context
	 * @see org.cip4.bambi.core.AbstractDevice#getCallback(java.lang.String)
	 * @param url
	 * @return IConverterCallback the callback, null if none found
	 */
	@Override
	public IConverterCallback getCallback(final String url)
	{
		final String devID = BambiContainer.getDeviceIDFromURL(url);
		final AbstractDevice dev = devID == null ? null : getDevice(devID);
		return (dev != null && !(dev instanceof RootDevice)) ? dev.getCallback(url) : _callback;
	}

	/**
	 * get an array of all child devices
	 * @return an array of all child devices
	 */
	public AbstractDevice[] getDeviceArray()
	{
		final Vector<AbstractDevice> deviceVector = ContainerUtil.toValueVector(_devices, true);
		return deviceVector == null ? null : deviceVector.toArray(new AbstractDevice[0]);
	}

	/**
	 * remove device
	 * @param deviceID ID of the device to be removed
	 * @return true if success
	 */
	public boolean removeDevice(final String deviceID)
	{
		if (_devices == null)
		{
			log.error("list of devices is null");
			return false;
		}
		if (_devices.get(deviceID) == null)
		{
			log.warn("tried to removing non-existing device");
			return false;
		}
		_devices.remove(deviceID);
		return true;
	}

	@Override
	protected XMLResponse showDevice(final ContainerRequest request, final boolean refresh)
	{
		final AbstractDevice[] devices = getDeviceArray();
		final XMLDoc deviceList = new XMLDoc("DeviceList", null);

		final KElement listRoot = deviceList.getRoot();
		listRoot.setAttribute("NumRequests", numRequests, null);
		listRoot.setAttribute(AttributeName.CONTEXT, request.getContextRoot());
		listRoot.setAttribute("MemFree", Runtime.getRuntime().freeMemory() / 1000 / 1000., null);
		listRoot.setAttribute("MemTotal", Runtime.getRuntime().totalMemory() / 1000 / 1000., null);
		MemorySpy memorySpy = new MemorySpy();
		listRoot.setAttribute("MemPerm", memorySpy.getPermGen(MemScope.current) / 1000 / 1000., null);
		listRoot.setAttribute("MemCurrent", memorySpy.getHeapUsed(MemScope.current) / 1000 / 1000., null);
		final XMLDevice dRoot = getXMLDevice(false, request);

		final KElement rootElem = dRoot.getRoot();
		rootElem.setAttribute("Root", true, null);
		listRoot.copyAttribute("DeviceType", rootElem, null, null, null);
		listRoot.copyElement(rootElem, null);

		if (devices != null)
		{
			final int listSize = devices.length;
			for (int i = 0; i < listSize; i++)
			{
				final AbstractDevice ad = devices[i];
				final XMLDevice dChild = ad.getXMLDevice(false, request);
				final KElement childElem = dChild.getRoot();
				childElem.setAttribute("Root", false, null);
				listRoot.copyElement(childElem, null);
			}
		}

		deviceList.setXSLTURL(getXSLT(request));
		XMLResponse r = new XMLResponse(listRoot);
		return r;

	}

	/**
	 * @return
	 */
	@Override
	public String getXSLT(final ContainerRequest request)
	{
		final String command = request.getContext();
		String s = null;
		if ("overview".equalsIgnoreCase(command) || "showDevice".equalsIgnoreCase(command) || command == null)
		{
			s = "/deviceList.xsl";
		}
		else
		{
			return super.getXSLT(request);
		}
		final String contextPath = request.getContextRoot();
		if (contextPath != null)
		{
			s = getXSLTBaseFromContext(contextPath) + s;
		}
		return s;
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#reloadQueue()
	 */
	@Override
	protected void reloadQueue()
	{
		// nop
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#getVersionString()
	 */
	@Override
	public String getVersionString()
	{
		AbstractDevice[] deviceArray = getDeviceArray();
		VString devices = new VString();
		if (deviceArray != null)
		{
			for (AbstractDevice dev : deviceArray)
			{
				if (!(dev instanceof RootDevice))
				{
					devices.add(dev.getVersionString());
				}
			}
		}
		devices.unify();
		if (devices.size() == 0)
		{
			return "Generic Bambi Root Device: " + JDFAudit.software();
		}
		else if (devices.size() == 1)
		{
			return devices.get(0);
		}
		else
		{
			return StringUtil.setvString(devices, "\n", "Generic Bambi Root Device: \n", null);
		}
	}
	// ////////////////////////////////////////////////////////////////////////////////

}