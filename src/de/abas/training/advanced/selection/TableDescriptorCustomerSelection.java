package de.abas.training.advanced.selection;

import de.abas.erp.db.DbContext;
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

		return 0;
	}

}
