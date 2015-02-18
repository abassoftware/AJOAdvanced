package de.abas.training.advanced.calling;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;

/**
 * This class shows how AJO classes can be called within a FOP.
 * The calling FOP is FOP.CALLS.JFOP.ARGUMENTS.STATUS:
 *
 * ..!interpreter english translate noabbrev
 * ..*****************************************************************************
 * .. FOP-Name : FOP.CALLS.AJO.TO.CHECK.INPUT.STRING
 * .. Date : 20.10.2014
 * .. Author : abas Software AG
 * .. Responsible :
 * .. Supervisor :
 * .. Copyright : (c) 2014
 * .. Function :
 * ..*****************************************************************************
 * ..
 * .. variable definition --------------------------------------------------------
 * .type text xtinput
 * .type text xtmessage
 * .. ----------------------------------------------------------------------------
 * .set debug +
 * .. All user defined variables are available in AJO using the U buffer
 * .. call JFOP --------------------------------------------- with returned status
 * !INPUT
 * .formula U|xtmessage = "Ok"
 * .read "Please enter alphabetical letters and numbers only" U|xtinput
 * ..
 * .input "de.abas.training.basic.calling.FopCallsAjoToCheckInputString.class"
 * .continue END ? G|status = 0
 * .box "Message"
 * 'U|xtmessage'
 * ..
 * .continue INPUT
 * ..
 * .. end ------------------------------------------------------------------------
 * !END
 * .end
 *
 * @author abas Software AG
 *
 */
public class FOcallsAJO implements ContextRunnable {

	@Override
	public int runFop(FOPSessionContext context, String[] args) throws FOPException {
		UserTextBuffer userTextBuffer =
				BufferFactory.newInstance(true).getUserTextBuffer();

		// all variables have to be declared in the FOP
		if (userTextBuffer.isVarDefined("xtinput")
				& userTextBuffer.isVarDefined("xtmessage")) {
			if (!userTextBuffer.getStringValue("xtinput").matches("[a-zA-Z0-9]+")) {
				userTextBuffer.setValue("xtmessage",
						"You can only enter alphabetical letters and numbers");
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			userTextBuffer.setValue("xtmessage", "xtinput or xtmessage not defined");
			return 1;
		}
	}
}
