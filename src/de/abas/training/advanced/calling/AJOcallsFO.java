package de.abas.training.advanced.calling;

import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.db.DbContext;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * This class shows how to call a FOP within an AJO program.
 *
 * @author abas Software AG
 *
 */
public class AJOcallsFO extends AbstractAjoAccess {

	@Override
	public int run(String[] args) {
		final DbContext ctx = getDbContext();

		// find FO file in files/ASSIGN.VALUE.FO2
		FOe.input("ow1/ASSIGN.VALUE.FO");

		final UserTextBuffer userTextBuffer = BufferFactory.newInstance(true).getUserTextBuffer();
		if (userTextBuffer.isVarDefined("xrvalue")) {
			final double value = userTextBuffer.getDoubleValue("xrvalue");
			ctx.out().println("R7.2: " + value);
			return 0;
		}
		return 1;
	}

}
