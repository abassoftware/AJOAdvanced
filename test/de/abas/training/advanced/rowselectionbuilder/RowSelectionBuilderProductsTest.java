package de.abas.training.advanced.rowselectionbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.EditorAction;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.RowSelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.training.advanced.selection.RowSelectionBuilderProducts;
import de.abas.training.advanced.testutility.TestLogger;
import de.abas.training.advanced.testutility.Utility;

public class RowSelectionBuilderProductsTest {

	@Rule
	public TestName testName = new TestName();

	private Utility utility = new Utility();
	private DbContext ctx;
	private RowSelectionBuilderProducts instance;
	private static Logger logger = TestLogger.getLogger();

	@Test
	public void rowSelectionBuilderProductsNotEmptyTest() throws Exception {
		List<Product> products = utility.getObjects(ctx, Product.class, "NN10021");
		assertEquals("product exists exactly once", 1, products.size());
		RowSelectionBuilder<Product, Product.Row> rowSelectionBuilder = RowSelectionBuilder.create(Product.class, Product.Row.class);
		rowSelectionBuilder.addForHead(Conditions.between(Product.META.idno, "10001", "10010"));
		rowSelectionBuilder.add(Conditions.eq(Product.Row.META.productListElem, products.get(0)));
		List<Product.Row> rows = ctx.createQuery(rowSelectionBuilder.build()).execute();
		assertNotEquals("selection is not empty", 0, rows.size());
	}

	@Test
	public void rowSelectionBuilderProductsTest() throws Exception {
		logger.info(String.format(TestLogger.TEST_INIT_MESSAGE, testName.getMethodName()));
		instance.run(null);
	}

	@Before
	public void setup() {
		logger.info(TestLogger.SETUP_MESSAGE);
		ctx = utility.createClientContext();
		instance = new RowSelectionBuilderProducts();
		makeProductUnique();
	}

	private void createProductAndSubBOMEntrys() {
		try {
			ProductEditor productEditor = ctx.newObject(ProductEditor.class);
			productEditor.setSwd("NN10021");
			productEditor.commit();

			Product subBOMProduct = QueryUtil.getFirstByIdNo(ctx, "10010", Product.class);
			ProductEditor subBOMProductEditor = subBOMProduct.createEditor();
			subBOMProductEditor.open(EditorAction.UPDATE);
			ProductEditor.Row row = subBOMProductEditor.table().appendRow();
			row.setProductListElem(productEditor.objectId());
			row.setElemQty(1);
			subBOMProductEditor.commit();
		}
		catch (CommandException e) {
			logger.error("An exception occurred while trying to change subBOM product", e);
			throw new RuntimeException(e);
		}
	}

	private void makeProductUnique() {
		try {
			List<Product> products = utility.getObjects(ctx, Product.class, "NN10021");
			if (products.size() > 1) {
				logger.info("Search word NN10021 was not unique: all products with that search word will be deleted "
						+ "and a new product NN10021 will be created and entered in SubBOM of product 30010");
				for (Product product : products) {
					product.delete();
				}
				createProductAndSubBOMEntrys();
			}
			logger.info("Search word NN10021 is present and unique");
		}
		catch (NullPointerException e) {
			logger.info("Search word NN10021 does not exist. It will be created and entered in SubBOM of product 30010");
			createProductAndSubBOMEntrys();
		}

	}
}
