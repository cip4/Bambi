package org.cip4.bambi.core.messaging;

import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public class CallURL implements Comparable<CallURL> {
    String url;

    /**
     * get the base url that is used to define equal senders
     *
     * @return the base url
     */
    public String getBaseURL() {
        if (url == null) {
            return null;
        }
        final VString v = StringUtil.tokenize(url, "/?", true);
        int len = v.size();
        if (len > 6) {
            len = 6; // 6= host / / root </?> last
        }
        final StringBuffer b = new StringBuffer();
        for (int i = 0; i < len; i++) {
            b.append(v.get(i));
        }
        return b.toString();
    }

    /**
     * @param _url the url
     */
    public CallURL(final String _url) {
        url = _url;
        url = getBaseURL();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final String baseUrl = getBaseURL();
        return (baseUrl == null ? 0 : baseUrl.hashCode());
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "[CallURL: " + getBaseURL() + "]";
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CallURL)) {
            return false;
        }
        final CallURL other = (CallURL) obj;
        return ContainerUtil.equals(getBaseURL(), other.getBaseURL());
    }

    /**
     * compares based on url values
     *
     * @param o the other callURL to compare to
     * @return -1 if this is smaller
     */
    @Override
    public int compareTo(final CallURL o) {
        return ContainerUtil.compare(url, o == null ? null : o.url);
    }
}
