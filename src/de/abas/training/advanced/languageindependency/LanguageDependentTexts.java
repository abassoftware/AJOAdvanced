package de.abas.training.advanced.languageindependency;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;

/**
 * This class shows how to use language independent texts. The translation of
 * each text is stored in a Property file with the locale as extension in the
 * file.
 *
 * Property files can be translated in abas ERP using the infosystem TRANS.
 *
 * @author abas Software AG
 *
 */
public class LanguageDependentTexts implements ContextRunnable {

	FOPSessionContext fopSessionContext;

	@Override
	public int runFop(FOPSessionContext fopSessionContext, String[] args) throws FOPException {
		this.fopSessionContext = fopSessionContext;
		final DbContext ctx = fopSessionContext.getDbContext();

		// TextBox with simple text

		// TextBox containing variable text

		return 0;
	}

}