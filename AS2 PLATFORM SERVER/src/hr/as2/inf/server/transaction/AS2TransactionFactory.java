package hr.as2.inf.server.transaction;

import hr.as2.inf.common.core.AS2Context;

import java.util.Hashtable;

//import javax.naming.InitialContext;

public final class AS2TransactionFactory {
	private static AS2TransactionFactory _instance = null;
	private Hashtable<String, AS2Transaction> _transactions = new Hashtable<String, AS2Transaction>();

	// private InitialContext context = null;
	// protected UserTransaction userTran = null;
	private AS2TransactionFactory() {
		AS2Context.setSingletonReference(this);
		init();
	}

	public static AS2TransactionFactory getInstance() {
		if (_instance == null)
			_instance = new AS2TransactionFactory();
		return _instance;
	}

	public AS2Transaction currentTransaction() {
		String name = Thread.currentThread().getName();
		AS2Transaction tran = _transactions.get(name);
		return tran;
	}

	public void deleteTransaction() {
		String name = Thread.currentThread().getName();
		_transactions.remove(name);
	}

	public AS2Transaction getJ2EETransaction() {
		String name = Thread.currentThread().getName();
		AS2Transaction tran = _transactions.get(name);
		if (tran == null) {
			tran = new AS2Transaction();
			_transactions.put(name, tran);
		}
		return tran;
	}

	/**
	 * Check if transaction still exists. Can be used by client to check for
	 * existence of asynchronous transactions.
	 */
	public boolean isTransactionExists(String name) {
		AS2Transaction tran = _transactions.get(name);
		if (tran == null)
			return false;
		else
			return true;
	}

	private void init() {
		try {
			/*
			 * if(context ==null||userTran==null) { Properties parms =
			 * J2EEHelper.readPropertyFileAsURL(J2EEContext.propertiesPath +
			 * "jndi.properties"); Properties sp = System.getProperties();
			 * sp.put
			 * ("java.naming.provider.url",parms.get("java.naming.provider.url"
			 * )); sp.put("java.naming.factory.initial",parms.get(
			 * "java.naming.factory.initial")); System.setProperties(sp);
			 * context = new InitialContext(parms); userTran =
			 * (javax.transaction
			 * .UserTransaction)context.lookup("javax.transaction.UserTransaction"
			 * ); //dodana linija!!! userTran.setTransactionTimeout(240);
			 * System.out.println(userTran.toString()); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
