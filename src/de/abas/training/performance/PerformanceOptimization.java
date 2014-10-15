package de.abas.training.performance;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.sales.SalesOrder;
import de.abas.erp.db.selection.SelectionBuilder;

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
		return 0;
	}

	/**
	 * Displays information about the number of objects selected and the
	 * duration of this selection.
	 * 
	 * @param dbContext
	 * @param noObjects
	 * @param noRows
	 * @param start
	 */
	private void displayPerformanceInformation(final DbContext dbContext,
			int noObjects, int noRows, long start) {
		long sec = (System.currentTimeMillis() - start);
		dbContext.out().println("time in ms: " + sec);
		dbContext.out().println("number of objects: " + noObjects);
		dbContext.out().println("number of rows:  " + noRows);
	}

	/**
	 * Runs selection in server mode without optimization.
	 * 
	 * @param dbContext The database context.
	 */
	private void runOnServer(final DbContext dbContext) {
		int noObjects = 0;
		int noRows = 0;
		long start = System.currentTimeMillis();

		// selects all sales orders
		SelectionBuilder<SalesOrder> selectionBuilder =
				SelectionBuilder.create(SalesOrder.class);
		Query<SalesOrder> query =
				dbContext.createQuery(selectionBuilder.build());

		// iterates all sales orders
		for (SalesOrder salesOrder : query) {
			int j = salesOrder.table().getRowCount();

			// iterates all sales order rows
			for (int i = 1; i <= j; i++) {
				salesOrder.table().getRow(i);
				noRows++;
			}

			noObjects++;
		}

		displayPerformanceInformation(dbContext, noObjects, noRows, start);

	}

}
