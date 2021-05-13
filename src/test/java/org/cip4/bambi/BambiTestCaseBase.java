/*
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
package org.cip4.bambi;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoComChannel.EnumChannelType;
import org.cip4.jdflib.core.DocumentJDFImpl;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFCustomerInfo;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.JDFNodeInfo;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.datatypes.JDFIntegerRange;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource.EnumResourceClass;
import org.cip4.jdflib.resource.process.JDFComponent;
import org.cip4.jdflib.resource.process.JDFContact;
import org.cip4.jdflib.resource.process.JDFExposedMedia;
import org.cip4.jdflib.resource.process.JDFMedia;
import org.cip4.jdflib.resource.process.JDFPerson;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.logging.LogConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * base class for JDFLib test case classes
 *
 * @author prosirai
 *
 */
public abstract class BambiTestCaseBase
{
	/**
	 *
	 */
	public BambiTestCaseBase()
	{
		super();
		LogConfigurator.configureLog(null, null);
		setTestNetwork(false);

		// setTestNetwork(new UrlCheck("http://www.example.com").pingRC(1000) == 200);
	}

	/**
	 *
	 * @param name
	 */
	public BambiTestCaseBase(final String name)
	{
		LogConfigurator.configureLog(null, null);
		setTestNetwork(false);
	}

	static protected final String sm_dirTestData = getTestDataDir();
	static protected final String sm_dirTestSchema = sm_dirTestData + "schema" + File.separator + "Version_1_4" + File.separator;
	static protected final String sm_dirTestDataTemp = sm_dirTestData + "temp" + File.separator;
	static protected final EnumVersion defaultVersion = EnumVersion.Version_1_4;

	private static String getTestDataDir()
	{
		return BambiTestCaseBase.class.getResource("/data").getPath() + File.separator;

	}

	protected static JDFDoc creatXMDoc()
	{
		final JDFDoc doc = new JDFDoc("JDF");
		final JDFNode n = doc.getJDFRoot();
		n.setJobPartID("P1");
		n.setJobID("J1");
		n.setType("ConventionalPrinting", true);
		n.appendElement("NS:Foobar", "www.foobar.com");

		n.appendMatchingResource("Layout", JDFNode.EnumProcessUsage.AnyInput, null);
		final JDFComponent comp = (JDFComponent) n.appendMatchingResource("Component", JDFNode.EnumProcessUsage.AnyOutput, null);
		final JDFExposedMedia xm = (JDFExposedMedia) n.appendMatchingResource("ExposedMedia", JDFNode.EnumProcessUsage.Plate, null);
		final JDFNodeInfo ni = n.appendNodeInfo();
		final JDFMedia m = xm.appendMedia();
		m.appendElement("NS:FoobarMedia", "www.foobar.com");

		Assert.assertEquals("m Class", m.getResourceClass(), EnumResourceClass.Consumable);

		final VString vs = new VString();
		vs.add("SignatureName");
		vs.add("SheetName");
		vs.add("Side");

		final JDFAttributeMap mPart1 = new JDFAttributeMap("SignatureName", "Sig1");
		mPart1.put("SheetName", "S1");
		mPart1.put("Side", "Front");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);

