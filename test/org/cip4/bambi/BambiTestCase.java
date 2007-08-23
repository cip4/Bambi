
package org.cip4.bambi;

import java.io.File;
import java.net.MalformedURLException;

import org.cip4.jdflib.util.UrlUtil;

import junit.framework.TestCase;

public class BambiTestCase extends TestCase {
	
	// TODO fix: sm_dirTestData is pointing to nirvana (Bambi/null/WebApps/...)
    static protected final String sm_dirTestData     = "test" + File.separator + "data" + File.separator;
    static protected final String sm_UrlTestData     = "File:test/data/";
    final static protected String cwd =System.getProperty("user.dir");

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
       /**
     * @return
     */
	protected String getTestURL()
	{
	    String url=null;
	    try
	    {
	        url=UrlUtil.fileToUrl(new File(cwd), false);
	    }
	    catch (MalformedURLException x)
	    {
	        return null;
	    }
	    return url+"/test/data/";
	}

}
