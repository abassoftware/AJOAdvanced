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
	private boolean rollback = true;

	private String message = "Ok";
	private Transaction transaction = null;

	@Override
	public int run(String[] args) {

		dbContext = getDbContext();

		// adding jdom-2-0-5.jar to build path and enter in mandant.classpath

		if (args.length == 3) {
			xmlFile = args[1];
			logFile = args[2];
		}

		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = null;

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(logFile));
			document = saxBuilder.build(xmlFile);

			// get root element
			Element rootElement = document.getRootElement();

			// checks whether XML file is valid abas XML
			if (rootElement.getName().equals("abasData")) {
				rollback = false;
				// get recordSet
				Element recordSet = rootElement.getChild("recordSet");

				// get all records
				List<Element> records = recordSet.getChildren();

				// initializes transaction
				transaction = dbContext.getTransaction();
				transaction.begin();

				ProductEditor productEditor = null;
				// iterate all records
				for (Element record : records) {
					String swd = record.getAttribute("swd").getValue();
					dbContext.out().println("search word: " + swd);

					// create objects
					productEditor = dbContext.newObject(ProductEditor.class);

					SelectionBuilder<Product> selectionBuilder =
							SelectionBuilder.create(Product.class);
					selectionBuilder.add(Conditions.eq(Product.META.swd, swd));

					if ((QueryUtil.getFirst(dbContext, selectionBuilder.build())) == null) {
						dbContext.out().println(
								"Product " + swd + " does not yet exist");
						rollback = false;
						// gets all elements in next level
						List<Element> recordChildren = record.getChildren();
						for (Element recordChild : recordChildren) {
							if (recordChild.getName().equals("header")) {
								List<Element> headerFields =
										recordChild.getChildren();
								for (Element headerField : headerFields) {
									String name =
											headerField.getAttributeValue("name");
									String value = headerField.getValue();
									productEditor.setString(name, value);
									dbContext.out().println(name + " - " + value);
								}
							}
							else if (recordChild.getName().equals("row")) {
								Row appendRow = productEditor.table().appendRow();
								List<Element> rowFields = recordChild.getChildren();
								for (Element rowField : rowFields) {
									String name = rowField.getAttributeValue("name");
									String value = rowField.getValue();
									appendRow.setString(name, value);
									dbContext.out().println(name + " - " + value);
								}
							}
						}
						// For testing purposes
						// productEditor.abort();
						productEditor.commit();
						Product product = productEditor.objectId();
						message = product.getIdno() + " -> " + product.getSwd();
						dbContext.out().println("message: " + message);
						bufferedWriter.write(message);
						bufferedWriter.newLine();

					}
					else {
						productEditor.abort();
						rollback = true;
						message = "Product " + swd + " does already exist";
						dbContext.out().println("message: " + message);
						bufferedWriter.write(message);
						bufferedWriter.newLine();
						break;
					}
				}

				if (rollback == true) {
					message = message + " -> rollback";
					transaction.rollback();
				}
				else {
					message = message + " -> commit";
					transaction.commit();
				}
				dbContext.out().println("message: " + message);
				bufferedWriter.write(message);
				bufferedWriter.newLine();

			}
			else {
				message = "No valid abas xml";
				bufferedWriter.write(message);
				bufferedWriter.newLine();
			}

			message = "End of program";
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			dbContext.out().println(message);
			bufferedWriter.close();
		}
		catch (JDOMException e) {
			dbContext.out().println(e.getMessage());
			return 1;
		}
		catch (IOException e) {
			dbContext.out().println(e.getMessage());
			return 1;
		}
		return 0;
	}
}
