package org.cip4.bambi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.cip4.bambi.servlets.DeviceServlet;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.util.UrlUtil;
import junit.framework.TestCase;

public class BambiTestCase extends TestCase {
	protected final static String sm_dirTestData = "test" + File.separator + "data" + File.separator;
    protected final static String sm_UrlTestData = "File:test/data/";
    protected final static String cwd =System.getProperty("user.dir");
    protected static String BambiUrl="";

	protected void setUp() throws Exception {
		super.setUp();
		DeviceServlet.configDir=cwd+File.separator+"WebContent"+File.separator+"config"+File.separator;
		DeviceServlet.jdfDir=cwd+File.separator+"test"+File.separator+"data"+File.separator;
		
		Properties properties = new Properties();
		FileInputStream in=null;
		try {
			in = new FileInputStream(DeviceServlet.configDir+"Bambi.properties");
			properties.load(in);
			JDFJMF.setTheSenderID(properties.getProperty("SenderID"));
			BambiUrl= properties.getProperty("BambiURL")+"/"+properties.getProperty("RootDeviceID");
			in.close();
		} catch (IOException e) {
			System.out.println("failed to load Bambi properties on test init");
		}
		
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
    // dummy so that we can simply run the directory as a test
    public void testNothing() 
    {
        assertTrue(1==1);
    }
}
