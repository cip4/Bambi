/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import org.cip4.bambi.core.messaging.IJMFHandler;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.MessageSender;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * class that handles all bambi JDF/JMF requests - regardless of the servlet context
 * previously part of {@link BambiServlet}
 * 
 * note that the get handling routines still assume a servlet context - only the actual JDF / JMF post does not
  * @author Rainer Prosi, Heidelberger Druckmaschinen 
 */
public class BambiContainer extends BambiLogFactory
{
	private AbstractDevice rootDev = null;

	/**
	 * @return the root dispatcher device 
	 */
	RootDevice getRootDevice()
	{
		return (rootDev instanceof RootDevice) ? (RootDevice) rootDev : null;
	}

	/**
	 * get an {@link AbstractDevice} for a given deviceID
	 * @param deviceID
	 * @return
	 */
	AbstractDevice getDeviceFromID(final String deviceID)
	{
		final RootDevice root = getRootDevice();
		final AbstractDevice dev = root == null ? rootDev : root.getDevice(deviceID);
		if (dev == null)
		{
			log.info("invalid request: device with id=" + deviceID == null ? "null" : deviceID + " not found");
			return null;
		}
		return dev;
	}

	/**
	 * loads properties and instantiate the devices
	 * @param baseDir 
	 * @param context the servlet context information
	 * @param config the name of the Java config xml file
	 * @param dump the file where to dump debug requests
	 * @param propName 
	 * @return 
	 */
	boolean loadProperties(final File baseDir, final String context, final File config, final String dump, String propName)
	{
		final MultiDeviceProperties props;
		try
		{
			final Class<?> c = Class.forName(propName);
			final Constructor<?> con = c.getConstructor(new Class[] { File.class, String.class, File.class });
			props = (MultiDeviceProperties) con.newInstance(new Object[] { baseDir, context, config });
		}
		catch (final Exception x)
		{
			log.fatal("Cannot instantiate Device properties: " + propName, x);
			return false;
		}
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
		if (rootDev == null)
		{
			if (needController)
			{
				d = prop.getDeviceInstance();
				if (!(d instanceof RootDevice))
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
		if (d.mustDie())
		{
			d.shutdown();
			d = null;
		}

		return d;
	}

	/**
	 * create the specified directories, if the do not exist
	 * @param dirs the directories to create
	 */
	private void createDirs(final Vector<File> dirs)
	{
		for (int i = 0; i < dirs.size(); i++)
		{
			final File f = dirs.get(i);
			if (f != null && !f.exists())
			{
				if (!f.mkdirs())
				{
					log.error("failed to create directory " + f);
				}
			}
		}
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
		MessageSender.setBaseLocation(props.getJMFDir());
		final Vector<File> dirs = new Vector<File>();
		dirs.add(props.getBaseDir());
		dirs.add(props.getJDFDir());
		createDirs(dirs);
		final EnumVersion version = props.getJDFVersion();
		JDFElement.setDefaultJDFVersion(version);
		final VElement v = props.getDevices();
		final Iterator<KElement> iter = v.iterator();
		final boolean needController = v.size() > 1;
		while (iter.hasNext())
		{
			final KElement next = iter.next();
			log.info("Creating Device " + next.getAttribute("DeviceID"));
			final IDeviceProperties prop = props.createDeviceProps(next);
			final AbstractDevice d = createDevice(prop, needController);
			created = created || d != null;
			if (d != null && dump != null)
			{
				final String senderID = d.getDeviceID();
				final DumpDir dumpSendIn = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("inMessage." + senderID)));
				final DumpDir dumpSendOut = new DumpDir(FileUtil.getFileInDirectory(new File(dump), new File("outMessage." + senderID)));
				MessageSender.addDumps(senderID, dumpSendIn, dumpSendOut);
			}
		}
		return created;
	}

	/**
	 * @return the rootDev
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
	 * @param requestURI
	 * @param messageType
	 * @param returnCode
	 * @param notification
	 * @return 
	 */
	public XMLResponse processError(final String requestURI, final EnumType messageType, final int returnCode, final String notification)
	{
		log.warn("processError- rc: " + returnCode + " " + notification == null ? "" : notification);
		final JDFJMF error = JDFJMF.createJMF(EnumFamily.Response, messageType);
		final JDFResponse r = error.getResponse(0);
		r.setReturnCode(returnCode);
		r.setErrorText(notification, null);
		final IConverterCallback _callBack = getRootDev().getCallback(requestURI);
		if (_callBack != null)
		{
			_callBack.updateJMFForExtern(error.getOwnerDocument_JDFElement());
		}
		final XMLResponse response = new XMLResponse(error);
		response.setContentType(UrlUtil.VND_JMF);
		return response;
	}

	/**
	 * 
	 */
	public void shutDown()
	{
		rootDev.shutdown();
		final JMFFactory factory = JMFFactory.getJMFFactory();
		factory.shutDown(null, true);
		ThreadUtil.sleep(5234); // leave some time for cleanup
		factory.shutDown(null, false);

	}

