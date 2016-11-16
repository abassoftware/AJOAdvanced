package de.abas.training.advanced.transaction;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.training.advanced.common.ConnectionProvider;

public class CreateNewProductsFromXMLTransactionRefactoredTest {

	@Rule
	public TestName testName = new TestName();
	private CreateNewProductsFromXMLTransactionRefactored instance;
	private DbContext ctx;

	/**
	 * Deletes all products that might be left from previous tests.
	 */
	@After
	public void cleanup() {
		deleteProducts("TESTHEAD");
		deleteProducts("TESTMYPC");
		deleteProducts("TESTROW");
	}

	@Test
	public void importOneProduct() {
		final String input = "test/de/abas/training/advanced/transaction/example1.xml";
		instance.run(new String[] { "", input });
		final SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.eq(Product.META.swd, "TESTHEAD"));
		final List<Product> products = ctx.createQuery(selectionBuilder.build()).execute();
		assertEquals("count of products with swd TESTHEAD", 1, products.size());
	}

	@Test
	public void importProductWithRows() {
		final String input = "test/de/abas/training/advanced/transaction/example2.xml";
		final ProductEditor rowProduct = ctx.newObject(ProductEditor.class);
		rowProduct.setSwd("TESTROW");
		rowProduct.commit();
		instance.run(new String[] { "", input });

		final SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.eq(Product.META.swd, "TESTHEAD"));
		final List<Product> products = ctx.createQuery(selectionBuilder.build()).execute();
		assertEquals("count of products with swd TESTHEAD", 1, products.size());
		final Product product = products.get(0);
		assertEquals("product has 1 row", 1, product.table().getRowCount());
		assertEquals("product row contains product TESTROW",
				"TESTROW",
				product.table().getRow(1).getProductListElem().getSwd());
	}

	@Before
	public void setup() {
		instance = new CreateNewProductsFromXMLTransactionRefactored();
		createClientContext();
		cleanup();
	}

	@Test
	public void shouldNotReimportExistingProduct() {
		final String input = "test/de/abas/training/advanced/transaction/example1.xml";
		final ProductEditor productEditor = ctx.newObject(ProductEditor.class);
		productEditor.setSwd("TESTHEAD");
		productEditor.setDescrOperLang("Test product for doublet check");
		productEditor.commit();
		instance.run(new String[] { "", input });
		final SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.eq(Product.META.swd, "TESTHEAD"));
		final List<Product> products = ctx.createQuery(selectionBuilder.build()).execute();
		assertEquals("count of products with swd TESTHEAD", 1, products.size());
		assertEquals("the product should have its original value in descrOperLang",
				"Test product for doublet check",
				products.get(0).getDescrOperLang());
	}

	/**
	 * Creates a client context with the standard port and predefined
	 * application name using the ajo-access.properties file.
	 */
	private void createClientContext() {
		final ConnectionProvider connectionProvider = new ConnectionProvider();
		ctx = connectionProvider.createDbContext("test");
	}

	/**
	 * Deletes all products starting with the given search word.
	 *
	 * @param swd The search word prefix.
	 */
	private void deleteProducts(String swd) {
		final SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.starts(Product.META.swd, swd));
		final List<Product> products = ctx.createQuery(selectionBuilder.build()).execute();
		for (final Product product : products) {
			product.delete();
		}
	}

}
