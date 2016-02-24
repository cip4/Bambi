/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2015 The International Cooperation for the Integration of 
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

package org.cip4.bambi;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.apache.log4j.BasicConfigurator;
import org.cip4.bambi.core.BambiContainer;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumValidationLevel;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.goldenticket.BaseGoldenTicket;
import org.cip4.jdflib.goldenticket.IDPGoldenTicket;
import org.cip4.jdflib.goldenticket.MISCPGoldenTicket;
import org.cip4.jdflib.goldenticket.MISFinGoldenTicket;
import org.cip4.jdflib.goldenticket.MISPreGoldenTicket;
import org.cip4.jdflib.goldenticket.PackagingGoldenTicket;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.UrlUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 * 
 * abstract test case for all bambi tests note that this has some site specific details that must be modified
 */
public class BambiTestCase extends BambiGoldenTicketTest
{
    @Rule
    public Timeout globalTimeout = new Timeout(60000);

	@Override
    @Before
    public void setUp() throws Exception
	{
		super.setUp();
		BaseGoldenTicket.setMisURL("http://localhost:8080/httpdump/BambiTest");
		gt = getGTType();
		if (enumGTType.MISCP.equals(gt))
		{
			createMISCPGT();
		}
		else if (enumGTType.MISPRE.equals(gt))
		{
			createMISPreGT();
		}
		else if (enumGTType.IDP.equals(gt))
		{
			createIDPGT();
		}
		else if (enumGTType.MISFIN_STITCH.equals(gt))
		{
			createFinStitchGT();
		}
		else if (enumGTType.PACKAGING_LAYOUT.equals(gt))
		{
			createPackLayoutGT();
		}
		else if (enumGTType.PACKAGING_CAD.equals(gt))
		{
			createPackCADGT();
		}
		else if (enumGTType.PACKAGING_SHAPEDEF.equals(gt))
		{
			createPackShapeDefGT();
		}
		_theGT.devID = getDeviceID();
		_theGT.assign(null);
		postAssign();
	}

	/**
	 * @return
	 */
	protected String getDeviceID()
	{
		return deviceID;
	}

	/**
	 * 
	 */
	protected void postAssign()
	{
		// nop - hook for post assign cleanup

	}

	/**
	 * 
	 */
	protected void createPackShapeDefGT()
	{
		_theGT = new PackagingGoldenTicket(1, null, 1, 2, null);
		_theGT.addSheet("Sheet1");
	}

	/**
	 * 
	 */
	protected void createPackLayoutGT()
	{
		_theGT = new PackagingGoldenTicket(1, null, 1, 2, null);
		_theGT.addSheet("Sheet1");
	}

	/**
	 * 
	 */
	protected void createPackCADGT()
	{
		_theGT = new PackagingGoldenTicket(1, null, 1, 2, null);
		_theGT.addSheet("Sheet1");
	}

	/**
	 * 
	 */
	protected void createFinStitchGT()
	{
		_theGT = new MISFinGoldenTicket(1, null, 1, 2, null);
		_theGT.addSheet("Sheet1");
		_theGT.addSheet("Sheet2");
		((MISFinGoldenTicket) _theGT).setCategory(MISFinGoldenTicket.MISFIN_STITCHFIN);
	}

	/**
	 * 
	 */
	protected void createIDPGT()
	{
		final IDPGoldenTicket idpGoldenTicket = new IDPGoldenTicket(1);
		idpGoldenTicket.m_pdfFile = sm_dirTestData + "url1.pdf";
		idpGoldenTicket.good = 100;
		idpGoldenTicket.waste = 0;
		_theGT = idpGoldenTicket;
	}

	/**
	 * 
	 */
	protected void createMISPreGT()
	{
		_theGT = new MISPreGoldenTicket(2, EnumVersion.Version_1_5, 1, 2, null);
	}

	/**
	 * 
	 */
	protected void createMISCPGT()
	{
		_theGT = new MISCPGoldenTicket(1, null, 1, 2, true, null);
		_theGT.addSheet("Sheet1");
		_theGT.nCols = new int[] { 4, 4 };
	}

	/**
	 * @return
	 */
	protected enumGTType getGTType()
	{
		return gt == null ? enumGTType.MISCP : gt;
	}

	protected enum enumGTType
	{
		MISCP, MISPRE, IDP, MISFIN_STITCH, MISFIN_FOLD, PACKAGING_LAYOUT, PACKAGING_SHAPEDEF, PACKAGING_CAD
	}

