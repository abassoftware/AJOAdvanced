package de.abas.training.advanced.transaction;

import de.abas.erp.db.DbContext;
import de.abas.training.advanced.common.AbstractAjoAccess;

public class CreateNewProductsFromXMLTransaction extends AbstractAjoAccess {

	private DbContext dbContext = null;

	@Override
	public int run(String[] args) {
		dbContext = getDbContext();

		// adding jdom-2-0-5.jar to build path and enter in mandant.classpath

		return 0;
	}
}
