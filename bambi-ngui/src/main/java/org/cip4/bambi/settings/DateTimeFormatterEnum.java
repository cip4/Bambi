package org.cip4.bambi.settings;

public enum DateTimeFormatterEnum
{
	US("US",     "MM/dd/yyyy HH:mm:ss"),
	EURO("EURO", "dd/MM/yyyy HH:mm:ss"),
	ISO("ISO",   "yyyy/MM/dd HH:mm:ss");

	private final String name;
	private final String pattern;

	DateTimeFormatterEnum(String name, String pattern)
	{
		this.name = name;
		this.pattern = pattern;
	}
}
