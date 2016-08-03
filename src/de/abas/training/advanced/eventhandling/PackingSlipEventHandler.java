package de.abas.training.advanced.eventhandling;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.sales.PackingSlipEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

/**
 * The PackingSlipEventHandler handles events occurring in a packing slip object
 * (database 3:23).
 *
 * This class calculates the warranty date on the basis of the warranty period
 * as specified in the product master files.
 *
 * @author abas Software AG
 *
 */
@EventHandler(head = PackingSlipEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class PackingSlipEventHandler {

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, PackingSlipEditor head)
			throws EventException {

	}

}