package org.cip4.bambi.actions;

import org.apache.log4j.Logger;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiContainer;

import com.opensymphony.xwork2.ActionSupport;

public class JmfAction extends ActionSupport {
	final static Logger log = Logger.getLogger(JmfAction.class);
	
	public String execute() throws Exception {
//		BambiContainer.getCreateInstance();
		BambiContainer theContainer = BambiContainer.getInstance();
		System.out.println("theContainer action: " + theContainer);
		
		if (theContainer == null) {
		} else {
			System.out.println("... some work occurred here !!!");
			
			AbstractDevice rootDev = theContainer.getRootDev();
			rootDev.startWork();
//			String getPost = bPost ? "post" : "get";
			
			rootDev.endWork();
		}
		
		return SUCCESS;
	}
}
