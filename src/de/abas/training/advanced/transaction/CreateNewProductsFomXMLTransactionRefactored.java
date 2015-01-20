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

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Transaction;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.schema.part.ProductEditor.Row;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;

public class CreateNewProductsFomXMLTransactionRefactored implements ContextRunnable {
	String logFile = "win/tmp/ProductListToRead.log";
	String xmlFile = "win/tmp/ProductListToRead.xml";
	private boolean rollback;
	private BufferedWriter bufferedWriter;
	private ProductEditor productEditor;


	private void run(DbContext dbContext, String[] arg1) {
		
		// jdom-2-0-5.jar einbinden und in mandant.classpath eintragen
		
		initName_LogFile_XmlFile(arg1);
		
		getLogFile(dbContext);
		
		//Document document = null;
		
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(logFile));
			Element rootElement = new SAXBuilder().build(xmlFile).getRootElement();
			if(isValidXml(rootElement)){
				rollback = false;
				displayRootElementName(dbContext, rootElement);
				displayRecordSetAttributes(dbContext, rootElement);
				
				Transaction transaction = beginTransaction(dbContext);
				
				createProductsIfNotExisting(dbContext, rootElement);
				
				roolbackIfNecessary(dbContext, transaction);
			} // isValidXml -> abasDate?
			else {
				String message = "kein abasData xml-Format";
				dbContext.out().println(message);
				writeLogFile(message);
			}
			// Zum Schluss
			String message = "Programmende";
			dbContext.out().println(message);
			writeLogFile(message);
			bufferedWriter.close();
		} catch (IOException e) {
			dbContext.out().println("Fehler " + e.getMessage());
		} catch (JDOMException e) {
			dbContext.out().println(e.getMessage());
		} finally {
			closeProductEditor();
			closeBufferedWriter(dbContext);
		}
	}

	private void closeBufferedWriter(DbContext dbContext) {
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			dbContext.out().println("Feherl beim Schließen des LogFiles -> " + e.getMessage());
		}
	}

	private void closeProductEditor() {
		if(productEditor != null){
			if (productEditor.active()) {
				productEditor.abort();
			}
		}
	}

	private void roolbackIfNecessary(DbContext dbContext,
			Transaction transaction) throws IOException {
		if(rollback){
			transaction.rollback();
			String message = "rollback";
			dbContext.out().println(message);
			writeLogFile(message);
		}else {
			transaction.commit();
			String message = "commit";
			dbContext.out().println(message);
			writeLogFile(message);
		}
	}

	private void createProductsIfNotExisting(DbContext dbContext, Element rootElement)
			throws IOException {
		for (Element record : rootElement.getChild("recordSet").getChildren()) {
			productEditor = dbContext.newObject(ProductEditor.class);
			List<Attribute> recordAttributes = record.getAttributes();
			
			for (Attribute attribute : recordAttributes) {
				dbContext.out().println(attribute.getName() + " -> " + attribute.getValue());
				checkWhetherProductExists(dbContext, attribute);
			}
			
			if(rollback){
				productEditor.abort();
				break;
			} // if rollback = false, dann Datensatz anlegen
			else {
				createProduct(dbContext, record);
			} // else rollback
			
			// Zu Testzwecken wird der productEditor mit abort verlassen
//					productEditor.abort();
			String message = "Product wurde angelegt";
//					bufferedWriter.write(message);
//					bufferedWriter.newLine();
			// commit
			productEditor.commit();
			Product objectId = productEditor.objectId();
			String swd = objectId.getSwd();
			String idno = objectId.getIdno();
			// logFil schreiben
			message = swd + " - " + idno + " wurde angelegt";
			writeLogFile(message);
		} // for-each über records
	}

	private void createProduct(DbContext dbContext, Element record)
			throws IOException {
		List<Element> recordChildren = record.getChildren();
		for (Element recordChild : recordChildren) {
			if(recordChild.getName().equals("header")){
				writeProductHeaderFields(dbContext, recordChild);
			}else if (recordChild.getName().equals("row")) {
				writeProductRowFields(dbContext, recordChild);
			}							
		} // for-each recordChildren
	}

	private void checkWhetherProductExists(DbContext dbContext,
			Attribute attribute) throws IOException {
		if(attribute.getName().equals("swd")){
			//boolean isRecordExisting = false;
			SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
			selectionBuilder.add(Conditions.eq(Product.META.swd, attribute.getValue()));
			Product first = QueryUtil.getFirst(dbContext, selectionBuilder.build());
			
			if(first != null){
				//isRecordExisting = true;
				rollback = true;
				String message = "Datensatz swd: " + attribute.getValue() + " bereits vorhanden";
				writeLogFile(message);
			}							
		}
	}

	private void writeProductRowFields(DbContext dbContext, Element recordChild)
			throws IOException {
		dbContext.out().println("row schreiben");
		List<Element> fields = recordChild.getChildren();

		Row appendRow = productEditor.table().appendRow();
		for (Element field : fields) {
			String name = field.getAttributeValue("name");
			String value = field.getValue();
			String message = "row-Field: " + name + " -> " + value;
			dbContext.out().println(message);
			writeLogFile(message);
			appendRow.setString(name, value);
		}
	}

	private void writeProductHeaderFields(DbContext dbContext,
			Element recordChild) throws IOException {
		dbContext.out().println("header schreiben");
		List<Element> fields = recordChild.getChildren();
		for (Element field : fields) {
			String name = field.getAttributeValue("name");
			String value = field.getValue();
			String message = "header-Field: " + name + " -> " + value;
			dbContext.out().println(message);
			writeLogFile(message);
			productEditor.setString(name, value);
		}
	}

	private void writeLogFile(String message) throws IOException {
		bufferedWriter.write(message);
		bufferedWriter.newLine();
	}

	private Transaction beginTransaction(DbContext dbContext) {
		Transaction transaction = dbContext.getTransaction();
		transaction.begin();
		return transaction;
	}

	private void displayRecordSetAttributes(DbContext dbContext,
			Element rootElement) {
		List<Attribute> attributes = rootElement.getChild("recordSet").getAttributes();
		for (Attribute attribute : attributes) {
			dbContext.out().println(attribute.getName() + " -> " + attribute.getValue());
		}
	}

	private void displayRootElementName(DbContext dbContext, Element rootElement) {
		dbContext.out().println("rootElement: " + rootElement.getName());
	}

	private boolean isValidXml(Element rootElement) {
		return rootElement.getName().equals("abasData");
	}

	private void getLogFile(DbContext dbContext) {
		File file = new File(logFile);
		if(!file.exists()){
			try {
				boolean createNewFile = file.createNewFile();
				if(createNewFile){
					dbContext.out().println("Datei " + logFile + "wurde angelegt");
				}
			} catch (IOException e) {
				dbContext.out().println(e.getMessage());
			}
		}
	}

	private void initName_LogFile_XmlFile(String[] arg1) {
		if(arg1.length == 3){
			xmlFile = arg1[1];
			logFile = arg1[2];
		}
	}
	
	@Override
	public int runFop(FOPSessionContext arg0, String[] arg1)
			throws FOPException {
		DbContext dbContext = arg0.getDbContext();
		run(dbContext,arg1);
		return 0;
	}
}
