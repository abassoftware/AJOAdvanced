package de.abas.training.advanced.selection;

import de.abas.erp.common.type.IdImpl;
import de.abas.erp.common.type.enums.EnumSalesProcess;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.SelectableRecord;
import de.abas.erp.db.TableDescriptor;
import de.abas.erp.db.TableDescriptor.FieldQuantum;
import de.abas.erp.db.schema.sales.SalesOrder;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * Example for selecting various groups of Sales database using the
 * TableDescriptor class.
 *
 * @author abas Software AG
 *
 */
public class TableDescriptorSalesSelection extends AbstractAjoAccess {

	public static void main(String[] args) {
		new TableDescriptorSalesSelection().runClientProgram(args);
	}

	DbContext ctx;

	@Override
	public int run(String[] args) {
		ctx = getDbContext();

		final TableDescriptor tableDescriptor = new TableDescriptor(3, new int[] { 22, 23, 24 }, FieldQuantum.Mixed);
		final Query<SelectableRecord> query = ctx.createQuery(SelectionBuilder.create(tableDescriptor).build());
		for (final SelectableRecord selectableRecord : query) {
			final String id = selectableRecord.getString("id");
			if (selectableRecord.getEnum(EnumSalesProcess.class, "type").equals(EnumSalesProcess.SalesOrder)) {
				final SalesOrder salesOrder = ctx.load(SalesOrder.class, new IdImpl(id));
				ctx.out().println(
						"Sales order: " + salesOrder.getIdno() + " - Customer: " + salesOrder.getCustomer().getSwd());
			}
		}

		return 0;
	}

}
