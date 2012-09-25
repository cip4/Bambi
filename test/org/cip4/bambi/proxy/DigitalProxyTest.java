/**
 * The CIP4 Software License, Version 1.0
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
package org.cip4.bambi.proxy;

import java.net.HttpURLConnection;

import org.cip4.bambi.server.BambiServer;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.goldenticket.IDPGoldenTicket;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.JDFNode.EnumType;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * TODO Please insert comment!
 * @author rainerprosi
 * @date Nov 7, 2011
 */
public class DigitalProxyTest extends ProxyTest
{
	@Override
	protected void startContainer()
	{
		//nop
	}

	/**
	 * 
	 * TODO Please insert comment!
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		new BambiServer().runServer();
	}

	/**
	 *
	 */
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		workerURLBase = "http://localhost:44484/BambiProxy/jmf/";
		deviceID = "EFI";
		deviceID = "HP";
	}

	/**
	 * 
	 */
	@Override
	protected void createIDPGT()
	{
		super.createIDPGT();
		_theGT.m_pdfFile = "file:/data/hd-city-08.pdf";
	}

	/**
	 * @return
	 */
	@Override
	protected enumGTType getGTType()
	{
		return enumGTType.IDP;
	}

	/**
	 * @return
	 */
	private IDPGoldenTicket initDigitalPrinting()
	{

		// BaseGoldenTicket.misURL = "http://192.168.14.112:8080/digitalpressproxy/jmf/HPIndigoProductionManagerGate";
		// BaseGoldenTicket.misURL = "http://192.168.14.143:8010/FJC/Fiery";
		JDFAudit.setStaticAgentName("JDF IDP golden ticket generator");
		final IDPGoldenTicket idpGoldenTicket = (IDPGoldenTicket) _theGT;

		idpGoldenTicket.good = 10;
		idpGoldenTicket.waste = 0;
		idpGoldenTicket.assign(null);
		return idpGoldenTicket;

	}

	/**
	 * @throws Exception
	 */
	public void testSimpleDigiNode() throws Exception
	{
		for (int ii = 0; ii < 1; ii++)
		{
			final IDPGoldenTicket goldenTicket = initDigitalPrinting();
			goldenTicket.makeReadyAll();
			final JDFNode node = goldenTicket.getNode();
			node.setType(EnumType.Combined);
			String workerURL = getWorkerURL();
			//			workerURL = "http://192.168.1.107:8080/dpp/jmf/dfe";
			HttpURLConnection p = submitMimetoURL(workerURL);
			assertEquals(p.getResponseCode(), 200);
			ThreadUtil.sleep(1234);
			System.out.println("sub: " + ii);
		}
	}
}
