package de.abas.training.advanced.testutility;

import org.apache.log4j.Logger;

public class TestLogger {

	public static final String CLEANUP_MESSAGE = "Running cleanup";
	public static final String TEST_INIT_MESSAGE = "Running test %s";
	public static final String SETUP_MESSAGE = "Running setup";
	public static final String CONNECTION_MESSAGE = "Connected to %s on host %s";

	public static Logger getLogger() {
		final Throwable t = new Throwable();
		t.fillInStackTrace();
		final String fullClassName = t.getStackTrace()[1].getClassName();

		return Logger.getLogger(fullClassName);
	}

}
