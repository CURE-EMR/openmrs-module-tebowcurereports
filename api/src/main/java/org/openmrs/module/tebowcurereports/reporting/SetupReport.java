package org.openmrs.module.tebowcurereports.reporting;

/**
 * Report Setup / teardown interface
 */
public interface SetupReport {
	
	public void setup() throws Exception;
	
	public void delete();
}
