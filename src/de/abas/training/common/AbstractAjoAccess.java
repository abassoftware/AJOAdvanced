package de.abas.training.common;

import java.io.FileWriter;
import java.io.IOException;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.DbMessage;
import de.abas.erp.db.MessageListener;
import de.abas.erp.db.util.ContextHelper;

public abstract class AbstractAjoAccess implements ContextRunnable {

	// Enumeration: ContextMode
	public enum ContextMode {
		UNDEFINED {
			@Override
			public String toString() {
				return "";
			}
		},
		SERVER_MODE {
			@Override
			public String toString() {
				return "server-mode";
			}
		},
		CLIENT_MODE {
			@Override
			public String toString() {
				return "client-mode";
			}
		}
	}

	// Enumeration: Status
	public enum Status {
		UNDEFINED {
			@Override
			public String toString() {
				return "";
			}
		},
		OK_MODE {

			@Override
			public String toString() {
				return "ok-mode";
			}

		},
		ERROR_MODE {

			@Override
			public String toString() {
				return "error-mode";
			}
		}
	}

	// define EDP connection properties
	private String hostname = "schulung";
	private String mandant = "i7erp4";

	private String password = "sy";

	private int port = 6550;

	private FileWriter fileWriterLogging;

	// Initialize DbContext
	private DbContext dbContext = null;
	private ContextMode mode = ContextMode.UNDEFINED;

	// add default message listener.
	// gets all text, status and error messages
	public void addDefaultMessageListener() {
		getDbContext().addMessageListener(new MessageListener() {
			// Displays all text, status and error messages
			@Override
			public void receiveMessage(DbMessage message) {
				getDbContext().out().println("|" + message + "|");
			}

		});
	}

	// enable EDP-logging. Creates log file name, using class description
	public final void enableLogging() {
		enableLogging(getClass().getSimpleName() + ".log");
	}

	// enable EDP-logging. creates log file name, using parameter filename
	public void enableLogging(String fileName) {
		try {
			fileWriterLogging = new FileWriter(fileName);
			getDbContext().setLogger(fileWriterLogging);
		}
		catch (IOException e) {
			getDbContext().out().println(e.getMessage());
		}
	}

	// get DbContext
	// creates a Client-Context if dbContext == null
	public DbContext getDbContext() {
		if (dbContext == null) {
			dbContext =
					ContextHelper.createClientContext(hostname, port, mandant,
							password, this.getClass().getSimpleName());
			mode = ContextMode.CLIENT_MODE;
		}
		return dbContext;
	}

	// Connection parameter
	// get hostname
	public String getHostname() {
		return hostname;
	}

	// get mandant
	public String getMandant() {
		return mandant;
	}

	// get dbContext mode
	public String getMode() {
		return mode.toString();
	}

	// get port
	public int getPort() {
		return port;
	}

	// Abstrakte run-Methode definieren. Diese muss in allen abgeleiteten
	// Klassen eingebaut
	// und vervollständigt werden.
	public abstract void run(String[] args);

	// if content of dbContext == null. Initialize mode
	public final void runClientProgram(String[] args) {
		run(args);
		// Protokollierung abschalten
		disableLogging();
		// Datenbankkontext schließen
		getDbContext().close();
	}

	// server access: get server context. Initialize mode
	@Override
	public int runFop(FOPSessionContext fopSessionContext, String[] args)
			throws FOPException {
		dbContext = fopSessionContext.getDbContext();
		mode = ContextMode.SERVER_MODE;
		// run-methode aufrufen. Argumente übergeben
		run(args);
		return 0;
	}

	// if client context is running, set hostname
	// close dbContext and define dbContont = null
	public void setHostname(String hostname) {
		if (isClientContextRunning()) {
			this.hostname = hostname;
			dbContext.close();
			dbContext = null;
		}
	}

	// if client context is running, set mandant
	// close dbContext and define dbContont = null
	public void setMandant(String mandant) {
		if (isClientContextRunning()) {
			this.mandant = mandant;
			dbContext.close();
			dbContext = null;
		}
	}

	// if client context is running, set passwort
	// close dbContext and define dbContont = null
	public void setPassword(String password) {
		if (isClientContextRunning()) {
			this.password = password;
			dbContext.close();
			dbContext = null;
		}
	}

	// if client context is running, set port
	// close dbContext and define dbContont = null
	public void setPort(int port) {
		if (isClientContextRunning()) {
			this.port = port;
			dbContext.close();
			dbContext = null;
		}
	}

	// disable EDP-logging
	private void disableLogging() {
		if (null != fileWriterLogging) {
			try {
				fileWriterLogging.close();
			}
			catch (IOException e) {
				getDbContext().out().println(e.getMessage());
			}
			finally {
				fileWriterLogging = null;
			}
		}
		getDbContext().setLogger(null);
	}

	// is client context running?
	// true -> client is running
	// false -> client context is not running
	private boolean isClientContextRunning() {
		if (mode.equals(ContextMode.CLIENT_MODE)) {
			return true;
		}
		else {
			dbContext.out().println(
					"No Client-Mode running -> parameter may not be changed");
			return false;
		}
	}
}
