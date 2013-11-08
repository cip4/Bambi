/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2013 The International Cooperation for the Integration of 
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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.core.IDeviceProperties.QERetrieval;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.core.messaging.AcknowledgeMap;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.IResponseHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.NotificationHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler;
import org.cip4.bambi.core.messaging.ShutdownJMFHandler;
import org.cip4.bambi.core.messaging.StatusOptimizer;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.bambi.core.queues.QueueProcessor;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoGeneralID.EnumDataType;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFCustomerInfo;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.jmf.JDFDeviceFilter;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResourceQuParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFSignal;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumActivation;
import org.cip4.jdflib.resource.JDFDevice;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.cip4.jdflib.resource.process.JDFEmployee;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.CPUTimer.CPUTimerFactory;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StatusCounter;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.hotfolder.QueueHotFolder;
import org.cip4.jdflib.util.mime.BodyPartHelper;
import org.cip4.jdflib.util.mime.MimeReader;
import org.cip4.jdflib.util.mime.MimeWriter;
import org.cip4.jdflib.util.thread.MyMutex;

/**
 * basis for JDF devices. <br>
 * Devices are defined in /WebContent/config/devices.xml<br>
 * Derived classes should be final: if they were ever subclassed, the DeviceProcessor thread would be started before the constructor from the subclass has a
 * chance to fire.
 * @author boegerni
 */
public abstract class AbstractDevice extends BambiLogFactory implements IGetHandler, IJMFHandler
{
	/**
	 * @return the queueprocessor of this device
	 */
	public QueueProcessor getQueueProcessor()
	{
		return _theQueueProcessor;
	}

	/**
	 * 
	 * @author Rainer Prosi, Heidelberger Druckmaschinen *
	 */
	private class QueueEntryRequester
	{

		protected QueueEntryRequester()
		{
			super();
		}

		/**
		 * @param currentQE
		 */
		private void importQEFromRoot(final IQueueEntry currentQE)
		{
			if (currentQE != null)
			{
				// grab the qe and pass it on to the devices queue...
				final JDFQueue queue = _theQueueProcessor.getQueue();
				JDFQueueEntry queueEntry = currentQE.getQueueEntry();
				final String queueEntryID = queueEntry.getQueueEntryID();
				log.info("extracting queue entry from root queue: qeid=" + queueEntryID);
				queueEntry = (JDFQueueEntry) queue.moveElement(queueEntry, null);

				// sort the root queue as it doesn't know that it lost a kid
				_rootDevice._theQueueProcessor.getQueue().sortChildren();
				currentQE.setQueueEntry(queueEntry);

				// clean up file references to the stored docuuments
				final String oldFil = BambiNSExtension.getDocURL(queueEntry);
				final String newFil = getJDFStorage(queueEntryID);
				if (!ContainerUtil.equals(oldFil, newFil))
				{
					final boolean bMoved = FileUtil.moveFile(UrlUtil.urlToFile(oldFil), UrlUtil.urlToFile(newFil));
					if (bMoved)
					{
						BambiNSExtension.setDocURL(queueEntry, newFil);
						currentQE.getJDF().getOwnerDocument_KElement().setOriginalFileName(newFil);
					}
				}
			}
		}

		/**
		 * sends a request for a new qe to the proxy
		 * @return true if we really sent an rqe
		 */
		protected boolean sendRequestQueueEntry()
		{
			final String proxyURL = _devProperties.getProxyControllerURL();
			if (KElement.isWildCard(proxyURL))
			{
				return false;
			}
			final String queueURL = getDeviceURL();
			if (log.isDebugEnabled())
				log.debug("Sending RequestQueueEntry for " + queueURL + " to: " + proxyURL);
			JMFBuilder jmfBuilder = JMFBuilderFactory.getJMFBuilder(getDeviceID());
			final JDFJMF jmf = jmfBuilder.buildRequestQueueEntry(queueURL, null);
			boolean ok = sendJMF(jmf, proxyURL, null);
			ThreadUtil.wait(mutex, 2222); // wait a short while for an immediate response
			return ok;
		}

		/**
		 * @return the next executable queueentry, null if none were found
		 */
		protected IQueueEntry getQEFromParent()
		{
			IQueueEntry currentQE = getQEFromQueue();
			if (currentQE == null)
			{
				if (sendRequestQueueEntry())
				{
					currentQE = getQEFromQueue();
					if (currentQE != null)
					{
						log.info("processing requested qe: " + currentQE.getQueueEntryID());
					}
				}
			}
			return currentQE;
		}

		/**
		 * @return
		 */
		private IQueueEntry getQEFromQueue()
		{
			IQueueEntry currentQE;
			if (_theQueueProcessor == null)
			{
				log.warn("no queueprocessor for device");
				return null;
			}

			synchronized (_theQueueProcessor.getQueue())
			{
				final QERetrieval canPush = getProperties().getQERetrieval();
				currentQE = _theQueueProcessor.getNextEntry(null, canPush);
				if (currentQE == null && _rootDevice != null)
				{
					currentQE = _rootDevice._theQueueProcessor.getNextEntry(getDeviceID(), canPush);
					importQEFromRoot(currentQE);
				}
				if (currentQE != null)
				{
					currentQE.getQueueEntry().setDeviceID(getDeviceID());
				}
			}
			return currentQE;
		}
	}

	/**
	 * @author prosirai
	 */
	protected class XMLDevice extends XMLDoc
	{

