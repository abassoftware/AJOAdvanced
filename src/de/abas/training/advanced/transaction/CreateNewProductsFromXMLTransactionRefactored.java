package de.abas.training.advanced.transaction;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.Transaction;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.schema.part.ProductEditor.Row;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.training.advanced.common.AbstractAjoAccess;

public class CreateNewProductsFromXMLTransactionRefactored extends AbstractAjoAccess {

	// TODO: Write test for class: Invalid field value productListElem(!1) = [MYMOB0]
	// MYMOB0: Not found [1361]

	private static final Logger logger = Logger
			.getLogger(CreateNewProductsFromXMLTransactionRefactored.class);

	public static void main(String[] args) {
		new CreateNewProductsFromXMLTransactionRefactored().runClientProgram(args);
	}

	private String xmlFile = "files/products.xml";
	private DbContext dbContext = getDbContext();
	private boolean rollback;

	private ProductEditor productEditor;

	@Override
	public int run(String[] arg1) {

		// adding jdom-2-0-5.jar to build path and enter in mandant.classpath

		initXmlFileName(arg1);

		try {
			Element rootElement = new SAXBuilder().build(xmlFile).getRootElement();
			if (isValidXml(rootElement)) {
				rollback = false;
				displayRootElementName(rootElement);
				displayRecordSetAttributes(rootElement);

				Transaction transaction = beginTransaction();

				createProductsIfNotExisting(rootElement);

				roolbackIfNecessary(transaction);
			}
			else {
				logger.error("is not valid xml formatting");
			}
			logger.info("end of program");
		}
		catch (IOException e) {
			logger.fatal(e.getMessage(), e);
			return 1;
		}
		catch (JDOMException e) {
			logger.fatal(e.getMessage(), e);
			return 1;
		}
		finally {
			closeProductEditor();
		}
		return 0;
	}

	private Transaction beginTransaction() {
		Transaction transaction = dbContext.getTransaction();
		transaction.begin();
		return transaction;
	}

	private void checkWhetherProductExists(Attribute attribute) throws IOException {
		if (attribute.getName().equals("swd")) {
			SelectionBuilder<Product> selectionBuilder =
					SelectionBuilder.create(Product.class);
			selectionBuilder.add(Conditions.eq(Product.META.swd,
					attribute.getValue()));
			Product first = QueryUtil.getFirst(dbContext, selectionBuilder.build());

			if (first != null) {
				rollback = true;
				logger.error(String.format("Object swd: %s already exiting",
						attribute.getValue()));
			}
		}
	}

	private void closeProductEditor() {
		if (productEditor != null) {
			if (productEditor.active()) {
				productEditor.abort();
			}
		}
	}

	private void createProduct(DbContext dbContext, Element record)
			throws IOException {
		List<Element> recordChildren = record.getChildren();
		for (Element recordChild : recordChildren) {
			if (recordChild.getName().equals("header")) {
				writeProductHeaderFields(dbContext, recordChild);
			}
			else if (recordChild.getName().equals("row")) {
				writeProductRowFields(dbContext, recordChild);
			}
		}
	}

	private void createProductsIfNotExisting(Element rootElement) throws IOException {
		for (Element record : rootElement.getChild("recordSet").getChildren()) {
			productEditor = dbContext.newObject(ProductEditor.class);
			List<Attribute> recordAttributes = record.getAttributes();

			for (Attribute attribute : recordAttributes) {
				logger.info(String.format("Attributes: %s -> %s",
						attribute.getName(), attribute.getValue()));
				checkWhetherProductExists(attribute);
			}

			if (rollback) {
				productEditor.abort();
				break;
			}
			else {
				createProduct(dbContext, record);
			}

			// for testing purposes
			// productEditor.abort();
			// commit
			productEditor.commit();
			Product objectId = productEditor.objectId();
			String swd = objectId.getSwd();
			String idno = objectId.getIdno();
			logger.info(String.format("Product %s - %s was created", swd, idno));
		}
	}

	private void displayRecordSetAttributes(Element rootElement) {
		List<Attribute> attributes =
				rootElement.getChild("recordSet").getAttributes();
		for (Attribute attribute : attributes) {
			logger.info(String.format("Attributes: %s -> %s", attribute.getName(),
					attribute.getValue()));
		}
	}

	private void displayRootElementName(Element rootElement) {
		logger.info(String.format("rootElement: %s", rootElement.getName()));
	}

	private void initXmlFileName(String[] arg1) {
		if (arg1.length == 2) {
			xmlFile = arg1[1];
		}
	}

	private boolean isValidXml(Element rootElement) {
		return rootElement.getName().equals("abasData");
	}

	private void roolbackIfNecessary(Transaction transaction) throws IOException {
		if (rollback) {
			transaction.rollback();
			logger.info("rollback");
		}
		else {
			transaction.commit();
			logger.info("commit");
		}
	}

	private void writeProductHeaderFields(DbContext dbContext, Element recordChild)
			throws IOException {
		logger.info("writing header");
		List<Element> fields = recordChild.getChildren();
		for (Element field : fields) {
			String name = field.getAttributeValue("name");
			String value = field.getValue();
			logger.info(String.format("header field: %s -> %s", name, value));
			productEditor.setString(name, value);
		}
	}

	private void writeProductRowFields(DbContext dbContext, Element recordChild)
			throws IOException {
		logger.info("writing row");
		List<Element> fields = recordChild.getChildren();

		Row appendRow = productEditor.table().appendRow();
		for (Element field : fields) {
			String name = field.getAttributeValue("name");
			String value = field.getValue();
			logger.info(String.format("row field: %s -> %s", name, value));
			appendRow.setString(name, value);
		}
	}
}
