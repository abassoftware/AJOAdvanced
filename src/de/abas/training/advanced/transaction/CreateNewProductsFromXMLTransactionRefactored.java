package de.abas.training.advanced.transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
	String logFile = "win/tmp/ProductListToRead.log";
	String xmlFile = "win/tmp/ProductListToRead.xml";
	private DbContext dbContext = getDbContext();
	private boolean rollback;
	private BufferedWriter bufferedWriter;
	private ProductEditor productEditor;

	@Override
	public int run(String[] arg1) {

		// adding jdom-2-0-5.jar to build path and enter in mandant.classpath

		initName_LogFile_XmlFile(arg1);

		getLogFile();

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(logFile));
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
				String message = "kein abasData xml-Format";
				dbContext.out().println(message);
				writeLogFile(message);
			}
			String message = "Programmende";
			dbContext.out().println(message);
			writeLogFile(message);
			bufferedWriter.close();
		}
		catch (IOException e) {
			dbContext.out().println("Fehler " + e.getMessage());
			return 1;
		}
		catch (JDOMException e) {
			dbContext.out().println(e.getMessage());
			return 1;
		}
		finally {
			closeProductEditor();
			closeBufferedWriter(dbContext);
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
				String message =
						"Object swd: " + attribute.getValue() + " already existing";
				writeLogFile(message);
			}
		}
	}

	private void closeBufferedWriter(DbContext dbContext) {
		try {
			bufferedWriter.close();
		}
		catch (IOException e) {
			dbContext.out().println(
					"Error while closing the log file -> " + e.getMessage());
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
				dbContext.out().println(
						attribute.getName() + " -> " + attribute.getValue());
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
			String message = "Product was created";
			// commit
			productEditor.commit();
			Product objectId = productEditor.objectId();
			String swd = objectId.getSwd();
			String idno = objectId.getIdno();
			// write log file
			message = swd + " - " + idno + " was created";
			writeLogFile(message);
		}
	}

	private void displayRecordSetAttributes(Element rootElement) {
		List<Attribute> attributes =
				rootElement.getChild("recordSet").getAttributes();
		for (Attribute attribute : attributes) {
			dbContext.out().println(
					attribute.getName() + " -> " + attribute.getValue());
		}
	}

	private void displayRootElementName(Element rootElement) {
		dbContext.out().println("rootElement: " + rootElement.getName());
	}

	private void getLogFile() {
		File file = new File(logFile);
		if (!file.exists()) {
			try {
				boolean createNewFile = file.createNewFile();
				if (createNewFile) {
					dbContext.out().println("File " + logFile + " created");
				}
			}
			catch (IOException e) {
				dbContext.out().println(e.getMessage());
			}
		}
	}

	private void initName_LogFile_XmlFile(String[] arg1) {
		if (arg1.length == 3) {
			xmlFile = arg1[1];
			logFile = arg1[2];
		}
	}

	private boolean isValidXml(Element rootElement) {
		return rootElement.getName().equals("abasData");
	}

	private void roolbackIfNecessary(Transaction transaction) throws IOException {
		if (rollback) {
			transaction.rollback();
			String message = "rollback";
			dbContext.out().println(message);
			writeLogFile(message);
		}
		else {
			transaction.commit();
			String message = "commit";
			dbContext.out().println(message);
			writeLogFile(message);
		}
	}

	private void writeLogFile(String message) throws IOException {
		bufferedWriter.write(message);
		bufferedWriter.newLine();
	}

	private void writeProductHeaderFields(DbContext dbContext, Element recordChild)
			throws IOException {
		dbContext.out().println("writing header");
		List<Element> fields = recordChild.getChildren();
		for (Element field : fields) {
			String name = field.getAttributeValue("name");
			String value = field.getValue();
			String message = "header field: " + name + " -> " + value;
			dbContext.out().println(message);
			writeLogFile(message);
			productEditor.setString(name, value);
		}
	}

	private void writeProductRowFields(DbContext dbContext, Element recordChild)
			throws IOException {
		dbContext.out().println("writing row");
		List<Element> fields = recordChild.getChildren();

		Row appendRow = productEditor.table().appendRow();
		for (Element field : fields) {
			String name = field.getAttributeValue("name");
			String value = field.getValue();
			String message = "row field: " + name + " -> " + value;
			dbContext.out().println(message);
			writeLogFile(message);
			appendRow.setString(name, value);
		}
	}
}