		/**
		 * XML representation of this simDevice fore use as html display using an XSLT
		 * @param addProcs if true, add processor elements
		 * @param request 
		 */
		protected XMLDevice(final boolean addProcs, final ContainerRequest request)
		{
			super("XMLDevice", null);
			prepare();
			final KElement deviceRoot = getRoot();
			setXSLTURL(getXSLT(request));

			deviceRoot.setAttribute(AttributeName.CONTEXT, request.getContextRoot());
			final boolean bModify = request.getBooleanParam("modify");
			deviceRoot.setAttribute("modify", bModify, null);
			deviceRoot.setAttribute("NumRequests", numRequests, null);
			deviceRoot.setAttribute("EntriesProcessed", entriesProcessed, null);
			deviceRoot.setAttribute("VersionString", getVersionString(), null);
			deviceRoot.copyElement(getDeviceTimer(true).toXML(), null);
			deviceRoot.setAttribute(AttributeName.DEVICEID, getDeviceID());
			deviceRoot.setAttribute(AttributeName.DEVICETYPE, getDeviceType());
			deviceRoot.setAttribute("Description", getDescription());
			deviceRoot.setAttribute("DeviceURL", getDeviceURL());
			final IDeviceProperties properties = getProperties();
			deviceRoot.setAttribute("WatchURL", properties.getWatchURL());
			deviceRoot.setAttribute(AttributeName.DEVICESTATUS, getDeviceStatus().getName());
			if (_rootDevice == null && BambiContainer.getInstance() != null)
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
			final File inputHF = getInputHFUrl();
			if (inputHF != null)
			{
				deviceRoot.setAttribute("InputHF", inputHF.getPath());
			}
			final IDeviceProperties properties = getProperties();
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
			if (_theQueueProcessor == null)
			{
				log.error("device with null queueprocessor - bailing ot: ID=" + getDeviceID());
			}
			final JDFQueue jdfQueue = _theQueueProcessor.getQueue();
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
			for (int i = 0; i < _deviceProcessors.size(); i++)
			{
				_deviceProcessors.get(i).addToDisplayXML(getRoot());
			}
		}
	}

