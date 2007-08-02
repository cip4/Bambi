/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2007 The International Cooperation for the Integration of 
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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;

/**
 * a simulated job for Bambi
 * @author boegerni
 *
 */
public class BambiJob
{
	/**
	 * a single job phase
	 * 
	 * @author boegerni
	 *
	 */
	public class JobPhase {
		/**
		 * status to be displayed for this job phase
		 */
		public EnumDeviceStatus status=EnumDeviceStatus.Idle;
		/**
		 * job phase details (e. g. "running, producing waste"
		 */
		public String statusDetails = "";
		/**
		 * duration of job phase in milliseconds
		 */
		public int  duration=0;

		/**
		 * output to be produced in this job phase
		 */
		public int Output_Good=0;
		/**
		 * waste to be produced in this job phase
		 */
		public int Output_Waste=0;

	}

	private static Log log = LogFactory.getLog(DeviceServlet.class.getName());
	public static final String configDir=System.getProperty("catalina.base")+"/webapps/Bambi/"+"config"+File.separator;
	public List jobs;

	public BambiJob()
	{

	}

	public BambiJob(String fileName)
	{
		loadBambiJobFromFile(fileName);
	}

	/**
	 * load Bambi job definition from file
	 * @param fileName file to load
	 * @return true, if successful
	 */
	public boolean loadBambiJobFromFile(String fileName)
	{
		JDFParser p = new JDFParser();
		JDFDoc doc = p.parseFile(fileName);
		if (doc == null)
		{
			log.error( fileName+" not found, list of job phases remains empty" );
			return false;
		}

		KElement e = doc.getRoot();
		VElement v = e.getXPathElementVector("//BambiJob/*", 99);

		int counter = 0;


		for (int i = 0; i < v.size(); i++)
		{
			try 
			{
				KElement job = (KElement)v.elementAt(i);
				JobPhase phase = new JobPhase();
				phase.status = EnumDeviceStatus.getEnum(job.getXPathAttribute("@Status", "Idle"));
				phase.statusDetails = job.getXPathAttribute("@StatusDetails", "");
				phase.duration = Integer.valueOf( job.getXPathAttribute("@Duration", "0") ).intValue();
				phase.Output_Good = Integer.valueOf( job.getXPathAttribute("@Good", "0") ).intValue();
				phase.Output_Waste = Integer.valueOf( job.getXPathAttribute("@Waste", "0") ).intValue();

				jobs.add(phase);
				counter++;
			}
			catch (Exception ex)
			{
				log.warn("tried to add invalid job phase");
			}
		}

		if (counter > 0)
		{
			log.debug("created new job from "+fileName+" with "+counter+" job phases.");
			return true;
		}
		else
		{
			log.warn("no job phases were added from "+fileName);
			return false;
		}
	}
}
