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

package org.cip4.bambi;

import java.io.File;
import java.net.MalformedURLException;

import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.IDeviceProperties;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.VJDFAttributeMap;
import org.cip4.jdflib.goldenticket.BaseGoldenTicket;
import org.cip4.jdflib.goldenticket.BaseGoldenTicketTest;
import org.cip4.jdflib.goldenticket.MISCPGoldenTicket;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.UrlUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen abstract test case for all bambi tests note that this has some site specific details that must be modified
 */
public class BambiTestCase extends BaseGoldenTicketTest
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		BaseGoldenTicket.setMisURL("http://kie-prosirai-lg:8080/httpdump/BambiTest");
		_theGT = new MISCPGoldenTicket(1, null, 1, 1, true, null);
		_theGT.assign(null);
	}

	protected final static String cwd = System.getProperty("user.dir");
	protected final static String sm_dirTestData = cwd + File.separator + "test" + File.separator + "data" + File.separator;
	protected final static String sm_dirTestTemp = cwd + File.separator + "test" + File.separator + "temp" + File.separator;
	protected final static String sm_UrlTestData = "File:test/data/";

	protected static String simWorkerUrl = "http://localhost:8080/SimWorker/jmf/sim002";
	protected static String proxyUrl = "http://kie-prosirai-lg:8080/BambiProxy/jmf/pushproxy";
	protected static String proxySlaveUrl = "http://kie-prosirai-lg:8080/BambiProxy/slavejmf/pushproxy";
	// protected static String simWorkerUrl = "http://kie-prosirai-lg:8080/potato/jmf/GreatPotato";
	protected static String manualWorkerUrl = null;
	protected static String returnJMF = "http://localhost:8080/httpDump/returnJMF";
	protected static String subscriptionURL = "http://localhost:8080/httpdump/testSubscriptions";
	protected static String returnURL = null;// "http://localhost:8080/httpDump/returnURL";

	protected boolean bUpdateJobID = false;
	protected int chunkSize = -1;
	protected String transferEncoding = UrlUtil.BASE64;

	protected BaseGoldenTicket _theGT = null;

	static class BambiTestProp implements IDeviceProperties
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getAppDir()
		 */
		public File getAppDir()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getBaseDir()
		 */
		public File getBaseDir()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getCallBackClass()
		 */
		public IConverterCallback getCallBackClass()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceID()
		 */
		public String getDeviceID()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceType()
		 */
		public String getDeviceType()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceURL()
		 */
		public String getDeviceURL()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getErrorHF()
		 */
		public File getErrorHF()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getInputHF()
		 */
		public File getInputHF()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getJDFDir()
		 */
		public File getJDFDir()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getMaxPush()
		 */
		public int getMaxPush()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getOutputHF()
		 */
		public File getOutputHF()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getProxyControllerURL()
		 */
		public String getProxyControllerURL()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveDeviceID()
		 */
		public String getSlaveDeviceID()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveErrorHF()
		 */
		public File getSlaveErrorHF()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveInputHF()
		 */
		public File getSlaveInputHF()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveOutputHF()
		 */
		public File getSlaveOutputHF()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getSlaveURL()
		 */
		public String getSlaveURL()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getTrackResource()
		 */
		public String getTrackResource()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getTypeExpression()
		 */
		public String getTypeExpression()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getAmountResources()
		 */
		public VString getAmountResources()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceHTTPChunk()
		 */
		public int getControllerHTTPChunk()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceMIMEEncoding()
		 */
		public String getControllerMIMEEncoding()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getReturnMIME()
		 */
		public QEReturn getReturnMIME()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getContextURL()
		 */
		public String getContextURL()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceAttribute(java.lang.String)
		 */
		public String getDeviceAttribute(final String key)
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getDeviceClass()
		 */
		public AbstractDevice getDeviceInstance()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getWatchURL()
		 */
		public String getWatchURL()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.cip4.bambi.core.IDeviceProperties#getControllerMIMEExpansion()
		 */
		public boolean getControllerMIMEExpansion()
		{
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#getConfigDir()
		 * @return
		 */
		public File getConfigDir()
		{
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#serialize()
		 */
		public boolean serialize()
		{
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setDeviceType(java.lang.String)
		 */
		public void setDeviceType(final String deviceType)
		{
			// TODO Auto-generated method stub

		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setWatchURL(java.lang.String)
		 */
		public void setWatchURL(final String WatchURL)
		{
			// TODO Auto-generated method stub

		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setErrorHF(java.io.File)
		 */
		public void setErrorHF(final File hf)
		{
			// TODO Auto-generated method stub

		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setInputHF(java.io.File)
		 */
		public void setInputHF(final File hf)
		{
			// TODO Auto-generated method stub

		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setOutputHF(java.io.File)
		 */
		public void setOutputHF(final File hf)
		{
			// TODO Auto-generated method stub

		}

		/**
		 * @see org.cip4.bambi.core.IDeviceProperties#setTypeExpression(java.lang.String)
		 */
		public void setTypeExpression(final String exp)
		{
			// TODO Auto-generated method stub

		}

	}

	/**
	 * banbi test case
	 */
	public BambiTestCase()
	{
		JDFJMF.setTheSenderID("BambiTest");
	}

	/**
	 * @return the url of the test
	 */
	protected String getTestURL()
	{
		String url = null;
		url = UrlUtil.fileToUrl(new File(cwd), false);
		return url + "/test/data/";
	}

	/**
	 * cleaning up, brute-force-sytle: send a AbortQueueEntry and a RemoveQueueEntry message to every QueueEntry in the Queue
	 * @param url the URL of the device to send the command to
	 */
	protected void abortRemoveAll(final String url)
	{
		final JMFFactory factory = JMFFactory.getJMFFactory();
		JDFJMF jmf = factory.buildQueueStatus();
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
			jmf = factory.buildAbortQueueEntry(qeid);
			factory.send2URL(jmf, url, null, null, "testcase");
		}

		// wait to allow the worker to process the AbortQueueEntries,
		// then send RemoveQueueEntry messages

		for (int i = 0; i < siz; i++)
		{
			final String qeid = ((JDFQueueEntry) qVec.get(i)).getQueueEntryID();
			jmf = factory.buildRemoveQueueEntry(qeid);
			factory.send2URL(jmf, url, null, null, "testcase");
		}
	}

	/**
	 * dummy so that we can simply run the directory as a test
	 */
	public void testNothing()
	{
		assertTrue(1 == 1);
	}

	/**
	 * requires assigned node...
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	protected void submitMimetoURL(final String url) throws MalformedURLException
	{
		final JDFDoc doc = _theGT.getNode().getOwnerDocument_JDFElement();
		submitMimetoURL(doc, url);
	}

	/**
	 * requires assigned node...
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	protected void submitXtoURL(final String url) throws MalformedURLException
	{
		final JDFNode n = _theGT.getNode();
		final BambiTestHelper helper = new BambiTestHelper();
		helper.returnJMF = returnJMF;
		helper.chunkSize = chunkSize;
		helper.transferEncoding = transferEncoding;
		helper.submitXtoURL(n, url);
	}

	/**
	 * @param n the node to send as root node
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	protected void submitMimetoURL(final JDFDoc d, final String url) throws MalformedURLException
	{
		ensureCurrentGT();
		final BambiTestHelper helper = new BambiTestHelper();
		helper.returnJMF = returnJMF;
		helper.chunkSize = chunkSize;
		helper.transferEncoding = transferEncoding;
		helper.submitMimetoURL(d, url);
	}

	protected JDFQueue getQueueStatus(final String qURL)
	{
		return new BambiTestHelper().getQueueStatus(qURL);
	}

	/**
	 * @param jmf the jmf to send
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	protected JDFDoc submitJMFtoURL(final JDFJMF jmf, final String url)
	{
		return new BambiTestHelper().submitJMFtoURL(jmf, url);
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

}
