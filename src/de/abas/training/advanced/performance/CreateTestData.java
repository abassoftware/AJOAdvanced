package de.abas.training.advanced.performance;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.schema.part.ProductEditor.Row;

/**
 * Creates test data for <code>PerformanceOptimization</code>.
 *
 * @author abas Software AG
 *
 */
public class CreateTestData implements ContextRunnable {

	private DbContext ctx = null;

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		this.ctx = ctx.getDbContext();
		createSalesOrders(45000);
		return 0;
	}

	private void createOneProduct(int i) {
		final ProductEditor newObject = ctx.newObject(ProductEditor.class);
		newObject.setSwd("AJOPERF" + i);
		final Row newRow = newObject.table().appendRow();
		newRow.setString("productListElem", "10001");
		newRow.setElemQty(1);
		newObject.commit();
	}

	private void createSalesOrders(int count) {
		for (int i = 0; i < count; i++) {
			createOneProduct(i);
		}
	}

}