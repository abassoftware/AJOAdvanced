package de.abas.training.advanced.selection;

import de.abas.erp.db.DbContext;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Example for selecting various groups of Sales database using the
 * TableDescriptor class.
 *
 * @author abas Software AG
 *
 */
public class TableDescriptorSalesSelection extends AbstractAjoAccess {

	public static void main(String[] args) {
		new TableDescriptorSalesSelection().runClientProgram(args);
	}

	DbContext ctx;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();

		return 0;
	}

}
