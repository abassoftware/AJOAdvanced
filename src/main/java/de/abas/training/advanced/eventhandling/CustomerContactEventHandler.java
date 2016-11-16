package de.abas.training.advanced.eventhandling;

import de.abas.erp.axi.event.EditableFieldEvent;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.event.EventHandler;
import de.abas.erp.axi.event.FieldEvent;
import de.abas.erp.axi.event.ObjectEventHandler;
import de.abas.erp.axi.event.listener.FieldListenerAdapter;
import de.abas.erp.db.schema.customer.CustomerContactEditor;
import de.abas.jfop.base.Color;

/**
 * Shows how to use original AXI EventHandler which enables usage of class variables. In case the field ysurname was modified the fields addr, descr and contactPerson are
 * highlighted.
 *
 * @author abas Software AG
 *
 */
public class CustomerContactEventHandler extends EventHandler<CustomerContactEditor> {

	/**
	 * Inner class HeadFieldListener
	 *
	 * @author abas Software AG
	 *
	 */
	class HeadFieldListener extends FieldListenerAdapter<CustomerContactEditor> {

		@Override
		public void fill(EditableFieldEvent<CustomerContactEditor> event) throws EventException {
			super.fill(event);
			getContext().out().println("Old value: " + event.getSourceRecord().getYsurname());
			fieldOldValue = event.getSourceRecord().getYsurname();
		}

		@Override
		public void validation(FieldEvent<CustomerContactEditor> event) throws EventException {
			super.validation(event);
			getContext().out().println("Old value: " + fieldOldValue);
			fieldNewValue = event.getSourceRecord().getYsurname();
			getContext().out().println("New value: " + fieldNewValue);
			if (!fieldOldValue.equals(fieldNewValue)) {
				getScreenCtrl().setColor(event.getSourceRecord(), CustomerContactEditor.META.addr, Color.DEFAULT, Color.RED);
				getScreenCtrl().setColor(event.getSourceRecord(), CustomerContactEditor.META.descr, Color.DEFAULT, Color.RED);
				getScreenCtrl().setColor(event.getSourceRecord(), CustomerContactEditor.META.contactPerson, Color.DEFAULT, Color.RED);
			}
		}
	}

	String fieldOldValue = "";
	String fieldNewValue = "";

	/**
	 * Constructor
	 */
	public CustomerContactEventHandler() {
		super(CustomerContactEditor.class);
	}

	@Override
	protected void configureEventHandler(ObjectEventHandler<CustomerContactEditor> objectHandler) {
		super.configureEventHandler(objectHandler);
		objectHandler.addListener(new HeadFieldListener());
	}

}