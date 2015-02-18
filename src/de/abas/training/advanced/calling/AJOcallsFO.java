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
		DbContext ctx = getDbContext();

		// ..!interpreter english translate noabbrev
		// ..*****************************************************************************
		// .. FOP-Name : FOP.ASSIGN.VALUE
		// .. Date : Oct 20, 2014
		// .. Author : abas Software AG
		// .. Responsible :
		// .. Supervisor :
		// .. Copyright : (c) 2014
		// .. Function :
		// ..*****************************************************************************
		// ..
		// .type text xtread
		// .type R7.2 xrvalue
		// ..
		// .set debug +
		// !INPUT
		// .read "Please enter valid R7.2 floating point number:" U|xtread
		// .assign U|xrvalue = U|xtread
		// .continue END ? G|mehr = G|true
		// .continue INPUT
		// ..
		// !END
		// .continue
		FOe.input("ow1/FOP.ASSIGN.VALUE");

		UserTextBuffer userTextBuffer =
				BufferFactory.newInstance(true).getUserTextBuffer();
		if (userTextBuffer.isVarDefined("xrvalue")) {
			double value = userTextBuffer.getDoubleValue("xrvalue");
			ctx.out().println("R7.2: " + value);
			return 0;
		}
		return 1;
	}

}
