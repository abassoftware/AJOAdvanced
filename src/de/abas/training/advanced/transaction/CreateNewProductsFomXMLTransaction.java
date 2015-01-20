package de.abas.training.advanced.transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
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

public class CreateNewProductsFomXMLTransaction implements ContextRunnable {

	@Override
	public int runFop(FOPSessionContext arg0, String[] arg1)
			throws FOPException {
		DbContext dbContext = arg0.getDbContext();
		run(dbContext,arg1);
		return 0;
	}

	public void run(DbContext dbContext, String[] arg1) {
		
		// jdom-2-0-5.jar einbinden und in mandant.classpath eintragen
		
		String logFile = "win/tmp/ProductListToRead.log";
		String xmlFile = "win/tmp/ProductListToRead.xml";
		
		if(arg1.length == 3){
			xmlFile = arg1[1];
			logFile = arg1[2];
		}
		
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
		
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = null;
		
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile));
			document = saxBuilder.build(xmlFile);
			Element rootElement = document.getRootElement();
			if(rootElement.getName().equals("abasData")){
				boolean rollback = false;
				dbContext.out().println("rootElement: " + rootElement.getName());
				Element recordSet = rootElement.getChild("recordSet");
				List<Attribute> attributes = recordSet.getAttributes();
				for (Attribute attribute : attributes) {
					dbContext.out().println(attribute.getName() + " -> " + attribute.getValue());
				}
				
				List<Element> records = recordSet.getChildren();
				
				Transaction transaction = dbContext.getTransaction();
				transaction.begin();
				
				for (Element record : records) {
					ProductEditor productEditor = dbContext.newObject(ProductEditor.class);
					List<Attribute> recordAttributes = record.getAttributes();
					
					for (Attribute attribute : recordAttributes) {
						dbContext.out().println(attribute.getName() + " -> " + attribute.getValue());
						if(attribute.getName().equals("swd")){
							SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
							selectionBuilder.add(Conditions.eq(Product.META.swd, attribute.getValue()));
							Product first = QueryUtil.getFirst(dbContext, selectionBuilder.build());
							
							if(first != null){
								rollback = true;
								String message = "Datensatz swd: " + attribute.getValue() + " bereits vorhanden";
								bufferedWriter.write(message);
								bufferedWriter.newLine();
							}							
						}
					}
					
					if(rollback){
						productEditor.abort();
						break;
					} // if rollback = false, dann Datensatz anlegen
					else {
						List<Element> recordChildren = record.getChildren();
						for (Element recordChild : recordChildren) {
							if(recordChild.getName().equals("header")){
								dbContext.out().println("header schreiben");
								List<Element> fields = recordChild.getChildren();
								for (Element field : fields) {
									String name = field.getAttributeValue("name");
									String value = field.getValue();
									String message = "header-Field: " + name + " -> " + value;
									dbContext.out().println(message);
									bufferedWriter.write(message);
									bufferedWriter.newLine();
									productEditor.setString(name, value);
								}
							}else if (recordChild.getName().equals("row")) {
								dbContext.out().println("row schreiben");
								List<Element> fields = recordChild.getChildren();

								Row appendRow = productEditor.table().appendRow();
								for (Element field : fields) {
									String name = field.getAttributeValue("name");
									String value = field.getValue();
									String message = "row-Field: " + name + " -> " + value;
									dbContext.out().println(message);
									bufferedWriter.write(message);
									bufferedWriter.newLine();
									appendRow.setString(name, value);
								}
							}							
						} // for-each recordChildren
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
					bufferedWriter.write(message);
					bufferedWriter.newLine();
				} // for-each über records
				
				if(rollback){
					transaction.rollback();
					String message = "rollback";
					dbContext.out().println(message);
					bufferedWriter.write(message);
					bufferedWriter.newLine();
				}else {
					transaction.commit();
					String message = "commit";
					dbContext.out().println(message);
					bufferedWriter.write(message);
					bufferedWriter.newLine();
				}
			} //abasDate?
			else {
				String message = "kein abasData xml-Format";
				dbContext.out().println(message);
				bufferedWriter.write(message);
				bufferedWriter.newLine();
			}
			// Zum Schluss
			String message = "Programm Ende";
			dbContext.out().println(message);
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			bufferedWriter.close();
		} catch (IOException e) {
			dbContext.out().println("Fehler " + e.getMessage());
		} catch (JDOMException e) {
			dbContext.out().println(e.getMessage());
		}
	}
}
