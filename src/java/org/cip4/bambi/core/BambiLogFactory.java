/**
 * 
 */
package org.cip4.bambi.core;

import org.apache.commons.logging.LogFactory;

/**
 * class that automatically generates a logger for its sub-classes
 * 
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * July 30, 2009
 */
public class BambiLogFactory
{
	final protected BambiLog log;
	static int countPlus = 0;
	static int countMinus = 0;

	/**
	 * 
	 */
	public BambiLogFactory()
	{
		super();
		countPlus++;
		log = new BambiLog(LogFactory.getLog(this.getClass()));
	}

	/**
	 * @param clazz the class
	 * 
	 */
	public BambiLogFactory(final Class<?> clazz)
	{
		super();
		countPlus++;
		log = new BambiLog(LogFactory.getLog(clazz));
	}

	protected static BambiLog getLog(final Class<?> clazz)
	{
		return new BambiLogFactory(clazz).log;
	}

	/**
	 * @return total # of created log objects
	 */
	public int getCreated()
	{
		return countPlus;
	}

	/**
	 * @return total # of deleted (garbage collected) log objects
	 */
	public int getDeleted()
	{
		return countMinus;
	}

	/**
	 * @return
	 */
	public BambiLog getLog()
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
		log.debug("destroying: " + this.getClass().getName());
		super.finalize();
	}
}
