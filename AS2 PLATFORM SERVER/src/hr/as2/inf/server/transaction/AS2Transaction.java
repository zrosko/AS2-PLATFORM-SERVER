package hr.as2.inf.server.transaction;

/**
 * Pattern: Coordinator (Patterns for resource management, Volume 3, page 111).
 * A simple transaction is comprised of the simple steps
 * BEGIN, COMMIT/ABORT transaction. Simple transaction
 * model may not be enough in certain situations. The
 * requirements for transaction processing systems can be categorized:
 *	- ability to commit or abort all the changes
 *  - ability to abort part of the changes, yet commit the transaction
 *  - ability to incrementally commit part of the changes in the transaction
 * Various models have evolved to address these needs. The are:
 *  - Flat transactions
 *  - Chained transactions
 *  - Nested transaction
 * Here we have implemented the FLAT TRANSACTION model.
 * The transaction is flat because the units of work done as 
 * part of the transaction are at the same hierarchy. 
 * The changes that are done as part of this transaction are
 * either committed or aborted.
 * This model suffers from a few limitations that fail to
 * address some of the business requirements:
 *  - TP application that require an abort of a
 *    selective unit of work (with flat model this can be split
 *    to more transaction).
 *  - Mass update problem (with flat model again it can be split).
 * To read more on Chained and Nested transaction model refer to
 * www.omg.org.
 *
 * @author Zdravko Rosko
 */
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.server.connections.J2EEConnection;