	/**
	 * handler for the KnownDevices query
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
			// "I am the known device"
			if (m == null || resp == null)
			{
				return false;
			}
			log.debug("Handling " + m.getType());
			final EnumType typ = m.getEnumType();
			if (EnumType.KnownDevices.equals(typ))
			{

				final JDFDeviceList dl = resp.appendDeviceList();
				appendDeviceInfo(dl);
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

	/**
	 * handler for the Resource Query/Signal
	 */
	public class ResourceHandler extends AbstractHandler
	{
		/**
		 * 
		 */
		public ResourceHandler()
		{
			super(EnumType.Resource, new EnumFamily[] { EnumFamily.Query });
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#handleMessage(org.cip4.jdflib.jmf.JDFMessage, org.cip4.jdflib.jmf.JDFResponse)
		 */
		@Override
		public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
		{
			//TODO reasonable filter on list versus individual
			if (isGlobal(inputMessage))
			{
				return getResourceList(inputMessage, response);
			}
			else
			{
				return getJobResources(inputMessage, response);
			}
		}

		/**
		 * 
		 * check whether we have a global or job context
		 * @param inputMessage
		 * @return
		 */
		protected boolean isGlobal(JDFMessage inputMessage)
		{
			// TODO use JDF 1.5 ResourceQuParams/@Context when available
			boolean b = _theStatusListener == null;
			b = b || !inputMessage.getBoolAttribute(JMFHandler.subscribed, null, false);
			return b;
		}

		protected boolean getJobResources(final JDFMessage inputMessage, final JDFResponse response)
		{
			final StatusCounter sc = _theStatusListener.getStatusCounter();
			final JDFDoc docJMFResource = sc == null ? null : sc.getDocJMFResource();
			if (docJMFResource == null)
			{
				return getResourceList(inputMessage, response);
			}
			final JDFSignal response2 = docJMFResource.getJMFRoot().getSignal(0);
			response.copyInto(response2, false);
			response.removeAttribute(AttributeName.REFID);
			final JDFResourceQuParams inRQP = inputMessage.getResourceQuParams();
			if (inRQP != null)
			{
				final JDFResourceQuParams rqPStatusListner = (JDFResourceQuParams) response.removeChild(ElementName.RESOURCEQUPARAMS, null, 0);
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

		/**
		 * fill a resourcelist - overwrite this with your favorite real lists
		 * @param inMessage 
		 * @param response
		 * @return
		 */
		public boolean getResourceList(JDFMessage inMessage, JDFResponse response)
		{
			return true;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#isSubScribable()
		 */
		@Override
		public boolean isSubScribable()
		{
			return true;
		}

	}

	/**
	 * handler for the Status Query
	 */
	public class StatusHandler extends AbstractHandler
	{
		/**
		 * 
		 */
		public StatusHandler()
		{
			super(EnumType.Status, new EnumFamily[] { EnumFamily.Query });
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
			if (_theStatusListener == null)
			{
				return false;
			}
			if (!_theStatusListener.matchesQuery(inputMessage))
			{
				return false;
			}

			final JDFDoc docJMF = _theStatusListener.getStatusCounter().getDocJMFPhaseTime();
			if (docJMF == null)
			{
				return false;
			}
			final boolean bOK = copyPhaseTimeFromCounter(response, docJMF);
			if (bOK)
			{
				addQueueToStatusResponse(inputMessage, response);
			}
			return bOK;
		}

		/**
		 * @see org.cip4.bambi.core.messaging.JMFHandler.AbstractHandler#isSubScribable()
		 */
		@Override
		public boolean isSubScribable()
		{
			return true;
		}
	}

	protected static final String SHOW_DEVICE = "showDevice";
	protected static final String SHOW_SUBSCRIPTIONS = "showSubscriptions";
	protected final QueueProcessor _theQueueProcessor;
	protected final Vector<AbstractDeviceProcessor> _deviceProcessors;
	protected final SignalDispatcher _theSignalDispatcher;
	protected final JMFHandler _jmfHandler;
	private final QueueEntryRequester qeRequester;

	/**
	 * @return the _jmfHandler
	 */
	public JMFHandler getJMFHandler()
	{
		return _jmfHandler;
	}

	/**
	 * overwrite to provide your favorite version string
	 * @return
	 */
	public String getVersionString()
	{
		return "Generic Bambi Device " + JDFAudit.software();
	}

	/**
	 * hook to add additional information to the SignalDispatcher subscription XML
	 * @param rootList the xml root element
	 */
	public void addMoreToXMLSubscriptions(KElement rootList)
	{
		//nop 
	}

	private final IDeviceProperties _devProperties;
	protected QueueHotFolder _submitHotFolder;
	protected final IConverterCallback _callback;
	protected RootDevice _rootDevice = null;
	protected final StatusListener _theStatusListener;
	protected long numRequests;
	protected boolean acceptAll;
	protected final MyMutex mutex;
	private int entriesProcessed;
	protected int skipIdle;
	protected int idleCount;

	/**
	 * creates a new device instance
	 * @param prop the properties for the device
	 */
	public AbstractDevice(final IDeviceProperties prop)
	{
		super();
		entriesProcessed = idleCount = 0;
		skipIdle = 10;
		_devProperties = prop;
		copyToCache();
		numRequests = 0;
		_submitHotFolder = null;
		_callback = getCallBackClass();
		_jmfHandler = new JMFHandler(this);
		_theSignalDispatcher = linkDispatcher();

		qeRequester = getQueueEntryRequester();
		_deviceProcessors = new Vector<AbstractDeviceProcessor>();
		acceptAll = false;
		preSetup();
		mutex = new MyMutex();
		_theQueueProcessor = buildQueueProcessor();
		_theQueueProcessor.addHandlers(_jmfHandler);
		_theQueueProcessor.addListener(mutex);
		_theStatusListener = new StatusListener(_theSignalDispatcher, getDeviceID(), getICSVersions());
		init();
	}

	/**
	 * 
	 * create the signal dispatcher and link it to the jmf handler
	 * @return
	 */
	private SignalDispatcher linkDispatcher()
	{
		SignalDispatcher s = new SignalDispatcher(_jmfHandler, this);
		s.addHandlers(_jmfHandler);
		_jmfHandler.setDispatcher(s);
		_jmfHandler.setFilterOnDeviceID(true);
		return s;
	}

	/**
	 * preparation setup, e.g. for logging
	 */
	protected void preSetup()
	{
		// empty shell
	}

	/**
	 * copy anything required by this device to the cache directory
	 */
	protected void copyToCache()
	{
		final Vector<File> dirs = getCacheDirs();
		File baseDir = getProperties().getBaseDir();
		for (File configDir : dirs)
		{
			File cacheDir = FileUtil.getFileInDirectory(baseDir, new File(configDir.getName()));
			File[] configFiles = configDir.listFiles();
			if (configFiles == null)
			{
				log.warn("something is wrong - no directory exists: " + configDir.getAbsolutePath());
			}
			else
			{
				for (File f : configFiles)
				{
					File configFile = FileUtil.getFileInDirectory(configDir, new File(f.getName()));
					FileUtil.ensureFileInDir(configFile, cacheDir);
				}
			}
		}
	}

	/**
	 * 
	 * vector of directories that need to be copied to cache
	 * @return
	 */
	protected Vector<File> getCacheDirs()
	{
		Vector<File> v = new Vector<File>();
		final File configDir = getProperties().getConfigDir();
		v.add(configDir);
		return v;
	}

	protected void init()
	{
		final String deviceID = getDeviceID();
		JMFBuilderFactory.setSenderID(deviceID, deviceID);

		createNewProcessor();
		reloadQueue();

		final File hfURL = getInputHFUrl();
		createHotFolder(hfURL);

		addHandlers();
		addWatchSubscriptions();

		StatusOptimizer statusOptimizer = getStatusOptimizer();
		if (statusOptimizer != null)
		{
			log.info("adding statusoptimizer: " + statusOptimizer);
			getJMFFactory().addOptimizer(EnumType.Status, statusOptimizer);
		}
		// defer message sending until everything is set up
		_theSignalDispatcher.startup();
	}

	/**
	 * 
	 *create a new independent processor
	 */
	protected void createNewProcessor()
	{
		final AbstractDeviceProcessor newDevProc = buildDeviceProcessor();
		if (newDevProc != null)
		{
			newDevProc.setParent(this);
			newDevProc.init(_theQueueProcessor, _theStatusListener, _devProperties);
			final String deviceProcessorClass = newDevProc.getClass().getSimpleName();
			String threadName = deviceProcessorClass + "_" + getDeviceID() + "_" + AbstractDeviceProcessor.processorCount++;
			new Thread(newDevProc, threadName).start();
			log.info("device processor thread started: " + threadName);
			_deviceProcessors.add(newDevProc);
		}
	}

	/**
	 * 
	 * get the input hot folder path. If null, we won't create a hot folder
	 * @return
	 */
	protected File getInputHFUrl()
	{
		return _devProperties.getInputHF();
	}

	/**
	 * 
	 *  
	 * @return
	 */
	protected QueueEntryRequester getQueueEntryRequester()
	{
		return new QueueEntryRequester();
	}

	/**
	 * 
	 *  
	 * @return
	 */
	protected IConverterCallback getCallBackClass()
	{
		return _devProperties.getCallBackClass();
	}

	/**
	 * 
	 * get the approriate statusoptimizer<br/>
	 * may be overwritten for additional optimizers
	 * @return
	 */
	protected StatusOptimizer getStatusOptimizer()
	{
		return new StatusOptimizer();
	}

	/**
	 * 
	 */
	protected abstract void reloadQueue();

	/**
	 * add generic subscriptions in case watchurl!=null we'll assume 30 seconds is reasonable...
	 */
	protected void addWatchSubscriptions()
	{
		final String watchURL = _devProperties.getWatchURL();
		if (KElement.isWildCard(watchURL))
		{
			log.info("no watch subscriptions are specified");
			return;
		}

		JMFBuilder jmfBuilder = JMFBuilderFactory.getJMFBuilder(getDeviceID());
		final JDFJMF[] jmfs = jmfBuilder.createSubscriptions(watchURL, null, 30., 0);
		if (jmfs == null)
		{
			return;
		}
		for (int i = 0; i < jmfs.length; i++)
		{
			final JDFQuery query = jmfs[i].getQuery(0);
			updateWatchSubscription(query);
			_theSignalDispatcher.addSubscription(query, null);
		}
	}

	/**
	 * hook to clean up watch subscriptions
	 * @param query
	 */
	protected void updateWatchSubscription(final JDFQuery query)
	{
		// nop
	}

	/**
	 * creates the hotfolder on the file system
	 * @param hfURL the URL of the hotfolder to create. If hfURL is null, no hotfolder will be created.
	 */
	protected void createHotFolder(final File hfURL)
	{
		if (ContainerUtil.equals(hfURL, _submitHotFolder == null ? null : _submitHotFolder.getHfDirectory()))
		{
			return; // no change - bail out
		}
		if (_submitHotFolder != null)
		{
			String oldHF = _submitHotFolder.getHfDirectory().getAbsolutePath();
			log.info("Stopping input hot folder: " + oldHF + " for Device: " + getDeviceID());
			_submitHotFolder.stop();
			_submitHotFolder = null;
		}
		if (hfURL == null)
		{
			return;
		}

		log.info("enabling input hot folder: " + hfURL + " for Device: " + getDeviceID());
		final File hfStorage = new File(getDeviceDir() + File.separator + "HFTmpStorage");
		hfStorage.mkdirs(); // just in case
		if (hfStorage.isDirectory())
		{
			_submitHotFolder = new QueueHotFolder(hfURL, hfStorage, "jdf,xjdf,xml", new QueueHFListener(_theQueueProcessor, _devProperties.getCallBackClass()), null);
		}
		else
		{
			log.error("input hot folder could not be created " + hfURL);
		}
	}

	/**
	 * 
	 */
	protected void addHandlers()
	{
		addHandler(this.new KnownDevicesHandler());
		addHandler(new NotificationHandler(this, _theStatusListener));
		addHandler(AcknowledgeMap.getMap());
		addHandler(getShutdownHandler());
		addJobHandlers();
	}

	/**
	 * create a default shutdown handler
	 * @return
	 */
	public IMessageHandler getShutdownHandler()
	{
		ShutdownJMFHandler shutdownJMFHandler = new ShutdownJMFHandler(this);
		shutdownJMFHandler.setKillContainer(true);
		return shutdownJMFHandler;
	}

	/**
	 * add any job related handlers - may be overwritten by non-job handling devices
	 */
	protected void addJobHandlers()
	{
		addHandler(this.new ResourceHandler());
		addHandler(this.new StatusHandler());
	}

	/**
	 * register an employee with this device
	 * @param emp
	 */
	public void addEmployee(final JDFEmployee emp)
	{
		_theStatusListener.getStatusCounter().addEmployee(emp);
	}

	/**
	 * register an emplyee with this device
	 * @param emp
	 */
	public void removeEmployee(final JDFEmployee emp)
	{
		_theStatusListener.getStatusCounter().removeEmployee(emp);
	}

	/**
	 * get the device type of this device
	 * @return the device type
	 */
	public String getDeviceType()
	{
		return _devProperties.getDeviceType();
	}

	/**
	 * get the device type of this device
	 * @return the device type
	 */
	public String getDescription()
	{
		String description = _devProperties.getDescription();
		if (description == null)
			description = getDeviceType() + " " + getDeviceID() + " " + getVersionString();
		return description;
	}

	/**
	 * @return the device ID
	 */
	public String getDeviceID()
	{
		return _devProperties.getDeviceID();
	}

	/**
	 * @param deviceID
	 * @return the device with ID
	 */
	public AbstractDevice getDevice(final String deviceID)
	{
		if (KElement.isWildCard(deviceID))
		{
			return this;
		}
		return (ContainerUtil.equals(deviceID, getDeviceID())) ? this : null;
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IJMFHandler#processJMF(org.cip4.jdflib.core.JDFDoc)
	 * @param doc
	 * @return the doc representing the response
	 */
	@Override
	public JDFDoc processJMF(final JDFDoc doc)
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
	public boolean appendDeviceInfo(final JDFDeviceList dl)
	{
		final JDFDeviceInfo info = dl.appendDeviceInfo();

		final JDFDevice dev = info.appendDevice();
		fillJDFDevice(dev);
		info.setDeviceStatus(getDeviceStatus());
		info.setDeviceID(getDeviceID());
		return true;
	}

	/**
	 * @param dev the device to fill
	 */
	protected void fillJDFDevice(final JDFDevice dev)
	{
		dev.setDeviceID(getDeviceID());
		dev.setJMFSenderID(getDeviceID());
		dev.setDeviceType(getDeviceType());
		dev.setDescriptiveName(getDeviceType());
		dev.setJMFURL(getDeviceURL());
		dev.setJDFInputURL(UrlUtil.fileToUrl(getInputHFUrl(), false));
		dev.setJDFOutputURL(UrlUtil.fileToUrl(_devProperties.getOutputHF(), false));
		dev.setJDFErrorURL(UrlUtil.fileToUrl(_devProperties.getErrorHF(), false));
		dev.setJDFVersions(EnumVersion.Version_1_3.getName());

		if (_devProperties instanceof DeviceProperties)
		{
			final DeviceProperties dp = (DeviceProperties) _devProperties;
			final KElement root = dp.getDevRoot();
			if (root != null)
			{
				final KElement deviceCap = root.getElement(ElementName.DEVICECAP);
				dev.copyElement(deviceCap, null);
			}
		}
	}

	/**
	 * add a MessageHandler to this devices JMFHandler, if null - don't
	 * @param handler the MessageHandler to add
	 */
	@Override
	public void addHandler(final IMessageHandler handler)
	{
		if (handler != null)
			_jmfHandler.addHandler(handler);
	}

	/**
	 * get the JMFHandler of this device
	 * @return the jmfHandler for this device
	 */
	public IJMFHandler getHandler()
	{
		return _jmfHandler;
	}

	/**
	 * @return the static ICS versions of this
	 */
	public VString getICSVersions()
	{
		if (_devProperties instanceof DeviceProperties)
		{
			return ((DeviceProperties) _devProperties).getICSVersions();
		}
		return null;
	}

	/**
	 * factory for the XML representation of this simDevice fore use as html display using an XSLT
	 * 
	 * @param addProcs
	 * @param request
	 * @return the XMLDEvice
	 */
	public XMLDevice getXMLDevice(final boolean addProcs, final ContainerRequest request)
	{
		return new XMLDevice(addProcs, request);
	}

	/**
	 * @param request request
	 * @return the matching xslt
	 */
	public String getXSLT(final ContainerRequest request)
	{
		final String command = request.getContext();
		String s = "/showDevice.xsl";
		if ("showQueue".equalsIgnoreCase(command) || "modifyQE".equalsIgnoreCase(command))
		{
			s = "/queue2html.xsl";
		}
		if ("showDevice".equalsIgnoreCase(command) || "processNextPhase".equalsIgnoreCase(command))
		{
			s = "/showDevice.xsl";
		}
		if ("showJDF".equalsIgnoreCase(command))
		{
			final String jobPartID = StringUtil.getNonEmpty(request.getParameter(AttributeName.JOBPARTID));
			if (jobPartID == null)
			{
				s = "/jdf.xsl";
			}
			else
			{
				s = "/xjdf.xsl";
			}
		}
		final String contextPath = request.getContextRoot();
		if (contextPath != null)
		{
			s = getXSLTBaseFromContext(contextPath) + s;
		}
		return s;
	}

	/**
	 * @param contextPath
	 * @return
	 */
	protected final String getXSLTBaseFromContext(final String contextPath)
	{
		return "/" + StringUtil.token(contextPath, 0, "/");
	}

	/**
	 * get the DeviceStatus of this device
	 * @return the DeviceStatus. Returns EnumDeviceStatus.Idle, if the StatusListener is null
	 */
	public EnumDeviceStatus getDeviceStatus()
	{
		final StatusListener listener = getStatusListener(0);
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
	public JDFQueueEntry stopProcessing(final String queueEntryID, final EnumNodeStatus status)
	{
		if (status == null && StringUtil.getNonEmpty(queueEntryID) != null)
		{
			getSignalDispatcher().removeSubScriptions(queueEntryID, null, null);
		}

		final AbstractDeviceProcessor deviceProcessor = getProcessor(queueEntryID, 0);
		if (deviceProcessor == null)
		{
			log.warn("cannot find processor to stop for qe=" + queueEntryID);
			return null;
		}
		deviceProcessor.stopProcessing(status);
		final IQueueEntry currentQE = deviceProcessor.getCurrentQE();
		return currentQE == null ? null : currentQE.getQueueEntry();
	}

	/**
	 * gets the device processor for a given queuentry
	 * @param queueEntryID - if null use any
	 * @param n the index of the respective processor
	 * @return the processor that is processing queueEntryID, null if none matches
	 */
	public AbstractDeviceProcessor getProcessor(final String queueEntryID, int n)
	{
		final List<AbstractDeviceProcessor> allProcs = getAllProcessors();
		int nn = 0;
		for (AbstractDeviceProcessor theDeviceProcessor : allProcs)
		{
			final IQueueEntry iqe = theDeviceProcessor.getCurrentQE();
			if (iqe == null) // we have an idle proc
			{
				if (queueEntryID == null) // we are not searching by qeID
				{
					if (nn++ == n) // we have the right count
					{
						return theDeviceProcessor;
					}
				}
				continue;
			}
			else if (queueEntryID == null || queueEntryID.equals(iqe.getQueueEntryID()))
			{
				if (nn++ == n)
				{
					return theDeviceProcessor;
				}
			}
		}
		return null; // none here
	}

	private List<AbstractDeviceProcessor> getAllProcessors()
	{
		return _deviceProcessors;
	}

	/**
	 * stop the signal dispatcher, hot folder and device processor, if they are not null
	 */
	public void shutdown()
	{
		log.info("shutting down device: " + getDeviceID());
		if (_theSignalDispatcher != null)
		{
			_theSignalDispatcher.shutdown();
		}
		Vector<AbstractDeviceProcessor> vTmp = new Vector<AbstractDeviceProcessor>();
		vTmp.addAll(_deviceProcessors);
		for (AbstractDeviceProcessor p : vTmp)
		{
			p.shutdown();
		}
		_deviceProcessors.clear();
		if (_submitHotFolder != null)
		{
			_submitHotFolder.stop();
		}

		if (_theQueueProcessor != null)
		{
			_theQueueProcessor.shutdown();
		}
	}

	/**
	 * reset the signal dispatcher, hot folder and device processor
	 * 
	 * this is a hard reset that removes any and all data!
	 */
	public void reset()
	{
		if (_theSignalDispatcher != null)
		{
			_theSignalDispatcher.reset();
		}

		if (_theQueueProcessor != null)
		{
			_theQueueProcessor.reset();
		}
	}

	/**
	 * build a new QueueProcessor
	 * @return the new queueprocessor
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
	 * returns null if the device cannot process the jdf ticket
	 * @param jdf
	 * @param queueEntryID may be null in case of a new submission
	 * @return list of valid deviceIDS if any, else null if none
	 */
	public abstract VString canAccept(JDFNode jdf, String queueEntryID);

	/**
	 * stub that allows moving data to and from the jdfdoc to the queueentry 
	 * 
	 * @param qe
	 * @param doc
	 * @return the updated queueEntryID
	 */
	public String fixEntry(final JDFQueueEntry qe, final JDFDoc doc)
	{
		final JDFNode root = getNodeFromDoc(doc);
		if (qe == null || root == null)
		{
			return null;
		}
		qe.setFromJDF(root); // set jobid, jobpartid, partmaps
		final int prio = qe.getPriority();
		if (prio > 0)
		{
			final JDFNodeInfo ni = root.getCreateNodeInfo();
			if (!ni.hasAttribute(AttributeName.JOBPRIORITY))
			{
				ni.setJobPriority(prio);
			}
		}
		JDFCustomerInfo ci = root.getInheritedCustomerInfo(null);
		if (ci != null)
		{
			String cid = StringUtil.getNonEmpty(ci.getCustomerID());
			if (cid != null)
			{
				qe.setGeneralID(AttributeName.CUSTOMERID, cid).setDataType(EnumDataType.string);
			}
		}
		final String qeID = qe.getQueueEntryID();
		return qeID;
	}

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
	public StatusListener getStatusListener(final int i)
	{
		if (_deviceProcessors == null || i >= _deviceProcessors.size())
		{
			return null;
		}
		return _deviceProcessors.get(i).getStatusListener();
	}

	/**
	 * handles http get requests - typically web pages...
	 * @param request
	 * @return true if handled
	 */
	@Override
	public XMLResponse handleGet(final ContainerRequest request)
	{
		if (!isMyRequest(request))
		{
			return null;
		}

		if (request.isMyContext(SHOW_DEVICE) || request.isMyContext("jmf") || request.isMyContext("slavejmf"))
		{
			if (request.getBooleanParam("restart") && getRootDevice() != null)
			{
				final AbstractDevice newDev = getRootDevice().createDevice(_devProperties);
				return newDev.showDevice(request, request.getBooleanParam("refresh"));
			}
			else if (request.getBooleanParam("shutdown") && getRootDevice() != null)
			{
				shutdown();
				getRootDevice().removeDevice(getDeviceID());
				return getRootDevice().showDevice(request, false);
			}
			else
			{
				if (request.getBooleanParam("reset"))
				{
					reset();
				}
				updateDevice(request);
				return showDevice(request, request.getBooleanParam("refresh"));
			}
		}
		if (request.isMyContext(SHOW_SUBSCRIPTIONS))
		{
			return _theSignalDispatcher == null ? null : _theSignalDispatcher.handleGet(request);
		}

		if (request.isMyContext("data"))
		{
			return new DataRequestHandler(this, "data").handleGet(request);
		}

		if (_theQueueProcessor != null)
		{
			final XMLResponse r = _theQueueProcessor.handleGet(request);
			if (r != null)
			{
				return r;
			}
		}
		return null;
	}

	/**
	 * @param request
	 */
	protected void updateDevice(final ContainerRequest request)
	{

		final JDFAttributeMap map = request.getParameterMap();
		final Set<String> stringSet = map == null ? null : map.keySet();
		if (stringSet == null)
			return;

		final String watchURL = request.getParameter("WatchURL");
		if (watchURL != null && stringSet.contains("WatchURL"))
		{
			updateWatchURL(watchURL);
		}
		if (stringSet.contains("InputHF"))
		{
			final String hf = request.getParameter("InputHF");
			updateInputHF(hf);
		}
		if (stringSet.contains("OutputHF"))
		{
			final String hf = request.getParameter("OutputHF");
			updateOutputHF(hf);
		}
		if (stringSet.contains("ErrorHF"))
		{
			final String hf = request.getParameter("ErrorHF");
			updateErrorHF(hf);
		}
		if (stringSet.contains("UpdateDump") && (_rootDevice == null))
		{
			final boolean dumpSwitch = request.getBooleanParam("Dump");
			updateDump(dumpSwitch);
		}

		if (stringSet.contains("DeviceType"))
		{
			final String deviceType = request.getParameter("DeviceType");
			updateDeviceType(deviceType);
		}
		if (stringSet.contains("Description"))
		{
			final String description = request.getParameter("Description");
			updateDescription(description);
		}
	}

	/**
	 *  
	 * @param description
	 */
	private void updateDescription(String description)
	{
		final String old = getDescription();
		if (ContainerUtil.equals(old, description))
		{
			return;
		}
		IDeviceProperties properties = getProperties();
		properties.setDescription(description);
		properties.serialize();
	}

	/**
	 *  
	 * @param dumpSwitch
	 */
	protected void updateDump(boolean dumpSwitch)
	{
		BambiContainer container = BambiContainer.getInstance();
		if (container != null)
		{
			log.info("Switching http dump " + (dumpSwitch ? "on" : "off"));
			container.setWantDump(dumpSwitch);
			if (_devProperties instanceof DeviceProperties)
			{
				KElement root = ((DeviceProperties) _devProperties).getRoot();
				boolean old = root.getBoolAttribute("Dump", null, true);
				if (dumpSwitch != old)
				{
					root.setAttribute("Dump", dumpSwitch, null);
					_devProperties.serialize();
				}
			}
		}
	}

	/**
	 * update newWatchURL from the UI
	 * @param newWatchURL 
	 */
	private void updateWatchURL(String newWatchURL)
	{
		if ("-".equals(newWatchURL))
		{
			newWatchURL = null;
			log.info("explicitly removing watchUrl");
		}
		final IDeviceProperties properties = getProperties();
		final String oldWatchURL = properties.getWatchURL();
		if (!ContainerUtil.equals(oldWatchURL, newWatchURL))
		{
			newWatchURL = StringUtil.getNonEmpty(newWatchURL);
			// explicit empty strings must be handled
			if (newWatchURL != null && !UrlUtil.isHttp(newWatchURL))
			{
				log.warn("attempting to set invalid watch url: (" + newWatchURL + ") ignore");
				return;
			}
			properties.setWatchURL(newWatchURL);
			if (StringUtil.getNonEmpty(oldWatchURL) != null)
			{
				log.info("removing watch subscriptions to: " + oldWatchURL);
				_theSignalDispatcher.removeSubScriptions(null, oldWatchURL, null);
			}
			addWatchSubscriptions();
			properties.serialize();
		}
	}

	/**
	 * @param newHF 
	 * 
	 */
	private void updateInputHF(String newHF)
	{
		log.info("request update hf to: " + newHF);
		newHF = StringUtil.getNonEmpty(newHF);
		final IDeviceProperties properties = getProperties();
		final File oldHF = properties.getInputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			log.info(" update hf from " + oldHF + " to: " + newHFF);
			properties.setInputHF(newHFF);
			properties.serialize();
			createHotFolder(newHFF);
		}
	}

	/**
	 * @param newHF 
	 * 
	 */
	private void updateOutputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IDeviceProperties properties = getProperties();
		final File oldHF = properties.getOutputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setOutputHF(newHF == null ? null : new File(newHF));
			properties.serialize();
		}
	}

	/**
	 * @param newHF 
	 * 
	 */
	private void updateErrorHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IDeviceProperties properties = getProperties();
		final File oldHF = properties.getErrorHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setErrorHF(newHF == null ? null : new File(newHF));
			properties.serialize();
		}
	}

