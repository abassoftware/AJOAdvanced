package de.abas.training.advanced.testutility;

import java.util.List;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.Deletable;
import de.abas.erp.db.SelectableObject;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.training.advanced.common.ConnectionProvider;

public class Utility {

	private String hostname;
	private String client;

	/**
	 * Creates a client context with the standard port and predefined application
	 * name.
	 *
	 * @return A client database context.
	 */
	public DbContext createClientContext() {
		ConnectionProvider connectionProvider = new ConnectionProvider();
		DbContext ctx = connectionProvider.createDbContext("test");
		hostname = connectionProvider.hostname;
		client = connectionProvider.mandant;
		return ctx;
	}

	/**
	 * Deletes specified objects.
	 *
	 * @param ctx The database context.
	 * @param className The name of the class from which to objects are to be
	 * deleted.
	 * @param swd The search word that specifies the objects.
	 */
	public <C extends SelectableObject & Deletable> void deleteObjects(
			DbContext ctx, Class<C> className, String swd) {
		SelectionBuilder<C> selectionBuilder = SelectionBuilder.create(className);
		selectionBuilder.add(Conditions.starts("swd", swd));
		List<C> objects = ctx.createQuery(selectionBuilder.build()).execute();
		for (C object : objects) {
			object.delete();
		}
	}

	/**
	 * Getter for client.
	 *
	 * @return The client.
	 */
	public String getClient() {
		return client;
	}

	/**
	 * Getter for host name.
	 *
	 * @return The host name.
	 */
	public String getHostname() {
		return hostname;
	}

	public <C extends SelectableObject> List<C> getObjects(DbContext ctx,
			Class<C> className, String swd) {
		SelectionBuilder<C> selectionBuilder = SelectionBuilder.create(className);
		selectionBuilder.add(Conditions.eq("swd", swd));
		return ctx.createQuery(selectionBuilder.build()).execute();
	}

	public <C extends SelectableObject> List<C> getObjects(DbContext ctx,
			Class<C> className, String idnoFrom, String idnoTo) {
		SelectionBuilder<C> selectionBuilder = SelectionBuilder.create(className);
		selectionBuilder.add(Conditions.between("idno", idnoFrom, idnoTo));
		return ctx.createQuery(selectionBuilder.build()).execute();
	}

}
