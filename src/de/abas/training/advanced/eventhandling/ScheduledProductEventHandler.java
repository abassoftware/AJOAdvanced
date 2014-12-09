package de.abas.training.advanced.eventhandling;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.common.type.enums.EnumSchedulingMode;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.jfop.base.Color;

/**
 * The ProductEventHandler handles events occurring in a product object
 * (database 2:1).
 *
 * This class shows how to color fields according to field values.
 *
 * @author abas Software AG
 *
 */
@EventHandler(head = ProductEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class ScheduledProductEventHandler {

	/**
	 * On FieldExit of schedulingMode the background of some fields is set to
	 * light green if the schedulingMode is requirement related.
	 *
	 * @param event The event that occurred.
	 * @param screenControl The ScreenConrol instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurred.
	 */
	@FieldEventHandler(field = "schedulingMode", type = FieldEventType.EXIT)
	public void schedulingModeExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, ProductEditor head) throws EventException {
		EnumSchedulingMode schedulingMode = head.getSchedulingMode();

		// colors background of scheduling relevant fields if scheduling mode is
		// set to requirement related
		if (schedulingMode.equals(EnumSchedulingMode.RequirementRelated)) {
			screenControl.setColor(head, ProductEditor.META.minStock, Color.BLACK, Color.LIGHT_GREEN);
			screenControl.setColor(head, ProductEditor.META.batchGrpPeriod, Color.BLACK, Color.LIGHT_GREEN);
			screenControl.setColor(head, ProductEditor.META.batchSize, Color.BLACK, Color.LIGHT_GREEN);
		}
		else {
			screenControl.setColor(head, ProductEditor.META.minStock, Color.DEFAULT, Color.DEFAULT);
			screenControl.setColor(head, ProductEditor.META.batchGrpPeriod, Color.DEFAULT, Color.DEFAULT);
			screenControl.setColor(head, ProductEditor.META.batchSize, Color.DEFAULT, Color.DEFAULT);
		}
	}

}
