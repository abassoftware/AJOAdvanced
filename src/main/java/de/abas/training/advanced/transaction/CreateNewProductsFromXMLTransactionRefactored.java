package de.abas.training.advanced.transaction;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.FieldValueSetter;
import de.abas.erp.db.Transaction;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.training.advanced.common.AbstractAjoAccess;

public class CreateNewProductsFromXMLTransactionRefactored extends AbstractAjoAccess {

	// TODO: Write test for class: Invalid field value productListElem(!1) =
	// [MYMOB0]
	// MYMOB0: Not found [1361]

	private static final Logger logger = Logger.getLogger(CreateNewProductsFromXMLTransactionRefactored.class);

	public static void main(String[] args) {
		new CreateNewProductsFromXMLTransactionRefactored().runClientProgram(args);
	}

	private String xmlFile = "files/products.xml";
	private final DbContext dbContext = getDbContext();
	private Element rootElement;
	private Transaction transaction;

	private ProductEditor productEditor;

	@Override
	public int run(String[] arg1) {
		initXmlFileName(arg1);

		try {
			processXmlFile();
			if (isValidXml(rootElement)) {
				beginTransaction();
				createProducts(rootElement);
				commitTransaction();

			} else {
				logger.warn("is not valid xml formatting");
			}
			logger.info("end of program");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			closeProductEditor();
		}
		return 0;
	}

	private void beginTransaction() {
		logger.debug("Transaction begin");
		transaction = dbContext.getTransaction();
		transaction.begin();
	}

	private void checkForRollback(String swd) throws Exception {
		if (QueryUtil.getFirst(dbContext,
				SelectionBuilder.create(Product.class).add(Conditions.eq(Product.META.swd, swd)).build()) != null) {
			logger.debug("Transaction rollback");
			transaction.rollback();
			throw new Exception(String.format("Object swd: %s already exits", swd));
		}
	}

	private void closeProductEditor() {
		if (productEditor != null) {
			if (productEditor.active()) {
				productEditor.abort();
			}
		}
	}

	private void commitTransaction() {
		logger.debug("Transaction commit");
		transaction.commit();
	}

	private void createProduct(Element record) {
		final List<Element> recordChildren = record.getChildren();
		for (final Element recordChild : recordChildren) {
			if (recordChild.getName().equals("header")) {
				logger.debug("writing header fields");
				writeProductFields(recordChild, productEditor);
			} else if (recordChild.getName().equals("row")) {
				logger.debug("writing row fields");
				writeProductFields(recordChild, productEditor.table().appendRow());
			}
		}
	}

	private void createProducts(Element rootElement) throws Exception {
		for (final Element record : rootElement.getChild("recordSet").getChildren()) {
			checkForRollback(record.getAttributeValue("swd"));

			productEditor = dbContext.newObject(ProductEditor.class);
			createProduct(record);
			// for testing purposes
			// productEditor.abort();
			productEditor.commit();

			final String swd = productEditor.objectId().getSwd();
			final String idno = productEditor.objectId().getIdno();
			logger.debug(String.format("Product %s - %s created", swd, idno));
		}
	}

	private void initXmlFileName(String[] arg1) {
		if (arg1.length == 2) {
			xmlFile = arg1[1];
		}
	}

	private boolean isValidXml(Element rootElement) {
		return rootElement.getName().equals("abasData");
	}

	private void processXmlFile() throws JDOMException, IOException {
		rootElement = new SAXBuilder().build(xmlFile).getRootElement();
	}

	private void writeProductFields(Element recordChild, FieldValueSetter fieldValueSetter) {
		final List<Element> fields = recordChild.getChildren();
		for (final Element field : fields) {
			final String name = field.getAttributeValue("name");
			final String value = field.getValue();
			logger.debug(String.format("field: %s -> %s", name, value));
			fieldValueSetter.setString(name, value);
		}
	}

}
