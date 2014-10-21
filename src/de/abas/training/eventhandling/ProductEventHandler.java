package de.abas.training.eventhandling;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import de.abas.eks.jfop.FOPExitException;
import de.abas.erp.api.AppContext;
import de.abas.erp.api.commands.CommandFactory;
import de.abas.erp.api.commands.FieldManipulator;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.common.type.enums.EnumEditorAction;
import de.abas.erp.db.infosystem.standard.la.PlanChart;
import de.abas.erp.db.infosystem.standard.st.StructuralBOMTreeView;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;

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

	/**
	 * This class handles the ButtonAfter logic of the custom button
	 * yisplanchart.
	 * 
	 * Variable table row of the button yisplankarte's definition: 2 14
	 * xxyisplanchart - BU3 - 2 1 0 1 0 1 6 0 A A 0 #plan chart # #
	 * 
	 * The button yisplanchart opens the infosystem PlanChart using parameters.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yisplanchart", type = ButtonEventType.AFTER)
	public void yisplanchartAfter(ButtonEvent event,
			ScreenControl screenControl, DbContext ctx, ProductEditor head)
			throws EventException {
		ctx.out().println("call infosystem ");

		// creates a CommandFactory object
		CommandFactory commandFactory =
				AppContext.createFor(ctx).getCommandFactory();
		// create a FieldManipulator object of PlanChart as parameter for the
		// infosystem PlanChart
		FieldManipulator<PlanChart> scrParamBuilder =
				commandFactory.getScrParamBuilder(PlanChart.class);

		// adds product as parameter and presses start button
		scrParamBuilder.setReference(PlanChart.META.kart, head);
		scrParamBuilder.pressButton(PlanChart.META.start);

		// opens the infosystem PlanChart using the previously defined
		// parameters
		commandFactory.startInfosystem(PlanChart.class, scrParamBuilder);
	}

	/**
	 * This class handles the ButtonAfter logic of the custom button yissubbom.
	 * 
	 * Variable table row of the button ycallis' definition: 2 15 xxyissubbom -
	 * BU3 - 2 1 0 1 0 1 6 0 A A 0 #sub BOM # #
	 * 
	 * The button yissubbom opens the infosystem StructuralBOMTreeView using
	 * parameters.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "yissubbom", type = ButtonEventType.AFTER)
	public void yissubbomAfter(ButtonEvent event, ScreenControl screenControl,
			DbContext ctx, ProductEditor head) throws EventException {
		// creates a CommandFactory object
		CommandFactory commandFactory =
				AppContext.createFor(ctx).getCommandFactory();
		// creates a FieldManipulator object of StructuralBOMTreeView as
		// parameter for the infosystem StructuralBOMTreeView
		FieldManipulator<StructuralBOMTreeView> scrParamBuilder =
				commandFactory.getScrParamBuilder(StructuralBOMTreeView.class);

		// adds the product to the parameter for the infosystem
		// StructuralBOMTreeView
		scrParamBuilder.setReference(StructuralBOMTreeView.META.artikel, head);
		// adds activated start button to the parameter for the infosystem
		// StructuralBOMTreeView
		scrParamBuilder.pressButton(StructuralBOMTreeView.META.start);

		// opens the infosystem StructuralBOMTreeView using the previously
		// defined parameters
		commandFactory.startInfosystem(StructuralBOMTreeView.class,
				scrParamBuilder);
	}

}
