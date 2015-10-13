package org.cip4.bambi.settings;

import java.util.Date;

public class BambiServerUtils
{
	private static final String NO_VALUE = "---";
	
	
	public static String convertTime(final long timeMs)
	{
		String result = NO_VALUE;
		if (timeMs > 0)
		{
			result = ConfigurationHandler.getInstance().getDateTimeFormatter().format(new Date(timeMs));
		}
		return result;
	}
}
