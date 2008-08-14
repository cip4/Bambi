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

package org.cip4.bambi.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.NotificationBufferHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.resource.JDFNotification;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.QueueHotFolder;
import org.cip4.jdflib.util.QueueHotFolderListener;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * basis for JDF devices. <br>
 * Devices are defined in /WebContent/config/devices.xml<br>
 * Derived classes should be final: if they were ever subclassed, the DeviceProcessor thread 
 * would be started before the constructor from the subclass has a chance to fire.
 * 
 * @author boegerni
 * 
 */
public abstract class AbstractDevice implements IDevice, IGetHandler
{
	/**
	 * 
	 * @author prosirai
	 *
	 */
	protected class XMLDevice extends XMLDoc
	{

		/**
		 * XML representation of this simDevice
		 * fore use as html display using an XSLT
		 * @param addProcs TODO
		 * @param contextPath 
		 * @param dev
		 */
		public XMLDevice(boolean addProcs, String contextPath)
		{
			super("XMLDevice", null);
			KElement deviceRoot = getRoot();
			setXSLTURL(getXSLT(SHOW_DEVICE, contextPath));

			deviceRoot.setAttribute(AttributeName.CONTEXT, contextPath);
			deviceRoot.setAttribute(AttributeName.DEVICEID, getDeviceID());
			deviceRoot.setAttribute(AttributeName.DEVICETYPE, getDeviceType());
			deviceRoot.setAttribute("DeviceURL", getDeviceURL());
			deviceRoot.setAttribute(AttributeName.DEVICESTATUS, getDeviceStatus().getName());
			JDFQueue jdfQueue = _theQueueProcessor.getQueue();
			EnumQueueStatus queueStatus = jdfQueue == null ? null : jdfQueue.getQueueStatus();
			int running = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Running);
			int waiting = jdfQueue == null ? 0 : jdfQueue.numEntries(EnumQueueEntryStatus.Waiting);

			deviceRoot.setAttribute("QueueStatus", queueStatus == null ? "Unknown" : queueStatus.getName());
			deviceRoot.setAttribute("QueueWaiting", waiting, null);
			deviceRoot.setAttribute("QueueRunning", running, null);
			if (addProcs)
				addProcessors();

		}

