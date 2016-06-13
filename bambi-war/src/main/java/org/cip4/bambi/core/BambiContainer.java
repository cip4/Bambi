/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2016 The International Cooperation for the Integration of 
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.extensions.XJDFConstants;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.MimeReader;

/**
 * class that handles all bambi JDF/JMF requests - regardless of the servlet context
 * previously part of {@link BambiServlet}
 * it is implemented as a Singleton so that you always have static access
 * 
 * note that the get handling routines still assume a servlet context - only the actual JDF / JMF post does not
 * @author Rainer Prosi, Heidelberger Druckmaschinen 
 */
public final class BambiContainer extends ServletContainer
{
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
			theInstance.log.info("created new Singleton bambi container");
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
	 * @param deviceID
	 * @return
	 */
	public AbstractDevice getDeviceFromID(final String deviceID)
	{
		final RootDevice root = getRootDevice();
		final AbstractDevice dev = root == null ? rootDev : root.getDevice(deviceID);
		if (dev == null)
		{
			log.warn("invalid request: device with id=" + (deviceID == null ? "null" : deviceID + " not found"));
			return null;
		}
		return dev;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getDevices()
	{
		List<String> result = new ArrayList<String>();
		final RootDevice root = getRootDevice();
		AbstractDevice[] devices = root.getDeviceArray();

		for (AbstractDevice device : devices)
		{
			result.add(device.getDeviceID());
		}

		return result;
	}

	/**
	 * loads properties and instantiates the devices
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
				d = prop.getDeviceInstance();
			}
			rootDev = d;
		}
		else
		// we already have a root / dispatcher device - use it as base
		{
			final RootDevice rd = getRootDevice();
			d = rd.createDevice(prop);
		}
		if (d != null && d.mustDie())
		{
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
			for (KElement nextDevice : v)
			{
				log.info("Creating Device " + nextDevice.getAttribute("DeviceID"));
				final IDeviceProperties prop = props.createDeviceProps(nextDevice);
				final AbstractDevice d = createDevice(prop, needController);
				created = created || d != null;
				if (d != null && dump != null)
				{
					final String senderID = d.getDeviceID();
					final DumpDir dumpSendIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("inMessage." + senderID)));
					final DumpDir dumpSendOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("outMessage." + senderID)));
					MessageSender.addDumps(senderID, dumpSendIn, dumpSendOut);
					log.info("Created Device JMF dumps for senderID " + senderID);
				}
				else if (d != null)
				{
					final String senderID = d.getDeviceID();
					log.info("Skipping Device JMF dumps for senderID " + senderID);
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
	public void setRootDev(AbstractDevice rootDev)
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
		catch (Throwable x)
		{
			log.error("exception shutting down! ", x);
		}
		super.shutDown();
		if (this == theInstance)
		{
			log.info("removing singleton container instance ");
			theInstance = null;
		}
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
	protected void startTimer(ContainerRequest request)
	{
		CPUTimer deviceTimer = getTimer(request);
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
	CPUTimer getTimer(ContainerRequest request)
	{
		String deviceID = request == null ? null : request.getDeviceID();
		AbstractDevice dev = getDeviceFromID(deviceID);
		if (dev == null)
		{
			dev = getRootDev();
		}
		CPUTimer deviceTimer = dev == null ? null : dev.getDeviceTimer(false);
		return deviceTimer;
	}

	/**
	 * @param request
	 */
	@Override
	protected void stopTimer(ContainerRequest request)
	{
		CPUTimer deviceTimer = getTimer(request);
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
	public XMLResponse processXMLDoc(XMLRequest request)
	{
		log.info("Processing xml document: content type=" + request.getContentType(true));
		XMLRequest newRequest = getRootDev().convertToJMF(request);
		if (newRequest != null)
		{
			KElement e = newRequest.getXML();
			// jmf with incorrect mime type or something that the device could translate to jmf
			if (e instanceof JDFJMF || XJDFConstants.XJMF.equals(e.getLocalName()))
			{
				return processJMFDoc(newRequest);
			}
		}

		KElement e = request.getXML();
		String notification = "cannot process xml of type root = " + ((e == null) ? "null" : e.getLocalName()) + "; Content-Type: " + request.getContentType(false);
		return processError(request.getRequestURI(), EnumType.Notification, 3, notification);
	}

	/**
	 * Parses a multipart request.
	 * @param request 
	 * @return 
	 * @throws IOException 
	 */
	@Override
	public XMLResponse processMultiPart(final StreamRequest request) throws IOException
	{
		startTimer(request);
		final InputStream inStream = request.getInputStream();
		final MimeReader mr = new MimeReader(inStream);
		final BodyPart bp[] = mr.getBodyParts();
		log.info("Body Parts: " + ((bp == null) ? 0 : bp.length));
		XMLResponse r = null;
		if (bp == null || bp.length == 0)
		{
			r = processError(request.getRequestURI(), EnumType.Notification, 9, "No body parts in mime package");
		}
		else
		{
			try
			{// messaging exceptions
				if (bp.length > 1)
				{
					MimeRequest req = new MimeRequest(mr);
					req.setContainer(request);
					r = processMultipleDocuments(req);
				}
				else
				// unpack the only body part and throw it at the processor again
				{
					StreamRequest sr = new StreamRequest(bp[0].getInputStream());
					sr.setContainer(request);
					r = processStream(sr);
				}
			}
			catch (final MessagingException x)
			{
				r = processError(request.getRequestURI(), null, 9, "Messaging exception\n" + x.getLocalizedMessage());
			}
		}
		stopTimer(request);
		return r;
	}

	/**
	 * process a multipart request - including job submission
	 * @param request
	 * @return the generated response
	 */
	@Override
	public XMLResponse processMultipleDocuments(final MimeRequest request)
	{
		startTimer(request);
		final XMLResponse r;
		MimeReader reader = request.getReader();
		BodyPart[] bp = reader == null ? null : reader.getBodyParts();
		log.info("processMultipleDocuments- parts: " + (bp == null ? 0 : bp.length));
		if (bp == null || bp.length == 0)
		{
			r = processError(request.getRequestURI(), EnumType.Notification, 2, "processMultipleDocuments- not enough parts, bailing out");
		}
		else
		{
			final JDFDoc docJDF[] = MimeUtil.getJMFSubmission(bp[0].getParent());
			if (docJDF == null || docJDF.length == 0)
			{
				r = processError(request.getRequestURI(), EnumType.Notification, 2, "proccessMultipleDocuments- no body parts, bailing out!");
			}
			else
			{
				XMLRequest r2 = new XMLRequest(docJDF[0].getJMFRoot());
				r2.setContainer(request);
				r = processXMLDoc(r2);
				request.setName(r2.getName());
			}
		}
		stopTimer(request);

		return r;
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
		JDFElement requestRoot = (JDFElement) request.getXML();
		final XMLResponse response;
		if (requestRoot == null)
		{
			response = processError(request.getRequestURI(), EnumType.Notification, 3, "Error Parsing JMF");
		}
		else
		{
			JDFDoc jmfDoc = requestRoot.getOwnerDocument_JDFElement();
			final String deviceID = request.getDeviceID();
			String requestURI = request.getLocalURL();
			final IConverterCallback _callBack = getRootDev().getCallback(requestURI);

			if (_callBack != null)
			{
				jmfDoc = _callBack.prepareJMFForBambi(jmfDoc);
			}
			JDFJMF jmf = jmfDoc.getJMFRoot();

			if (jmf == null)
			{
				response = processError(request.getRequestURI(), EnumType.Notification, 3, "Error processing JMF " + requestRoot.getLocalName());
			}
			else
			{
				// switch: sends the jmfDoc to correct device
				JDFDoc responseJMF = null;
				final AbstractDevice device = getDeviceFromID(deviceID);
				final IJMFHandler handler = (device == null) ? rootDev.getJMFHandler(requestURI) : device.getJMFHandler(requestURI);
				if (handler != null)
				{
					responseJMF = handler.processJMF(jmfDoc);
				}

				if (responseJMF != null)
				{
					KElement jmfRoot = responseJMF.getJMFRoot();
					if (_callBack != null)
					{
						responseJMF = _callBack.updateJMFForExtern(responseJMF);
						if (responseJMF != null)
						{
							jmfRoot = responseJMF.getRoot();
						}
					}
					response = new XMLResponse(jmfRoot);
					response.setContentType(UrlUtil.VND_JMF);
				}
				else
				{

					VElement v = jmf.getMessageVector(null, null);
					final int nMess = v == null ? 0 : v.size();
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
						response = null;
					}
				}
			}
		}
		stopTimer(request);
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
	protected IConverterCallback getCallback(String requestURI)
	{
		return getRootDev().getCallback(requestURI);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected XMLRequest convertToJMF(XMLRequest request)
	{
		return getRootDev().convertToJMF(request);
	}

}
