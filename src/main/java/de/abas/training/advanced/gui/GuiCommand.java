package de.abas.training.advanced.gui;

import de.abas.erp.api.AppContext;
import de.abas.erp.api.commands.CommandFactory;
import de.abas.erp.api.commands.FieldManipulator;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.standard.st.Customers;
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

		final CommandFactory commandFactory = AppContext.createFor(ctx).getCommandFactory();

		final FieldManipulator<Customers> manipulator = commandFactory.getScrParamBuilder(Customers.class);
		manipulator.setField(Customers.META.binteressent, true);
		manipulator.pressButton(Customers.META.start);

		commandFactory.startInfosystem(Customers.class, manipulator);

		return 0;
	}

}
