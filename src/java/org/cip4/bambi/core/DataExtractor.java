/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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
import java.util.Vector;

import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.elementwalker.URLExtractor;
import org.cip4.jdflib.ifaces.IElementConverter;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.util.UrlUtil.URLProtocol;

/**
 * class to extract data from URLs
 * 
 * @author rainer prosi
 * @date Nov 24, 2010
 */
public class DataExtractor extends BambiLogFactory
{

	/**
	 * create a data extractor for a given device
	 * @param parentDevice the device to get directories from
	 * @param bSubmit 
	 */
	public DataExtractor(AbstractDevice parentDevice, boolean bSubmit)
	{
		super();
		this.parentDevice = parentDevice;
		protocols = new Vector<URLProtocol>();
		// don't do http 
		protocols.add(URLProtocol.cid);
		protocols.add(URLProtocol.file);
		this.bSubmit = bSubmit;
	}

	/**
	 * 
	 * add a protocol to those that are required
	 * @param protocol the protocol to add
	 */
	public void addProtocol(URLProtocol protocol)
	{
		protocols.add(protocol);
	}

	protected final AbstractDevice parentDevice;
	protected final Vector<URLProtocol> protocols;
	protected final boolean bSubmit;

	/**
	 * stub that copies url links to local storage if required
	 * 
	 * @param newQE the queueEntry that files are extracted from
	 * @param doc the JDF document to modify
	 */
	public void extractFiles(JDFQueueEntry newQE, JDFDoc doc)
	{
		if (doc == null || newQE == null)
		{
			if (doc == null)
				log.warn("cannot extract files for doc=null ; newQE" + newQE == null ? "null" : newQE.getQueueEntryID());
			if (newQE == null)
				log.warn("cannot extract files for newQE=null");
			return;
		}

		final File jobDirectory = parentDevice.getExtractDirectory(newQE, bSubmit);
		if (jobDirectory == null)
		{
			log.warn("no Job Directory for: " + newQE.getQueueEntryID());
			return;
		}

		log.info("extracting attached files to: " + jobDirectory);
		String dataURL = getDataURL(newQE);
		if (dataURL != null)
		{
			IElementConverter ex = getExtractor(jobDirectory, dataURL);
			ex.convert(doc.getRoot());
		}
	}

	protected String getDataURL(JDFQueueEntry newQE)
	{
		String dataURL = parentDevice.getDataURL(newQE, bSubmit);
		return dataURL;
	}

	protected IElementConverter getExtractor(final File jobDirectory, String dataURL)
	{
		File hfDirectory = parentDevice._submitHotFolder == null ? null : parentDevice._submitHotFolder.getHfDirectory();
		String absolutePath = hfDirectory == null ? null : hfDirectory.getAbsolutePath();
		URLExtractor ex = new URLExtractor(jobDirectory, absolutePath, dataURL);
		for (URLProtocol protocol : protocols)
			ex.addProtocol(protocol);
		return ex;
	}

}
