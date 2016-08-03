package de.abas.training.advanced.infosystemcontrol;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.abas.erp.api.gui.ButtonSet;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.api.session.OperatorInformation;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi.screen.ScreenControl.StatusBarArea;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.common.type.enums.EnumDialogBox;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.EditorAction;
import de.abas.erp.db.Query;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.infosystem.custom.ow1.Inventory;
import de.abas.erp.db.schema.custom.inventorycounter.InventoryCounter;
import de.abas.erp.db.schema.custom.inventorycounter.InventoryCounterEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.settings.DisplayMode;
import de.abas.erp.db.type.AbasUnit;
import de.abas.erp.db.util.QueryUtil;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.jfop.base.Color;

@EventHandler(head = Inventory.class, row = Inventory.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class InventoryEventHandler {

	private static final Color NOTE = Color.WHITE;
	private static final Color DEFAULT = Color.DEFAULT;
	private static final Color ERROR = Color.getColor("205 90 27");

	@ButtonEventHandler(field = "commitqty", type = ButtonEventType.AFTER)
	public void commitqtyAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if product is selected
		if (head.getProduct() == null) {
			screenControl.moveCursor(head, Inventory.META.product);
			screenControl.setColor(head, Inventory.META.product, DEFAULT, ERROR);
			throw new EventException("Invalid field entries: product cannot be empty", 1);
		}
		screenControl.setColor(head, Inventory.META.product, DEFAULT, DEFAULT);

		// create new InventoryCounter object
		final InventoryCounterEditor editor = ctx.newObject(InventoryCounterEditor.class);
		editor.setSwd("INVENTORY" + getCurrentYear());
		editor.setYteam(head.getTeam());
		editor.setYdate(new AbasDate());
		editor.setYarticle(head.getProduct());
		editor.setYqty(head.getQty());
		editor.setYwarehouseunit(head.getUnit());
		editor.commit();

		// display note in status bar
		screenControl.setColorStatusBar(StatusBarArea.TEXT, NOTE, DEFAULT);
		screenControl.setNote(
				String.format("Data secured: %s - %s", editor.objectId().getIdno(), editor.objectId().getSwd()));
	}

	@FieldEventHandler(field = "datefrom", type = FieldEventType.EXIT)
	public void datefromExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		if (head.getDateto() == null) {
			head.setDateto(head.getDatefrom());
		}
	}

	@FieldEventHandler(field = "dateto", type = FieldEventType.EXIT)
	public void datetoExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		if (head.getDatefrom() == null) {
			head.setDatefrom(head.getDateto());
		}
	}

	@FieldEventHandler(field = "product", type = FieldEventType.EXIT)
	public void productExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// set descr and unit
		if (head.getProduct() == null) {
			head.setDescr("");
			head.setUnit(AbasUnit.UNITS.valueOf(21));
		} else {
			head.setDescr(head.getProduct().getDescrOperLang());
			head.setUnit(head.getProduct().getSU());
			screenControl.setColor(head, Inventory.META.product, DEFAULT, DEFAULT);
		}
	}

	@FieldEventHandler(field = "qty", type = FieldEventType.VALIDATION)
	public void qtyValidation(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if quantity is negative
		if (head.getQty().compareTo(BigDecimal.ZERO) == -1) {
			screenControl.setColor(head, Inventory.META.qty, DEFAULT, ERROR);
			throw new EventException("Invalid field entries: quantity cannot be negative", 1);
		}
		screenControl.setColor(head, Inventory.META.qty, DEFAULT, DEFAULT);
	}

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// set screen protection
		final String operatorCode = new OperatorInformation(ctx).getCode();
		if (!operatorCode.equals("sy")) {
			screenControl.setProtection(head, Inventory.META.showqty, true);
			screenControl.setColorStatusBar(StatusBarArea.TEXT, ERROR, DEFAULT);
			screenControl.setNote("Selecting and displaying created data in table area is not permitted.");
		}
		screenControl.setProtection(head, Inventory.META.descr, true);
		// init fields team, teamfrom, teamto, datefrom, dateto and the table
		head.setTeam(1);
		head.setTeamfrom(1);
		head.setTeamto(1);
		head.setDatefrom(new AbasDate());
		head.setDateto(new AbasDate());
		head.table().clear();
	}

	@ButtonEventHandler(field = "showqty", type = ButtonEventType.AFTER)
	public void showqtyAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if selection contitions are valid
		if (head.getTeamfrom() > head.getTeamto()) {
			screenControl.setColor(head, Inventory.META.teamfrom, DEFAULT, ERROR);
			screenControl.setColor(head, Inventory.META.teamto, DEFAULT, ERROR);
			throw new EventException("Invalid condition: teamto must be greater than teamfrom", 1);
		}
		if (head.getDateto().compareTo(head.getDatefrom()) == -1) {
			screenControl.setColor(head, Inventory.META.datefrom, DEFAULT, ERROR);
			screenControl.setColor(head, Inventory.META.dateto, DEFAULT, ERROR);
			throw new EventException("Invalid condition: dateto must be greater than datefrom", 1);
		}
		screenControl.setColor(head, Inventory.META.teamfrom, DEFAULT, DEFAULT);
		screenControl.setColor(head, Inventory.META.teamto, DEFAULT, DEFAULT);
		screenControl.setColor(head, Inventory.META.datefrom, DEFAULT, DEFAULT);
		screenControl.setColor(head, Inventory.META.dateto, DEFAULT, DEFAULT);

		// load table
		loadTable(ctx, head);
	}

	@FieldEventHandler(field = "tcorrqty", type = FieldEventType.EXIT, table = true)
	public void tcorrqtyExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head,
			Inventory.Row currentRow) throws EventException {
		// check if tcorrqty is negative
		if (currentRow.getTcorrqty().compareTo(BigDecimal.ZERO) == -1) {
			screenControl.moveCursor(currentRow, Inventory.Row.META.tcorrqty);
			screenControl.setColor(currentRow, Inventory.Row.META.tcorrqty, DEFAULT, ERROR);
			throw new EventException("Invalid field entry: tcorrqty cannot be negative", 1);
		}
		screenControl.setColor(currentRow, Inventory.Row.META.tcorrqty, DEFAULT, DEFAULT);
		// use TextBox to confirm quantity change
		final TextBox textBox = new TextBox(ctx, "Really?",
				"Do you really want to update the quantity to " + currentRow.getTcorrqty() + "?");
		textBox.setButtons(ButtonSet.NO_YES);
		final EnumDialogBox answer = textBox.show();
		if (answer.equals(EnumDialogBox.Yes)) {
			// load current table row's InventoryCounter object
			final InventoryCounter inventoryCounter = QueryUtil.getFirst(ctx,
					SelectionBuilder.create(InventoryCounter.class)
							.add(Conditions.eq(InventoryCounter.META.yteam, currentRow.getTteam()))
							.add(Conditions.eq(InventoryCounter.META.ydate, currentRow.getTdate()))
							.add(Conditions.eq(InventoryCounter.META.yarticle, currentRow.getTproduct()))
							.add(Conditions.eq(InventoryCounter.META.yqty, currentRow.getTqty())).build());
			// edit quantity of InventoryCounter object
			final InventoryCounterEditor editor = inventoryCounter.createEditor();
			try {
				editor.open(EditorAction.UPDATE);
				editor.setYqty(currentRow.getTcorrqty());
				editor.commit();
			} catch (final CommandException e) {
				if ((editor != null) && editor.active()) {
					editor.abort();
				}
				throw new EventException("Error: Could not save changes to " + inventoryCounter.getId()
						+ ".\nAn error occurred: " + e.getMessage(), 1);
			}
		}
		// reload table
		loadTable(ctx, head);
	}

	@FieldEventHandler(field = "team", type = FieldEventType.EXIT)
	public void teamExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// initialize teamfrom and teamto
		head.setTeamfrom(head.getTeam());
		head.setTeamto(head.getTeam());
	}

	@FieldEventHandler(field = "teamfrom", type = FieldEventType.EXIT)
	public void teamfromExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		if (head.getTeamto() == 0) {
			head.setTeamto(head.getTeamfrom());
		}
	}

	@FieldEventHandler(field = "teamto", type = FieldEventType.EXIT)
	public void teamtoExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		if (head.getTeamfrom() == 0) {
			head.setTeamfrom(head.getTeamto());
		}
	}

	@FieldEventHandler(field = "team", type = FieldEventType.VALIDATION)
	public void teamValidation(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if team is a number between 1 and 10
		if ((head.getTeam() < 1) || (head.getTeam() > 10)) {
			screenControl.setColor(head, Inventory.META.team, DEFAULT, ERROR);
			throw new EventException("Invalid field entries: Team number has to be between 1 and 10", 1);
		}
		screenControl.setColor(head, Inventory.META.team, DEFAULT, DEFAULT);
	}

	private int getCurrentYear() {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(new AbasDate().toDate());
		return calendar.get(Calendar.YEAR);
	}

	private void loadTable(DbContext ctx, Inventory head) {
		head.table().clear();
		ctx.getSettings().setDisplayMode(DisplayMode.DISPLAY);
		final Query<InventoryCounter> query = ctx.createQuery(SelectionBuilder.create(InventoryCounter.class)
				.add(Conditions.between(InventoryCounter.META.yteam, head.getTeamfrom(), head.getTeamto()))
				.add(Conditions.between(InventoryCounter.META.ydate, head.getDatefrom(), head.getDateto())).build());
		int counter = 0;
		for (final InventoryCounter inventoryCounter : query) {
			final Inventory.Row row = head.table().appendRow();
			row.setTitem(counter++);
			row.setTdate(inventoryCounter.getYdate());
			row.setTteam(inventoryCounter.getYteam());
			row.setTproduct(inventoryCounter.getYarticle());
			row.setTqty(inventoryCounter.getYqty());
			row.setString(Inventory.Row.META.tunit, inventoryCounter.getString(InventoryCounter.META.ywarehouseunit));
			row.setTarticledescr(inventoryCounter.getYarticle().getDescrOperLang());
			row.setTpurchprice(inventoryCounter.getYarticle().getPurchPrice());
			row.setTwarehousevalue(inventoryCounter.getYqty().multiply(inventoryCounter.getYarticle().getPurchPrice()));
		}
	}

}
