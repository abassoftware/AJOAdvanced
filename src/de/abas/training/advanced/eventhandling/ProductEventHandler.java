package de.abas.training.advanced.eventhandling;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

/**
 * The ProductEventHandler handles events occurring in a product object
 * (database 2:1).
 *
 * This class shows how to work with FreeTexts.
 *
 * @author abas Software AG
 *
 */
@EventHandler(head = ProductEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class ProductEventHandler {

	@ScreenEventHandler(type = ScreenEventType.VALIDATION)
	public void screenValidation(ScreenEvent event, ScreenControl screenControl, DbContext ctx, ProductEditor head)
			throws EventException {

	}

	/**
	 * This class handles the ButtonAfter logic of the custom button
	 * yisplanchart.
	 *
	 * Variable table row of the button yisplankarte's definition: 2 14
	 * xxyisplanchart - BU3 - 2 1 0 1 0 1 6 0 A A 0 #plan chart # #
	 *
	 * The button yisplanchart opens the infosystem PlanChart using parameters.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yisplanchart", type = ButtonEventType.AFTER)
	public void yisplanchartAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, ProductEditor head)
			throws EventException {

	}

	/**
	 * This class handles the ButtonAfter logic of the custom button yissubbom.
	 *
	 * Variable table row of the button ycallis' definition: 2 15 xxyissubbom -
	 * BU3 - 2 1 0 1 0 1 6 0 A A 0 #sub BOM # #
	 *
	 * The button yissubbom opens the infosystem StructuralBOMTreeView using
	 * parameters.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yissubbom", type = ButtonEventType.AFTER)
	public void yissubbomAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, ProductEditor head)
			throws EventException {

	}

}
