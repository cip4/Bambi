package org.cip4.bambi.core;

public interface Observable {
	public void addListener(Observer obs);
	public void removeListener(Observer obs);
}
