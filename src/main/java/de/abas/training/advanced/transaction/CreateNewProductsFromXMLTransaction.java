package de.abas.training.advanced.transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
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

public class CreateNewProductsFromXMLTransaction extends AbstractAjoAccess {

	private String xmlFile = "win/tmp/products.xml";
	private String logFile = "win/tmp/products.log";

	private BufferedWriter bufferedWriter = null;
	private DbContext dbContext = null;

	private String message = "Ok";
	private Transaction transaction = null;
	ProductEditor productEditor = null;

	@Override
	public int run(String[] args) {

		dbContext = getDbContext();

		// adding jdom-2-0-5.jar to build path and enter in mandant.classpath

		if (args.length == 3) {
			xmlFile = args[1];
			logFile = args[2];
		}

		final SAXBuilder saxBuilder = new SAXBuilder();
		Document document = null;

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(logFile));
			document = saxBuilder.build(xmlFile);

			// get root element
			final Element rootElement = document.getRootElement();

			// checks whether XML file is valid abas XML
			if (rootElement.getName().equals("abasData")) {
				// get recordSet
				final Element recordSet = rootElement.getChild("recordSet");

				// get all records
				final List<Element> records = recordSet.getChildren();

				// initializes transaction
				transaction = dbContext.getTransaction();
				transaction.begin();

				// iterate all records
				for (final Element record : records) {
					final String swd = record.getAttribute("swd").getValue();
					dbContext.out().println("search word: " + swd);

					if ((QueryUtil.getFirst(dbContext,
							SelectionBuilder.create(Product.class).add(Conditions.eq(Product.META.swd, swd))
									.build())) != null) {
						transaction.rollback();
						message = "Product " + swd + " does already exist";
						bufferedWriter.write(message);
						bufferedWriter.newLine();
						throw new Exception("Product with swd " + swd + " already exists.");
					}

					// create objects
					productEditor = dbContext.newObject(ProductEditor.class);

					dbContext.out().println("Product " + swd + " does not yet exist");
					// gets all elements in next level
					final List<Element> recordChildren = record.getChildren();
					for (final Element recordChild : recordChildren) {
						if (recordChild.getName().equals("header")) {
							final List<Element> headerFields = recordChild.getChildren();
							for (final Element headerField : headerFields) {
								final String name = headerField.getAttributeValue("name");
								final String value = headerField.getValue();
								productEditor.setString(name, value);
								dbContext.out().println(name + " - " + value);
							}
						} else if (recordChild.getName().equals("row")) {
							final Row appendRow = productEditor.table().appendRow();
							final List<Element> rowFields = recordChild.getChildren();
							for (final Element rowField : rowFields) {
								final String name = rowField.getAttributeValue("name");
								final String value = rowField.getValue();
								appendRow.setString(name, value);
								dbContext.out().println(name + " - " + value);
							}
						}
					}
					// For testing purposes
					// productEditor.abort();
					productEditor.commit();
					final Product product = productEditor.objectId();
					message = product.getIdno() + " -> " + product.getSwd();
					dbContext.out().println("message: " + message);
					bufferedWriter.write(message);
					bufferedWriter.newLine();

				}

				message = message + " -> commit";
				transaction.commit();
				dbContext.out().println("message: " + message);
				bufferedWriter.write(message);
				bufferedWriter.newLine();

			} else {
				message = "No valid abas xml";
				bufferedWriter.write(message);
				bufferedWriter.newLine();
			}

			message = "End of program";
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			dbContext.out().println(message);
			bufferedWriter.close();
		} catch (final JDOMException e) {
			dbContext.out().println(e.getMessage());
			return 1;
		} catch (final IOException e) {
			dbContext.out().println(e.getMessage());
			return 1;
		} catch (final Exception e) {
			dbContext.out().println(e.getMessage());
			return 1;
		} finally {
			if (productEditor != null) {
				if (productEditor.active()) {
					productEditor.abort();
				}
			}
		}
		return 0;
	}
}
