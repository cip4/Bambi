/**
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
		log.debug(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object)
	 */
	public void debug(final Object arg0)
	{
		log.debug(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
	 */
	public void error(final Object arg0, final Throwable arg1)
	{
		log.error(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#error(java.lang.Object)
	 */
	public void error(final Object arg0)
	{
		log.error(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
	 */
	public void fatal(final Object arg0, final Throwable arg1)
	{
		log.fatal(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
	 */
	public void fatal(final Object arg0)
	{
		log.fatal(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(final Object arg0, final Throwable arg1)
	{
		log.info(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#info(java.lang.Object)
	 */
	public void info(final Object arg0)
	{
		log.info(arg0);
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
		log.trace(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object)
	 */
	public void trace(final Object arg0)
	{
		log.trace(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
	 */
	public void warn(final Object arg0, final Throwable arg1)
	{
		log.warn(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object)
	 */
	public void warn(final Object arg0)
	{
		log.warn(arg0);
	}
}
