package org.cip4.bambi.actions;

import static org.junit.Assert.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SettingsActionTest
{
	private static final String PAGE_SUCCESS = "success";
	private SettingsAction settingsAction = new SettingsAction();

	@Mock
	private HttpServletRequest httpServletRequest;

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
		
		List<String> formatters = settingsAction.getFormatters();
		assertNotNull("Should initialize and return formatters", formatters);
		assertTrue("Should return more then 0 formatters", formatters.size() > 0);

		String currentFormatter = settingsAction.getCurrentFormatter();
		assertTrue("Should contain current formatter", formatters.contains(currentFormatter));
	}

	@Test
	public void shouldSetAndReturnServletRequest()
	{
		settingsAction.setServletRequest(httpServletRequest);
		assertEquals("Should return expected HttpServletRequest object", httpServletRequest, settingsAction.getServletRequest());
	}

}
