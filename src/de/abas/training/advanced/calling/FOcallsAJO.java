package de.abas.training.advanced.calling;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;

/**
 * This class shows how AJO classes can be called within a FOP. find FO file in
 * files/JFOP.WITH.ARGUMENTS.STATUS.FO2
 *
 * @author abas Software AG
 *
 */
public class FOcallsAJO implements ContextRunnable {

	@Override
	public int runFop(FOPSessionContext context, String[] args) throws FOPException {

		return 0;
	}
}
