/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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

package org.cip4.bambi.proxy;

import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.AbstractDeviceProcessor;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.BambiServletRequest;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.messaging.JMFBufferHandler;
import org.cip4.bambi.core.messaging.JMFBuilder;
import org.cip4.bambi.core.messaging.MessageSender.MessageResponseHandler;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.QueueHotFolder;
import org.cip4.jdflib.util.QueueHotFolderListener;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public abstract class AbstractProxyDevice extends AbstractDevice
{

	/**
	 * the url flag for incoming messages
	 */
	public static final String SLAVEJMF = "slavejmf";
	protected QueueHotFolder slaveJDFOutput = null;
	protected QueueHotFolder slaveJDFError = null;

	/**
	 * @author Rainer Prosi, Heidelberger Druckmaschinen enumeration how to set up synchronization of status with the slave
	 */
	public enum EnumSlaveStatus
	{
		/**
		 * update status via JMF
		 */
		JMF,

		/**
		 * update status via NodeInfo/JMF
		 */
		NODEINFO
	}

	protected EnumSlaveStatus slaveStatus = null;
	protected IConverterCallback _slaveCallback;

	protected class QueueEntryAbortHandler extends MessageResponseHandler
	{
		private final EnumNodeStatus newStatus;

		/**
		 * @param newStatus the status type of the original message, e.g Aborted for AbortQE
		 * @param refID the refID of the outgoing message
		 */
		public QueueEntryAbortHandler(final EnumNodeStatus newStatus, final String refID)
		{
			super(refID);
			this.newStatus = newStatus;
		}

		@Override
		public boolean handleMessage()
		{
			super.handleMessage();
			if (resp != null)
			{
				System.out.print(resp);
			}
			// TODO actually handle the queue updates in here, rather than downstream
			return true;
		}

		/**
		 * @return the final status, null if the message failed
		 */
		public EnumNodeStatus getFinalStatus()
		{
			final JDFMessage response = getFinalMessage();
			if (response != null)
			{
				if (response.getReturnCode() == 0)
				{
					return newStatus;
				}
			}
			return null;
		}
	}

	protected class QueueSynchronizeHandler extends MessageResponseHandler
	{
		/**
		 * 
		 */
		public QueueSynchronizeHandler()
		{
			super(null);
		}

		@Override
		public boolean handleMessage()
		{
			super.handleMessage();
			final JDFMessage r = getFinalMessage();
			if (r != null)
			{
				final JDFQueue q = r.getQueue(0);
				if (q != null)
				{
					final Map<String, JDFQueueEntry> slaveMap = q.getQueueEntryIDMap();
					final JDFQueue myQueue = _theQueueProcessor.getQueue();
					synchronized (myQueue)
					{
						final VElement vQ = myQueue.getQueueEntryVector();
						for (int i = 0; i < vQ.size(); i++)
						{
							final JDFQueueEntry qe = (JDFQueueEntry) vQ.get(i);
							final String slaveQEID = BambiNSExtension.getSlaveQueueEntryID(qe);
							if (slaveQEID != null)
							{
								final JDFQueueEntry slaveQE = slaveMap == null ? null : slaveMap.get(slaveQEID);
								if (slaveQE != null)
								{
									final EnumQueueEntryStatus status = slaveQE.getQueueEntryStatus();
									if (!ContainerUtil.equals(status, qe.getQueueEntryStatus()))
									{
										_theQueueProcessor.updateEntry(qe, status, null, null);
										if (EnumQueueEntryStatus.Completed.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Completed);
										}
										else if (EnumQueueEntryStatus.Aborted.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Aborted);
										}
										else if (EnumQueueEntryStatus.Suspended.equals(status))
										{
											stopProcessing(qe.getQueueEntryID(), EnumNodeStatus.Suspended);
										}
									}
								}
								else
								{
									log.info("Slave queueentry " + slaveQEID + " was removed");
									_theQueueProcessor.updateEntry(qe, EnumQueueEntryStatus.Removed, null, null);
								}
							}
						}
					}
				}
			}
			return true; // we always assume ok
		}
	}

	protected class ReturnHFListner implements QueueHotFolderListener
	{
		private final EnumQueueEntryStatus hfStatus;

		/**
		 * @param aborted
		 */
		public ReturnHFListner(final EnumQueueEntryStatus status)
		{
			hfStatus = status;
		}

		public void submitted(final JDFJMF submissionJMF)
		{
			log.info("ReturnHFListner:submitted");
			final JDFCommand command = submissionJMF.getCommand(0);
			final JDFReturnQueueEntryParams rqp = command.getReturnQueueEntryParams(0);

			final JDFDoc doc = rqp == null ? null : rqp.getURLDoc();
			if (doc == null)
			{
				log.warn("could not process JDF File");
				return;
			}
			if (_jmfHandler != null)
			{
				final KElement n = doc.getRoot();
				if (n == null)
				{
					log.warn("could not process JDF File");
					return;
				}

				// assume the rootDev was the executed baby...
				rqp.setAttribute(hfStatus.getName(), n.getAttribute(AttributeName.ID));
				// let the standard returnqe handler do the work
				final JDFDoc responseJMF = _jmfHandler.processJMF(submissionJMF.getOwnerDocument_JDFElement());
				try
				{
					final JDFJMF jmf = responseJMF.getJMFRoot();
					final JDFResponse r = jmf.getResponse(0);
					if (r != null && r.getReturnCode() == 0)
					{
						final File urlToFile = UrlUtil.urlToFile(rqp.getURL());
						boolean byebye = false;
						if (urlToFile != null)
						{
							byebye = urlToFile.delete();
						}
						if (!byebye)
						{
							log.error("could not delete JDF File: " + urlToFile);
						}
					}
					else
					{
						log.error("could not process JDF File");
					}
				}
				catch (final Exception e)
				{
					handleError(submissionJMF);
				}
			}
		}

		/**
		 * @param submissionJMF
		 */
		private void handleError(final JDFJMF submissionJMF)
		{
			log.error("error handling hf return");
		}
	}

	/**
	 * @author prosirai
	 */
	protected class XMLProxyDevice extends XMLDevice
	{

		/**
		 * XML representation of this simDevice fore use as html display using an XSLT
		 * @param addProcs - always ignored
		 * @param request BambiServletRequest http context in which this is called
		 */
		public XMLProxyDevice(final boolean addProcs, final BambiServletRequest request)
		{
			// proxies never show processors
			super(false, request);
			updateSlaveProperties();
		}

		/**
		 * 
		 */
		private void updateSlaveProperties()
		{
			final KElement deviceRoot = getRoot();
			final IProxyProperties proxyProperties = getProxyProperties();
			if (proxyProperties != null)
			{
				deviceRoot.setAttribute("SlaveURL", proxyProperties.getSlaveURL());
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
			}
		}
	}

	/**
	 * @param properties properties with device details
	 */
	public AbstractProxyDevice(final IDeviceProperties properties)
	{
		super(properties);
		final IProxyProperties proxyProperties = getProxyProperties();
		_slaveCallback = proxyProperties.getSlaveCallBackClass();

		prepareSlaveHotfolders(proxyProperties);
		_jmfHandler.setFilterOnDeviceID(false);
		_theSignalDispatcher.setIgnoreURL(getDeviceURLForSlave());
	}

	/**
	 * prepare output and error hot folders if they are specified
	 * @param proxyProperties
	 */
	private void prepareSlaveHotfolders(final IProxyProperties proxyProperties)
	{
		final File fDeviceJDFOutput = proxyProperties.getSlaveOutputHF();
		if (fDeviceJDFOutput != null)
		{
			reloadSlaveOutputHF(fDeviceJDFOutput);
		}

		final File fDeviceErrorOutput = proxyProperties.getSlaveErrorHF();
		if (fDeviceErrorOutput != null)
		{
			reloadSlaveErrorHF(fDeviceErrorOutput);
		}
	}

	/**
	 * @param fDeviceErrorOutput
	 */
	private void reloadSlaveErrorHF(final File fDeviceErrorOutput)
	{
		if (slaveJDFError != null)
		{
			slaveJDFError.stop();
			slaveJDFError = null;
		}
		final File hfStorage = new File(_devProperties.getBaseDir() + File.separator + "HFDevTmpStorage"
				+ File.separator + _devProperties.getDeviceID());
		log.info("Device error output HF:" + fDeviceErrorOutput.getPath() + " device ID= " + getSlaveDeviceID());
		final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
		slaveJDFError = new QueueHotFolder(fDeviceErrorOutput, hfStorage, null, new ReturnHFListner(EnumQueueEntryStatus.Aborted), rqCommand);
	}

	/**
	 * @param proxyProperties
	 * @param fDeviceJDFOutput
	 */
	private void reloadSlaveOutputHF(final File fDeviceJDFOutput)
	{
		if (slaveJDFOutput != null)
		{
			slaveJDFOutput.stop();
			slaveJDFOutput = null;
		}
		final File hfStorage = new File(getDeviceDir() + File.separator + "HFDevTmpStorage");
		log.info("Device output HF:" + fDeviceJDFOutput.getPath() + " device ID= " + getSlaveDeviceID());
		final JDFJMF rqCommand = JDFJMF.createJMF(EnumFamily.Command, EnumType.ReturnQueueEntry);
		slaveJDFOutput = new QueueHotFolder(fDeviceJDFOutput, hfStorage, null, new ReturnHFListner(EnumQueueEntryStatus.Completed), rqCommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#init()
	 */
	@Override
	protected void init()
	{
		final IProxyProperties dp = getProxyProperties();
		_slaveCallback = dp.getSlaveCallBackClass();
		super.init();
		_theQueueProcessor.getQueue().resumeQueue(); // proxy queues should start up by default
	}

	public IConverterCallback getSlaveCallback()
	{
		return _slaveCallback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveDeviceID()
	 */
	public String getSlaveDeviceID()
	{
		// TODO - dynamically grab with knowndevices
		return getProxyProperties().getSlaveDeviceID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cip4.bambi.core.AbstractDevice#shutdown()
	 */
	@Override
	public void shutdown()
	{
		super.shutdown();
		if (slaveJDFError != null)
		{
			slaveJDFError.stop();
		}
		if (slaveJDFOutput != null)
		{
			slaveJDFOutput.stop();
		}
	}

	/**
	 * sends messages to the slave to stop processing
	 * @param newStatus
	 * @param slaveQE
	 */
	protected EnumNodeStatus stopSlaveProcess(final String slaveQE, final EnumNodeStatus newStatus)
	{
		JDFJMF jmf = null;
		final JMFBuilder builder = getBuilderForSlave();
		if (EnumNodeStatus.Aborted.equals(newStatus))
		{
			jmf = builder.buildAbortQueueEntry(slaveQE);
		}
		else if (EnumNodeStatus.Suspended.equals(newStatus))
		{
			jmf = builder.buildSuspendQueueEntry(slaveQE);
		}
		if (jmf != null)
		{
			final QueueEntryAbortHandler ah = new QueueEntryAbortHandler(newStatus, jmf.getCommand(0).getID());
			getJMFFactory().send2URL(jmf, getProxyProperties().getSlaveURL(), ah, _slaveCallback, getDeviceID());
			ah.waitHandled(5000, 10000, false);
			return ah.getFinalStatus();
		}
		return null;
	}

	/**
	 * get the JMF Builder for messages to the slave device; also allow for asynch submission handling
	 * @return
	 */
	protected JMFBuilder getBuilderForSlave()
	{
		final JMFBuilder builder = new JMFBuilder();
		if (getProxyProperties().getDeviceURLForSlave() != null)
		{
			builder.setAcknowledgeURL(getProxyProperties().getDeviceURLForSlave());
		}
		return builder;
	}

	/**
	 * @return
	 */
	public EnumSlaveStatus getSlaveStatus()
	{
		final String s = getProperties().getDeviceAttribute("SlaveStatus");
		if (s == null)
		{
			return null;
		}
		return EnumSlaveStatus.valueOf(s.toUpperCase());
	}

	/**
	 * @return the url of this proxy that the slave sends messages to
	 */
	public String getDeviceURLForSlave()
	{
		return getProxyProperties().getDeviceURLForSlave();
	}

	/**
	 * get the correct callback assuming that all slave urls contain the string "/slavejmf"
	 * @see org.cip4.bambi.core.AbstractDevice#getCallback(java.lang.String)
	 */
	@Override
	public IConverterCallback getCallback(final String url)
	{
		if (StringUtil.hasToken(url, SLAVEJMF, "/", 0))
		{
			return _slaveCallback;
		}
		if (ContainerUtil.equals(getProxyProperties().getDeviceURLForSlave(), url))
		{
			return _slaveCallback;
		}
		return _callback;
	}

	/**
	 * @return the proxyProperties
	 */
	public IProxyProperties getProxyProperties()
	{
		return (IProxyProperties) _devProperties;
	}

	/**
	 * add a generic catch-all buffer handler that simply proxies all messages
	 */
	@Override
	protected void addHandlers()
	{
		super.addHandlers();
		addHandler(new JMFBufferHandler("*", new EnumFamily[] { EnumFamily.Signal }, _theSignalDispatcher, _theQueueProcessor));
	}

	@Override
	protected AbstractDeviceProcessor buildDeviceProcessor()
	{
		return null;
	}

	@Override
	public JDFNode getNodeFromDoc(final JDFDoc doc)
	{
		return null;
	}

	/**
	 * @param request
	 */
	@Override
	protected void updateDevice(final BambiServletRequest request)
	{
		super.updateDevice(request);

		final Enumeration<String> en = request.getParameterNames();
		final Set<String> s = ContainerUtil.toHashSet(en);

		String slave = request.getParameter("SlaveURL");
		if (slave != null && s.contains("SlaveURL"))
		{
			updateSlaveURL(slave);
		}
		slave = request.getParameter("SlaveDeviceID");
		if (slave != null && s.contains("SlaveDeviceID"))
		{
			updateSlaveDeviceID(slave);
		}
		slave = request.getParameter("MaxPush");
		if (slave != null && s.contains("MaxPush"))
		{
			updateMaxPush(slave);
		}

		String hf = request.getParameter("SlaveInputHF");
		if (hf != null && s.contains("SlaveInputHF"))
		{
			updateSlaveInputHF(hf);
		}
		hf = request.getParameter("SlaveOutputHF");
		if (hf != null && s.contains("SlaveOutputHF"))
		{
			updateSlaveOutputHF(hf);
		}
		hf = request.getParameter("SlaveErrorHF");
		if (hf != null && s.contains("SlaveErrorHF"))
		{
			updateSlaveErrorHF(hf);
		}

	}

	/**
	 * 
	 */
	private void updateSlaveErrorHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProxyProperties();
		final File oldHF = properties.getSlaveErrorHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveErrorHF(newHFF);
			properties.serialize();
		}
	}

	/**
	 * 
	 */
	private void updateSlaveOutputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProxyProperties();
		final File oldHF = properties.getSlaveOutputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveOutputHF(newHFF);
			properties.serialize();
		}
	}

	/**
	 * 
	 */
	private void updateSlaveInputHF(String newHF)
	{
		newHF = StringUtil.getNonEmpty(newHF);
		final IProxyProperties properties = getProxyProperties();
		final File oldHF = properties.getSlaveInputHF();
		final File newHFF = newHF == null ? null : new File(newHF);
		if (!ContainerUtil.equals(oldHF, newHFF))
		{
			properties.setSlaveInputHF(newHFF);
			properties.serialize();
		}
	}

	/**
	 * 
	 */
	private void updateSlaveURL(final String newSlaveURL)
	{
		if (newSlaveURL == null)
		{
			return;
		}
		final IProxyProperties properties = getProxyProperties();
		final String oldSlaveURL = properties.getSlaveURL();
		if (ContainerUtil.equals(oldSlaveURL, newSlaveURL))
		{
			return;
		}
		properties.setSlaveURL(newSlaveURL);
		properties.serialize();
	}

	/**
	 * 
	 */
	private void updateMaxPush(final String push)
	{
		final int iPush = StringUtil.parseInt(push, -1);
		if (iPush < 0)
		{
			return;
		}
		final IProxyProperties properties = getProxyProperties();
		final int oldPush = properties.getMaxPush();
		if (oldPush == iPush)
		{
			return;
		}
		properties.setMaxPush(iPush);
		properties.serialize();
	}

	/**
	 * 
	 */
	private void updateSlaveDeviceID(final String newSlave)
	{
		if (newSlave == null)
		{
			return;
		}
		final IProxyProperties properties = getProxyProperties();
		final String oldSlave = properties.getSlaveURL();
		if (ContainerUtil.equals(oldSlave, newSlave))
		{
			return;
		}
		properties.setSlaveDeviceID(newSlave);
		properties.serialize();
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#getXMLDevice(boolean, org.cip4.bambi.core.BambiServletRequest)
	 */
	@Override
	public XMLDevice getXMLDevice(final boolean addProcs, final BambiServletRequest request)
	{
		return new XMLProxyDevice(addProcs, request);
	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#reloadQueue()
	 */
	@Override
	protected void reloadQueue()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.cip4.bambi.core.AbstractDevice#canAccept(org.cip4.jdflib.core.JDFDoc, java.lang.String)
	 */
	@Override
	public int canAccept(final JDFDoc doc, final String queueEntryID)
	{
		if (queueEntryID == null)
		{
			return 0;
		}
		final AbstractProxyProcessor proc = (AbstractProxyProcessor) getProcessor(queueEntryID, 0);
		if (proc == null)
		{
			return 0; // no processor is working on queueEntryID - assume ok
		}
		return proc.canAccept(doc, queueEntryID);
	}

}