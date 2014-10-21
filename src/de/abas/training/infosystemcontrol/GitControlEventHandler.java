package de.abas.training.infosystemcontrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.abas.eks.jfop.remote.EKS;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.api.system.SystemCommand;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.ow1.GitControl;
import de.abas.erp.db.infosystem.custom.ow1.GitControl.Row;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

@EventHandler(head = GitControl.class, row = GitControl.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class GitControlEventHandler {

    private final String ICON_STAGED = "icon:ball_green";
    private final String ICON_MODIFIED = "icon:ball_orange";
    private final String ICON_UNTRACKED = "icon:ball_red";

    @ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
    public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	head.table().clear();
	getGitStatus(head);
    }

    @ButtonEventHandler(field = "yadd", type = ButtonEventType.AFTER)
    public void yaddAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	// TODO Auto-generated method stub
    }

    @ButtonEventHandler(field = "ycommit", type = ButtonEventType.AFTER)
    public void ycommitAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	// TODO Auto-generated method stub
    }

    @ButtonEventHandler(field = "ycommitall", type = ButtonEventType.AFTER)
    public void ycommitallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	// TODO Auto-generated method stub
    }

    @ButtonEventHandler(field = "ygitignore", type = ButtonEventType.AFTER)
    public void ygitignoreAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	try {
	    initializeGitIgnore(ctx);
	    openGitIgnoreInEditor();
	}
	catch (SecurityException e) {
	    throw new EventException("An error occurred while accessing .gitignore. Your user rights do not seem to be sufficient.");
	}
	catch (IOException e) {
	    throw new EventException("An error occurred while editing .gitignore.");
	}
    }

    @ButtonEventHandler(field = "yinit", type = ButtonEventType.AFTER)
    public void yinitAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	BufferedReader bufferedReader = null;
	try {
	    bufferedReader = runSystemCommand("cd $MANDANTDIR && git init");
	    String console = getConsole(bufferedReader);
	    new TextBox(ctx, "git init", console).show();
	}
	catch (IOException e) {
	    throw new EventException(e.getMessage());
	}
	finally {
	    closeBufferedReader(bufferedReader);
	}
    }

    @ButtonEventHandler(field = "ystatus", type = ButtonEventType.AFTER)
    public void ystatusAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	// TODO Auto-generated method stub
    }

    private void closeBufferedReader(BufferedReader bufferedReader) throws EventException {
	if (bufferedReader != null) {
	    try {
		bufferedReader.close();
	    }
	    catch (IOException e) {
		throw new EventException("An error occurred while trying to close BufferedReader instance.");
	    }
	}
    }

    private void closeBufferedWriter(BufferedWriter bufferedWriter) throws EventException {
	if (bufferedWriter != null) {
	    try {
		bufferedWriter.close();
	    }
	    catch (IOException e) {
		throw new EventException("An error occurred while trying to close BufferedWriter instance.");
	    }
	}
    }

    private String getConsole(BufferedReader bufferedReader) throws IOException {
	String line = "";
	String message = "";
	while ((line = bufferedReader.readLine()) != null) {
	    message = message + line + "\n";
	}
	bufferedReader.close();
	return message;
    }

    private void getFiles(GitControl head, String command, boolean isStaged, String icon) throws EventException {
	BufferedReader bufferedReader = null;
	try {
	    bufferedReader = runSystemCommand(command);
	    String line = "";
	    while ((line = bufferedReader.readLine()) != null) {
		Row row = head.table().appendRow();
		row.setYstaged(isStaged);
		row.setYfile(line);
		row.setYstate(icon);
	    }
	}
	catch (IOException e) {
	    throw new EventException("An error occurred while reading from console.");
	}
	finally {
	    closeBufferedReader(bufferedReader);
	}
    }

    private void getGitStatus(GitControl head) throws EventException {
	getFiles(head, "git ls-files --others --exclude-standard", false, ICON_UNTRACKED);
	getFiles(head, "git ls-files -m", false, ICON_MODIFIED);
	getFiles(head, "git diff --name-only --staged", true, ICON_STAGED);
    }

    private void initializeGitIgnore(DbContext ctx) throws IOException, EventException {
	BufferedWriter bufferedWriter = null;
	try {
	    File file = new File(".gitignore");
	    if (file.createNewFile()) {
		bufferedWriter = new BufferedWriter(new FileWriter(file));
		bufferedWriter.append("*\n!masken/\n!java/\n!screens/\n!ow*/\n!.gitignore\n!fop.txt\n!masken/*\n!java/*\n!screens/*\n!ow*/*");
		bufferedWriter.close();
		new TextBox(ctx, ".gitignore created", "The file .gitignore was created, as it did not already exist").show();
	    }
	}
	catch (IOException e) {
	    throw new IOException();
	}
	finally {
	    closeBufferedWriter(bufferedWriter);
	}
    }

    private void openGitIgnoreInEditor() {
	EKS.editiere("\".gitignore\"");
    }

    private BufferedReader runSystemCommand(String command) throws IOException {
	SystemCommand systemCommand = new SystemCommand(command, false);
	if (systemCommand.runHidden()) {
	    return new BufferedReader(systemCommand.getOut());
	}
	throw new IOException("Running system command " + command + "faild.");
    }

}
