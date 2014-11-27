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

	private DbContext ctx = null;

	@Override
	public void run(String[] args) {
		ctx = getDbContext();
		ctx.out().println("JFOP running ...");

		// gets the U buffer
		// BufferFactory.newInstance(false) => FO commands German
		// BufferFactory.newInstance(true) => FO commands English
		UserTextBuffer userTextBuffer = BufferFactory.newInstance(false).getUserTextBuffer();

		// uses a method to initialize the variables
		initializeUBufferVariables(userTextBuffer, "int", "xiNumber1");
		initializeUBufferVariables(userTextBuffer, "int", "xiNumber2");
		initializeUBufferVariables(userTextBuffer, "int", "xiResult");

		// assigns variables
		userTextBuffer.assign("xiNumber1", 7);
		userTextBuffer.assign("xiNumber2", 7);
		userTextBuffer.assign("xiResult", 0);

		// ..!interpreter english translate noabbrev
		// ..*****************************************************************************
		// .. FOP-Name : FOP.CALLED.BY.AJO.CLASS
		// .. Date : Oct 20, 2014
		// .. Author : abas Software AG
		// .. Responsible :
		// .. Supervisor :
		// .. Copyright : (c) 2014
		// .. Function :
		// ..*****************************************************************************
		// FOP is executed ...
		// ..
		// .type integer xiResult ? _F|defined(U|xiResult)
		// .type integer xiNumber1 ? _F|defined(U|xiNumber1)
		// .type integer xiNumber2 ? _F|defined(U|xiNumber2)
		// ..
		// .formula U|xiResult = U|xiNumber1 + U|xiNumber2
		// ..
		// .continue
		FOe.input("FOP.CALLED.BY.AJO.CLASS");

		// gets content of xiResult from U buffer and outputs it
		int result = userTextBuffer.getIntegerValue("xiResult");
		ctx.out().println("AJO class still running ...");
		ctx.out().println("xiResult: " + result);
	}

	/**
	 * Initializes variables in U buffer.
	 *
	 * @param userTextBuffer The U buffer instance.
	 * @param type The type of variable to initialize.
	 * @param varname The name of the variable.
	 */
	private void initializeUBufferVariables(UserTextBuffer userTextBuffer, String type, String varname) {
		if (!userTextBuffer.isVarDefined(varname)) {
			userTextBuffer.defineVar(type, varname);
		}
	}

}
