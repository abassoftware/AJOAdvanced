package de.abas.training.advanced.performance;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.schema.part.ProductEditor.Row;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Creates test data for <code>PerformanceOptimization</code>.
 *
 * @author abas Software AG
 *
 */
public class CreateTestData extends AbstractAjoAccess {

	public static void main(String[] args) {
		new CreateTestData().runClientProgram(args);
	}

	private DbContext ctx = null;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();
		createProducts(5000);
		ctx.out().println(getMandant() + ": Done.");
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

	private void createProducts(int count) {
		for (int i = 0; i < count; i++) {
			createOneProduct(i);
		}
	}

}