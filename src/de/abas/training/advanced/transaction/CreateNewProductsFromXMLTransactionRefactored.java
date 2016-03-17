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

	// TODO: Write test for class: Invalid field value productListElem(!1) =
	// [MYMOB0]
	// MYMOB0: Not found [1361]

	private static final Logger logger = Logger.getLogger(CreateNewProductsFromXMLTransactionRefactored.class);

	public static void main(String[] args) {
		new CreateNewProductsFromXMLTransactionRefactored().runClientProgram(args);
	}

	private String xmlFile = "files/products.xml";
	private final DbContext dbContext = getDbContext();
	private Transaction transaction;

	private ProductEditor productEditor;

	@Override
	public int run(String[] arg1) {

		// adding jdom-2-0-5.jar to build path and enter in mandant.classpath

		initXmlFileName(arg1);

		try {
			final Element rootElement = new SAXBuilder().build(xmlFile).getRootElement();
			if (isValidXml(rootElement)) {
				displayRootElementName(rootElement);
				displayRecordSetAttributes(rootElement);

				beginTransaction();

				createProductsIfNotExisting(rootElement);

			} else {
				logger.warn("is not valid xml formatting");
			}
			logger.info("end of program");
		} catch (final IOException e) {
			logger.fatal(e.getMessage(), e);
			return 1;
		} catch (final JDOMException e) {
			logger.fatal(e.getMessage(), e);
			return 1;
		} catch (final Exception e) {
			logger.warn(e.getMessage(), e);
		} finally {
			closeProductEditor();
		}
		return 0;
	}

	private void beginTransaction() {
		transaction = dbContext.getTransaction();
		transaction.begin();
	}

	private void checkForRollback(Attribute attribute) throws Exception, IOException {
		if (attribute.getName().equals("swd")) {
			final SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
			selectionBuilder.add(Conditions.eq(Product.META.swd, attribute.getValue()));
			final Product first = QueryUtil.getFirst(dbContext, selectionBuilder.build());

			if (first != null) {
				final String message = String.format("Object swd: %s already exiting", attribute.getValue());
				logger.error(message);
				transaction.rollback();
				throw new Exception(message);
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

	private void createProduct(DbContext dbContext, Element record) throws IOException {
		final List<Element> recordChildren = record.getChildren();
		for (final Element recordChild : recordChildren) {
			if (recordChild.getName().equals("header")) {
				writeProductHeaderFields(dbContext, recordChild);
			} else if (recordChild.getName().equals("row")) {
				writeProductRowFields(dbContext, recordChild);
			}
		}
	}

	private void createProductsIfNotExisting(Element rootElement) throws Exception, IOException {
		for (final Element record : rootElement.getChild("recordSet").getChildren()) {
			final List<Attribute> recordAttributes = record.getAttributes();

			for (final Attribute attribute : recordAttributes) {
				logger.debug(String.format("Attributes: %s -> %s", attribute.getName(), attribute.getValue()));
				checkForRollback(attribute);
			}

			productEditor = dbContext.newObject(ProductEditor.class);
			createProduct(dbContext, record);

			// for testing purposes
			// productEditor.abort();
			productEditor.commit();
			final Product objectId = productEditor.objectId();
			final String swd = objectId.getSwd();
			final String idno = objectId.getIdno();
			logger.debug(String.format("Product %s - %s was created", swd, idno));
		}
	}

	private void displayRecordSetAttributes(Element rootElement) {
		final List<Attribute> attributes = rootElement.getChild("recordSet").getAttributes();
		for (final Attribute attribute : attributes) {
			logger.debug(String.format("Attributes: %s -> %s", attribute.getName(), attribute.getValue()));
		}
	}

	private void displayRootElementName(Element rootElement) {
		logger.debug(String.format("rootElement: %s", rootElement.getName()));
	}

	private void initXmlFileName(String[] arg1) {
		if (arg1.length == 2) {
			xmlFile = arg1[1];
		}
	}

	private boolean isValidXml(Element rootElement) {
		return rootElement.getName().equals("abasData");
	}

	private void writeProductHeaderFields(DbContext dbContext, Element recordChild) throws IOException {
		logger.debug("writing header");
		final List<Element> fields = recordChild.getChildren();
		for (final Element field : fields) {
			final String name = field.getAttributeValue("name");
			final String value = field.getValue();
			logger.debug(String.format("header field: %s -> %s", name, value));
			productEditor.setString(name, value);
		}
	}

	private void writeProductRowFields(DbContext dbContext, Element recordChild) throws IOException {
		logger.debug("writing row");
		final List<Element> fields = recordChild.getChildren();

		final Row appendRow = productEditor.table().appendRow();
		for (final Element field : fields) {
			final String name = field.getAttributeValue("name");
			final String value = field.getValue();
			logger.debug(String.format("row field: %s -> %s", name, value));
			appendRow.setString(name, value);
		}
	}
}
