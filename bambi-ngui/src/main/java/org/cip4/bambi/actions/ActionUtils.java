package org.cip4.bambi.actions;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.cip4.bambi.core.StreamRequest;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.StringUtil;

public class ActionUtils {
	
	public static StreamRequest createStreamRequest(final HttpServletRequest request) throws IOException {
		StreamRequest sr = new StreamRequest(request.getInputStream());
		final String contentType = request.getContentType();
		sr.setContentType(contentType);
		sr.setRequestURI("/" /*request.getRequestURL().toString()*/);
		sr.setHeaderMap(getHeaderMap(request));
		sr.setParameterMap(new JDFAttributeMap(getParameterMap(request)));
		sr.setRemoteHost(request.getRemoteHost());
		return sr;
	}
	
	public static Map<String, String> getParameterMap(HttpServletRequest request) {
		Map<String, String[]> pm = request.getParameterMap();
		Map<String, String> retMap = new JDFAttributeMap();
		Set<String> keyset = pm.keySet();
		for (String key : keyset)
		{
			String[] strings = pm.get(key);
			if (strings != null && strings.length > 0)
			{
				String s = strings[0];
				for (int i = 1; i < strings.length; i++)
				{
					s += "," + strings[i];
				}
				s = StringUtil.getNonEmpty(s);
				if (s != null)
					retMap.put(key, s);
			}
		}
		return retMap.size() == 0 ? null : retMap;
	}
	
	public static JDFAttributeMap getHeaderMap(HttpServletRequest request) {
		Enumeration<String> headers = request.getHeaderNames();
		if (!headers.hasMoreElements())
		{
			return null;
		}
		final JDFAttributeMap map = new JDFAttributeMap();
		while (headers.hasMoreElements())
		{
			String header = headers.nextElement();
			Enumeration<String> e = request.getHeaders(header);
			VString v = new VString(e);
			if (v.size() > 0)
			{
				map.put(header, StringUtil.setvString(v, ",", null, null));
			}
		}
		return map.size() == 0 ? null : map;
	}
}
