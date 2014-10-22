package de.abas.training.performance;

import de.abas.ceks.jedp.EDPSession;
import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FO;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.FieldSet;
import de.abas.erp.db.FieldValueProvider;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;
import de.abas.erp.db.util.ContextHelper;
import de.abas.erp.db.util.LegacyUtil;

/**
 * This class shows how to optimize the performance of database requests with
 * AJO.
 *
 * @author abas Software AG
 * @version 1.0
 *
 */
public class PerformanceOptimization implements ContextRunnable {

    @Override
    public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
	DbContext dbContext = ctx.getDbContext();
	runOnServer(dbContext);
	runClient(dbContext, false);
	runClient(dbContext, true);
	return 0;
    }

    private void printStats(DbContext dbContext, int counter, long delta) {
	dbContext.out().println("Number of objects: " + counter);
	dbContext.out().println("Duration: " + delta + "ms");
	double deltaInSecond = (double) delta / 1000;
	dbContext.out().println("Performance: " + (int) (counter / deltaInSecond) + " objects/s!!!");
    }

    private int readQuery(Query<Product> query) {
	int counter = 0;
	for (Product product : query) {
	    product.getIdno();
	    product.getSwd();
	    counter++;
	}
	return counter;
    }

    /**
     * Runs selection using FieldSet and LazyLoad.
     *
     * @param dbContext
     *            The database context.
     */
    private void runClient(DbContext dbContext, boolean optimize) {
	DbContext clientContext = ContextHelper.createClientContext(null, 6550, "", FO.Gvar("einmalpw"), "AJO-Local-ClientContext");
	try {
	    if (optimize) {
		EDPSession session = LegacyUtil.getSession(clientContext);
		session.setDataSetSize(1000);
	    }

	    long start = System.currentTimeMillis();
	    Selection<Product> selection = ExpertSelection.create(Product.class, "swd=AJOPERF");
	    Query<Product> query = clientContext.createQuery(selection);

	    if (optimize) {
		// uses FieldSet to define the needed fields instead of loading
		// all fields
		FieldSet<FieldValueProvider> fieldSet = FieldSet.of("id", "idno", "swd", "product^idno", "price", "head", "head^idno", "head^swd");
		query.setFields(fieldSet);
		// disable lazy load to read everything at once
		query.setLazyLoad(false);
	    }

	    int counter = readQuery(query);

	    dbContext.out().println("========== runClient() Optimize: " + (optimize ? "on" : "off"));
	    printStats(dbContext, counter, System.currentTimeMillis() - start);
	}
	finally {
	    clientContext.close();
	}
    }

    /**
     * Runs selection in server mode without optimization.
     *
     * @param dbContext
     *            The database context.
     */
    private void runOnServer(final DbContext dbContext) {
	long start = System.currentTimeMillis();

	Selection<Product> selection = ExpertSelection.create(Product.class, "swd=AJOPERF");
	Query<Product> query = dbContext.createQuery(selection);

	int counter = readQuery(query);

	dbContext.out().println("========== runOnServer()");
	printStats(dbContext, counter, System.currentTimeMillis() - start);
    }

}