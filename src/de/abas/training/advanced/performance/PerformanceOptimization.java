package de.abas.training.advanced.performance;

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
 *
 */
public class PerformanceOptimization implements ContextRunnable {

	private DbContext ctx = null;

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		this.ctx = ctx.getDbContext();
		runOnServer();
		runClient(false);
		runClient(true);
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
		final double deltaInSecond = (double) delta / 1000;
		ctx.out().println("Performance: " + (int) (counter / deltaInSecond) + " objects/s!!!");
	}

	/**
	 * Reads query and gets object specific information.
	 *
	 * @param query The query.
	 * @return The number of objects.
	 */
	private int readQuery(Query<Product> query) {
		int counter = 0;
		for (final Product product : query) {
			product.getIdno();
			product.getSwd();
			counter++;
		}
		return counter;
	}

	/**
	 * Runs selection using FieldSet and LazyLoad.
	 *
	 * @param optimize Whether or not to optimize the performance.
	 */
	private void runClient(boolean optimize) {

	}

	/**
	 * Runs selection in server mode without optimization.
	 */
	private void runOnServer() {
		final long start = System.currentTimeMillis();

		final Selection<Product> selection = ExpertSelection.create(Product.class, "swd=AJOPERF");
		final Query<Product> query = ctx.createQuery(selection);

		final int counter = readQuery(query);

		ctx.out().println("========== runOnServer()");
		printStats(counter, System.currentTimeMillis() - start);
	}

}