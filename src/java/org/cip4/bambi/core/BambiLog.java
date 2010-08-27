/*
 *
 * The CIP4 Software License, Version 1.0
 *
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

import org.apache.commons.logging.Log;

/**
 * class that creates a Log4J compliant logger for any object
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * July 30, 2009
 */
public class BambiLog
{

	final private Log log;

	private String whoCalledMe()
	{
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements.length >= 4)
		{
			// 0=getStackTrace
			// 1=this
			// 2=the BambiLog log call
			// 3=the original caller
			StackTraceElement caller = stackTraceElements[3];
			String classname = caller.getClassName();
			String methodName = caller.getMethodName();
			int lineNumber = caller.getLineNumber();
			return classname + "." + methodName + ":" + lineNumber + " - ";
		}
		else
			return "";
	}

	/**
	* @param _log
	* 
	*/
	public BambiLog(final Log _log)
	{
		super();
		log = _log;
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
	 */
	public void debug(final Object arg0, final Throwable arg1)
	{
		log.debug(whoCalledMe() + arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object)
	 */
	public void debug(final Object arg0)
	{
		log.debug(whoCalledMe() + arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
	 */
	public void error(final Object arg0, final Throwable arg1)
	{
		log.error(whoCalledMe() + arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#error(java.lang.Object)
	 */
	public void error(final Object arg0)
	{
		log.error(whoCalledMe() + arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
	 */
	public void fatal(final Object arg0, final Throwable arg1)
	{
		log.fatal(whoCalledMe() + arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
	 */
	public void fatal(final Object arg0)
	{
		log.fatal(whoCalledMe() + arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(final Object arg0, final Throwable arg1)
	{
		log.info(whoCalledMe() + arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#info(java.lang.Object)
	 */
	public void info(final Object arg0)
	{
		log.info(whoCalledMe() + arg0);
	}

	/**
	 * @return
	 * @see org.apache.commons.logging.Log#isDebugEnabled()
	 */
	public boolean isDebugEnabled()
	{
		return log.isDebugEnabled();
	}

	/**
	 * @return
	 * @see org.apache.commons.logging.Log#isErrorEnabled()
	 */
	public boolean isErrorEnabled()
	{
		return log.isErrorEnabled();
	}

	/**
	 * @return
	 * @see org.apache.commons.logging.Log#isFatalEnabled()
	 */
	public boolean isFatalEnabled()
	{
		return log.isFatalEnabled();
	}

	/**
	 * @return
	 * @see org.apache.commons.logging.Log#isInfoEnabled()
	 */
	public boolean isInfoEnabled()
	{
		return log.isInfoEnabled();
	}

	/**
	 * @return
	 * @see org.apache.commons.logging.Log#isTraceEnabled()
	 */
	public boolean isTraceEnabled()
	{
		return log.isTraceEnabled();
	}

	/**
	 * @return
	 * @see org.apache.commons.logging.Log#isWarnEnabled()
	 */
	public boolean isWarnEnabled()
	{
		return log.isWarnEnabled();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
	 */
	public void trace(final Object arg0, final Throwable arg1)
	{
		log.trace(whoCalledMe() + arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object)
	 */
	public void trace(final Object arg0)
	{
		log.trace(whoCalledMe() + arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
	 */
	public void warn(final Object arg0, final Throwable arg1)
	{
		log.warn(whoCalledMe() + arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object)
	 */
	public void warn(final Object arg0)
	{
		log.warn(whoCalledMe() + arg0);
	}
}
