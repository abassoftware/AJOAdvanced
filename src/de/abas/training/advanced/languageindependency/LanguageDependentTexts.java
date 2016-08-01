package de.abas.training.advanced.languageindependency;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.api.gui.InputBox;
import de.abas.erp.api.gui.TextBox;
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
		new TextBox(ctx, getTranslatedText("LanguageDependentTexts.1"), getTranslatedText("LanguageDependentTexts.2"))
				.show();

		// TextBox containing variable text
		final String param = new InputBox(ctx, getTranslatedText("LanguageDependentTexts.4")).read();
		new TextBox(ctx, getTranslatedText("LanguageDependentTexts.1"),
				getTranslatedText("LanguageDependentTexts.3", param)).show();

		return 0;
	}

	/**
	 * Gets text specified by key from LanguageIndependentTexts_lang.properties
	 * in current operating language
	 *
	 * @param key The key of the text in ControlFOPCopySystem.properties.
	 * @param params Strings containing parameters needed for bundle text.
	 * @return Returns text in current operating language.
	 */
	protected String getTranslatedText(String key, Object... params) {
		// gets current operating language
		final Locale locale = fopSessionContext.getOperatingLangLocale();
		// gets text specified by key from ControlFOPCopySystem.properties in
		// previously specified operating language
		final String bundle = ResourceBundle
				.getBundle(LanguageDependentTexts.class.getPackage().getName() + ".messages", locale).getString(key);
		// fills replacement characters {number} with the according parameters
		return MessageFormat.format(bundle, params);
	}

}