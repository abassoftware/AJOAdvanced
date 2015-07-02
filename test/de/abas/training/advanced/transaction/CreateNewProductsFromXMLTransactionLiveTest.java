package de.abas.training.advanced.transaction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.EditorAction;
import de.abas.erp.db.RowQuery;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.Product.Row;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.RowSelectionBuilder;
import de.abas.training.advanced.testutility.TestLogger;
import de.abas.training.advanced.testutility.Utility;

public class CreateNewProductsFromXMLTransactionLiveTest {

	@Rule
	public TestName testName = new TestName();
	private Utility utility = new Utility();
	private DbContext ctx;
	private ArrayList<String> swds = new ArrayList<String>();
	private static Logger logger = TestLogger.getLogger();

	@Before
	public void setup() {
		logger.info(TestLogger.SETUP_MESSAGE);
		ctx = utility.createClientContext();
		fillSwds();
		reset();
	}

	@Test
	public void subBOMProductsExits() {
		logger.info(String.format(TestLogger.TEST_INIT_MESSAGE, testName.getMethodName()));
		for (String swd : swds) {
			checkSubBOMProducts(swd);
		}
	}

	/**
	 * Checks weather products used in SubBOM exit.
	 */
	private void checkSubBOMProducts(String swd) {
		List<Product> products = null;
		try {
			products = utility.getObjects(ctx, Product.class, swd);
		}
		catch (NullPointerException e) {
			Assert.fail(String.format("Product %s did not exist", swd));
		}
		logger.info(String.format("Product %s exited %d times", swd, products.size()));
		assertEquals("product exits once", 1, products.size());
	}

	/**
	 * Creates a new product using the search word given as parameter.
	 *
	 * @param swd Search word of new product.
	 */
	private void createSubBOMProduct(String swd) {
		ProductEditor productEditor = ctx.newObject(ProductEditor.class);
		productEditor.setSwd(swd);
		productEditor.setDescrOperLang(swd);
		productEditor.setSalesPrice(22d);
		productEditor.commit();
		logger.info(String.format("Product %s created: %s", swd, productEditor.objectId().getIdno()));
	}

	/**
	 * Deletes the given product from other product's subBOMs.
	 *
	 * @param product SubBOM product.
	 */
	private void deleteProductFromOtherSubBOMs(Product product) {
		try {
			RowSelectionBuilder<Product, Row> rowSelectionBuilder = RowSelectionBuilder.create(Product.class, Product.Row.class);
			rowSelectionBuilder.add(Conditions.eq(Product.Row.META.productListElem, product));
			RowQuery<Product, Row> query = ctx.createQuery(rowSelectionBuilder.build());
			for (Row parentRow : query) {
				ProductEditor productEditor = parentRow.header().createEditor();
				productEditor.open(EditorAction.UPDATE);
				productEditor.table().deleteRow(parentRow.getRowNo());
				productEditor.commit();
			}
		}
		catch (CommandException e) {
			logger.error(String.format("Error while trying to delete product %s from other SubBOMs", product.getSwd()), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Fills swds with all necessary SubBOM products.
	 */
	private void fillSwds() {
		swds.add("MYRAM0");
		swds.add("MYRAM2");
		swds.add("MYCPU0");
		swds.add("MYCPU1");
		swds.add("MYCPU2");
		swds.add("MYMOB0");
		swds.add("MYMOB1");
		swds.add("MYHDD1");
	}

	/**
	 * Resets products in SubBOM.
	 */
	private void reset() {
		for (String swd : swds) {
			try {
				List<Product> products = utility.getObjects(ctx, Product.class, swd);
				for (Product product : products) {
					deleteProductFromOtherSubBOMs(product);
					product.delete();
					logger.info(String.format("Product %s deleted", product.getSwd()));
				}
			}
			catch (NullPointerException e) {
				logger.info(String.format("Product %s did not exist", swd));
			}
			createSubBOMProduct(swd);
		}
	}

}
