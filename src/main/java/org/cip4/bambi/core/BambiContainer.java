/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.mail.BodyPart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.extensions.XJDFConstants;
import org.cip4.jdflib.extensions.XJMFHelper;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.BodyPartHelper;
import org.cip4.jdflib.util.mime.MimeReader;

/**
 * class that handles all bambi JDF/JMF requests - regardless of the servlet context previously part of {@link BambiServlet} it is implemented as a Singleton so that you always
 * have static access
 *
 * note that the get handling routines still assume a servlet context - only the actual JDF / JMF post does not
 *
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public final class BambiContainer extends ServletContainer
{
	private final static Log log = LogFactory.getLog(BambiContainer.class);

	/**
	 * use getCreateInstance from outside
	 */
	private BambiContainer()
	{
		super();
		rootDev = null;
		log.info("Creating Bambi Container");
		nLogGet = 0;
	}

	private AbstractDevice rootDev;
	private MultiDeviceProperties props;
	private static BambiContainer theInstance = null;

	/**
	 *
	 *
	 * @return the singleton bambi container instance
	 */
	public synchronized static BambiContainer getCreateInstance()
	{
		if (theInstance == null)
		{
			theInstance = new BambiContainer();
			log.info("created new Singleton bambi container");
		}

		return theInstance;
	}

	/**
	 *
	 *
	 * @return the singleton bambi container instance
	 */
	public synchronized static BambiContainer getInstance()
	{
		return theInstance;
	}

	/**
	 * handler for the overview page
	 *
	 * @author prosirai
	 *
	 */
	protected class OverviewHandler implements IGetHandler
	{
		/**
		 *
		 * @see org.cip4.bambi.core.IGetHandler#handleGet(org.cip4.bambi.core.ContainerRequest)
		 * @param request
		 * @return
		 */
		@Override
		public XMLResponse handleGet(final ContainerRequest request)
		{
			final String context = request.getContext();
			if (KElement.isWildCard(context) || context.equalsIgnoreCase("overview"))
			{
				if (request.getBooleanParam("UpdateDump"))
				{
					getRootDev().updateDump(request.getBooleanParam("Dump"));
				}
				return getRootDev().showDevice(request, false);
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * @return the root dispatcher device
	 */
	private RootDevice getRootDevice()
	{
		return (rootDev instanceof RootDevice) ? (RootDevice) rootDev : null;
	}

	/**
	 * get an {@link AbstractDevice} for a given deviceID
	 *
	 * @param deviceID
	 * @return
	 */
	public AbstractDevice getDeviceFromID(final String deviceID)
	{
		final RootDevice root = getRootDevice();
		return root == null ? rootDev : root.getDevice(deviceID);
	}

	/**
	 *
	 * @return
	 */
	public List<String> getDevices()
	{
		final List<String> result = new ArrayList<>();
		final RootDevice root = getRootDevice();
		final AbstractDevice[] devices = root == null ? null : root.getDeviceArray();
		if (devices != null)
		{
			for (final AbstractDevice device : devices)
			{
				result.add(device.getDeviceID());
			}
		}
		return result;
	}

	/**
	 * loads properties and instantiates the devices
	 *
	 * @param baseDir the initial application directory
	 * @param context the servlet context information
	 * @param config the name of the Java config xml file
	 * @param dump the file where to dump debug requests
	 * @return
	 */
	public boolean loadProperties(final File baseDir, final String context, final String dump)
	{
		props = MultiDeviceProperties.getProperties(baseDir, context);
		bWantDump = props.wantDump();
		return createDevices(props, dump);
	}

	/**
	 *
	 * @param prop
	 * @param needController
	 * @return
	 */
	AbstractDevice createDevice(final IDeviceProperties prop, final boolean needController)
	{
		AbstractDevice d = null;
		if (prop == null)
		{
			log.fatal("no properties specified for create device");
			return null;
		}
		if (rootDev == null)
		{
			if (needController)
			{
				log.info("creating root device " + prop.getDeviceID());
				d = prop.getDeviceInstance();
				if (d != null && !(d instanceof RootDevice))
				{
					log.info("Updating Root Device " + prop.getDeviceID());
					d.shutdown();
					d = new RootDevice(prop);
				}
			}
			else
			{
				log.info("creating non root device " + prop.getDeviceID());
				d = prop.getDeviceInstance();
			}
			rootDev = d;
		}
		else
		// we already have a root / dispatcher device - use it as base
		{
			log.info("adding non root device " + prop.getDeviceID());
			final RootDevice rd = getRootDevice();
			d = rd == null ? null : rd.createDevice(prop);
		}
		if (d != null && d.mustDie())
		{
			log.info("removing non root device " + prop.getDeviceID());
			d.shutdown();
			d = null;
		}
		if (d == null)
		{
			log.warn("could not create device, ID=" + prop.getDeviceID());
		}
		return d;
	}

	/**
	 * create devices based on the list of devices given in a file
	 *
	 * @param props
	 * @param dump the file where to dump debug requests
	 * @return true if successful, otherwise false
	 */
	public boolean createDevices(final MultiDeviceProperties props, final String dump)
	{
		boolean created = false;
		MessageSender.setBaseLocation(props.getBaseDir());
		final EnumVersion version = props.getJDFVersion();
		JDFElement.setDefaultJDFVersion(version);
		final VElement v = props.getDevices();
		if (v == null || v.size() == 0)
		{
			log.fatal("no devices found " + props);
		}
		else
		{
			final boolean needController = v.size() > 1;
			for (final KElement nextDevice : v)
			{
				final String devID = nextDevice.getAttribute("DeviceID");
				log.info("Creating Device " + devID);
				final IDeviceProperties prop = props.createDeviceProps(nextDevice);
				final AbstractDevice d = createDevice(prop, needController);
				created = created || d != null;
				if (d != null)
				{
					final String senderID = d.getDeviceID();
					if (dump != null)
					{
						final DumpDir dumpSendIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("inMessage." + senderID)));
						final DumpDir dumpSendOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("outMessage." + senderID)));
						MessageSender.addDumps(senderID, dumpSendIn, dumpSendOut);
						log.info("Created Device JMF dumps for senderID " + senderID);
					}
					else
					{
						log.info("Skipping Device JMF dumps for senderID " + senderID);
					}
				}
				else
				{
					log.warn("Not creating " + devID);
				}
			}
		}
		return created;
	}

	/**
	 * @return the rootDevice, which may be a generic device in case we have only one device
	 */
	public AbstractDevice getRootDev()
	{
		return rootDev;
	}

	/**
	 * @param rootDev the rootDev to set
	 */
	public void setRootDev(final AbstractDevice rootDev)
	{
		this.rootDev = rootDev;
	}

	/**
	 *
	 */
	@Override
	public void shutDown()
	{
		try
		{
			log.info("shutting down container: ");
			rootDev.shutdown();
		}
		catch (final Throwable x)
		{
			log.error("exception shutting down! ", x);
		}
		super.shutDown();
		if (this == theInstance)
		{
			log.info("removing singleton container instance ");
			removeInstance();
		}
	}

	/**
	 *
	 */
	static void removeInstance()
	{
		theInstance = null;
	}

	/**
	 *
	 */
	@Override
	public void reset()
	{
		log.info("resetting: " + toString());
		rootDev.reset();
		super.reset();
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected XMLResponse handleGet(final StreamRequest request)
	{
		if ((nLogGet++ < 10) || (nLogGet % 100 == 0))
		{
			log.info("Handling UI Get request# " + nLogGet + " " + request);
		}
		XMLResponse r = new OverviewHandler().handleGet(request);
		if (r == null)
		{
			r = getRootDev().getGetDispatchHandler().handleGet(request);
		}

		if (r == null)
		{
			final UnknownErrorHandler unknownErrorHandler = new UnknownErrorHandler();
			r = unknownErrorHandler.handleGet(request);
		}
		return r;
	}

	/**
	 * @param request
	 */
	@Override
	protected void startTimer(final ContainerRequest request)
	{
		final CPUTimer deviceTimer = getTimer(request);
		if (deviceTimer != null)
		{
			deviceTimer.start();
		}
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	CPUTimer getTimer(final ContainerRequest request)
	{
		final String deviceID = request == null ? null : request.getDeviceID();
		AbstractDevice dev = getDeviceFromID(deviceID);
		if (dev == null)
		{
			dev = getRootDev();
		}
		final CPUTimer deviceTimer = dev == null ? null : dev.getDeviceTimer(false);
		return deviceTimer;
	}

	/**
	 * @param request
	 */
	@Override
	protected void stopTimer(final ContainerRequest request)
	{
		final CPUTimer deviceTimer = getTimer(request);
		if (deviceTimer != null)
		{
			deviceTimer.stop();
		}

	}

	/**
	 * @param request
	 * @return
	 */
	@Override
	public XMLResponse processXMLDoc(final XMLRequest request)
	{
		log.info("Processing xml document: content type=" + request.getContentType(true));
		final XMLRequest newRequest = getRootDev().convertToJMF(request);
		if (newRequest != null)
		{
			final KElement e = newRequest.getXML();
			// jmf with incorrect mime type or something that the device could translate to jmf
			if (XJDFConstants.XJMF.equals(e.getLocalName()) || ElementName.JMF.equals(e.getLocalName()))
			{
				return processJMFDoc(newRequest);
			}
		}

		final KElement e = request.getXML();
		final String notification = "cannot process xml of type root = " + ((e == null) ? "null" : e.getLocalName()) + "; Content-Type: " + request.getContentType(false);
		return processError(request.getRequestURI(), EnumType.Notification, 3, notification);
	}

	/**
	 * process a multipart request - including job submission
	 *
	 * @param request
	 * @return the generated response
	 */
	@Override
	public XMLResponse processMultipleDocuments(final MimeRequest request)
	{
		startTimer(request);
		final XMLResponse r;
		final MimeReader reader = request.getReader();
		final BodyPart[] bp = reader == null ? null : reader.getBodyParts();
		if (bp == null || bp.length == 0)
		{
			r = processError(request.getRequestURI(), EnumType.Notification, 2, "processMultipleDocuments- not enough parts, bailing out");
		}
		else
		{
			r = processMultipleGood(request, bp);
		}
		stopTimer(request);

		return r;
	}

	/**
	 *
	 * @param request
	 * @param bp
	 * @return
	 */
	protected XMLResponse processMultipleGood(final MimeRequest request, final BodyPart[] bp)
	{
		final BodyPartHelper bph = new BodyPartHelper(bp[0]);
		final XMLResponse r;
		final JDFDoc[] docJDF;
		if (MimeUtil.isJSONType(bph.getContentType()))
		{
			docJDF = getJSONDocs(bp);
		}
		else
		{
			docJDF = MimeUtil.getJMFSubmission(bp[0].getParent());
		}
		if (docJDF == null || docJDF.length == 0)
		{
			r = processError(request.getRequestURI(), EnumType.Notification, 2, "proccessMultipleDocuments- no body parts, bailing out!");
		}
		else
		{
			final XMLRequest r2 = new XMLRequest(docJDF[0].getRoot());
			r2.setContainer(request);
			r = processXMLDoc(r2);
			request.setName(r2.getName());
		}
		if (MimeUtil.isJSONType(bph.getContentType()))
		{
			final NetResponse nr = new NetResponse(r, bph.getContentType());
			nr.setJSON(true);
			return nr;
		}
		return r;
	}

	JDFDoc[] getJSONDocs(final BodyPart[] bp)
	{
		if (bp == null || bp.length < 1)
		{
			return null;
		}
		final BodyPartHelper bodyPartHelper0 = new BodyPartHelper(bp[0]);
		final JDFDoc jmf = getDocFromJSONStream(bodyPartHelper0.getInputStream());
		final XJMFHelper h = XJMFHelper.getHelper(jmf);
		if (h == null)
		{
			return null;
		}
		jmf.setBodyPart(bp[0]);
		String subURL = h.getXPathValue("CommandSubmitQueueEntry/QueueSubmissionParams/@URL");
		if (subURL == null)
			subURL = h.getXPathValue("CommandResubmitQueueEntry/ResubmissionParams/@URL");
		if (subURL == null)
			subURL = h.getXPathValue("CommandReturnQueueEntry/ReturnQueueEntryParams/@URL");

		if (subURL == null)
		{
			log.warn("No URL in mime packaged - process raw");
			return new JDFDoc[] { jmf };
		}
		final MimeReader r = new MimeReader(bp[0].getParent());

		final BodyPartHelper bpJDF = r.getPartHelperByLocalName(subURL);
		final JDFDoc jdf = bpJDF == null ? null : getDocFromJSONStream(bpJDF.getInputStream());

		if (jdf == null)
		{
			return new JDFDoc[] { jmf };
		}
		else
		{
			jdf.setBodyPart(bpJDF.getBodyPart());
			return new JDFDoc[] { jmf, jdf };
		}
	}

	/**
	 * process the main, i.e. doc #0 JMF document
	 *
	 * @param request the http request to service
	 * @return
	 */
	@Override
	public XMLResponse processJMFDoc(final XMLRequest request)
	{
		startTimer(request);
		final KElement requestRoot = request.getXML();
		final XMLResponse response;
		if (requestRoot == null)
		{
			response = processError(request.getRequestURI(), EnumType.Notification, 3, "Error Parsing JMF");
		}
		else
		{
			request.ensureJDF();
			JDFDoc jmfDoc = (JDFDoc) request.getXMLDoc();
			final String deviceID = request.getDeviceID();
			final String requestURI = request.getLocalURL();
			final IConverterCallback _callBack = rootDev.getCallback(requestURI);

			if (_callBack != null)
			{
				jmfDoc = _callBack.prepareJMFForBambi(jmfDoc);
			}
			final JDFJMF jmf = jmfDoc.getJMFRoot();

			if (jmf == null)
			{
				response = processError(request.getRequestURI(), EnumType.Notification, 3, "Error processing JMF " + requestRoot.getLocalName());
			}
			else
			{
				// switch: sends the jmfDoc to correct device
				JDFDoc responseDoc = null;
				rootDev.updateFromRequest(jmf, request);
				final AbstractDevice device = getDeviceFromID(deviceID);
				final IJMFHandler handler = (device == null) ? rootDev.getJMFHandler(requestURI) : device.getJMFHandler(requestURI);
				if (handler != null)
				{
					responseDoc = handler.processJMF(jmfDoc);
				}
				else
				{
					log.warn("No handler for " + requestURI);
				}

				if (responseDoc != null)
				{
					response = processGoodJMF(_callBack, jmf, responseDoc);
				}
				else
				{
					response = processBadJMF(request, jmf);
				}
			}
		}
		stopTimer(request);
		final RootDevice rd = getRootDevice();
		if (rd != null)
			rd.postProcessJMF(request, response);
		return response;
	}

	protected XMLResponse processBadJMF(final XMLRequest request, final JDFJMF jmf)
	{
		final XMLResponse response;
		VElement v = jmf.getMessageVector(null, null);
		final int nMess = ContainerUtil.size(v);
		v = jmf.getMessageVector(EnumFamily.Signal, null);
		int nSigs = v.size();
		v = jmf.getMessageVector(EnumFamily.Acknowledge, null);
		nSigs += v.size();
		if (nMess > nSigs || nMess == 0)
		{
			response = processError(request.getRequestURI(), null, 1, "General Error Handling JMF");
		}
		else
		{
			response = new XMLResponse(null);
		}
		return response;
	}

	protected XMLResponse processGoodJMF(final IConverterCallback _callBack, final JDFJMF jmf, JDFDoc responseDoc)
	{
		final XMLResponse response;
		KElement jmfRoot = responseDoc.getJMFRoot();
		jmfRoot.copyAttribute(AttributeName.MAXVERSION, jmf);
		if (_callBack != null)
		{
			responseDoc = _callBack.updateJMFForExtern(responseDoc);
			if (responseDoc != null)
			{
				jmfRoot = responseDoc.getRoot();
			}
		}
		response = new XMLResponse(jmfRoot);
		response.setContentType(XJDFConstants.XJMF.equals(jmfRoot.getLocalName()) ? UrlUtil.VND_XJMF : UrlUtil.VND_JMF);
		return response;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	@Override
	public String toString()
	{
		return "BambiContainer: \n" + rootDev;
	}

	/**
	 * get the singleton props
	 *
	 * @return
	 */
	public MultiDeviceProperties getProps()
	{
		return props;
	}

	/**
	 *
	 * @param requestURI
	 * @return
	 */
	@Override
	protected IConverterCallback getCallback(final String requestURI)
	{
		return getRootDev().getCallback(requestURI);
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	@Override
	protected XMLRequest convertToJMF(final XMLRequest request)
	{
		return getRootDev().convertToJMF(request);
	}

	/**
	 * @see org.cip4.bambi.core.ServletContainer#processStream(org.cip4.bambi.core.StreamRequest)
	 */
	@Override
	public XMLResponse processStream(final StreamRequest request) throws IOException
	{
		final RootDevice rootDevice = getRootDevice();
		if (rootDevice != null)
		{
			if (rootDevice.isDisconnected())
			{
				return rootDevice.processDisconnect(request);
			}
			else
			{
				final XMLResponse resp = rootDevice.processStream(request);
				if (resp != null)
				{
					return resp;
				}
			}

		}
		return super.processStream(request);
	}

	/**
	 * @see org.cip4.bambi.core.ServletContainer#processRestStream(org.cip4.bambi.core.StreamRequest)
	 */
	@Override
	public HTTPResponse processRestStream(final StreamRequest sr) throws IOException
	{
		final RootDevice rootDevice = getRootDevice();
		if (rootDevice != null)
		{
			final HTTPResponse resp = rootDevice.processRestStream(sr);
			if (resp != null)
			{
				return resp;
			}
		}

		return processStream(sr);
	}

}