	/**
	 * @param newDeviceType 
	 * 
	 */
	private void updateDeviceType(final String newDeviceType)
	{
		if (newDeviceType == null)
		{
			return;
		}
		final IDeviceProperties properties = getProperties();
		final String oldDeviceType = properties.getDeviceType();
		if (ContainerUtil.equals(oldDeviceType, newDeviceType))
		{
			return;
		}
		properties.setDeviceType(newDeviceType);
		properties.serialize();
	}

	/**
	 * increments the request counter
	 */
	private void incNumRequests()
	{
		numRequests++;
	}

	/**
	 * method called when processing begins
	 */
	public void startWork()
	{
		incNumRequests();
		getDeviceTimer(false).start();
	}

	/**
	 * @param bGlobal 
	 * @return
	 */
	public CPUTimer getDeviceTimer(boolean bGlobal)
	{
		CPUTimerFactory factory = CPUTimer.getFactory();
		final String id = "AbstractDevice_" + getDeviceID();
		CPUTimer ct = bGlobal ? factory.getGlobalTimer(id) : factory.getCreateCurrentTimer(id);
		return ct;
	}

	/**
	 * method called when processing ends
	 */
	public void endWork()
	{
		getDeviceTimer(false).stop();
	}

	protected boolean isMyRequest(final ContainerRequest request)
	{
		return request.isMyRequest(getDeviceID());
	}

