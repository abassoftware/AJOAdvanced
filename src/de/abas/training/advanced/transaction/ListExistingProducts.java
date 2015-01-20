package de.abas.training.advanced.transaction;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;

public class ListExistingProducts implements ContextRunnable {

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		DbContext dbContext = ctx.getDbContext();
		run(dbContext);
		return 0;
	}

	private void run(DbContext dbContext) {
		// Alle Producte deren Suchwort mit MYPC beginnt löschen
		SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.starts(Product.META.swd, "MYPC"));
		Query<Product> query = dbContext.createQuery(selectionBuilder.build());
		for (Product product : query) {
			dbContext.out().println(product.getSwd() + " - " + product.getIdno());
		}
		dbContext.out().println("end of program");
	}
}
