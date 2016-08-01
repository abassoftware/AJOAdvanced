package de.abas.training.advanced.selection;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.SelectableRecord;
import de.abas.erp.db.TableDescriptor;
import de.abas.erp.db.schema.customer.Customer;
import de.abas.erp.db.schema.customer.CustomerContact;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Example of selecting all groups of database Customer using the
 * TableDescriptor class.
 *
 * @author abas Software AG
 *
 */
public class TableDescriptorCustomerSelection extends AbstractAjoAccess {

	public static void main(String[] args) {
		new TableDescriptorCustomerSelection().runClientProgram(args);
	}

	DbContext ctx;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();

		final TableDescriptor tableDescriptor = new TableDescriptor(0);

		final Query<SelectableRecord> query = ctx.createQuery(SelectionBuilder.create(tableDescriptor).build());
		for (final SelectableRecord selectableRecord : query) {
			if (selectableRecord instanceof CustomerContact) {
				final CustomerContact customerContact = (CustomerContact) selectableRecord;
				ctx.out().println("Customer contact: " + customerContact.getIdno() + " - " + customerContact.getSwd()
						+ " - " + customerContact.getContactPerson());
			} else if (selectableRecord instanceof Customer) {
				final Customer customer = (Customer) selectableRecord;
				ctx.out().println("Customer: " + customer.getIdno() + " - " + customer.getSwd());
			} else {
				ctx.out().println("Could not convert");
			}
		}

		return 0;
	}

}
