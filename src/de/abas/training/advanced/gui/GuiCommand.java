package de.abas.training.advanced.gui;

import de.abas.erp.db.DbContext;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Shows how to preselect and display an Infosystem.
 *
 * @author abas Software AG
 *
 */
public class GuiCommand extends AbstractAjoAccess {

	DbContext ctx;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();

		return 0;
	}

}
