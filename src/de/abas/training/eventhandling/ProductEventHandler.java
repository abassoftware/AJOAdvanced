package de.abas.training.eventhandling;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import de.abas.eks.jfop.FOPExitException;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.common.type.enums.EnumEditorAction;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;

/**
 * The ProductEventHandler handles events occurring in a product object
 * (database 2:1).
 * 
 * This class shows how to work with FreeTexts.
 * 
 * @author abas Software AG
 *
 */
@EventHandler(head = ProductEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class ProductEventHandler {

	@ScreenEventHandler(type = ScreenEventType.VALIDATION)
	public void screenValidation(ScreenEvent event,
			ScreenControl screenControl, DbContext ctx, ProductEditor head)
			throws EventException {
		try {
			// gets screen mode that triggered the event
			EnumEditorAction screenMode = event.getCommand();
			// continues only if event was triggered in edit mode
			if (screenMode.equals(EnumEditorAction.Edit)) {
				if (ProductEditor.META.drawingNorm.isModified(head)) {
					// gets current date from client
					if (ProductEditor.META.freeText2.isEmpty(head)) {
						writeTo(head);
					}
					else {
						appendTo(head);
					}
				}
				else {
					ctx.out().println("Field drawingNorm was not changed.");
				}
			}
		}
		catch (IOException e) {
			throw new FOPExitException("Cannot write freetext2", 1);
		}
	}

	/**
	 * Appends to FreeText2.
	 * 
	 * @param head The ProductEditor instance.
	 * @throws IOException Thrown if writing to FreeText is not possible.
	 */
	private void appendTo(ProductEditor head) throws IOException {
		// uses StringWriter object to read current content of
		// freeText2
		Writer writer = head.getFreeText2(new StringWriter());
		String write = head.getDrawingNorm();
		writer.append('\n');
		write = new AbasDate() + " -> " + write;
		writer.append(write);
		// uses StringReader to Read content of Writer object
		// and assigns it to freeText2
		head.setFreeText2(new StringReader(writer.toString()));
	}

	/**
	 * Writes to FreeText2, overwriting any content.
	 * 
	 * @param head The ProductEditor instance.
	 * @throws IOException Thrown if writing to FreeText is not possible.
	 */
	private void writeTo(ProductEditor head) throws IOException {
		// stores date and content of field drawingNorm
		String write = new AbasDate() + " -> " + head.getDrawingNorm();
		// uses StringReader object to read the String variable
		// write and writes its content to freeText2
		head.setFreeText2(new StringReader(write));
	}

}
