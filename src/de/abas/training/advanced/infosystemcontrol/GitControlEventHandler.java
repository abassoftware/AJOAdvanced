package de.abas.training.advanced.infosystemcontrol;

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

/**
 * Infosystem for usage of git commands in $MANDANDIR.
 *
 * @author abas Software AG
 *
 */
@EventHandler(head = GitControl.class, row = GitControl.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class GitControlEventHandler {

	private final String ICON_STAGED = "icon:ball_green";
	private final String ICON_MODIFIED = "icon:ball_orange";
	private final String ICON_UNTRACKED = "icon:ball_red";

	private ScreenControl screenControl = null;

	/**
	 * Start button after logic. Gets git status after pressing start button.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		this.screenControl = screenControl;
		getGitStatus(head);
	}

	/**
	 * Button after logic of yadd. Executes git add for all selected files in table.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yadd", type = ButtonEventType.AFTER)
	public void yaddAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		this.screenControl = screenControl;
		final Iterable<Row> rows = head.table().getRows();
		for (final Row row : rows) {
			addFile(row);
		}
	}

	/**
	 * Button after logic of yaddall. Executes git add --all, which adds all modified files (but not the untracked ones).
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yaddall", type = ButtonEventType.AFTER)
	public void yaddallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		this.screenControl = screenControl;
		addAllFiles(head);
	}

	/**
	 * Button after logic of ycheckall. Checks all ystaged checkboxes in table.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "ycheckall", type = ButtonEventType.AFTER)
	public void ycheckallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		final Iterable<Row> rows = head.table().getRows();
		for (final Row row : rows) {
			row.setYstaged(true);
		}
	}

	/**
	 * Button after logic of ycommit. Commits staged changes, needing a commit message and user name and email of committer.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "ycommit", type = ButtonEventType.AFTER)
	public void ycommitAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		this.screenControl = screenControl;
		final String commitMessage = getCommitMessage(head);
		setUsername(ctx);
		setEmail(ctx);
		commitChanges(ctx, head, "git commit -m \"" + commitMessage + "\"", commitMessage);
	}

	/**
	 * Button after logic of ycommitall. Automatically adds and commits all modified (already tracked) files, needing a commit message and user name and email of committer.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "ycommitall", type = ButtonEventType.AFTER)
	public void ycommitallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		this.screenControl = screenControl;
		final EnumDialogBox box =
				new TextBox(ctx, "commiting all changes", "Are you sure you want to commit all changes in all files?\nThis does not include untracked files.").setButtons(
						ButtonSet.YES_NO).show();
		if (box.equals(EnumDialogBox.Yes)) {
			final String commitMessage = getCommitMessage(head);
			setUsername(ctx);
			setEmail(ctx);
			commitChanges(ctx, head, "git commit -a -m \"" + commitMessage + "\"", commitMessage);
		}
	}

	/**
	 * Button after logic of ygitignore. Creates .gitignore file if not already existent. Opens .gitignore file in editor.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "ygitignore", type = ButtonEventType.AFTER)
	public void ygitignoreAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		try {
			createGitIgnore(ctx);
			openGitIgnoreInEditor();
		}
		catch (final SecurityException e) {
			throw new EventException("An error occurred while accessing .gitignore. Your user rights do not seem to be sufficient.");
		}
		catch (final IOException e) {
			throw new EventException("An error occurred while editing .gitignore.");
		}
	}

	/**
	 * Button after logic of yinit. Executes git init.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yinit", type = ButtonEventType.AFTER)
	public void yinitAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = runSystemCommand("cd $MANDANTDIR && git init");
			final String console = getConsole(bufferedReader);
			new TextBox(ctx, "git init", console).show();
		}
		catch (final IOException e) {
			throw new EventException(e.getMessage());
		}
		finally {
			closeBufferedReader(bufferedReader);
		}
	}

	/**
	 * Button after logic of ystatus. Gets git status.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "ystatus", type = ButtonEventType.AFTER)
	public void ystatusAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		this.screenControl = screenControl;
		getGitStatus(head);
	}

	/**
	 * Button after logic of yuncheckall. Unchecks all ystaged checkboxes in table.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yuncheckall", type = ButtonEventType.AFTER)
	public void yuncheckallAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, GitControl head) throws EventException {
		final Iterable<Row> rows = head.table().getRows();
		for (final Row row : rows) {
			row.setYstaged(false);
		}
	}

	/**
	 * Executes git add --all and updates Infosystem table.
	 *
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void addAllFiles(GitControl head) throws EventException {
		try {
			runSystemCommand("git add --all");
			final Iterable<Row> rows = head.table().getRows();
			for (final Row row : rows) {
				updateInfosystemTable(row);
			}
		}
		catch (final IOException e) {
			throw new EventException(e.getMessage());
		}
	}

	/**
	 * Executes git add for each staged table row and updates Infosystem table.
	 *
	 * @param row The current table row.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void addFile(Row row) throws EventException {
		if (row.getYstaged()) {
			try {
				runSystemCommand("git add " + row.getYfile());
				updateInfosystemTable(row);
			}
			catch (final IOException e) {
				throw new EventException(e.getMessage());
			}
		}
	}

	/**
	 * Closes BufferedReader instance.
	 *
	 * @param bufferedReader The BufferedReader instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void closeBufferedReader(BufferedReader bufferedReader) throws EventException {
		if (bufferedReader != null) {
			try {
				bufferedReader.close();
			}
			catch (final IOException e) {
				throw new EventException("An error occurred while trying to close BufferedReader instance.");
			}
		}
	}

	/**
	 * Closes BufferedWriter instance.
	 *
	 * @param bufferedWriter The BufferedWriter instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void closeBufferedWriter(BufferedWriter bufferedWriter) throws EventException {
		if (bufferedWriter != null) {
			try {
				bufferedWriter.close();
			}
			catch (final IOException e) {
				throw new EventException("An error occurred while trying to close BufferedWriter instance.");
			}
		}
	}

	/**
	 * Executes command, gets git status, resets commit message and displays text box.
	 *
	 * @param ctx The database context.
	 * @param head The GitControl instance.
	 * @param command The command to execute.
	 * @param commitMessage The commit message.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void commitChanges(DbContext ctx, GitControl head, String command, String commitMessage) throws EventException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = runSystemCommand(command);
			final String console = getConsole(bufferedReader);
			getGitStatus(head);
			resetCommitMessage(head);
			showSuccessfulCommitTextBox(ctx, console);
		}
		catch (final IOException e) {
			throw new EventException(e.getMessage());
		}
		finally {
			closeBufferedReader(bufferedReader);
		}
	}

	/**
	 * Creates .gitignore if .gitignore does not exist and initializes it with default values..
	 *
	 * @param ctx The database context.
	 * @throws IOException Exception thrown if something went wrong with the BufferedWriter.
	 * @throws EventException Exception thrown if an error occurs.
	 */
	private void createGitIgnore(DbContext ctx) throws IOException, EventException {
		boolean newFileFlag = false;
		newFileFlag = initializeGitIgnore(ctx, "*\n!.gitignore\n!fop.txt\n!/masken/\n!masken/**\n!/java/\n!java/mandant.classpath\n!/java/projects/\n!java/projects/**\n!/screens/\n!screens/**\n!/ow*/\n!ow*/**");
		if (newFileFlag) {
			new TextBox(ctx, ".gitignore created", "The file .gitignore was created, as it did not already exist").show();
		}
	}

	/**
	 * Gets the commit message. If the commit message is empty, the cursor is moved to field ycommitmessage and an error message is displayed.
	 *
	 * @param head The GitControl instance.
	 * @return The commit message.
	 * @throws EventException Thrown if an error occurs.
	 */
	private String getCommitMessage(GitControl head) throws EventException {
		final String commitMessage = head.getYcommitmessage();
		if (head.getYcommitmessage().isEmpty()) {
			screenControl.moveCursor(head, GitControl.META.ycommitmessage);
			throw new EventException("The commit message cannot be left empty.");
		}
		return commitMessage;
	}

	/**
	 * Gets the console output.
	 *
	 * @param bufferedReader The BufferedReader instance.
	 * @return The console output.
	 * @throws IOException Thrown if an error occurs.
	 */
	private String getConsole(BufferedReader bufferedReader) throws IOException {
		String line = "";
		String message = "";
		while ((line = bufferedReader.readLine()) != null) {
			message = message + line + "\n";
		}
		bufferedReader.close();
		return message;
	}

	/**
	 * Gets staged, modified and untracked files and sets icon and screen protection accordingly.
	 *
	 * @param head The GitControl instance.
	 * @param command The command to execute.
	 * @param isStaged Whether or not the files are staged already.
	 * @param icon The icon to set.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void getFiles(GitControl head, String command, boolean isStaged, String icon) throws EventException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = runSystemCommand(command);
			loadInfosystemTable(head, isStaged, icon, bufferedReader);
		}
		catch (final IOException e) {
			throw new EventException("An error occurred while reading from console.");
		}
		finally {
			closeBufferedReader(bufferedReader);
		}
	}

	/**
	 * Gets git status. Resets table and displays all files in table.
	 *
	 * @param head The GitControl instance.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void getGitStatus(GitControl head) throws EventException {
		head.table().clear();
		getFiles(head, "git ls-files --others --exclude-standard", false, ICON_UNTRACKED);
		getFiles(head, "git ls-files -m", false, ICON_MODIFIED);
		getFiles(head, "git diff --name-only --staged", true, ICON_STAGED);
	}

	/**
	 * Overrides initializeGitIgnore(String path, String fileContent) for path being the current directory.
	 *
	 * @param fileContent The content of .gitignore file.
	 * @return Whether or not new .gitignore file was created.
	 * @throws EventException Thrown if an error occurs.
	 * @throws IOException Thrown if something went from with the BufferdWriter instance.
	 */
	private boolean initializeGitIgnore(DbContext ctx, String fileContent) throws EventException, IOException {
		return initializeGitIgnore("", fileContent);
	}

	/**
	 * Creates .gitignore if not existent and initializes it.
	 *
	 * @param path The path to .gitignore file.
	 * @param fileContent The content of .gitignore file.
	 * @return Whether or not new .gitignore file was created.
	 * @throws EventException Thrown if an error occurs.
	 * @throws IOException Thrown if something went from with the BufferdWriter instance.
	 */
	private boolean initializeGitIgnore(String path, String fileContent) throws IOException, EventException {
		BufferedWriter bufferedWriter = null;
		boolean newFileFlag = false;
		try {
			final File file = new File(path + ".gitignore");
			if (file.createNewFile()) {
				bufferedWriter = new BufferedWriter(new FileWriter(file));
				bufferedWriter.append(fileContent);
				bufferedWriter.close();
				newFileFlag = true;
			}
			return newFileFlag;
		}
		catch (final IOException e) {
			throw new IOException();
		}
		finally {
			closeBufferedWriter(bufferedWriter);
		}
	}

	/**
	 * Loads the infosystem table.
	 *
	 * @param head The GitControl instance.
	 * @param isStaged Whether or not the file is already staged.
	 * @param icon The icon.
	 * @param bufferedReader The BufferedReader instance.
	 * @throws IOException Thrown if an error occurs.
	 */
	private void loadInfosystemTable(GitControl head, boolean isStaged, String icon, BufferedReader bufferedReader) throws IOException {
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			final Row row = head.table().appendRow();
			row.setYstaged(false);
			row.setYfile(line);
			row.setYstate(icon);
			setProtectionForStagedFiles(isStaged, row);
		}
	}

	/**
	 * Opens .gitignore file in editor.
	 */
	private void openGitIgnoreInEditor() {
		EKS.editiere("\".gitignore\"");
	}

	/**
	 * Resets the commit message.
	 *
	 * @param head The GitControl instance.
	 */
	private void resetCommitMessage(GitControl head) {
		head.setYcommitmessage("");
	}

	/**
	 * Runs a system command.
	 *
	 * @param command The command to execute.
	 * @return The output stream of executed command as BufferdReader instance.
	 * @throws IOException Thrown if an error occurs.
	 */
	private BufferedReader runSystemCommand(String command) throws IOException {
		final SystemCommand systemCommand = new SystemCommand(command, false);
		if (systemCommand.runHidden()) {
			return new BufferedReader(systemCommand.getOut());
		}
		throw new IOException("Running system command " + command + " failed.");
	}

	/**
	 * Checks whether email is valid using regex. Sets email for git.
	 *
	 * @param ctx The database context.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void setEmail(DbContext ctx) throws EventException {
		String email = FO.lesen(new String[] { "Please enter your email address:" });
		while (!email.matches("^[A-Za-z0-9\\.\\-_]+@[A-Za-z0-9\\.\\-_]+\\.[a-z]+$")) {
			new TextBox(ctx, "Invalid email address", "Please enter a valid email address:\n e.g.: john.doe@domain.locale").show();
			email = FO.lesen(new String[] { "Please enter your email address:" });
		}
		try {
			runSystemCommand("git config --global user.email " + email);
		}
		catch (final IOException e) {
			throw new EventException(e.getMessage());
		}
	}

	/**
	 * Sets protection for staged file in table row.
	 *
	 * @param isStaged Whether or not the file is staged.
	 * @param row The current table row.
	 */
	private void setProtectionForStagedFiles(boolean isStaged, Row row) {
		if (isStaged) {
			screenControl.setProtection(row, GitControl.Row.META.ystaged, true);
		}
	}

	/**
	 * Checks whether user name is valid using regex. Sets user name for git.
	 *
	 * @param ctx The database context.
	 * @throws EventException Thrown if an error occurs.
	 */
	private void setUsername(DbContext ctx) throws EventException {
		String username = FO.lesen(new String[] { "Please enter your name:" });
		while (!username.matches("^[A-ZÄÖÜ][a-zäöüß]+[ ][A-ZÄÖÜa-zäöüß ]*[A-ZÄÖÜ][a-zäöüß]+$")) {
			new TextBox(ctx, "Invalid name", "Your name needs to consist of your first and last name and it is case sensitive:\n e.g.: John Doe").show();
			username = FO.lesen(new String[] { "Please enter your name:" });
		}
		try {
			runSystemCommand("git config --global user.name " + username);
		}
		catch (final IOException e) {
			throw new EventException(e.getMessage());
		}
	}

	/**
	 * Shows text box if commit was successful.
	 *
	 * @param ctx The database context.
	 * @param console The console output.
	 */
	private void showSuccessfulCommitTextBox(DbContext ctx, String console) {
		if (console.length() < 132000) {
			new TextBox(ctx, "git commit", console).show();
		}
		else {
			new TextBox(ctx, "git commit", "commit successful").show();
		}
	}

	/**
	 * Updates ystaged, protection and icon in current table row.
	 *
	 * @param row The current table row.
	 */
	private void updateInfosystemTable(Row row) {
		row.setYstaged(false);
		screenControl.setProtection(row, GitControl.Row.META.ystaged, true);
		row.setYstateicon(ICON_STAGED);
	}

}
