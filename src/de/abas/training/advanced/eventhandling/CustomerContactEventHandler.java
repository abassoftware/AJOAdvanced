package de.abas.training.advanced.eventhandling;

import de.abas.erp.axi.event.EventHandler;
import de.abas.erp.axi.event.ObjectEventHandler;
import de.abas.erp.axi.event.listener.FieldListenerAdapter;
import de.abas.erp.db.schema.customer.CustomerContactEditor;

/**
 * Shows how to use original AXI EventHandler which enables usage of class
 * variables. In case the field ysurname was modified the fields addr, descr and
 * contactPerson are highlighted.
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

	}

	/**
	 * Constructor
	 */
	public CustomerContactEventHandler() {
		super(CustomerContactEditor.class);
	}

	@Override
	protected void configureEventHandler(ObjectEventHandler<CustomerContactEditor> objectHandler) {
		super.configureEventHandler(objectHandler);
	}

}