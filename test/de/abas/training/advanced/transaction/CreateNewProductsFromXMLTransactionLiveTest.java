package de.abas.training.advanced.transaction;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.Product;
import de.abas.training.advanced.testutility.TestLogger;
import de.abas.training.advanced.testutility.Utility;

public class CreateNewProductsFromXMLTransactionLiveTest {

	@Rule
	public TestName testName = new TestName();
	private Utility utility = new Utility();
	private DbContext ctx;
	private static Logger logger = TestLogger.getLogger();

	@Test
	public void productMYCPU0exits() {
		checkSubBOMProducts("MYCPU0");
	}

	@Test
	public void productMYCPU1exits() {
		checkSubBOMProducts("MYCPU1");
	}

	@Test
	public void productMYCPU2exits() {
		checkSubBOMProducts("MYCPU2");
	}

	@Test
	public void productMYHDD1exits() {
		checkSubBOMProducts("MYHDD1");
	}

	@Test
	public void productMYMOB0exits() {
		checkSubBOMProducts("MYMOB0");
	}

	@Test
	public void productMYMOB1exits() {
		checkSubBOMProducts("MYMOB1");
	}

	@Test
	public void productMYRAM0exits() {
		checkSubBOMProducts("MYRAM0");
	}

	@Test
	public void productMYRAM2exits() {
		checkSubBOMProducts("MYCPU2");
	}

	@Before
	public void setup() {
		utility.createClientContext();
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

}