import java.util.Enumeration;
import java.util.Stack;
public final class AS2Transaction {
	private Stack<J2EEConnection> _pool = new Stack<J2EEConnection>();
	private int _status = AS2TransactionStatus.NoTransaction;
	private long _timeOut = 0;
	private long _startTime = 0;
	private long _endTime = 0;
	//private UserTransaction userTran = null;
	protected AS2Transaction() { 
		_timeOut = AS2Context.getInstance().TXNTIMEOUT;
	}
	public static void addConnection(J2EEConnection con) throws AS2Exception{
		AS2Transaction tran = AS2TransactionFactory.getInstance().currentTransaction();
		if(tran.getStatus() != AS2TransactionStatus.Active){ 
			AS2Trace.trace(AS2Trace.E,tran, "No J2EETransaction in progress.");
			throw new IllegalStateException("No J2EETransaction in progress.");
		}			
		tran._pool.push(con);
	}
	public static void begin() throws IllegalStateException {
		AS2Transaction tran = AS2TransactionFactory.getInstance().getJ2EETransaction();
		if (tran.getStatus() != AS2TransactionStatus.NoTransaction) 
			throw new IllegalStateException("J2EETransaction already in progress.");
		tran._status = AS2TransactionStatus.Active;
		try{
			// tran.userTran =J2EETransactionFactory.getInstance().userTran;
			// tran.userTran.begin(); //JTA transaction*/
		}catch(Exception e)  {
			System.out.println("J2EETransaction.begin problem:"+e);   
		}
	}
	public static void begin(long timeOut) throws IllegalStateException{
		AS2Transaction tran = AS2TransactionFactory.getInstance().getJ2EETransaction();
		if (tran.getStatus() != AS2TransactionStatus.NoTransaction) 
			throw new IllegalStateException("J2EETransaction already in progress.");
		tran._status = AS2TransactionStatus.Active;
		tran._timeOut = timeOut;
		tran._startTime = System.currentTimeMillis();
		try{
			// tran.userTran =J2EETransactionFactory.getInstance().userTran;
			//  tran.userTran.begin(); //JTA transaction*/
		}catch(Exception e)  {
			System.out.println("J2EETransaction.begin problem:"+e);   
		}
	}
	/**
	 * Commit all used connections and notify all threads waiting on current
	 * transaction object.
	 */
	public static void commit() throws IllegalStateException, AS2Exception {
		AS2Transaction tran = AS2TransactionFactory.getInstance().currentTransaction();
		if (tran == null || tran.getStatus() != AS2TransactionStatus.Active)
			throw new IllegalStateException("No J2EETransaction in progress.");
		tran._endTime = System.currentTimeMillis();
		if (((tran._endTime - tran._startTime) > tran._timeOut) && tran._timeOut != 0)
			throw new AS2Exception("453");
		tran._status = AS2TransactionStatus.Committing;
		try {
			//  tran.userTran.commit(); //JTA transaction*/
			Enumeration<J2EEConnection> E = tran._pool.elements();
			while (E.hasMoreElements()) {
				AS2Trace.trace(AS2Trace.I, "J2EETransaction ", "Commitning J2EETransaction");
				J2EEConnection con = E.nextElement();
				con.commit();
			}
		} catch (Exception e) {
			AS2Trace.trace(AS2Trace.E, e, "Have to rollback J2EETransaction");
			AS2Transaction.rollback();
			throw new AS2Exception("451");
		} finally {
			tran = AS2TransactionFactory.getInstance().currentTransaction();	
			if(tran != null){
				synchronized (tran) {
					tran._status = AS2TransactionStatus.Committed;
					AS2Transaction.releaseAllConnections();
					tran.notifyAll();	
				}	
				AS2TransactionFactory.getInstance().deleteTransaction();
			}
		}
	}
	public void finalize() {
		try {
			if(!AS2Context.getInstance().EXTERNAL_TXN_COMMIT_IND && _status != AS2TransactionStatus.Committed && _status != AS2TransactionStatus.RolledBack)
				releaseAllConnections();
		}catch(Exception e){
			AS2Trace.trace(AS2Trace.E, e, "J2EETransaction.finalize error");
		}	
	}
	public static J2EEConnection getConnectionByName(String name) {
		AS2Transaction tran = AS2TransactionFactory.getInstance().currentTransaction();
		if(tran.getStatus() != AS2TransactionStatus.Active){ 
			AS2Trace.trace(AS2Trace.E,tran, "No J2EETransaction in progress.");
			throw new IllegalStateException("No J2EETransaction in progress.");
		}			

		J2EEConnection conn = null;
		if (!tran._pool.empty()) {
			Enumeration<J2EEConnection> E = tran._pool.elements();
			while (E.hasMoreElements()) {
				conn = E.nextElement();
				if (conn.getName().equals(name))
					return conn;
				else
					conn = null;
			}
		}
		return conn;
	}
	public int getStatus() {
		return _status;
	}
	/**
	 * Used to keep the connections for the EJB and JTA controler.
	 * No commit or rollback is done to this transaction.
	 * Commit, Rollback and retun of connections to pool is done
	 * by EJB or JTA transaction.
	 */
	public static void prepareTxn() throws IllegalStateException {
		AS2Transaction tran = AS2TransactionFactory.getInstance().getJ2EETransaction();
		if (tran.getStatus() != AS2TransactionStatus.NoTransaction) 
			throw new IllegalStateException("J2EETransaction already in progress.");
		tran._status = AS2TransactionStatus.Active;
	}
	public static void releaseAllConnections() throws AS2Exception {
		AS2Transaction tran = AS2TransactionFactory.getInstance().currentTransaction();	
		while (tran != null && !tran._pool.empty())	{
			J2EEConnection con = (J2EEConnection) tran._pool.pop();
			con._connectionManager.returnConnectionToPool(con);
		}
	}
	/**
	 * Delete the transaction object.
	 */
	public static void releaseTxn() throws IllegalStateException, AS2Exception {
		AS2Transaction tran = AS2TransactionFactory.getInstance().currentTransaction();
		if (tran == null || tran.getStatus() != AS2TransactionStatus.Active)
			throw new IllegalStateException("No J2EETransaction in progress.");
		tran._status = AS2TransactionStatus.Committed;
		AS2TransactionFactory.getInstance().deleteTransaction();
	}
	/**
	 * Rollback all used connections and notify all threads waiting on current
	 * transaction object.
	 */
	public static void rollback() throws IllegalStateException, AS2Exception {
		AS2Transaction tran = AS2TransactionFactory.getInstance().currentTransaction();
		if (tran == null)
			throw new IllegalStateException("No J2EETransaction in progress.");
		tran._endTime = System.currentTimeMillis();
		if (((tran._endTime - tran._startTime) > tran._timeOut) && tran._timeOut != 0)
			throw new AS2Exception("453");
		int status = tran.getStatus();
		try{
			if (status != AS2TransactionStatus.Active && status != AS2TransactionStatus.Committing)
				throw new IllegalStateException("No J2EETransaction in progress.");
			tran._status = AS2TransactionStatus.RollingBack;
			try{
				// tran.userTran.rollback(); //JTA transaction*/
			}catch(Exception e)  {
				System.out.println("J2EETransaction.rollback problem:"+e);   
			}
			Enumeration<J2EEConnection> E = tran._pool.elements();
			while (E.hasMoreElements()) {
				AS2Trace.trace(AS2Trace.I, "J2EETransaction ", "Roolback J2EETransaction");
				J2EEConnection con = (J2EEConnection) E.nextElement();
				con.rollback();
			}
		} finally {
			tran = AS2TransactionFactory.getInstance().currentTransaction();	
			if(tran != null){	
				synchronized (tran) {
					tran._status = AS2TransactionStatus.RolledBack;
					AS2Transaction.releaseAllConnections();
					tran.notifyAll();	
				}
				AS2TransactionFactory.getInstance().deleteTransaction();
			}
		}
	}
}