	/**
	 * @param request 
	 * @return 
	 * @throws IOException 
	 */
	public XMLResponse processStream(final StreamRequest request) throws IOException
	{

		final String contentType = request.getContentType();
		if (UrlUtil.VND_JMF.equals(contentType))
		{
			XMLRequest req = new XMLRequest(request);
			return processJMFDoc(req);
		}
		else if (UrlUtil.TEXT_XML.equals(contentType))
		{
			XMLRequest req = new XMLRequest(request);
			return processXMLDoc(req);
		}
		else
		{
			final boolean isMultipart = MimeUtil.isMimeMultiPart(contentType);
			if (isMultipart)
			{
				log.info("Processing multipart request... (ContentType: " + contentType + ")");
				return processMultiPart(request);
			}
			else
			{
				String ctWarn = "Unknown HTTP ContentType: " + contentType;
				log.error(ctWarn);
				ctWarn += "\nFor JMF , please use: " + UrlUtil.VND_JMF;
				ctWarn += "\nFor JDF , please use: " + UrlUtil.VND_JDF;
				ctWarn += "\nFor MIME, please use: " + MimeUtil.MULTIPART_RELATED;
				ctWarn += "\n\n Input Message:\n\n";
				return processError(request.getRequestURI(), null, 9, ctWarn);
			}
		}
	}

	/**
	 * @param request
	 * @return
	 */
	private XMLResponse processXMLDoc(XMLRequest request)
	{
		log.info("Processing text/xml");
		return processJMFDoc(request);
	}

	/**
	 * Parses a multipart request.
	 * @param request 
	 * @return 
	 * @throws IOException 
	 */
	public XMLResponse processMultiPart(final StreamRequest request) throws IOException
	{
		final InputStream inStream = request.getStream();
		final BodyPart bp[] = MimeUtil.extractMultipartMime(inStream);
		log.info("Body Parts: " + ((bp == null) ? 0 : bp.length));
		XMLResponse r = null;
		if (bp == null || bp.length == 0)
		{
			r = processError(request.getRequestURI(), null, 9, "No body parts in mime package");
		}
		else
		{
			try
			{// messaging exceptions
				if (bp.length > 1)
				{
					MimeRequest req = new MimeRequest(bp);
					req.setRequestURI(request.getRequestURI());
					r = processMultipleDocuments(req);
				}
				else
				{
					final String s = bp[0].getContentType();
					if (UrlUtil.VND_JMF.equalsIgnoreCase(s))
					{
						StreamRequest sr = new StreamRequest(bp[0].getInputStream());
						sr.setContainer(request);
						r = processStream(sr);
					}
				}
			}
			catch (final MessagingException x)
			{
				r = processError(request.getRequestURI(), null, 9, "Messaging exception\n" + x.getLocalizedMessage());
			}
		}
		return r;
	}

	/**
	 * process a multipart request - including job submission
	 * @param request
	 * @return the generated response
	 */
	public XMLResponse processMultipleDocuments(final MimeRequest request)
	{
		BodyPart[] bp = request.getBodyParts();
		log.info("processMultipleDocuments- parts: " + (bp == null ? 0 : bp.length));
		if (bp == null || bp.length < 2)
		{
			return processError(request.getRequestURI(), EnumType.Notification, 2, "processMultipleDocuments- not enough parts, bailing out");
		}
		final JDFDoc docJDF[] = MimeUtil.getJMFSubmission(bp[0].getParent());
		if (docJDF == null || docJDF.length == 0)
		{
			return processError(request.getRequestURI(), EnumType.Notification, 2, "proccessMultipleDocuments- incorrect jmf/jdf parts, bailing out!");
		}
		else if (docJDF.length == 1)
		{
			final JDFMessage messageElement = docJDF[0].getJMFRoot().getMessageElement(null, null, 0);
			EnumType typ = messageElement == null ? EnumType.Notification : messageElement.getEnumType();
			if (typ == null)
			{
				typ = EnumType.Notification;
			}
			return processError(request.getRequestURI(), typ, 2, "proccessMultipleDocuments- incorrect jmf/jdf parts, bailing out!");

		}
		XMLRequest r2 = new XMLRequest(docJDF[0].getJMFRoot());
		r2.setRequestURI(request.getRequestURI());
		// callbacks must be handled individually
		return processJMFDoc(r2);
	}

	/**
	 * process the main, i.e. doc #0 JMF document
	 * 
	 * @param request the http request to service
	 * @return
	 */
	public XMLResponse processJMFDoc(final XMLRequest request)
	{
		JDFJMF jmf = (JDFJMF) request.getXML();
		JDFDoc jmfDoc = new JDFDoc(request.getXML().getOwnerDocument());
		final XMLResponse response;
		if (jmf == null)
		{
			response = processError(request.getRequestURI(), null, 3, "Error Parsing JMF");
		}
		else
		{
			final String deviceID = request.getDeviceID();
			String requestURI = request.getRequestURI();
			final IConverterCallback _callBack = getRootDev().getCallback(requestURI);

			if (_callBack != null)
			{
				_callBack.prepareJMFForBambi(jmfDoc);
			}

			// switch: sends the jmfDoc to correct device
			JDFDoc responseJMF = null;
			final AbstractDevice device = getDeviceFromID(deviceID);
			final IJMFHandler handler = (device == null) ? rootDev.getHandler() : device.getHandler();
			if (handler != null)
			{
				responseJMF = handler.processJMF(jmfDoc);
			}

			if (responseJMF != null)
			{
				response = new XMLResponse(responseJMF.getJMFRoot());
				response.setContentType(UrlUtil.VND_JMF);
				if (_callBack != null)
				{
					_callBack.updateJMFForExtern(responseJMF);
				}

			}
			else
			{
				if (jmf != null)
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
				else
				{
					response = processError(request.getRequestURI(), null, 3, "Error Parsing JMF");
				}
			}
		}
		return response;
	}

}
