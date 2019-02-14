/*
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2019 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StreamUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdfutility.html.HTMLUtil;

/**
 * class to forward general requests for data from a non-tomcat enabled directory
 *
 * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class DataRequestHandler extends BambiLogFactory implements IGetHandler
{
	protected final AbstractDevice dev;
	protected final String dataToken;

	/**
	 * @param dev the parent device
	 * @param dataToken the token in the path to check for data
	 *
	 */
	public DataRequestHandler(final AbstractDevice dev, final String dataToken)
	{
		super();
		this.dev = dev;
		this.dataToken = "/" + StringUtil.token(dataToken, 0, "/") + "/";
	}

	/**
	 * @return the data requested
	 * @param request the get request to handle
	 */
	@Override
	public XMLResponse handleGet(final ContainerRequest request)
	{
		final String path = request.getRequestURI();
		final File dataDir = getDataDir(request);
		if (dataDir == null)
		{
			log.warn("cannot find data directory for: " + path);
			return null;
		}
		final String last = getRelativePath(path);
		if (last == null)
		{
			log.warn("cannot retrieve data for: " + path);
			return null;
		}

		log.info("serving file data for: " + path);
		File file = FileUtil.getFileInDirectory(dataDir, new File(last));
		if (!file.canRead())
		{
			file = FileUtil.getFileInDirectory(dataDir, UrlUtil.urlToFile(last));
		}

		final XMLResponse response = new XMLResponse(null);

		if (file.canRead())
		{
			try
			{
				final OutputStream outputStream = response.getOutputStream();
				if (file.isDirectory())
				{
					final KElement root = HTMLUtil.createHTMLRoot();
					HTMLUtil.appendHeader(root, 1, "Directory listing is not supported ");
					root.write2Stream(outputStream);
					StreamUtil.close(outputStream);
				}
				else
				{
					log.info("serving file at path= " + path + " from " + file.getAbsolutePath());
					IOUtils.copyLarge(FileUtil.getBufferedInputStream(file), outputStream);
				}
			}
			catch (final IOException x)
			{
				log.error("error serving file data for: " + path + " at: " + file);
			}
		}
		else
		{
			log.warn("cannot find file data for: " + path + " at: " + file);
		}
		return response;
	}

	protected String getRelativePath(final String path)
	{
		final int posData = path.indexOf(dataToken);
		String last = path.substring(posData + dataToken.length() + 2);
		final int posToken = last == null ? -1 : last.indexOf("/");
		if (posToken > 0 && last != null)
		{
			last = last.substring(posToken);
		}
		if (posData < 0)
		{
			last = null;
		}
		return last;
	}

	/**
	 *
	 * get the data directory based on the request
	 * 
	 * @param request
	 * @return
	 */
	public File getDataDir(final ContainerRequest request)
	{
		final File dataDir = dev.getExtractDirectory(null, true);
		return dataDir;
	}
}
