package de.abas.training.advanced.gui;

import de.abas.erp.api.gui.GUISelectionBuilder;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.Selection;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Shows how to use a GUI selection within an AJO program.
 *
 * @author abas Software AG
 *
 */
public class GuiSelection extends AbstractAjoAccess {

	DbContext ctx;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();

		final Selection<Product> selection = GUISelectionBuilder.select(ctx, Product.class);
		if (selection != null) {
			final Query<Product> query = ctx.createQuery(selection);
			for (final Product product : query) {
				ctx.out().println(product.getIdno() + " - " + product.getSwd());
			}
		} else {
			ctx.out().println("Nothing selected");
		}

		return 0;
	}

}
