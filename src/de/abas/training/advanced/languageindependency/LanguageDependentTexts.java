package de.abas.training.advanced.languageindependency;

import java.util.Locale;
import java.util.ResourceBundle;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.EKS;
import de.abas.eks.jfop.remote.FO;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.api.gui.TextBox;

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

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		// TextBox with simple text
		new TextBox(ctx.getDbContext(), getTextInOperatingLanguage("LanguageDependentTexts.1"),
				getTextInOperatingLanguage("LanguageDependentTexts.2")).show();

		// TextBox containing variable text
		final String param = FO.lesen(new String[] { getTextInOperatingLanguage("LanguageDependentTexts.4") });
		new TextBox(ctx.getDbContext(), getTextInOperatingLanguage("LanguageDependentTexts.1"),
				getTextInOperatingLanguage("LanguageDependentTexts.3", param)).show();

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
	protected String getTextInOperatingLanguage(String key, Object... params) {
		// gets current operating language
		final Locale operatingLangLocale = EKS.getFOPSessionContext().getOperatingLangLocale();
		// gets text specified by key from ControlFOPCopySystem.properties in
		// previously specified operating language
		final String bundle = ResourceBundle.getBundle(LanguageDependentTexts.class.getName(),
				operatingLangLocale,
				LanguageDependentTexts.class.getClassLoader()).getString(key);
		// fills replacement characters (%s) with the according parameters from
		// params
		return String.format(bundle, params);
	}

}