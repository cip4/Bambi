/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.util.CPUTimer;

/**
 * class that automatically generates a logger for its sub-classes
 *
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *
 * July 30, 2009
 */
public class BambiLogFactory
{
	final protected Log log;
	static long countPlus = 0;
	static long countMinus = 0;

	/**
	 *
	 */
	public BambiLogFactory()
	{
		super();
		incrementCount();
		log = LogFactory.getLog(this.getClass());
	}

	/**
	 *
	 */
	static void incrementCount()
	{
		countPlus++;
	}

	/**
	 * @param clazz the class
	 *
	 */
	public BambiLogFactory(final Class<?> clazz)
	{
		super();
		incrementCount();
		log = LogFactory.getLog(clazz);
	}

	/**
	 * @return total # of created log objects
	 */
	public long getCreated()
	{
		return countPlus;
	}

	/**
	 * @return total # of deleted (garbage collected) log objects
	 */
	public long getDeleted()
	{
		return countMinus;
	}

	/**
	 * @return the name for a given timer
	 */
	protected String getTimerName()
	{
		return getClass().getName();
	}

	/**
	 * @return
	 */
	protected CPUTimer getGlobalTimer()
	{
		return CPUTimer.getFactory().getGlobalTimer(getTimerName());
	}

	/**
	 * @return
	 */
	protected CPUTimer getLocalTimer()
	{
		return CPUTimer.getFactory().getCreateCurrentTimer(getTimerName());
	}

	/**
	 * @return
	 */
	public Log getLog()
	{
		return log;
	}

	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		countMinus++;
		if (countMinus % 1000 == 0)
			log.debug("destroying: " + this.getClass().getName() + " + " + countPlus + " - " + countMinus + " = " + (countPlus - countMinus));
		super.finalize();
	}
}