		private void addProcessors()
		{
			for (int i = 0; i < _deviceProcessors.size(); i++)
			{
				_deviceProcessors.get(i).addToDisplayXML(getRoot());
			}
		}
	}

	/**
	 * 
	 * handler for the KnownDevices query
	 */
	protected class KnownDevicesHandler extends AbstractHandler
	{

		public KnownDevicesHandler()
		{
			super(EnumType.KnownDevices, new EnumFamily[] { EnumFamily.Query });
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage m, JDFResponse resp)
		{
			// "I am the known device"
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			EnumType typ = m.getEnumType();
			if (EnumType.KnownDevices.equals(typ))
			{
				JDFDeviceList dl = resp.appendDeviceList();
				appendDeviceInfo(dl);
				return true;
			}

			return false;
		}
	}

	protected class HFListner implements QueueHotFolderListener
	{
		private IConverterCallback _callBack = null;

		/**
		 * @param callBackClass
		 */
		public HFListner(IConverterCallback callBackClass)
		{
			_callBack = callBackClass;
		}

		public void submitted(JDFJMF submissionJMF)
		{
			log.info("HFListner:submitted");
			JDFCommand command = submissionJMF.getCommand(0);

			if (_callBack != null)
				_callBack.prepareJMFForBambi(submissionJMF.getOwnerDocument_JDFElement());

			JDFQueueSubmissionParams qsp = command.getQueueSubmissionParams(0);

			JDFDoc doc = qsp.getURLDoc();
			if (doc == null)
			{
				log.warn("could not process JDF File");
			}
			else
			{
				if (_callBack != null)
					_callBack.prepareJDFForBambi(doc);

				JDFQueueEntry qe = _theQueueProcessor.addEntry(command, null, doc);
				if (qe == null)
					log.warn("_theQueue.addEntry returned null");
				final String tmpURL = qsp.getURL();
				final File tmpFile = UrlUtil.urlToFile(tmpURL);
				if (tmpFile != null)
				{
					if (!tmpFile.delete())
						log.warn("failed to delete temporary file " + tmpFile.getAbsolutePath());
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * handler for the StopPersistentChannel command
	 */
	public class ResourceHandler extends AbstractHandler
	{

		public ResourceHandler()
		{
			super(EnumType.Resource, new EnumFamily[] { EnumFamily.Query });
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
		{
			if (_theStatusListener == null)
				return false;
			StatusCounter sc = _theStatusListener.getStatusCounter();
			JDFDoc docJMFResource = sc == null ? null : sc.getDocJMFResource();
			if (docJMFResource == null)
				return false;
			final JDFSignal response2 = docJMFResource.getJMFRoot().getSignal(0);
			response.mergeElement(response2, false);
			response.removeAttribute(AttributeName.REFID);
			JDFResourceQuParams inRQP = inputMessage.getResourceQuParams();
			if (inRQP != null)
			{
				JDFResourceQuParams rqPStatusListner = (JDFResourceQuParams) response.removeChild(ElementName.RESOURCEQUPARAMS, null, 0);
				inRQP.copyAttribute(AttributeName.JOBID, rqPStatusListner);
				inRQP.copyAttribute(AttributeName.JOBPARTID, rqPStatusListner);
				inRQP.copyAttribute(AttributeName.QUEUEENTRYID, rqPStatusListner);
			}
			else
			{
				inputMessage.moveElement(response.getElement(ElementName.RESOURCEQUPARAMS, null, 0), null);
			}
			return true;
		}

	}

	/**
	 * 
	 * generic dispatcher handler for  dispatching to the respective low level processors
	 */
	protected abstract class DispatchHandler extends JMFHandler.AbstractHandler
	{

		public DispatchHandler(String _type, EnumFamily[] _families)
		{
			super(_type, _families);
			// TODO Auto-generated constructor stub
		}

		public DispatchHandler(EnumType _type, EnumFamily[] _families)
		{
			super(_type, _families);
		}

		/**
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		public boolean handleMessage(JDFMessage inputMessage, JDFResponse response, IJMFHandler[] devs, boolean checkReturnCode)
		{

			if (devs == null)
				return false;
			boolean b = false;
			JDFNotification notif = (JDFNotification) response.removeChild(ElementName.NOTIFICATION, null, 0);

			for (int i = 0; i < devs.length; i++)
			{
				IMessageHandler mh = devs[i].getHandler(inputMessage.getType(), inputMessage.getFamily());
				if (mh != null)
				{
					response.setReturnCode(0);
					boolean b1 = mh.handleMessage(inputMessage, response);
					if (b1 && checkReturnCode)
					{
						int rc = response.getReturnCode();
						b1 = rc == 0;
					}
					b = b1 || b;
					if (response.hasChildElement(ElementName.NOTIFICATION, null))
					{
						notif = (JDFNotification) response.removeChild(ElementName.NOTIFICATION, null, 0);
					}
				}
			}
			if (b)
				response.setReturnCode(0);
			else if (notif != null)
				response.moveElement(notif, null);

			return b;
		}
	}

	/**
	 * 
	 * handler for the Status Query
	 */
	public class StatusHandler extends AbstractHandler
	{

		public StatusHandler()
		{
			super(EnumType.Status, new EnumFamily[] { EnumFamily.Query });
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
		{
			if (_theStatusListener == null)
				return false;
			if (!_theStatusListener.matchesQuery(inputMessage))
				return false;

			JDFDoc docJMF = _theStatusListener.getStatusCounter().getDocJMFPhaseTime();
			boolean bOK = copyPhaseTimeFromCounter(response, docJMF);
			if (bOK)
				addQueueToStatusResponse(inputMessage, response);
			return bOK;
		}
	}

	/**
	 * 
	 * handler for the Status Query
	 */
	public class NotificationHandler extends NotificationBufferHandler
	{

		public NotificationHandler()
		{
			super(_theSignalDispatcher);
		}

		/* (non-Javadoc)
		 * @see org.cip4.bambi.IMessageHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFMessage)
		 */
		@Override
		public boolean handleMessage(JDFMessage inputMessage, JDFResponse response)
		{
			if (_theStatusListener == null)
				return false;
			if (!_theStatusListener.matchesQuery(inputMessage))
				return false;

			JDFDoc notification = _theStatusListener.getStatusCounter().getDocJMFNotification(true);
			if (notification != null) //fills the buffer
			{
				JDFJMF jmf = notification.getJMFRoot();
				VElement v = jmf.getMessageVector(EnumFamily.Signal, EnumType.Notification);
				int siz = v == null ? 0 : v.size();
				for (int i = 0; i < siz; i++)
					super.handleMessage((JDFMessage) v.get(i), jmf.getCreateResponse(0));
			}
			return super.handleMessage(inputMessage, response);
		}
	}

	private static final Log log = LogFactory.getLog(AbstractDevice.class.getName());
	protected static final String SHOW_DEVICE = "showDevice";
	protected static final String SHOW_SUBSCRIPTIONS = "showSubscriptions";
	protected QueueProcessor _theQueueProcessor = null;
	protected Vector<AbstractDeviceProcessor> _deviceProcessors = null;
	protected SignalDispatcher _theSignalDispatcher = null;
	protected JMFHandler _jmfHandler = null;
	protected IDeviceProperties _devProperties = null;
	protected QueueHotFolder _submitHotFolder = null;
	protected IConverterCallback _callback = null;
	protected RootDevice _rootDevice = null;
	protected StatusListener _theStatusListener = null;

	/**
	 * creates a new device instance
	 * @param prop the properties for the device
	 */
	public AbstractDevice(IDeviceProperties prop)
	{
		super();
		_devProperties = prop;
		init();
	}

	protected void init()
	{
		_jmfHandler = new JMFHandler(this);

		_callback = _devProperties.getCallBackClass();
		_theSignalDispatcher = new SignalDispatcher(_jmfHandler, getDeviceID(), _callback);
		_theSignalDispatcher.addHandlers(_jmfHandler);

		_jmfHandler.setDispatcher(_theSignalDispatcher);
		_jmfHandler.setFilterOnDeviceID(true);

		_theQueueProcessor = buildQueueProcessor();
		_theQueueProcessor.addHandlers(_jmfHandler);

		String deviceID = _devProperties.getDeviceID();
		_deviceProcessors = new Vector<AbstractDeviceProcessor>();
		AbstractDeviceProcessor newDevProc = buildDeviceProcessor();
		if (newDevProc != null)
		{
			newDevProc.setParent(this);
			_theStatusListener = new StatusListener(_theSignalDispatcher, getDeviceID());
			newDevProc.init(_theQueueProcessor, _theStatusListener, _devProperties);
			String deviceProcessorClass = newDevProc.getClass().getSimpleName();
			new Thread(newDevProc, deviceProcessorClass + "_" + deviceID).start();
			log.info("device processor thread started: " + deviceProcessorClass + "_" + deviceID);
			_deviceProcessors.add(newDevProc);
		}

		final File hfURL = _devProperties.getInputHF();
		createHotFolder(hfURL);

		addHandlers();
		addWatchSubscriptions();
	}

	/**
	 * add generic subscriptions in case watchurl!=null
	 */
	protected void addWatchSubscriptions()
	{
		final String watchURL = _devProperties.getWatchURL();
		if (watchURL == null)
			return;
		JDFQuery status = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status).getQuery(0);
		JDFStatusQuParams sqp = status.appendStatusQuParams();
		sqp.setQueueInfo(true);
		sqp.setJobDetails(EnumJobDetails.Brief);
		JDFSubscription sub = status.appendSubscription();
		sub.setURL(watchURL);
		sub.setRepeatTime(30);
		sub.appendObservationTarget().setObservationPath("*");
		_theSignalDispatcher.addSubscription(status, null);

	}

	/**
	 * creates the hotfolder on the file system
	 * @param hfURL the URL of the hotfolder to create. If hfURL is null, no hotfolder will be created.
	 */
	protected void createHotFolder(File hfURL)
	{
		if (hfURL == null)
			return;
		log.info("enabling input hot folder: " + hfURL);
		File hfStorage = new File(_devProperties.getBaseDir() + File.separator + "HFTmpStorage" + File.separator
				+ _devProperties.getDeviceID());
		hfStorage.mkdirs(); // just in case
		if (hfStorage.isDirectory())
		{
			_submitHotFolder = new QueueHotFolder(hfURL, hfStorage, null, new HFListner(_devProperties.getCallBackClass()), null);
		}
		else
		{
			log.error("input hot folder could not be created " + hfURL);
		}

	}

	protected void addHandlers()
	{
		addHandler(this.new KnownDevicesHandler());
		addHandler(this.new ResourceHandler());
		addHandler(this.new StatusHandler());
		addHandler(this.new NotificationHandler());
	}

	/**
	 * register an emplyee with this device
	 * @param emp
	 */
	public void addEmployee(JDFEmployee emp)
	{
		_theStatusListener.getStatusCounter().addEmployee(emp);
	}

	/**
	 * register an emplyee with this device
	 * @param emp
	 */
	public void removeEmployee(JDFEmployee emp)
	{
		_theStatusListener.getStatusCounter().removeEmployee(emp);
	}

	/**
	 * get the device type of this device
	 * @return
	 */
	public String getDeviceType()
	{
		return _devProperties.getDeviceType();
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.IDevice#getDeviceID()
	 */
	public String getDeviceID()
	{
		return _devProperties.getDeviceID();
	}

	public IDevice getDevice(String deviceID)
	{
		if (KElement.isWildCard(deviceID))
			return this;
		return (ContainerUtil.equals(deviceID, getDeviceID())) ? this : null;
	}

	public JDFDoc processJMF(JDFDoc doc)
	{
		log.info("JMF processed by " + _devProperties.getDeviceID());
		return _jmfHandler.processJMF(doc);
	}

	/**
	 * get a String representation of this device
	 */
	@Override
	public String toString()
	{
		return ("[" + this.getClass().getName() + " Properties=" + _devProperties.toString() + "]");
	}

	/**
	 * append the JDFDeviceInfo of this device to a given JDFDeviceList
	 * @param dl the JDFDeviceList, where the JDFDeviceInfo will be appended
	 * @return true, if successful
	 */
	public boolean appendDeviceInfo(JDFDeviceList dl)
	{
		JDFDeviceInfo info = dl.appendDeviceInfo();
		JDFDevice dev = info.appendDevice();
		dev.setDeviceID(getDeviceID());
		dev.setDeviceType(getDeviceType());
		dev.setJMFURL(getDeviceURL());
		dev.setJDFInputURL(UrlUtil.fileToUrl(_devProperties.getInputHF(), false));
		dev.setJDFOutputURL(UrlUtil.fileToUrl(_devProperties.getOutputHF(), false));
		dev.setJDFErrorURL(UrlUtil.fileToUrl(_devProperties.getErrorHF(), false));
		dev.setJDFVersions(EnumVersion.Version_1_3.getName());

		if (_devProperties instanceof DeviceProperties)
		{
			DeviceProperties dp = (DeviceProperties) _devProperties;
			KElement root = dp.getDevRoot();
			if (root != null)
			{
				KElement deviceCap = root.getElement(ElementName.DEVICECAP);
				dev.copyElement(deviceCap, null);
			}
		}
		info.setDeviceStatus(getDeviceStatus());
		return true;
	}

	/**
	 * add a MessageHandler to this devices JMFHandler
	 * @param handler the MessageHandler to add
	 */
	public void addHandler(IMessageHandler handler)
	{
		_jmfHandler.addHandler(handler);
	}

	/**
	 * get the JMFHandler of this device
	 * @return
	 */
	public IJMFHandler getHandler()
	{
		return _jmfHandler;
	}

	/**
	 * @return
	 */
	public String getXSLT(String command, String contextPath)
	{
		String s = null;
		if ("showQueue".equalsIgnoreCase(command))
			s = "/queue2html.xsl";
		if ("showDevice".equalsIgnoreCase(command))
			s = "/showDevice.xsl";
		if (s != null && contextPath != null)
		{
			s = "/" + StringUtil.token(contextPath, 0, "/") + s;
		}
		return s;
	}

	/**
	 * get the DeviceStatus of this device
	 * @return the DeviceStatus. Returns EnumDeviceStatus.Idle, if the StatusListener is null
	 */
	public EnumDeviceStatus getDeviceStatus()
	{
		StatusListener listener = getStatusListener(0);
		if (listener == null)
		{
			return EnumDeviceStatus.Idle;
		}

		EnumDeviceStatus status = listener.getDeviceStatus();
		if (status == null)
		{
			log.error("StatusListener returned a null device status");
			status = EnumDeviceStatus.Unknown;
		}
		return status;
	}

	/**
	 * stop the processing the given QueueEntry
	 * @param queueEntryID the ID of the QueueEntry to stop
	 * @param status target status of the QueueEntry (Suspended,Aborted,Held)
	 * @return the updated QueueEntry
	 */
	public JDFQueueEntry stopProcessing(String queueEntryID, EnumNodeStatus status)
	{
		AbstractDeviceProcessor theDeviceProcessor = getProcessor(queueEntryID);
		if (theDeviceProcessor == null)
			return null;
		theDeviceProcessor.stopProcessing(status);
		final IQueueEntry currentQE = theDeviceProcessor.getCurrentQE();
		return currentQE == null ? null : currentQE.getQueueEntry();
	}

	/**
	 * gets the device processor for a given queuentry
	 * @param queueEntryID - if null use any
	 * @return the processor that is processing queueEntryID, null if none matches
	 */
	protected AbstractDeviceProcessor getProcessor(String queueEntryID)
	{
		for (int i = 0; i < _deviceProcessors.size(); i++)
		{
			AbstractDeviceProcessor theDeviceProcessor = _deviceProcessors.get(i);
			IQueueEntry iqe = theDeviceProcessor.getCurrentQE();
			if (iqe == null) // processor is currently idle
				continue;
			if (queueEntryID == null || queueEntryID.equals(iqe.getQueueEntryID())) // gotcha
				return theDeviceProcessor;
		}
		return null; // none here
	}

	/**
	 * stop the signal dispatcher, hot folder and device processor, if they are not null
	 */
	public void shutdown()
	{
		if (_theSignalDispatcher != null)
		{
			_theSignalDispatcher.shutdown();
		}

		if (_deviceProcessors != null)
		{
			for (int i = _deviceProcessors.size() - 1; i >= 0; i--)
			{
				_deviceProcessors.get(i).shutdown();
			}
		}

		if (_submitHotFolder != null)
		{
			_submitHotFolder.stop();
		}
	}

	/**
	 * build a new QueueProcessor
	 * @return
	 */
	protected QueueProcessor buildQueueProcessor()
	{
		return new QueueProcessor(this);
	}

	/**
	 * build a new DeviceProcessor
	 * @return
	 */
	protected abstract AbstractDeviceProcessor buildDeviceProcessor();

	/**
	 * returns true if the device cann process the jdf ticket
	 * @return
	 */
	public abstract boolean canAccept(JDFDoc doc);

	/**
	 * @param doc
	 * @return
	 */
	public abstract JDFNode getNodeFromDoc(JDFDoc doc);

	/**
	 * get the StatusListener of the i'th DeviceProcessor
	 * @param i the index of the DeviceProcessor to the the StatusListener of
	 * @return the StatusListener
	 */
	public StatusListener getStatusListener(int i)
	{
		if (i >= _deviceProcessors.size())
			return null;
		return _deviceProcessors.get(i).getStatusListener();
	}

	public void addSubscriptionHandler(EnumType typ, IMessageHandler handler)
	{
		_jmfHandler.addSubscriptionHandler(typ, handler);
	}

	/**
	 * @param request
	 * @param response
	 * @param context
	 * @return
	 */
	public boolean handleGet(BambiServletRequest request, BambiServletResponse response)
	{
		if (!isMyRequest(request))
			return false;

		if (BambiServlet.isMyContext(request, SHOW_DEVICE))
		{
			return showDevice(request, response, request.getBooleanParam("refresh"));
		}
		if (BambiServlet.isMyContext(request, SHOW_SUBSCRIPTIONS))
		{
			return _theSignalDispatcher == null ? false : _theSignalDispatcher.handleGet(request, response);
		}
		if (_theQueueProcessor != null)
		{
			boolean bH = _theQueueProcessor.handleGet(request, response);
			if (bH)
				return true;
		}
		return false;

	}

	protected boolean isMyRequest(HttpServletRequest request)
	{
		return BambiServlet.isMyRequest(request, getDeviceID());
	}

	/**
	 * sends a request for a new qe to the proxy
	 */
	public void sendRequestQueueEntry()
	{
		final String proxyURL = _devProperties.getProxyControllerURL();
		if (KElement.isWildCard(proxyURL))
			return;
		final String queueURL = getDeviceURL();
		JDFJMF jmf = JMFFactory.buildRequestQueueEntry(queueURL, getDeviceID());
		new JMFFactory(_callback).send2URL(jmf, proxyURL, null, getDeviceID()); // TODO handle reponse
	}

	public SignalDispatcher getSignalDispatcher()
	{
		return _theSignalDispatcher;
	}

	public IConverterCallback getCallback(String url)
	{
		return _callback;
	}

	public void setCallback(IConverterCallback callback)
	{
		this._callback = callback;
	}

	/**
	 * get the device properties
	 * @return
	 */
	public IDeviceProperties getProperties()
	{
		return _devProperties;
	}

	/**
	 * @return
	 */
	public File getJDFDir()
	{
		return _devProperties.getJDFDir();
	}

	/**
	 * @return
	 */
	public File getBaseDir()
	{
		return _devProperties.getBaseDir();
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.IDevice#getDeviceURL()
	 */
	public String getDeviceURL()
	{
		return _devProperties.getDeviceURL();
	}

	/**
	 * @return
	 */
	public int activeProcessors()
	{
		// TODO Auto-generated method stub
		return _deviceProcessors.size();
	}

	protected boolean showDevice(BambiServletRequest request, BambiServletResponse response, boolean refresh)
	{
		XMLDevice simDevice = this.new XMLDevice(true, request.getContextRoot());
		if (refresh)
			simDevice.getRoot().setAttribute("refresh", true, null);

		try
		{
			simDevice.write2Stream(response.getBufferedOutputStream(), 0, true);
		}
		catch (IOException x)
		{
			return false;
		}
		response.setContentType(MimeUtil.TEXT_XML);
		return true;
	}

	public RootDevice get_rootDevice()
	{
		return _rootDevice;
	}

	public void setRootDevice(RootDevice rootDevice)
	{
		this._rootDevice = rootDevice;
		if (_theStatusListener != null && _rootDevice != null)
			_theStatusListener.setRootDispatcher(_rootDevice._theSignalDispatcher);
	}

	/* (non-Javadoc)
	 * @see org.cip4.bambi.core.messaging.IJMFHandler#getHandler(org.cip4.jdflib.jmf.JDFMessage.EnumType, org.cip4.jdflib.jmf.JDFMessage.EnumFamily)
	 */
	public IMessageHandler getHandler(String typ, EnumFamily family)
	{
		return _jmfHandler == null ? null : _jmfHandler.getHandler(typ, family);
	}

	/**
	 * @param inputMessage
	 * @param response
	 */
	protected void addQueueToStatusResponse(JDFMessage inputMessage, JDFResponse response)
	{
		final JDFStatusQuParams statusQuParams = inputMessage.getStatusQuParams();
		boolean bQueue = statusQuParams == null ? false : statusQuParams.getQueueInfo();
		if (bQueue)
		{
			JDFQueue qq = (JDFQueue) response.copyElement(_theQueueProcessor.getQueue(), null);
			QueueProcessor.removeBambiNSExtensions(qq);
		}
	}

	/**
	 * @param response
	 * @param docJMF
	 */
	protected boolean copyPhaseTimeFromCounter(JDFResponse response, JDFDoc docJMF)
	{
		JDFJMF root = docJMF == null ? null : docJMF.getJMFRoot();
		if (root == null)
		{
			log.error("StatusHandler.handleMessage: StatusCounter response = null");
			return false;
		}
		JDFJMF respRoot = response.getJMFRoot();
		int nResp = root.numChildElements(ElementName.RESPONSE, null);

		for (int i = 0; i < nResp; i++)
		{
			JDFResponse r = root.getResponse(i);
			JDFResponse rResp = respRoot.getCreateResponse(i);
			rResp.setType(response.getType());
			rResp.copyAttribute(AttributeName.REFID, response);
			VElement v = r.getChildElementVector(ElementName.DEVICEINFO, null);
			int siz = v == null ? 0 : v.size();

			for (int j = 0; j < siz; j++)
			{
				rResp.copyElement(v.elementAt(j), null);
			}
		}
		return true;
	}

	/**
	 * flushes any and all message queues
	 *
	 */
	public void flush()
	{
		if (_theSignalDispatcher != null)
			_theSignalDispatcher.flush();
	}

	@Override
	protected void finalize() throws Throwable
	{
		shutdown();
		super.finalize();
	}

}
