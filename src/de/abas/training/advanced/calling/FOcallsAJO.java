package de.abas.training.advanced.calling;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;

/**
 * This class shows how AJO classes can be called within a FOP.
 * The calling FOP is FOP.CALLS.JFOP.ARGUMENTS.STATUS:
 *
 * ..!interpreter english translate noabbrev
 * ..*****************************************************************************
 * .. FOP-Name : FOP.CALLS.JFOP.ARGUMENTS.STATUS
 * .. Date : 20.10.2014
 * .. Author : abas Software AG
 * .. Responsible :
 * .. Supervisor :
 * .. Copyright : (c) 2014
 * .. Function :
 * ..*****************************************************************************
 * ..
 * .. variable definition --------------------------------------------------------
 * .type int xiNo1 ? F|defined(U|xiZahl1) = G|false
 * .type int xiNo2 ? F|defined(U|xiZahl2) = G|false
 * .type int xiResult ? F|defined(U|xiResult) = G|false
 * .. ----------------------------------------------------------------------------
 * .. initialize variables
 * .formula U|xiNo1 = 2
 * .formula U|xiNo2 = 10
 * .. initialize xiResult --------------------------------------------------------
 * .formula U|xiResult = 9999
 * ..
 * .set debug +
 * .. All user defined variables are available in AJO using the U buffer
 * .. call JFOP ------------------------------------------------------------------
 * .. -- with returned status
 * .input "de.abas.training.calling.FopCallsJFopArgumentsStatus.class"
 * ..
 * .. .set debug +
 * .. display result -------------------------------------------------------------
 * xiResult: 'U|xiResult'
 * ..
 * If Result =! 1 => 0 (ok) else 1 (error)
 * Status: 'G|status'
 * .. end ------------------------------------------------------------------------
 * !END
 * .end
 *
 * @author abas Software AG
 *
 */
public class FOcallsAJO implements ContextRunnable {

	private DbContext ctx = null;

	@Override
	public int runFop(FOPSessionContext context, String[] args) throws FOPException {
		ctx = context.getDbContext();
		ctx.out().println("JFOP running ...");
		ctx.close();

		// gets the U buffer
		// BufferFactory.newInstance(false) => FO commands German
		// BufferFactory.newInstance(true) => FO commands English
		UserTextBuffer userTextBuffer = BufferFactory.newInstance(false).getUserTextBuffer();

		int no1 = 0;
		int no2 = 0;

		// checks whether the U buffer variable xiNo1 was already defined
		if (userTextBuffer.isVarDefined("xiNo1")) {
			// assigns value of xiNo1 to no1
			no1 = userTextBuffer.getIntegerValue("xiNo1");
		}

		// checks whether the U buffer variable xiNo2 was already defined
		if (userTextBuffer.isVarDefined("xiNo2")) {
			// assigns value of xiNo2 to no2
			no2 = userTextBuffer.getIntegerValue("xiNo2");
		}

		// adds no1 and no2
		int result = no1 + no2;

		// assigns result to U buffer variable xiResult
		userTextBuffer.setValue("xiResult", result);

		// returns to calling FOP
		if (result != 0) {
			// returns status 0 (ok) if result is not 0
			return 0;
		}
		else {
			// returns status 1 (error) else
			return 1;
		}
	}
}
