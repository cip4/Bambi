/**
 * 
 */
package org.cip4.bambi.core.messaging;

import org.cip4.bambi.core.BambiLogFactory;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFSignal;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * August 9, 2009
 */
public class StatusSignalComparator extends BambiLogFactory
{

	/**
	 * 
	 */
	public StatusSignalComparator()
	{
		super();
	}

	/**
	 * @param inSignal
	 * @param last
	 * @return true if the signals are equivalent
	 */
	public boolean isSameStatusSignal(final JDFSignal inSignal, final JDFSignal last)
	{
		if (last == null)
		{
			return inSignal == null;
		}
		boolean bAllSame = true;
		for (int i = 0; bAllSame; i++)
		{
			final JDFDeviceInfo di = inSignal.getDeviceInfo(i);
			if (di == null)
			{
				break;
			}
			boolean bSameDI = false;
			for (int j = 0; !bSameDI; j++)
			{
				final JDFDeviceInfo diLast = last.getDeviceInfo(j);
				if (diLast == null)
				{
					break;
				}
				bSameDI = di.isSamePhase(diLast, false);
			}
			bAllSame = bAllSame && bSameDI;
		}
		return bAllSame;
	}

	/**
	 * @param inSignal
	 * @param last
	 */
	public void mergeStatusSignal(final JDFSignal inSignal, final JDFSignal last)
	{
		for (int i = 0; true; i++)
		{
			final JDFDeviceInfo di = inSignal.getDeviceInfo(i);
			if (di == null)
			{
				break;
			}
			boolean bSameDI = false;
			for (int j = 0; !bSameDI; j++)
			{
				final JDFDeviceInfo diLast = last.getDeviceInfo(j);
				if (diLast == null)
				{
					break;
				}
				bSameDI = di.mergeLastPhase(diLast);
			}
		}
	}

}
