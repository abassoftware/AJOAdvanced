package de.abas.training.advanced.infosystemcontrol;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
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
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.ow1.Inventory;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

@EventHandler(head = Inventory.class, row = Inventory.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class InventoryEventHandler {

	@ButtonEventHandler(field = "commitqty", type = ButtonEventType.AFTER)
	public void commitqtyAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if product is selected

		// create new InventoryCounter object

		// display note in status bar
	}

	@FieldEventHandler(field = "datefrom", type = FieldEventType.EXIT)
	public void datefromExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {

	}

	@FieldEventHandler(field = "dateto", type = FieldEventType.EXIT)
	public void datetoExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {

	}

	@FieldEventHandler(field = "product", type = FieldEventType.EXIT)
	public void productExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// set descr and unit

	}

	@FieldEventHandler(field = "qty", type = FieldEventType.VALIDATION)
	public void qtyValidation(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if quantity is negative

	}

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// set screen protection

		// init fields team, teamfrom, teamto, datefrom, dateto and the table

	}

	@ButtonEventHandler(field = "showqty", type = ButtonEventType.AFTER)
	public void showqtyAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if selection contitions are valid

		// load table

	}

	@FieldEventHandler(field = "tcorrqty", type = FieldEventType.EXIT, table = true)
	public void tcorrqtyExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head,
			Inventory.Row currentRow) throws EventException {
		// check if tcorrqty is negative

		// use TextBox to confirm quantity change

		// load current table row's InventoryCounter object

		// edit quantity of InventoryCounter object

		// reload table
	}

	@FieldEventHandler(field = "team", type = FieldEventType.EXIT)
	public void teamExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// initialize teamfrom and teamto

	}

	@FieldEventHandler(field = "teamfrom", type = FieldEventType.EXIT)
	public void teamfromExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {

	}

	@FieldEventHandler(field = "teamto", type = FieldEventType.EXIT)
	public void teamtoExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {

	}

	@FieldEventHandler(field = "team", type = FieldEventType.VALIDATION)
	public void teamValidation(FieldEvent event, ScreenControl screenControl, DbContext ctx, Inventory head)
			throws EventException {
		// check if team is a number between 1 and 10

	}

}
