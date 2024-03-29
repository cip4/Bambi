/**
 * The CIP4 Software License, Version 1.0
 * <p>
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights
 * reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * <p>
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress (www.cip4.org)"
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 * <p>
 * 4. The names "CIP4" and "The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress" must
 * not be used to endorse or promote products derived from this
 * software without prior written permission. For written
 * permission, please contact info@cip4.org.
 * <p>
 * 5. Products derived from this software may not be called "CIP4",
 * nor may "CIP4" appear in their name, without prior written
 * permission of the CIP4 organization
 * <p>
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 * <p>
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
 * <p>
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG
 * copyright (c) 1999-2001, Agfa-Gevaert N.V.
 * <p>
 * For more information on The International Cooperation for the
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 */
package org.cip4.bambi.server;

import org.cip4.jdflib.util.file.UserDir;
import org.cip4.jdfutility.logging.LogConfigurator;
import org.cip4.jdfutility.server.JettyServer;
import org.cip4.jdfutility.server.JettyService;

/**
 * Standard bambi windows service wrapper
 * 
 * @author rainer prosi
 * @date Oct 26, 2011
 */
public class BambiService extends JettyService
{
	/**
	 * Default constructor.
	 */
	public BambiService()
	{
		super();
		BambiFrame.retrieveVersion();
		log.info("Creating bambi service instance.");
	}

	/**
	 * The applications main entrance point.
	 * 
	 * @param args Command line arguments as string array.
	 */
	public static void main(final String[] args)
	{

		LogConfigurator.configureLog(new UserDir("bambi").getLogPath(), "bambi.log");
		if (theService == null)
			theService = new BambiService();
		theService.doMain(args);
	}

	/**
	 * @see org.cip4.jdfutility.server.JettyService#getServer(java.lang.String[])
	 */
	@Override
	public JettyServer getServer(final String[] args)
	{
		return BambiServer.getBambiServer();
	}
}
