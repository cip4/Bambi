package org.cip4.bambi.actions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SettingsActionTest
{
	private static final String PAGE_SUCCESS = "success";
	private SettingsAction settingsAction = new SettingsAction();

	@Before
	public void setUp() throws Exception
	{
	}
	
	@Test
	public void shouldReturnPageSuccess()
	{
		try
		{
			String page = settingsAction.execute();
			assertEquals("Extected 'success' page", PAGE_SUCCESS, page);
		} catch (Exception e)
		{
			fail("Should not fail");
		}
	}

	@Test
	public void shouldReturnListOfFormatters()
	{
		try
		{
			settingsAction.execute();
		} catch (Exception e)
		{
			fail("Should not fail");
		}
		
		settingsAction.getFormatters();
	}

}
