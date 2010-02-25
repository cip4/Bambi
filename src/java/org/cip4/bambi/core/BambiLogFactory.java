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
	static long countPlus = 0;
	static long countMinus = 0;

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
		if (countMinus % 1000 == 0)
			log.debug("destroying: " + this.getClass().getName() + " + " + countPlus + " - " + countMinus + " = " + (countPlus - countMinus));
		super.finalize();
	}
}