	/**
	 * send a jmf via the factory, setting all defaults to this device
	 * 
	 * @param jmf the jmf to send
	 * @param url the url to send to
	 * @param responseHandler the response handler - may be null
	 * 
	 * @return true if successfully queued
	 */
	public boolean sendJMF(final JDFJMF jmf, final String url, final IResponseHandler responseHandler)
	{
		return getJMFFactory().send2URL(jmf, url, responseHandler, getCallback(url), getDeviceID());
	}

	/**
	 * @return
	 */
	public SignalDispatcher getSignalDispatcher()
	{
		return _theSignalDispatcher;
	}

	/**
	 * @param url
	 * @return
	 */
	public IConverterCallback getCallback(final String url)
	{
		return _callback;
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
	 * @return the root directory for JDFs
	 */
	public File getJDFDir()
	{
		return FileUtil.getFileInDirectory(getDeviceDir(), new File("JDF"));
	}

	/**
	 * return the name of the JDF file storage for a given queueentryid
	 * 
	 * @param newQEID the QueueEntryID of the entry to search
	 * @return {@link String} the file name of the storage
	 */
	public String getJDFStorage(final String newQEID)
	{
		if (newQEID == null)
		{
			return null;
		}
		return getJDFDir() + File.separator + newQEID + ".jdf";
	}

	/**
	 * @return the cached configuration directory
	 */
	public File getCachedConfigDir()
	{
		File baseDir = getProperties().getBaseDir();
		File configDir = FileUtil.getFileInDirectory(baseDir, new File("config"));
		return configDir;
	}

	/**
	 * return the name of the storage directory for a given queueentryid
	 * 
	 * @param newQEID the QueueEntryID of the entry to search, if null. get the parent directory
	 * @return {@link File} the file of the storage
	 */
	public File getJobDirectory(final String newQEID)
	{
		if (newQEID == null)
		{
			return getJDFDir();
		}
		return FileUtil.getFileInDirectory(getJDFDir(), new File(newQEID));
	}

	/**
	 * @return the application base directory
	 */
	public File getBaseDir()
	{
		return _devProperties.getBaseDir();
	}

	/**
	 * @return the application base directory for an individual device
	 */
	public File getDeviceDir()
	{
		return FileUtil.getFileInDirectory(getBaseDir(), new File(getDeviceID()));
	}

	/**
	 * @return the url of this device
	 */
	public String getDeviceURL()
	{
		return _devProperties.getDeviceURL();
	}

	/**
	 * @return the number of active processors
	 */
	public int activeProcessors()
	{
		int siz = _deviceProcessors == null ? 0 : _deviceProcessors.size();
		for (int i = siz - 1; i >= 0; i--)
		{
			if (!_deviceProcessors.get(i).isActive())
			{
				siz--;
			}
		}
		return siz;
	}

	protected XMLResponse showDevice(final ContainerRequest request, final boolean refresh)
	{

		final XMLDevice simDevice = getXMLDevice(true, request);
		if (refresh)
		{
			simDevice.getRoot().setAttribute("refresh", true, null);
		}

		XMLResponse r = new XMLResponse(simDevice.getRoot());
		return r;
	}

	/**
	 * get the root controller
	 * @return the root controller
	 */
	public RootDevice getRootDevice()
	{
		return _rootDevice;
	}

	/**
	 * @return
	 */
	public JMFFactory getJMFFactory()
	{
		return JMFFactory.getJMFFactory();
	}

	/**
	 * set the root controller device
	 * @param rootDevice the root controller
	 */
	public void setRootDevice(final RootDevice rootDevice)
	{
		this._rootDevice = rootDevice;
		if (_theStatusListener != null && _rootDevice != null)
		{
			_theStatusListener.setRootDispatcher(_rootDevice._theSignalDispatcher);
		}
	}

	/**
	 * @see org.cip4.bambi.core.messaging.IJMFHandler#getHandler(java.lang.String, org.cip4.jdflib.jmf.JDFMessage.EnumFamily)
	 */
	@Override
	public IMessageHandler getHandler(final String typ, final EnumFamily family)
	{
		return _jmfHandler == null ? null : _jmfHandler.getHandler(typ, family);
	}

	/**
	 * @param inputMessage
	 * @param response
	 */
	public void addQueueToStatusResponse(final JDFMessage inputMessage, final JDFResponse response)
	{
		final JDFStatusQuParams statusQuParams = inputMessage.getStatusQuParams();
		final boolean bQueue = statusQuParams == null ? false : statusQuParams.getQueueInfo();
		if (bQueue)
		{
			final JDFQueue queue = _theQueueProcessor.getQueue();
			if (queue != null)
			{
				synchronized (queue)
				{
					final JDFQueue qq = (JDFQueue) response.copyElement(queue, null);
					QueueProcessor.removeBambiNSExtensions(qq);
				}
			}
			else
			{
				log.warn("no queue in queueprocessor - ignoring");
			}
		}
	}

	/**
	 * @param response
	 * @param docJMF
	 * @return true if successful
	 */
	protected boolean copyPhaseTimeFromCounter(final JDFResponse response, final JDFDoc docJMF)
	{
		final JDFJMF root = docJMF == null ? null : docJMF.getJMFRoot();
		if (root == null)
		{
			log.error("StatusHandler.handleMessage: StatusCounter response = null");
			return false;
		}
		final JDFJMF respRoot = response.getJMFRoot();
		final int nResp = root.numChildElements(ElementName.RESPONSE, null);
		final String refID = response.getrefID();

		for (int i = 0; i < nResp; i++)
		{
			final JDFResponse r = root.getResponse(i);
			final JDFResponse rResp = respRoot.getCreateResponse(i);
			final String id = rResp.getID();
			rResp.setrefID(refID);
			rResp.setAttributes(r);
			rResp.setID(id);
			final VElement v = r.getChildElementVector(null, null);
			if (v != null)
			{
				final int siz = v.size();
				for (int j = 0; j < siz; j++)
				{
					rResp.copyElement(v.elementAt(j), null);
				}
			}
		}
		return true;
	}

	/**
	 * flushes any and all message queues
	 */
	public void flush()
	{
		if (_theSignalDispatcher != null)
		{
			_theSignalDispatcher.flush();
		}
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
	}

	/**
	 * continue with a queueentry at startup
	 * @param qe the queueentry to continue with
	 */
	public void continueQE(final JDFQueueEntry qe)
	{
		// nop
	}

	/**
	 * @return the gethandler for this device
	 */
	public IGetHandler getGetDispatchHandler()
	{
		return this;
	}

	/**
	 * must I go? used e.g. for licensing
	 * @return
	 */
	public boolean mustDie()
	{
		return false;
	}

	/**
	 * @return the next executable queueentry, null if none were found
	 */
	protected IQueueEntry getQEFromParent()
	{
		return qeRequester.getQEFromParent();
	}

	/**
	 * flush the file from the processor and then display it
	 * @param qeID
	 * @return
	 */
	public String getUpdatedFile(String qeID)
	{
		AbstractDeviceProcessor proc = getProcessor(qeID, 0);
		if (proc != null)
		{
			StatusListener sl = proc.getStatusListener();
			if (sl != null)
				sl.saveJDF(5000); // we don't need the very newest but it shouldn't be older than a few seconds
		}
		final String fil = getJDFStorage(qeID);
		return fil;
	}

	/**
	 * get the data url, if no data forwarding is defined, return null
	 * @param queueEntry
	 * @param bSubmit if true, called incoming, else returning
	 * @return
	 */
	public String getDataURL(JDFQueueEntry queueEntry, boolean bSubmit)
	{
		return null;
	}

	/**
	 * @return the _submitHotFolder
	 */
	public QueueHotFolder getQueueSubmitHotFolder()
	{
		return _submitHotFolder;
	}

	/**
	 * convert any XML to the appropriate JMF
	 * the default implementation only checks whether it already is jmf
	 * 
	 * @param request
	 * @return
	 */
	public XMLRequest convertToJMF(XMLRequest request)
	{
		KElement e = request.getXML();
		if (e == null)
			return null;
		if (e instanceof JDFJMF)
		{
			return request;
		}
		if (!(e instanceof JDFNode) && !XJDF20.rootName.equals(e.getLocalName()))
		{
			KElement e2 = e.getChildByTagName(ElementName.JDF, null, 0, null, false, false);
			if (e2 == null)
				e2 = e.getChildByTagName(XJDF20.rootName, null, 0, null, false, false);
			if (e2 != null)
			{
				e = e2.cloneNewDoc();
			}
		}
		if (e instanceof JDFNode || XJDF20.rootName.equals(e.getLocalName()))
		{
			final XMLRequest r2 = createSubmitFromJDF(e);
			if (r2 != null)
			{
				r2.setContainer(request);
				return r2;
			}
		}
		else
		{

		}
		return null;
	}

	/**
	 * create a submitqueuentry from raw JDF
	 * @param e the jdf or xjdf root element
	 * @return the xmlrequest to submit
	 */
	protected XMLRequest createSubmitFromJDF(KElement e)
	{
		JDFJMF sqe = new JMFBuilder().buildSubmitQueueEntry(null);
		sqe.getCommand(0).getQueueSubmissionParams(0).setURL("dummy");
		MimeWriter mimeWriter = new MimeWriter();
		mimeWriter.buildMimePackage(sqe.getOwnerDocument_JDFElement(), e.getOwnerDocument_KElement(), false);
		MimeReader mimeReader = new MimeReader(mimeWriter);
		BodyPartHelper jmfHelper = mimeReader.getBodyPartHelper(0);
		JDFDoc docJDF = jmfHelper.getJDFDoc();
		return docJDF != null ? new XMLRequest(docJDF) : null;
	}

	/**
	 * get the directory for extracted files
	 * defaults to the device's job directory see {@link AbstractDevice#getJobDirectory(String)}
	 * @param qe the queueEntry to get a job directory for
	 * @param bSubmit if true, called incomuing, else outgoing
	 * @return the directory to dump to
	 */
	public File getExtractDirectory(JDFQueueEntry qe, boolean bSubmit)
	{
		return getJobDirectory(qe == null ? null : qe.getQueueEntryID());
	}

	/**
	 * @return the dataextractor required for this
	 * @param bSubmit if true we are in the submission process, if false in the return process
	 */
	public DataExtractor getDataExtractor(boolean bSubmit)
	{
		return new DataExtractor(this, bSubmit);
	}

	/**
	 * return true if the node/ qe combination is active
	 * @param n
	 * @param qe
	 * @return
	 */
	public boolean isActive(JDFNode n, JDFQueueEntry qe)
	{
		if (n == null || qe == null)
			return false;
		return EnumActivation.isActive(n.getActivation(true)) && EnumActivation.isActive(EnumActivation.getEnum(qe.getAttribute(AttributeName.ACTIVATION)));
	}

	/**
	 *  
	 */
	void incEntriesProcessed()
	{
		entriesProcessed++;
	}

	/**
	 * all dispatched signals are checked here and may be modified / removed by a callback
	 * by default, we skip all idle status signals except 1 of 10
	 * @param s
	 * @return true if s should be deleted
	 */
	public boolean deleteSignal(JDFSignal s)
	{
		if (s == null)
			return true;

		EnumType typ = s.getEnumType();
		if (EnumType.Status.equals(typ))
		{
			final Vector<JDFDeviceInfo> devInfos = s.getChildrenByClass(JDFDeviceInfo.class, false, -1);
			if (devInfos == null || devInfos.size() == 0)
			{
				log.warn("removing status signal with no DeviceInfo; ID=" + s.getID());
				return true;
			}

			for (JDFDeviceInfo di : devInfos)
			{
				EnumDeviceStatus stat = di.getDeviceStatus();
				if (!EnumDeviceStatus.Idle.equals(stat) && !EnumDeviceStatus.Down.equals(stat))
				{
					idleCount = 0;
					return false; // we have something that is a bit active
				}
			}
			return (idleCount++ % skipIdle) != 0;
		}
		return false;
	}
}
