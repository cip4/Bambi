package org.cip4.bambi.core;

public interface Observer {
	void refreshData(final Observable observable, final String xmlRespStr);
}
