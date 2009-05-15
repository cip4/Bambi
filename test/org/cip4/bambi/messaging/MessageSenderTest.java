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

package org.cip4.bambi.messaging;

import java.io.IOException;
import java.net.URL;

import javax.mail.Multipart;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 * 
 */
public class MessageSenderTest extends BambiTestCase
{

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 * @throws Exception
	 */
	String snafu = "http://www.foobar.snafu/next";

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		simWorkerUrl = "http://kie-prosirai-lg:8080/speedmaster/jmf/MAN75";
	}

	/**
	 * @throws Exception
	 */
	public void testSerialize() throws Exception
	{
		final JMFFactory factory = JMFFactory.getJMFFactory();
		final JDFJMF jmf = factory.buildStatusSubscription(snafu, 1, 0, null);
		final JDFResponse resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertNotNull(resp);
		submitToQueue(new URL(simWorkerUrl));
		ThreadUtil.sleep(10);
	}

	protected void submitToQueue(final URL qurl) throws Exception
	{
		final JDFJMF jmf = JDFJMF.createJMF(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);
		final JDFCommand com = (JDFCommand) jmf.getCreateMessageElement(JDFMessage.EnumFamily.Command, null, 0);
		final JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();
		qsp.setURL("dummy"); // replaced by mimeutil

		qsp.setReturnJMF(snafu);
		final JDFNode nod = new JDFParser().parseString("<JDF Type=\"ConventionalPrinting\" ID=\"a1\"/>").getJDFRoot();
		final String urlString = qurl == null ? null : qurl.toExternalForm();

		writeToQueue(jmf.getOwnerDocument_JDFElement(), nod.getOwnerDocument_JDFElement(), urlString);

	}

	protected void writeToQueue(final JDFDoc docJMF, final JDFDoc docJDF, final String strUrl) throws IOException
	{
		final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, false);
		JMFFactory.getJMFFactory().send2URL(mp, strUrl, null, null, null, "did");
	}

}