	protected enumGTType gt = null;

	protected final static String sm_UrlTestData = "File:test/data/";
	protected final String sm_dirContainer = sm_dirTestDataTemp + "ContainerTest/";

	// protected static String simWorkerUrl = "http://localhost:8080/potato/jmf/GreatPotato";
	protected String returnJMF = "http://localhost:8080/httpdump/returnJMF";
	protected String subscriptionURL = "http://localhost:8080/httpdump/testSubscriptions";
	protected String returnURL = null;// "http://localhost:8080/httpdump/returnURL";
	protected String acknowledgeURL = null;// "http://localhost:8080/httpdump/returnURL";

	protected boolean bUpdateJobID = false;
	protected int chunkSize = -1;
	protected String transferEncoding = UrlUtil.BASE64;

	protected BaseGoldenTicket _theGT = null;
	protected BambiContainer bambiContainer = null;
	protected String deviceID = "device";
	protected String workerURLBase = "http://localhost:44482/SimWorker/jmf/";

	protected boolean wantContainer;

	/**
	 * banbi test case
	 */
	public BambiTestCase()
	{
		BasicConfigurator.configure();
		JDFJMF.setTheSenderID("BambiTest");
		wantContainer = false;
	}

	/**
	 * @return
	 * 
	 */
	protected String getWorkerURL()
	{
		return UrlUtil.getURLWithDirectory(getWorkerURLBase(), getDeviceID());
	}

	protected String getWorkerURLBase()
	{
		return workerURLBase;
	}

	/**
	 * @return
	 * 
	 */
	protected String getDumpURL()
	{
		return "http://localhost:8080/httpdump/BambiDevice/" + deviceID;
	}

	/**
	 * cleaning up, brute-force-sytle: send a AbortQueueEntry and a RemoveQueueEntry message to every QueueEntry in the Queue
	 * @param url the URL of the device to send the command to
	 */
	protected void abortRemoveAll(final String url)
	{
		final JMFFactory factory = JMFFactory.getJMFFactory();
		JDFJMF jmf = new JMFBuilder().buildQueueStatus();
		final JDFResponse resp = factory.send2URLSynchResp(jmf, url, null, "testcase", 2000);
		if (resp == null)
		{
			System.err.println("failed to send QueueStatus");
			return;
		}
		final JDFQueue theQueue = resp.getQueue(0);
		if (theQueue == null)
		{
			return;
		}
		final VElement qVec = theQueue.getQueueEntryVector();
		final int siz = qVec.size();
		if (siz == 0)
		{
			return;
		}

		for (int i = siz - 1; i >= 0; i--)
		{
			final String qeid = ((JDFQueueEntry) qVec.get(i)).getQueueEntryID();
			jmf = new JMFBuilder().buildAbortQueueEntry(qeid);
			factory.send2URL(jmf, url, null, null, "testcase");
		}

		// wait to allow the worker to process the AbortQueueEntries,
		// then send RemoveQueueEntry messages

		for (int i = 0; i < siz; i++)
		{
			final String qeid = ((JDFQueueEntry) qVec.get(i)).getQueueEntryID();
			jmf = new JMFBuilder().buildRemoveQueueEntry(qeid);
			factory.send2URL(jmf, url, null, null, "testcase");
		}
	}

	/**
	 * dummy so that we can simply run the directory as a test
	 */
    @Test
	public void testNothing()
	{
		final int i = 1;
		assertTrue(1 == i);
	}

	/**
	 * requires assigned node...
	 * @param url the url to send to
	 * @return
	 * @throws MalformedURLException
	 */
	protected HttpURLConnection submitMimetoURL(final String url) throws MalformedURLException
	{
		final JDFDoc doc = _theGT.getNode().getOwnerDocument_JDFElement();
		return submitMimetoURL(doc, url, false);
	}

	/**
	 * requires assigned node...
	 * @param qeID
	 * @param url the url to send to
	 * @return
	 * @throws MalformedURLException
	 */
	protected HttpURLConnection resubmitMimetoURL(final String qeID, final String url) throws MalformedURLException
	{
		final JDFDoc doc = _theGT.getNode().getOwnerDocument_JDFElement();
		return resubmitMimetoURL(qeID, doc, url);
	}

