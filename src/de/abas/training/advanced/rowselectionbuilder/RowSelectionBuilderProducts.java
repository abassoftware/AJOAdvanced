package de.abas.training.advanced.rowselectionbuilder;

import de.abas.erp.db.RowQuery;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.Product.Row;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.RowSelectionBuilder;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.training.advanced.common.AbstractAjoAccess;

/**
 * This class shows advanced selections in the head and row of a data set.
 *
 * @author abas Software AG
 *
 */
public class RowSelectionBuilderProducts extends AbstractAjoAccess {

	public static void main(String[] args) {
		new RowSelectionBuilderProducts().runClientProgram(args);
	}

	@Override
	public int run(String[] args) {
		RowSelectionBuilder<Product, Row> rowSelectionBuilder = RowSelectionBuilder.create(Product.class, Row.class);

		// row selection criteria
		// Caution: The method getSelectedProduct can also return null
		rowSelectionBuilder.add(Conditions.eq(Row.META.productListElem, getSelectedProduct("NN10021")));

		// head selection criteria
		rowSelectionBuilder.addForHead(Conditions.between(Product.META.idno, "10001", "10010"));

		RowQuery<Product, Row> rowQueryProduct = getDbContext().createQuery(rowSelectionBuilder.build());

		// displays query result
		for (Row row : rowQueryProduct) {
			getDbContext().out().println(row.header().getIdno() + " - " + row.header().getSwd());
			getDbContext().out().println("--" + row.getProductListElem().getIdno() + " -- " + row.getProductListElem().getSwd());
		}

		return 0;

	}

	/**
	 * Selects first product with the specified search word.
	 *
	 * @param swd The search word.
	 * @return Returns the product as instance of SelectableObject.
	 */
	private SelectableObject getSelectedProduct(String swd) {
		SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.eq(Product.META.swd, swd));
		Product product = QueryUtil.getFirst(getDbContext(), selectionBuilder.build());
		return product;
	}

}
