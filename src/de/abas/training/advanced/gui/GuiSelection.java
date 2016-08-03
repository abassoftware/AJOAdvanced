package de.abas.training.advanced.gui;

import de.abas.erp.db.DbContext;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Shows how to use a GUI selection within an AJO program.
 *
 * @author abas Software AG
 *
 */
public class GuiSelection extends AbstractAjoAccess {

	DbContext ctx;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();

		return 0;
	}

}