	/**
	 * requires assigned node...
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	protected void submitXtoURL(final String url) throws MalformedURLException
	{
		final JDFNode n = _theGT.getNode();
		final BambiTestHelper helper = getHelper();
		helper.submitXtoURL(n, url);
	}

	/**
	 * @param d
	 * @param url the url to send to, if null simply grab the default worker url
	 * @param extendReference
	 * @return
	 * @throws MalformedURLException
	 */
	protected HttpURLConnection submitMimetoURL(final JDFDoc d, String url, final boolean extendReference) throws MalformedURLException
	{
		ensureCurrentGT();
		if (url == null)
        {
            url = getWorkerURL();
        }
		final BambiTestHelper helper = getHelper();
		helper.extendReference = extendReference;

		return helper.submitMimetoURL(d, url);
	}

	/**
	 * @return
	 */
	protected BambiTestHelper getHelper()
	{
		final BambiTestHelper helper = new BambiTestHelper();
		helper.returnJMF = returnJMF;
		helper.chunkSize = chunkSize;
		helper.transferEncoding = transferEncoding;
		helper.acknowledgeURL = acknowledgeURL;
		helper.bUpdateJobID = bUpdateJobID;
		helper.container = bambiContainer;
		return helper;
	}

	/**
	 * @param qeid
	 * @param d the doc of the root node
	 * @param url the url to send to
	 * @return
	 * @throws MalformedURLException
	 */
	protected HttpURLConnection resubmitMimetoURL(final String qeid, final JDFDoc d, final String url) throws MalformedURLException
	{
		ensureCurrentGT();
		final BambiTestHelper helper = getHelper();
		return helper.resubmitMimetoURL(qeid, d, url);
	}

	protected JDFQueue getQueueStatus(final String qURL)
	{
		return getHelper().getQueueStatus(qURL);
	}

	/**
	 * @param jmf the jmf to send
	 * @param url the url to send to
	 * @return
	 */
	protected JDFDoc submitJMFtoURL(final JDFJMF jmf, final String url)
	{
		return getHelper().submitJMFtoURL(jmf, url);
	}

	/**
	 * ensure that we have some dummy golden ticket
	 */
	private void ensureCurrentGT()
	{
		if (_theGT != null)
		{
			return;
		}
		final VJDFAttributeMap vParts = new VJDFAttributeMap();
		final JDFAttributeMap map = new JDFAttributeMap("SignatureName", "sig1");
		map.put("SheetName", "s1");
		vParts.add(map);
		_theGT = new MISCPGoldenTicket(2, EnumVersion.Version_1_3, 2, 2, false, vParts);
	}

	/**
	 * @param jmf
	 * @param url
	 * @return the response
	 */
	protected JDFResponse sendToURL(final JDFJMF jmf, final String url)
	{
		final JDFJMF respRoot = jmf.getOwnerDocument_JDFElement().write2URL(url).getJMFRoot();
		final JDFResponse resp = respRoot.getResponse(0);
		assertTrue(resp.getJMFRoot().isValid(EnumValidationLevel.Complete));
		return resp;
	}

	/**
	 * @param devProp
	 */
	protected void moreSetup(final DeviceProperties devProp)
	{
		// dummy stub

	}

	/**
	 * 
	 */
	protected void startContainer()
	{
		if (!wantContainer)
        {
            return;
        }
		bambiContainer = BambiContainer.getCreateInstance();
		final MultiDeviceProperties props = createPropertiesForContainer();
		props.getRoot().setAttribute("WebProxy", "proxy:8080");

		final DeviceProperties devProp = props.createDeviceProps(null);
		devProp.setDeviceID(deviceID);
		devProp.setCallBackClassName("org.cip4.bambi.extensions.ExtensionCallback");
		moreSetup(devProp);
		bambiContainer.createDevices(props, sm_dirTestDataTemp + "BambiDump");
		bambiContainer.reset();

	}

	/**
	 * @return
	 */
	protected MultiDeviceProperties createPropertiesForContainer()
	{
		final MultiDeviceProperties props = new MultiDeviceProperties(new File(sm_dirContainer));
		return props;
	}

	/**
	 * @see org.cip4.jdflib.goldenticket.BambiGoldenTicketTest#tearDown()
	 * @throws Exception
	 */
	@Override
    @After
    public void tearDown() throws Exception
	{
		super.tearDown();
		if (bambiContainer != null)
        {
            bambiContainer.shutDown();
        }
	}

}
