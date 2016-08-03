package de.abas.training.advanced.calling;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;

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
		final UserTextBuffer userTextBuffer = BufferFactory.newInstance(true).getUserTextBuffer();

		// all variables have to be declared in the FOP
		if (userTextBuffer.isVarDefined("xtinput") & userTextBuffer.isVarDefined("xtmessage")) {
			if (!userTextBuffer.getStringValue("xtinput").matches("[a-zA-Z0-9]+")) {
				userTextBuffer.setValue("xtmessage", "You can only enter alphabetical letters and numbers");
				return 1;
			} else {
				return 0;
			}
		} else {
			userTextBuffer.setValue("xtmessage", "xtinput or xtmessage not defined");
			return 1;
		}
	}
}
