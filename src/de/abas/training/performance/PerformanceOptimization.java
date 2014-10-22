package de.abas.training.performance;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;

/**
 * This class shows how to optimize the performance of database requests with
 * AJO.
 *
 * @author abas Software AG
 * @version 1.0
 *
 */
public class PerformanceOptimization implements ContextRunnable {

    private DbContext ctx = null;

    @Override
    public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
	this.ctx = ctx.getDbContext();
	runOnServer();
	return 0;
    }

    /**
     * Print info about performance.
     *
     * @param counter Number of objects.
     * @param delta Time delta.
     */
    private void printStats(int counter, long delta) {
	ctx.out().println("Number of objects: " + counter);
	ctx.out().println("Duration: " + delta + "ms");
	double deltaInSecond = (double) delta / 1000;
	ctx.out().println("Performance: " + (int) (counter / deltaInSecond) + " objects/s!!!");
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
     * Runs selection in server mode without optimization.
     */
    private void runOnServer() {
	long start = System.currentTimeMillis();

	Selection<Product> selection = ExpertSelection.create(Product.class, "swd=AJOPERF");
	Query<Product> query = ctx.createQuery(selection);

	int counter = readQuery(query);

	ctx.out().println("========== runOnServer()");
	printStats(counter, System.currentTimeMillis() - start);
    }

}