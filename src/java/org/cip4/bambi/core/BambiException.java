/**
 * 
 */
package org.cip4.bambi.core;

/**
 * generic bambi exception class
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * 02.12.2008
 */
public class BambiException extends Exception
{
	/**
	 * 
	 */
	public BambiException()
	{
		super();
	}

	/**
	 * @param message the description
	 * @param cause the previous exception
	 */
	public BambiException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause the previous exception
	 */
	public BambiException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param string the description
	 */
	public BambiException(final String string)
	{
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -302286003368076995L;

}
