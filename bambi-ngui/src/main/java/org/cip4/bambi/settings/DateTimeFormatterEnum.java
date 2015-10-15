package org.cip4.bambi.settings;

import java.util.HashMap;
import java.util.Map;

public enum DateTimeFormatterEnum
{
	US("US",     "MM/dd/yyyy HH:mm:ss"),
	EURO("EURO", "dd/MM/yyyy HH:mm:ss"),
	ISO("ISO",   "yyyy/MM/dd HH:mm:ss");
	
	private static final Map<String, DateTimeFormatterEnum> lookupByNameMap = new HashMap<String, DateTimeFormatterEnum>();
	private static final Map<String, DateTimeFormatterEnum> lookupByPatternMap = new HashMap<String, DateTimeFormatterEnum>();

	private final String name;
	private final String pattern;
	
	static
	{
		for (DateTimeFormatterEnum f : DateTimeFormatterEnum.values())
		{
			lookupByNameMap.put(f.getName(), f);
			lookupByPatternMap.put(f.getPattern(), f);
		}
	}

	private DateTimeFormatterEnum(String name, String pattern)
	{
		this.name = name;
		this.pattern = pattern;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPattern()
	{
		return pattern;
	}
	
	public static DateTimeFormatterEnum lookupByName(String name)
	{
		return lookupByNameMap.get(name);
	}
	
	public static DateTimeFormatterEnum lookupByPattern(String pattern)
	{
		return lookupByPatternMap.get(pattern);
	}
}