		mPart1.put("Side", "Back");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);

		mPart1.put("SheetName", "S2");
		mPart1.put("Side", "Front");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);

		mPart1.put("Side", "Back");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);

		mPart1.put("SignatureName", "Sig2");
		mPart1.put("SheetName", "S1");
		mPart1.put("Side", "Front");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);
		comp.appendElement("foo:bar", "www.foobar.com");

		mPart1.put("Side", "Back");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);

		mPart1.put("SheetName", "S2");
		mPart1.put("Side", "Front");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);

		mPart1.put("Side", "Back");
		xm.getCreatePartition(mPart1, vs);
		ni.getCreatePartition(mPart1, vs);
		comp.getCreatePartition(mPart1, vs);
		return doc;
	}

	protected String agentName;
	protected String agentVersion;
	protected String author;
	protected String senderID;
	protected long mem;
	protected Log log;
	private boolean bTestNetwork;

	// //////////////////////////////////////////////////////////////////////////
	/**
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception
	{
		JDFElement.setDefaultJDFVersion(defaultVersion);
		senderID = "TestSender";
		JDFJMF.setTheSenderID(senderID);
		KElement.uniqueID(1);
		JDFIntegerRange.setDefaultDef(0);
		agentName = JDFAudit.getStaticAgentName();
		agentVersion = JDFAudit.getStaticAgentVersion();
		author = JDFAudit.getStaticAuthor();
		mem = getCurrentMem();
		log = LogFactory.getLog(getClass());
		new File(sm_dirTestDataTemp).mkdirs();
	}

	/**
	 * get the currently used memory
	 * @return the used memory
	 */
	protected long getCurrentMem()
	{
		System.gc();
		final Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
	}

	/**
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() throws Exception
	{
		KElement.setLongID(true);
		JDFElement.setDefaultJDFVersion(EnumVersion.Version_1_3);
		JDFAudit.setStaticAgentName(agentName);
		JDFAudit.setStaticAgentVersion(agentVersion);
		JDFAudit.setStaticAuthor(author);
		JDFNodeInfo.setDefaultWorkStepID(false);
		DocumentJDFImpl.setStaticStrictNSCheck(true);
	}

	/**
	 *
	 * write an element to the standard test directory sm_dirTestDataTemp
	 * @param e
	 * @param filename
	 */
	protected void writeTest(final KElement e, final String filename)
	{
		e.write2File(sm_dirTestDataTemp + filename);
	}

	/**
	 * create a standard customerInfo
	 * @param doc the doc to preparein
	 * @return the new customerInfo
	 */
	protected JDFCustomerInfo prepareCustomerInfo(final JDFDoc doc)
	{
		final JDFNode n = doc.getJDFRoot();
		final JDFCustomerInfo info = n.appendCustomerInfo();
		info.setCustomerID("MISCustomerID");
		final VString vct = new VString();
		vct.add("Customer");
		final JDFContact contact = info.appendContact();
		contact.setContactTypes(vct);
		final JDFPerson p = contact.appendPerson();
		p.setFamilyName("LastName");
		p.setFirstName("FirstName");
		p.appendComChannel().setPhoneNumber("+49 123 4567", null, EnumChannelType.Phone);
		p.appendComChannel().setPhoneNumber("+49 123 4568", null, EnumChannelType.Fax);
		p.appendComChannel().setEMailLocator("customer@company.com");
		info.setCustomerJobName("Customer Job Identifier or Name");
		return info;
	}

	protected JDFNodeInfo prepareNodeInfo(final JDFDoc doc)
	{
		final JDFNode n = doc.getJDFRoot();
		final JDFNodeInfo ni = n.getCreateNodeInfo();
		final JDFDate date = new JDFDate();
		ni.setFirstStart(date);
		date.addOffset(0, 0, 0, 5);
		ni.setLastEnd(date);
		ni.setDescriptiveName("must be done 5 days after start");
		return ni;
	}

	/**
	 * @see junit.framework.TestCase#toString()
	 * @return
	 */
	@Override
	public String toString()
	{
		return "[" + StringUtil.token(this.getClass().getName(), -1, ".") + " Version:  " + defaultVersion + " " + new File(sm_dirTestData).getAbsolutePath() + " ]\n";
	}

	/**
	 * Setter for bTestNetwork attribute.
	 * @param bTestNetwork the bTestNetwork to set
	 */
	public void setTestNetwork(final boolean bTestNetwork)
	{
		this.bTestNetwork = bTestNetwork;
	}

	/**
	 * Getter for bTestNetwork attribute.
	 * @return the bTestNetwork
	 */
	public boolean isTestNetwork()
	{
		return bTestNetwork;
	}

}
// ///////////////////////////////////////////////////////////////////////////
// ///////////////////////////////////////////////////////////////////////////
// ///////////////////////////////////////////////////////////////////////////
// ///////////////////////////////////////////////////////////////////////////
// ///////////////////////////////////////////////////////////////////////////
// ///////////////////////////////////////////////////////////////////////////
// ///////////////////////////////////////////////////////////////////////////
