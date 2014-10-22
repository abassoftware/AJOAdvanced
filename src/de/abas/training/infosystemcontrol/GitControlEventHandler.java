package de.abas.training.infosystemcontrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.abas.eks.jfop.remote.EKS;
import de.abas.eks.jfop.remote.FO;
import de.abas.erp.api.gui.ButtonSet;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.api.system.SystemCommand;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.common.type.enums.EnumDialogBox;
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

    private ScreenControl screenControl = null;

    @ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
    public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	this.screenControl = screenControl;
	getGitStatus(head);
    }

    @ButtonEventHandler(field = "yadd", type = ButtonEventType.AFTER)
    public void yaddAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	this.screenControl = screenControl;
	Iterable<Row> rows = head.table().getRows();
	for (Row row : rows) {
	    addFile(row);
	}
    }

    @ButtonEventHandler(field = "yaddall", type = ButtonEventType.AFTER)
    public void yaddallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	this.screenControl = screenControl;
	addAllFiles(head);
    }

    @ButtonEventHandler(field = "ycheckall", type = ButtonEventType.AFTER)
    public void ycheckallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	Iterable<Row> rows = head.table().getRows();
	for (Row row : rows) {
	    row.setYstaged(true);
	}
    }

    @ButtonEventHandler(field = "ycommit", type = ButtonEventType.AFTER)
    public void ycommitAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	this.screenControl = screenControl;
	String commitMessage = getCommitMessage(head);
	setUsername(ctx);
	setEmail(ctx);
	commitChanges(ctx, head, "git commit -m \"" + commitMessage + "\"", commitMessage);
    }

    @ButtonEventHandler(field = "ycommitall", type = ButtonEventType.AFTER)
    public void ycommitallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	this.screenControl = screenControl;
	EnumDialogBox box = new TextBox(ctx, "commiting all changes", "Are you sure you want to commit all changes in all files?\nThis does not include untracked files.")
		.setButtons(ButtonSet.YES_NO).show();
	if (box.equals(EnumDialogBox.Yes)) {
	    String commitMessage = getCommitMessage(head);
	    setUsername(ctx);
	    setEmail(ctx);
	    commitChanges(ctx, head, "git commit -a -m \"" + commitMessage + "\"", commitMessage);
	}
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
	this.screenControl = screenControl;
	getGitStatus(head);
    }

    @ButtonEventHandler(field = "yuncheckall", type = ButtonEventType.AFTER)
    public void yuncheckallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
	Iterable<Row> rows = head.table().getRows();
	for (Row row : rows) {
	    row.setYstaged(false);
	}
    }

    private void addAllFiles(GitControl head) throws EventException {
	try {
	    runSystemCommand("git add --all");
	    Iterable<Row> rows = head.table().getRows();
	    for (Row row : rows) {
		updateInfosystemTable(row);
	    }
	}
	catch (IOException e) {
	    throw new EventException(e.getMessage());
	}
    }

    private void addFile(Row row) throws EventException {
	if (row.getYstaged()) {
	    try {
		runSystemCommand("git add " + row.getYfile());
		updateInfosystemTable(row);
	    }
	    catch (IOException e) {
		throw new EventException(e.getMessage());
	    }
	}
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

    private void commitChanges(DbContext ctx, GitControl head, String command, String commitMessage) throws EventException {
	BufferedReader bufferedReader = null;
	try {
	    bufferedReader = runSystemCommand(command);
	    String console = getConsole(bufferedReader);
	    getGitStatus(head);
	    resetCommitMessage(head);
	    showSuccessfulCommitTextBox(ctx, console);
	}
	catch (IOException e) {
	    throw new EventException(e.getMessage());
	}
	finally {
	    closeBufferedReader(bufferedReader);
	}
    }

    private String getCommitMessage(GitControl head) throws EventException {
	String commitMessage = head.getYcommitmessage();
	if (head.getYcommitmessage().isEmpty()) {
	    screenControl.moveCursor(head, GitControl.META.ycommitmessage);
	    throw new EventException("The commit message cannot be left empty.");
	}
	return commitMessage;
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
	    loadInfosystemTable(head, isStaged, icon, bufferedReader);
	}
	catch (IOException e) {
	    throw new EventException("An error occurred while reading from console.");
	}
	finally {
	    closeBufferedReader(bufferedReader);
	}
    }

    private void getGitStatus(GitControl head) throws EventException {
	head.table().clear();
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
		bufferedWriter.append("*\n!masken/\n!java/\n!screens/\n!ow*/\n!.gitignore\n!fop.txt\n!masken/**\n!java/**\n!screens/**\n!ow*/**\njava/jfopserver");
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

    private void loadInfosystemTable(GitControl head, boolean isStaged, String icon, BufferedReader bufferedReader) throws IOException {
	String line = "";
	while ((line = bufferedReader.readLine()) != null) {
	    Row row = head.table().appendRow();
	    row.setYstaged(false);
	    row.setYfile(line);
	    row.setYstate(icon);
	    setProtectionForStagedFiles(isStaged, row);
	}
    }

    private void openGitIgnoreInEditor() {
	EKS.editiere("\".gitignore\"");
    }

    private void resetCommitMessage(GitControl head) {
	head.setYcommitmessage("");
    }

    private BufferedReader runSystemCommand(String command) throws IOException {
	SystemCommand systemCommand = new SystemCommand(command, false);
	if (systemCommand.runHidden()) {
	    return new BufferedReader(systemCommand.getOut());
	}
	throw new IOException("Running system command " + command + " failed.");
    }

    private void setEmail(DbContext ctx) throws EventException {
	String email = FO.lesen(new String[] { "Please enter your email address:" });
	while (!email.matches("^[A-Za-z0-9\\.\\-_]+@[A-Za-z0-9\\.\\-_]+\\.[a-z]+$")) {
	    new TextBox(ctx, "Invalid email address", "Please enter a valid email address:\n e.g.: john.doe@domain.locale").show();
	    email = FO.lesen(new String[] { "Please enter your email address:" });
	}
	try {
	    runSystemCommand("git config --global user.email " + email);
	}
	catch (IOException e) {
	    throw new EventException(e.getMessage());
	}
    }

    private void setProtectionForStagedFiles(boolean isStaged, Row row) {
	if (isStaged) {
	    screenControl.setProtection(row, GitControl.Row.META.ystaged, true);
	}
    }

    private void setUsername(DbContext ctx) throws EventException {
	String username = FO.lesen(new String[] { "Please enter your name:" });
	while (!username.matches("^[A-ZÄÖÜ][a-zäöüß]+[ ][A-ZÄÖÜa-zäöüß ]*[A-ZÄÖÜ][a-zäöüß]+$")) {
	    new TextBox(ctx, "Invalid name", "Your name needs to consist of your first and last name and it is case sensitive:\n e.g.: John Doe").show();
	    username = FO.lesen(new String[] { "Please enter your name:" });
	}
	try {
	    runSystemCommand("git config --global user.name " + username);
	}
	catch (IOException e) {
	    throw new EventException(e.getMessage());
	}
    }

    private void showSuccessfulCommitTextBox(DbContext ctx, String console) {
	if (console.length() < 132000) {
	    new TextBox(ctx, "git commit", console).show();
	}
	else {
	    new TextBox(ctx, "git commit", "commit successful").show();
	}
    }

    private void updateInfosystemTable(Row row) {
	row.setYstaged(false);
	screenControl.setProtection(row, GitControl.Row.META.ystaged, true);
	row.setYstateicon(ICON_STAGED);
    }

}
